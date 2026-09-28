package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.SimpleEodFormScreen
import com.example.ui.screens.DepartmentRosterScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.UpdateAvailableDialog
import com.example.ui.screens.CheckingUpdateDialog
import com.example.ui.theme.EodActivePillBg
import com.example.ui.theme.EodActivePillText
import com.example.ui.theme.EodAvatarBg
import com.example.ui.theme.EodAvatarText
import com.example.ui.theme.EodDarkBackground
import com.example.ui.theme.EodDarkCardBorder
import com.example.ui.theme.EodDarkInputBg
import com.example.ui.theme.EodDarkSurface
import com.example.ui.theme.EodTextPrimary
import com.example.ui.theme.EodTextSecondary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Role
import com.example.ui.screens.AddEmployeeByManagerDialog
import com.example.ui.screens.AddEmployeeScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CreateOrEditTeamDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DownloadEodScreen
import com.example.ui.screens.EmployeeDetailDialog
import com.example.ui.screens.ExportReportDialog
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.TeamHistoryScreen
import com.example.ui.screens.MoveEmployeeDialog
import com.example.ui.screens.NotificationCenterDialog
import com.example.ui.screens.PreviousEodScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SimpleEmployeesScreen
import com.example.ui.screens.SubmitEodDialog
import com.example.ui.screens.TeamsManagementScreen
import com.example.ui.screens.WorkMonitorScreen
import com.example.ui.viewmodel.AuthSession
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WorkBackground
import com.example.ui.theme.WorkOutlineVariant
import com.example.ui.theme.WorkPrimary
import com.example.ui.theme.WorkSurface
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.WorkCoreViewModel
import com.example.util.AppUpdateChecker
import com.example.util.AppVersion

class MainActivity : ComponentActivity() {
  private val viewModel: WorkCoreViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        WorkCoreApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun WorkCoreApp(viewModel: WorkCoreViewModel) {
  val authSession by viewModel.currentAuthSession.collectAsState()
  val currentRole by viewModel.currentRole.collectAsState()
  val currentEmployeeId by viewModel.currentEmployeeId.collectAsState()
  val currentTab by viewModel.currentTab.collectAsState()

  val employees by viewModel.employees.collectAsState()
  val visibleEmployees by viewModel.visibleEmployees.collectAsState()
  val visibleTeams by viewModel.visibleTeams.collectAsState()
  val dailyEods by viewModel.dailyEods.collectAsState()
  val visibleDailyEods by viewModel.visibleDailyEods.collectAsState()
  val teams by viewModel.teams.collectAsState()
  val notifications by viewModel.notifications.collectAsState()
  val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()

  val summary by viewModel.dashboardSummary.collectAsState()
  val monitorItems by viewModel.workMonitorItems.collectAsState()
  val behaviorMetrics by viewModel.behaviorMetrics.collectAsState()
  val teamSummaries by viewModel.teamAnalytics.collectAsState()

  val monitorFilter by viewModel.monitorFilter.collectAsState()
  val teamDeptFilter by viewModel.teamDepartmentFilter.collectAsState()

  val showSubmitEodDialog by viewModel.showSubmitEodDialog.collectAsState()
  val eodBeingEdited by viewModel.eodBeingEdited.collectAsState()
  val selectedEmployeeForDetail by viewModel.selectedEmployeeForDetail.collectAsState()

  val showExportDialog by viewModel.showExportDialog.collectAsState()
  val exportReportType by viewModel.exportReportType.collectAsState()
  val exportFormat by viewModel.exportFormat.collectAsState()
  val exportedReportContent by viewModel.exportedReportContent.collectAsState()

  val showNotificationsDialog by viewModel.showNotificationsDialog.collectAsState()
  val showCreateTeamDialog by viewModel.showCreateTeamDialog.collectAsState()
  val teamBeingEdited by viewModel.teamBeingEdited.collectAsState()
  val showMoveEmployeeDialog by viewModel.showMoveEmployeeDialog.collectAsState()
  val employeeToMove by viewModel.employeeToMove.collectAsState()

  val showAddEmployeeByManagerDialog by viewModel.showAddEmployeeByManagerDialog.collectAsState()

  val snackbarMessage by viewModel.snackbarMessage.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  val departments by viewModel.departments.collectAsState()
  val selectedDepartmentCode by viewModel.selectedDepartmentCode.collectAsState()
  val managerTeamMembers by viewModel.managerTeamMembers.collectAsState()
  var showAddEmployeeScreen by remember { mutableStateOf(false) }

  // App Update Check State
  val context = androidx.compose.ui.platform.LocalContext.current
  var showUpdateDialog by remember { mutableStateOf(false) }
  var showCheckingUpdate by remember { mutableStateOf(false) }
  var updateInfo by remember { mutableStateOf<AppVersion?>(null) }
  val currentVersionName = remember { AppUpdateChecker.getCurrentVersionName(context) }

  // Check for updates on app start (only once when user is logged in)
  LaunchedEffect(authSession) {
    if (authSession != null) {
      kotlinx.coroutines.delay(2000) // Wait 2 seconds after login
      showCheckingUpdate = true
      
      try {
        val update = AppUpdateChecker.checkForUpdate(context)
        showCheckingUpdate = false
        
        if (update != null) {
          // Don't show if user already dismissed this version (unless mandatory)
          if (update.isMandatory || !AppUpdateChecker.isUpdateDismissed(context, update.versionCode)) {
            updateInfo = update
            showUpdateDialog = true
          }
        }
      } catch (e: Exception) {
        showCheckingUpdate = false
      }
    }
  }

  LaunchedEffect(snackbarMessage) {
    snackbarMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearSnackbar()
    }
  }

