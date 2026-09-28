package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.util.FileDownloadHelper
import com.example.util.DownloadResult
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import java.util.Calendar
import java.util.Locale
import com.example.data.NotificationEntity
import com.example.data.TeamEntity
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.DailyEodEntity
import com.example.data.EmployeeBehaviorMetrics
import com.example.data.EmployeeEntity
import com.example.data.Role
import com.example.data.SeedData
import com.example.ui.components.IndicatorBadge
import com.example.ui.components.MetricCard
import com.example.ui.components.SegmentedStatusBar
import com.example.ui.components.SimpleBarChart
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.StatusErrorBg
import com.example.ui.theme.StatusErrorText
import com.example.ui.theme.StatusInfoBg
import com.example.ui.theme.StatusInfoText
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

/**
 * Section 2: Daily EOD Submission Dialog
 */
@Composable
fun SubmitEodDialog(
  availableEmployees: List<EmployeeEntity>,
  currentEmployeeId: String,
  currentRole: Role,
  existingEod: DailyEodEntity?,
  onDismiss: () -> Unit,
  onSubmit: (
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
  ) -> Unit
) {
  val context = LocalContext.current
  val defaultEmp = availableEmployees.firstOrNull { it.employeeId == currentEmployeeId }
    ?: availableEmployees.firstOrNull()
    ?: EmployeeEntity(employeeId = "EMP001", name = "Ravi Kumar", email = "", phone = "", department = "Engineering", designation = "Developer", joiningDate = "2024-01-01")

  var date by remember { mutableStateOf(existingEod?.date ?: SeedData.TODAY) }
  var empName by remember {
    mutableStateOf(existingEod?.employeeName ?: defaultEmp.name)
  }
  var selectedEmpId by remember {
    mutableStateOf(existingEod?.employeeId ?: defaultEmp.employeeId)
  }
  var project by remember {
    mutableStateOf(existingEod?.project ?: defaultEmp.defaultProject)
  }
  var workStatus by remember {
    mutableStateOf(existingEod?.workStatus ?: "Completed")
  }
  var todayWork by remember {
    mutableStateOf(existingEod?.todayWork ?: "")
  }
  var empDropdownExpanded by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val statusOptions = listOf("Completed", "In Progress", "Blocked", "No Work", "On Leave")

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(20.dp)),
      color = Color.White,
      shadowElevation = 8.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Dialog Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF21005D)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column {
              Text(
                text = if (existingEod != null) "Edit EOD Form" else "EOD Form",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
              )
              Text(
                text = "Daily work status submission",
                fontSize = 12.sp,
                color = Color(0xFF49454F)
              )
            }
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF49454F))
          }
        }

        HorizontalDivider(color = WorkOutlineVariant)

        // Form Fields (Date, Emp-Name, Assigned Project [manual typing], Status of Work)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // 1. DATE
          OutlinedTextField(
            value = date,
            onValueChange = {
              date = it
              errorMessage = null
            },
            label = { Text("Date *") },
            placeholder = { Text("YYYY-MM-DD") },
            singleLine = true,
            leadingIcon = {
              Icon(Icons.Default.CalendarToday, contentDescription = null, tint = WorkPrimary, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
              IconButton(onClick = {
                val c = Calendar.getInstance()
                DatePickerDialog(
                  context,
                  { _, y, m, d ->
                    date = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                    errorMessage = null
                  },
                  c.get(Calendar.YEAR),
                  c.get(Calendar.MONTH),
                  c.get(Calendar.DAY_OF_MONTH)
                ).show()
              }) {
                Icon(Icons.Default.DateRange, contentDescription = "Pick Date", tint = WorkPrimary)
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("eod_form_date"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = WorkPrimary,
              unfocusedBorderColor = WorkOutline
            )
          )

          // 2. EMP-NAME (Employee Name)
          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = empName,
              onValueChange = {
                empName = it
                errorMessage = null
              },
              label = { Text("Emp-Name *") },
              placeholder = { Text("Enter employee name...") },
              singleLine = true,
              leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null, tint = WorkPrimary, modifier = Modifier.size(18.dp))
              },
              trailingIcon = {
                if (availableEmployees.isNotEmpty()) {
                  IconButton(onClick = { empDropdownExpanded = !empDropdownExpanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Employee", tint = WorkPrimary)
                  }
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("eod_form_emp_name"),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WorkPrimary,
                unfocusedBorderColor = WorkOutline
              )
            )

            DropdownMenu(
              expanded = empDropdownExpanded,
              onDismissRequest = { empDropdownExpanded = false }
            ) {
              availableEmployees.forEach { emp ->
                DropdownMenuItem(
                  text = { Text("${emp.name} (${emp.employeeId} - ${emp.team})") },
                  onClick = {
                    empName = emp.name
                    selectedEmpId = emp.employeeId
                    if (project.isBlank() || project == defaultEmp.defaultProject) {
                      project = emp.defaultProject
                    }
                    empDropdownExpanded = false
                  }
                )
              }
            }
          }

          // 3. ASSIGNED PROJECT (Manual typing section)
          OutlinedTextField(
            value = project,
            onValueChange = {
              project = it
              errorMessage = null
            },
            label = { Text("Assigned Project (Manual Typing) *") },
            placeholder = { Text("Type assigned project name...") },
            singleLine = true,
            leadingIcon = {
              Icon(Icons.Default.Work, contentDescription = null, tint = WorkPrimary, modifier = Modifier.size(18.dp))
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("eod_form_project"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = WorkPrimary,
              unfocusedBorderColor = WorkOutline
            )
          )

          // 4. STATUS OF WORK
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Status of Work *",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF21005D)
              )
              StatusBadge(status = workStatus)
            }

            // Status selection chips
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              statusOptions.forEach { statusOption ->
                val isSelected = workStatus.equals(statusOption, ignoreCase = true)
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    workStatus = statusOption
                    errorMessage = null
                  },
                  label = {
                    Text(
                      text = statusOption,
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (statusOption) {
                      "Completed" -> Color(0xFFC8E6C9)
                      "In Progress" -> Color(0xFFD0E4FF)
                      "Blocked" -> Color(0xFFFFCDD2)
                      "On Leave" -> Color(0xFFFFE0B2)
                      else -> Color(0xFFE0E0E0)
                    },
                    selectedLabelColor = Color(0xFF1D1B20)
                  )
                )
              }
            }
          }

          // Optional Work Summary / Notes
          OutlinedTextField(
            value = todayWork,
            onValueChange = { todayWork = it },
            label = { Text("Work Details / Notes (Optional)") },
            placeholder = { Text("Brief summary of work done...") },
            minLines = 2,
            maxLines = 3,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("eod_form_work_details"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = WorkPrimary,
              unfocusedBorderColor = WorkOutline
            )
          )

          // Error banner
          if (errorMessage != null) {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
              color = Color(0xFFFFDAD6)
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB3261E), modifier = Modifier.size(16.dp))
                Text(errorMessage!!, color = Color(0xFF410002), fontSize = 12.sp)
              }
            }
          }
        }

        HorizontalDivider(color = WorkOutlineVariant)

        // Actions: Cancel and Submit EOD
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(10.dp))
          Button(
            onClick = {
              if (date.isBlank()) {
                errorMessage = "Date is required"
                return@Button
              }
              if (empName.isBlank()) {
                errorMessage = "Emp-name is required"
                return@Button
              }
              if (project.isBlank()) {
                errorMessage = "Assigned project is required (manual typing)"
                return@Button
              }
              if (workStatus.isBlank()) {
                errorMessage = "Status of work is required"
                return@Button
              }

              errorMessage = null

              // Match employeeId by empName if user typed someone from the roster, or fallback to selectedEmpId
              val matchedEmp = availableEmployees.firstOrNull { it.name.equals(empName.trim(), ignoreCase = true) }
              val finalEmpId = matchedEmp?.employeeId ?: selectedEmpId

              val taskSummary = if (todayWork.isNotBlank()) todayWork.trim() else "$workStatus work on $project"
              val hours = if (workStatus == "On Leave" || workStatus == "No Work") 0.0 else existingEod?.hoursWorked ?: 8.0
              val progress = when (workStatus) {
                "Completed" -> 100
                "On Leave", "No Work" -> 0
                else -> existingEod?.progressPercentage ?: 75
              }

              onSubmit(
                finalEmpId,
                empName.trim(),
                date.trim(),
                project.trim(),
                taskSummary,
                existingEod?.workCompleted ?: taskSummary,
                hours,
                progress,
                workStatus,
                if (workStatus == "Blocked") (existingEod?.blockers?.ifBlank { "Work Blocked" } ?: "Work Blocked") else "",
                existingEod?.remarks ?: "",
                existingEod?.tomorrowPlan ?: "",
                existingEod != null
              )
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21005D)),
            modifier = Modifier.testTag("submit_eod_button")
          ) {
            Text(if (existingEod != null) "Update EOD" else "Submit EOD", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

/**
 * Section 7: Individual Employee Profile Analytics Dialog
 */
@Composable
fun EmployeeDetailDialog(
  employee: EmployeeEntity,
  metrics: EmployeeBehaviorMetrics?,
  eods: List<DailyEodEntity>,
  onDismiss: () -> Unit
) {
  val empEods = eods.filter { it.employeeId == employee.employeeId }.sortedByDescending { it.date }

  // Chart data points
  val hoursPoints = empEods.reversed().map { it.date to it.hoursWorked }
  val progressPoints = empEods.reversed().map { it.date to it.progressPercentage.toDouble() }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.92f)
        .clip(RoundedCornerShape(24.dp)),
      color = WorkBackground
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = Color.White,
          shadowElevation = 2.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              UserAvatar(name = employee.name, size = 44)
              Column {
                Text(employee.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                Text(
                  "${employee.employeeId} · ${employee.designation} · ${employee.department} (${employee.team})",
                  fontSize = 12.sp,
                  color = Color(0xFF49454F)
                )
              }
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF49454F))
            }
          }
        }

        // Body Content
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Alert banner if attention needed
          if (metrics?.attentionAlert != null) {
            item {
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp)),
                color = Color(0xFFFFEBEE),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF2B8B5))
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB3261E))
                  Column {
                    Text("Attention Required", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB3261E))
                    Text(metrics.attentionAlert, fontSize = 12.sp, color = Color(0xFF410002))
                  }
                }
              }
            }
          }

          // Section 7 Summary Cards (2x3 grid)
          item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text("PERFORMANCE & CONSISTENCY SUMMARY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF79747E), letterSpacing = 0.5.sp)

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                  title = "Submission Rate",
                  count = "${metrics?.submissionRate ?: 0}%",
                  subtitle = "${metrics?.submittedEods ?: 0} of ${metrics?.totalExpectedEods ?: 0} days",
                  backgroundColor = Color(0xFFF3E7FF),
                  textColor = Color(0xFF21005D),
                  borderColor = WorkOutlineVariant,
                  modifier = Modifier.weight(1f)
                )
                MetricCard(
                  title = "Avg Hours",
                  count = "${metrics?.avgHours ?: 0.0}h",
                  subtitle = "Weekly ~${metrics?.weeklyAvgHours ?: 0.0}h",
                  backgroundColor = Color(0xFFE8F5E9),
                  textColor = Color(0xFF1B5E20),
                  borderColor = WorkOutlineVariant,
                  modifier = Modifier.weight(1f)
                )
              }

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                  title = "Avg Progress",
                  count = "${metrics?.avgProgress ?: 0}%",
                  subtitle = "Trend: ${metrics?.progressTrend ?: "Stable"}",
                  backgroundColor = Color(0xFFE3F2FD),
                  textColor = Color(0xFF0D47A1),
                  borderColor = WorkOutlineVariant,
                  modifier = Modifier.weight(1f)
                )
                MetricCard(
                  title = "On-Time Rate",
                  count = "${metrics?.onTimeRate ?: 0}%",
                  subtitle = "${metrics?.lateCount ?: 0} late submissions",
                  backgroundColor = Color(0xFFFFF3E0),
                  textColor = Color(0xFFE65100),
                  borderColor = WorkOutlineVariant,
                  modifier = Modifier.weight(1f)
                )
              }

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                  title = "Missed EODs",
                  count = "${metrics?.missingEods ?: 0}",
                  subtitle = "${metrics?.consecutiveMissedEods ?: 0} consecutive missed",
                  backgroundColor = if ((metrics?.missingEods ?: 0) > 2) Color(0xFFFFEBEE) else Color(0xFFF5F5F5),
                  textColor = if ((metrics?.missingEods ?: 0) > 2) Color(0xFFB71C1C) else Color(0xFF49454F),
                  borderColor = WorkOutlineVariant,
                  modifier = Modifier.weight(1f)
                )
                MetricCard(
                  title = "Blocked Days",
                  count = "${metrics?.blockedDaysCount ?: 0}",
                  subtitle = "${metrics?.blockedWorkPercentage ?: 0}% of reported days",
                  backgroundColor = if ((metrics?.blockedDaysCount ?: 0) >= 2) Color(0xFFFFF8E1) else Color(0xFFF5F5F5),
                  textColor = if ((metrics?.blockedDaysCount ?: 0) >= 2) Color(0xFFF57F17) else Color(0xFF49454F),
                  borderColor = WorkOutlineVariant,
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }

          // Section 6: Work Behavior Indicators
          if (metrics != null) {
            item {
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp)),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
              ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                  Text("WORK BEHAVIOR INDICATORS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF79747E), letterSpacing = 0.5.sp)

                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("EOD Consistency", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    IndicatorBadge(indicator = metrics.eodConsistencyStatus)
                  }
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Work Consistency", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    IndicatorBadge(indicator = metrics.workConsistencyStatus)
                  }
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Progress Consistency", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    IndicatorBadge(indicator = metrics.progressConsistencyStatus)
                  }
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Reporting Reliability", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    IndicatorBadge(indicator = metrics.reportingReliabilityStatus)
                  }
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Completion Pattern", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    IndicatorBadge(indicator = metrics.completionPatternStatus)
                  }
                }
              }
            }

            // Objective Insights (Rule-Based Explanations)
            item {
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp)),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
              ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Text("OBJECTIVE INSIGHTS (RULE-BASED)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF79747E), letterSpacing = 0.5.sp)
                  metrics.objectiveInsights.forEach { insight ->
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      Text("•", color = WorkPrimary, fontWeight = FontWeight.Bold)
                      Text(insight, fontSize = 12.sp, color = Color(0xFF1D1B20), lineHeight = 16.sp)
                    }
                  }
                }
              }
            }
          }

          // Section 7 Charts: Daily Hours and Daily Progress
          item {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
              color = Color.White,
              border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("DAILY WORKING HOURS (RECENT)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF79747E), letterSpacing = 0.5.sp)
                SimpleBarChart(dataPoints = hoursPoints, maxValue = 10.0, barColor = WorkPrimary, unit = "h")

                HorizontalDivider(color = WorkOutlineVariant)

                Text("DAILY PROGRESS TREND (%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF79747E), letterSpacing = 0.5.sp)
                SimpleBarChart(dataPoints = progressPoints, maxValue = 100.0, barColor = Color(0xFF1D6C2F), unit = "%")
              }
            }
          }

          // Chronological EOD History Table
          item {
            Text("CHRONOLOGICAL EOD HISTORY (${empEods.size} RECORDS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF79747E), letterSpacing = 0.5.sp)
          }

          items(empEods) { eod ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
              color = Color.White,
              border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(eod.date, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                    Text("(${eod.submissionTime})", fontSize = 11.sp, color = Color(0xFF79747E))
                  }
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusBadge(status = eod.workStatus)
                    if (!eod.isOnTime) {
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(4.dp))
                          .background(Color(0xFFFFF3E0))
                          .padding(horizontal = 6.dp, vertical = 2.dp)
                      ) {
                        Text("Late", fontSize = 10.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                      }
                    }
                  }
                }

                Text(
                  text = "Project: ${eod.project}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = WorkPrimary
                )

                Text(
                  text = eod.todayWork,
                  fontSize = 12.sp,
                  color = Color(0xFF1D1B20)
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Hours: ${eod.hoursWorked}h", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Medium)
                  Text("Progress: ${eod.progressPercentage}%", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Medium)
                }

                if (eod.blockers.isNotBlank()) {
                  Text("Blockers: ${eod.blockers}", fontSize = 11.sp, color = Color(0xFFB3261E), fontWeight = FontWeight.Medium)
                }

                if (eod.tomorrowPlan.isNotBlank()) {
                  Text("Tomorrow: ${eod.tomorrowPlan}", fontSize = 11.sp, color = Color(0xFF1D6C2F))
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Section 11: Export Report Dialog (Multi-Sheet Excel / XLSX & CSV compatible)
 */
@Composable
fun ExportReportDialog(
  reportType: String,
  reportContent: String,
  format: String = "EXCEL",
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val isExcel = format == "EXCEL" || reportContent.trimStart().startsWith("<?xml")
  val mimeType = if (isExcel) "application/vnd.ms-excel" else "text/csv"
  val ext = if (isExcel) "xls" else "csv"
  val fileName = "WorkCore_${reportType}_${System.currentTimeMillis()}.$ext"

  var downloadResult by remember { mutableStateOf<DownloadResult?>(null) }
  var showPreview by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.85f)
        .clip(RoundedCornerShape(24.dp)),
      color = Color.White
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Export ${when (reportType) {
                "EOD" -> "Daily EOD Records"
                "PERFORMANCE" -> "Employee Performance Analytics"
                "TEAM" -> "Team Comprehensive Report"
                "COMPANY" -> "Company Summary Workbook"
                else -> "WorkCore Report"
              }}",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF21005D)
            )
            Text(
              text = if (isExcel) "Format: Excel Compatible ($ext)" else "Format: CSV Standard ($ext)",
              fontSize = 11.sp,
              color = Color(0xFF49454F)
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF49454F))
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = WorkOutlineVariant)

        // Summary Card
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF7F2FA),
          border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "FILE SUMMARY",
              fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF79747E)
            )
            Text("File Name: $fileName", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF21005D))
            Text("Size: ${(reportContent.toByteArray().size / 1024).coerceAtLeast(1)} KB", fontSize = 12.sp, color = Color(0xFF49454F))
            
            Text(
              text = if (showPreview) "Hide Content Preview ▲" else "View Content Preview ▼",
              fontSize = 11.sp,
              color = WorkPrimary,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.clickable { showPreview = !showPreview }
            )
          }
        }

        if (showPreview) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp)),
            color = Color(0xFFF7F2FA),
            border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
            ) {
              Text(
                text = reportContent,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = Color(0xFF1D1B20),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
              )
            }
          }
        } else {
          Spacer(modifier = Modifier.weight(1f))
        }

        // Download Status Banner
        downloadResult?.let { res ->
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (res.success) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (res.success) Color(0xFF81C784) else Color(0xFFEF9A9A)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = if (res.success) "✓ File Downloaded Directly to Downloads!" else "✕ Download Failed",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (res.success) Color(0xFF1B5E20) else Color(0xFFB71C1C)
              )
              Text(
                text = if (res.success) "Saved as: ${res.fileName}" else (res.error ?: "Unknown error"),
                fontSize = 12.sp,
                color = Color(0xFF49454F)
              )
              if (res.success) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Button(
                    onClick = { FileDownloadHelper.openDownloadedFile(context, res) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                  ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open File", fontSize = 12.sp)
                  }

                  OutlinedButton(
                    onClick = { FileDownloadHelper.shareDownloadedFile(context, res) },
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Direct Download & Options
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = {
              val res = FileDownloadHelper.downloadFileDirectly(
                context = context,
                fileName = fileName,
                content = reportContent,
                mimeType = mimeType
              )
              downloadResult = res
              if (res.success) {
                Toast.makeText(context, "Downloaded ${res.fileName} to Downloads folder", Toast.LENGTH_LONG).show()
              } else {
                Toast.makeText(context, "Download failed: ${res.error}", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.weight(1.3f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21005D))
          ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Download File Directly", fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("WorkCore Report", reportContent)
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.weight(0.7f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Copy")
          }
        }
      }
    }
  }
}

