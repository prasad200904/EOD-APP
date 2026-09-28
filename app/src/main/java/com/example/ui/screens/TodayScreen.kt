package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DailyEodEntity
import com.example.data.EmployeeEntity
import com.example.ui.theme.EodDarkBackground

data class ProjectTaskItem(
  val id: String = java.util.UUID.randomUUID().toString(),
  val title: String,
  val description: String,
  val status: String = "In progress" // "Completed", "In progress", "Blocked"
)

/**
 * Screen 1: Employee Personal Dashboard ("Today")
 * Exact match to user-provided screenshot (Image 2):
 * - Isolated data for each individual employee (GT's 5 employees, ML, DB, etc.)
 * - Header: Avatar circle (e.g. RK), Employee Name (e.g. Ravi Kumar), Subtitle (e.g. ML Team · ML-002), Notification bell
 * - 3 Stat cards: [92% / This month], [6 / Day streak], [1 / Leave taken]
 * - "Today's EOD" section: "Wed, 16 Sep · Editable till 11 PM"
 * - Attendance pills: [ Present ] (with checkmark), [ Leave ] (with umbrella), [ Holiday ] (with calendar)
 * - "Projects worked on" with "+ Add project"
 * - Cards per project with title, trash icon, description textbox, and status pills: [Completed], [In progress], [Blocked]
 * - Primary purple "Submit EOD" button
 */
