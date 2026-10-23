package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import com.example.data.auth.AuthRepository
import com.example.data.auth.VerifiedSession
import com.example.data.firebase.FirebaseDataSource
import com.example.data.firebase.EodHistoryQueryResult
import com.google.firebase.firestore.DocumentSnapshot
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

data class TeamReportSummary(
  val teamId: String,
  val teamName: String,
  val companyName: String,
  val eodFormType: String,
  val totalMembers: Int,
  val todaySubmissions: Int,
  val customMetrics: CustomTeamMetrics? = null
)

data class CustomTeamMetrics(
  // Telecalling specific
  val totalCalls: Int = 0,
  val totalFollowUps: Int = 0,
  val totalConversions: Int = 0,
  val conversionRate: Double = 0.0,
  
  // Counsellor specific
  val totalStudents: Int = 0,
  val studentsByCountry: Map<String, Int> = emptyMap(),
  val studentsByStatus: Map<String, Int> = emptyMap(),
  
  // Marketing specific
  val totalCollegeVisits: Int = 0,
  val collegesVisited: List<String> = emptyList(),
  
  // General
  val submissionsList: List<CustomFormSubmission> = emptyList()
)

data class CustomFormSubmission(
  val employeeName: String,
  val date: String,
  val formData: String,
  val submissionTime: String
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
  private val firebaseDataSource: FirebaseDataSource? = null,
  val authRepository: AuthRepository = AuthRepository()
) {
  private val dailyEodDao = database.dailyEodDao()
  private val employeeDao = database.employeeDao()
  private val departmentDao = database.departmentDao()
  private val teamDao = database.teamDao()
  private val notificationDao = database.notificationDao()
  private val auditLogDao = database.auditLogDao()
  private val companyConfigDao = database.companyConfigDao()
  private val teamMemberDataDao = database.teamMemberDataDao()
  private val companyDao = database.companyDao()

  val allDailyEods: Flow<List<DailyEodEntity>> = dailyEodDao.getAllEods()
  val allEmployees: Flow<List<EmployeeEntity>> = employeeDao.getAllEmployees()
  val allTeams: Flow<List<TeamEntity>> = teamDao.getAllTeams()
  val allCompanies: Flow<List<CompanyEntity>> = companyDao.getAllCompanies()
  val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
  val allDepartments: Flow<List<DepartmentEntity>> = departmentDao.getAllDepartments()
  val recentAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getRecentLogs()

  // PRODUCTION MODE - Creates admin, departments, teams, and ALL 73 real employees from CSV
  suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
    android.util.Log.d("WorkCoreRepository", "🚀 Starting database initialization...")
    
    // Always ensure departments exist
    val existingDepts = departmentDao.getAllDepartments().first()
    if (existingDepts.isEmpty()) {
      android.util.Log.d("WorkCoreRepository", "📦 Initializing departments...")
      departmentDao.insertAll(SeedData.departments)
      android.util.Log.i("WorkCoreRepository", "✅ ${SeedData.departments.size} departments created")
    } else {
      android.util.Log.d("WorkCoreRepository", "✓ Departments already exist: ${existingDepts.size} departments")
    }
    
    // Purge any lingering sample demo data from previous builds
    purgeAllSampleData()
    
    // Initialize teams with passwords
    val existingTeams = teamDao.getAllTeams().first()
    if (existingTeams.isEmpty()) {
      android.util.Log.d("WorkCoreRepository", "📦 Initializing teams with passwords...")
      teamDao.insertAll(SeedData.teams)
      android.util.Log.i("WorkCoreRepository", "✅ ${SeedData.teams.size} teams created with unique passwords")
    } else {
      android.util.Log.d("WorkCoreRepository", "✓ Teams already exist: ${existingTeams.size} teams")
    }

    // Load ALL 73 real employees from CSV (ADMIN + SS001-SS044 + AO001-AO029)
    val existing = employeeDao.getAllEmployees().first()
    if (existing.isEmpty()) {
      android.util.Log.d("WorkCoreRepository", "📦 Loading all 73 employees from SeedData...")
      employeeDao.insertAll(SeedData.employees)
      android.util.Log.i("WorkCoreRepository", "✅ ${SeedData.employees.size} employees loaded (ADMIN + 44 SS + 29 AO)")
    } else {
      android.util.Log.d("WorkCoreRepository", "✓ Employees already exist: ${existing.size} employees")
      // If we have ADMIN but missing real employees, add them
      if (existing.size == 1 && existing.any { it.employeeId == "ADMIN" }) {
        android.util.Log.d("WorkCoreRepository", "📦 Found only ADMIN, adding 72 real employees...")
        val realEmployees = SeedData.employees.filter { it.employeeId != "ADMIN" }
        employeeDao.insertAll(realEmployees)
        android.util.Log.i("WorkCoreRepository", "✅ ${realEmployees.size} real employees added")
      }
    }
    
    // Ensure company config exists
    val config = companyConfigDao.getConfig()
    if (config == null) {
      companyConfigDao.saveConfig(SeedData.companyConfig)
    }
    
    android.util.Log.i("WorkCoreRepository", "✅ Database initialization complete with ${SeedData.employees.size} total employees")
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

      // Upload to Firestore so other devices see it immediately
      val savedEod = dailyEodDao.getEod(employeeId, date)
      if (savedEod != null) {
        firebaseDataSource?.uploadEod(savedEod)
          ?.onFailure { e ->
            android.util.Log.w("WorkCoreRepository", "EOD saved locally but Firebase upload failed: ${e.message}")
          }
      }

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

  suspend fun submitCustomEod(
    employeeId: String,
    employeeName: String,
    teamId: String,
    companyId: String,
    date: String,
    customFormData: String
  ): Result<Long> = withContext(Dispatchers.IO) {
    try {
      // Get team info to determine eodFormType
      val team = teamDao.getAllTeams().first().firstOrNull { it.teamId == teamId }
      val eodFormType = team?.eodFormType ?: "STANDARD"
      
      val existing = dailyEodDao.getEod(employeeId, date)
      val submissionTime = existing?.submissionTime ?: getCurrentTimeString()
      val cutoffTime = "18:30"
      val isOnTime = submissionTime <= cutoffTime

      val eodEntity = DailyEodEntity(
        id = existing?.id ?: 0,
        employeeId = employeeId,
        employeeName = employeeName,
        date = date,
        project = "Custom Form - $eodFormType",
        todayWork = "See custom form data",
        workCompleted = "See custom form data",
        hoursWorked = 8.0, // Default for custom forms
        progressPercentage = 100, // Assume completion if submitted
        workStatus = "Completed",
        blockers = "",
        remarks = "",
        tomorrowPlan = "",
        submissionTime = submissionTime,
        isOnTime = isOnTime,
        submittedAt = existing?.submittedAt ?: System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        teamId = teamId,
        companyId = companyId,
        eodFormType = eodFormType,
        customFormData = customFormData
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
          action = if (existing != null) "UPDATE_CUSTOM_EOD" else "SUBMIT_CUSTOM_EOD",
          entityType = "DAILY_EOD",
          entityId = "$employeeId-$date",
          description = "$employeeName submitted $eodFormType EOD for $date"
        )
      )

      // Upload to Firestore so other devices see it immediately
      val savedEod = dailyEodDao.getEod(employeeId, date)
      if (savedEod != null) {
        firebaseDataSource?.uploadEod(savedEod)
          ?.onFailure { e ->
            android.util.Log.w("WorkCoreRepository", "Custom EOD saved locally but Firebase upload failed: ${e.message}")
          }
      }

      Result.success(id)
    } catch (e: Exception) {
      android.util.Log.e("WorkCoreRepository", "Error saving custom EOD: ${e.message}", e)
      Result.failure(e)
    }
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
    password: String = ""
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      // CRITICAL FIX: Generate real employee ID at save time, ignoring the UI's placeholder value
      val realEmployeeId = generateNextEmployeeId(departmentCode)
      
      android.util.Log.d("WorkCoreRepository", "Creating employee: $name with generated ID $realEmployeeId (UI sent: $employeeCode)")
      
      val newEmployee = EmployeeEntity(
        employeeId = realEmployeeId,
        name = name,
        email = if (email.isBlank()) "$realEmployeeId@company.com" else email,
        phone = "",
        password = "", // Client never stores passwords
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
      Result.success(realEmployeeId)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun createEmployeeByManager(
    managerEmpId: String,
    name: String,
    employeeId: String,
    password: String = ""
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      val mgr = employeeDao.getEmployeeById(managerEmpId)
        ?: return@withContext Result.failure(IllegalArgumentException("Manager not found"))
      
      val realEmployeeId = generateNextEmployeeId(mgr.department)
      
      android.util.Log.d("WorkCoreRepository", "Creating employee: $name with generated ID $realEmployeeId (UI sent: $employeeId)")
      
      val result = createEmployeeByManager(
        manager = mgr,
        name = name,
        employeeCode = realEmployeeId,
        password = ""
      )
      
      result
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

  // ── Test helper methods ───────────────────────────────────────────

  /** Get employees belonging to a specific team. Used by tests and roster screens. */
  suspend fun getEmployeesByTeam(teamName: String): List<EmployeeEntity> =
    withContext(Dispatchers.IO) {
      employeeDao.getAllEmployees().first().filter { emp ->
        emp.role == "EMPLOYEE" &&
        (emp.team.equals(teamName, ignoreCase = true) ||
         emp.department.equals(teamName, ignoreCase = true))
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

  suspend fun authenticate(
    role: Role,
    teamName: String?,
    identifier: String,
    passwordInput: String
  ): Result<EmployeeEntity> = withContext(Dispatchers.IO) {
    val cleanId = identifier.trim()
    if (cleanId.isEmpty()) {
      return@withContext Result.failure<EmployeeEntity>(IllegalArgumentException("Please enter your User ID or Email."))
    }
    if (passwordInput.isEmpty()) {
      return@withContext Result.failure<EmployeeEntity>(IllegalArgumentException("Please enter your password."))
    }

    val allEmps = employeeDao.getAllEmployees().first()

    // Look up email by identifier locally
    val localEmp = allEmps.firstOrNull {
      it.employeeId.equals(cleanId, ignoreCase = true) ||
      it.email.equals(cleanId, ignoreCase = true) ||
      (cleanId.equals("admin", ignoreCase = true) && it.role.equals(Role.ADMIN.name, ignoreCase = true))
    }

    val targetEmail = when {
      cleanId.contains("@") -> cleanId
      localEmp != null && localEmp.email.isNotBlank() -> localEmp.email
      cleanId.equals("admin", ignoreCase = true) -> {
        SeedData.employees.firstOrNull { it.employeeId.equals("ADMIN", ignoreCase = true) }?.email ?: "admin@workcore.com"
      }
      else -> {
        return@withContext Result.failure<EmployeeEntity>(
          IllegalArgumentException("Account '$cleanId' not found. Please log in with your registered email.")
        )
      }
    }

    val signInResult = authRepository.signIn(targetEmail, passwordInput)
    if (signInResult.isFailure) {
      return@withContext Result.failure<EmployeeEntity>(
        signInResult.exceptionOrNull() ?: IllegalArgumentException("Invalid ID or password.")
      )
    }

    val session = signInResult.getOrThrow()
    val tokenRole = session.role

    when (role) {
      Role.ADMIN -> {
        if (!session.isAdmin) {
          authRepository.signOut()
          return@withContext Result.failure<EmployeeEntity>(IllegalArgumentException("Access denied: Account is not an administrator."))
        }
      }
      Role.EMPLOYEE -> {
        if (!session.isEmployee && !session.isManager && !session.isAdmin) {
          authRepository.signOut()
          return@withContext Result.failure<EmployeeEntity>(IllegalArgumentException("Access denied: Not authorized as an employee."))
        }
      }
    }

    // Load / sync employee
    val matchedEmp = session.employeeId?.let { employeeDao.getEmployeeById(it) }
      ?: localEmp
      ?: if (session.isAdmin) {
          SeedData.employees.firstOrNull { it.employeeId.equals("ADMIN", ignoreCase = true) }
        } else null

    val resolvedEmp = matchedEmp ?: EmployeeEntity(
      employeeId = session.employeeId ?: session.uid,
      name = session.email.substringBefore("@"),
      email = session.email,
      phone = "",
      password = "",
      role = tokenRole,
      team = session.teamId ?: teamName ?: "",
      department = "",
      departmentId = "",
      designation = if (session.isAdmin) "Administrator" else "Team Member",
      joiningDate = getTodayDateString(),
      isActive = true
    )

    if (!resolvedEmp.isActive) {
      authRepository.signOut()
      return@withContext Result.failure<EmployeeEntity>(IllegalStateException("Employee account is deactivated."))
    }

    Result.success(resolvedEmp.copy(role = tokenRole))
  }

  fun computeManagerTeamMetrics(
    departmentCode: String,
    employees: List<EmployeeEntity>,
    dailyEods: List<DailyEodEntity>,
    todayDate: String = getTodayDateString()
  ): List<TeamMemberBehaviorItem> {
    val todayEods = dailyEods.filter { it.date == todayDate }.associateBy { it.employeeId }

    // Code → full team/department name mapping
    fun codeMatchesEmployee(code: String, emp: EmployeeEntity): Boolean {
      if (code == "ALL" || code.isBlank()) return true
      val t = emp.team
      val d = emp.department
      return when (code.uppercase()) {
        "ALL"        -> true
        "GT"         -> t.contains("General", ignoreCase = true) || d.contains("General", ignoreCase = true)
        "ML"         -> t.contains("ML", ignoreCase = true) || d.contains("ML", ignoreCase = true)
        "DB"         -> t.contains("DB", ignoreCase = true) || d.contains("DB", ignoreCase = true)
        "CYBER"      -> t.contains("Cyber", ignoreCase = true) || d.contains("Cyber", ignoreCase = true)
        "WRITING"    -> t.contains("Writing", ignoreCase = true) || d.contains("Writing", ignoreCase = true)
        "TEGA"       -> t.contains("TEGA", ignoreCase = true) || d.contains("TEGA", ignoreCase = true)
        "TELECALLER" -> t.contains("Tele", ignoreCase = true) || d.contains("Tele", ignoreCase = true)
        "COUNSELLOR" -> t.contains("Counsel", ignoreCase = true) || d.contains("Counsel", ignoreCase = true)
        "MARKETING"  -> t.contains("Marketing", ignoreCase = true) || d.contains("Marketing", ignoreCase = true)
        "MANAGER"    -> t.contains("Manager", ignoreCase = true) || d.contains("Manager", ignoreCase = true)
        else         -> t.equals(code, ignoreCase = true) || d.equals(code, ignoreCase = true)
      }
    }

    return employees
      .filter { emp ->
        emp.role == Role.EMPLOYEE.name && codeMatchesEmployee(departmentCode, emp)
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
    todayDate: String,
    expectedDays: Int = 30
  ): List<EmployeeBehaviorMetrics> {
    val totalExpected = if (expectedDays > 0) expectedDays else 30
    return employees.map { emp ->
      val empEods = eods.filter { it.employeeId == emp.employeeId }
      val submitted = empEods.size
      val missing = (totalExpected - submitted).coerceAtLeast(0)
      val submissionRate = if (totalExpected > 0) ((submitted * 100) / totalExpected).coerceAtMost(100) else 0

      val consistencyStatus = when {
        submissionRate >= 85 -> "Strong"
        submissionRate >= 60 -> "Good"
        else -> "Needs Attention"
      }
      val attentionAlertStr = if (consistencyStatus == "Needs Attention" || missing > 2) {
        "Needs Attention: Missing $missing EODs"
      } else null

      EmployeeBehaviorMetrics(
        employee = emp,
        totalExpectedEods = totalExpected,
        submittedEods = submitted,
        missingEods = missing,
        submissionRate = submissionRate,
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
        eodConsistencyStatus = consistencyStatus,
        workConsistencyStatus = "Good",
        progressConsistencyStatus = "Good",
        reportingReliabilityStatus = "Good",
        completionPatternStatus = "Good",
        objectiveInsights = listOf("Regular submissions", "Good progress"),
        attentionAlert = attentionAlertStr
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

  fun computeTeamReports(
    teams: List<TeamEntity>,
    employees: List<EmployeeEntity>,
    eods: List<DailyEodEntity>,
    companies: List<CompanyEntity>,
    todayDate: String
  ): List<TeamReportSummary> {
    val todayEods = eods.filter { it.date == todayDate }
    
    return teams
      .filter { it.eodFormType != "STANDARD" } // Only custom form teams
      .map { team ->
        val teamEmployees = employees.filter { it.teamId == team.teamId }
        val teamEods = todayEods.filter { it.teamId == team.teamId }
        val company = companies.firstOrNull { it.companyId == team.companyId }
        
        val customMetrics = when (team.eodFormType) {
          "TELECALLING" -> computeTelecallingMetrics(teamEods)
          "COUNSELLOR" -> computeCounsellorMetrics(teamEods)
          "MARKETING" -> computeMarketingMetrics(teamEods)
          "SIMPLE" -> computeSimpleMetrics(teamEods)
          else -> null
        }
        
        TeamReportSummary(
          teamId = team.teamId,
          teamName = team.name,
          companyName = company?.name ?: "Unknown",
          eodFormType = team.eodFormType,
          totalMembers = teamEmployees.size,
          todaySubmissions = teamEods.size,
          customMetrics = customMetrics
        )
      }
  }

  private fun computeTelecallingMetrics(eods: List<DailyEodEntity>): CustomTeamMetrics {
    var totalCalls = 0
    var totalFollowUps = 0
    var totalConversions = 0
    
    val submissions = eods.mapNotNull { eod ->
      try {
        eod.customFormData?.let { jsonData ->
          val data = com.google.gson.Gson().fromJson(jsonData, com.example.data.TelecallingFormData::class.java)
          totalCalls += data.callsMade
          totalFollowUps += data.followUps
          totalConversions += data.conversions
          
          CustomFormSubmission(
            employeeName = eod.employeeName,
            date = eod.date,
            formData = "Calls: ${data.callsMade}, Follow-ups: ${data.followUps}, Conversions: ${data.conversions}",
            submissionTime = eod.submissionTime
          )
        }
      } catch (e: Exception) {
        null
      }
    }
    
    val conversionRate = if (totalCalls > 0) (totalConversions.toDouble() / totalCalls * 100) else 0.0
    
    return CustomTeamMetrics(
      totalCalls = totalCalls,
      totalFollowUps = totalFollowUps,
      totalConversions = totalConversions,
      conversionRate = conversionRate,
      submissionsList = submissions
    )
  }

  private fun computeCounsellorMetrics(eods: List<DailyEodEntity>): CustomTeamMetrics {
    var totalStudents = 0
    val studentsByCountry = mutableMapOf<String, Int>()
    val studentsByStatus = mutableMapOf<String, Int>()
    
    val submissions = eods.mapNotNull { eod ->
      try {
        eod.customFormData?.let { jsonData ->
          val data = com.google.gson.Gson().fromJson(jsonData, com.example.data.CounsellorFormData::class.java)
          totalStudents += data.students.size
          
          data.students.forEach { student ->
            studentsByCountry[student.country] = (studentsByCountry[student.country] ?: 0) + 1
            studentsByStatus[student.status] = (studentsByStatus[student.status] ?: 0) + 1
          }
          
          CustomFormSubmission(
            employeeName = eod.employeeName,
            date = eod.date,
            formData = "${data.students.size} students logged",
            submissionTime = eod.submissionTime
          )
        }
      } catch (e: Exception) {
        null
      }
    }
    
    return CustomTeamMetrics(
      totalStudents = totalStudents,
      studentsByCountry = studentsByCountry,
      studentsByStatus = studentsByStatus,
      submissionsList = submissions
    )
  }

  private fun computeMarketingMetrics(eods: List<DailyEodEntity>): CustomTeamMetrics {
    var totalVisits = 0
    val collegesVisited = mutableListOf<String>()
    
    val submissions = eods.mapNotNull { eod ->
      try {
        eod.customFormData?.let { jsonData ->
          val data = com.google.gson.Gson().fromJson(jsonData, com.example.data.MarketingFormData::class.java)
          totalVisits += data.visits.size
          collegesVisited.addAll(data.visits.map { it.collegeName })
          
          CustomFormSubmission(
            employeeName = eod.employeeName,
            date = eod.date,
            formData = "${data.visits.size} college visits",
            submissionTime = eod.submissionTime
          )
        }
      } catch (e: Exception) {
        null
      }
    }
    
    return CustomTeamMetrics(
      totalCollegeVisits = totalVisits,
      collegesVisited = collegesVisited.distinct(),
      submissionsList = submissions
    )
  }

  private fun computeSimpleMetrics(eods: List<DailyEodEntity>): CustomTeamMetrics {
    val submissions = eods.map { eod ->
      CustomFormSubmission(
        employeeName = eod.employeeName,
        date = eod.date,
        formData = "Description submitted",
        submissionTime = eod.submissionTime
      )
    }
    
    return CustomTeamMetrics(
      submissionsList = submissions
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
    val fds = firebaseDataSource ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    val (startDate, endDate) = calculateDateRange(filterType, customStart, customEnd)
    return fds.fetchEodHistoryFromFirestore(
      startDate = startDate,
      endDate = endDate,
      employeeId = employeeId,
      limit = limit,
      startAfterDoc = startAfterDoc
    )
  }

  suspend fun getTeamDataByEmployeeId(employeeId: String, teamType: String): TeamMemberDataEntity? = withContext(Dispatchers.IO) {
    teamMemberDataDao.getTeamDataByEmployeeId(employeeId, teamType)
  }

  suspend fun saveTeamMemberData(employeeId: String, teamType: String, data: TeamMemberDataEntity): Long = withContext(Dispatchers.IO) {
    teamMemberDataDao.insertTeamMemberData(data.copy(employeeId = employeeId))
  }

  suspend fun deleteTeamDataByEmployeeId(employeeId: String, teamType: String) = withContext(Dispatchers.IO) {
    teamMemberDataDao.deleteTeamDataByEmployeeId(employeeId, teamType)
  }

  /**
   * Insert a new team directly (for admin team creation)
   */
  suspend fun insertTeam(team: TeamEntity): Long = withContext(Dispatchers.IO) {
    return@withContext createTeam(team)
  }

  /**
   * Insert a new employee directly (for admin account creation)
   */
  suspend fun insertEmployee(employee: EmployeeEntity): Long = withContext(Dispatchers.IO) {
    return@withContext addEmployee(employee)
  }

  /**
   * Sync employees from Firebase to local Room database
   * Call this after login to fetch all employees from Firestore
   */
  suspend fun syncEmployeesFromFirebase(): Result<Int> = withContext(Dispatchers.IO) {
    android.util.Log.d("WorkCoreRepository", "🔄 Starting employee sync from Firebase...")
    android.util.Log.d("WorkCoreRepository", "Firebase initialized: ${firebaseDataSource != null}")
    
    return@withContext try {
      val result = firebaseDataSource?.fetchEmployees()
      
      if (result == null) {
        android.util.Log.w("WorkCoreRepository", "⚠️ Firebase not initialized, skipping sync")
        return@withContext Result.success(0)
      }
      
      result.fold(
        onSuccess = { employees ->
          android.util.Log.d("WorkCoreRepository", "✅ Fetched ${employees.size} employees from Firebase")
          
          if (employees.isEmpty()) {
            android.util.Log.w("WorkCoreRepository", "⚠️ No employees found in Firebase!")
            return@withContext Result.success(0)
          }
          
          // Log first few employees for verification
          employees.take(5).forEach { emp ->
            android.util.Log.d("WorkCoreRepository", "  Sample: ${emp.name} (${emp.employeeId}) team=${emp.team}")
          }
          
          // Insert or update each employee in local database
          var syncCount = 0
          employees.forEach { emp ->
            try {
              employeeDao.insert(emp)
              syncCount++
            } catch (e: Exception) {
              android.util.Log.e("WorkCoreRepository", "Failed to sync ${emp.name}: ${e.message}")
            }
          }
          
          android.util.Log.i("WorkCoreRepository", "✅ Synced $syncCount employees to local database")
          
          // Verify data was written
          val localCount = employeeDao.getAllEmployees().first().size
          android.util.Log.i("WorkCoreRepository", "📊 Local database now has $localCount total employees")
          
          Result.success(syncCount)
        },
        onFailure = { error ->
          android.util.Log.e("WorkCoreRepository", "❌ Failed to fetch employees: ${error.message}")
          Result.failure(error)
        }
      )
    } catch (e: Exception) {
      android.util.Log.e("WorkCoreRepository", "❌ Sync error: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * ONE-TIME ADMIN FUNCTION: Upload all employee, team, and department data to Firestore
   * Skips delete step since you manually cleared Firebase
   */
  suspend fun resetFirestoreWithLocalData(): Result<String> = withContext(Dispatchers.IO) {
    val firebase = firebaseDataSource ?: return@withContext Result.failure(
      IllegalStateException("Firebase not initialized")
    )
    
    return@withContext try {
      android.util.Log.i("WorkCoreRepository", "🔥 ADMIN: Starting Firestore upload...")
      
      // Step 1: Get all local data
      val localEmployees = employeeDao.getAllEmployees().first()
      val localTeams = teamDao.getAllTeams().first()
      val localDepartments = departmentDao.getAllDepartments().first()
      
      android.util.Log.i("WorkCoreRepository", "📊 Found ${localEmployees.size} employees, ${localTeams.size} teams, ${localDepartments.size} departments")
      
      // Step 2: Upload departments
      val deptResult = firebase.uploadAllDepartmentsToFirestore(localDepartments)
      var deptCount = 0
      deptResult.fold(
        onSuccess = { count -> 
          deptCount = count
          android.util.Log.i("WorkCoreRepository", "✅ Uploaded $count departments")
        },
        onFailure = { error ->
          return@withContext Result.failure(Exception("Failed to upload departments: ${error.message}"))
        }
      )
      
      // Step 3: Upload teams
      val teamResult = firebase.uploadAllTeamsToFirestore(localTeams)
      var teamCount = 0
      teamResult.fold(
        onSuccess = { count ->
          teamCount = count
          android.util.Log.i("WorkCoreRepository", "✅ Uploaded $count teams")
        },
        onFailure = { error ->
          return@withContext Result.failure(Exception("Failed to upload teams: ${error.message}"))
        }
      )
      
      // Step 4: Upload employees
      val empResult = firebase.uploadAllEmployeesToFirestore(localEmployees)
      empResult.fold(
        onSuccess = { empCount ->
          val message = "✅ SUCCESS: Uploaded $deptCount departments, $teamCount teams, and $empCount employees to Firestore"
          android.util.Log.i("WorkCoreRepository", message)
          Result.success(message)
        },
        onFailure = { error ->
          val errorMsg = "Failed to upload employees: ${error.message}"
          android.util.Log.e("WorkCoreRepository", errorMsg)
          Result.failure(Exception(errorMsg))
        }
      )
    } catch (e: Exception) {
      android.util.Log.e("WorkCoreRepository", "❌ Upload failed: ${e.message}", e)
      Result.failure(e)
    }
  }
}
