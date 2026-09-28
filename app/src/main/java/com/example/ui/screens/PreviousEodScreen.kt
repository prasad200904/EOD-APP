package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyEodEntity
import com.example.data.EmployeeEntity
import com.example.data.SeedData
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WorkBackground
import com.example.ui.theme.WorkOutline
import com.example.ui.theme.WorkOutlineVariant
import com.example.ui.theme.WorkPrimary
import java.util.Calendar
import java.util.Locale

/**
 * Screen that represents previous EOD data fetched by a user-selected date.
 * When the user selects or types a date, this dashboard fetches and displays
 * all EOD submissions for that specific day in a clean, comfortable view.
 */
@Composable
fun PreviousEodScreen(
  eods: List<DailyEodEntity>,
  employees: List<EmployeeEntity>,
  onSelectEmployee: ((EmployeeEntity) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Selected date state (defaults to today's date in seed data)
  var targetDate by remember { mutableStateOf(SeedData.TODAY) }
  var searchQuery by remember { mutableStateOf("") }
  var statusFilter by remember { mutableStateOf("All") }

  val empMap = remember(employees) { employees.associateBy { it.employeeId } }

  // Distinct dates available in database for quick shortcuts
  val availableDates = remember(eods) {
    eods.map { it.date }.distinct().sortedDescending()
  }

  // Fetch all EOD records for the selected date
  val eodsForDate = remember(eods, targetDate) {
    eods.filter { it.date == targetDate.trim() }
  }

  // Filtered by optional search query and work status
  val displayedEods = remember(eodsForDate, searchQuery, statusFilter) {
    eodsForDate.filter { eod ->
      val matchesStatus = statusFilter == "All" || eod.workStatus.equals(statusFilter, ignoreCase = true)
      val matchesQuery = searchQuery.isBlank() ||
        eod.employeeName.contains(searchQuery, ignoreCase = true) ||
        eod.employeeId.contains(searchQuery, ignoreCase = true) ||
        eod.project.contains(searchQuery, ignoreCase = true) ||
        eod.todayWork.contains(searchQuery, ignoreCase = true)
      matchesStatus && matchesQuery
    }.sortedBy { it.employeeName }
  }

  // Quick summary statistics for this date
  val totalSubmissions = eodsForDate.size
  val totalHours = eodsForDate.sumOf { it.hoursWorked }
  val completedCount = eodsForDate.count { it.workStatus.equals("Completed", ignoreCase = true) }
  val inProgressCount = eodsForDate.count { it.workStatus.equals("In Progress", ignoreCase = true) }
  val blockedCount = eodsForDate.count { it.workStatus.equals("Blocked", ignoreCase = true) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(WorkBackground)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // -------------------------------------------------------------------
    // 1. Header Card: Fetch Previous EOD by Date
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
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Previous EOD Data",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Pick a date below to fetch all employee EOD records",
                fontSize = 12.sp,
                color = Color(0xFFEADDFF)
              )
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color.White.copy(alpha = 0.15f)
            ) {
              Text(
                text = targetDate,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }
          }

          HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

          // Date Picker & Input Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = targetDate,
              onValueChange = { targetDate = it },
              label = { Text("Selected Date (YYYY-MM-DD)", color = Color(0xFFEADDFF)) },
              singleLine = true,
              leadingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              },
              trailingIcon = {
                IconButton(onClick = {
                  val c = Calendar.getInstance()
                  val dialog = DatePickerDialog(
                    context,
                    { _, y, m, d ->
                      targetDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
                  )
                  dialog.show()
                }) {
                  Icon(Icons.Default.DateRange, contentDescription = "Open Calendar", tint = Color.White)
                }
              },
              modifier = Modifier
                .weight(1f)
                .testTag("fetch_eod_date_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
              )
            )

            Button(
              onClick = {
                val c = Calendar.getInstance()
                val dialog = DatePickerDialog(
                  context,
                  { _, y, m, d ->
                    targetDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                  },
                  c.get(Calendar.YEAR),
                  c.get(Calendar.MONTH),
                  c.get(Calendar.DAY_OF_MONTH)
                )
                dialog.show()
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF21005D)
              ),
              modifier = Modifier
                .height(52.dp)
                .testTag("fetch_eod_pick_date_btn")
            ) {
              Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Calendar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          // Quick Date Shortcuts
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
          ) {
            item {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (targetDate == SeedData.TODAY) Color.White else Color.White.copy(alpha = 0.15f),
                modifier = Modifier.clickable { targetDate = SeedData.TODAY }
              ) {
                Text(
                  text = "Today (${SeedData.TODAY})",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (targetDate == SeedData.TODAY) Color(0xFF21005D) else Color.White,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
            item {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (targetDate == SeedData.DAY_1) Color.White else Color.White.copy(alpha = 0.15f),
                modifier = Modifier.clickable { targetDate = SeedData.DAY_1 }
              ) {
                Text(
                  text = "Yesterday (${SeedData.DAY_1})",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (targetDate == SeedData.DAY_1) Color(0xFF21005D) else Color.White,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
            item {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (targetDate == SeedData.DAY_2) Color.White else Color.White.copy(alpha = 0.15f),
                modifier = Modifier.clickable { targetDate = SeedData.DAY_2 }
              ) {
                Text(
                  text = SeedData.DAY_2,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (targetDate == SeedData.DAY_2) Color(0xFF21005D) else Color.White,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
            item {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (targetDate == SeedData.DAY_3) Color.White else Color.White.copy(alpha = 0.15f),
                modifier = Modifier.clickable { targetDate = SeedData.DAY_3 }
              ) {
                Text(
                  text = SeedData.DAY_3,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (targetDate == SeedData.DAY_3) Color(0xFF21005D) else Color.White,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }
    }

    // -------------------------------------------------------------------
    // 2. Summary of Fetched Date
    // -------------------------------------------------------------------
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp)),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "DATA FOR $targetDate",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF79747E),
              letterSpacing = 0.5.sp
            )
            Text(
              text = "$totalSubmissions employee submissions",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF21005D)
            )
          }

          // Stat Pills Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFF3E7FF),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Total Hours", fontSize = 10.sp, color = Color(0xFF49454F))
                Text(text = "${String.format(Locale.US, "%.1f", totalHours)}h", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
              }
            }
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFE8F5E9),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Completed", fontSize = 10.sp, color = Color(0xFF1B5E20))
                Text(text = "$completedCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
              }
            }
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFE3F2FD),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "In Progress", fontSize = 10.sp, color = Color(0xFF0D47A1))
                Text(text = "$inProgressCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
              }
            }
            if (blockedCount > 0) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFEBEE),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "Blocked", fontSize = 10.sp, color = Color(0xFFB71C1C))
                  Text(text = "$blockedCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                }
              }
            }
          }
        }
      }
    }

    // -------------------------------------------------------------------
    // 3. Search & Status Filter Controls
    // -------------------------------------------------------------------
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search by employee name or project...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WorkPrimary) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("previous_eod_search_field"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WorkPrimary,
            unfocusedBorderColor = WorkOutlineVariant
          )
        )

        // Status Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          val chips = listOf("All", "Completed", "In Progress", "Blocked", "No Work", "On Leave")
          items(chips) { chip ->
            FilterChip(
              selected = statusFilter == chip,
              onClick = { statusFilter = chip },
              label = { Text(chip, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
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
    // 4. EOD Submissions List
    // -------------------------------------------------------------------
    if (displayedEods.isEmpty()) {
      item {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF79747E), modifier = Modifier.size(36.dp))
            Text(
              text = "No EOD Data Found for $targetDate",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1D1B20)
            )
            Text(
              text = "Try picking another date like ${SeedData.TODAY} or ${SeedData.DAY_1} using the date selector above.",
              fontSize = 12.sp,
              color = Color(0xFF79747E),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            OutlinedButton(
              onClick = { targetDate = SeedData.TODAY },
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Switch to Today (${SeedData.TODAY})")
            }
          }
        }
      }
    } else {
      items(displayedEods) { eod ->
        val emp = empMap[eod.employeeId]
        PreviousEodRecordCard(
          eod = eod,
          employee = emp,
          onCardClick = { if (emp != null) onSelectEmployee?.invoke(emp) }
        )
      }
    }
  }
}

