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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyEodEntity
import com.example.data.EmployeeEntity
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSlateDeep
import com.example.ui.theme.SubtleText
import com.example.ui.theme.WarmCardBorder
import com.example.ui.theme.WarmCardWhite
import com.example.ui.theme.WarmCream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class AdminHistoryMonth(
  val key: String,
  val label: String,
  val endDate: String,
  val expectedWeekdays: Int
)

private data class EmployeeMonthBehavior(
  val employee: EmployeeEntity,
  val eodRate: Int?,
  val onTimeRate: Int?,
  val workCompletionRate: Int?,
  val submittedDays: Int,
  val expectedDays: Int,
  val lateDays: Int,
  val missingDays: Int
)

private fun adminHistoryMonth(monthOffset: Int): AdminHistoryMonth {
  val now = Calendar.getInstance()
  val month = (now.clone() as Calendar).apply {
    add(Calendar.MONTH, -monthOffset)
    set(Calendar.DAY_OF_MONTH, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
  }
  val lastDay = if (monthOffset == 0) {
    now.get(Calendar.DAY_OF_MONTH)
  } else {
    month.getActualMaximum(Calendar.DAY_OF_MONTH)
  }
  val end = (month.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, lastDay) }
  val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  var weekdays = 0
  val dayCursor = month.clone() as Calendar
  for (dayOfMonth in 1..lastDay) {
    dayCursor.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    val day = dayCursor.get(Calendar.DAY_OF_WEEK)
    if (day != Calendar.SATURDAY && day != Calendar.SUNDAY) weekdays++
  }

  return AdminHistoryMonth(
    key = SimpleDateFormat("yyyy-MM", Locale.US).format(month.time),
    label = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(month.time),
    endDate = dateFormat.format(end.time),
    expectedWeekdays = weekdays
  )
}

private fun isWeekday(date: String): Boolean {
  val parsed = runCatching {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date.take(10))
  }.getOrNull() ?: return false
  val day = Calendar.getInstance().apply { time = parsed }.get(Calendar.DAY_OF_WEEK)
  return day != Calendar.SATURDAY && day != Calendar.SUNDAY
}

private fun isExcusedEod(eod: DailyEodEntity): Boolean =
  eod.workStatus.equals("On Leave", ignoreCase = true) ||
    eod.attendanceStatus.equals("Leave", ignoreCase = true) ||
    eod.attendanceStatus.equals("Holiday", ignoreCase = true)

private fun percentage(numerator: Int, denominator: Int): Int? =
  if (denominator <= 0) null else ((numerator * 100) / denominator).coerceIn(0, 100)

private fun calculateEmployeeMonthBehavior(
  employee: EmployeeEntity,
  eods: List<DailyEodEntity>,
  month: AdminHistoryMonth
): EmployeeMonthBehavior {
  val employeeEods = eods.filter {
    it.employeeId == employee.employeeId &&
      it.date.take(7) == month.key &&
      it.date.take(10) <= month.endDate
  }
  val excusedDays = employeeEods
    .filter { isExcusedEod(it) && isWeekday(it.date) }
    .map { it.date.take(10) }
    .distinct()
    .size
  val expectedDays = (month.expectedWeekdays - excusedDays).coerceAtLeast(0)
  val workEods = employeeEods.filter { !isExcusedEod(it) && isWeekday(it.date) }
  val submittedDays = workEods.map { it.date.take(10) }.distinct().size
  val completedCount = workEods.count { it.workStatus.equals("Completed", ignoreCase = true) }

  return EmployeeMonthBehavior(
    employee = employee,
    eodRate = percentage(submittedDays, expectedDays),
    onTimeRate = percentage(workEods.count { it.isOnTime }, workEods.size),
    workCompletionRate = percentage(completedCount, workEods.size),
    submittedDays = submittedDays,
    expectedDays = expectedDays,
    lateDays = workEods.count { !it.isOnTime },
    missingDays = (expectedDays - submittedDays).coerceAtLeast(0)
  )
}

