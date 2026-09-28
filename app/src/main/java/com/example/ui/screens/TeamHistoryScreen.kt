package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DailyEodEntity
import com.example.data.DepartmentEntity
import com.example.data.EmployeeEntity
import com.example.data.TeamMemberBehaviorItem
import com.example.data.firebase.FirebaseDataSource
import com.example.ui.theme.EodDarkBackground
import com.example.ui.theme.EodDarkCardBorder
import com.example.ui.theme.EodDarkSurface
import com.example.ui.theme.EodTextMuted
import com.example.ui.theme.EodTextPrimary
import com.example.ui.theme.EodTextSecondary
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class TeamHistoryDayItem(
  val dayNumber: String,
  val dayName: String,
  val title: String,
  val status: String, // "Present", "Missed", "Leave", "Late"
  val rawEod: DailyEodEntity? = null
)

typealias ManagerHistoryDayItem = TeamHistoryDayItem

/**
 * Calculates date ranges according to Requirements 3 & 4:
 * - "This month": 1st day of month 00:00:00 to last day 23:59:59
 * - "Previous month": 1st day of last month 00:00:00 to last day 23:59:59 (Handles Jan -> Dec of last year)
 * - "Custom date range": start date 00:00:00 to end date 23:59:59
 */
fun calculateDateRangeStrings(filterType: String, customStart: String, customEnd: String): Pair<String, String> {
  val cal = Calendar.getInstance()
  return when (filterType) {
    "This month" -> {
      cal.set(Calendar.DAY_OF_MONTH, 1)
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      val year = cal.get(Calendar.YEAR)
      val month = cal.get(Calendar.MONTH) + 1
      val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
      val start = String.format(Locale.US, "%04d-%02d-01 00:00:00", year, month)
      val end = String.format(Locale.US, "%04d-%02d-%02d 23:59:59", year, month, maxDay)
      Pair(start, end)
    }
    "Previous month" -> {
      // Calendar.add(Calendar.MONTH, -1) handles January -> December of last year automatically
      cal.add(Calendar.MONTH, -1)
      cal.set(Calendar.DAY_OF_MONTH, 1)
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      val year = cal.get(Calendar.YEAR)
      val month = cal.get(Calendar.MONTH) + 1
      val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
      val start = String.format(Locale.US, "%04d-%02d-01 00:00:00", year, month)
      val end = String.format(Locale.US, "%04d-%02d-%02d 23:59:59", year, month, maxDay)
      Pair(start, end)
    }
    "Custom date range" -> {
      val s = if (customStart.isNotBlank()) {
        if (customStart.contains(" ")) customStart else "$customStart 00:00:00"
      } else "2024-01-01 00:00:00"

      val e = if (customEnd.isNotBlank()) {
        if (customEnd.contains(" ")) customEnd else "$customEnd 23:59:59"
      } else "2030-12-31 23:59:59"

      Pair(s, e)
    }
    else -> Pair("2024-01-01 00:00:00", "2030-12-31 23:59:59")
  }
}

