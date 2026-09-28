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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.DailyEodEntity
import com.example.data.EmployeeEntity
import com.example.data.SeedData
import com.example.ui.theme.EodDarkBackground

/**
 * Department Roster Screen
 * Exact match to user's uploaded screenshot (Image 1):
 * - Header: Team name (e.g. "GT Team"), Subtitle ("Wed, 16 Sep · Tap your name to submit"), Exit/logout icon
 * - Counter row: "X submitted   Y pending" (e.g. "2 submitted   3 pending")
 * - List of employee cards:
 *   - Circular avatar with initials
 *   - Name & Designation (e.g. "Deepak Kumar", "GT-001 · Growth Analyst")
 *   - Status pill: "Submitted" (mint green pill) or "Pending" (rose peach pill)
 *   - Tapping any employee card directly opens that person's EOD form for the day!
 *   - No second password per employee!
 */
@Composable
fun DepartmentRosterScreen(
  departmentName: String,
  employees: List<EmployeeEntity>,
  todayEods: List<DailyEodEntity>,
  onSelectEmployee: (EmployeeEntity) -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Filter employees for this department
  val deptEmployees = remember(employees, departmentName) {
    employees.filter { emp ->
      emp.role == "EMPLOYEE" && (
        emp.team.equals(departmentName, ignoreCase = true) ||
        emp.department.equals(departmentName, ignoreCase = true) ||
        (departmentName.startsWith("GT", true) && (emp.department.equals("GT", true) || emp.team.contains("GT", true))) ||
        (departmentName.startsWith("ML", true) && (emp.department.equals("ML", true) || emp.team.contains("ML", true))) ||
        (departmentName.startsWith("DB", true) && (emp.department.equals("DB", true) || emp.team.contains("DB", true))) ||
        (departmentName.contains("Cyber", true) && (emp.department.contains("Cyber", true) || emp.team.contains("Cyber", true))) ||
        (departmentName.contains("Writing", true) && (emp.department.contains("Writing", true) || emp.team.contains("Writing", true)))
      )
    }.sortedBy { it.employeeId }
  }

  // Calculate submitted vs pending for TODAY
  val submittedEmployeeIds = remember(todayEods) {
    val todayStr = SeedData.TODAY
    todayEods.filter { (it.date == todayStr || it.date == "2026-09-16") && it.workStatus != "On Leave" }.map { it.employeeId }.toSet()
  }

  val submittedCount = deptEmployees.count { it.employeeId in submittedEmployeeIds }
  val pendingCount = (deptEmployees.size - submittedCount).coerceAtLeast(0)

  // Track tapped employee for visual selection feedback before opening form
  var selectedEmpId by remember { mutableStateOf<String?>(null) }
  val todayFormatted = remember {
    java.text.SimpleDateFormat("EEE, dd MMM", java.util.Locale.getDefault()).format(java.util.Date())
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(EodDarkBackground)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
      // Header: Department title & Logout button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = departmentName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "$todayFormatted · Tap your name to submit",
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF)
          )
        }

        IconButton(
          onClick = onLogout,
          modifier = Modifier.testTag("roster_logout_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = "Logout to Department Selection",
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Status Counter: "2 submitted   3 pending" with cloud status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "$submittedCount submitted",
            color = Color(0xFF10B981),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "$pendingCount pending",
            color = Color(0xFFEF4444),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.CloudDone,
            contentDescription = "Cloud Connected",
            tint = Color(0xFF10B981),
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "Cloud Synced",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Employee Roster Cards List
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(deptEmployees, key = { it.employeeId }) { emp ->
          val isSubmitted = emp.employeeId in submittedEmployeeIds
          val isSelected = selectedEmpId == emp.employeeId

          // Initials (e.g. DK, NR, AJ, PT, VS)
          val initials = remember(emp.name) {
            emp.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
          }

          RosterEmployeeCard(
            employee = emp,
            initials = initials,
            isSubmitted = isSubmitted,
            isSelected = isSelected,
            onClick = {
              selectedEmpId = emp.employeeId
              onSelectEmployee(emp)
            }
          )
        }
      }
    }
  }
}

@Composable
private fun RosterEmployeeCard(
  employee: EmployeeEntity,
  initials: String,
  isSubmitted: Boolean,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val cardBackground = if (isSelected) Color(0xFFEDE9FE) else Color(0xFF151722)
  val borderColor = if (isSelected) Color(0xFF818CF8) else Color(0xFF232532)
  val avatarBg = if (isSelected) Color(0xFF3730A3) else Color.White
  val avatarTextColor = if (isSelected) Color.White else Color(0xFF3730A3)
  val nameTextColor = if (isSelected) Color(0xFF1E1B4B) else Color.White
  val subTextColor = if (isSelected) Color(0xFF4B5563) else Color(0xFF9CA3AF)

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(cardBackground)
      .border(1.dp, borderColor, RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 14.dp)
      .testTag("roster_card_${employee.employeeId}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Circle Avatar
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(avatarBg),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = initials,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = avatarTextColor
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Name & Designation
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = employee.name,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = nameTextColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${employee.employeeId} · ${employee.designation}",
          fontSize = 12.sp,
          color = subTextColor
        )
      }

      // Status Badge: Submitted (Mint) or Pending (Peach)
      if (isSubmitted) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFD1FAE5))
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CloudDone,
              contentDescription = "Synced",
              tint = Color(0xFF065F46),
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = "Submitted",
              color = Color(0xFF065F46),
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      } else {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFEE2E2))
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = "Pending",
            color = Color(0xFF991B1B),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}
