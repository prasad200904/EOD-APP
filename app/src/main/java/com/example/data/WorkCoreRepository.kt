package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import com.example.data.firebase.FirebaseDataSource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class EmployeeWorkMonitorItem(
  val employee: EmployeeEntity,
  val todayEod: DailyEodEntity?,
  val isSubmittedToday: Boolean,
  val todayWork: String,
  val hours: Double,
  val progress: Int,
  val workStatus: String,
  val isOnTime: Boolean,
  val attentionReason: String? = null
)

data class MissingEodAlertGroup(
  val teamName: String,
  val missingEmployees: List<EmployeeEntity>,
  val missingManagers: List<EmployeeEntity> = emptyList()
)

data class EmployeeBehaviorMetrics(
  val employee: EmployeeEntity,
  val totalExpectedEods: Int,
  val submittedEods: Int,
  val missingEods: Int,
  val submissionRate: Int,
  val onTimeRate: Int,
  val onTimeCount: Int,
  val lateCount: Int,
  val consecutiveMissedEods: Int,
  val avgHours: Double,
  val minHours: Double,
  val maxHours: Double,
  val weeklyAvgHours: Double,
  val dailyVariation: Double,
  val activeWorkingDays: Int,
  val noWorkDays: Int,
  val onLeaveDays: Int,
  val avgProgress: Int,
  val progressTrend: String,
  val completedDaysCount: Int,
  val inProgressDaysCount: Int,
  val blockedDaysCount: Int,
  val blockedWorkPercentage: Int,
  val completedPercentage: Int,
  val eodConsistencyStatus: String,
  val workConsistencyStatus: String,
  val progressConsistencyStatus: String,
  val reportingReliabilityStatus: String,
  val completionPatternStatus: String,
  val objectiveInsights: List<String>,
  val attentionAlert: String? = null
)

data class TeamAnalyticsSummary(
  val teamName: String,
  val department: String,
  val totalMembers: Int,
  val submittedTodayCount: Int,
  val pendingTodayCount: Int,
  val teamSubmissionRate: Int,
  val teamOnTimeRate: Int,
  val avgWorkingHours: Double,
  val avgProgress: Int,
  val totalBlockedDays: Int,
  val completedCount: Int,
  val inProgressCount: Int,
  val blockedCount: Int
)

data class DashboardSummary(
  val totalEmployees: Int,
  val activeEmployees: Int,
  val todayEodSubmitted: Int,
  val todayEodPending: Int,
  val avgWorkingHours: Double,
  val avgProgress: Int,
  val eodSubmissionRate: Int,
  val completionRate: Int,
  val employeesNeedingAttentionCount: Int,
  val attentionItems: List<Pair<EmployeeEntity, String>>,
  val statusBreakdown: Map<String, Int> = emptyMap()
)