/**
 * Team History Dashboard
 * Fetches EOD history from Firestore "eodReports" collection by date range.
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
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val firebaseDataSource = remember { FirebaseDataSource(context) }

  // Requirement 1: Filter options: "This month", "Previous month", "Custom date range"
  var dateFilterType by remember { mutableStateOf("This month") }
  val filterOptions = listOf("This month", "Previous month", "Custom date range")

  // Custom date range state
  val sdfYmd = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
  val todayStr = remember { sdfYmd.format(Calendar.getInstance().time) }
  val firstDayStr = remember {
    val c = Calendar.getInstance()
    c.set(Calendar.DAY_OF_MONTH, 1)
    sdfYmd.format(c.time)
  }

  var customStartDate by remember { mutableStateOf(firstDayStr) }
  var customEndDate by remember { mutableStateOf(todayStr) }

  // Requirement 9: Validation - start date cannot be after end date
  val dateValidationError = remember(dateFilterType, customStartDate, customEndDate) {
    if (dateFilterType == "Custom date range" && customStartDate.isNotBlank() && customEndDate.isNotBlank()) {
      if (customStartDate.trim() > customEndDate.trim()) {
        "Start date ($customStartDate) cannot be after end date ($customEndDate)."
      } else null
    } else null
  }

  // Employee selection strip
  val sortedMembers = remember(teamMembers, showDepartmentChips) {
    if (!showDepartmentChips) {
      teamMembers.sortedBy { it.employee.employeeId }
    } else {
      teamMembers.sortedBy { it.submissionRate }
    }
  }

  // Requirement 5: Admin/manager filter by employee ID ("All" or specific ID). Normal employee locked.
  var selectedEmployeeIdFilter by remember { mutableStateOf("All") }

  // Firestore state
  var historyEodList by remember { mutableStateOf<List<DailyEodEntity>>(emptyList()) }
  var isLoadingHistory by remember { mutableStateOf(false) }
  var historyError by remember { mutableStateOf<String?>(null) }
  var lastDocSnapshot by remember { mutableStateOf<DocumentSnapshot?>(null) }
  var hasMoreHistory by remember { mutableStateOf(false) }

  // Detail dialog state
  var selectedDayItemForDetail by remember { mutableStateOf<ManagerHistoryDayItem?>(null) }

  // Function to perform one-time fetch from Firestore "eodReports"
  fun loadEodHistoryFromFirestore(reset: Boolean = true) {
    if (dateValidationError != null) return

    scope.launch {
      if (reset) {
        lastDocSnapshot = null
        historyEodList = emptyList()
      }

      isLoadingHistory = true
      historyError = null

      val (startDate, endDate) = calculateDateRangeStrings(dateFilterType, customStartDate, customEndDate)

      val targetEmpId = if (selectedEmployeeIdFilter == "All") null else selectedEmployeeIdFilter

      val res = firebaseDataSource.fetchEodHistoryFromFirestore(
        startDate = startDate,
        endDate = endDate,
        employeeId = targetEmpId,
        limit = 50,
        startAfterDoc = if (reset) null else lastDocSnapshot
      )

      isLoadingHistory = false

      res.onSuccess { queryResult ->
        if (reset) {
          historyEodList = queryResult.items
        } else {
          historyEodList = historyEodList + queryResult.items
        }
        lastDocSnapshot = queryResult.lastDocumentSnapshot
        hasMoreHistory = queryResult.hasMore
      }.onFailure { err ->
        val msg = err.message ?: "Failed to query Firestore eodReports."
        historyError = if (msg.contains("INDEX", ignoreCase = true) || msg.contains("FAILED_PRECONDITION", ignoreCase = true)) {
          "Firestore composite index required for (employeeId ASC, date DESC). Please create index in Firebase Console.\n\n$msg"
        } else {
          msg
        }
      }
    }
  }

  // Trigger query whenever filter options change
  LaunchedEffect(dateFilterType, customStartDate, customEndDate, selectedEmployeeIdFilter) {
    if (dateValidationError == null) {
      loadEodHistoryFromFirestore(reset = true)
    }
  }

  // Combined displayed EOD list (Firestore results fallback to local eods if empty)
  val displayedRecords = remember(historyEodList, eods, dateFilterType, customStartDate, customEndDate, selectedEmployeeIdFilter) {
    val rawList = if (historyEodList.isNotEmpty()) historyEodList else eods
    val (sDate, eDate) = calculateDateRangeStrings(dateFilterType, customStartDate, customEndDate)
    val sOnly = sDate.take(10)
    val eOnly = eDate.take(10)

    rawList.filter { eod ->
      val dateOnly = eod.date.take(10)
      val dateMatches = dateOnly >= sOnly && dateOnly <= eOnly
      val empMatches = selectedEmployeeIdFilter == "All" || eod.employeeId == selectedEmployeeIdFilter
      dateMatches && empMatches
    }.sortedByDescending { it.date }.map { eod ->
      var dayNum = "01"
      var dayName = "Day"
      try {
        val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dObj = sdfInput.parse(eod.date.take(10))
        if (dObj != null) {
          dayNum = SimpleDateFormat("dd", Locale.US).format(dObj)
          dayName = SimpleDateFormat("EEE", Locale.US).format(dObj)
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
              text = "EOD History",
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              letterSpacing = (-0.5).sp
            )

            val mgrInitials = remember(currentManager) {
              val name = currentManager?.name ?: "Admin User"
              name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
            }

            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF1D4ED8))
                .clickable { onAvatarClick() },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = mgrInitials.ifEmpty { "AD" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }

      // 2. Requirement 1: Filter Options Selector ("This month", "Previous month", "Custom date range")
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
            Text("Date Range Filter", fontSize = 13.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.SemiBold)
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
          ) {
            filterOptions.forEach { opt ->
              val isSelected = dateFilterType == opt
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(if (isSelected) Color.White else Color(0xFF191A22))
                  .border(
                    width = 1.dp,
                    color = if (isSelected) Color.Transparent else Color(0xFF2B2D3A),
                    shape = RoundedCornerShape(20.dp)
                  )
                  .clickable { dateFilterType = opt }
                  .padding(horizontal = 16.dp, vertical = 7.dp)
                  .testTag("date_filter_$opt"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = opt,
                  fontSize = 13.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color(0xFF0F0F12) else Color(0xFF9CA3AF)
                )
              }
            }
          }
        }
      }

      // 3. Requirement 1 & 3: Custom Date Range Pickers (Start Date & End Date)
      if (dateFilterType == "Custom date range") {
        item {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF191A22),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B2D3A)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text(
                text = "Select Custom Start & End Date (00:00:00 to 23:59:59)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF60A5FA)
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Start Date Input & Picker
                OutlinedTextField(
                  value = customStartDate,
                  onValueChange = { customStartDate = it },
                  label = { Text("Start Date", color = Color(0xFF9CA3AF), fontSize = 11.sp) },
                  singleLine = true,
                  trailingIcon = {
                    IconButton(onClick = {
                      val c = Calendar.getInstance()
                      val dialog = DatePickerDialog(
                        context,
                        { _, y, m, d ->
                          customStartDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                        },
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)
                      )
                      dialog.show()
                    }) {
                      Icon(Icons.Default.DateRange, contentDescription = "Pick Start Date", tint = Color.White)
                    }
                  },
                  modifier = Modifier.weight(1f),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color(0xFF2B2D3A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  )
                )

                // End Date Input & Picker
                OutlinedTextField(
                  value = customEndDate,
                  onValueChange = { customEndDate = it },
                  label = { Text("End Date", color = Color(0xFF9CA3AF), fontSize = 11.sp) },
                  singleLine = true,
                  trailingIcon = {
                    IconButton(onClick = {
                      val c = Calendar.getInstance()
                      val dialog = DatePickerDialog(
                        context,
                        { _, y, m, d ->
                          customEndDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                        },
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)
                      )
                      dialog.show()
                    }) {
                      Icon(Icons.Default.DateRange, contentDescription = "Pick End Date", tint = Color.White)
                    }
                  },
                  modifier = Modifier.weight(1f),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color(0xFF2B2D3A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  )
                )
              }
            }
          }
        }
      }

      // Requirement 9: Validation Error banner if start date > end date
      dateValidationError?.let { valErr ->
        item {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF450A0A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF991B1B)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF87171))
              Text(text = valErr, fontSize = 12.sp, color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Requirement 5: Employee Selector Strip for Admin / Manager
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Filter by Employee",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Medium
          )

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            item {
              val isAllSel = selectedEmployeeIdFilter == "All"
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(if (isAllSel) Color(0xFF4338CA) else Color(0xFF191A22))
                  .border(1.dp, if (isAllSel) Color(0xFF6366F1) else Color(0xFF2B2D3A), RoundedCornerShape(16.dp))
                  .clickable { selectedEmployeeIdFilter = "All" }
                  .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Text("All Employees", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }

            items(sortedMembers) { memberItem ->
              val emp = memberItem.employee
              val isSel = selectedEmployeeIdFilter == emp.employeeId
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(if (isSel) Color(0xFF4338CA) else Color(0xFF191A22))
                  .border(1.dp, if (isSel) Color(0xFF6366F1) else Color(0xFF2B2D3A), RoundedCornerShape(16.dp))
                  .clickable { selectedEmployeeIdFilter = emp.employeeId }
                  .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Text("${emp.name} (${emp.employeeId})", fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = Color.White)
              }
            }
          }
        }
      }

      // Requirement 8: Loading State
      if (isLoadingHistory && historyEodList.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              CircularProgressIndicator(color = Color(0xFF60A5FA), strokeWidth = 3.dp)
              Text("Fetching EOD reports from eodReports collection...", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            }
          }
        }
      }

      // Requirement 8: Error Message State
      historyError?.let { err ->
        item {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF3F0F16),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF991B1B)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF87171))
                Text("Query Failed", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF87171))
              }
              Text(err, fontSize = 12.sp, color = Color.White)
              OutlinedButton(
                onClick = { loadEodHistoryFromFirestore(reset = true) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retry")
              }
            }
          }
        }
      }

      // Requirement 8: Empty State ("No EOD found for this range")
      if (!isLoadingHistory && historyError == null && displayedRecords.isEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF14151D),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282A38))
          ) {
            Column(
              modifier = Modifier.padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(36.dp))
              Text("No EOD found for this range", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
              Text(
                "Try selecting a different date range or employee filter above.",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
              )
            }
          }
        }
      }

      // Displayed EOD Records
      items(displayedRecords) { record ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedDayItemForDetail = record }
            .padding(vertical = 12.dp)
            .testTag("history_row_${record.dayNumber}"),
          verticalAlignment = Alignment.CenterVertically
        ) {
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

          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = record.title,
              fontSize = 14.sp,
              fontWeight = if (record.status == "Missed") FontWeight.Normal else FontWeight.Medium,
              color = if (record.status == "Missed") Color(0xFF71717A) else Color.White,
              maxLines = 1
            )
            record.rawEod?.let { raw ->
              Text(
                text = "${raw.employeeName} • ${raw.date}",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF)
              )
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          StatusBadgePill(status = record.status)
        }

        HorizontalDivider(
          color = Color(0xFF1A1B24),
          thickness = 0.5.dp
        )
      }

      // Requirement 6: Pagination - "Load more" button using startAfter
      if (hasMoreHistory) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Button(
              onClick = { loadEodHistoryFromFirestore(reset = false) },
              enabled = !isLoadingHistory,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
              shape = RoundedCornerShape(12.dp)
            ) {
              if (isLoadingHistory) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
              }
              Text("Load more", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Inspection Dialog
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
                text = "${dayItem.dayNumber} · ${dayItem.dayName}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = dayItem.rawEod?.employeeName ?: "Employee",
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
    "Present" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
    "Missed" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
    "Leave" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
    "Late" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
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
