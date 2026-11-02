package com.example.ui.screens

import com.example.ui.theme.WarmCream
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSlateDeep
import com.example.ui.theme.SubtleText
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmployeeEntity
import com.example.data.DailyEodEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * AO Roster Dashboard
 * Shows all employees for selected AO role (Marketing, Counsellor, Tele Caller)
 * Allows selecting an employee to submit their EOD
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AORosterDashboardScreen(
  role: String, // "Marketing", "Counsellor", "Tele Caller"
  employees: List<EmployeeEntity>,
  todayEods: List<DailyEodEntity>,
  onEmployeeClick: (EmployeeEntity) -> Unit,
  onLogout: () -> Unit
) {
  // Light warm theme colors
  val backgroundColor = com.example.ui.theme.WarmCream
  val cardColor = Color.White
  val primaryColor = com.example.ui.theme.CoralPrimary
  val textPrimary = com.example.ui.theme.DarkSlateDeep
  val textSecondary = com.example.ui.theme.SubtleText
  val successColor = Color(0xFF10B981)
  val warningColor = Color(0xFFF59E0B)

  val today = remember {
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
  }

  // Filter employees for this AO role ONLY (Marketing/Counsellor/Tele Caller)
  val roleEmployees = remember(employees, role) {
    android.util.Log.d("AORosterDashboard", "=== FILTERING AO EMPLOYEES ===")
    android.util.Log.d("AORosterDashboard", "Role: '$role'")
    android.util.Log.d("AORosterDashboard", "Total Employees: ${employees.size}")
    
    val filtered = employees.filter { emp ->
      // Must be from Academic Overseas company
      val isAO = emp.companyId == "AO" || emp.employeeId.startsWith("AO")
      
      // Must match the specific role/designation
      val matchesRole = when {
        role.contains("Marketing", ignoreCase = true) -> 
          emp.designation.contains("Marketing", ignoreCase = true) ||
          emp.team.contains("Marketing", ignoreCase = true)
        role.contains("Counsellor", ignoreCase = true) || role.contains("Counselor", ignoreCase = true) -> 
          emp.designation.contains("Counsellor", ignoreCase = true) ||
          emp.designation.contains("Counselor", ignoreCase = true) ||
          emp.team.contains("Counsellor", ignoreCase = true) ||
          emp.team.contains("Counselor", ignoreCase = true)
        role.contains("Tele Caller", ignoreCase = true) || role.contains("Telecaller", ignoreCase = true) -> 
          emp.designation.contains("Tele Caller", ignoreCase = true) ||
          emp.designation.contains("Telecaller", ignoreCase = true) ||
          emp.team.contains("Tele Caller", ignoreCase = true) ||
          emp.team.contains("Telecaller", ignoreCase = true)
        else -> false
      }
      
      isAO && matchesRole && emp.role == "EMPLOYEE"
    }.sortedBy { it.employeeId }
    
    android.util.Log.d("AORosterDashboard", "Filtered Employees: ${filtered.size}")
    filtered.forEach { emp ->
      android.util.Log.d("AORosterDashboard", "  - ${emp.name} (${emp.employeeId}) designation=${emp.designation} team=${emp.team}")
    }
    
    filtered
  }

  Scaffold(
    containerColor = backgroundColor,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "$role Team",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = textPrimary
            )
            Text(
              text = "Academic Overseas",
              fontSize = 12.sp,
              color = textSecondary
            )
          }
        },
        actions = {
          IconButton(onClick = onLogout) {
            Icon(
              imageVector = Icons.Default.Logout,
              contentDescription = "Logout",
              tint = textPrimary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = cardColor
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(backgroundColor)
    ) {
      // Stats Header
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = cardColor
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          StatItem(
            label = "Total",
            value = roleEmployees.size.toString(),
            color = primaryColor
          )
          StatItem(
            label = "Submitted",
            value = todayEods.filter { it.date == today }.size.toString(),
            color = successColor
          )
          StatItem(
            label = "Pending",
            value = (roleEmployees.size - todayEods.filter { it.date == today }.size).toString(),
            color = warningColor
          )
        }
      }

      // Section Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Group,
          contentDescription = null,
          tint = textSecondary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Team Members",
          fontSize = 14.sp,
          color = textSecondary,
          fontWeight = FontWeight.Medium
        )
      }

      // Employee List
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(roleEmployees) { employee ->
          val hasSubmittedToday = todayEods.any { 
            it.employeeId == employee.employeeId && it.date == today 
          }
          
          EmployeeCard(
            employee = employee,
            hasSubmittedToday = hasSubmittedToday,
            onClick = { onEmployeeClick(employee) },
            cardColor = cardColor,
            primaryColor = primaryColor,
            successColor = successColor,
            warningColor = warningColor,
            textPrimary = textPrimary,
            textSecondary = textSecondary
          )
        }
        
        // Bottom spacing
        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
private fun StatItem(
  label: String,
  value: String,
  color: Color
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = value,
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold,
      color = color
    )
    Text(
      text = label,
      fontSize = 12.sp,
      color = SubtleText
    )
  }
}

@Composable
private fun EmployeeCard(
  employee: EmployeeEntity,
  hasSubmittedToday: Boolean,
  onClick: () -> Unit,
  cardColor: Color,
  primaryColor: Color,
  successColor: Color,
  warningColor: Color,
  textPrimary: Color,
  textSecondary: Color
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    colors = CardDefaults.cardColors(
      containerColor = cardColor
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar with initial
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(primaryColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = employee.name.firstOrNull()?.uppercase() ?: "?",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = primaryColor
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      // Employee Info
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = employee.name,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          color = textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = employee.employeeId,
          fontSize = 13.sp,
          color = textSecondary
        )
        if (employee.defaultProject.isNotBlank()) {
          Text(
            text = employee.defaultProject,
            fontSize = 11.sp,
            color = textSecondary.copy(alpha = 0.7f)
          )
        }
      }

      // Status Indicator
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (hasSubmittedToday) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Submitted",
            tint = successColor,
            modifier = Modifier.size(24.dp)
          )
          Text(
            text = "Done",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = successColor
          )
        } else {
          Icon(
            imageVector = Icons.Default.PendingActions,
            contentDescription = "Pending",
            tint = warningColor,
            modifier = Modifier.size(24.dp)
          )
          Text(
            text = "Pending",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = warningColor
          )
        }
        
        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = "Open",
          tint = textSecondary,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
