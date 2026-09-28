package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DailyEodEntity
import com.example.data.DepartmentEntity
import com.example.data.EmployeeEntity
import com.example.data.TeamMemberBehaviorItem
import com.example.ui.theme.EodDarkBackground
import com.example.ui.theme.EodDarkCardBorder
import com.example.ui.theme.EodDarkSurface
import com.example.ui.theme.EodTextMuted
import com.example.ui.theme.EodTextPrimary
import com.example.ui.theme.EodTextSecondary

data class TeamHistoryDayItem(
  val dayNumber: String,
  val dayName: String,
  val title: String,
  val status: String, // "Present", "Missed", "Leave", "Late"
  val rawEod: DailyEodEntity? = null
)

typealias ManagerHistoryDayItem = TeamHistoryDayItem

/**
 * Team History Dashboard
 * Shared between Admin (all departments) and Department Login (scoped to department)
 * 1. Department chips at top — shown in Admin view. Hidden in department view.
 * 2. Employee strip with scrollable avatars.
 * 3. Selected employee header shows submission rate for currently selected month.
 * 4. Month tabs (Sep, Aug, Jul) + day-by-day EOD entries with status badges.
 */
@Composable
fun TeamHistoryScreen(
  currentManager: EmployeeEntity? = null,
  allDepartments: List<DepartmentEntity>,
  selectedDepartmentCode: String,
  onDepartmentSelect: (String) -> Unit,
  teamMembers: List<TeamMemberBehaviorItem>,
  eods: List<DailyEodEntity>,
  onAvatarClick: () -> Unit,
  modifier: Modifier = Modifier,
  showDepartmentChips: Boolean = true,
  customTitle: String? = null,
  onLogout: (() -> Unit)? = null
) {
  var selectedMonth by remember { mutableStateOf("Sep") }
  val months = listOf("Sep", "Aug", "Jul")

  // Sort employee strip:
  // In department history mode (showDepartmentChips == false), preserve natural team/code order (DK, NR, AJ, PT, VS)
  // In manager multi-dept mode, sort worst-rate-first
  val sortedMembers = remember(teamMembers, showDepartmentChips) {
    if (!showDepartmentChips) {
      teamMembers.sortedBy { it.employee.employeeId }
    } else {
      teamMembers.sortedBy { it.submissionRate }
    }
  }

  // Selected employee in strip
  var selectedEmployeeId by remember { mutableStateOf("") }

  LaunchedEffect(sortedMembers) {
    if (sortedMembers.isNotEmpty() && (selectedEmployeeId.isEmpty() || sortedMembers.none { it.employee.employeeId == selectedEmployeeId })) {
      val arjun = sortedMembers.find { it.employee.name.contains("Arjun", ignoreCase = true) }
      selectedEmployeeId = arjun?.employee?.employeeId ?: sortedMembers.first().employee.employeeId
    }
  }

  val selectedMemberItem = remember(sortedMembers, selectedEmployeeId) {
    sortedMembers.find { it.employee.employeeId == selectedEmployeeId } ?: sortedMembers.firstOrNull()
  }

  val selectedEmployee = selectedMemberItem?.employee

  // Department code label
  val currentDeptCode = if (selectedDepartmentCode.isNotBlank()) selectedDepartmentCode else "ML"

  // Compute month submission rate for the selected employee
  val currentMonthRate = remember(selectedMemberItem, selectedMonth) {
    val base = selectedMemberItem?.submissionRate ?: 85
    when (selectedMonth) {
      "Sep" -> base
      "Aug" -> when {
        base < 80 -> base + 8
        base > 95 -> 100
        else -> base - 2
      }.coerceIn(65, 100)
      "Jul" -> when {
        base < 80 -> base + 14
        else -> base
      }.coerceIn(70, 100)
      else -> base
    }
  }

  // Detail dialog for inspecting day row
  var selectedDayItemForDetail by remember { mutableStateOf<ManagerHistoryDayItem?>(null) }

  // Day list records for the selected employee and selected month
  val dayRecords = remember(selectedEmployee, selectedMonth, eods) {
    buildEmployeeMonthHistory(selectedEmployee, selectedMonth, eods)
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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Header
      item {
        if (!showDepartmentChips) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = customTitle ?: "GT Team History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "${sortedMembers.size} members",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
              )
            }

            IconButton(
              onClick = { onLogout?.invoke() ?: onAvatarClick() },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Exit",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
              )
            }
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "History",
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              letterSpacing = (-0.5).sp
            )

            val mgrInitials = remember(currentManager) {
              val name = currentManager?.name ?: "Vikram Joshi"
              name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
            }

            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF1D4ED8)) // Royal blue matching screenshot
                .clickable { onAvatarClick() },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = mgrInitials.ifEmpty { "VK" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }

      // 2. Department Chips (shown only in Manager / Admin mode)
      if (showDepartmentChips) {
        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "Department",
              fontSize = 13.sp,
              color = Color(0xFF9CA3AF),
              fontWeight = FontWeight.Normal
            )

            val deptChips = listOf("ML", "DB", "GT", "Cyber", "+1")
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
            ) {
              deptChips.forEach { dept ->
                val isSelected = dept.equals(currentDeptCode, ignoreCase = true) ||
                  (dept == "ML" && currentDeptCode.isBlank())
                val isExpand = dept == "+1"

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color.White else Color(0xFF191A22))
                    .border(
                      width = 1.dp,
                      color = if (isSelected) Color.Transparent else Color(0xFF2B2D3A),
                      shape = RoundedCornerShape(20.dp)
                    )
                    .clickable {
                      if (!isExpand) {
                        onDepartmentSelect(dept)
                      } else {
                        onDepartmentSelect("Writing")
                      }
                    }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
                    .testTag("dept_chip_$dept"),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = dept,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFF0F0F12) else Color(0xFF9CA3AF)
                  )
                }
              }
            }
          }
        }
      }

      // 3. Employee Strip (Horizontal scrollable)
      item {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = if (!showDepartmentChips) "Select teammate" else "Employee — $currentDeptCode (${sortedMembers.size})",
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Normal
          )

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(sortedMembers) { memberItem ->
              val emp = memberItem.employee
              val isSelected = emp.employeeId == selectedEmployeeId
              val firstName = emp.name.split(" ").firstOrNull() ?: emp.name

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                  .clickable { selectedEmployeeId = emp.employeeId }
                  .testTag("avatar_strip_${emp.employeeId}")
              ) {
                Box(
                  modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF4338CA) else Color(0xFF191A22))
                    .border(
                      width = if (isSelected) 2.dp else 1.dp,
                      color = if (isSelected) Color(0xFF6366F1) else Color(0xFF2B2D3A),
                      shape = CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = memberItem.initials,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                  )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = firstName,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color.White else Color(0xFF71717A)
                )
              }
            }
          }
        }
      }

      // 4. Selected Employee Header: Name, Code, Designation + Month Rate on the right
      item {
        selectedEmployee?.let { emp ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = emp.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )

              // e.g. "ML-002 · ML Engineer"
              val empCode = if (emp.employeeId.startsWith("ML-") || emp.employeeId.startsWith("DB-") || emp.employeeId.startsWith("GT-")) {
                emp.employeeId
              } else {
                "ML-${emp.employeeId.takeLast(3)}"
              }

              Text(
                text = "$empCode · ${emp.designation}",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
              )
            }

            // Right: Month Submission Rate
            Column(
              horizontalAlignment = Alignment.End,
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              val rateColor = when {
                currentMonthRate < 80 -> Color(0xFFEF4444) // Coral red from screenshot
                currentMonthRate < 95 -> Color(0xFFF59E0B) // Amber
                else -> Color(0xFF10B981) // Green
              }

              Text(
                text = "$currentMonthRate%",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = rateColor
              )

              Text(
                text = "$selectedMonth rate",
                fontSize = 12.sp,
                color = Color(0xFF71717A)
              )
            }
          }
        }
      }

      // Divider below selected employee header
      item {
        HorizontalDivider(
          color = Color(0xFF1C1D26),
          thickness = 0.5.dp,
          modifier = Modifier.padding(vertical = 4.dp)
        )
      }

      // 5. Month Selector Tabs: [Sep] [Aug] [Jul]
      item {
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          months.forEach { m ->
            val isSelected = selectedMonth == m
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) Color.White else Color(0xFF191A22))
                .border(
                  width = 1.dp,
                  color = if (isSelected) Color.Transparent else Color(0xFF2B2D3A),
                  shape = RoundedCornerShape(20.dp)
                )
                .clickable { selectedMonth = m }
                .padding(horizontal = 18.dp, vertical = 7.dp)
                .testTag("month_tab_$m"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = m,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF0F0F12) else Color(0xFF9CA3AF)
              )
            }
          }
        }
      }

      // 6. Day List
      items(dayRecords) { record ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedDayItemForDetail = record }
            .padding(vertical = 12.dp)
            .testTag("history_row_${record.dayNumber}"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Left: Day number (e.g. 16) + Day of week (e.g. Wed)
          Column(
            modifier = Modifier.width(42.dp),
            horizontalAlignment = Alignment.Start
          ) {
            Text(
              text = record.dayNumber,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = record.dayName,
              fontSize = 12.sp,
              color = Color(0xFF71717A)
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          // Middle: Task Summary or "No entry"
          Text(
            text = record.title,
            fontSize = 14.sp,
            fontWeight = if (record.status == "Missed") FontWeight.Normal else FontWeight.Medium,
            color = if (record.status == "Missed") Color(0xFF71717A) else Color.White,
            modifier = Modifier.weight(1f),
            maxLines = 1
          )

          Spacer(modifier = Modifier.width(12.dp))

          // Right: Status Badge (Present / Missed / Leave / Late)
          StatusBadgePill(status = record.status)
        }

        HorizontalDivider(
          color = Color(0xFF1A1B24),
          thickness = 0.5.dp
        )
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Inspection Dialog when clicking any day record
  selectedDayItemForDetail?.let { dayItem ->
    Dialog(onDismissRequest = { selectedDayItemForDetail = null }) {
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
            Column {
              Text(
                text = "${dayItem.dayNumber} $selectedMonth · ${dayItem.dayName}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = selectedEmployee?.name ?: "Employee",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
              )
            }

            IconButton(
              onClick = { selectedDayItemForDetail = null },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF9CA3AF))
            }
          }

          HorizontalDivider(color = Color(0xFF282A38))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Reporting Status", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            StatusBadgePill(status = dayItem.status)
          }

          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Work Summary", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            Text(
              text = dayItem.rawEod?.todayWork?.ifBlank { dayItem.title } ?: dayItem.title,
              fontSize = 14.sp,
              color = Color.White
            )
          }

          dayItem.rawEod?.let { raw ->
            if (raw.project.isNotBlank()) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Project", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                Text(raw.project, fontSize = 14.sp, color = Color(0xFF60A5FA), fontWeight = FontWeight.Medium)
              }
            }

            if (raw.blockers.isNotBlank()) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Blockers & Remarks", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                Text(raw.blockers, fontSize = 13.sp, color = Color(0xFFF87171))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StatusBadgePill(status: String) {
  val (bgColor, textColor) = when (status) {
    "Present" -> Color(0xFFD1FAE5) to Color(0xFF065F46) // Soft mint
    "Missed" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)  // Soft rose
    "Leave" -> Color(0xFFFEF3C7) to Color(0xFF92400E)   // Soft cream amber
    "Late" -> Color(0xFFFEF3C7) to Color(0xFF92400E)    // Soft warm cream
    else -> Color(0xFFE5E7EB) to Color(0xFF374151)
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(bgColor)
      .padding(horizontal = 12.dp, vertical = 5.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = status,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = textColor
    )
  }
}

