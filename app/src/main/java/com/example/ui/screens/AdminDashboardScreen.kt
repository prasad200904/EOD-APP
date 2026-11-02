package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.DepartmentEntity
import com.example.data.CompanyEntity
import com.example.data.DailyEodEntity
import com.example.data.EmployeeEntity
import com.example.data.TeamMemberBehaviorItem
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSlateDeep
import com.example.ui.theme.EodActivePillBg
import com.example.ui.theme.EodActivePillText
import com.example.ui.theme.EodAvatarBg
import com.example.ui.theme.EodAvatarText
import com.example.ui.theme.EodDarkBackground
import com.example.ui.theme.EodDarkCardBorder
import com.example.ui.theme.EodDarkSurface
import com.example.ui.theme.EodDonePillBg
import com.example.ui.theme.EodDonePillText
import com.example.ui.theme.EodPrimaryPurple
import com.example.ui.theme.EodTextMuted
import com.example.ui.theme.EodTextPrimary
import com.example.ui.theme.EodTextSecondary
import com.example.ui.theme.WarmCardBorder
import com.example.ui.theme.WarmCardWhite
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmTextMuted

/**
 * Admin Dashboard Screen
 * Capabilities:
 * 1. Add employee account (form with department selector)
 * 2. Behaviour rollups: submission rate, missed days, late submissions, current streak per employee/department
 * 3. Scoped across all departments with quick department filter chips
 */
