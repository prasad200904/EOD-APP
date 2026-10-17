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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
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
  onAddEmployeeClick: () -> Unit,
  onAddTeamClick: () -> Unit = {},
  onResetFirestoreClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var selectedMemberForDetail by remember { mutableStateOf<TeamMemberBehaviorItem?>(null) }
  var sortByPendingFirst by remember { mutableStateOf(true) }

  // Filter members if a specific department is chosen
  val filteredMembers = remember(teamMembers, selectedDepartmentCode, sortByPendingFirst) {
    val list = if (selectedDepartmentCode == "ALL" || selectedDepartmentCode.isBlank()) {
      teamMembers
    } else {
      teamMembers.filter {
        it.employee.department.equals(selectedDepartmentCode, ignoreCase = true) ||
          it.employee.team.contains(selectedDepartmentCode, ignoreCase = true) ||
          it.employee.departmentId.contains(selectedDepartmentCode, ignoreCase = true)
      }
    }
    if (sortByPendingFirst) {
      list.sortedWith(compareBy({ it.todayStatus != "Pending" }, { it.submissionRate }))
    } else {
      list.sortedBy { it.submissionRate }
    }
  }

  val displaySubmitted = remember(filteredMembers) {
    filteredMembers.count { it.todayStatus == "Done" }
  }
  val displayTotal = filteredMembers.size
  val displayOnLeave = remember(filteredMembers) {
    filteredMembers.count { it.todayStatus == "Leave" }
  }
  val displayMissedTotal = remember(filteredMembers) {
    filteredMembers.fold(0) { acc, item -> acc + item.missedCount }
  }
  val displayLateTotal = remember(filteredMembers) {
    filteredMembers.fold(0) { acc, item -> acc + item.lateCount }
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

      // 2. Department Filter Chips: [ All ] [ ML ] [ GT ] [ DB ] [ Cyber ] [ Writing ]
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Filter by Department",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = WarmTextMuted
          )
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



      // 3c. Firebase Sync Banner: Reset Firestore with Local Data (ONE-TIME ADMIN)
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFFFFE8D6), // Warm orange tint
          border = BorderStroke(1.dp, Color(0xFFFB923C)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "🔥 Reset Firebase Data (Admin Only)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Delete all old Firestore employees and upload 73 new CSV employees",
                fontSize = 11.sp,
                color = WarmTextMuted
              )
            }

            Button(
              onClick = onResetFirestoreClick,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB923C)),
              modifier = Modifier.testTag("admin_reset_firestore_btn")
            ) {
              Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Reset Firebase", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

          // Missed Days Rollup
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = WarmCardWhite,
            border = BorderStroke(1.dp, WarmCardBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Total Missed",
                fontSize = 11.sp,
                color = WarmTextMuted
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "$displayMissedTotal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (displayMissedTotal > 0) Color(0xFFF87171) else Color(0xFF34D399)
              )
            }
          }
        }
      }

      // 5. Live Team Status Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
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
              text = "Today's submission status per employee",
              fontSize = 11.sp,
              color = WarmTextMuted
            )
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (sortByPendingFirst) WarmCardWhite else Color.Transparent,
            border = BorderStroke(1.dp, WarmCardBorder),
            modifier = Modifier
              .clickable { sortByPendingFirst = !sortByPendingFirst }
              .testTag("sort_pending_first_btn")
          ) {
            Text(
              text = if (sortByPendingFirst) "Pending first" else "Name order",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = DarkSlateDeep,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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

    // Detail Modal when tapping on employee behavior rollup
    selectedMemberForDetail?.let { item ->
      AdminBehaviorDetailModal(
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
  val initials = remember(emp.name) {
    val parts = emp.name.trim().split(" ")
    if (parts.size >= 2) "${parts[0].first()}${parts[1].first()}".uppercase()
    else emp.name.take(2).uppercase()
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = WarmCardWhite,
    border = BorderStroke(1.dp, WarmCardBorder),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("admin_employee_card_${emp.employeeId}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(CoralPrimary),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = initials,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        Column {
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = emp.name,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = DarkSlateDeep
            )
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = CoralPrimary.copy(alpha = 0.1f),
              border = BorderStroke(1.dp, CoralPrimary)
            ) {
              Text(
                text = emp.department,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = CoralPrimary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = "${emp.employeeId} · ${emp.designation}",
            fontSize = 11.sp,
            color = WarmTextMuted
          )
        }
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Today's Status Pill
        when (item.todayStatus) {
          "Done" -> {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = EodDonePillBg
            ) {
              Text(
                text = "Done",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = EodDonePillText,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
          "Leave" -> {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color(0xFF2E2412)
            ) {
              Text(
                text = "Leave",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFBBF24),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
          else -> {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = EodActivePillBg
            ) {
              Text(
                text = "Pending",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = EodActivePillText,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
        }

        // Nudge action if pending
        if (item.todayStatus == "Pending") {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = CoralPrimary.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, CoralPrimary),
            modifier = Modifier.clickable { onNudge() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Send, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(11.dp))
              Text("Nudge", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoralPrimary)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AdminBehaviorDetailModal(
  item: TeamMemberBehaviorItem,
  onDismiss: () -> Unit,
  onSendNudge: () -> Unit
) {
  val emp = item.employee
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = WarmCardWhite,
      border = BorderStroke(1.dp, WarmCardBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Modal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = emp.name,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = DarkSlateDeep
            )
            Text(
              text = "${emp.department} Team · ${emp.employeeId}",
              fontSize = 12.sp,
              color = WarmTextMuted
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
          }
        }

        HorizontalDivider(color = WarmCardBorder)

        // 4 Rollup metrics
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Submission Rate
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F1017)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("Rate", fontSize = 10.sp, color = EodTextMuted)
              Text(
                "${item.submissionRate}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.submissionRate >= 80) Color(0xFF34D399) else Color(0xFFF87171)
              )
            }
          }

          // Missed Days
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F1017)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("Missed", fontSize = 10.sp, color = EodTextMuted)
              Text(
                "${item.missedCount} days",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.missedCount > 0) Color(0xFFF87171) else Color(0xFF9CA3AF)
              )
            }
          }

          // Late
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F1017)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("Late", fontSize = 10.sp, color = EodTextMuted)
              Text(
                "${item.lateCount}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.lateCount > 0) Color(0xFFFBBF24) else Color(0xFF9CA3AF)
              )
            }
          }

          // Streak
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F1017)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("Streak", fontSize = 10.sp, color = EodTextMuted)
              Text(
                "🔥 ${item.currentStreak}d",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFB923C)
              )
            }
          }
        }

        // Details summary
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF14151D),
          border = BorderStroke(1.dp, EodDarkCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Designation: ${emp.designation}", fontSize = 12.sp, color = EodTextSecondary)
            Text("Default Project: ${emp.defaultProject}", fontSize = 12.sp, color = EodTextSecondary)
            Text("Email: ${emp.email}", fontSize = 12.sp, color = EodTextSecondary)
            Text("Joining Date: ${emp.joiningDate}", fontSize = 12.sp, color = EodTextSecondary)
          }
        }

        // Action Button
        Button(
          onClick = onSendNudge,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EodPrimaryPurple),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Send EOD Reminder Nudge", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }
}