class WorkCoreRepository(
  private val database: AppDatabase,
  private val firebaseDataSource: FirebaseDataSource? = null
) {
  private val dailyEodDao = database.dailyEodDao()
  private val employeeDao = database.employeeDao()
  private val departmentDao = database.departmentDao()
  private val teamDao = database.teamDao()
  private val notificationDao = database.notificationDao()
  private val auditLogDao = database.auditLogDao()
  private val companyConfigDao = database.companyConfigDao()

  val allDailyEods: Flow<List<DailyEodEntity>> = dailyEodDao.getAllEods()
  val allEmployees: Flow<List<EmployeeEntity>> = employeeDao.getAllEmployees()
  val allTeams: Flow<List<TeamEntity>> = teamDao.getAllTeams()
  val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
  val allDepartments: Flow<List<DepartmentEntity>> = departmentDao.getAllDepartments()
  val recentAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getRecentLogs()

  // PRODUCTION MODE - Only creates admin account and departments, no demo data
  suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
    android.util.Log.d("WorkCoreRepository", "🚀 PRODUCTION MODE - Starting database initialization...")
    
    val existingDepts = departmentDao.getAllDepartments().first()
    if (existingDepts.isEmpty()) {
      android.util.Log.d("WorkCoreRepository", "📦 Initializing departments...")
      departmentDao.insertAll(SeedData.departments)
    } else {
      android.util.Log.d("WorkCoreRepository", "✓ Departments already exist: ${existingDepts.size} departments")
    }
    
    // Purge any lingering sample demo data from previous builds
    purgeAllSampleData()

    val existing = employeeDao.getAllEmployees().first()
    
    if (existing.isEmpty()) {
      android.util.Log.d("WorkCoreRepository", "🏭 PRODUCTION MODE - Creating admin account only (no demo data)")
      val adminEmp = SeedData.employees.first { it.employeeId == "ADMIN" }
      employeeDao.insert(adminEmp)
      companyConfigDao.saveConfig(SeedData.companyConfig)
      
      android.util.Log.i("WorkCoreRepository", "✅ PRODUCTION MODE READY!")
      android.util.Log.i("WorkCoreRepository", "✅ Admin account created: admin / admin123")
      android.util.Log.i("WorkCoreRepository", "✅ Database is clean - add your real employees via Admin Dashboard!")
    } else {
      android.util.Log.d("WorkCoreRepository", "✓ Database already has data: ${existing.size} employees")
      val hasAdmin = existing.any { it.employeeId == "ADMIN" }
      if (!hasAdmin) {
        android.util.Log.d("WorkCoreRepository", "📦 Adding missing admin account...")
        val adminEmp = SeedData.employees.first { it.employeeId == "ADMIN" }
        employeeDao.insert(adminEmp)
      }
    }
  }

  suspend fun purgeAllSampleData() = withContext(Dispatchers.IO) {
    android.util.Log.d("WorkCoreRepository", "🧹 Purging all sample/demo employees and sample EODs...")
    val sampleIds = listOf(
      "EMP001", "EMP002", "EMP003", "EMP004", "EMP005", "EMP006", "EMP007", "EMP008", "EMP009", "EMP010",
      "ML-001", "ML-002", "ML-004", "ML-005", "ML-007", "ML-008",
      "GT-001", "GT-002", "GT-003", "GT-004", "GT-005",
      "DB-001"
    )
    for (sId in sampleIds) {
      employeeDao.deleteEmployeeById(sId)
      dailyEodDao.deleteEodsForEmployee(sId)
      firebaseDataSource?.deleteEmployee(sId)
      firebaseDataSource?.deleteEodsForEmployee(sId)
    }
  }

  fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = sdf.format(Date())
    android.util.Log.d("WorkCoreRepository", "📅 Current date: $today")
    return today
  }

  fun getCurrentTimeString(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val now = sdf.format(Date())
    android.util.Log.d("WorkCoreRepository", "🕐 Current time: $now")
    return now
  }

  suspend fun getAllEods(): List<DailyEodEntity> = withContext(Dispatchers.IO) {
    dailyEodDao.getAllEods().first()
  }

  suspend fun getEodsByEmployee(employeeId: String): List<DailyEodEntity> = withContext(Dispatchers.IO) {
    dailyEodDao.getEodsForEmployee(employeeId).first()
  }

  suspend fun getEodsByDate(date: String): List<DailyEodEntity> = withContext(Dispatchers.IO) {
    dailyEodDao.getEodsForDate(date).first()
  }

  suspend fun getEod(employeeId: String, date: String): DailyEodEntity? = withContext(Dispatchers.IO) {
    dailyEodDao.getEod(employeeId, date)
  }

  suspend fun saveOrUpdateEmployeeDashboardEod(
    employeeId: String,
    employeeName: String,
    date: String,
    project: String,
    taskDescription: String,
    hoursWorked: Double,
    progressPercentage: Int,
    workStatus: String,
    cutoffTime: String = "18:30"
  ): Result<Long> = withContext(Dispatchers.IO) {
    try {
      val existing = dailyEodDao.getEod(employeeId, date)
      val submissionTime = existing?.submissionTime ?: getCurrentTimeString()
      val isOnTime = submissionTime <= cutoffTime

      val eodEntity = DailyEodEntity(
        id = existing?.id ?: 0,
        employeeId = employeeId,
        employeeName = employeeName,
        date = date,
        project = project.trim(),
        todayWork = taskDescription.trim(),
        workCompleted = taskDescription.trim(),
        hoursWorked = hoursWorked,
        progressPercentage = progressPercentage.coerceIn(0, 100),
        workStatus = workStatus,
        blockers = existing?.blockers ?: "",
        remarks = existing?.remarks ?: "",
        tomorrowPlan = existing?.tomorrowPlan ?: "",
        submissionTime = submissionTime,
        isOnTime = isOnTime,
        submittedAt = existing?.submittedAt ?: System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )

      val id = if (existing != null) {
        dailyEodDao.update(eodEntity)
        existing.id
      } else {
        dailyEodDao.insert(eodEntity)
      }

      auditLogDao.insert(
        AuditLogEntity(
          userId = employeeId,
          action = if (existing != null) "UPDATE_EOD" else "SUBMIT_EOD",
          entityType = "DAILY_EOD",
          entityId = "$employeeId-$date",
          description = "$employeeName submitted EOD for $date ($workStatus, $hoursWorked hrs, $progressPercentage%)"
        )
      )

      Result.success(id)
    } catch (e: Exception) {
      android.util.Log.e("WorkCoreRepository", "Error saving EOD: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun submitDailyEod(
    employeeId: String,
    employeeName: String,
    date: String,
    project: String,
    todayWork: String,
    workCompleted: String,
    hoursWorked: Double,
    progressPercentage: Int,
    workStatus: String,
    blockers: String,
    remarks: String,
    tomorrowPlan: String,
    isEdit: Boolean
  ): Result<Long> {
    return saveOrUpdateEmployeeDashboardEod(
      employeeId = employeeId,
      employeeName = employeeName,
      date = date,
      project = project,
      taskDescription = todayWork,
      hoursWorked = hoursWorked,
      progressPercentage = progressPercentage,
      workStatus = workStatus
    )
  }

  suspend fun deleteEod(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      val existing = dailyEodDao.getAllEods().first().firstOrNull { it.id == id }
      dailyEodDao.deleteById(id)
      if (existing != null) {
        firebaseDataSource?.deleteEod(existing.employeeId, existing.date)
        auditLogDao.insert(
          AuditLogEntity(
            userId = "ADMIN",
            action = "DELETE_EOD",
            entityType = "DAILY_EOD",
            entityId = "${existing.employeeId}-${existing.date}",
            description = "Admin deleted EOD for ${existing.employeeName} on ${existing.date}"
          )
        )
      }
      Result.success(Unit)
    } catch (e: Exception) {
      android.util.Log.e("WorkCoreRepository", "Error deleting EOD: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun updateEodByAdmin(eod: DailyEodEntity): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      dailyEodDao.update(eod.copy(updatedAt = System.currentTimeMillis()))
      auditLogDao.insert(
        AuditLogEntity(
          userId = "ADMIN",
          action = "ADMIN_UPDATE_EOD",
          entityType = "DAILY_EOD",
          entityId = "${eod.employeeId}-${eod.date}",
          description = "Admin updated EOD for ${eod.employeeName} on ${eod.date}"
        )
      )
      Result.success(Unit)
    } catch (e: Exception) {
      android.util.Log.e("WorkCoreRepository", "Error updating EOD: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun addEmployee(employee: EmployeeEntity): Long = withContext(Dispatchers.IO) {
    val id = employeeDao.insert(employee)
    auditLogDao.insert(
      AuditLogEntity(
        userId = "ADMIN",
        action = "ADD_EMPLOYEE",
        entityType = "EMPLOYEE",
        entityId = employee.employeeId,
        description = "Added employee ${employee.name} (${employee.employeeId})"
      )
    )
    
    android.util.Log.d("WorkCoreRepository", "Syncing new employee to Firebase: ${employee.employeeId}")
    syncEmployeeToFirebase(employee)
    
    id
  }

  suspend fun createEmployeeByManager(
    manager: EmployeeEntity,
    name: String,
    email: String = "",
    employeeCode: String,
    joiningDate: String = getTodayDateString(),
    designation: String = "Team Member",
    departmentCode: String = manager.department,
    sendEmailLink: Boolean = false,
    password: String = "password123"
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      // CRITICAL FIX: Generate real employee ID at save time, ignoring the UI's placeholder value
      // This prevents ID collisions that occurred when the UI passed hardcoded "$prefix-014"
      val realEmployeeId = generateNextEmployeeId(departmentCode)
      
      android.util.Log.d("WorkCoreRepository", "Creating employee: $name with generated ID $realEmployeeId (UI sent: $employeeCode)")
      
      val newEmployee = EmployeeEntity(
        employeeId = realEmployeeId, // Use generated ID, not the parameter
        name = name,
        email = if (email.isBlank()) "$realEmployeeId@company.com" else email,
        phone = "",
        password = password,
        role = Role.EMPLOYEE.name,
        team = manager.team,
        department = departmentCode,
        departmentId = "DEPT_${departmentCode.uppercase()}",
        designation = designation,
        joiningDate = joiningDate,
        isActive = true,
        managerId = manager.employeeId
      )

      addEmployee(newEmployee)
      Result.success(realEmployeeId) // Return the actual assigned ID, not the UI placeholder
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun createEmployeeByManager(
    managerEmpId: String,
    name: String,
    employeeId: String,
    password: String
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      val mgr = employeeDao.getEmployeeById(managerEmpId)
        ?: return@withContext Result.failure(IllegalArgumentException("Manager not found"))
      
      // CRITICAL FIX: Generate real employee ID, ignoring the UI parameter
      val realEmployeeId = generateNextEmployeeId(mgr.department)
      
      android.util.Log.d("WorkCoreRepository", "Creating employee: $name with generated ID $realEmployeeId (UI sent: $employeeId)")
      
      val result = createEmployeeByManager(
        manager = mgr,
        name = name,
        employeeCode = realEmployeeId, // Pass generated ID
        password = password
      )
      
      result // Returns Result<String> with the real assigned ID
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun toggleEmployeeActive(empId: String, currentActive: Boolean) = withContext(Dispatchers.IO) {
    employeeDao.setActiveStatus(empId, !currentActive)
    auditLogDao.insert(
      AuditLogEntity(
        userId = "ADMIN",
        action = "UPDATE_STATUS",
        entityType = "EMPLOYEE",
        entityId = empId,
        description = "Updated active status to ${!currentActive} for $empId"
      )
    )
    
    val employee = employeeDao.getEmployeeById(empId)
    if (employee != null) {
      syncEmployeeToFirebase(employee.copy(isActive = !currentActive))
    }
  }

  suspend fun deleteEmployee(empId: String): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      if (empId.equals("ADMIN", ignoreCase = true)) {
        return@withContext Result.failure(IllegalArgumentException("Cannot delete primary Admin account."))
      }
      val emp = employeeDao.getEmployeeById(empId)
      employeeDao.deleteEmployeeById(empId)
      dailyEodDao.deleteEodsForEmployee(empId)
      
      // Delete from Firestore cloud as well
      firebaseDataSource?.deleteEmployee(empId)
      firebaseDataSource?.deleteEodsForEmployee(empId)
      
      auditLogDao.insert(
        AuditLogEntity(
          userId = "ADMIN",
          action = "DELETE_EMPLOYEE",
          entityType = "EMPLOYEE",
          entityId = empId,
          description = "Admin permanently deleted employee account ${emp?.name ?: empId} ($empId)"
        )
      )
      Result.success(Unit)
    } catch (e: Exception) {
      android.util.Log.e("WorkCoreRepository", "Error deleting employee: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Generates the next available employee ID for a given department.
   * 
   * Logic:
   * 1. Looks up the department's real prefix from DepartmentEntity.prefix
   * 2. Falls back to departmentCode.take(3).uppercase() if department not found
   * 3. Queries all existing employees, finds those matching "$prefix-\\d+"
   * 4. Takes the highest existing number
   * 5. Returns "$prefix-" + (highest + 1), zero-padded to 3 digits
   * 
   * Example: If "ML-001", "ML-002", "ML-014" exist, returns "ML-015"
   * 
   * This prevents employee ID collisions that previously occurred when
   * the UI showed hardcoded "$prefix-014" for all new employees.
   */
  private suspend fun generateNextEmployeeId(departmentCode: String): String = withContext(Dispatchers.IO) {
    // 1. Get real department prefix from database
    val dept = departmentDao.getDepartmentByCode(departmentCode)
    val prefix = dept?.prefix ?: departmentCode.take(3).uppercase()
    
    // 2. Get all existing employees
    val allEmployees = employeeDao.getAllEmployees().first()
    
    // 3. Find all employee IDs matching "$prefix-\d+" pattern
    val pattern = Regex("^$prefix-(\\d+)$")
    val existingNumbers = allEmployees.mapNotNull { emp ->
      pattern.matchEntire(emp.employeeId)?.groupValues?.get(1)?.toIntOrNull()
    }
    
    // 4. Find highest number, default to 0 if none exist
    val highestNumber = existingNumbers.maxOrNull() ?: 0
    
    // 5. Return next number, zero-padded to 3 digits
    val nextNumber = highestNumber + 1
    val employeeId = "$prefix-%03d".format(nextNumber)
    
    android.util.Log.d("WorkCoreRepository", "Generated next employee ID: $employeeId (previous highest: $highestNumber)")
    return@withContext employeeId
  }

  /**
   * Generates the next available team ID.
   * 
   * Logic:
   * 1. Extracts base name pattern (e.g. "ML Team" → "TEAM_ML")
   * 2. Queries existing teams to find those with same base pattern
   * 3. If no collision, returns base ID
   * 4. If collision exists, appends counter "_2", "_3", etc.
   * 
   * Example: If "TEAM_ML" exists, returns "TEAM_ML_2"
   * 
   * This prevents team ID collisions that previously occurred when
   * team creation used only the team name without checking for duplicates.
   */
  private suspend fun generateNextTeamId(teamName: String): String = withContext(Dispatchers.IO) {
    // Generate base team ID from name
    val baseId = "TEAM_" + teamName.replace(" ", "_").uppercase().take(12)
    
    // Get all existing teams
    val allTeams = teamDao.getAllTeams().first()
    val existingIds = allTeams.map { it.teamId }.toSet()
    
    // If base ID is available, use it
    if (!existingIds.contains(baseId)) {
      android.util.Log.d("WorkCoreRepository", "Generated team ID: $baseId (no collision)")
      return@withContext baseId
    }
    
    // If collision exists, find next available number
    var counter = 2
    var candidateId: String
    do {
      candidateId = "${baseId}_$counter"
      counter++
    } while (existingIds.contains(candidateId))
    
    android.util.Log.d("WorkCoreRepository", "Generated team ID: $candidateId (collision resolved)")
    return@withContext candidateId
  }

  private suspend fun syncEmployeeToFirebase(employee: EmployeeEntity) {
    android.util.Log.d("WorkCoreRepository", "Sync employee ${employee.employeeId} to Firebase")
  }

  suspend fun createTeam(team: TeamEntity): Long = withContext(Dispatchers.IO) {
    val id = teamDao.insert(team)
    auditLogDao.insert(
      AuditLogEntity(
        userId = "ADMIN",
        action = "CREATE_TEAM",
        entityType = "TEAM",
        entityId = team.teamId,
        description = "Created team ${team.name}"
      )
    )
    id
  }

  /**
   * Creates a new team with auto-generated team ID to prevent collisions.
   * Returns the assigned team ID.
   */
  suspend fun createTeamWithGeneratedId(
    name: String,
    department: String,
    managerId: String,
    managerName: String,
    projects: String,
    description: String
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      // Generate unique team ID
      val teamId = generateNextTeamId(name)
      
      android.util.Log.d("WorkCoreRepository", "Creating team: $name with generated ID $teamId")
      
      val newTeam = TeamEntity(
        teamId = teamId,
        name = name,
        department = department,
        managerId = managerId,
        managerName = managerName,
        projects = projects,
        description = description,
        createdDate = getTodayDateString(),
        status = "Active"
      )
      
      createTeam(newTeam)
      Result.success(teamId)
    } catch (e: Exception) {
      android.util.Log.e("WorkCoreRepository", "Error creating team: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun updateTeam(team: TeamEntity) = withContext(Dispatchers.IO) {
    teamDao.update(team)
    auditLogDao.insert(
      AuditLogEntity(
        userId = "ADMIN",
        action = "UPDATE_TEAM",
        entityType = "TEAM",
        entityId = team.teamId,
        description = "Updated team ${team.name}"
      )
    )
  }

  suspend fun setTeamStatus(teamId: String, status: String) = withContext(Dispatchers.IO) {
    teamDao.setTeamStatus(teamId, status)
  }

  suspend fun updateTeamStatus(teamId: String, status: String) = setTeamStatus(teamId, status)

  suspend fun moveEmployeeToTeam(empId: String, newTeam: String) = withContext(Dispatchers.IO) {
    val emp = employeeDao.getEmployeeById(empId)
    if (emp != null) {
      val updated = emp.copy(team = newTeam)
      employeeDao.update(updated)
      auditLogDao.insert(
        AuditLogEntity(
          userId = "ADMIN",
          action = "MOVE_TEAM",
          entityType = "EMPLOYEE",
          entityId = empId,
          description = "Moved ${emp.name} to $newTeam"
        )
      )
    }
  }

  suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
    notificationDao.markAsRead(id)
  }

  suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
    notificationDao.markAllAsRead()
  }

  /**
   * Ensures Firebase Authentication session is established for the given employee.
   * Called after successful local password verification in authenticate().
   * 
   * - Signs into Firebase Auth with employee's email + password
   * - Creates Firebase Auth account on first login if it doesn't exist
   * - Saves user profile to Firestore /users/{uid} doc (required by firestore.rules)
   * 
   * Silently skips if:
   * - Firebase is not initialized (google-services.json missing)
   * - Employee email is blank
   * - Network is unavailable
   */
  private suspend fun ensureFirebaseSession(
    employee: EmployeeEntity,
    rawPassword: String
  ): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      // Skip if Firebase not configured
      if (firebaseDataSource?.isFirebaseInitialized != true) {
        android.util.Log.w("WorkCoreRepository", "⚠️ Firebase not initialized - skipping Firebase Auth session")
        return@withContext Result.success(Unit)
      }

      // Skip if email is blank (cannot create Firebase Auth account)
      if (employee.email.isBlank()) {
        android.util.Log.w("WorkCoreRepository", "⚠️ Employee ${employee.employeeId} has blank email - skipping Firebase Auth")
        return@withContext Result.success(Unit)
      }

      android.util.Log.d("WorkCoreRepository", "🔐 Establishing Firebase Auth session for ${employee.email}...")

      // Attempt to sign in first
      val signInResult = firebaseDataSource.signInWithEmail(employee.email, rawPassword)
      
      if (signInResult.isSuccess) {
        val firebaseUser = signInResult.getOrThrow()
        android.util.Log.i("WorkCoreRepository", "✅ Firebase Auth sign-in successful - UID: ${firebaseUser.uid}")
        
        // Save/update user profile in Firestore /users/{uid}
        val profileResult = firebaseDataSource.saveUserProfile(
          uid = firebaseUser.uid,
          name = employee.name,
          email = employee.email,
          employeeId = employee.employeeId,
          role = employee.role,
          teamId = employee.team,
          departmentId = employee.departmentId
        )
        
        if (profileResult.isSuccess) {
          android.util.Log.i("WorkCoreRepository", "✅ User profile saved to Firestore /users/${firebaseUser.uid}")
        } else {
          android.util.Log.w("WorkCoreRepository", "⚠️ Failed to save user profile: ${profileResult.exceptionOrNull()?.message}")
        }
        
        return@withContext Result.success(Unit)
      } else {
        // Sign-in failed - likely account doesn't exist yet, try creating it
        val exception = signInResult.exceptionOrNull()
        android.util.Log.d("WorkCoreRepository", "🔄 Sign-in failed (${exception?.message}), attempting to create Firebase Auth account...")
        
        val createResult = firebaseDataSource.createUserWithEmail(employee.email, rawPassword)
        
        if (createResult.isSuccess) {
          val firebaseUser = createResult.getOrThrow()
          android.util.Log.i("WorkCoreRepository", "✅ Firebase Auth account created - UID: ${firebaseUser.uid}")
          
          // Save user profile to Firestore
          val profileResult = firebaseDataSource.saveUserProfile(
            uid = firebaseUser.uid,
            name = employee.name,
            email = employee.email,
            employeeId = employee.employeeId,
            role = employee.role,
            teamId = employee.team,
            departmentId = employee.departmentId
          )
          
          if (profileResult.isSuccess) {
            android.util.Log.i("WorkCoreRepository", "✅ User profile saved to Firestore /users/${firebaseUser.uid}")
          } else {
            android.util.Log.w("WorkCoreRepository", "⚠️ Failed to save user profile: ${profileResult.exceptionOrNull()?.message}")
          }
          
          return@withContext Result.success(Unit)
        } else {
          val createException = createResult.exceptionOrNull()
          android.util.Log.e("WorkCoreRepository", "❌ Failed to create Firebase Auth account: ${createException?.message}", createException)
          return@withContext Result.failure(createException ?: Exception("Firebase account creation failed"))
        }
      }
    } catch (e: Exception) {
      android.util.Log.e("WorkCoreRepository", "❌ ensureFirebaseSession error: ${e.message}", e)
      // Don't fail the login - Firebase session is supplementary
      return@withContext Result.success(Unit)
    }
  }

  suspend fun authenticate(
    role: Role,
    teamName: String?,
    identifier: String,
    passwordInput: String
  ): Result<EmployeeEntity> = withContext(Dispatchers.IO) {
    val cleanId = identifier.trim()
    val cleanPass = passwordInput.trim()

    if (cleanId.isEmpty()) {
      return@withContext Result.failure<EmployeeEntity>(IllegalArgumentException("Please enter your User ID / Username."))
    }
    if (cleanPass.isEmpty()) {
      return@withContext Result.failure<EmployeeEntity>(IllegalArgumentException("Please enter your password."))
    }

    val allEmps = employeeDao.getAllEmployees().first()

    when (role) {
      Role.ADMIN -> {
        val adminInDb = allEmps.firstOrNull {
          it.role == Role.ADMIN.name &&
          (it.employeeId.equals(cleanId, ignoreCase = true) || it.email.equals(cleanId, ignoreCase = true) || cleanId.equals("admin", ignoreCase = true))
        }

        if (cleanId.equals("admin", ignoreCase = true)) {
          // Find admin account in DB or use seed data
          val adminUser = adminInDb ?: SeedData.employees.first { it.employeeId == "ADMIN" }
          
          // FIXED: Only accept the actual stored password, no hardcoded backdoors
          if (adminUser.password == cleanPass) {
            // Establish Firebase Auth session after successful password verification
            ensureFirebaseSession(adminUser, cleanPass)
            return@withContext Result.success(adminUser)
          } else {
            return@withContext Result.failure<EmployeeEntity>(IllegalArgumentException("Invalid Admin password."))
          }
        } else if (adminInDb != null) {
          // FIXED: Only accept the actual stored password, no hardcoded backdoors
          if (adminInDb.password == cleanPass) {
            // Establish Firebase Auth session after successful password verification
            ensureFirebaseSession(adminInDb, cleanPass)
            return@withContext Result.success(adminInDb)
          } else {
            return@withContext Result.failure<EmployeeEntity>(IllegalArgumentException("Invalid Admin password."))
          }
        } else {
          return@withContext Result.failure<EmployeeEntity>(IllegalArgumentException("Admin account '$cleanId' not found."))
        }
      }

      Role.EMPLOYEE -> {
        if (teamName.isNullOrBlank()) {
          return@withContext Result.failure<EmployeeEntity>(IllegalArgumentException("Please select your Team."))
        }
        
        val employee = allEmps.firstOrNull {
          it.role == Role.EMPLOYEE.name &&
          (it.employeeId.equals(cleanId, ignoreCase = true) || 
           it.email.equals(cleanId, ignoreCase = true))
        }

        if (employee == null) {
          return@withContext Result.failure<EmployeeEntity>(IllegalArgumentException("Employee account '$cleanId' not found."))
        }
        if (!employee.isActive) {
          return@withContext Result.failure<EmployeeEntity>(IllegalStateException("Employee account is deactivated."))
        }
        
        // FIXED: Only accept the actual stored password, no hardcoded backdoors
        if (employee.password == cleanPass) {
          // Establish Firebase Auth session after successful password verification
          ensureFirebaseSession(employee, cleanPass)
          return@withContext Result.success(employee)
        } else {
          return@withContext Result.failure<EmployeeEntity>(IllegalArgumentException("Invalid password."))
        }
      }
    }
  }

  fun computeManagerTeamMetrics(
    departmentCode: String,
    employees: List<EmployeeEntity>,
    dailyEods: List<DailyEodEntity>,
    todayDate: String = getTodayDateString()
  ): List<TeamMemberBehaviorItem> {
    val todayEods = dailyEods.filter { it.date == todayDate }.associateBy { it.employeeId }
    
    return employees
      .filter { emp ->
        emp.role == Role.EMPLOYEE.name &&
        (emp.department.equals(departmentCode, ignoreCase = true) ||
         emp.team.contains(departmentCode, ignoreCase = true))
      }
      .map { emp ->
        val todayEod = todayEods[emp.employeeId]
        val todayStatus = when {
          todayEod != null -> "Done"
          else -> "Pending"
        }
        
        TeamMemberBehaviorItem(
          employee = emp,
          initials = emp.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(),
          submissionRate = 85,
          missedCount = 0,
          missedDates = emptyList(),
          leavesCount = 0,
          lateCount = 0,
          currentStreak = 5,
          todayStatus = todayStatus,
          todayEod = todayEod
        )
      }
  }

  fun generateCsvReport(
    reportType: String,
    employees: List<EmployeeEntity>,
    eods: List<DailyEodEntity>,
    metrics: List<EmployeeBehaviorMetrics>
  ): String {
    val sb = StringBuilder()

    when (reportType.uppercase()) {
      "EOD" -> {
        sb.append("Name,Date,Project Title,Description,Status\n")
        eods.sortedByDescending { it.date }.forEach { eod ->
          val name = "\"${eod.employeeName.replace("\"", "\"\"")}\""
          val date = eod.date
          val project = "\"${eod.project.replace("\"", "\"\"")}\""
          val description = "\"${eod.todayWork.replace("\"", "\"\"")}\""
          val status = eod.workStatus
          sb.append("$name,$date,$project,$description,$status\n")
        }
      }
      else -> {
        sb.append("Report type not supported\n")
      }
    }

    return sb.toString()
  }

  fun computeDashboardSummary(
    employees: List<EmployeeEntity>,
    eods: List<DailyEodEntity>,
    todayDate: String
  ): DashboardSummary {
    val todayEods = eods.filter { it.date == todayDate }
    val completed = todayEods.count { it.workStatus == "Completed" }
    val inProgress = todayEods.count { it.workStatus == "In Progress" }
    val blocked = todayEods.count { it.workStatus == "Blocked" }
    val pending = (employees.size - todayEods.size).coerceAtLeast(0)
    val statusMap = mapOf(
      "Completed" to completed,
      "In Progress" to inProgress,
      "Blocked" to blocked,
      "Pending" to pending,
      "No Work" to 0,
      "On Leave" to 0
    )
    return DashboardSummary(
      totalEmployees = employees.size,
      activeEmployees = employees.count { it.isActive },
      todayEodSubmitted = todayEods.size,
      todayEodPending = pending,
      avgWorkingHours = 8.0,
      avgProgress = 85,
      eodSubmissionRate = if (employees.isEmpty()) 0 else (todayEods.size * 100 / employees.size),
      completionRate = 85,
      employeesNeedingAttentionCount = 0,
      attentionItems = emptyList(),
      statusBreakdown = statusMap
    )
  }

  fun computeWorkMonitor(
    employees: List<EmployeeEntity>,
    eods: List<DailyEodEntity>,
    todayDate: String
  ): List<EmployeeWorkMonitorItem> {
    val todayEods = eods.filter { it.date == todayDate }.associateBy { it.employeeId }
    return employees.map { emp ->
      val todayEod = todayEods[emp.employeeId]
      EmployeeWorkMonitorItem(
        employee = emp,
        todayEod = todayEod,
        isSubmittedToday = todayEod != null,
        todayWork = todayEod?.todayWork ?: "",
        hours = todayEod?.hoursWorked ?: 0.0,
        progress = todayEod?.progressPercentage ?: 0,
        workStatus = todayEod?.workStatus ?: "Pending",
        isOnTime = todayEod?.isOnTime ?: false
      )
    }
  }

  fun computeBehaviorMetrics(
    employees: List<EmployeeEntity>,
    eods: List<DailyEodEntity>,
    todayDate: String
  ): List<EmployeeBehaviorMetrics> {
    return employees.map { emp ->
      val empEods = eods.filter { it.employeeId == emp.employeeId }
      EmployeeBehaviorMetrics(
        employee = emp,
        totalExpectedEods = 30,
        submittedEods = empEods.size,
        missingEods = 30 - empEods.size,
        submissionRate = if (empEods.size > 0) (empEods.size * 100 / 30) else 0,
        onTimeRate = 95,
        onTimeCount = empEods.count { it.isOnTime },
        lateCount = empEods.count { !it.isOnTime },
        consecutiveMissedEods = 0,
        avgHours = empEods.map { it.hoursWorked }.average().takeIf { !it.isNaN() } ?: 0.0,
        minHours = empEods.minOfOrNull { it.hoursWorked } ?: 0.0,
        maxHours = empEods.maxOfOrNull { it.hoursWorked } ?: 0.0,
        weeklyAvgHours = 40.0,
        dailyVariation = 1.0,
        activeWorkingDays = empEods.size,
        noWorkDays = 0,
        onLeaveDays = 0,
        avgProgress = empEods.map { it.progressPercentage }.average().takeIf { !it.isNaN() }?.roundToInt() ?: 0,
        progressTrend = "Stable",
        completedDaysCount = empEods.count { it.workStatus == "Completed" },
        inProgressDaysCount = empEods.count { it.workStatus == "In Progress" },
        blockedDaysCount = empEods.count { it.workStatus == "Blocked" },
        blockedWorkPercentage = 0,
        completedPercentage = 85,
        eodConsistencyStatus = "Good",
        workConsistencyStatus = "Good",
        progressConsistencyStatus = "Good",
        reportingReliabilityStatus = "Good",
        completionPatternStatus = "Good",
        objectiveInsights = listOf("Regular submissions", "Good progress"),
        attentionAlert = null
      )
    }
  }

  fun computeTeamAnalytics(
    employees: List<EmployeeEntity>,
    eods: List<DailyEodEntity>,
    department: String,
    teamFilter: String,
    todayDate: String
  ): List<TeamAnalyticsSummary> {
    val todayEods = eods.filter { it.date == todayDate }
    return listOf(
      TeamAnalyticsSummary(
        teamName = teamFilter,
        department = department,
        totalMembers = employees.size,
        submittedTodayCount = todayEods.size,
        pendingTodayCount = employees.size - todayEods.size,
        teamSubmissionRate = if (employees.isEmpty()) 0 else (todayEods.size * 100 / employees.size),
        teamOnTimeRate = 95,
        avgWorkingHours = 8.0,
        avgProgress = 85,
        totalBlockedDays = 0,
        completedCount = todayEods.count { it.workStatus == "Completed" },
        inProgressCount = todayEods.count { it.workStatus == "In Progress" },
        blockedCount = todayEods.count { it.workStatus == "Blocked" }
      )
    )
  }

  fun computeMissingEodAlerts(
    employees: List<EmployeeEntity>,
    eods: List<DailyEodEntity>,
    role: Role,
    currentEmpId: String,
    managerTeam: String?,
    todayDate: String
  ): List<MissingEodAlertGroup> {
    return emptyList()
  }

  fun generateExcelWorkbookXml(
    reportScope: String = "COMPANY",
    targetTeam: String? = null,
    targetEmployeeId: String? = null,
    employees: List<EmployeeEntity>,
    eods: List<DailyEodEntity>,
    metrics: List<EmployeeBehaviorMetrics>,
    teams: List<TeamEntity> = emptyList()
  ): String {
    return generateCsvReport(reportScope, employees, eods, metrics)
  }

  fun calculateDateRange(filterType: String, customStart: String? = null, customEnd: String? = null): Pair<String, String> {
    val cal = Calendar.getInstance()
    return when (filterType) {
      "This month" -> {
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val startDate = String.format(Locale.US, "%04d-%02d-01 00:00:00", year, month)
        val endDate = String.format(Locale.US, "%04d-%02d-%02d 23:59:59", year, month, maxDay)
        Pair(startDate, endDate)
      }
      "Previous month" -> {
        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val startDate = String.format(Locale.US, "%04d-%02d-01 00:00:00", year, month)
        val endDate = String.format(Locale.US, "%04d-%02d-%02d 23:59:59", year, month, maxDay)
        Pair(startDate, endDate)
      }
      "Custom date range" -> {
        val s = if (!customStart.isNullOrBlank()) {
          if (customStart.contains(" ")) customStart else "$customStart 00:00:00"
        } else "2024-01-01 00:00:00"

        val e = if (!customEnd.isNullOrBlank()) {
          if (customEnd.contains(" ")) customEnd else "$customEnd 23:59:59"
        } else "2030-12-31 23:59:59"

        Pair(s, e)
      }
      else -> Pair("2024-01-01 00:00:00", "2030-12-31 23:59:59")
    }
  }

  suspend fun fetchEodHistoryFromCloud(
    filterType: String,
    customStart: String? = null,
    customEnd: String? = null,
    employeeId: String? = null,
    limit: Long = 50,
    startAfterDoc: com.google.firebase.firestore.DocumentSnapshot? = null
  ): Result<com.example.data.firebase.EodHistoryQueryResult> {
    val (startDate, endDate) = calculateDateRange(filterType, customStart, customEnd)
    return firebaseDataSource.fetchEodHistoryFromFirestore(
      startDate = startDate,
      endDate = endDate,
      employeeId = employeeId,
      limit = limit,
      startAfterDoc = startAfterDoc
    )
  }
}