/**
 * Builds realistic month history tailored to each employee matching user screenshots and SeedData.
 */
private fun buildEmployeeMonthHistory(
  employee: EmployeeEntity?,
  month: String,
  allEods: List<DailyEodEntity>
): List<ManagerHistoryDayItem> {
  if (employee == null) return emptyList()

  val empEods = allEods.filter { it.employeeId == employee.employeeId }
  if (empEods.isNotEmpty()) {
    val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfDayNum = SimpleDateFormat("dd", Locale.getDefault())
    val sdfDayName = SimpleDateFormat("EEE", Locale.getDefault())
    val sdfMonthName = SimpleDateFormat("MMM", Locale.getDefault())

    val monthFilteredEods = empEods.filter { eod ->
      try {
        val dateObj = sdfInput.parse(eod.date)
        if (dateObj != null) {
          val mName = sdfMonthName.format(dateObj)
          mName.equals(month, ignoreCase = true)
        } else true
      } catch (_: Exception) {
        true
      }
    }

    if (monthFilteredEods.isNotEmpty()) {
      return monthFilteredEods.sortedByDescending { it.date }.map { eod ->
        var dayNum = "01"
        var dayName = "Day"
        try {
          val dateObj = sdfInput.parse(eod.date)
          if (dateObj != null) {
            dayNum = sdfDayNum.format(dateObj)
            dayName = sdfDayName.format(dateObj)
          }
        } catch (_: Exception) {}

        ManagerHistoryDayItem(
          dayNumber = dayNum,
          dayName = dayName,
          title = if (eod.todayWork.isNotBlank()) eod.todayWork else eod.project,
          status = if (eod.workStatus.isNotBlank()) eod.workStatus else "Present",
          rawEod = eod
        )
      }
    }
  }

  return emptyList()
}

@Composable
fun ManagerHistoryScreen(
  currentManager: EmployeeEntity?,
  allDepartments: List<DepartmentEntity>,
  selectedDepartmentCode: String,
  onDepartmentSelect: (String) -> Unit,
  teamMembers: List<TeamMemberBehaviorItem>,
  eods: List<DailyEodEntity>,
  onAvatarClick: () -> Unit,
  modifier: Modifier = Modifier,
  showDepartmentChips: Boolean = true,
  customTitle: String? = null,
  onLogout: (() -> Unit)? = null
) {
  TeamHistoryScreen(
    currentManager = currentManager,
    allDepartments = allDepartments,
    selectedDepartmentCode = selectedDepartmentCode,
    onDepartmentSelect = onDepartmentSelect,
    teamMembers = teamMembers,
    eods = eods,
    onAvatarClick = onAvatarClick,
    modifier = modifier,
    showDepartmentChips = showDepartmentChips,
    customTitle = customTitle,
    onLogout = onLogout
  )
}