  // If not logged in, show the single unified LoginScreen (Admin / Manager / Employee)
  if (authSession == null) {
    LoginScreen(
      viewModel = viewModel,
      onLoginSuccess = { /* auth session state triggers recomposition automatically */ }
    )
    return
  }

  var showProfileDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(EodDarkBackground)
      .statusBarsPadding()
      .navigationBarsPadding(),
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    bottomBar = {
      val activeEmp by viewModel.activeRosterEmployee.collectAsState()
      if (currentRole != Role.EMPLOYEE || activeEmp == null) {
        WorkCoreBottomNavigation(
          currentTab = currentTab,
          currentRole = currentRole,
          onTabSelected = {
            viewModel.closeRosterEmployee()
            viewModel.setTab(it)
          }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
        when (tab) {
          AppTab.DASHBOARD -> {
            if (showAddEmployeeScreen) {
              AddEmployeeScreen(
                currentManager = authSession?.employee,
                allDepartments = departments,
                onBackClick = { showAddEmployeeScreen = false },
                onCreateAccount = { name, email, code, joiningDate, designation, deptCode, sendLink ->
                  viewModel.createEmployeeScoped(
                    name = name,
                    email = email,
                    employeeCode = code,
                    joiningDate = joiningDate,
                    designation = designation,
                    departmentCode = deptCode,
                    sendEmailLink = sendLink,
                    onSuccess = {
                      showAddEmployeeScreen = false
                    }
                  )
                }
              )
            } else if (currentRole == Role.ADMIN) {
              val submittedToday = managerTeamMembers.count { it.todayStatus == "Done" }
              val onLeaveToday = managerTeamMembers.count { it.todayStatus == "Leave" }
              val totalTeam = managerTeamMembers.size

              AdminDashboardScreen(
                allDepartments = departments,
                selectedDepartmentCode = selectedDepartmentCode,
                onDepartmentSelect = { viewModel.selectedDepartmentCode.value = it },
                teamMembers = managerTeamMembers,
                submittedCount = submittedToday,
                totalCount = totalTeam,
                onLeaveCount = onLeaveToday,
                onNotificationsClick = { viewModel.showNotificationsDialog.value = true },
                onAvatarClick = { showProfileDialog = true },
                onSendNudge = { emp ->
                  viewModel.snackbarMessage.value = "Nudge notification sent to ${emp.name}."
                },
                onAddEmployeeClick = { showAddEmployeeScreen = true }
              )
            } else {
              val activeEmp by viewModel.activeRosterEmployee.collectAsState()
              if (activeEmp != null) {
                // Use the new Simple EOD Form
                SimpleEodFormScreen(
                  currentEmployee = activeEmp,
                  onSubmit = { projectTitle, description, status ->
                    // Get current date from repository
                    val currentDate = viewModel.repository.getTodayDateString()
                    
                    // Map the simple form data to submitRosterEod parameters
                    viewModel.submitRosterEod(
                      employee = activeEmp!!,
                      date = currentDate,
                      project = projectTitle,
                      taskDescription = description,
                      hoursWorked = 8.0, // Default full day
                      progressPercentage = when (status) {
                        "Completed" -> 100
                        "In Progress" -> 50
                        "Blocked" -> 0
                        else -> 50
                      },
                      workStatus = status,
                      onComplete = {
                        viewModel.closeRosterEmployee()
                      }
                    )
                  },
                  onBackClick = { viewModel.closeRosterEmployee() }
                )
              } else {
                val currentDept = authSession?.departmentTeam ?: "GT Team"
                DepartmentRosterScreen(
                  departmentName = currentDept,
                  employees = employees,
                  todayEods = dailyEods,
                  onSelectEmployee = { emp ->
                    viewModel.selectRosterEmployee(emp)
                  },
                  onLogout = {
                    viewModel.logout()
                  }
                )
              }
            }
          }
          AppTab.EOD_HISTORY -> {
            if (currentRole == Role.ADMIN) {
              TeamHistoryScreen(
                currentManager = authSession?.employee,
                allDepartments = departments,
                selectedDepartmentCode = selectedDepartmentCode,
                onDepartmentSelect = { viewModel.selectedDepartmentCode.value = it },
                teamMembers = managerTeamMembers,
                eods = dailyEods,
                onAvatarClick = { showProfileDialog = true },
                onLogout = { viewModel.logout() }
              )
            } else {
              val currentDeptName = authSession?.departmentTeam ?: "GT Team"
              val deptCode = if (currentDeptName.contains("GT", ignoreCase = true)) "GT"
                else if (currentDeptName.contains("ML", ignoreCase = true)) "ML"
                else if (currentDeptName.contains("DB", ignoreCase = true)) "DB"
                else if (currentDeptName.contains("Writing", ignoreCase = true)) "Writing"
                else if (currentDeptName.contains("Cyber", ignoreCase = true)) "Cyber"
                else "GT"

              val gtMembers = remember(employees, dailyEods, deptCode) {
                viewModel.repository.computeManagerTeamMetrics(deptCode, employees, dailyEods)
              }

              TeamHistoryScreen(
                currentManager = null,
                allDepartments = departments,
                selectedDepartmentCode = deptCode,
                onDepartmentSelect = {},
                teamMembers = gtMembers,
                eods = dailyEods,
                onAvatarClick = { showProfileDialog = true },
                showDepartmentChips = false,
                customTitle = "$currentDeptName History",
                onLogout = { viewModel.logout() }
              )
            }
          }
          AppTab.DOWNLOAD -> {
            val currentDeptName = authSession?.departmentTeam
              ?: authSession?.employee?.team
              ?: "GT Team"

            DownloadEodScreen(
              eods = dailyEods,
              employees = employees,
              teams = teams,
              currentRole = currentRole,
              initialDepartment = currentDeptName,
              onAvatarClick = { showProfileDialog = true },
              onLogout = { viewModel.logout() }
            )
          }
          AppTab.EMPLOYEES -> SimpleEmployeesScreen(
            employees = visibleEmployees,
            eods = visibleDailyEods,
            teams = visibleTeams,
            currentRole = currentRole,
            onOpenAddEmployee = {
              showAddEmployeeScreen = true
              viewModel.setTab(AppTab.DASHBOARD)
            },
            onToggleEmployeeActive = { empId, currentActive ->
              viewModel.toggleEmployeeActiveStatus(empId, currentActive)
            },
            onDeleteEmployeeAccount = { empId -> viewModel.deleteEmployeeAccount(empId) }
          )
          AppTab.TEAMS -> TeamsManagementScreen(
            teams = visibleTeams,
            employees = visibleEmployees,
            dailyEods = dailyEods,
            currentRole = currentRole,
            onCreateTeamClick = { viewModel.showCreateTeamDialog.value = true },
            onEditTeamClick = { team -> viewModel.teamBeingEdited.value = team },
            onMoveEmployeeClick = { emp ->
              viewModel.employeeToMove.value = emp
              viewModel.showMoveEmployeeDialog.value = true
            },
            onToggleTeamStatus = { teamId, status ->
              viewModel.setTeamStatus(teamId, if (status == "Active") "Inactive" else "Active")
            },
            onAddEmployeeClick = { viewModel.showAddEmployeeByManagerDialog.value = true }
          )
          AppTab.MONITOR -> WorkMonitorScreen(
            monitorItems = monitorItems,
            currentFilter = monitorFilter,
            onFilterChange = { viewModel.monitorFilter.value = it },
            onSelectEmployee = { emp -> viewModel.selectedEmployeeForDetail.value = emp }
          )
          AppTab.ANALYTICS -> AnalyticsScreen(
            behaviorMetrics = behaviorMetrics,
            teamSummaries = teamSummaries,
            departmentFilter = teamDeptFilter,
            onDepartmentFilterChange = { viewModel.teamDepartmentFilter.value = it },
            onSelectEmployee = { emp -> viewModel.selectedEmployeeForDetail.value = emp }
          )
        }
      }
    }
  }

