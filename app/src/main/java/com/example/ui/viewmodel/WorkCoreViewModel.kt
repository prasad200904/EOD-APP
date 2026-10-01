package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
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
import com.example.data.WorkCoreRepository
import com.example.data.firebase.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

  val dailyEods: StateFlow<List<DailyEodEntity>> = repository.allDailyEods
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val managerTeamMembers: StateFlow<List<TeamMemberBehaviorItem>> = combine(
    selectedDepartmentCode,
    employees,
    dailyEods
  ) { dept, emps, eods ->
    repository.computeManagerTeamMetrics(dept, emps, eods, repository.getTodayDateString())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val teams: StateFlow<List<TeamEntity>> = repository.allTeams
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
        Role.EMPLOYEE -> notif.targetRole == "ALL" || notif.targetRole == "EMPLOYEE" || notif.targetEmployeeId == empId
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
      Role.EMPLOYEE -> {
        val dept = currentAuthSession.value?.departmentTeam ?: "GT Team"
        val deptCode = if (dept.contains("GT", true)) "GT"
          else if (dept.contains("ML", true)) "ML"
          else if (dept.contains("DB", true)) "DB"
          else if (dept.contains("Writing", true)) "Writing"
          else if (dept.contains("Cyber", true)) "Cyber"
          else "GT"
        emps.filter { it.department.equals(deptCode, true) || it.team.contains(deptCode, true) || it.departmentId.contains(deptCode, true) }
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
      Role.EMPLOYEE -> {
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
      Role.EMPLOYEE -> {
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
      Role.EMPLOYEE -> {
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
      Role.EMPLOYEE -> {
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
    }
    syncManager.startMonitoring(viewModelScope)
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
      Role.EMPLOYEE -> currentEmployeeId.value = "GT-001"
    }
    snackbarMessage.value = "Switched to ${when (role) {
      Role.ADMIN -> "Administrator (Full Access)"
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
        closeSubmitEod()
        // Trigger background sync to cloud
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
    password: String,
    onResult: (Boolean, String?) -> Unit
  ) {
    viewModelScope.launch {
      // Debug: Log all available teams
      android.util.Log.d("WorkCoreAuth", "Available teams: ${teams.value.map { it.name }}")
      android.util.Log.d("WorkCoreAuth", "Looking for team: '$teamName'")
      
      // Fetch team from database to verify password
      val team = teams.value.firstOrNull { 
        it.name.equals(teamName, ignoreCase = true) || 
        it.department.equals(teamName, ignoreCase = true)
      }
      
      if (team == null) {
        // More detailed error message
        val availableTeams = teams.value.joinToString(", ") { it.name }
        onResult(false, "Team '$teamName' not found. Available: $availableTeams")
        return@launch
      }
      
      android.util.Log.d("WorkCoreAuth", "Found team: ${team.name}, password check: ${team.teamPassword}")
      
      // Check if password matches
      if (team.teamPassword != password) {
        onResult(false, "Invalid password for $teamName")
        return@launch
      }
      
      // Password is correct - allow login
      currentDepartmentTeam.value = teamName
      currentRole.value = Role.EMPLOYEE
      activeRosterEmployee.value = null
      currentAuthSession.value = AuthSession(
        role = Role.EMPLOYEE,
        employee = null,
        departmentTeam = teamName
      )
      currentTab.value = AppTab.DASHBOARD
      snackbarMessage.value = "Signed in to $teamName roster"
      
      // AUTO-SYNC: Fetch employees from Firebase after successful login
      launch {
        android.util.Log.d("WorkCoreAuth", "🔄 Auto-syncing employees from Firebase...")
        val syncResult = repository.syncEmployeesFromFirebase()
        syncResult.fold(
          onSuccess = { count ->
            if (count > 0) {
              android.util.Log.i("WorkCoreAuth", "✅ Synced $count employees from Firebase")
            }
          },
          onFailure = { error ->
            android.util.Log.w("WorkCoreAuth", "⚠️ Sync failed: ${error.message}")
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
        currentAuthSession.value = AuthSession(role, user)
        currentRole.value = role
        currentEmployeeId.value = user.employeeId
        currentTab.value = AppTab.DASHBOARD
        snackbarMessage.value = "Welcome back, ${user.name}!"
        
        // AUTO-SYNC: Fetch employees from Firebase after successful login
        if (role == Role.ADMIN) {
          launch {
            android.util.Log.d("WorkCoreAuth", "🔄 Auto-syncing employees from Firebase (Admin login)...")
            val syncResult = repository.syncEmployeesFromFirebase()
            syncResult.fold(
              onSuccess = { count ->
                if (count > 0) {
                  android.util.Log.i("WorkCoreAuth", "✅ Synced $count employees from Firebase")
                }
              },
              onFailure = { error ->
                android.util.Log.w("WorkCoreAuth", "⚠️ Sync failed: ${error.message}")
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
    currentAuthSession.value = null
    currentTab.value = AppTab.DASHBOARD
    snackbarMessage.value = "Logged out successfully."
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
        
        // Trigger immediate sync to Firebase so other devices get the update
        syncManager.syncPendingToCloud()
        
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
          historyEodList.value = queryResult.items
        } else {
          historyEodList.value = historyEodList.value + queryResult.items
        }
        historyLastDoc.value = queryResult.lastDocumentSnapshot
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
        
        android.util.Log.i("WorkCoreViewModel", "✅ Team created: $teamName with password")
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
    password: String,
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
        
        // Create admin employee entity
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
          password = password
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
}