/**
 * Section 6: Create or Edit Team Dialog
 */
@Composable
fun CreateOrEditTeamDialog(
  teamToEdit: TeamEntity?,
  employees: List<EmployeeEntity>,
  onDismiss: () -> Unit,
  onSaveTeam: (name: String, department: String, manager: EmployeeEntity, projects: String, description: String) -> Unit,
  onUpdateTeam: (TeamEntity) -> Unit
) {
  var name by remember { mutableStateOf(teamToEdit?.name ?: "") }
  var department by remember { mutableStateOf(teamToEdit?.department ?: "Engineering") }
  var projects by remember { mutableStateOf(teamToEdit?.projects ?: "") }
  var description by remember { mutableStateOf(teamToEdit?.description ?: "") }

  // Default manager to Sita Verma or first manager
  var selectedManager by remember {
    mutableStateOf(
      employees.find { it.employeeId == teamToEdit?.managerId }
        ?: employees.first()
    )
  }

  var managerMenuOpen by remember { mutableStateOf(false) }
  var departmentMenuOpen by remember { mutableStateOf(false) }

  val departmentOptions = listOf("Machine Learning", "Content & Writing", "Database", "Operations", "Engineering", "QA")

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(24.dp)),
      color = Color.White
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (teamToEdit == null) "Create New Team" else "Edit Team (${teamToEdit.teamId})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF21005D)
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF49454F))
          }
        }

        HorizontalDivider(color = WorkOutlineVariant)

        // Team Name
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("Team Name *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20))
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("e.g. Mobile Engineering Team") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
          )
        }

        // Department
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("Department *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20))
          Box {
            OutlinedButton(
              onClick = { departmentMenuOpen = true },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(department, modifier = Modifier.weight(1f), textAlign = TextAlign.Start, color = Color(0xFF1D1B20))
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(
              expanded = departmentMenuOpen,
              onDismissRequest = { departmentMenuOpen = false }
            ) {
              departmentOptions.forEach { dept ->
                DropdownMenuItem(
                  text = { Text(dept) },
                  onClick = {
                    department = dept
                    departmentMenuOpen = false
                  }
                )
              }
            }
          }
        }

        // Assigned Manager
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("Assign Manager *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20))
          Box {
            OutlinedButton(
              onClick = { managerMenuOpen = true },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            ) {
              Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                UserAvatar(name = selectedManager.name, size = 24.dp)
                Text("${selectedManager.name} (${selectedManager.designation})", color = Color(0xFF1D1B20), fontSize = 12.sp)
              }
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(
              expanded = managerMenuOpen,
              onDismissRequest = { managerMenuOpen = false }
            ) {
              employees.forEach { emp ->
                DropdownMenuItem(
                  text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                      UserAvatar(name = emp.name, size = 20.dp)
                      Text("${emp.name} (${emp.designation})", fontSize = 12.sp)
                    }
                  },
                  onClick = {
                    selectedManager = emp
                    managerMenuOpen = false
                  }
                )
              }
            }
          }
        }

        // Projects / Work Areas
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("Assigned Projects / Work Areas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20))
          OutlinedTextField(
            value = projects,
            onValueChange = { projects = it },
            placeholder = { Text("Comma-separated e.g. Customer Portal, Android Client") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
        }

        // Description
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("Description", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20))
          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Purpose and responsibilities of this team") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            minLines = 2
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Save Button
        Button(
          onClick = {
            if (name.isNotBlank()) {
              if (teamToEdit == null) {
                onSaveTeam(name, department, selectedManager, projects, description)
              } else {
                onUpdateTeam(
                  teamToEdit.copy(
                    name = name,
                    department = department,
                    managerId = selectedManager.employeeId,
                    managerName = selectedManager.name,
                    projects = projects,
                    description = description
                  )
                )
              }
            }
          },
          enabled = name.isNotBlank(),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = WorkPrimary)
        ) {
          Text(if (teamToEdit == null) "Create Team" else "Save Changes")
        }
      }
    }
  }
}