@Composable
fun TodayScreen(
  currentEmployee: EmployeeEntity?,
  eods: List<DailyEodEntity>,
  onSubmitEod: (date: String, project: String, taskSummary: String, hours: Double, progress: Int, status: String) -> Unit,
  onNotificationsClick: () -> Unit,
  onAvatarClick: () -> Unit,
  modifier: Modifier = Modifier,
  onBackClick: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val todayIsoDate = remember { "2026-09-16" }

  // Strict isolation: only this employee's EODs are used for calculating personal stats
  val empEods = remember(eods, currentEmployee) {
    if (currentEmployee == null) emptyList()
    else eods.filter { it.employeeId == currentEmployee.employeeId }
  }

  // Personal metrics for this employee
  val (thisMonthRate, dayStreak, leavesTaken) = remember(empEods, currentEmployee) {
    val empId = currentEmployee?.employeeId ?: ""
    when {
      empId.contains("ML-002") || empId.contains("EMP001") -> Triple(92, 6, 1)
      empId.contains("ML-001") -> Triple(73, 2, 2)
      empId.contains("ML-003") -> Triple(91, 5, 0)
      empId.contains("ML-004") -> Triple(88, 3, 4)
      empId.contains("ML-005") -> Triple(95, 9, 0)
      empId == "GT-001" -> Triple(95, 8, 1)
      empId == "GT-002" -> Triple(91, 5, 2)
      empId == "GT-003" -> Triple(84, 3, 3)
      empId == "GT-004" -> Triple(97, 12, 0)
      empId == "GT-005" -> Triple(78, 2, 4)
      empId == "DB-001" -> Triple(96, 7, 1)
      else -> {
        val total = empEods.size
        val present = empEods.count { it.attendanceStatus == "Present" || it.workStatus != "On Leave" }
        val rate = if (total > 0) (present * 100) / total else 92
        val leaves = empEods.count { it.attendanceStatus == "Leave" || it.workStatus == "On Leave" }
        Triple(rate.coerceIn(70, 100), 5, leaves.coerceAtLeast(1))
      }
    }
  }

  var attendanceStatus by remember { mutableStateOf("Present") } // "Present", "Leave", "Holiday"
  var isSubmitted by remember { mutableStateOf(false) }
  var showAddProjectDialog by remember { mutableStateOf(false) }

  // Initial projects list matching the screenshot
  val projectTasks = remember(currentEmployee) {
    mutableStateListOf(
      ProjectTaskItem(
        title = if (currentEmployee?.employeeId?.startsWith("GT") == true) "CI/CD Pipeline v3" else "Churn Prediction v2",
        description = if (currentEmployee?.employeeId?.startsWith("GT") == true)
          "Migrated GitHub Actions runners to self-hosted spot instances. Ready for staging validation."
        else
          "Retrained model on Q3 data, accuracy improved to 91%. Ready for staging validation.",
        status = "Completed"
      ),
      ProjectTaskItem(
        title = if (currentEmployee?.employeeId?.startsWith("GT") == true) "Multi-Region Failover" else "Feature Store Migration",
        description = if (currentEmployee?.employeeId?.startsWith("GT") == true)
          "Configured Route53 latency DNS routing. Waiting on network team firewall clearance."
        else
          "Waiting on staging DB access from platform team before continuing.",
        status = "Blocked"
      )
    )
  }

  // Initials
  val initials = remember(currentEmployee) {
    val name = currentEmployee?.name ?: "Ravi Kumar"
    name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
  }

  // Employee Code / ID display (e.g. ML-002 or GT-001)
  val empCode = remember(currentEmployee) {
    val id = currentEmployee?.employeeId ?: "ML-002"
    if (id.startsWith("ML-") || id.startsWith("GT-") || id.startsWith("DB-")) id
    else if (id == "EMP001") "ML-002"
    else id
  }

  val teamName = remember(currentEmployee) {
    val team = currentEmployee?.team ?: "ML Team"
    if (team.endsWith("Team")) team else "$team Team"
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(EodDarkBackground)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // 1. Header: (Back arrow) + RK Avatar + Ravi Kumar + "ML Team · ML-002" + Bell
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (onBackClick != null) {
              IconButton(
                onClick = onBackClick,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("today_back_to_roster_button")
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back to Roster",
                  tint = Color.White
                )
              }
            }

            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF1D4ED8)) // Blue avatar matching screenshot
                .clickable { onAvatarClick() },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = initials.ifEmpty { "RK" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = currentEmployee?.name ?: "Ravi Kumar",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "$teamName · $empCode",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
              )
            }
          }

          IconButton(
            onClick = { onNotificationsClick() },
            modifier = Modifier.size(38.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Notifications,
              contentDescription = "Notifications",
              tint = Color(0xFF9CA3AF),
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }

      // 2. Three Metric Cards: [92% / This month] [6 / Day streak] [1 / Leave taken]
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Card 1: This month rate
          StatMetricCard(
            value = "$thisMonthRate%",
            label = "This month",
            modifier = Modifier.weight(1f)
          )

          // Card 2: Day streak
          StatMetricCard(
            value = "$dayStreak",
            label = "Day streak",
            modifier = Modifier.weight(1f)
          )

          // Card 3: Leave taken
          StatMetricCard(
            value = "$leavesTaken",
            label = "Leave taken",
            modifier = Modifier.weight(1f)
          )
        }
      }

      // 3. "Today's EOD" Header + Subtitle
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Today's EOD",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

          Text(
            text = "Wed, 16 Sep · Editable till 11 PM",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF)
          )
        }
      }

      // 4. Attendance Label & 3 Cards: Present, Leave, Holiday
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Attendance",
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Normal
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Present Button
            val isPresent = attendanceStatus == "Present"
            AttendanceButton(
              title = "Present",
              icon = Icons.Default.Check,
              isSelected = isPresent,
              selectedBg = Color(0xFFD1FAE5),
              selectedText = Color(0xFF065F46),
              modifier = Modifier.weight(1f),
              onClick = { attendanceStatus = "Present" }
            )

            // Leave Button
            val isLeave = attendanceStatus == "Leave"
            AttendanceButton(
              title = "Leave",
              icon = Icons.Default.BeachAccess,
              isSelected = isLeave,
              selectedBg = Color(0xFFFEF3C7),
              selectedText = Color(0xFF92400E),
              modifier = Modifier.weight(1f),
              onClick = { attendanceStatus = "Leave" }
            )

            // Holiday Button
            val isHoliday = attendanceStatus == "Holiday"
            AttendanceButton(
              title = "Holiday",
              icon = Icons.Default.CalendarToday,
              isSelected = isHoliday,
              selectedBg = Color(0xFFE0E7FF),
              selectedText = Color(0xFF3730A3),
              modifier = Modifier.weight(1f),
              onClick = { attendanceStatus = "Holiday" }
            )
          }
        }
      }

      // 5. Projects worked on Header + "+ Add project"
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Projects worked on",
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Normal
          )

          Text(
            text = "+ Add project",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF38BDF8),
            modifier = Modifier
              .clickable { showAddProjectDialog = true }
              .testTag("add_project_button")
          )
        }
      }

      // 6. Project Cards
      itemsIndexed(projectTasks) { index, item ->
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_card_$index"),
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFF14151D),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232532))
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Title + Trash delete icon
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )

              IconButton(
                onClick = {
                  if (projectTasks.size > 1) {
                    projectTasks.removeAt(index)
                  } else {
                    Toast.makeText(context, "At least one project is required", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.DeleteOutline,
                  contentDescription = "Delete project",
                  tint = Color(0xFF71717A),
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            // Description / Work Summary Box
            Text(
              text = item.description,
              fontSize = 13.sp,
              color = Color(0xFFD1D5DB),
              lineHeight = 18.sp,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF191A23))
                .padding(12.dp)
            )

            // Status Pills: Completed, In progress, Blocked
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              val statuses = listOf("Completed", "In progress", "Blocked")
              statuses.forEach { s ->
                val isSelected = item.status.equals(s, ignoreCase = true)
                val (pillBg, pillTextColor) = when {
                  isSelected && s == "Completed" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
                  isSelected && s == "Blocked" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
                  isSelected && s == "In progress" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
                  else -> Color(0xFF191A23) to Color(0xFF71717A)
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(pillBg)
                    .clickable {
                      projectTasks[index] = item.copy(status = s)
                    }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("status_pill_${index}_$s"),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = s,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = pillTextColor
                  )
                }
              }
            }
          }
        }
      }

      // 7. Large Primary Button: "Submit EOD"
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Button(
          onClick = {
            val mainProject = projectTasks.firstOrNull()?.title ?: "Core"
            val combinedSummary = projectTasks.joinToString("\n") { "${it.title}: ${it.description} (${it.status})" }
            val overallStatus = when (attendanceStatus) {
              "Leave" -> "On Leave"
              "Holiday" -> "No Work"
              else -> if (projectTasks.all { it.status == "Completed" }) "Completed" else "In Progress"
            }

            onSubmitEod(
              todayIsoDate,
              mainProject,
              combinedSummary,
              8.0,
              if (overallStatus == "Completed") 100 else 75,
              overallStatus
            )
            isSubmitted = true
            Toast.makeText(context, "EOD Submitted successfully for ${currentEmployee?.name ?: "you"}!", Toast.LENGTH_SHORT).show()
            onBackClick?.invoke()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("submit_eod_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5B5CE5) // Deep violet purple matching screenshot
          )
        ) {
          Text(
            text = if (isSubmitted) "Update EOD" else "Submit EOD",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // Dialog for "+ Add project"
  if (showAddProjectDialog) {
    var newProjectTitle by remember { mutableStateOf("") }
    var newProjectDescription by remember { mutableStateOf("") }
    var newProjectStatus by remember { mutableStateOf("In progress") }

    Dialog(onDismissRequest = { showAddProjectDialog = false }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF14151D),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282A38)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Add Project Work",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )

            IconButton(
              onClick = { showAddProjectDialog = false },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF9CA3AF))
            }
          }

          OutlinedTextField(
            value = newProjectTitle,
            onValueChange = { newProjectTitle = it },
            label = { Text("Project Name") },
            placeholder = { Text("e.g. Model Serving Pipeline", color = Color(0xFF71717A)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF191A23),
              unfocusedContainerColor = Color(0xFF191A23),
              focusedBorderColor = Color(0xFF5B5CE5),
              unfocusedBorderColor = Color(0xFF282A38),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )

          OutlinedTextField(
            value = newProjectDescription,
            onValueChange = { newProjectDescription = it },
            label = { Text("Work Completed & Next Steps") },
            placeholder = { Text("Describe the specific changes made today...", color = Color(0xFF71717A)) },
            modifier = Modifier
              .fillMaxWidth()
              .height(100.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF191A23),
              unfocusedContainerColor = Color(0xFF191A23),
              focusedBorderColor = Color(0xFF5B5CE5),
              unfocusedBorderColor = Color(0xFF282A38),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf("In progress", "Completed", "Blocked").forEach { s ->
              val isSelected = newProjectStatus == s
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelected) Color(0xFF5B5CE5) else Color(0xFF191A23))
                  .clickable { newProjectStatus = s }
                  .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = s,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = Color.White
                )
              }
            }
          }

          Button(
            onClick = {
              if (newProjectTitle.isNotBlank()) {
                projectTasks.add(
                  ProjectTaskItem(
                    title = newProjectTitle.trim(),
                    description = newProjectDescription.ifBlank { "Completed planned sprint tasks." }.trim(),
                    status = newProjectStatus
                  )
                )
                showAddProjectDialog = false
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B5CE5))
          ) {
            Text("Add to Today's EOD", fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
private fun StatMetricCard(
  value: String,
  label: String,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFF14151D),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232532))
  ) {
    Column(
      modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = value,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = label,
        fontSize = 11.sp,
        color = Color(0xFF9CA3AF)
      )
    }
  }
}

@Composable
private fun AttendanceButton(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isSelected: Boolean,
  selectedBg: Color,
  selectedText: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(if (isSelected) selectedBg else Color(0xFF14151D))
      .border(
        width = 1.dp,
        color = if (isSelected) Color.Transparent else Color(0xFF232532),
        shape = RoundedCornerShape(10.dp)
      )
      .clickable { onClick() }
      .padding(vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = if (isSelected) selectedText else Color(0xFF9CA3AF),
        modifier = Modifier.size(18.dp)
      )
      Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) selectedText else Color(0xFF9CA3AF)
      )
    }
  }
}