  // Profile and Account Dialog
  if (showProfileDialog) {
    Dialog(onDismissRequest = { showProfileDialog = false }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = EodDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, EodDarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(EodAvatarBg),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "RK",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = EodAvatarText
              )
            }

            Column {
              Text(
                text = authSession?.employee?.name ?: "Ravi Kumar",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = EodTextPrimary
              )
              Text(
                text = "${authSession?.employee?.designation ?: "Senior Full-Stack Engineer"} (${authSession?.employee?.employeeId ?: "EMP001"})",
                fontSize = 12.sp,
                color = EodTextSecondary
              )
            }
          }

          HorizontalDivider(color = EodDarkCardBorder)

          Text("Switch Role (Demo)", fontSize = 13.sp, color = EodTextSecondary)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(Role.EMPLOYEE, Role.ADMIN).forEach { r ->
              val isSel = currentRole == r
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSel) EodActivePillBg else EodDarkInputBg)
                  .border(1.dp, if (isSel) Color.Transparent else EodDarkCardBorder, RoundedCornerShape(8.dp))
                  .clickable { viewModel.setRole(r) }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = r.name,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSel) EodActivePillText else EodTextSecondary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Button(
            onClick = {
              showProfileDialog = false
              viewModel.logout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B151E)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Log Out", color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  // Section 2: EOD Submission Dialog
  if (showSubmitEodDialog) {
    val available = if (currentRole == Role.EMPLOYEE) {
      visibleEmployees.filter { it.employeeId == currentEmployeeId }
    } else {
      visibleEmployees
    }

    SubmitEodDialog(
      availableEmployees = available,
      currentEmployeeId = currentEmployeeId,
      currentRole = currentRole,
      existingEod = eodBeingEdited,
      onDismiss = { viewModel.closeSubmitEod() },
      onSubmit = { empId, empName, dt, proj, work, comp, hrs, prg, st, blk, rmk, tmrw, isEdit ->
        viewModel.submitEod(empId, empName, dt, proj, work, comp, hrs, prg, st, blk, rmk, tmrw, isEdit)
      }
    )
  }

  // Section 7: Employee Profile Analytics Dialog
  selectedEmployeeForDetail?.let { emp ->
    val empMetrics = behaviorMetrics.firstOrNull { it.employee.employeeId == emp.employeeId }
    EmployeeDetailDialog(
      employee = emp,
      metrics = empMetrics,
      eods = dailyEods,
      onDismiss = { viewModel.selectedEmployeeForDetail.value = null }
    )
  }

  // Section 11: Export Report Dialog (Multi-Sheet Excel / CSV)
  if (showExportDialog && exportedReportContent != null) {
    ExportReportDialog(
      reportType = exportReportType,
      reportContent = exportedReportContent!!,
      format = exportFormat,
      onDismiss = { viewModel.showExportDialog.value = false }
    )
  }

  // Notification Center Dialog
  if (showNotificationsDialog) {
    NotificationCenterDialog(
      notifications = notifications,
      onDismiss = { viewModel.showNotificationsDialog.value = false },
      onMarkAllAsRead = { viewModel.markAllNotificationsRead() },
      onSelectNotification = { notif ->
        viewModel.showNotificationsDialog.value = false
        viewModel.setTab(AppTab.MONITOR)
      }
    )
  }

  // Create or Edit Team Dialog
  if (showCreateTeamDialog || teamBeingEdited != null) {
    CreateOrEditTeamDialog(
      teamToEdit = teamBeingEdited,
      employees = employees,
      onDismiss = {
        viewModel.showCreateTeamDialog.value = false
        viewModel.teamBeingEdited.value = null
      },
      onSaveTeam = { name, dept, mgr, projs, desc ->
        viewModel.createTeam(name, dept, mgr, projs, desc)
      },
      onUpdateTeam = { team ->
        viewModel.updateTeam(team)
      }
    )
  }

  // Move Employee Dialog
  if (showMoveEmployeeDialog && teams.isNotEmpty()) {
    MoveEmployeeDialog(
      employeePreselected = employeeToMove,
      employees = employees,
      teams = teams,
      onDismiss = {
        viewModel.showMoveEmployeeDialog.value = false
        viewModel.employeeToMove.value = null
      },
      onConfirmMove = { empId, newTeam ->
        viewModel.moveEmployeeToTeam(empId, newTeam)
      }
    )
  }

  // Manager: Add Employee to Own Team Dialog
  if (showAddEmployeeByManagerDialog) {
    val currentManager = authSession?.employee ?: employees.firstOrNull { it.employeeId == currentEmployeeId }
    if (currentManager != null) {
      AddEmployeeByManagerDialog(
        manager = currentManager,
        onDismiss = { viewModel.showAddEmployeeByManagerDialog.value = false },
        onCreateAccount = { name, employeeId, password, confirmPassword ->
          viewModel.createEmployeeByManager(name, employeeId, password, confirmPassword)
        }
      )
    }
  }

  // App Update Dialogs
  if (showCheckingUpdate) {
    CheckingUpdateDialog()
  }

  if (showUpdateDialog && updateInfo != null) {
    UpdateAvailableDialog(
      currentVersion = currentVersionName,
      updateInfo = updateInfo!!,
      onDownloadClick = {
        // Open download URL in browser
        if (updateInfo!!.downloadUrl.isNotBlank()) {
          AppUpdateChecker.openDownloadUrl(context, updateInfo!!.downloadUrl)
        }
        // Don't dismiss for mandatory updates
        if (!updateInfo!!.isMandatory) {
          showUpdateDialog = false
        }
      },
      onDismiss = {
        // Mark as dismissed (won't show again for this version)
        AppUpdateChecker.markUpdateDismissed(context, updateInfo!!.versionCode)
        showUpdateDialog = false
      }
    )
  }


}