/**
 * Section 6: Move / Transfer Employee Between Teams
 */
@Composable
fun MoveEmployeeDialog(
  employeePreselected: EmployeeEntity?,
  employees: List<EmployeeEntity>,
  teams: List<TeamEntity>,
  onDismiss: () -> Unit,
  onConfirmMove: (employeeId: String, newTeamName: String) -> Unit
) {
  var selectedEmp by remember { mutableStateOf(employeePreselected ?: employees.first()) }
  var selectedTeam by remember { mutableStateOf(teams.first().name) }
  var empMenuOpen by remember { mutableStateOf(false) }
  var teamMenuOpen by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(24.dp)),
      color = Color.White
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Transfer Team Member",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF21005D)
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF49454F))
          }
        }

        HorizontalDivider(color = WorkOutlineVariant)

        // Select Employee
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("Select Member", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20))
          Box {
            OutlinedButton(
              onClick = { empMenuOpen = true },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            ) {
              Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                UserAvatar(name = selectedEmp.name, size = 24.dp)
                Text("${selectedEmp.name} (Current: ${selectedEmp.team})", color = Color(0xFF1D1B20), fontSize = 12.sp)
              }
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(
              expanded = empMenuOpen,
              onDismissRequest = { empMenuOpen = false }
            ) {
              employees.forEach { emp ->
                DropdownMenuItem(
                  text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                      UserAvatar(name = emp.name, size = 20.dp)
                      Text("${emp.name} • ${emp.team}", fontSize = 12.sp)
                    }
                  },
                  onClick = {
                    selectedEmp = emp
                    empMenuOpen = false
                  }
                )
              }
            }
          }
        }

        // Select New Team
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("New Assigned Team", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20))
          Box {
            OutlinedButton(
              onClick = { teamMenuOpen = true },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(selectedTeam, modifier = Modifier.weight(1f), textAlign = TextAlign.Start, color = Color(0xFF1D1B20))
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(
              expanded = teamMenuOpen,
              onDismissRequest = { teamMenuOpen = false }
            ) {
              teams.forEach { team ->
                DropdownMenuItem(
                  text = { Text("${team.name} (${team.department})") },
                  onClick = {
                    selectedTeam = team.name
                    teamMenuOpen = false
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
          onClick = {
            onConfirmMove(selectedEmp.employeeId, selectedTeam)
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = WorkPrimary)
        ) {
          Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Transfer to $selectedTeam")
        }
      }
    }
  }
}

/**
 * Section 10 & 26: Missing EOD Alerts & Notification Center Dialog
 */
@Composable
fun NotificationCenterDialog(
  notifications: List<NotificationEntity>,
  onDismiss: () -> Unit,
  onMarkAllAsRead: () -> Unit,
  onSelectNotification: (NotificationEntity) -> Unit
) {
  var filter by remember { mutableStateOf("All") }

  val filteredNotifs = notifications.filter { notif ->
    when (filter) {
      "Pending" -> notif.type == "PENDING" || notif.type == "MULTIPLE_PENDING"
      "Late" -> notif.type == "LATE"
      "Complete" -> notif.type == "COMPLETE"
      else -> true
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.85f)
        .clip(RoundedCornerShape(24.dp)),
      color = Color.White
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = WorkPrimary)
            Column {
              Text(
                text = "Notifications & Alerts",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
              )
              Text(
                text = "${notifications.count { !it.isRead }} unread alerts",
                fontSize = 11.sp,
                color = Color(0xFF49454F)
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(
              onClick = onMarkAllAsRead,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Mark Read", fontSize = 11.sp)
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF49454F))
            }
          }
        }

        // Filter chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("All", "Pending", "Late", "Complete").forEach { f ->
            FilterChip(
              selected = filter == f,
              onClick = { filter = f },
              label = { Text(f, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = WorkPrimaryContainer,
                selectedLabelColor = Color(0xFF21005D)
              )
            )
          }
        }

        HorizontalDivider(color = WorkOutlineVariant)

        // Notification Items
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(top = 10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredNotifs, key = { it.id }) { notif ->
            val (bg, textColor) = when (notif.type) {
              "PENDING", "MULTIPLE_PENDING" -> StatusErrorBg to StatusErrorText
              "LATE" -> StatusWarningBg to StatusWarningText
              "COMPLETE" -> StatusSuccessBg to StatusSuccessText
              else -> StatusInfoBg to StatusInfoText
            }

            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSelectNotification(notif) },
              color = bg,
              border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (notif.isRead) Color.Transparent else textColor)
                    .padding(top = 4.dp)
                )

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = notif.title,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = textColor
                    )
                    Text(
                      text = notif.timeAgo,
                      fontSize = 10.sp,
                      color = Color(0xFF79747E)
                    )
                  }
                  Text(
                    text = notif.message,
                    fontSize = 12.sp,
                    color = Color(0xFF1D1B20)
                  )
                  if (notif.targetTeam.isNotBlank()) {
                    Text(
                      text = "Team: ${notif.targetTeam}",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Medium,
                      color = Color(0xFF49454F)
                    )
                  }
                }
              }
            }
          }

          if (filteredNotifs.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(40.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "No alerts in this category",
                  fontSize = 13.sp,
                  color = Color(0xFF79747E)
                )
              }
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// 1. MANAGER-LED EMPLOYEE ACCOUNT CREATION DIALOG
// -------------------------------------------------------------
@Composable
fun AddEmployeeByManagerDialog(
  manager: EmployeeEntity,
  onDismiss: () -> Unit,
  onCreateAccount: (name: String, employeeId: String, password: String, confirmPassword: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var employeeId by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var localError by remember { mutableStateOf<String?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(24.dp)),
      color = Color.White,
      shadowElevation = 8.dp
    ) {
      Column(
        modifier = Modifier
          .padding(24.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = WorkPrimaryContainer,
              modifier = Modifier.size(40.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = WorkPrimary)
              }
            }
            Column {
              Text(
                text = "Add Employee Account",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
              )
              Text(
                text = "Team: ${manager.team} (Managed by ${manager.name})",
                fontSize = 11.sp,
                color = Color(0xFF49454F)
              )
            }
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF49454F))
          }
        }

        HorizontalDivider(color = WorkOutlineVariant)

        // Error message if any
        if (localError != null) {
          Surface(
            color = Color(0xFFF9DEDC),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = localError ?: "",
              fontSize = 12.sp,
              color = Color(0xFFB3261E),
              modifier = Modifier.padding(10.dp)
            )
          }
        }

        // Employee Name
        OutlinedTextField(
          value = name,
          onValueChange = {
            name = it
            localError = null
          },
          label = { Text("Employee Name *") },
          placeholder = { Text("e.g. Anand Sharma") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("manager_add_emp_name"),
          shape = RoundedCornerShape(12.dp)
        )

        // Employee ID
        OutlinedTextField(
          value = employeeId,
          onValueChange = {
            employeeId = it
            localError = null
          },
          label = { Text("Employee ID *") },
          placeholder = { Text("e.g. EMP010") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("manager_add_emp_id"),
          shape = RoundedCornerShape(12.dp)
        )

        // Team (Automatically Manager's Own Team - Read Only)
        OutlinedTextField(
          value = manager.team,
          onValueChange = {},
          readOnly = true,
          enabled = false,
          label = { Text("Team (Locked to Manager's Team)") },
          leadingIcon = {
            Icon(Icons.Default.Group, contentDescription = null, tint = WorkPrimary)
          },
          trailingIcon = {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = WorkPrimaryContainer,
              modifier = Modifier.padding(end = 8.dp)
            ) {
              Text(
                text = "Auto-Assigned",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = WorkPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("manager_add_emp_team_locked"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = WorkOutlineVariant,
            disabledTextColor = Color(0xFF1D1B20),
            disabledLabelColor = Color(0xFF49454F)
          )
        )

        // Password
        OutlinedTextField(
          value = password,
          onValueChange = {
            password = it
            localError = null
          },
          label = { Text("Password *") },
          leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = WorkPrimary)
          },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("manager_add_emp_password"),
          shape = RoundedCornerShape(12.dp)
        )

        // Confirm Password
        OutlinedTextField(
          value = confirmPassword,
          onValueChange = {
            confirmPassword = it
            localError = null
          },
          label = { Text("Confirm Password *") },
          leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = WorkPrimary)
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("manager_add_emp_confirm_password"),
          shape = RoundedCornerShape(12.dp)
        )

        Text(
          text = "The employee will log in using Select Team: '${manager.team}' + their Employee ID + Password.",
          fontSize = 11.sp,
          color = Color(0xFF79747E)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Cancel")
          }

          Button(
            onClick = {
              if (name.isBlank() || employeeId.isBlank() || password.isBlank()) {
                localError = "All fields are required."
              } else if (password != confirmPassword) {
                localError = "Passwords do not match."
              } else if (password.length < 4) {
                localError = "Password must be at least 4 characters."
              } else {
                onCreateAccount(name, employeeId, password, confirmPassword)
              }
            },
            modifier = Modifier
              .weight(1.5f)
              .testTag("btn_confirm_create_employee"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WorkPrimary)
          ) {
            Text("Create Account", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