@Composable
fun AdminDashboardScreen(
  allDepartments: List<DepartmentEntity>,
  allCompanies: List<CompanyEntity>,
  selectedDepartmentCode: String,
  selectedCompanyId: String,
  onDepartmentSelect: (String) -> Unit,
  onCompanySelect: (String) -> Unit,
  teamMembers: List<TeamMemberBehaviorItem>,
  submittedCount: Int,
  totalCount: Int,
  onLeaveCount: Int,
  onNotificationsClick: () -> Unit,
  onAvatarClick: () -> Unit,
  onSendNudge: (EmployeeEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedMemberForDetail by remember { mutableStateOf<TeamMemberBehaviorItem?>(null) }

  // teamMembers is already filtered by the ViewModel/Repository using the correct code↔name mapping.
  // Sort: submitted (Done) first, then alphabetical within each group.
  val filteredMembers = remember(teamMembers) {
    teamMembers.sortedWith(compareBy({ it.todayEod == null }, { it.employee.name }))
  }

  val displaySubmitted = remember(filteredMembers) {
    filteredMembers.count { it.todayEod != null }
  }
  val displayTotal = filteredMembers.size
  val displayOnLeave = remember(filteredMembers) {
    filteredMembers.count { it.todayEod?.let(::isLeaveSubmission) == true }
  }
  val displayPending = remember(filteredMembers) {
    filteredMembers.count { it.todayEod == null }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(WarmCream)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Header: Admin Console title, date, bell icon, avatar
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Admin Console",
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = DarkSlateDeep
            )
            Text(
              text = "All Departments · Live Monitoring",
              fontSize = 12.sp,
              color = WarmTextMuted
            )
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(WarmCardWhite)
                .border(BorderStroke(1.dp, WarmCardBorder), CircleShape)
                .clickable { onNotificationsClick() }
                .testTag("admin_notifications_bell"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = WarmTextMuted,
                modifier = Modifier.size(20.dp)
              )
            }

            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CoralPrimary)
                .border(BorderStroke(1.dp, CoralPrimary), CircleShape)
                .clickable { onAvatarClick() }
                .testTag("admin_avatar_button"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "AD",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }

      // 2. Department Filter Chips: [ All ] [ ML ] [ GT ] [ DB ] [ Cyber ] [ Writing ] + Clear Filters button
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Filter by Department",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = WarmTextMuted
            )
            
            // Clear Filters Button
            val hasActiveFilters = selectedDepartmentCode != "ALL" || selectedCompanyId != "ALL"
            if (hasActiveFilters) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = CoralPrimary,
                modifier = Modifier
                  .clickable {
                    onDepartmentSelect("ALL")
                    onCompanySelect("ALL")
                  }
                  .testTag("clear_filters_btn")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.FilterAltOff,
                    contentDescription = "Clear Filters",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = "Clear Filters",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              }
            }
          }
          
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            item {
              val isSelected = selectedDepartmentCode == "ALL" || selectedDepartmentCode.isBlank()
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) CoralPrimary else WarmCardWhite,
                border = BorderStroke(1.dp, if (isSelected) CoralPrimary else WarmCardBorder),
                modifier = Modifier
                  .clickable { onDepartmentSelect("ALL") }
                  .testTag("dept_chip_ALL")
              ) {
                Text(
                  text = "All Depts",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isSelected) Color.White else DarkSlateDeep,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
              }
            }

            items(allDepartments) { dept ->
              val isSelected = selectedDepartmentCode.equals(dept.code, ignoreCase = true)
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) CoralPrimary else WarmCardWhite,
                border = BorderStroke(1.dp, if (isSelected) CoralPrimary else WarmCardBorder),
                modifier = Modifier
                  .clickable { onDepartmentSelect(dept.code) }
                  .testTag("dept_chip_${dept.code}")
              ) {
                Text(
                  text = "${dept.code} · ${dept.name}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isSelected) Color.White else DarkSlateDeep,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
              }
            }
          }
        }
      }

      // 2.5 Company Filter Chips: [ All Companies ] [ Sandspace Technologies ] [ Academic Overseas ]
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Filter by Company",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = WarmTextMuted
          )
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            item {
              val isSelected = selectedCompanyId == "ALL"
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) CoralPrimary else WarmCardWhite,
                border = BorderStroke(1.dp, if (isSelected) CoralPrimary else WarmCardBorder),
                modifier = Modifier
                  .clickable { onCompanySelect("ALL") }
                  .testTag("company_chip_ALL")
              ) {
                Text(
                  text = "All Companies",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isSelected) Color.White else DarkSlateDeep,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
              }
            }

            items(allCompanies) { company ->
              val isSelected = selectedCompanyId == company.companyId
              val displayName = when {
                company.type == "PARENT" -> "🏢 ${company.name}"
                else -> "🏛️ ${company.name}"
              }
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) CoralPrimary else WarmCardWhite,
                border = BorderStroke(1.dp, if (isSelected) CoralPrimary else WarmCardBorder),
                modifier = Modifier
                  .clickable { onCompanySelect(company.companyId) }
                  .testTag("company_chip_${company.companyId}")
              ) {
                Text(
                  text = displayName,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isSelected) Color.White else DarkSlateDeep,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
              }
            }
          }
        }
      }

      // 4. Metric Cards
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Submitted Today
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = WarmCardWhite,
            border = BorderStroke(1.dp, WarmCardBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Submitted today",
                fontSize = 11.sp,
                color = WarmTextMuted
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                  text = "$displaySubmitted",
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  color = DarkSlateDeep
                )
                Text(
                  text = " / $displayTotal",
                  fontSize = 14.sp,
                  color = WarmTextMuted,
                  modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                )
              }
            }
          }

          // On Leave
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = WarmCardWhite,
            border = BorderStroke(1.dp, WarmCardBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "On leave",
                fontSize = 11.sp,
                color = WarmTextMuted
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "$displayOnLeave",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFBBF24)
              )
            }
          }

          // Pending today
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = WarmCardWhite,
            border = BorderStroke(1.dp, WarmCardBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Pending today",
                fontSize = 11.sp,
                color = WarmTextMuted
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "$displayPending",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (displayPending > 0) Color(0xFFF87171) else Color(0xFF34D399)
              )
            }
          }
        }
      }

      // 5. Live Team Status Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Live Team Status",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = DarkSlateDeep
            )
            Text(
              text = "Submitted first · Today's EOD status",
              fontSize = 11.sp,
              color = WarmTextMuted
            )
          }
        }
      }

      // 6. Live Status List (Today's status only)
      items(filteredMembers, key = { it.employee.employeeId }) { item ->
        AdminEmployeeLiveStatusCard(
          item = item,
          onClick = { selectedMemberForDetail = item },
          onNudge = { onSendNudge(item.employee) }
        )
      }
    }

    // Show the employee's current EOD submission instead of behavior rollups.
    selectedMemberForDetail?.let { item ->
      AdminEodStatusModal(
        item = item,
        onDismiss = { selectedMemberForDetail = null },
        onSendNudge = {
          onSendNudge(item.employee)
          selectedMemberForDetail = null
        }
      )
    }
  }
}

@Composable
private fun AdminEmployeeLiveStatusCard(
  item: TeamMemberBehaviorItem,
  onClick: () -> Unit,
  onNudge: () -> Unit,
  modifier: Modifier = Modifier
) {
  val emp = item.employee
  val eod = item.todayEod
  val isLeave = eod?.let(::isLeaveSubmission) == true
  val isDone = eod != null && !isLeave
  val statusLabel = when {
    isLeave -> "On leave"
    eod == null -> "Pending"
    eod.workStatus.isBlank() -> "Submitted"
    else -> eod.workStatus
  }
  val initials = remember(emp.name) {
    val parts = emp.name.trim().split(" ")
    if (parts.size >= 2) "${parts[0].first()}${parts[1].first()}".uppercase()
    else emp.name.take(2).uppercase()
  }

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = if (isDone) Color(0xFFD1FAE5) else WarmCardWhite,
    border = BorderStroke(1.dp, if (isDone) Color(0xFF6EE7B7) else WarmCardBorder),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("admin_employee_card_${emp.employeeId}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        // Avatar
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (isDone) Color(0xFF10B981) else CoralPrimary),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = initials,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
        Column {
          Text(
            text = emp.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkSlateDeep,
            maxLines = 1
          )
          Text(
            text = "${emp.employeeId} · ${emp.team}",
            fontSize = 11.sp,
            color = WarmTextMuted
          )
        }
      }
      // Status pill — compact
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = when {
          isDone -> Color(0xFF10B981)
          isLeave -> Color(0xFFF59E0B)
          else -> Color(0xFFE5E7EB)
        }
      ) {
        Text(
          text = when {
            isDone -> statusLabel
            isLeave -> "On leave"
            else -> "Pending"
          },
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = when {
            isDone -> Color.White
            isLeave -> Color.White
            else -> Color(0xFF6B7280)
          },
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
      }
    }
  }
}

