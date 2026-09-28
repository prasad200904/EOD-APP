package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyEodEntity
import com.example.data.EmployeeEntity
import com.example.data.SeedData
import com.example.data.SyncStatus
import com.example.data.TeamEntity
import com.example.util.FileDownloadHelper
import com.example.ui.components.UserAvatar
import com.example.ui.theme.StatusErrorBg
import com.example.ui.theme.StatusErrorText
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.WorkBackground
import com.example.ui.theme.WorkOutline
import com.example.ui.theme.WorkOutlineVariant
import com.example.ui.theme.WorkPrimary
import com.example.ui.theme.WorkPrimaryContainer
import com.example.ui.theme.WorkSurface
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun EmployeeDashboardScreen(
  employeeId: String,
  employee: EmployeeEntity?,
  eods: List<DailyEodEntity>,
  teams: List<TeamEntity>,
  onSubmitEod: (date: String, project: String, taskDescription: String, hoursWorked: Double, progressPercentage: Int, status: String) -> Unit,
  onOpenEodPopup: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Strictly scoped to the logged-in employee's own data
  val myEods = remember(eods, employeeId) {
    eods.filter { it.employeeId == employeeId }.sortedByDescending { it.date }
  }

  // Today's EOD check
  val myTodayEod = remember(myEods) {
    myEods.firstOrNull { it.date == SeedData.TODAY }
  }

  // Active projects for the employee's assigned team
  val assignedTeam = remember(teams, employee) {
    teams.firstOrNull {
      it.name.equals(employee?.team, ignoreCase = true) ||
      it.teamId.equals(employee?.team, ignoreCase = true)
    }
  }

  val activeProjects = remember(assignedTeam, employee) {
    val teamProjs = assignedTeam?.projects
      ?.split(",")
      ?.map { it.trim() }
      ?.filter { it.isNotEmpty() }
      ?: emptyList()
    val combined = (teamProjs + listOfNotNull(employee?.defaultProject).filter { it.isNotEmpty() }).distinct()
    if (combined.isEmpty()) listOf("Core Engineering", "Product Features", "Technical Documentation") else combined
  }

  // Form States (defaults to today)
  var formDate by remember { mutableStateOf(SeedData.TODAY) }
  var formProject by remember { mutableStateOf(activeProjects.firstOrNull() ?: "Core Engineering") }
  var formTaskDescription by remember { mutableStateOf("") }
  var formHoursWorked by remember { mutableDoubleStateOf(8.0) }
  var formProgressPercentage by remember { mutableIntStateOf(100) }
  var formStatus by remember { mutableStateOf("Completed") } // "Completed", "In Progress", "Blocked"

  var projectDropdownOpen by remember { mutableStateOf(false) }
  var validationError by remember { mutableStateOf<String?>(null) }
  var formSubmittedSuccess by remember { mutableStateOf(false) }

  // Sync with today's EOD if already submitted and form is on today
  LaunchedEffect(myTodayEod, formDate) {
    if (formDate == SeedData.TODAY && myTodayEod != null && formTaskDescription.isBlank()) {
      formProject = myTodayEod.project
      formTaskDescription = myTodayEod.todayWork
      formHoursWorked = myTodayEod.hoursWorked
      formProgressPercentage = myTodayEod.progressPercentage
      formStatus = myTodayEod.workStatus
    }
  }

  // Filter States for EOD History Table
  val earliestDate = remember(myEods) {
    myEods.minOfOrNull { it.date } ?: "2026-08-01"
  }
  var filterFromDate by remember { mutableStateOf(earliestDate) }
  var filterToDate by remember { mutableStateOf(SeedData.TODAY) }

  // Filtered EOD records for History Table and CSV Download
  val filteredHistoryEods = remember(myEods, filterFromDate, filterToDate) {
    myEods.filter { eod ->
      val inFrom = filterFromDate.isBlank() || eod.date >= filterFromDate
      val inTo = filterToDate.isBlank() || eod.date <= filterToDate
      inFrom && inTo
    }.sortedByDescending { it.date }
  }

  // Project history aggregation
  val projectStats = remember(myEods) {
    myEods.groupBy { it.project.trim().ifEmpty { "General" } }
      .map { (proj, records) ->
        val totalHours = records.sumOf { it.hoursWorked }
        val entriesCount = records.size
        val latestDate = records.maxOfOrNull { it.date } ?: "N/A"
        Triple(proj, totalHours, Pair(entriesCount, latestDate))
      }
      .sortedByDescending { it.second }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(WorkBackground)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // ----------------------------------------------------
    // Welcome / Employee Persona Banner
    // ----------------------------------------------------
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp)),
        color = Color(0xFF21005D)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              UserAvatar(name = employee?.name ?: "Employee", size = 42, backgroundColor = WorkPrimary)
              Column {
                Text(
                  text = employee?.name ?: "Employee Workspace",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = "${employee?.designation ?: "Team Member"} • ${employee?.team ?: "Engineering"}",
                  fontSize = 12.sp,
                  color = Color(0xFFEADDFF)
                )
              }
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (myTodayEod != null) Color(0xFF1B5E20) else Color(0xFFB3261E)
              ) {
                Text(
                  text = if (myTodayEod != null) "TODAY SUBMITTED" else "EOD PENDING",
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
              if (onOpenEodPopup != null) {
                Button(
                  onClick = onOpenEodPopup,
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF21005D)
                  ),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.testTag("employee_header_eod_btn")
                ) {
                  Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("EOD", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
              }
            }
          }

          Text(
            text = "// EMPLOYEE DASHBOARD • ID: $employeeId",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFFCAC4D0),
            letterSpacing = 0.5.sp
          )
        }
      }
    }

    // ----------------------------------------------------
    // 2. Performance Summary (4 Stat Tiles)
    // ----------------------------------------------------
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "PERFORMANCE OVERVIEW",
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF79747E),
          letterSpacing = 0.8.sp
        )

        val eodPendingCount = if (myTodayEod != null) 0 else 1
        val completedCount = myEods.count { it.workStatus.equals("Completed", ignoreCase = true) }
        val avgHours = if (myEods.isNotEmpty()) {
          String.format(Locale.US, "%.1fh", myEods.map { it.hoursWorked }.average())
        } else "0.0h"
        val activeProjectsCount = myEods.map { it.project.trim() }.filter { it.isNotEmpty() }.distinct().size

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatTile(
            title = "EOD Pending",
            value = "$eodPendingCount",
            subtitle = if (eodPendingCount == 0) "Today complete" else "Action required",
            backgroundColor = if (eodPendingCount == 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
            valueColor = if (eodPendingCount == 0) Color(0xFF1B5E20) else Color(0xFFB71C1C),
            icon = if (eodPendingCount == 0) Icons.Default.CheckCircle else Icons.Default.PendingActions,
            modifier = Modifier.weight(1f).testTag("stat_tile_eod_pending")
          )
          StatTile(
            title = "Tasks Completed",
            value = "$completedCount",
            subtitle = "${myEods.size} total entries",
            backgroundColor = Color(0xFFF3E7FF),
            valueColor = Color(0xFF21005D),
            icon = Icons.Default.TaskAlt,
            modifier = Modifier.weight(1f).testTag("stat_tile_tasks_completed")
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatTile(
            title = "Avg Hours/Day",
            value = avgHours,
            subtitle = "Reported work pace",
            backgroundColor = Color(0xFFEDE7F6),
            valueColor = WorkPrimary,
            icon = Icons.Default.HourglassEmpty,
            modifier = Modifier.weight(1f).testTag("stat_tile_avg_hours")
          )
          StatTile(
            title = "Active Projects",
            value = "$activeProjectsCount",
            subtitle = "${assignedTeam?.name ?: "Team"} scope",
            backgroundColor = Color(0xFFF3E7FF),
            valueColor = Color(0xFF21005D),
            icon = Icons.Default.Lightbulb,
            modifier = Modifier.weight(1f).testTag("stat_tile_active_projects")
          )
        }
      }
    }

    // ----------------------------------------------------
    // 1. EOD Submission Form
    // ----------------------------------------------------
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp)),
        color = Color.White,
        border = BorderStroke(1.dp, WorkOutlineVariant)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "EOD SUBMISSION FORM",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF21005D),
                letterSpacing = 0.5.sp
              )
              Text(
                text = "Log your daily achievements and hours for ${employee?.team ?: "your team"}",
                fontSize = 11.sp,
                color = Color(0xFF49454F)
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (myTodayEod != null && formDate == SeedData.TODAY) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFFE8DEF8)
                ) {
                  Text(
                    text = "EDITING TODAY",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21005D),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
              if (onOpenEodPopup != null) {
                OutlinedButton(
                  onClick = onOpenEodPopup,
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Popup Form", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
              }
            }
          }

          HorizontalDivider(color = WorkOutlineVariant)

          // 1. Date Field (Defaults to today, editable)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = formDate,
              onValueChange = {
                formDate = it
                validationError = null
              },
              label = { Text("Date (YYYY-MM-DD) *") },
              singleLine = true,
              leadingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = WorkPrimary, modifier = Modifier.size(18.dp))
              },
              trailingIcon = {
                IconButton(onClick = {
                  val c = Calendar.getInstance()
                  val dialog = DatePickerDialog(
                    context,
                    { _, y, m, d ->
                      formDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
                  )
                  dialog.show()
                }) {
                  Icon(Icons.Default.DateRange, contentDescription = "Pick Date", tint = WorkPrimary)
                }
              },
              modifier = Modifier
                .weight(1f)
                .testTag("employee_eod_date_field"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WorkPrimary,
                unfocusedBorderColor = WorkOutline
              )
            )

            // Quick Today Button
            OutlinedButton(
              onClick = { formDate = SeedData.TODAY },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.height(52.dp)
            ) {
              Text("Today", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          // 2. Project Field (Manual typing with suggestion dropdown)
          Column {
            Text(
              text = "ASSIGNED PROJECT (MANUAL TYPING) *",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF79747E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
              OutlinedTextField(
                value = formProject,
                onValueChange = { formProject = it },
                label = { Text("Assigned Project (Manual Typing) *") },
                placeholder = { Text("Type assigned project name...") },
                trailingIcon = {
                  IconButton(onClick = { projectDropdownOpen = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Project Suggestion")
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("employee_eod_project_selector"),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = WorkPrimary,
                  unfocusedBorderColor = WorkOutline
                )
              )

              DropdownMenu(
                expanded = projectDropdownOpen,
                onDismissRequest = { projectDropdownOpen = false },
                modifier = Modifier.fillMaxWidth(0.88f)
              ) {
                activeProjects.forEach { proj ->
                  DropdownMenuItem(
                    text = {
                      Column {
                        Text(proj, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Active Project in ${assignedTeam?.name ?: "Team"}", fontSize = 10.sp, color = Color(0xFF79747E))
                      }
                    },
                    onClick = {
                      formProject = proj
                      projectDropdownOpen = false
                    }
                  )
                }
              }
            }
          }

          // 3. Task Description Field (Multiline text, required)
          Column {
            Text(
              text = "TASK DESCRIPTION *",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF79747E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = formTaskDescription,
              onValueChange = {
                formTaskDescription = it
                validationError = null
              },
              placeholder = { Text("Detail the specific modules, PRs, features, or tickets worked on...") },
              minLines = 3,
              maxLines = 6,
              isError = validationError != null && formTaskDescription.isBlank(),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("employee_eod_task_description"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WorkPrimary,
                unfocusedBorderColor = WorkOutline
              )
            )
          }

          // 4. Hours Worked (Numeric 0–24, step 0.5, required)
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "HOURS WORKED *",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF79747E)
              )
              Text(
                text = "${formHoursWorked}h",
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WorkPrimary
              )
            }

            Slider(
              value = formHoursWorked.toFloat(),
              onValueChange = { rawVal ->
                // Step of 0.5
                val stepped = (rawVal * 2).roundToInt() / 2.0
                formHoursWorked = stepped.coerceIn(0.0, 24.0)
                validationError = null
              },
              valueRange = 0f..24f,
              steps = 47, // (24 / 0.5) - 1 = 47
              colors = SliderDefaults.colors(
                thumbColor = WorkPrimary,
                activeTrackColor = WorkPrimary,
                inactiveTrackColor = Color(0xFFE7E0EC)
              ),
              modifier = Modifier.fillMaxWidth().testTag("employee_eod_hours_slider")
            )

            // Preset Quick Hour Chips
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              listOf(4.0, 6.0, 7.5, 8.0, 9.0).forEach { preset ->
                FilterChip(
                  selected = formHoursWorked == preset,
                  onClick = { formHoursWorked = preset },
                  label = { Text("${preset}h", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WorkPrimaryContainer,
                    selectedLabelColor = Color(0xFF21005D)
                  )
                )
              }
            }
          }

          // 5. Progress % (Numeric 0–100)
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "PROGRESS %",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF79747E)
              )
              Text(
                text = "$formProgressPercentage%",
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D6C2F)
              )
            }

            Slider(
              value = formProgressPercentage.toFloat(),
              onValueChange = { formProgressPercentage = it.roundToInt().coerceIn(0, 100) },
              valueRange = 0f..100f,
              colors = SliderDefaults.colors(
                thumbColor = Color(0xFF1D6C2F),
                activeTrackColor = Color(0xFF1D6C2F),
                inactiveTrackColor = Color(0xFFE7E0EC)
              ),
              modifier = Modifier.fillMaxWidth().testTag("employee_eod_progress_slider")
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              listOf(25, 50, 75, 100).forEach { preset ->
                FilterChip(
                  selected = formProgressPercentage == preset,
                  onClick = { formProgressPercentage = preset },
                  label = { Text("$preset%", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFD1F2D9),
                    selectedLabelColor = Color(0xFF1D6C2F)
                  )
                )
              }
            }
          }

          // 6. Status (Single-select: Completed / In Progress / Blocked)
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "TASK STATUS *",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF79747E)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              StatusSelectorCard(
                label = "Completed",
                isSelected = formStatus == "Completed",
                activeBg = Color(0xFFD1F2D9),
                activeText = Color(0xFF1D6C2F),
                activeBorder = Color(0xFF1D6C2F),
                icon = Icons.Default.CheckCircle,
                onClick = {
                  formStatus = "Completed"
                  if (formProgressPercentage < 100) formProgressPercentage = 100
                },
                modifier = Modifier.weight(1f).testTag("employee_status_completed")
              )

              StatusSelectorCard(
                label = "In Progress",
                isSelected = formStatus == "In Progress",
                activeBg = Color(0xFFEADDFF),
                activeText = Color(0xFF21005D),
                activeBorder = WorkPrimary,
                icon = Icons.Default.Sync,
                onClick = { formStatus = "In Progress" },
                modifier = Modifier.weight(1f).testTag("employee_status_in_progress")
              )

              StatusSelectorCard(
                label = "Blocked",
                isSelected = formStatus == "Blocked",
                activeBg = Color(0xFFFFDAD6),
                activeText = Color(0xFFB3261E),
                activeBorder = Color(0xFFB3261E),
                icon = Icons.Default.Block,
                onClick = { formStatus = "Blocked" },
                modifier = Modifier.weight(1f).testTag("employee_status_blocked")
              )
            }
          }

          // Validation Error Notice
          if (validationError != null) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFFFDAD6),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB3261E), modifier = Modifier.size(16.dp))
                Text(
                  text = validationError ?: "",
                  fontSize = 12.sp,
                  color = Color(0xFF410002),
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          // Submit Button
          Button(
            onClick = {
              if (formTaskDescription.isBlank()) {
                validationError = "Task description is required before submitting."
                return@Button
              }
              if (formHoursWorked <= 0.0) {
                validationError = "Hours worked must be greater than 0."
                return@Button
              }

              validationError = null
              onSubmitEod(
                formDate.trim(),
                formProject.trim(),
                formTaskDescription.trim(),
                formHoursWorked,
                formProgressPercentage,
                formStatus
              )
              formSubmittedSuccess = true
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WorkPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("employee_submit_eod_btn")
          ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (myTodayEod != null && formDate == SeedData.TODAY) "Update Today's EOD" else "Submit EOD Record",
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }
    }

    // ----------------------------------------------------
    // 3. Project History Section
    // ----------------------------------------------------
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp)),
        color = Color.White,
        border = BorderStroke(1.dp, WorkOutlineVariant)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "PROJECT HISTORY",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF21005D),
                letterSpacing = 0.5.sp
              )
              Text(
                text = "Grouped by project • Sorted by total hours",
                fontSize = 11.sp,
                color = Color(0xFF49454F)
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFF3E7FF)
            ) {
              Text(
                text = "${projectStats.size} Projects",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = WorkPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          HorizontalDivider(color = WorkOutlineVariant)

          if (projectStats.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No project EOD records logged yet.",
                fontSize = 12.sp,
                color = Color(0xFF79747E)
              )
            }
          } else {
            val maxHours = projectStats.maxOfOrNull { it.second } ?: 1.0

            projectStats.forEach { (projName, totalHours, meta) ->
              val (entryCount, lastActiveDate) = meta
              val fraction = (totalHours / maxHours).coerceIn(0.0, 1.0).toFloat()

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFDF7FF),
                border = BorderStroke(1.dp, Color(0xFFEADDFF)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = projName,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = Color(0xFF21005D),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.weight(1f)
                    )

                    Text(
                      text = String.format(Locale.US, "%.1fh", totalHours),
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = WorkPrimary
                    )
                  }

                  LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = WorkPrimary,
                    trackColor = Color(0xFFEADDFF)
                  )

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "$entryCount logged ${if (entryCount == 1) "entry" else "entries"}",
                      fontSize = 11.sp,
                      color = Color(0xFF49454F)
                    )
                    Text(
                      text = "Last active: $lastActiveDate",
                      fontFamily = FontFamily.Monospace,
                      fontSize = 10.sp,
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

    // ----------------------------------------------------
    // 5. Date Range Filter + CSV Download Controls
    // ----------------------------------------------------
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp)),
        color = Color.White,
        border = BorderStroke(1.dp, WorkOutlineVariant)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.FilterList, contentDescription = null, tint = WorkPrimary, modifier = Modifier.size(18.dp))
              Text(
                text = "DATE RANGE FILTER & EXPORT",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF21005D),
                letterSpacing = 0.5.sp
              )
            }

            Text(
              text = "${filteredHistoryEods.size} Records",
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = Color(0xFF49454F)
            )
          }

          // From and To Date Pickers
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = filterFromDate,
              onValueChange = { filterFromDate = it },
              label = { Text("From (YYYY-MM-DD)", fontSize = 11.sp) },
              singleLine = true,
              trailingIcon = {
                IconButton(onClick = {
                  val c = Calendar.getInstance()
                  DatePickerDialog(
                    context,
                    { _, y, m, d ->
                      filterFromDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
                  ).show()
                }) {
                  Icon(Icons.Default.DateRange, contentDescription = "From Date", modifier = Modifier.size(18.dp))
                }
              },
              modifier = Modifier.weight(1f).testTag("filter_from_date"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WorkPrimary,
                unfocusedBorderColor = WorkOutline
              )
            )

            OutlinedTextField(
              value = filterToDate,
              onValueChange = { filterToDate = it },
              label = { Text("To (YYYY-MM-DD)", fontSize = 11.sp) },
              singleLine = true,
              trailingIcon = {
                IconButton(onClick = {
                  val c = Calendar.getInstance()
                  DatePickerDialog(
                    context,
                    { _, y, m, d ->
                      filterToDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
                  ).show()
                }) {
                  Icon(Icons.Default.DateRange, contentDescription = "To Date", modifier = Modifier.size(18.dp))
                }
              },
              modifier = Modifier.weight(1f).testTag("filter_to_date"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WorkPrimary,
                unfocusedBorderColor = WorkOutline
              )
            )
          }

          // Preset Quick Filter Buttons & Reset
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                filterFromDate = earliestDate
                filterToDate = SeedData.TODAY
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            ) {
              Text("All Time", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
              onClick = {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -7)
                filterFromDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                filterToDate = SeedData.TODAY
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            ) {
              Text("Last 7D", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
              onClick = {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -30)
                filterFromDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                filterToDate = SeedData.TODAY
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            ) {
              Text("Last 30D", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
          }

          HorizontalDivider(color = WorkOutlineVariant)

          // "Download CSV" button exporting eod-report_<start>_to_<end>.csv
          Button(
            onClick = {
              val fileName = "eod-report_${filterFromDate}_to_${filterToDate}.csv"
              val csvContent = buildString {
                append("Date,Project,Task,Hours,Progress %,Status\n")
                for (r in filteredHistoryEods) {
                  val safeProj = "\"" + r.project.replace("\"", "\"\"") + "\""
                  val safeTask = "\"" + r.todayWork.replace("\"", "\"\"") + "\""
                  append("${r.date},$safeProj,$safeTask,${r.hoursWorked},${r.progressPercentage}%,${r.workStatus}\n")
                }
              }

              // Save directly to device Downloads folder
              val res = FileDownloadHelper.downloadFileDirectly(
                context = context,
                fileName = fileName,
                content = csvContent,
                mimeType = "text/csv"
              )
              if (res.success) {
                Toast.makeText(context, "Downloaded directly: ${res.fileName}", Toast.LENGTH_LONG).show()
              } else {
                Toast.makeText(context, "Download issue: ${res.error}", Toast.LENGTH_SHORT).show()
              }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21005D)),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("download_csv_button")
          ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Download CSV (eod-report_${filterFromDate}_to_${filterToDate}.csv)",
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }

    // ----------------------------------------------------
    // 4. EOD History Table
    // ----------------------------------------------------
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "EOD HISTORY TABLE (${filteredHistoryEods.size})",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF79747E),
            letterSpacing = 0.8.sp
          )
          Text(
            text = "Sorted by date descending",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = Color(0xFF79747E)
          )
        }

        if (filteredHistoryEods.isEmpty()) {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.dp, WorkOutlineVariant)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No EOD records found in selected range ($filterFromDate to $filterToDate)",
                fontSize = 12.sp,
                color = Color(0xFF79747E)
              )
            }
          }
        } else {
          // Responsive EOD History Card List / Table
          filteredHistoryEods.forEach { item ->
            EodHistoryRecordCard(
              record = item,
              onEditClick = {
                formDate = item.date
                formProject = item.project
                formTaskDescription = item.todayWork
                formHoursWorked = item.hoursWorked
                formProgressPercentage = item.progressPercentage
                formStatus = item.workStatus
              }
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun StatTile(
  title: String,
  value: String,
  subtitle: String,
  backgroundColor: Color,
  valueColor: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp)),
    color = backgroundColor,
    border = BorderStroke(1.dp, WorkOutlineVariant)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF49454F)
        )
        Icon(icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(16.dp))
      }

      Text(
        text = value,
        fontFamily = FontFamily.Monospace,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = valueColor
      )

      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = Color(0xFF49454F),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun StatusSelectorCard(
  label: String,
  isSelected: Boolean,
  activeBg: Color,
  activeText: Color,
  activeBorder: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick),
    color = if (isSelected) activeBg else Color(0xFFF7F2FA),
    border = BorderStroke(
      width = if (isSelected) 2.dp else 1.dp,
      color = if (isSelected) activeBorder else WorkOutlineVariant
    )
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) activeText else Color(0xFF79747E),
        modifier = Modifier.size(18.dp)
      )
      Text(
        text = label,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) activeText else Color(0xFF49454F),
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun EodHistoryRecordCard(
  record: DailyEodEntity,
  onEditClick: () -> Unit
) {
  val (statusBg, statusText) = when (record.workStatus) {
    "Completed" -> StatusSuccessBg to StatusSuccessText
    "In Progress" -> WorkPrimaryContainer to Color(0xFF21005D)
    "Blocked" -> StatusErrorBg to StatusErrorText
    else -> Color(0xFFE8DEF8) to Color(0xFF1D192B)
  }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp)),
    color = Color.White,
    border = BorderStroke(1.dp, WorkOutlineVariant)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Row 1: Date & Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(13.dp), tint = WorkPrimary)
          Text(
            text = record.date,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B20)
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (record.syncStatus) {
              SyncStatus.SYNCED.name, "SYNCED" -> Color(0xFFE8F5E9)
              SyncStatus.SYNCING.name, "SYNCING" -> Color(0xFFE3F2FD)
              SyncStatus.PENDING.name, "PENDING" -> Color(0xFFFFF3E0)
              SyncStatus.FAILED.name, "FAILED" -> Color(0xFFFFEBEE)
              else -> Color(0xFFE8F5E9)
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Icon(
                imageVector = when (record.syncStatus) {
                  SyncStatus.SYNCED.name, "SYNCED" -> Icons.Default.CheckCircle
                  SyncStatus.SYNCING.name, "SYNCING" -> Icons.Default.Sync
                  SyncStatus.PENDING.name, "PENDING" -> Icons.Default.HourglassEmpty
                  SyncStatus.FAILED.name, "FAILED" -> Icons.Default.Block
                  else -> Icons.Default.CheckCircle
                },
                contentDescription = null,
                tint = when (record.syncStatus) {
                  SyncStatus.SYNCED.name, "SYNCED" -> Color(0xFF2E7D32)
                  SyncStatus.SYNCING.name, "SYNCING" -> Color(0xFF1976D2)
                  SyncStatus.PENDING.name, "PENDING" -> Color(0xFFE65100)
                  SyncStatus.FAILED.name, "FAILED" -> Color(0xFFC62828)
                  else -> Color(0xFF2E7D32)
                },
                modifier = Modifier.size(11.dp)
              )
              Text(
                text = when (record.syncStatus) {
                  SyncStatus.SYNCED.name, "SYNCED" -> "Synced"
                  SyncStatus.SYNCING.name, "SYNCING" -> "Syncing"
                  SyncStatus.PENDING.name, "PENDING" -> "Pending"
                  SyncStatus.FAILED.name, "FAILED" -> "Failed"
                  else -> "Synced"
                },
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = when (record.syncStatus) {
                  SyncStatus.SYNCED.name, "SYNCED" -> Color(0xFF2E7D32)
                  SyncStatus.SYNCING.name, "SYNCING" -> Color(0xFF1976D2)
                  SyncStatus.PENDING.name, "PENDING" -> Color(0xFFE65100)
                  SyncStatus.FAILED.name, "FAILED" -> Color(0xFFC62828)
                  else -> Color(0xFF2E7D32)
                }
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = statusBg
          ) {
            Text(
              text = record.workStatus,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = statusText,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }

      // Row 2: Project Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = record.project,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = Color(0xFF21005D)
        )

        Text(
          text = "${record.hoursWorked} hrs",
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = WorkPrimary
        )
      }

      // Row 3: Task Description
      Text(
        text = record.todayWork,
        fontSize = 12.sp,
        color = Color(0xFF313033),
        lineHeight = 17.sp
      )

      HorizontalDivider(color = Color(0xFFF3E7FF))

      // Row 4: Progress Bar & Edit Affordance
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          LinearProgressIndicator(
            progress = { record.progressPercentage / 100f },
            modifier = Modifier
              .width(80.dp)
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = when {
              record.progressPercentage >= 100 -> Color(0xFF1D6C2F)
              record.workStatus == "Blocked" -> Color(0xFFB3261E)
              else -> WorkPrimary
            },
            trackColor = Color(0xFFEADDFF)
          )
          Text(
            text = "${record.progressPercentage}% progress",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = Color(0xFF49454F)
          )
        }

        IconButton(
          onClick = onEditClick,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit in Form",
            tint = WorkPrimary,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
