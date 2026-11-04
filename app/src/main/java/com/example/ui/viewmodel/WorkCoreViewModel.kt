package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AuditLogEntity
import com.example.data.DailyEodEntity
import com.example.data.DepartmentEntity
import com.example.data.EmployeeBehaviorMetrics
import com.example.data.EmployeeEntity
import com.example.data.EmployeeWorkMonitorItem
import com.example.data.MissingEodAlertGroup
import com.example.data.NotificationEntity
import com.example.data.Role
import com.example.data.SeedData
import com.example.data.TeamAnalyticsSummary
import com.example.data.TeamEntity
import com.example.data.TeamMemberBehaviorItem
import com.example.data.TeamMemberDataEntity
import com.example.data.TeamType
import com.example.data.CompanyEntity
import com.example.data.TeamReportSummary
import com.example.data.WorkCoreRepository
import com.example.data.firebase.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
  DASHBOARD,
  EOD_HISTORY,
  DOWNLOAD,
  EMPLOYEES,
  TEAMS,
  MONITOR,
  ANALYTICS
}

data class AuthSession(
  val role: Role,
  val employee: EmployeeEntity?,
  val departmentTeam: String? = null
)

class WorkCoreViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application)
  private val auditLogDao = database.auditLogDao()
  val firebaseDataSource = com.example.data.firebase.FirebaseDataSource(application)
  val repository = WorkCoreRepository(database, firebaseDataSource)
  val syncManager = SyncManager(application, database, firebaseDataSource)

  // Cloud sync observables
  val syncStatus = syncManager.syncState
  val isOnline = syncManager.networkMonitor.isOnline
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
  val pendingSyncCount = syncManager.pendingSyncCount

  // Auth & Session state
  val currentAuthSession = MutableStateFlow<AuthSession?>(null)
  val isAuthenticating = MutableStateFlow(false)

  // Department Roster flow
  val currentDepartmentTeam = MutableStateFlow("GT Team")
  val activeRosterEmployee = MutableStateFlow<EmployeeEntity?>(null)

  val currentRole = MutableStateFlow(Role.ADMIN)
  val currentEmployeeId = MutableStateFlow("GT-001")
  val currentTab = MutableStateFlow(AppTab.DASHBOARD)

  // Filters
  val monitorFilter = MutableStateFlow("All") // "All", "EOD Submitted", "EOD Pending", "Completed", "In Progress", "Blocked", "No Work", "On Leave"
  val teamDepartmentFilter = MutableStateFlow("All")
  val teamFilter = MutableStateFlow("All")
  val historySearchQuery = MutableStateFlow("")
  val historyStatusFilter = MutableStateFlow("All")
  val analyticsDateRange = MutableStateFlow("14 Days") // "7 Days", "14 Days", "30 Days"
  val analyticsRoleFilter = MutableStateFlow("All") // "All", "Employees", "Managers"

  // Dialog & Selection states
  val showSubmitEodDialog = MutableStateFlow(false)
  val eodBeingEdited = MutableStateFlow<DailyEodEntity?>(null)
  val selectedEmployeeForDetail = MutableStateFlow<EmployeeEntity?>(null)
  val showExportDialog = MutableStateFlow(false)
  val exportReportType = MutableStateFlow("EOD") // "EOD", "PERFORMANCE", "TEAM", "COMPANY"
  val exportFormat = MutableStateFlow("EXCEL") // "EXCEL", "CSV"
  val exportScope = MutableStateFlow("COMPANY") // "COMPANY", "TEAM", "INDIVIDUAL"
  val exportTargetTeam = MutableStateFlow("All")
  val exportTargetEmployeeId = MutableStateFlow("All")
  val exportedReportContent = MutableStateFlow<String?>(null)

  // Team, Manager & Account Creation modals
  val showNotificationsDialog = MutableStateFlow(false)
  val showCreateTeamDialog = MutableStateFlow(false)
  val teamBeingEdited = MutableStateFlow<TeamEntity?>(null)
  val showMoveEmployeeDialog = MutableStateFlow(false)
  val employeeToMove = MutableStateFlow<EmployeeEntity?>(null)
  val showAddEmployeeByManagerDialog = MutableStateFlow(false)

  // Feedback notifications
  val snackbarMessage = MutableStateFlow<String?>(null)

  val employees: StateFlow<List<EmployeeEntity>> = repository.allEmployees
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val departments: StateFlow<List<DepartmentEntity>> = repository.allDepartments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val selectedDepartmentCode = MutableStateFlow("ML")
  val selectedCompanyId = MutableStateFlow("ALL")

  val dailyEods: StateFlow<List<DailyEodEntity>> = repository.allDailyEods
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val managerTeamMembers: StateFlow<List<TeamMemberBehaviorItem>> = combine(
    selectedDepartmentCode,
    selectedCompanyId,
    employees,
    dailyEods
  ) { dept, companyId, emps, eods ->
    // Filter by company first
    val companyFilteredEmps = if (companyId == "ALL") {
      emps
    } else {
      emps.filter { it.companyId == companyId }
    }
    repository.computeManagerTeamMetrics(dept, companyFilteredEmps, eods, repository.getTodayDateString())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val teams: StateFlow<List<TeamEntity>> = repository.allTeams
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val companies: StateFlow<List<CompanyEntity>> = repository.allCompanies
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val unreadNotificationsCount: StateFlow<Int> = combine(
    notifications,
    currentRole,
    currentEmployeeId,
    employees
  ) { notifs, role, empId, emps ->
    val managerTeam = emps.firstOrNull { it.employeeId == empId }?.team
    notifs.count { notif ->
      if (notif.isRead) return@count false
      when (role) {
        Role.ADMIN -> true
        Role.MANAGER, Role.EMPLOYEE -> notif.targetRole == "ALL" || notif.targetRole == "EMPLOYEE" || notif.targetRole == "MANAGER" || notif.targetEmployeeId == empId
      }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val auditLogs = repository.recentAuditLogs
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Visible Employees (Strict Role/Team Isolation)
  val visibleEmployees: StateFlow<List<EmployeeEntity>> = combine(
    employees,
    currentRole,
    currentEmployeeId
  ) { emps, role, empId ->
    when (role) {
      Role.ADMIN -> emps
      Role.MANAGER, Role.EMPLOYEE -> {
        val dept = currentAuthSession.value?.departmentTeam ?: "GT Team"
        
        // Handle AO roles (exact department name match)
        when (dept) {
          "Marketing" -> emps.filter { it.department == "Marketing" && it.companyId == "COMP_AO" }
          "Counsellor" -> emps.filter { it.department == "Counsellor" && it.companyId == "COMP_AO" }
          "Tele Caller" -> emps.filter { it.department == "Tele Caller" && it.companyId == "COMP_AO" }
          "Manager" -> emps.filter { it.department == "Manager" && it.companyId == "COMP_AO" }
          else -> {
            // Regular Sandspace teams - filter by team name
            emps.filter { it.team == dept || it.team.contains(dept, true) }
          }
        }
      }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Visible Teams (Strict Role Isolation)
  val visibleTeams: StateFlow<List<TeamEntity>> = combine(
    teams,
    employees,
    currentRole,
    currentEmployeeId
  ) { tms, emps, role, empId ->
    when (role) {
      Role.ADMIN -> tms
      Role.MANAGER, Role.EMPLOYEE -> {
        val dept = currentAuthSession.value?.departmentTeam ?: "GT Team"
        tms.filter { it.name.contains(dept, true) || it.department.contains(dept, true) }
      }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Visible Daily EODs (Strict Role/Team Isolation)
  val visibleDailyEods: StateFlow<List<DailyEodEntity>> = combine(
    dailyEods,
    visibleEmployees
  ) { eods, vEmps ->
    val allowedIds = vEmps.map { it.employeeId }.toSet()
    eods.filter { allowedIds.contains(it.employeeId) }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Computed dashboard summary
  val dashboardSummary = combine(
    employees,
    dailyEods,
    currentRole,
    currentEmployeeId
  ) { emps, eods, role, empId ->
    val visibleEmps = when (role) {
      Role.ADMIN -> emps
      Role.MANAGER, Role.EMPLOYEE -> {
        val dept = currentAuthSession.value?.departmentTeam ?: "GT Team"
        emps.filter { it.department.contains(dept, true) || it.team.contains(dept, true) }
      }
    }
    repository.computeDashboardSummary(visibleEmps, eods, repository.getTodayDateString())
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    repository.computeDashboardSummary(emptyList(), emptyList(), repository.getTodayDateString())
  )

  // Work monitor items
  val workMonitorItems: StateFlow<List<EmployeeWorkMonitorItem>> = combine(
    employees,
    dailyEods,
    currentRole,
    currentEmployeeId
  ) { emps, eods, role, empId ->
    val visibleEmps = when (role) {
      Role.ADMIN -> emps
      Role.MANAGER, Role.EMPLOYEE -> {
        val dept = currentAuthSession.value?.departmentTeam ?: "GT Team"
        emps.filter { it.department.contains(dept, true) || it.team.contains(dept, true) }
      }
    }
    repository.computeWorkMonitor(visibleEmps, eods, repository.getTodayDateString())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Behavior & pattern metrics
  val behaviorMetrics: StateFlow<List<EmployeeBehaviorMetrics>> = combine(
    employees,
    dailyEods,
    currentRole,
    currentEmployeeId
  ) { emps, eods, role, empId ->
    val visibleEmps = when (role) {
      Role.ADMIN -> emps
      Role.MANAGER, Role.EMPLOYEE -> {
        val dept = currentAuthSession.value?.departmentTeam ?: "GT Team"
        emps.filter { it.department.contains(dept, true) || it.team.contains(dept, true) }
      }
    }
    repository.computeBehaviorMetrics(visibleEmps, eods, repository.getTodayDateString())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Team performance analytics
  val teamAnalytics: StateFlow<List<TeamAnalyticsSummary>> = combine(
    visibleTeams,
    employees,
    dailyEods,
    teamDepartmentFilter
  ) { vTeams, emps, eods, dept ->
    val teamNames = vTeams.map { it.name }.toSet()
    val allSummaries = repository.computeTeamAnalytics(emps, eods, dept, "All", repository.getTodayDateString())
    allSummaries.filter { it.teamName in teamNames }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Team Reports (Custom Form Aggregations)
  val teamReports: StateFlow<List<TeamReportSummary>> = combine(
    teams,
    employees,
    dailyEods,
    companies
  ) { tms, emps, eods, comps ->
    repository.computeTeamReports(tms, emps, eods, comps, repository.getTodayDateString())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Missing EOD Alerts (Role & Team isolated, excluding On Leave users)
  val missingEodAlerts: StateFlow<List<MissingEodAlertGroup>> = combine(
    employees,
    dailyEods,
    currentRole,
    currentEmployeeId
  ) { emps, eods, role, empId ->
    val managerTeam = emps.firstOrNull { it.employeeId == empId }?.team
    repository.computeMissingEodAlerts(emps, eods, role, empId, managerTeam, repository.getTodayDateString())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    viewModelScope.launch {
      repository.initializeSeedDataIfNeeded()
      restoreSession()
    }
    syncManager.startMonitoring(viewModelScope)
  }

  private suspend fun restoreSession() {
    val session = repository.authRepository.readSession() ?: return
    val allEmps = repository.allEmployees.first()
    val emp = session.employeeId?.let { empId -> allEmps.firstOrNull { it.employeeId == empId } }
      ?: allEmps.firstOrNull { it.email.equals(session.email, ignoreCase = true) }
      ?: if (session.isAdmin) SeedData.employees.firstOrNull { it.employeeId == "ADMIN" } else null

    val role = when {
      session.isAdmin -> Role.ADMIN
      session.isManager -> Role.MANAGER
      else -> Role.EMPLOYEE
    }
    val resolvedEmp = emp ?: EmployeeEntity(
      employeeId = session.employeeId ?: session.uid,
      name = session.email.substringBefore("@"),
      email = session.email,
      phone = "",
      password = "",
      role = session.role,
      team = session.teamId ?: "",
      department = "",
      departmentId = "",
      designation = if (session.isAdmin) "Administrator" else if (session.isManager) "Manager" else "Team Member",
      joiningDate = repository.getTodayDateString(),
      isActive = true
    )

    currentAuthSession.value = AuthSession(
      role = role,
      employee = resolvedEmp,
      departmentTeam = session.teamId
    )
    currentRole.value = role
    currentEmployeeId.value = resolvedEmp.employeeId
    session.teamId?.let { currentDepartmentTeam.value = it }
  }

  fun triggerManualSync() {
    viewModelScope.launch {
      syncManager.syncPendingToCloud()
      syncManager.pullFromCloud()
      snackbarMessage.value = "Sync completed"
    }
  }

  fun setRole(role: Role) {
    currentRole.value = role
    when (role) {
      Role.ADMIN -> currentEmployeeId.value = "ADMIN"
      Role.MANAGER -> currentEmployeeId.value = "MGR-001"
      Role.EMPLOYEE -> currentEmployeeId.value = "GT-001"
    }
    snackbarMessage.value = "Switched to ${when (role) {
      Role.ADMIN -> "Administrator (Full Access)"
      Role.MANAGER -> "Team Manager"
      Role.EMPLOYEE -> "Department Roster (GT Team)"
    }}"
  }

  fun setTab(tab: AppTab) {
    currentTab.value = tab
  }

  fun openSubmitEod(existingEod: DailyEodEntity? = null) {
    eodBeingEdited.value = existingEod
    showSubmitEodDialog.value = true
  }

  fun closeSubmitEod() {
    showSubmitEodDialog.value = false
    eodBeingEdited.value = null
  }

  fun submitEod(
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
  ) {
    viewModelScope.launch {
      val result = repository.submitDailyEod(
        employeeId = employeeId,
        employeeName = employeeName,
        date = date,
        project = project,
        todayWork = todayWork,
        workCompleted = workCompleted,
        hoursWorked = hoursWorked,
        progressPercentage = progressPercentage,
        workStatus = workStatus,
        blockers = blockers,
        remarks = remarks,
        tomorrowPlan = tomorrowPlan,
        isEdit = isEdit
      )

      if (result.isSuccess) {
        snackbarMessage.value = if (isEdit) "EOD for $date updated successfully!" else "Today's EOD submitted successfully!"
        // Track EOD submission
        com.example.util.Analytics.logEodSubmitted(
          employeeId = employeeId,
          team = currentDepartmentTeam.value ?: "unknown",
          formType = "STANDARD",
          isEdit = isEdit
        )
        closeSubmitEod()
        syncManager.syncPendingToCloud()
      } else {
        snackbarMessage.value = result.exceptionOrNull()?.message ?: "Error submitting EOD"
      }
    }
  }

  fun deleteEodByAdmin(eodId: Long, employeeName: String = "", date: String = "") {
    viewModelScope.launch {
      val result = repository.deleteEod(eodId)
      if (result.isSuccess) {
        snackbarMessage.value = "EOD entry deleted successfully."
        syncManager.syncPendingToCloud()
      } else {
        snackbarMessage.value = result.exceptionOrNull()?.message ?: "Failed to delete EOD entry."
      }
    }
  }

  fun updateEodByAdmin(eod: DailyEodEntity) {
    viewModelScope.launch {
      val result = repository.updateEodByAdmin(eod)
      if (result.isSuccess) {
        snackbarMessage.value = "EOD entry updated by Admin."
        syncManager.syncPendingToCloud()
      } else {
        snackbarMessage.value = result.exceptionOrNull()?.message ?: "Failed to update EOD entry."
      }
    }
  }

  fun loginDepartment(
    teamName: String,
    password: String = "",
    onResult: (Boolean, String?) -> Unit
  ) {
    viewModelScope.launch {
      android.util.Log.d("WorkCoreAuth", "Selecting department team: '$teamName'")

      val cleanPassword = password.trim()
      if (cleanPassword.isBlank()) {
        onResult(false, "Please enter the department password.")
        return@launch
      }

      val team = teams.value.firstOrNull { 
        it.name.equals(teamName, ignoreCase = true) || 
        it.department.equals(teamName, ignoreCase = true)
      }

      if (team == null) {
        val availableTeams = teams.value.joinToString(", ") { it.name }
        onResult(false, "Team '$teamName' not found. Available: $availableTeams")
        return@launch
      }

      val teamCode = when {
        team.name.contains("General", ignoreCase = true) || team.name.contains("GT", ignoreCase = true) -> "gt123"
        team.name.contains("ML", ignoreCase = true) -> "ml123"
        team.name.contains("DB", ignoreCase = true) -> "db123"
        team.name.contains("Cyber", ignoreCase = true) -> "cyber123"
        team.name.contains("Writing", ignoreCase = true) -> "write123"
        team.name.contains("Marketing", ignoreCase = true) -> "marketing123"
        team.name.contains("Counsellor", ignoreCase = true) -> "counsellor123"
        team.name.contains("Tele", ignoreCase = true) -> "telecaller123"
        else -> null
      }

      val isPasscodeValid = (team.teamPassword.isNotBlank() && team.teamPassword == cleanPassword) ||
          teamCode == cleanPassword

      if (!isPasscodeValid) {
        onResult(false, "Invalid passcode for ${team.name}.")
        return@launch
      }

      // Passcode is valid: set active roster view
      currentDepartmentTeam.value = team.name
      currentRole.value = Role.EMPLOYEE
      activeRosterEmployee.value = null
      currentAuthSession.value = AuthSession(
        role = Role.EMPLOYEE,
        employee = null,
        departmentTeam = team.name
      )
      currentTab.value = AppTab.DASHBOARD
      snackbarMessage.value = "Signed in to ${team.name} roster"
      val company = if (team.name.contains("AO") || 
        listOf("Marketing","Counsellor","Tele Caller","Manager").any { team.name.contains(it) })
        "AO" else "SS"
      com.example.util.Analytics.logLogin(team = team.name, company = company)

      // NOTE: Do NOT sync for password-free team login because:
      // 1. Firebase Auth is not initialized (no email/password provided)
      // 2. SyncManager will fail and block further operations
      // 3. Local seed data is sufficient for roster display
      // Firebase sync only makes sense for email-based admin/employee logins
      android.util.Log.d("WorkCoreAuth", "✅ Team login successful - using local seed data (no Firebase sync for password-free mode)")

      onResult(true, null)
    }
  }

  fun loginAORole(
    role: String, // "Marketing", "Counsellor", "Tele Caller"
    passcode: String = "",
    onResult: (Boolean, String?) -> Unit
  ) {
    viewModelScope.launch {
      // Hardcoded passcodes removed for password-free login
      currentDepartmentTeam.value = role
      currentRole.value = Role.EMPLOYEE
      activeRosterEmployee.value = null
      currentAuthSession.value = AuthSession(
        role = Role.EMPLOYEE,
        employee = null,
        departmentTeam = role
      )
      currentTab.value = AppTab.DASHBOARD
      snackbarMessage.value = "Signed in to $role roster (AO)"

      // Auto-sync employees from Firebase
      launch {
        android.util.Log.d("WorkCoreAuth", "Auto-syncing AO employees from Firebase...")
        val syncResult = repository.syncEmployeesFromFirebase()
        syncResult.fold(
          onSuccess = { count ->
            if (count > 0) {
              android.util.Log.i("WorkCoreAuth", "Synced $count employees from Firebase")
            }
          },
          onFailure = { error ->
            android.util.Log.w("WorkCoreAuth", "Sync failed: ${error.message}")
          }
        )
      }

      onResult(true, null)
    }
  }

  fun selectRosterEmployee(employee: EmployeeEntity) {
    activeRosterEmployee.value = employee
    currentEmployeeId.value = employee.employeeId
  }

  fun closeRosterEmployee() {
    activeRosterEmployee.value = null
  }

  fun submitRosterEod(
    employee: EmployeeEntity,
    date: String,
    project: String,
    taskDescription: String,
    hoursWorked: Double,
    progressPercentage: Int,
    workStatus: String,
    onComplete: () -> Unit = {}
  ) {
    viewModelScope.launch {
      val result = repository.saveOrUpdateEmployeeDashboardEod(
        employeeId = employee.employeeId,
        employeeName = employee.name,
        date = date,
        project = project,
        taskDescription = taskDescription,
        hoursWorked = hoursWorked,
        progressPercentage = progressPercentage,
        workStatus = workStatus
      )
      if (result.isSuccess) {
        snackbarMessage.value = "EOD submitted successfully for ${employee.name}!"
        activeRosterEmployee.value = null
        onComplete()
        syncManager.syncPendingToCloud()
      } else {
        snackbarMessage.value = result.exceptionOrNull()?.message ?: "Failed to submit EOD"
      }
    }
  }

  fun submitCustomEod(
    employee: EmployeeEntity,
    date: String,
    customFormData: String
  ) {
    viewModelScope.launch {
      val result = repository.submitCustomEod(
        employeeId = employee.employeeId,
        employeeName = employee.name,
        teamId = employee.teamId ?: "",
        companyId = employee.companyId ?: "",
        date = date,
        customFormData = customFormData
      )
      if (result.isSuccess) {
        snackbarMessage.value = "EOD submitted successfully for ${employee.name}!"
        // Track custom EOD submission
        com.example.util.Analytics.logEodSubmitted(
          employeeId = employee.employeeId,
          team = employee.team,
          formType = employee.department, // "Marketing", "Counsellor", "Tele Caller"
          isEdit = false
        )
        activeRosterEmployee.value = null
        syncManager.syncPendingToCloud()
      } else {
        snackbarMessage.value = result.exceptionOrNull()?.message ?: "Failed to submit custom EOD"
      }
    }
  }

  fun submitEmployeeDashboardEod(
    date: String,
    project: String,
    taskDescription: String,
    hoursWorked: Double,
    progressPercentage: Int,
    workStatus: String
  ) {
    viewModelScope.launch {
      val emp = employees.value.firstOrNull { it.employeeId == currentEmployeeId.value }
      val empName = emp?.name ?: "Employee"
      val result = repository.saveOrUpdateEmployeeDashboardEod(
        employeeId = currentEmployeeId.value,
        employeeName = empName,
        date = date,
        project = project,
        taskDescription = taskDescription,
        hoursWorked = hoursWorked,
        progressPercentage = progressPercentage,
        workStatus = workStatus
      )
      if (result.isSuccess) {
        snackbarMessage.value = "EOD for $date saved successfully ($hoursWorked hrs, $workStatus)"
        syncManager.syncPendingToCloud()
      } else {
        snackbarMessage.value = result.exceptionOrNull()?.message ?: "Failed to submit EOD"
      }
    }
  }

  fun exportReport(type: String) {
    viewModelScope.launch {
      exportReportType.value = type
      val content = if (exportFormat.value == "EXCEL") {
        repository.generateExcelWorkbookXml(
          reportScope = if (type == "TEAM") "TEAM" else "COMPANY",
          targetTeam = if (teamFilter.value != "All") teamFilter.value else null,
          employees = employees.value,
          eods = dailyEods.value,
          metrics = behaviorMetrics.value,
          teams = teams.value
        )
      } else {
        repository.generateCsvReport(
          reportType = type,
          employees = employees.value,
          eods = dailyEods.value,
          metrics = behaviorMetrics.value
        )
      }
      exportedReportContent.value = content
      showExportDialog.value = true
    }
  }

  fun exportFilteredReport(
    scope: String,
    format: String,
    targetTeam: String? = null,
    targetEmployeeId: String? = null
  ) {
    viewModelScope.launch {
      exportScope.value = scope
      exportFormat.value = format
      exportReportType.value = scope

      val content = if (format == "EXCEL") {
        repository.generateExcelWorkbookXml(
          reportScope = scope,
          targetTeam = targetTeam,
          targetEmployeeId = targetEmployeeId,
          employees = employees.value,
          eods = dailyEods.value,
          metrics = behaviorMetrics.value,
          teams = teams.value
        )
      } else {
        val emps = if (targetEmployeeId != null && targetEmployeeId != "All") {
          employees.value.filter { it.employeeId == targetEmployeeId }
        } else if (targetTeam != null && targetTeam != "All") {
          employees.value.filter { it.team.equals(targetTeam, ignoreCase = true) }
        } else {
          employees.value
        }
        val empIds = emps.map { it.employeeId }.toSet()
        val eods = dailyEods.value.filter { empIds.contains(it.employeeId) }
        val m = behaviorMetrics.value.filter { empIds.contains(it.employee.employeeId) }

        repository.generateCsvReport(
          reportType = if (scope == "INDIVIDUAL") "EOD" else if (scope == "TEAM") "TEAM" else "PERFORMANCE",
          employees = emps,
          eods = eods,
          metrics = m
        )
      }
      exportedReportContent.value = content
      showExportDialog.value = true
    }
  }

  fun createTeam(name: String, department: String, manager: EmployeeEntity, projects: String, description: String) {
    viewModelScope.launch {
      // FIXED: Use repository function that generates unique team ID
      val result = repository.createTeamWithGeneratedId(
        name = name,
        department = department,
        managerId = manager.employeeId,
        managerName = manager.name,
        projects = projects,
        description = description
      )
      
      result.onSuccess { assignedTeamId ->
        snackbarMessage.value = "Team '$name' created successfully! Team ID: $assignedTeamId"
        showCreateTeamDialog.value = false
        syncManager.syncPendingToCloud()
      }.onFailure { err ->
        snackbarMessage.value = err.message ?: "Failed to create team."
      }
    }
  }

  fun updateTeam(team: TeamEntity) {
    viewModelScope.launch {
      repository.updateTeam(team)
      snackbarMessage.value = "Team '${team.name}' updated successfully!"
      teamBeingEdited.value = null
    }
  }

  fun setTeamStatus(teamId: String, status: String) {
    viewModelScope.launch {
      repository.setTeamStatus(teamId, status)
      snackbarMessage.value = "Team status updated to $status"
    }
  }

  fun moveEmployeeToTeam(empId: String, newTeamName: String) {
    viewModelScope.launch {
      repository.moveEmployeeToTeam(empId, newTeamName)
      snackbarMessage.value = "Employee moved to $newTeamName"
      showMoveEmployeeDialog.value = false
      employeeToMove.value = null
    }
  }

  fun toggleEmployeeActiveStatus(empId: String, currentActive: Boolean) {
    viewModelScope.launch {
      repository.toggleEmployeeActive(empId, currentActive)
      val statusText = if (currentActive) "deactivated" else "activated"
      snackbarMessage.value = "Employee $empId has been $statusText"
      
      // Sync employee status change to Firebase immediately
      syncManager.syncPendingToCloud()
    }
  }

  fun deleteEmployeeAccount(empId: String) {
    viewModelScope.launch {
      val result = repository.deleteEmployee(empId)
      if (result.isSuccess) {
        snackbarMessage.value = "Employee account $empId deleted permanently."
        syncManager.syncPendingToCloud()
      } else {
        snackbarMessage.value = result.exceptionOrNull()?.message ?: "Failed to delete employee account."
      }
    }
  }

  /**
   * ONE-TIME ADMIN FUNCTION: Reset Firebase with local CSV data
   * Deletes all old Firestore employees and uploads 73 new ones from SeedData
   */
  fun resetFirestoreWithLocalData(onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      android.util.Log.i("WorkCoreViewModel", "🔥 Admin triggered Firebase reset...")
      val result: Result<String> = repository.resetFirestoreWithLocalData()
      result.fold(
        onSuccess = { message: String ->
          android.util.Log.i("WorkCoreViewModel", message)
          snackbarMessage.value = message
          onResult(true, message)
        },
        onFailure = { error: Throwable ->
          val errorMsg = "Reset failed: ${error.message}"
          android.util.Log.e("WorkCoreViewModel", errorMsg)
          snackbarMessage.value = errorMsg
          onResult(false, errorMsg)
        }
      )
    }
  }

  fun markAllNotificationsRead() {
    viewModelScope.launch {
      repository.markAllNotificationsAsRead()
    }
  }

  // -------------------------------------------------------------
  // AUTHENTICATION & SESSION MANAGEMENT
  // -------------------------------------------------------------

  fun login(
    role: Role,
    teamName: String?,
    identifier: String,
    passwordInput: String,
    onResult: (Boolean, String?) -> Unit
  ) {
    viewModelScope.launch {
      isAuthenticating.value = true
      val result = repository.authenticate(role, teamName, identifier, passwordInput)
      isAuthenticating.value = false
      if (result.isSuccess) {
        val user = result.getOrThrow()
        val session = repository.authRepository.readSession()
        val assignedTeam = session?.teamId ?: teamName ?: user.team
        currentDepartmentTeam.value = assignedTeam

        val effectiveRole = when {
          session?.isAdmin == true -> Role.ADMIN
          session?.isManager == true -> Role.MANAGER
          else -> role
        }

        currentAuthSession.value = AuthSession(effectiveRole, user, assignedTeam)
        currentRole.value = effectiveRole
        currentEmployeeId.value = user.employeeId
        currentTab.value = AppTab.DASHBOARD
        snackbarMessage.value = "Welcome back, ${user.name}!"

        // AUTO-SYNC: Fetch employees from Firebase after successful login
        if (effectiveRole == Role.ADMIN) {
          launch {
            android.util.Log.d("WorkCoreAuth", "Auto-syncing employees from Firebase (Admin login)...")
            val syncResult = repository.syncEmployeesFromFirebase()
            syncResult.fold(
              onSuccess = { count ->
                if (count > 0) {
                  android.util.Log.i("WorkCoreAuth", "Synced $count employees from Firebase")
                }
              },
              onFailure = { error ->
                android.util.Log.w("WorkCoreAuth", "Sync failed: ${error.message}")
              }
            )
          }
        }

        onResult(true, null)
      } else {
        val error = result.exceptionOrNull()?.message ?: "Authentication failed."
        snackbarMessage.value = error
        onResult(false, error)
      }
    }
  }

  fun logout() {
    val team = currentDepartmentTeam.value ?: currentAuthSession.value?.departmentTeam ?: "unknown"
    com.example.util.Analytics.logLogout(team = team)
    repository.authRepository.signOut()
    currentAuthSession.value = null
    currentTab.value = AppTab.DASHBOARD
    snackbarMessage.value = "Logged out successfully."
  }

  fun sendPasswordReset(email: String, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      repository.authRepository.sendPasswordReset(email).fold(
        onSuccess = {
          onResult(
            true,
            "If that address is registered, a password-reset email has been sent. Sign in with that same email address."
          )
        },
        onFailure = {
          onResult(false, "Could not send the reset email. Check your connection and try again.")
        }
      )
    }
  }

  // -------------------------------------------------------------
  // MANAGER EMPLOYEE ACCOUNT CREATION
  // -------------------------------------------------------------

  fun createEmployeeScoped(
    name: String,
    email: String,
    employeeCode: String,
    joiningDate: String,
    designation: String,
    departmentCode: String,
    sendEmailLink: Boolean,
    teamSpecificData: Map<TeamType, TeamMemberDataEntity?> = emptyMap(),
    onSuccess: (String) -> Unit = {},
    onError: (String) -> Unit = {}
  ) {
    viewModelScope.launch {
      val sessionEmp = currentAuthSession.value?.employee ?: employees.value.find { it.employeeId == currentEmployeeId.value }
      if (sessionEmp == null) {
        val err = "Manager session not found."
        snackbarMessage.value = err
        onError(err)
        return@launch
      }

      // NOTE: employeeCode parameter is ignored - real ID is generated in repository
      val result = repository.createEmployeeByManager(
        manager = sessionEmp,
        name = name,
        email = email,
        employeeCode = employeeCode, // This is the UI placeholder, repository will generate real ID
        joiningDate = joiningDate,
        designation = designation,
        departmentCode = departmentCode,
        sendEmailLink = sendEmailLink
      )

      result.onSuccess { assignedEmployeeId ->
        // FIXED: Display the actual assigned ID, not the placeholder from the UI
        val msg = "Employee $name added to $departmentCode successfully! Assigned ID: $assignedEmployeeId"
        snackbarMessage.value = msg
        showAddEmployeeByManagerDialog.value = false

        // Save team-specific data with the real employee ID
        viewModelScope.launch {
          teamSpecificData.forEach { (teamType, data) ->
            if (data != null) {
              repository.saveTeamMemberData(
                assignedEmployeeId,
                teamType.departmentCode,
                data.copy(employeeId = assignedEmployeeId)
              )
              auditLogDao.insert(
                AuditLogEntity(
                  userId = assignedEmployeeId,
                  action = "SAVE_TEAM_DATA",
                  entityType = "TEAM_MEMBER_DATA",
                  entityId = "${assignedEmployeeId}-${teamType.departmentCode}",
                  description = "Saved ${teamType.displayName} data for $assignedEmployeeId"
                )
              )
            }
          }
          snackbarMessage.value = snackbarMessage.value.plus(" ${teamSpecificData.count { it.value != null }} team type(s) saved.")
          syncManager.syncPendingToCloud()
        }

        onSuccess(msg)
      }.onFailure { err ->
        val msg = err.message ?: "Failed to create employee."
        snackbarMessage.value = msg
        onError(msg)
      }
    }
  }

  fun createEmployeeByManager(
    name: String,
    employeeId: String,
    password: String,
    confirmPassword: String
  ) {
    viewModelScope.launch {
      val session = currentAuthSession.value
      if (session == null || session.role != Role.ADMIN) {
        snackbarMessage.value = "Unauthorized: Only Administrators can create employee accounts."
        return@launch
      }
      if (password != confirmPassword) {
        snackbarMessage.value = "Passwords do not match."
        return@launch
      }

      val adminEmp = session.employee ?: employees.value.first()
      
      // NOTE: employeeId parameter is ignored - real ID is generated in repository
      val result = repository.createEmployeeByManager(
        managerEmpId = adminEmp.employeeId,
        name = name,
        employeeId = employeeId, // This is the UI placeholder, repository will generate real ID
        password = password
      )

      result.onSuccess { assignedEmployeeId ->
        // FIXED: Display the actual assigned ID, not the placeholder from the UI
        snackbarMessage.value = "Employee account created for $name! Assigned ID: $assignedEmployeeId (Team: ${adminEmp.team})"
        showAddEmployeeByManagerDialog.value = false
        syncManager.syncPendingToCloud()
      }.onFailure { err ->
        snackbarMessage.value = err.message ?: "Error creating employee account."
      }
    }
  }

  // History Query & Filter States
  val historyFilterType = MutableStateFlow("This month") // "This month", "Previous month", "Custom date range"
  val historyCustomStart = MutableStateFlow("")
  val historyCustomEnd = MutableStateFlow("")
  val historyEmployeeFilter = MutableStateFlow("All")
  val historyIsLoading = MutableStateFlow(false)
  val historyErrorMessage = MutableStateFlow<String?>(null)
  val historyEodList = MutableStateFlow<List<DailyEodEntity>>(emptyList())
  val historyHasMore = MutableStateFlow(false)
  val historyLastDoc = MutableStateFlow<com.google.firebase.firestore.DocumentSnapshot?>(null)

  fun loadEodHistory(reset: Boolean = true) {
    viewModelScope.launch {
      if (reset) {
        historyLastDoc.value = null
        historyEodList.value = emptyList()
      }

      // Requirement 9: Validate start date <= end date for custom range
      if (historyFilterType.value == "Custom date range") {
        val s = historyCustomStart.value.trim()
        val e = historyCustomEnd.value.trim()
        if (s.isNotBlank() && e.isNotBlank() && s > e) {
          historyErrorMessage.value = "Start date ($s) cannot be after end date ($e)."
          return@launch
        }
      }

      historyIsLoading.value = true
      historyErrorMessage.value = null

      val empId = if (currentRole.value == Role.ADMIN) {
        if (historyEmployeeFilter.value == "All") null else historyEmployeeFilter.value
      } else {
        currentEmployeeId.value
      }

      val res = repository.fetchEodHistoryFromCloud(
        filterType = historyFilterType.value,
        customStart = historyCustomStart.value.ifBlank { null },
        customEnd = historyCustomEnd.value.ifBlank { null },
        employeeId = empId,
        limit = 50,
        startAfterDoc = if (reset) null else historyLastDoc.value
      )

      historyIsLoading.value = false

      res.onSuccess { queryResult ->
        if (reset) {
          historyEodList.value = queryResult.eods
        } else {
          historyEodList.value = historyEodList.value + queryResult.eods
        }
        historyLastDoc.value = queryResult.lastDocument
        historyHasMore.value = queryResult.hasMore
      }.onFailure { err ->
        historyErrorMessage.value = err.message ?: "Failed to fetch EOD history from Firestore."
      }
    }
  }

  fun loadMoreEodHistory() {
    if (!historyIsLoading.value && historyHasMore.value) {
      loadEodHistory(reset = false)
    }
  }

  fun clearSnackbar() {
    snackbarMessage.value = null
  }

  /**
   * Create a new team with a unique password
   * Only accessible by Admin role
   */
  fun createTeam(
    teamName: String,
    department: String,
    departmentCode: String,
    managerId: String,
    managerName: String,
    teamPassword: String,
    description: String,
    projects: String,
    onResult: (Boolean, String?) -> Unit
  ) {
    viewModelScope.launch {
      try {
        // Check if team already exists
        val existingTeam = teams.value.firstOrNull { 
          it.name.equals(teamName, ignoreCase = true) 
        }
        
        if (existingTeam != null) {
          onResult(false, "Team with name '$teamName' already exists")
          return@launch
        }
        
        // Generate unique team ID
        val teamId = "TEAM_${teamName.uppercase().replace(" ", "_")}_${System.currentTimeMillis()}"
        
        // Create team entity
        val newTeam = TeamEntity(
          teamId = teamId,
          name = teamName,
          department = department,
          managerId = managerId,
          managerName = managerName,
          projects = projects.ifBlank { "General Projects" },
          createdDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date()),
          status = "Active",
          description = description.ifBlank { "Team for $department department" },
          teamPassword = teamPassword
        )
        
        // Insert into database
        repository.insertTeam(newTeam)
        
        android.util.Log.i("WorkCoreViewModel", "Team created: $teamName")
        snackbarMessage.value = "Team '$teamName' created successfully"
        onResult(true, null)
        
      } catch (e: Exception) {
        android.util.Log.e("WorkCoreViewModel", "❌ Failed to create team", e)
        onResult(false, e.message ?: "Failed to create team")
      }
    }
  }

  /**
   * Create the first admin account during initial setup
   * Only works if no admin exists
   */
  fun createAdminAccount(
    username: String,
    fullName: String,
    email: String,
    phone: String,
    password: String = "",
    onResult: (Boolean, String?) -> Unit
  ) {
    viewModelScope.launch {
      try {
        // Check if admin already exists
        val existingAdmin = employees.value.firstOrNull { 
          it.role == Role.ADMIN.name 
        }
        
        if (existingAdmin != null) {
          onResult(false, "Admin account already exists")
          return@launch
        }
        
        // Create admin employee entity without password
        val adminEmployee = EmployeeEntity(
          employeeId = "ADMIN",
          name = fullName,
          email = email,
          phone = phone.ifBlank { "+1 (555) 000-0001" },
          department = "Cyber",
          departmentId = "DEPT_CYBER",
          team = "Executive",
          managerId = "BOARD",
          designation = "System Administrator",
          defaultProject = "Executive Governance",
          joiningDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date()),
          role = Role.ADMIN.name,
          password = ""
        )
        
        // Insert into database
        repository.insertEmployee(adminEmployee)
        
        android.util.Log.i("WorkCoreViewModel", "✅ Admin account created: $username")
        snackbarMessage.value = "Admin account created. You can now log in."
        onResult(true, null)
        
      } catch (e: Exception) {
        android.util.Log.e("WorkCoreViewModel", "❌ Failed to create admin account", e)
        onResult(false, e.message ?: "Failed to create admin account")
      }
    }
  }

  /**
   * Check if any admin account exists in the system
   */
  fun hasAdminAccount(): Boolean {
    return employees.value.any { it.role == Role.ADMIN.name }
  }

  fun loadTeamMemberData(employeeId: String, teamType: TeamType, onResult: (TeamMemberDataEntity?) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      onResult(repository.getTeamDataByEmployeeId(employeeId, teamType.departmentCode))
    }
  }

  fun saveTeamMemberData(employeeId: String, teamType: TeamType, data: TeamMemberDataEntity) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        repository.saveTeamMemberData(employeeId, teamType.departmentCode, data)
        auditLogDao.insert(
          AuditLogEntity(
            userId = employeeId,
            action = "SAVE_TEAM_DATA",
            entityType = "TEAM_MEMBER_DATA",
            entityId = "$employeeId-${teamType.departmentCode}",
            description = "Saved ${teamType.displayName} data for $employeeId"
          )
        )
        snackbarMessage.value = "${teamType.displayName} data saved successfully."
      } catch (e: Exception) {
        android.util.Log.e("WorkCoreViewModel", "Error saving team member data", e)
        snackbarMessage.value = "Failed to save ${teamType.displayName} data."
      }
    }
  }

  fun deleteTeamMemberData(employeeId: String, teamType: TeamType) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        repository.deleteTeamDataByEmployeeId(employeeId, teamType.departmentCode)
        auditLogDao.insert(
          AuditLogEntity(
            userId = employeeId,
            action = "DELETE_TEAM_DATA",
            entityType = "TEAM_MEMBER_DATA",
            entityId = "$employeeId-${teamType.departmentCode}",
            description = "Deleted ${teamType.displayName} data for $employeeId"
          )
        )
        snackbarMessage.value = "${teamType.displayName} data deleted."
      } catch (e: Exception) {
        android.util.Log.e("WorkCoreViewModel", "Error deleting team member data", e)
        snackbarMessage.value = "Failed to delete ${teamType.displayName} data."
      }
    }
  }
}