@Composable
fun AdminHistoryScreen(
  employees: List<EmployeeEntity>,
  eods: List<DailyEodEntity>,
  onAvatarClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var monthOffset by remember { mutableStateOf(0) }
  var searchQuery by remember { mutableStateOf("") }
  val month = remember(monthOffset) { adminHistoryMonth(monthOffset) }

  val monthEods = remember(eods, month) {
    eods.filter { it.date.take(7) == month.key && it.date.take(10) <= month.endDate }
      .sortedWith(compareByDescending<DailyEodEntity> { it.date }.thenBy { it.employeeName })
  }
  val matchingEmployees = remember(employees, searchQuery) {
    employees.filter { employee ->
      searchQuery.isBlank() ||
        employee.name.contains(searchQuery, ignoreCase = true) ||
        employee.employeeId.contains(searchQuery, ignoreCase = true) ||
        employee.department.contains(searchQuery, ignoreCase = true) ||
        employee.team.contains(searchQuery, ignoreCase = true)
    }
  }
  val matchingEmployeeIds = remember(matchingEmployees) { matchingEmployees.map { it.employeeId }.toSet() }
  val visibleEods = remember(monthEods, matchingEmployees, searchQuery) {
    monthEods.filter { eod ->
      if (searchQuery.isBlank()) true
      else eod.employeeId in matchingEmployeeIds ||
        eod.employeeName.contains(searchQuery, ignoreCase = true) ||
        eod.todayWork.contains(searchQuery, ignoreCase = true) ||
        eod.project.contains(searchQuery, ignoreCase = true)
    }
  }
  val behavior = remember(matchingEmployees, monthEods, month) {
    matchingEmployees.map { calculateEmployeeMonthBehavior(it, monthEods, month) }
      .sortedWith(compareBy<EmployeeMonthBehavior>({ it.eodRate ?: 0 }, { it.employee.name }))
  }
  val workEods = remember(monthEods) {
    monthEods.filterNot(::isExcusedEod).filter { isWeekday(it.date) }
  }
  val onTimeRate = percentage(workEods.count { it.isOnTime }, workEods.size)
  val completionRate = percentage(
    workEods.count { it.workStatus.equals("Completed", ignoreCase = true) },
    workEods.size
  )

  Box(
    modifier = modifier.fillMaxSize().background(WarmCream)
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Admin EOD History", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkSlateDeep)
            Text("Monthly submissions and employee performance", fontSize = 12.sp, color = SubtleText)
          }
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = CoralPrimary,
            modifier = Modifier
              .testTag("admin_history_avatar")
              .clickable { onAvatarClick() }
              .padding(1.dp)
          ) {
            Text(
              "AD",
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = monthOffset == 0,
            onClick = { monthOffset = 0 },
            label = { Text("Current month") },
            modifier = Modifier.testTag("admin_history_current_month")
          )
          FilterChip(
            selected = monthOffset == 1,
            onClick = { monthOffset = 1 },
            label = { Text("Previous month") },
            modifier = Modifier.testTag("admin_history_previous_month")
          )
        }
        Text(month.label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkSlateDeep)
      }

      item {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          SummaryMetricCard("EOD reports", monthEods.size.toString(), Modifier.weight(1f))
          SummaryMetricCard("On-time EODs", onTimeRate?.let { "$it%" } ?: "—", Modifier.weight(1f))
          SummaryMetricCard("Work completed", completionRate?.let { "$it%" } ?: "—", Modifier.weight(1f))
        }
      }

      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier.fillMaxWidth().testTag("admin_history_search"),
          label = { Text("Search employee or report") },
          singleLine = true
        )
      }

      item {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
          Text("Employee behavior", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkSlateDeep)
          Text(
            "Rates use submitted EOD records and weekdays in ${month.label}; leave and holidays are excluded from work rates.",
            fontSize = 11.sp,
            color = SubtleText
          )
        }
      }

      if (behavior.isEmpty()) {
        item { HistoryEmptyState("No employees match this search.") }
      } else {
        items(behavior, key = { it.employee.employeeId }) { metrics ->
          EmployeeBehaviorCard(metrics)
        }
      }

      item {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
          Text("EOD submissions", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkSlateDeep)
          Text("${visibleEods.size} reports · ${month.label}", fontSize = 11.sp, color = SubtleText)
        }
      }

      if (visibleEods.isEmpty()) {
        item { HistoryEmptyState("No EOD submissions found for ${month.label}.") }
      } else {
        items(visibleEods, key = { "${it.employeeId}_${it.date}" }) { eod ->
          EodHistoryCard(eod)
        }
      }
    }
  }
}

@Composable
private fun SummaryMetricCard(label: String, value: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = WarmCardWhite,
    border = androidx.compose.foundation.BorderStroke(1.dp, WarmCardBorder)
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
      Text(label, fontSize = 10.sp, color = SubtleText)
      Text(value, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = DarkSlateDeep)
    }
  }
}

@Composable
private fun EmployeeBehaviorCard(metrics: EmployeeMonthBehavior) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = WarmCardWhite,
    border = androidx.compose.foundation.BorderStroke(1.dp, WarmCardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(metrics.employee.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkSlateDeep)
          Text("${metrics.employee.employeeId} · ${metrics.employee.team}", fontSize = 11.sp, color = SubtleText)
        }
        Text(
          "${metrics.submittedDays}/${metrics.expectedDays} EODs",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = DarkSlateDeep
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        BehaviorValue("EOD adherence", metrics.eodRate?.let { "$it%" } ?: "—", Modifier.weight(1f))
        BehaviorValue("On time", metrics.onTimeRate?.let { "$it%" } ?: "—", Modifier.weight(1f))
        BehaviorValue("Work completed", metrics.workCompletionRate?.let { "$it%" } ?: "—", Modifier.weight(1f))
      }
      Text("${metrics.missingDays} missing · ${metrics.lateDays} late", fontSize = 11.sp, color = SubtleText)
    }
  }
}

@Composable
private fun BehaviorValue(label: String, value: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text(label, fontSize = 9.sp, color = SubtleText, maxLines = 1)
    Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkSlateDeep)
  }
}

@Composable
private fun EodHistoryCard(eod: DailyEodEntity) {
  val isExcused = isExcusedEod(eod)
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = WarmCardWhite,
    border = androidx.compose.foundation.BorderStroke(1.dp, WarmCardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(eod.employeeName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkSlateDeep)
          Text("${eod.employeeId} · ${eod.date.take(10)}", fontSize = 11.sp, color = SubtleText)
        }
        Text(
          eod.workStatus.ifBlank { "Submitted" },
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = if (isExcused) Color(0xFFB45309) else if (eod.isOnTime) Color(0xFF15803D) else Color(0xFFB91C1C)
        )
      }
      if (eod.project.isNotBlank()) Text("Project: ${eod.project}", fontSize = 12.sp, color = DarkSlateDeep)
      if (eod.todayWork.isNotBlank()) Text(eod.todayWork, fontSize = 12.sp, color = SubtleText)
      Text(
        "${if (isExcused) eod.attendanceStatus else if (eod.isOnTime) "On time" else "Late"} · ${eod.submissionTime}",
        fontSize = 11.sp,
        color = SubtleText
      )
    }
  }
}

@Composable
private fun HistoryEmptyState(message: String) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = WarmCardWhite,
    border = androidx.compose.foundation.BorderStroke(1.dp, WarmCardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
      Text(message, fontSize = 13.sp, color = SubtleText)
    }
  }
}
