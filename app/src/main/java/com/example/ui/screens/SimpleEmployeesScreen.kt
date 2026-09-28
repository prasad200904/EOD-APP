package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DailyEodEntity
import com.example.data.EmployeeEntity
import com.example.data.Role
import com.example.data.SeedData
import com.example.data.TeamEntity
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WorkBackground
import com.example.ui.theme.WorkOutline
import com.example.ui.theme.WorkOutlineVariant
import com.example.ui.theme.WorkPrimary

/**
 * Screen presenting simple, visible data of all employees.
 * Stripped of student behavior metrics, penalty points, or disciplinary tracking.
 * Pure professional employee profiles, contacts, assignments, and verified EOD history.
 */
@Composable
fun SimpleEmployeesScreen(
  employees: List<EmployeeEntity>,
  eods: List<DailyEodEntity>,
  teams: List<TeamEntity> = emptyList(),
  currentRole: Role = Role.EMPLOYEE,
  onOpenAddEmployee: (() -> Unit)? = null,
  onToggleEmployeeActive: ((String, Boolean) -> Unit)? = null,
  onDeleteEmployeeAccount: ((String) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedTeamFilter by remember { mutableStateOf("All") }
  var selectedEmployeeForDetail by remember { mutableStateOf<EmployeeEntity?>(null) }

  // Unique team names
  val teamOptions = remember(employees) {
    listOf("All") + employees.map { it.team }.distinct().sorted()
  }

  // Filtered employees list
  val filteredEmployees = remember(employees, searchQuery, selectedTeamFilter) {
    employees.filter { emp ->
      val matchesTeam = selectedTeamFilter == "All" || emp.team.equals(selectedTeamFilter, ignoreCase = true)
      val matchesSearch = searchQuery.isBlank() ||
        emp.name.contains(searchQuery, ignoreCase = true) ||
        emp.employeeId.contains(searchQuery, ignoreCase = true) ||
        emp.department.contains(searchQuery, ignoreCase = true) ||
        emp.designation.contains(searchQuery, ignoreCase = true) ||
        emp.defaultProject.contains(searchQuery, ignoreCase = true)
      matchesTeam && matchesSearch
    }.sortedBy { it.name }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(WorkBackground)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // -------------------------------------------------------------------
    // 1. Header Card
    // -------------------------------------------------------------------
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
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Employee Directory",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Simple, visible profiles & assignments for all team members",
                fontSize = 12.sp,
                color = Color(0xFFEADDFF)
              )
            }

            if (currentRole != Role.EMPLOYEE && onOpenAddEmployee != null) {
              Button(
                onClick = onOpenAddEmployee,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color.White,
                  contentColor = Color(0xFF21005D)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "${employees.size} Total Employees",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
            Text(
              text = "•",
              fontSize = 12.sp,
              color = Color(0xFFEADDFF)
            )
            val todayEodSubmittedCount = employees.count { emp ->
              eods.any { it.employeeId == emp.employeeId && it.date == SeedData.TODAY }
            }
            Text(
              text = "$todayEodSubmittedCount Submitted Today",
              fontSize = 12.sp,
              color = Color(0xFFC8E6C9),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // -------------------------------------------------------------------
    // 2. Search Field & Team Filter Chips
    // -------------------------------------------------------------------
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search by name, ID, department, or project...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WorkPrimary) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("simple_employee_search_field"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WorkPrimary,
            unfocusedBorderColor = WorkOutlineVariant
          )
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(teamOptions) { teamName ->
            FilterChip(
              selected = selectedTeamFilter == teamName,
              onClick = { selectedTeamFilter = teamName },
              label = { Text(teamName, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFEADDFF),
                selectedLabelColor = Color(0xFF21005D)
              )
            )
          }
        }
      }
    }

    // -------------------------------------------------------------------
    // 3. Employee Cards List (Clean, Simple, Professional)
    // -------------------------------------------------------------------
    items(filteredEmployees) { emp ->
      val hasTodayEod = eods.any { it.employeeId == emp.employeeId && it.date == SeedData.TODAY }
      val empEodCount = eods.count { it.employeeId == emp.employeeId }

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable { selectedEmployeeForDetail = emp }
          .testTag("simple_emp_card_${emp.employeeId}"),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Top Row: Avatar + Name + ID + EOD Status
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              UserAvatar(name = emp.name, size = 42, backgroundColor = WorkPrimary)
              Column {
                Text(
                  text = emp.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = Color(0xFF1D1B20)
                )
                Text(
                  text = "${emp.employeeId} • ${emp.designation}",
                  fontSize = 12.sp,
                  color = Color(0xFF49454F)
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (hasTodayEod) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            ) {
              Text(
                text = if (hasTodayEod) "EOD SUBMITTED" else "EOD PENDING",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (hasTodayEod) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          HorizontalDivider(color = WorkOutlineVariant)

          // Team & Assigned Project Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Groups, contentDescription = null, tint = WorkPrimary, modifier = Modifier.size(15.dp))
              Text(
                text = "${emp.team} (${emp.department})",
                fontSize = 12.sp,
                color = Color(0xFF21005D),
                fontWeight = FontWeight.SemiBold
              )
            }

            Text(
              text = "$empEodCount logged EODs",
              fontSize = 11.sp,
              color = Color(0xFF79747E)
            )
          }

          // Project & Contact
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF79747E), modifier = Modifier.size(14.dp))
              Text(
                text = "Assigned: ${emp.defaultProject}",
                fontSize = 11.sp,
                color = Color(0xFF49454F)
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF79747E), modifier = Modifier.size(12.dp))
              Text(
                text = emp.email,
                fontSize = 11.sp,
                color = Color(0xFF79747E)
              )
            }
          }
        }
      }
    }
  }

  // Clean, comfortable employee detail dialog (NO student behavior metrics)
  selectedEmployeeForDetail?.let { emp ->
    val empEods = remember(eods, emp.employeeId) {
      eods.filter { it.employeeId == emp.employeeId }.sortedByDescending { it.date }
    }

    Dialog(onDismissRequest = { selectedEmployeeForDetail = null }) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              UserAvatar(name = emp.name, size = 44, backgroundColor = WorkPrimary)
              Column {
                Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1D1B20))
                Text("${emp.employeeId} • ${emp.designation}", fontSize = 12.sp, color = Color(0xFF49454F))
              }
            }

            IconButton(onClick = { selectedEmployeeForDetail = null }) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }

          HorizontalDivider(color = WorkOutlineVariant)

          // Details List
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Department: ${emp.department} • Team: ${emp.team}", fontSize = 12.sp, color = Color(0xFF1D1B20))
            Text("Email: ${emp.email} • Phone: ${emp.phone}", fontSize = 12.sp, color = Color(0xFF49454F))
            Text("Default Project: ${emp.defaultProject}", fontSize = 12.sp, color = Color(0xFF21005D), fontWeight = FontWeight.SemiBold)
          }

          HorizontalDivider(color = WorkOutlineVariant)

          // Recent EOD History
          Text(
            text = "RECENT EOD ENTRIES (${empEods.size})",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF79747E)
          )

          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (empEods.isEmpty()) {
              item {
                Text("No EOD submissions logged yet.", fontSize = 12.sp, color = Color(0xFF79747E))
              }
            } else {
              items(empEods) { e ->
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = Color(0xFFF7F2FA),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(e.date, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF21005D))
                      Text("${e.workStatus}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF10B981))
                    }
                    Text("Project: ${e.project}", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Medium)
                    Text(e.todayWork, fontSize = 11.sp, color = Color(0xFF1D1B20))
                  }
                }
              }
            }
          }

          Button(
            onClick = { selectedEmployeeForDetail = null },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WorkPrimary),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Close", fontWeight = FontWeight.Bold)
          }

          // Admin-only: Deactivate/Activate Employee Button
          if (currentRole == Role.ADMIN && onToggleEmployeeActive != null) {
            OutlinedButton(
              onClick = {
                onToggleEmployeeActive(emp.employeeId, emp.isActive)
                selectedEmployeeForDetail = null
              },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (emp.isActive) Color(0xFFB71C1C) else Color(0xFF1B5E20)
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                if (emp.isActive) Icons.Default.Close else Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                if (emp.isActive) "Deactivate Employee" else "Activate Employee",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }

          // Admin-only: Delete Employee Account Button with Confirmation
          if (currentRole == Role.ADMIN && onDeleteEmployeeAccount != null && emp.employeeId != "ADMIN") {
            var showConfirmDelete by remember { mutableStateOf(false) }

            if (showConfirmDelete) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFEE2E2),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text(
                    text = "Permanently delete ${emp.name}'s account?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF991B1B)
                  )
                  Text(
                    text = "This will remove the account and all associated EOD logs.",
                    fontSize = 11.sp,
                    color = Color(0xFF7F1D1D)
                  )
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedButton(
                      onClick = { showConfirmDelete = false },
                      shape = RoundedCornerShape(8.dp),
                      modifier = Modifier.weight(1f)
                    ) {
                      Text("Cancel", fontSize = 11.sp)
                    }
                    Button(
                      onClick = {
                        showConfirmDelete = false
                        selectedEmployeeForDetail = null
                        onDeleteEmployeeAccount(emp.employeeId)
                      },
                      shape = RoundedCornerShape(8.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                      modifier = Modifier.weight(1f)
                    ) {
                      Text("Confirm Delete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            } else {
              OutlinedButton(
                onClick = { showConfirmDelete = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Employee Account", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }
      }
    }
  }
}