@Composable
private fun AdminEodStatusModal(
  item: TeamMemberBehaviorItem,
  onDismiss: () -> Unit,
  onSendNudge: () -> Unit
) {
  val employee = item.employee
  val eod = item.todayEod
  val isLeave = eod?.let(::isLeaveSubmission) == true
  val status = when {
    eod == null -> "Pending"
    isLeave -> "On leave"
    eod.workStatus.isBlank() -> "Submitted"
    else -> eod.workStatus
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = WarmCardWhite,
      border = BorderStroke(1.dp, WarmCardBorder),
      modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 560.dp)
          .verticalScroll(rememberScrollState())
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(employee.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkSlateDeep)
            Text("${employee.department} · ${employee.employeeId}", fontSize = 12.sp, color = WarmTextMuted)
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
          }
        }

        HorizontalDivider(color = WarmCardBorder)

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (isLeave) Color(0xFFFFF7ED) else if (eod == null) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Today's EOD", fontSize = 12.sp, color = WarmTextMuted)
            Text(status, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkSlateDeep)
            if (eod == null) {
              Text("No EOD submission has been recorded for today.", fontSize = 13.sp, color = WarmTextMuted)
            } else {
              Text(
                "${eod.attendanceStatus} · ${if (eod.isOnTime) "On time" else "Late"} · ${eod.submissionTime}",
                fontSize = 12.sp,
                color = WarmTextMuted
              )
            }
          }
        }

        if (eod != null) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF14151D),
            border = BorderStroke(1.dp, EodDarkCardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
              if (eod.project.isNotBlank()) Text("Project: ${eod.project}", fontSize = 12.sp, color = EodTextSecondary)
              Text("Progress: ${eod.progressPercentage}%", fontSize = 12.sp, color = EodTextSecondary)
              if (eod.todayWork.isNotBlank()) Text("Work summary: ${eod.todayWork}", fontSize = 12.sp, color = EodTextSecondary)
              if (eod.workCompleted.isNotBlank()) Text("Completed: ${eod.workCompleted}", fontSize = 12.sp, color = EodTextSecondary)
              if (eod.blockers.isNotBlank()) Text("Blockers: ${eod.blockers}", fontSize = 12.sp, color = EodTextSecondary)
              if (eod.remarks.isNotBlank()) Text("Remarks: ${eod.remarks}", fontSize = 12.sp, color = EodTextSecondary)
              if (eod.tomorrowPlan.isNotBlank()) Text("Next: ${eod.tomorrowPlan}", fontSize = 12.sp, color = EodTextSecondary)
            }
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF14151D),
          border = BorderStroke(1.dp, EodDarkCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Employee information", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EodTextSecondary)
            Text("Designation: ${employee.designation}", fontSize = 12.sp, color = EodTextSecondary)
            Text("Team: ${employee.team}", fontSize = 12.sp, color = EodTextSecondary)
            Text("Company: ${employee.companyId}", fontSize = 12.sp, color = EodTextSecondary)
            if (employee.defaultProject.isNotBlank()) Text("Default project: ${employee.defaultProject}", fontSize = 12.sp, color = EodTextSecondary)
            if (employee.email.isNotBlank()) Text("Email: ${employee.email}", fontSize = 12.sp, color = EodTextSecondary)
            if (employee.phone.isNotBlank()) Text("Phone: ${employee.phone}", fontSize = 12.sp, color = EodTextSecondary)
            if (employee.joiningDate.isNotBlank()) Text("Joining date: ${employee.joiningDate}", fontSize = 12.sp, color = EodTextSecondary)
          }
        }

        if (eod == null) {
          Button(
            onClick = onSendNudge,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EodPrimaryPurple),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Send EOD Reminder", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    }
  }
}

private fun isLeaveSubmission(eod: DailyEodEntity): Boolean =
  eod.workStatus.equals("On Leave", ignoreCase = true) ||
    eod.attendanceStatus.equals("Leave", ignoreCase = true) ||
    eod.attendanceStatus.equals("Holiday", ignoreCase = true)