/**
 * Comfortable card displaying an individual employee's EOD submission.
 */
@Composable
private fun PreviousEodRecordCard(
  eod: DailyEodEntity,
  employee: EmployeeEntity?,
  onCardClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val statusColor = when (eod.workStatus.lowercase()) {
    "completed" -> Color(0xFF1B5E20)
    "in progress" -> Color(0xFF0D47A1)
    "blocked" -> Color(0xFFB71C1C)
    "on leave" -> Color(0xFFE65100)
    else -> Color(0xFF49454F)
  }

  val statusBg = when (eod.workStatus.lowercase()) {
    "completed" -> Color(0xFFE8F5E9)
    "in progress" -> Color(0xFFE3F2FD)
    "blocked" -> Color(0xFFFFEBEE)
    "on leave" -> Color(0xFFFFF3E0)
    else -> Color(0xFFF5F5F5)
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onCardClick() },
    color = Color.White,
    border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Top row: Avatar + Name + ID + Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          UserAvatar(name = eod.employeeName, size = 36, backgroundColor = WorkPrimary)
          Column {
            Text(
              text = eod.employeeName,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1D1B20)
            )
            Text(
              text = "${eod.employeeId} • ${employee?.team ?: "Team"}",
              fontSize = 11.sp,
              color = Color(0xFF79747E)
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = statusBg
        ) {
          Text(
            text = eod.workStatus.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      HorizontalDivider(color = WorkOutlineVariant)

      // Project & Hours row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "PROJECT:",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF79747E)
          )
          Text(
            text = eod.project,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF21005D)
          )
        }

        Text(
          text = "${eod.hoursWorked} hrs • ${eod.progressPercentage}% progress",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF49454F)
        )
      }

      // Today's Work text
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "TASKS & ACHIEVEMENTS:",
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF79747E)
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFF7F2FA),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = eod.todayWork,
            fontSize = 12.sp,
            color = Color(0xFF1D1B20),
            lineHeight = 17.sp,
            modifier = Modifier.padding(10.dp)
          )
        }
      }

      // Blockers or Remarks if present
      if (eod.blockers.isNotBlank() && !eod.blockers.equals("None", ignoreCase = true)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB71C1C), modifier = Modifier.size(14.dp))
          Text(
            text = "Blocker: ${eod.blockers}",
            fontSize = 11.sp,
            color = Color(0xFFB71C1C),
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}
