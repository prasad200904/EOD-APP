package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.DailyEodEntity
import com.example.data.EmployeeEntity
import com.example.data.Role
import com.example.data.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncManager(
  private val context: Context,
  private val database: AppDatabase,
  private val firebaseDataSource: FirebaseDataSource = FirebaseDataSource(context),
  val networkMonitor: NetworkMonitor = NetworkMonitor(context)
) {

  private val TAG = "SyncManager"
  private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  private val dailyEodDao = database.dailyEodDao()
  private val employeeDao = database.employeeDao()
  private val departmentDao = database.departmentDao()
  private val teamDao = database.teamDao()

  private val _syncState = MutableStateFlow(SyncStatus.SYNCED)
  val syncState: StateFlow<SyncStatus> = _syncState.asStateFlow()

  private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
  val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

  private val _pendingSyncCount = MutableStateFlow(0)
  val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

  init {
    // Start listening to network connectivity changes
    syncScope.launch {
      networkMonitor.isOnline.collect { isOnline ->
        if (isOnline) {
          Log.d(TAG, "Network became online - triggering auto-sync")
          syncAllPending()
        }
      }
    }

    // Periodically update pending count
    updatePendingCount()
    
    // Start periodic background sync (every 30 seconds)
    startPeriodicSync()

    // Start real-time Firestore snapshot listeners for live updates
    startRealtimeListeners()
  }

  fun updatePendingCount() {
    syncScope.launch {
      runCatching {
        val pending = dailyEodDao.getEodsBySyncStatus(SyncStatus.PENDING.name)
        val failed = dailyEodDao.getEodsBySyncStatus(SyncStatus.FAILED.name)
        _pendingSyncCount.value = pending.size + failed.size
      }
    }
  }

  /**
   * Called immediately after saving an EOD to Room locally.
   * If online and Firebase is configured, immediately uploads.
   * If offline or Firebase is unconfigured, remains marked as PENDING.
   */
  fun triggerEodSync(eod: DailyEodEntity) {
    syncScope.launch {
      if (!networkMonitor.isConnected() || !firebaseDataSource.isFirebaseInitialized) {
        dailyEodDao.updateSyncStatus(eod.employeeId, eod.date, SyncStatus.PENDING.name)
        updatePendingCount()
        return@launch
      }

      _syncState.value = SyncStatus.SYNCING
      dailyEodDao.updateSyncStatus(eod.employeeId, eod.date, SyncStatus.SYNCING.name)

      val uploadResult = firebaseDataSource.uploadEod(eod)
      if (uploadResult.isSuccess) {
        dailyEodDao.updateSyncStatus(eod.employeeId, eod.date, SyncStatus.SYNCED.name)
        _syncState.value = SyncStatus.SYNCED
        _lastSyncTimestamp.value = System.currentTimeMillis()
        Log.d(TAG, "Successfully synced EOD for ${eod.employeeId} on ${eod.date}")
      } else {
        dailyEodDao.updateSyncStatus(eod.employeeId, eod.date, SyncStatus.FAILED.name)
        _syncState.value = SyncStatus.FAILED
        Log.w(TAG, "Failed to sync EOD: ${uploadResult.exceptionOrNull()?.message}")
      }
      updatePendingCount()
    }
  }

  /**
   * Sync all pending/failed records from Room to Firestore, and pull latest records.
   */
  suspend fun syncAllPending(): Result<Unit> = withContext(Dispatchers.IO) {
    if (!networkMonitor.isConnected()) {
      return@withContext Result.failure(IllegalStateException("No internet connection available"))
    }
    if (!firebaseDataSource.isFirebaseInitialized) {
      return@withContext Result.failure(IllegalStateException("Firebase is not initialized (google-services.json not configured)"))
    }

    // Verify Firebase Authentication state before proceeding
    val currentUser = firebaseDataSource.currentUser
    if (currentUser == null) {
      Log.e(TAG, "❌ SyncManager cannot proceed - user not authenticated with Firebase Auth")
      Log.e(TAG, "❌ request.auth will be null - Firestore security rules will reject all operations")
      Log.e(TAG, "❌ Ensure authenticate() calls ensureFirebaseSession() after successful login")
      return@withContext Result.failure(IllegalStateException("User not authenticated with Firebase Auth - sync blocked by security rules"))
    }
    
    Log.d(TAG, "🔐 Firebase Auth verified - UID: ${currentUser.uid}, Email: ${currentUser.email}")

    _syncState.value = SyncStatus.SYNCING
    try {
      Log.d(TAG, "🔄 Starting full sync - uploading pending data and pulling cloud updates...")
      
      // 1. Upload Pending EODs
      val pendingList = dailyEodDao.getEodsBySyncStatus(SyncStatus.PENDING.name)
      val failedList = dailyEodDao.getEodsBySyncStatus(SyncStatus.FAILED.name)
      val toUpload = pendingList + failedList

      if (toUpload.isNotEmpty()) {
        Log.d(TAG, "📤 Uploading ${toUpload.size} pending EODs to Firebase...")
      }
      
      for (eod in toUpload) {
        dailyEodDao.updateSyncStatus(eod.employeeId, eod.date, SyncStatus.SYNCING.name)
        val res = firebaseDataSource.uploadEod(eod)
        if (res.isSuccess) {
          dailyEodDao.updateSyncStatus(eod.employeeId, eod.date, SyncStatus.SYNCED.name)
        } else {
          dailyEodDao.updateSyncStatus(eod.employeeId, eod.date, SyncStatus.FAILED.name)
        }
      }

      // 2. Download latest EOD submissions from Firestore to Room
      Log.d(TAG, "📥 Pulling latest EODs from Firebase...")
      val cloudEodsRes = firebaseDataSource.fetchAllEods()
      if (cloudEodsRes.isSuccess) {
        val cloudEods = cloudEodsRes.getOrNull().orEmpty()
        val cloudEodKeys = cloudEods.map { "${it.employeeId}_${it.date}" }.toSet()
        val localEods = dailyEodDao.getAllEods().first()

        // Purge local synced EODs deleted from cloud
        for (local in localEods) {
          if (local.syncStatus == SyncStatus.SYNCED.name && !cloudEodKeys.contains("${local.employeeId}_${local.date}")) {
            dailyEodDao.deleteById(local.id)
          }
        }

        for (cloudEod in cloudEods) {
          val local = dailyEodDao.getEod(cloudEod.employeeId, cloudEod.date)
          // If doesn't exist locally or cloud is newer, insert/update
          if (local == null || cloudEod.updatedAt >= local.updatedAt) {
            dailyEodDao.insert(cloudEod.copy(id = local?.id ?: 0, syncStatus = SyncStatus.SYNCED.name))
          }
        }
        Log.d(TAG, "✅ EODs synced successfully")
      }

      // 3. Upload local employees to Firebase (for new employees added on this device)
      Log.d(TAG, "📤 Uploading local employees to Firebase...")
      val localEmployees = employeeDao.getAllEmployees().first()
      for (emp in localEmployees) {
        firebaseDataSource.uploadEmployee(emp)
      }
      Log.d(TAG, "✅ Uploaded ${localEmployees.size} employees to Firebase")

      // 4. Download cloud employees and merge with local
      Log.d(TAG, "📥 Pulling latest employees from Firebase...")
      val cloudEmpsRes = firebaseDataSource.fetchEmployees()
      if (cloudEmpsRes.isSuccess) {
        val cloudEmps = cloudEmpsRes.getOrNull().orEmpty()
        val cloudEmpIds = cloudEmps.map { it.employeeId }.toSet()
        val localEmps = employeeDao.getAllEmployees().first()

        // Purge local employees deleted from cloud (except ADMIN)
        for (local in localEmps) {
          if (!local.employeeId.equals("ADMIN", true) && !cloudEmpIds.contains(local.employeeId)) {
            employeeDao.deleteEmployeeById(local.employeeId)
            dailyEodDao.deleteEodsForEmployee(local.employeeId)
          }
        }

        var newCount = 0
        var updatedCount = 0
        
        for (cloudEmp in cloudEmps) {
          val localEmp = employeeDao.getEmployeeById(cloudEmp.employeeId)
          if (localEmp == null) {
            // New employee from another device
            employeeDao.insert(cloudEmp)
            newCount++
            Log.d(TAG, "➕ New employee from cloud: ${cloudEmp.name} (${cloudEmp.employeeId})")
          } else if (cloudEmp.createdAt > localEmp.createdAt || cloudEmp.isActive != localEmp.isActive) {
            // Cloud version is newer, update local
            employeeDao.update(cloudEmp.copy(id = localEmp.id))
            updatedCount++
            Log.d(TAG, "🔄 Updated employee from cloud: ${cloudEmp.name} (${cloudEmp.employeeId})")
          }
        }
        
        if (newCount > 0 || updatedCount > 0) {
          Log.i(TAG, "✅ Employee sync: $newCount new, $updatedCount updated")
        } else {
          Log.d(TAG, "✅ Employees already in sync")
        }
      }

      // 5. Upload local departments to Firebase
      val localDepts = departmentDao.getAllDepartments().first()
      for (dept in localDepts) {
        firebaseDataSource.uploadDepartment(dept)
      }

      // 6. Download cloud departments
      val cloudDeptsRes = firebaseDataSource.fetchDepartments()
      if (cloudDeptsRes.isSuccess) {
        val cloudDepts = cloudDeptsRes.getOrNull().orEmpty()
        for (cloudDept in cloudDepts) {
          val localDept = departmentDao.getDepartmentByCode(cloudDept.code)
          if (localDept == null) {
            departmentDao.insert(cloudDept)
          }
        }
      }

      // 7. Upload local teams to Firebase
      val localTeams = teamDao.getAllTeams().first()
      for (team in localTeams) {
        firebaseDataSource.uploadTeam(team)
      }

      // 8. Download cloud teams
      val cloudTeamsRes = firebaseDataSource.fetchTeams()
      if (cloudTeamsRes.isSuccess) {
        val cloudTeams = cloudTeamsRes.getOrNull().orEmpty()
        for (cloudTeam in cloudTeams) {
          val localTeam = teamDao.getTeamById(cloudTeam.teamId)
          if (localTeam == null) {
            teamDao.insert(cloudTeam)
          }
        }
      }

      _syncState.value = SyncStatus.SYNCED
      _lastSyncTimestamp.value = System.currentTimeMillis()
      updatePendingCount()
      
      Log.i(TAG, "✅ Full sync completed successfully!")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "syncAllPending error: ${e.message}", e)
      _syncState.value = SyncStatus.FAILED
      updatePendingCount()
      Result.failure(e)
    }
  }

  suspend fun uploadEmployeeToCloud(emp: EmployeeEntity): Result<Unit> = withContext(Dispatchers.IO) {
    if (!networkMonitor.isConnected() || !firebaseDataSource.isFirebaseInitialized) {
      return@withContext Result.success(Unit) // stored in Room safely
    }
    firebaseDataSource.uploadEmployee(emp)
  }

  fun startMonitoring(scope: CoroutineScope) {
    scope.launch {
      networkMonitor.isOnline.collect { isOnline ->
        if (isOnline) {
          syncAllPending()
        }
      }
    }
  }

  fun syncPendingToCloud() {
    syncScope.launch {
      syncAllPending()
    }
  }

  suspend fun pullFromCloud(): Result<Unit> = withContext(Dispatchers.IO) {
    syncAllPending()
  }

  /**
   * Start periodic background sync every 30 seconds
   */
  private fun startPeriodicSync() {
    syncScope.launch {
      while (true) {
        kotlinx.coroutines.delay(30000) // 30 seconds
        if (networkMonitor.isConnected() && firebaseDataSource.isFirebaseInitialized) {
          Log.d(TAG, "⏰ Periodic sync triggered (every 30s)")
          syncAllPending()
        }
      }
    }
  }

  fun startRealtimeListeners() {
    if (!firebaseDataSource.isFirebaseInitialized) return
    Log.d(TAG, "⚡ Starting Firestore realtime listeners for instant updates...")

    firebaseDataSource.listenToEmployees { cloudEmps ->
      syncScope.launch(Dispatchers.IO) {
        runCatching {
          val cloudIds = cloudEmps.map { it.employeeId }.toSet()
          val localEmps = employeeDao.getAllEmployees().first()

          // Purge local employees no longer in Firestore (except ADMIN)
          for (local in localEmps) {
            if (!local.employeeId.equals("ADMIN", true) && !cloudIds.contains(local.employeeId)) {
              employeeDao.deleteEmployeeById(local.employeeId)
              dailyEodDao.deleteEodsForEmployee(local.employeeId)
              Log.d(TAG, "⚡ Realtime sync removed employee ${local.employeeId} from Room")
            }
          }

          // Insert or update employees
          for (cloudEmp in cloudEmps) {
            val localEmp = employeeDao.getEmployeeById(cloudEmp.employeeId)
            if (localEmp == null) {
              employeeDao.insert(cloudEmp)
            } else if (cloudEmp.createdAt > localEmp.createdAt || cloudEmp.isActive != localEmp.isActive) {
              employeeDao.update(cloudEmp.copy(id = localEmp.id))
            }
          }
        }
      }
    }

    firebaseDataSource.listenToEodSubmissions { cloudEods ->
      syncScope.launch(Dispatchers.IO) {
        runCatching {
          val cloudKeys = cloudEods.map { "${it.employeeId}_${it.date}" }.toSet()
          val localEods = dailyEodDao.getAllEods().first()

          // Purge local EODs no longer in Firestore
          for (local in localEods) {
            if (local.syncStatus == SyncStatus.SYNCED.name && !cloudKeys.contains("${local.employeeId}_${local.date}")) {
              dailyEodDao.deleteById(local.id)
              Log.d(TAG, "⚡ Realtime sync removed EOD for ${local.employeeId} on ${local.date}")
            }
          }

          // Insert or update EODs
          for (cloudEod in cloudEods) {
            val local = dailyEodDao.getEod(cloudEod.employeeId, cloudEod.date)
            if (local == null) {
              dailyEodDao.insert(cloudEod.copy(id = 0, syncStatus = SyncStatus.SYNCED.name))
            } else if (cloudEod.updatedAt >= local.updatedAt) {
              dailyEodDao.update(cloudEod.copy(id = local.id, syncStatus = SyncStatus.SYNCED.name))
            }
          }
        }
      }
    }
  }
}
