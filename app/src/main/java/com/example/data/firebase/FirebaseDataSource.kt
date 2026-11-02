package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.DailyEodEntity
import com.example.data.DepartmentEntity
import com.example.data.EmployeeEntity
import com.example.data.CompanyEntity
import com.example.data.Role
import com.example.data.SyncStatus
import com.example.data.TeamEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseDataSource(private val context: Context) {

  private val TAG = "FirebaseDataSource"

  // Graceful initialization: checks if FirebaseApp is configured via google-services.json
  val isFirebaseInitialized: Boolean
    get() = runCatching {
      FirebaseApp.getApps(context).isNotEmpty()
    }.getOrDefault(false)

  private val auth: FirebaseAuth?
    get() = if (isFirebaseInitialized) {
      runCatching { FirebaseAuth.getInstance() }.getOrNull()
    } else null

  private val firestore: FirebaseFirestore?
    get() = if (isFirebaseInitialized) {
      runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    } else null

  val currentUser: FirebaseUser?
    get() = auth?.currentUser

  suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
    val authInstance = auth ?: return Result.failure(IllegalStateException("Firebase is not initialized. Please connect your google-services.json."))
    return try {
      val res = authInstance.signInWithEmailAndPassword(email, pass).await()
      val user = res.user ?: throw IllegalStateException("Firebase returned null user.")
      Result.success(user)
    } catch (e: Exception) {
      Log.e(TAG, "signInWithEmail failed: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun createUserWithEmail(email: String, pass: String): Result<FirebaseUser> {
    val authInstance = auth ?: return Result.failure(IllegalStateException("Firebase is not initialized. Please connect your google-services.json."))
    return try {
      val res = authInstance.createUserWithEmailAndPassword(email, pass).await()
      val user = res.user ?: throw IllegalStateException("Firebase user creation failed.")
      Result.success(user)
    } catch (e: Exception) {
      Log.e(TAG, "createUserWithEmail failed: ${e.message}", e)
      Result.failure(e)
    }
  }

  fun signOut() {
    auth?.signOut()
  }

  // -------------------------------------------------------------
  // FIRESTORE COLLECTIONS & DOCUMENTS
  // -------------------------------------------------------------

  suspend fun saveUserProfile(
    uid: String,
    name: String,
    email: String,
    employeeId: String,
    teamId: String,
    departmentId: String,
    isNewUser: Boolean = false
  ): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val data = hashMapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "employeeId" to employeeId,
        "teamId" to teamId,
        "departmentId" to departmentId,
        "updatedAt" to System.currentTimeMillis()
      )
      
      // Only set role and isActive on new user creation
      // Existing users cannot change these fields from client
      if (isNewUser) {
        data["role"] = Role.EMPLOYEE.name
        data["isActive"] = true
      }
      
      db.collection("users").document(uid).set(data, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "saveUserProfile error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun uploadEod(eod: DailyEodEntity): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      // Deterministic document ID to prevent duplicates across retries
      val docId = "${eod.employeeId}_${eod.date}"
      val data = hashMapOf(
        "submissionId" to docId,
        "employeeId" to eod.employeeId,
        "employeeName" to eod.employeeName,
        "departmentId" to eod.departmentId,
        "departmentCode" to eod.departmentCode,
        "teamId" to (eod.teamId ?: ""), // NEW
        "companyId" to (eod.companyId ?: ""), // NEW
        "eodFormType" to (eod.eodFormType ?: "STANDARD"), // NEW
        "customFormData" to (eod.customFormData ?: ""), // NEW
        "date" to eod.date,
        "project" to eod.project,
        "todayWork" to eod.todayWork,
        "workCompleted" to eod.workCompleted,
        "workStatus" to eod.workStatus,
        "attendanceStatus" to eod.attendanceStatus,
        "blockers" to eod.blockers,
        "remarks" to eod.remarks,
        "tomorrowPlan" to eod.tomorrowPlan,
        "submissionTime" to eod.submissionTime,
        "isOnTime" to eod.isOnTime,
        "submittedAt" to eod.submittedAt,
        "updatedAt" to eod.updatedAt,
        "syncStatus" to SyncStatus.SYNCED.name
      )
      db.collection("eod_submissions").document(docId).set(data, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "uploadEod error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun fetchAllEods(): Result<List<DailyEodEntity>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("eod_submissions").get().await()
      val list = snapshot.documents.mapNotNull { doc ->
        val empId = doc.getString("employeeId") ?: return@mapNotNull null
        val date = doc.getString("date") ?: return@mapNotNull null
        DailyEodEntity(
          id = 0,
          employeeId = empId,
          employeeName = doc.getString("employeeName") ?: "",
          departmentId = doc.getString("departmentId") ?: "DEPT_ML",
          departmentCode = doc.getString("departmentCode") ?: "ML",
          teamId = doc.getString("teamId") ?: "", // NEW
          companyId = doc.getString("companyId") ?: "", // NEW
          eodFormType = doc.getString("eodFormType") ?: "STANDARD", // NEW
          customFormData = doc.getString("customFormData") ?: "", // NEW
          date = date,
          project = doc.getString("project") ?: "",
          todayWork = doc.getString("todayWork") ?: "",
          workCompleted = doc.getString("workCompleted") ?: "",
          workStatus = doc.getString("workStatus") ?: "Completed",
          attendanceStatus = doc.getString("attendanceStatus") ?: "Present",
          blockers = doc.getString("blockers") ?: "",
          remarks = doc.getString("remarks") ?: "",
          tomorrowPlan = doc.getString("tomorrowPlan") ?: "",
          submissionTime = doc.getString("submissionTime") ?: "17:45",
          isOnTime = doc.getBoolean("isOnTime") ?: true,
          syncStatus = SyncStatus.SYNCED.name,
          submittedAt = doc.getLong("submittedAt") ?: System.currentTimeMillis(),
          updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Log.e(TAG, "fetchAllEods error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun fetchEodsForEmployee(employeeId: String): Result<List<DailyEodEntity>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("eod_submissions")
        .whereEqualTo("employeeId", employeeId)
        .get().await()
      val list = snapshot.documents.mapNotNull { doc ->
        val empId = doc.getString("employeeId") ?: return@mapNotNull null
        val date = doc.getString("date") ?: return@mapNotNull null
        DailyEodEntity(
          id = 0,
          employeeId = empId,
          employeeName = doc.getString("employeeName") ?: "",
          departmentId = doc.getString("departmentId") ?: "DEPT_ML",
          departmentCode = doc.getString("departmentCode") ?: "ML",
          teamId = doc.getString("teamId") ?: "", // NEW
          companyId = doc.getString("companyId") ?: "", // NEW
          eodFormType = doc.getString("eodFormType") ?: "STANDARD", // NEW
          customFormData = doc.getString("customFormData") ?: "", // NEW
          date = date,
          project = doc.getString("project") ?: "",
          todayWork = doc.getString("todayWork") ?: "",
          workCompleted = doc.getString("workCompleted") ?: "",
          workStatus = doc.getString("workStatus") ?: "Completed",
          attendanceStatus = doc.getString("attendanceStatus") ?: "Present",
          blockers = doc.getString("blockers") ?: "",
          remarks = doc.getString("remarks") ?: "",
          tomorrowPlan = doc.getString("tomorrowPlan") ?: "",
          submissionTime = doc.getString("submissionTime") ?: "17:45",
          isOnTime = doc.getBoolean("isOnTime") ?: true,
          syncStatus = SyncStatus.SYNCED.name,
          submittedAt = doc.getLong("submittedAt") ?: System.currentTimeMillis(),
          updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Log.e(TAG, "fetchEodsForEmployee error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun uploadEmployee(emp: EmployeeEntity): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val data = hashMapOf(
        "employeeId" to emp.employeeId,
        "name" to emp.name,
        "email" to emp.email,
        "phone" to emp.phone,
        "department" to emp.department,
        "departmentId" to emp.departmentId,
        "team" to emp.team,
        "teamId" to (emp.teamId ?: ""), // NEW
        "companyId" to (emp.companyId ?: ""), // NEW
        "designation" to emp.designation,
        "defaultProject" to emp.defaultProject,
        "joiningDate" to emp.joiningDate,
        "role" to if (emp.role.equals(Role.ADMIN.name, ignoreCase = true)) Role.ADMIN.name else Role.EMPLOYEE.name,
        "isActive" to emp.isActive,
        "createdAt" to emp.createdAt,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection("employees").document(emp.employeeId).set(data, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "uploadEmployee error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun fetchEmployees(): Result<List<EmployeeEntity>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("employees").get().await()
      val list = snapshot.documents.mapNotNull { doc ->
        val empId = doc.getString("employeeId") ?: return@mapNotNull null
        
        // Handle createdAt field - can be Timestamp or Long
        val createdAtValue = try {
          val timestamp = doc.getTimestamp("createdAt")
          timestamp?.toDate()?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
          doc.getLong("createdAt") ?: System.currentTimeMillis()
        }
        
        EmployeeEntity(
          id = 0,
          employeeId = empId,
          name = doc.getString("name") ?: "",
          email = doc.getString("email") ?: "",
          phone = doc.getString("phone") ?: "",
          department = doc.getString("department") ?: "ML",
          departmentId = doc.getString("departmentId") ?: "DEPT_ML",
          team = doc.getString("team") ?: "Core Team",
          teamId = doc.getString("teamId") ?: "", // NEW
          companyId = doc.getString("companyId") ?: "", // NEW
          designation = doc.getString("designation") ?: "Engineer",
          defaultProject = doc.getString("defaultProject") ?: "Core Engineering",
          joiningDate = doc.getString("joiningDate") ?: "2024-01-01",
          role = doc.getString("role") ?: Role.EMPLOYEE.name,
          isActive = doc.getBoolean("isActive") ?: true,
          createdAt = createdAtValue
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Log.e(TAG, "fetchEmployees error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun uploadDepartment(dept: DepartmentEntity): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val data = hashMapOf(
        "deptId" to dept.deptId,
        "name" to dept.name,
        "code" to dept.code,
        "prefix" to dept.prefix,
        "description" to dept.description,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection("departments").document(dept.deptId).set(data, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun fetchDepartments(): Result<List<DepartmentEntity>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("departments").get().await()
      val list = snapshot.documents.mapNotNull { doc ->
        val deptId = doc.getString("deptId") ?: return@mapNotNull null
        DepartmentEntity(
          id = 0,
          deptId = deptId,
          name = doc.getString("name") ?: "",
          code = doc.getString("code") ?: "",
          prefix = doc.getString("prefix") ?: "",
          description = doc.getString("description") ?: ""
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun uploadTeam(team: TeamEntity): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val data = hashMapOf(
        "teamId" to team.teamId,
        "name" to team.name,
        "department" to team.department,
        "companyId" to (team.companyId ?: ""), // NEW
        "eodFormType" to (team.eodFormType ?: "STANDARD"), // NEW
        "projects" to team.projects,
        "status" to team.status,
        "description" to team.description,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection("teams").document(team.teamId).set(data, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun fetchTeams(): Result<List<TeamEntity>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("teams").get().await()
      val list = snapshot.documents.mapNotNull { doc ->
        val teamId = doc.getString("teamId") ?: return@mapNotNull null
        TeamEntity(
          id = 0,
          teamId = teamId,
          name = doc.getString("name") ?: "",
          department = doc.getString("department") ?: "",
          companyId = doc.getString("companyId") ?: "", // NEW
          eodFormType = doc.getString("eodFormType") ?: "STANDARD", // NEW
          managerId = "",
          managerName = "",
          projects = doc.getString("projects") ?: "",
          status = doc.getString("status") ?: "Active",
          description = doc.getString("description") ?: ""
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteEmployee(empId: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      db.collection("employees").document(empId).delete().await()
      Log.d(TAG, "Successfully deleted employee $empId from Firestore")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "deleteEmployee Firestore error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun deleteEod(empId: String, date: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val docId = "${empId}_${date}"
      db.collection("eod_submissions").document(docId).delete().await()
      Log.d(TAG, "Successfully deleted EOD $docId from Firestore")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "deleteEod Firestore error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun deleteEodsForEmployee(empId: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("eod_submissions")
        .whereEqualTo("employeeId", empId)
        .get().await()
      for (doc in snapshot.documents) {
        doc.reference.delete().await()
      }
      Log.d(TAG, "Successfully deleted all EODs for employee $empId from Firestore")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "deleteEodsForEmployee Firestore error: ${e.message}", e)
      Result.failure(e)
    }
  }

  fun listenToEodSubmissions(onUpdate: (List<DailyEodEntity>) -> Unit): com.google.firebase.firestore.ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("eod_submissions").addSnapshotListener { snapshot, e ->
      if (e != null || snapshot == null) return@addSnapshotListener
      val list = snapshot.documents.mapNotNull { doc ->
        val empId = doc.getString("employeeId") ?: return@mapNotNull null
        val date = doc.getString("date") ?: return@mapNotNull null
        DailyEodEntity(
          id = 0,
          employeeId = empId,
          employeeName = doc.getString("employeeName") ?: "",
          departmentId = doc.getString("departmentId") ?: "DEPT_ML",
          departmentCode = doc.getString("departmentCode") ?: "ML",
          date = date,
          project = doc.getString("project") ?: "",
          todayWork = doc.getString("todayWork") ?: "",
          workCompleted = doc.getString("workCompleted") ?: "",
          workStatus = doc.getString("workStatus") ?: "Completed",
          attendanceStatus = doc.getString("attendanceStatus") ?: "Present",
          blockers = doc.getString("blockers") ?: "",
          remarks = doc.getString("remarks") ?: "",
          tomorrowPlan = doc.getString("tomorrowPlan") ?: "",
          submissionTime = doc.getString("submissionTime") ?: "17:45",
          isOnTime = doc.getBoolean("isOnTime") ?: true,
          syncStatus = SyncStatus.SYNCED.name,
          submittedAt = doc.getLong("submittedAt") ?: System.currentTimeMillis(),
          updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
      }
      onUpdate(list)
    }
  }

  fun listenToEmployees(onUpdate: (List<EmployeeEntity>) -> Unit): com.google.firebase.firestore.ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("employees").addSnapshotListener { snapshot, e ->
      if (e != null || snapshot == null) return@addSnapshotListener
      val list = snapshot.documents.mapNotNull { doc ->
        val empId = doc.getString("employeeId") ?: return@mapNotNull null
        
        // Handle createdAt field - can be Timestamp or Long
        val createdAtValue = try {
          val timestamp = doc.getTimestamp("createdAt")
          timestamp?.toDate()?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
          doc.getLong("createdAt") ?: System.currentTimeMillis()
        }
        
        EmployeeEntity(
          id = 0,
          employeeId = empId,
          name = doc.getString("name") ?: "",
          email = doc.getString("email") ?: "",
          phone = doc.getString("phone") ?: "",
          department = doc.getString("department") ?: "ML",
          departmentId = doc.getString("departmentId") ?: "DEPT_ML",
          team = doc.getString("team") ?: "Core Team",
          designation = doc.getString("designation") ?: "Engineer",
          defaultProject = doc.getString("defaultProject") ?: "Core Engineering",
          joiningDate = doc.getString("joiningDate") ?: "2024-01-01",
          role = doc.getString("role") ?: Role.EMPLOYEE.name,
          isActive = doc.getBoolean("isActive") ?: true,
          createdAt = createdAtValue
        )
      }
      onUpdate(list)
    }
  }

  suspend fun fetchEodHistoryFromFirestore(
    startDate: String,
    endDate: String,
    employeeId: String? = null,
    limit: Long = 50,
    startAfterDoc: com.google.firebase.firestore.DocumentSnapshot? = null
  ): Result<EodHistoryQueryResult> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      // 1. Primary Query: "eod_submissions" collection (where active EODs are stored)
      var query: com.google.firebase.firestore.Query = db.collection("eod_submissions")

      if (!employeeId.isNullOrBlank() && !employeeId.equals("All", ignoreCase = true)) {
        query = query.whereEqualTo("employeeId", employeeId)
      }

      query = query.whereGreaterThanOrEqualTo("date", startDate)
        .whereLessThanOrEqualTo("date", endDate)
        .limit(limit)

      if (startAfterDoc != null) {
        query = query.startAfter(startAfterDoc)
      }

      val primaryRes = runCatching { query.get().await() }
      var snapshot = primaryRes.getOrNull()

      // 2. Fallback: If eod_submissions is empty or threw an error, query legacy "eodReports" collection
      if (snapshot == null || snapshot.isEmpty) {
        var fallbackQuery: com.google.firebase.firestore.Query = db.collection("eodReports")
        if (!employeeId.isNullOrBlank() && !employeeId.equals("All", ignoreCase = true)) {
          fallbackQuery = fallbackQuery.whereEqualTo("employeeId", employeeId)
        }
        fallbackQuery = fallbackQuery.whereGreaterThanOrEqualTo("date", startDate)
          .whereLessThanOrEqualTo("date", endDate)
          .limit(limit)

        if (startAfterDoc != null) {
          fallbackQuery = fallbackQuery.startAfter(startAfterDoc)
        }

        val fallbackRes = runCatching { fallbackQuery.get().await() }
        val fallbackSnapshot = fallbackRes.getOrNull()
        if (fallbackSnapshot != null && !fallbackSnapshot.isEmpty) {
          snapshot = fallbackSnapshot
        } else if (snapshot == null && primaryRes.isFailure) {
          // If primary query failed with exception (e.g. PERMISSION_DENIED), log warning & return failure
          val primaryErr = primaryRes.exceptionOrNull()
          Log.w(TAG, "Both eod_submissions and eodReports queries failed/empty: ${primaryErr?.message}")
          if (primaryErr != null) return Result.failure(primaryErr)
        }
      }

      val docs = snapshot?.documents ?: emptyList()
      val list = docs.mapNotNull { doc ->
        val empId = doc.getString("employeeId") ?: return@mapNotNull null
        val date = doc.getString("date") ?: return@mapNotNull null
        DailyEodEntity(
          id = 0,
          employeeId = empId,
          employeeName = doc.getString("employeeName") ?: doc.getString("name") ?: "",
          departmentId = doc.getString("departmentId") ?: "DEPT_ML",
          departmentCode = doc.getString("departmentCode") ?: "ML",
          date = date,
          project = doc.getString("project") ?: doc.getString("projectTitle") ?: "",
          todayWork = doc.getString("todayWork") ?: doc.getString("workSummary") ?: doc.getString("taskDescription") ?: "",
          workCompleted = doc.getString("workCompleted") ?: "",
          workStatus = doc.getString("workStatus") ?: doc.getString("status") ?: "Completed",
          attendanceStatus = doc.getString("attendanceStatus") ?: "Present",
          blockers = doc.getString("blockers") ?: "",
          remarks = doc.getString("remarks") ?: "",
          tomorrowPlan = doc.getString("tomorrowPlan") ?: "",
          submissionTime = doc.getString("submissionTime") ?: "17:45",
          isOnTime = doc.getBoolean("isOnTime") ?: true,
          syncStatus = SyncStatus.SYNCED.name,
          submittedAt = doc.getLong("submittedAt") ?: System.currentTimeMillis(),
          updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
      }

      // Sort in memory by date descending to avoid composite index requirement
      val sortedList = list.sortedByDescending { it.date }

      val lastDoc = docs.lastOrNull()
      val hasMore = list.size.toLong() >= limit

      Result.success(EodHistoryQueryResult(sortedList, lastDoc, hasMore))
    } catch (e: Exception) {
      Log.e(TAG, "fetchEodHistoryFromFirestore error: ${e.message}", e)
      Result.failure(e)
    }
  }

  // -------------------------------------------------------------
  // COMPANY SYNC METHODS
  // -------------------------------------------------------------

  suspend fun uploadCompany(company: CompanyEntity): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val data = hashMapOf(
        "companyId" to company.companyId,
        "name" to company.name,
        "type" to company.type,
        "parentCompanyId" to (company.parentCompanyId ?: ""),
        "updatedAt" to company.updatedAt
      )
      db.collection("companies").document(company.companyId).set(data).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "uploadCompany error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun fetchCompanies(): Result<List<CompanyEntity>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("companies").get().await()
      val list = snapshot.documents.mapNotNull { doc ->
        val companyId = doc.getString("companyId") ?: return@mapNotNull null
        CompanyEntity(
          id = 0,
          companyId = companyId,
          name = doc.getString("name") ?: "",
          type = doc.getString("type") ?: "PRIMARY",
          parentCompanyId = doc.getString("parentCompanyId")?.takeIf { it.isNotBlank() },
          updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Log.e(TAG, "fetchCompanies error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun deleteCompany(companyId: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      db.collection("companies").document(companyId).delete().await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "deleteCompany error: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * ONE-TIME ADMIN FUNCTION: Clear all employees from Firestore
   * This deletes all employee documents in batches
   */
  suspend fun clearAllEmployeesFromFirestore(): Result<Int> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      Log.d(TAG, "🗑️ Starting to delete all employees from Firestore...")
      var deletedCount = 0
      
      // Fetch all employee documents
      val snapshot = db.collection("employees").get().await()
      
      // Delete in batches (Firestore batch limit is 500)
      val batch = db.batch()
      var batchCount = 0
      
      for (doc in snapshot.documents) {
        batch.delete(doc.reference)
        batchCount++
        deletedCount++
        
        // Commit batch every 500 operations
        if (batchCount >= 500) {
          batch.commit().await()
          Log.d(TAG, "Batch committed: $deletedCount deleted so far...")
          batchCount = 0
        }
      }
      
      // Commit remaining batch
      if (batchCount > 0) {
        batch.commit().await()
      }
      
      Log.d(TAG, "✅ Successfully deleted $deletedCount employees from Firestore")
      Result.success(deletedCount)
    } catch (e: Exception) {
      Log.e(TAG, "❌ clearAllEmployeesFromFirestore error: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * ONE-TIME ADMIN FUNCTION: Upload all local employees to Firestore
   * This uploads all employee entities in batches
   */
  suspend fun uploadAllEmployeesToFirestore(employees: List<EmployeeEntity>): Result<Int> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      Log.d(TAG, "📤 Starting to upload ${employees.size} employees to Firestore...")
      var uploadedCount = 0
      
      // Upload in batches (Firestore batch limit is 500)
      val batches = employees.chunked(500)
      
      for ((batchIndex, batchEmployees) in batches.withIndex()) {
        val batch = db.batch()
        
        for (emp in batchEmployees) {
          val docRef = db.collection("employees").document(emp.employeeId)
          val empMap = hashMapOf(
            "employeeId" to emp.employeeId,
            "name" to emp.name,
            "email" to emp.email,
            "phone" to emp.phone,
            "companyId" to emp.companyId,
            "department" to emp.department,
            "departmentId" to emp.departmentId,
            "team" to emp.team,
            "teamId" to emp.teamId,
            "designation" to emp.designation,
            "role" to emp.role,
            "isActive" to emp.isActive,
            "joiningDate" to emp.joiningDate,
            "createdAt" to emp.createdAt
          )
          batch.set(docRef, empMap)
          uploadedCount++
        }
        
        batch.commit().await()
        Log.d(TAG, "Batch ${batchIndex + 1}/${batches.size} committed: $uploadedCount uploaded...")
      }
      
      Log.d(TAG, "✅ Successfully uploaded $uploadedCount employees to Firestore")
      Result.success(uploadedCount)
    } catch (e: Exception) {
      Log.e(TAG, "❌ uploadAllEmployeesToFirestore error: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Upload all teams to Firestore in batches
   */
  suspend fun uploadAllTeamsToFirestore(teams: List<TeamEntity>): Result<Int> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      Log.d(TAG, "📤 Starting to upload ${teams.size} teams to Firestore...")
      var uploadedCount = 0
      
      val batches = teams.chunked(500)
      for ((batchIndex, batchTeams) in batches.withIndex()) {
        val batch = db.batch()
        
        for (team in batchTeams) {
          val docRef = db.collection("teams").document(team.teamId)
          val teamMap = hashMapOf(
            "teamId" to team.teamId,
            "name" to team.name,
            "companyId" to team.companyId,
            "department" to team.department,
            "eodFormType" to team.eodFormType,
            "managerId" to team.managerId,
            "managerName" to team.managerName,
            "projects" to team.projects,
            "createdDate" to team.createdDate,
            "status" to team.status,
            "description" to team.description
          )
          batch.set(docRef, teamMap)
          uploadedCount++
        }
        
        batch.commit().await()
        Log.d(TAG, "Batch ${batchIndex + 1}/${batches.size} committed: $uploadedCount teams uploaded...")
      }
      
      Log.d(TAG, "✅ Successfully uploaded $uploadedCount teams to Firestore")
      Result.success(uploadedCount)
    } catch (e: Exception) {
      Log.e(TAG, "❌ uploadAllTeamsToFirestore error: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Upload all departments to Firestore in batches
   */
  suspend fun uploadAllDepartmentsToFirestore(departments: List<DepartmentEntity>): Result<Int> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      Log.d(TAG, "📤 Starting to upload ${departments.size} departments to Firestore...")
      var uploadedCount = 0
      
      val batches = departments.chunked(500)
      for ((batchIndex, batchDepts) in batches.withIndex()) {
        val batch = db.batch()
        
        for (dept in batchDepts) {
          val docRef = db.collection("departments").document(dept.deptId)
          val deptMap = hashMapOf(
            "deptId" to dept.deptId,
            "name" to dept.name,
            "code" to dept.code,
            "prefix" to dept.prefix,
            "managerIds" to dept.managerIds,
            "description" to dept.description
          )
          batch.set(docRef, deptMap)
          uploadedCount++
        }
        
        batch.commit().await()
        Log.d(TAG, "Batch ${batchIndex + 1}/${batches.size} committed: $uploadedCount departments uploaded...")
      }
      
      Log.d(TAG, "✅ Successfully uploaded $uploadedCount departments to Firestore")
      Result.success(uploadedCount)
    } catch (e: Exception) {
      Log.e(TAG, "❌ uploadAllDepartmentsToFirestore error: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Check if employees have already been uploaded to Firestore (tracked in SharedPreferences)
   */
  fun areEmployeesUploadedToFirestore(): Boolean {
    val prefs = context.getSharedPreferences("workcore_sync", android.content.Context.MODE_PRIVATE)
    return prefs.getBoolean("employees_uploaded_to_firebase", false)
  }

  /**
   * Mark employees as uploaded to Firestore (stored in SharedPreferences)
   */
  fun markEmployeesUploadedToFirestore(uploaded: Boolean) {
    val prefs = context.getSharedPreferences("workcore_sync", android.content.Context.MODE_PRIVATE)
    prefs.edit().putBoolean("employees_uploaded_to_firebase", uploaded).apply()
  }
}

// Data class for EOD history query result with pagination support
data class EodHistoryQueryResult(
  val eods: List<DailyEodEntity>,
  val lastDocument: com.google.firebase.firestore.DocumentSnapshot?,
  val hasMore: Boolean
)
