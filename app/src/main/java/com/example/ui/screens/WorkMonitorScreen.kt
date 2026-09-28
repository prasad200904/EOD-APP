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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmployeeEntity
import com.example.data.EmployeeWorkMonitorItem
import com.example.ui.components.EmployeeWorkMonitorCard
import com.example.ui.theme.WorkBackground
import com.example.ui.theme.WorkOutline
import com.example.ui.theme.WorkOutlineVariant
import com.example.ui.theme.WorkPrimary

@Composable
fun WorkMonitorScreen(
  monitorItems: List<EmployeeWorkMonitorItem>,
  currentFilter: String,
  onFilterChange: (String) -> Unit,
  onSelectEmployee: (EmployeeEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }

  // Section 4 Filters:
  // All Employees, EOD Submitted, EOD Pending, Completed, In Progress, Blocked, No Work, On Leave
  val filterOptions = listOf(
    "All Employees",
    "EOD Submitted",
    "EOD Pending",
    "Completed",
    "In Progress",
    "Blocked",
    "No Work",
    "On Leave"
  )

  val filteredItems = monitorItems.filter { item ->
    // Filter match
    val matchesFilter = when (currentFilter) {
      "All Employees", "All" -> true
      "EOD Submitted" -> item.isSubmittedToday
      "EOD Pending" -> !item.isSubmittedToday
      "Completed" -> item.workStatus == "Completed"
      "In Progress" -> item.workStatus == "In Progress"
      "Blocked" -> item.workStatus == "Blocked"
      "No Work" -> item.workStatus == "No Work"
      "On Leave" -> item.workStatus == "On Leave"
      else -> true
    }

    // Search match
    val matchesSearch = searchQuery.isBlank() ||
      item.employee.name.contains(searchQuery, ignoreCase = true) ||
      item.employee.employeeId.contains(searchQuery, ignoreCase = true) ||
      item.employee.department.contains(searchQuery, ignoreCase = true) ||
      item.todayWork.contains(searchQuery, ignoreCase = true)

    matchesFilter && matchesSearch
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(WorkBackground)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Employee Work Monitor",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF21005D)
        )
        Text(
          text = "Who is working on what today (${filteredItems.size} showing)",
          fontSize = 12.sp,
          color = Color(0xFF49454F)
        )
      }
    }

    // Search Box
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("monitor_search_input"),
      placeholder = { Text("Search employee, department, or work...", fontSize = 13.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF79747E)) },
      trailingIcon = {
        if (searchQuery.isNotBlank()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF79747E))
          }
        }
      },
      shape = RoundedCornerShape(12.dp),
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = WorkPrimary,
        unfocusedBorderColor = WorkOutlineVariant,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
      )
    )

    // Section 4 Filters Row (Horizontal Scroll)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      filterOptions.forEach { option ->
        val selected = currentFilter == option || (currentFilter == "All" && option == "All Employees")
        FilterChip(
          selected = selected,
          onClick = { onFilterChange(option) },
          label = {
            Text(
              text = option,
              fontSize = 11.sp,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFEADDFF),
            selectedLabelColor = Color(0xFF21005D),
            containerColor = Color.White
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) WorkPrimary else WorkOutlineVariant
          ),
          modifier = Modifier.testTag("filter_chip_$option")
        )
      }
    }

    // Section 4 Table Card List:
    // | Employee | Today's Work | Hours | Progress | Status | EOD (Submitted vs Pending) |
    if (filteredItems.isEmpty()) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
          .clip(RoundedCornerShape(16.dp)),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
      ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("No employees match this filter", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF49454F))
            Text("Try clearing filters or changing search terms", fontSize = 12.sp, color = Color(0xFF79747E))
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredItems, key = { it.employee.employeeId }) { item ->
          EmployeeWorkMonitorCard(
            employeeName = item.employee.name,
            department = "${item.employee.department} · ${item.employee.designation}",
            todayWork = item.todayWork,
            hours = item.hours,
            progress = item.progress,
            status = item.workStatus,
            isEodSubmitted = item.isSubmittedToday,
            isOnTime = item.isOnTime,
            attentionReason = item.attentionReason,
            onClick = { onSelectEmployee(item.employee) },
            modifier = Modifier.testTag("monitor_card_${item.employee.employeeId}")
          )
        }
      }
    }
  }
}
