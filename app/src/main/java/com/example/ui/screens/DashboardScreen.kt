package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyEodEntity
import com.example.data.DashboardSummary
import com.example.data.EmployeeBehaviorMetrics
import com.example.data.EmployeeEntity
import com.example.data.EmployeeWorkMonitorItem
import com.example.data.Role
import com.example.data.SeedData
import com.example.data.TeamEntity
import com.example.ui.components.EmployeeWorkMonitorCard
import com.example.ui.components.MetricCard
import com.example.ui.components.SegmentedStatusBar
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.StatusErrorBg
import com.example.ui.theme.StatusErrorText
import com.example.ui.theme.StatusInfoBg
import com.example.ui.theme.StatusInfoText
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingText
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.StatusWarningText
import com.example.ui.theme.WorkBackground
import com.example.ui.theme.WorkOutline
import com.example.ui.theme.WorkOutlineVariant
import com.example.ui.theme.WorkPrimary
import com.example.ui.theme.WorkPrimaryContainer
import com.example.ui.theme.WorkSurface
import com.example.ui.viewmodel.AppTab

@Composable
fun DashboardScreen(
  currentRole: Role,
  currentEmployeeId: String,
  summary: DashboardSummary,
  monitorItems: List<EmployeeWorkMonitorItem>,
  eods: List<DailyEodEntity>,
  employees: List<EmployeeEntity>,
  onNavigateTab: (AppTab) -> Unit,
  onOpenSubmitEod: (DailyEodEntity?) -> Unit,
  onOpenEmployeeDetail: (EmployeeEntity) -> Unit,
  onOpenAddEmployeeByManager: () -> Unit = {},
  onOpenCreateManager: () -> Unit = {},
  onOpenCreateTeam: () -> Unit = {},
  teams: List<TeamEntity> = emptyList(),
  onSubmitEmployeeDashboardEod: ((date: String, project: String, taskDescription: String, hoursWorked: Double, progressPercentage: Int, status: String) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val myTodayEod = eods.firstOrNull { it.employeeId == currentEmployeeId && it.date == SeedData.TODAY }
  val currentEmp = employees.firstOrNull { it.employeeId == currentEmployeeId }

  if (currentRole == Role.EMPLOYEE) {
    EmployeeDashboardScreen(
      employeeId = currentEmployeeId,
      employee = currentEmp,
      eods = eods,
      teams = teams,
      onSubmitEod = { date, project, task, hours, progress, status ->
        onSubmitEmployeeDashboardEod?.invoke(date, project, task, hours, progress, status)
      },
      onOpenEodPopup = { onOpenSubmitEod(myTodayEod) },
      modifier = modifier
    )
    return
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(WorkBackground)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Role & Quick EOD Submission Banner
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp)),
        color = Color(0xFF21005D)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = when (currentRole) {
                Role.ADMIN -> "Management Analytics Dashboard"
                Role.EMPLOYEE -> "My Daily EOD Tracker"
              },
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (myTodayEod != null) {
                "Today's EOD: Submitted (${myTodayEod.hoursWorked}h, ${myTodayEod.workStatus})"
              } else {
                "Today's EOD: Not Submitted Yet (Cutoff 18:30)"
              },
              fontSize = 12.sp,
              color = if (myTodayEod != null) Color(0xFFC8E6C9) else Color(0xFFFFD8E4)
            )
          }

          Button(
            onClick = { onOpenSubmitEod(myTodayEod) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (myTodayEod != null) Color.White.copy(alpha = 0.2f) else Color.White,
              contentColor = if (myTodayEod != null) Color.White else Color(0xFF21005D)
            ),
            modifier = Modifier.testTag("dashboard_submit_eod_cta")
          ) {
            Icon(
              imageVector = if (myTodayEod != null) Icons.Default.Edit else Icons.Default.Add,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (myTodayEod != null) "Edit EOD" else "Submit EOD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }

    // Admin Quick Actions
    if (currentRole == Role.ADMIN) {
      item {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text(
              text = "Admin Team & Manager Operations",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = Color(0xFF21005D)
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = onOpenCreateTeam,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WorkPrimary),
                modifier = Modifier
                  .weight(1f)
                  .testTag("admin_quick_create_team")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Team", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              OutlinedButton(
                onClick = onOpenCreateManager,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("admin_quick_create_manager")
              ) {
                Icon(Icons.Default.SupervisorAccount, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Manager", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // Clean Quick Navigation Bar
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp)),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = { onNavigateTab(AppTab.EOD_HISTORY) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Previous EOD", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }

          Button(
            onClick = { onNavigateTab(AppTab.DOWNLOAD) },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21005D)),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Download", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }

          OutlinedButton(
            onClick = { onNavigateTab(AppTab.EMPLOYEES) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Employees", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }

    // Section 8: Top 2x2 Metric Cards (~24dp radius)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "TODAY'S METRICS (${SeedData.TODAY})",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF79747E),
          letterSpacing = 0.5.sp
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          MetricCard(
            title = "Total Employees",
            count = "${summary.totalEmployees}",
            subtitle = "${summary.activeEmployees} active workforce",
            backgroundColor = Color(0xFFF3E7FF),
            textColor = Color(0xFF21005D),
            borderColor = WorkOutlineVariant,
            modifier = Modifier
              .weight(1f)
              .testTag("metric_total_employees"),
            onClick = { onNavigateTab(AppTab.MONITOR) }
          )
          MetricCard(
            title = "EOD Submitted",
            count = "${summary.todayEodSubmitted}",
            subtitle = "${summary.eodSubmissionRate}% reporting rate",
            backgroundColor = Color(0xFFE8F5E9),
            textColor = Color(0xFF1B5E20),
            borderColor = WorkOutlineVariant,
            modifier = Modifier
              .weight(1f)
              .testTag("metric_eod_submitted"),
            onClick = { onNavigateTab(AppTab.MONITOR) }
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          MetricCard(
            title = "EOD Pending",
            count = "${summary.todayEodPending}",
            subtitle = "Cutoff time 18:30",
            backgroundColor = if (summary.todayEodPending > 0) Color(0xFFFFEBEE) else Color(0xFFF5F5F5),
            textColor = if (summary.todayEodPending > 0) Color(0xFFB71C1C) else Color(0xFF49454F),
            borderColor = WorkOutlineVariant,
            modifier = Modifier
              .weight(1f)
              .testTag("metric_eod_pending"),
            onClick = { onNavigateTab(AppTab.MONITOR) }
          )
          MetricCard(
            title = "Avg Hours Logged",
            count = "${summary.avgWorkingHours}h",
            subtitle = "Avg Progress: ${summary.avgProgress}%",
            backgroundColor = Color(0xFFE3F2FD),
            textColor = Color(0xFF0D47A1),
            borderColor = WorkOutlineVariant,
            modifier = Modifier
              .weight(1f)
              .testTag("metric_avg_hours"),
            onClick = { onNavigateTab(AppTab.ANALYTICS) }
          )
        }
      }
    }

    // Section 8: Today's Work Status Breakdown
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp)),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TODAY'S WORK STATUS DISTRIBUTION",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF79747E),
              letterSpacing = 0.5.sp
            )
            Text(
              text = "Completion Rate: ${summary.completionRate}%",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1D6C2F)
            )
          }

          SegmentedStatusBar(
            completed = summary.statusBreakdown["Completed"] ?: 0,
            inProgress = summary.statusBreakdown["In Progress"] ?: 0,
            blocked = summary.statusBreakdown["Blocked"] ?: 0,
            noWorkOrLeave = (summary.statusBreakdown["No Work"] ?: 0) + (summary.statusBreakdown["On Leave"] ?: 0)
          )

          HorizontalDivider(color = WorkOutlineVariant)

          // Status Badges Count Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            StatusBadgeItem("Completed", summary.statusBreakdown["Completed"] ?: 0, Color(0xFF1D6C2F))
            StatusBadgeItem("In Progress", summary.statusBreakdown["In Progress"] ?: 0, Color(0xFF0D47A1))
            StatusBadgeItem("Blocked", summary.statusBreakdown["Blocked"] ?: 0, Color(0xFFE65100))
            StatusBadgeItem("Pending", summary.statusBreakdown["Pending"] ?: 0, Color(0xFFB71C1C))
          }
        }
      }
    }

    // Section 4 & 8 Preview: Employee Work Monitor Snapshot ("Who is working on what?")
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "EMPLOYEE WORK ACTIVITY (TODAY)",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF79747E),
          letterSpacing = 0.5.sp
        )
        Text(
          text = "View All (${monitorItems.size}) →",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = WorkPrimary,
          modifier = Modifier
            .clickable { onNavigateTab(AppTab.MONITOR) }
            .testTag("dashboard_view_all_monitor")
        )
      }
    }

    // Top 3 items from Work Monitor
    items(monitorItems.take(4)) { item ->
      EmployeeWorkMonitorCard(
        employeeName = item.employee.name,
        department = item.employee.department,
        todayWork = item.todayWork,
        hours = item.hours,
        progress = item.progress,
        status = item.workStatus,
        isEodSubmitted = item.isSubmittedToday,
        isOnTime = item.isOnTime,
        attentionReason = item.attentionReason,
        onClick = { onOpenEmployeeDetail(item.employee) }
      )
    }

    item {
      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}

@Composable
fun StatusBadgeItem(
  label: String,
  count: Int,
  color: Color
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = "$count",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = color
    )
    Text(
      text = label,
      fontSize = 10.sp,
      color = Color(0xFF49454F)
    )
  }
}