@Composable
fun WorkCoreTopBar(
  currentRole: Role,
  currentEmployeeId: String,
  authSession: AuthSession? = null,
  unreadNotificationsCount: Int = 0,
  onNotificationsClick: () -> Unit = {},
  onOpenEod: () -> Unit = {},
  onRoleChanged: (Role) -> Unit = {},
  onLogout: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var showRoleMenu by remember { mutableStateOf(false) }

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = WorkSurface,
    shadowElevation = 1.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.weight(1f, fill = false)
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(WorkPrimary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Assessment,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
        Column {
          Text(
            text = "WorkCore",
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = WorkPrimary,
            letterSpacing = (-0.5).sp
          )
          Text(
            text = authSession?.let {
              val empName = it.employee?.name ?: (it.departmentTeam ?: "Team")
              val teamName = if (it.role == Role.ADMIN) "Admin" else (it.employee?.team ?: (it.departmentTeam ?: "Team"))
              "$empName ($teamName)"
            } ?: "EOD & Work Analytics",
            fontSize = 10.sp,
            color = Color(0xFF49454F),
            fontWeight = FontWeight.Medium,
            maxLines = 1
          )
        }
      }

      // Actions: EOD Button + Notification Bell + Role Persona Switcher + Logout
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Instant EOD Form Popup Button
        Button(
          onClick = onOpenEod,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF21005D),
            contentColor = Color.White
          ),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("topbar_eod_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("EOD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // Notification bell with unread badge
        BadgedBox(
          badge = {
            if (unreadNotificationsCount > 0) {
              Badge(
                containerColor = Color(0xFFB3261E),
                contentColor = Color.White
              ) {
                Text(unreadNotificationsCount.toString(), fontSize = 10.sp)
              }
            }
          }
        ) {
          IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier
              .size(36.dp)
              .testTag("notification_bell_button")
          ) {
            Icon(
              Icons.Default.Notifications,
              contentDescription = "Notifications",
              tint = Color(0xFF21005D),
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Role Persona Switcher
        Box {
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .border(1.dp, WorkOutlineVariant, RoundedCornerShape(10.dp))
              .clickable { showRoleMenu = true }
              .testTag("role_switcher_button"),
            color = Color(0xFFF3E7FF)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(
                    when (currentRole) {
                      Role.ADMIN -> Color(0xFFB3261E)
                      Role.EMPLOYEE -> Color(0xFF1D6C2F)
                    }
                  )
              )
              Text(
                text = when (currentRole) {
                  Role.ADMIN -> "Admin"
                  Role.EMPLOYEE -> "Employee"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
              )
              Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF21005D),
                modifier = Modifier.size(12.dp)
              )
            }
          }

          DropdownMenu(
            expanded = showRoleMenu,
            onDismissRequest = { showRoleMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Admin View (Full Access)") },
              onClick = {
                onRoleChanged(Role.ADMIN)
                showRoleMenu = false
              }
            )
            DropdownMenuItem(
              text = { Text("Department View (GT Team)") },
              onClick = {
                onRoleChanged(Role.EMPLOYEE)
                showRoleMenu = false
              }
            )
            androidx.compose.material3.HorizontalDivider()
            DropdownMenuItem(
              text = { Text("Log Out to Login Screen", color = Color(0xFFB3261E), fontWeight = FontWeight.Bold) },
              leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFB3261E), modifier = Modifier.size(18.dp)) },
              onClick = {
                showRoleMenu = false
                onLogout()
              }
            )
          }
        }

        // Dedicated Logout Button
        IconButton(
          onClick = onLogout,
          modifier = Modifier
            .size(36.dp)
            .testTag("top_bar_logout_button")
        ) {
          Icon(
            Icons.Default.ExitToApp,
            contentDescription = "Log Out",
            tint = Color(0xFF79747E),
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun WorkCoreBottomNavigation(
  currentTab: AppTab,
  currentRole: Role = Role.EMPLOYEE,
  onTabSelected: (AppTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = Color(0xFF0F0F12),
    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF202128))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (currentRole == Role.ADMIN) {
        // 1. Team (Admin)
        val isTeam = currentTab == AppTab.DASHBOARD
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onTabSelected(AppTab.DASHBOARD) }
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("nav_tab_team")
        ) {
          Icon(
            imageVector = Icons.Default.Group,
            contentDescription = "Team",
            tint = if (isTeam) Color(0xFF38BDF8) else Color(0xFF71717A),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "Team",
            fontSize = 11.sp,
            fontWeight = if (isTeam) FontWeight.Bold else FontWeight.Medium,
            color = if (isTeam) Color(0xFF38BDF8) else Color(0xFF71717A)
          )
        }

        // 2. History
        val isHistory = currentTab == AppTab.EOD_HISTORY
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onTabSelected(AppTab.EOD_HISTORY) }
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("nav_tab_history")
        ) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = "History",
            tint = if (isHistory) Color(0xFF38BDF8) else Color(0xFF71717A),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "History",
            fontSize = 11.sp,
            fontWeight = if (isHistory) FontWeight.Bold else FontWeight.Medium,
            color = if (isHistory) Color(0xFF38BDF8) else Color(0xFF71717A)
          )
        }

        // 3. People
        val isPeople = currentTab == AppTab.EMPLOYEES
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onTabSelected(AppTab.EMPLOYEES) }
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("nav_tab_people")
        ) {
          Icon(
            imageVector = Icons.Default.PersonOutline,
            contentDescription = "People",
            tint = if (isPeople) Color(0xFF38BDF8) else Color(0xFF71717A),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "People",
            fontSize = 11.sp,
            fontWeight = if (isPeople) FontWeight.Bold else FontWeight.Medium,
            color = if (isPeople) Color(0xFF38BDF8) else Color(0xFF71717A)
          )
        }

        // 4. Reports
        val isReports = currentTab == AppTab.DOWNLOAD
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onTabSelected(AppTab.DOWNLOAD) }
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("nav_tab_reports")
        ) {
          Icon(
            imageVector = Icons.Default.Download,
            contentDescription = "Reports",
            tint = if (isReports) Color(0xFF38BDF8) else Color(0xFF71717A),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "Reports",
            fontSize = 11.sp,
            fontWeight = if (isReports) FontWeight.Bold else FontWeight.Medium,
            color = if (isReports) Color(0xFF38BDF8) else Color(0xFF71717A)
          )
        }
      } else {
        // Employee Portal Bottom Navigation
        // 1. Team
        val isTeam = currentTab == AppTab.DASHBOARD
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onTabSelected(AppTab.DASHBOARD) }
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .testTag("nav_tab_team")
        ) {
          Icon(
            imageVector = Icons.Default.Group,
            contentDescription = "Team",
            tint = if (isTeam) Color(0xFF38BDF8) else Color(0xFF71717A),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "Team",
            fontSize = 11.sp,
            fontWeight = if (isTeam) FontWeight.Bold else FontWeight.Medium,
            color = if (isTeam) Color(0xFF38BDF8) else Color(0xFF71717A)
          )
        }

        // 2. History
        val isHistory = currentTab == AppTab.EOD_HISTORY
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onTabSelected(AppTab.EOD_HISTORY) }
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .testTag("nav_tab_history")
        ) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = "History",
            tint = if (isHistory) Color(0xFF38BDF8) else Color(0xFF71717A),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "History",
            fontSize = 11.sp,
            fontWeight = if (isHistory) FontWeight.Bold else FontWeight.Medium,
            color = if (isHistory) Color(0xFF38BDF8) else Color(0xFF71717A)
          )
        }

        // 3. Reports
        val isReports = currentTab == AppTab.DOWNLOAD
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onTabSelected(AppTab.DOWNLOAD) }
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .testTag("nav_tab_reports")
        ) {
          Icon(
            imageVector = Icons.Default.Download,
            contentDescription = "Reports",
            tint = if (isReports) Color(0xFF38BDF8) else Color(0xFF71717A),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "Reports",
            fontSize = 11.sp,
            fontWeight = if (isReports) FontWeight.Bold else FontWeight.Medium,
            color = if (isReports) Color(0xFF38BDF8) else Color(0xFF71717A)
          )
        }
      }
    }
  }
}
