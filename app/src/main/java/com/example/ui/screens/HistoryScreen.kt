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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.window.Dialog
import com.example.data.DailyEodEntity
import com.example.data.EmployeeEntity
import com.example.ui.theme.EodAvatarBg
import com.example.ui.theme.EodAvatarText
import com.example.ui.theme.EodDarkBackground
import com.example.ui.theme.EodDarkCardBorder
import com.example.ui.theme.EodDarkSurface
import com.example.ui.theme.EodDarkSurfaceElevated
import com.example.ui.theme.EodMissedPillBg
import com.example.ui.theme.EodMissedPillText
import com.example.ui.theme.EodTextMuted
import com.example.ui.theme.EodTextPrimary
import com.example.ui.theme.EodTextSecondary

data class HistoryRowItem(
  val dayNumber: String,
  val dayName: String,
  val title: String,
  val subtitle: String,
  val isMissed: Boolean = false,
  val rawEod: DailyEodEntity? = null
)

/**
 * Screen 2: "History"
 * Exact dark obsidian UI representation from user screenshot 2:
 * - Header: "Your history" + RK Avatar
 * - Month selector: Jun | Jul | Aug | [Sep] (active white pill)
 * - Metric cards side-by-side: "Submitted 11 / 12", "Total hours 87.5"
 * - History rows: Day column (15 Tue), Title (Churn v2 evaluation, 3 tasks), Hours & status (8.5h · Present), chevron
 * - Missed entry row: 11 Fri | No entry | [Missed]
 */
@Composable
fun HistoryScreen(
  currentEmployee: EmployeeEntity?,
  eods: List<DailyEodEntity>,
  onAvatarClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedMonth by remember { mutableStateOf("Sep") }
  val months = listOf("Jun", "Jul", "Aug", "Sep")
  var selectedHistoryEod by remember { mutableStateOf<HistoryRowItem?>(null) }

  val historyItems = remember(selectedMonth, eods) {
    listOf(
      HistoryRowItem(
        dayNumber = "15",
        dayName = "Tue",
        title = "Churn v2 evaluation, 3 tasks",
        subtitle = "Present · On-time",
        rawEod = eods.find { it.date.endsWith("-15") }
      ),
      HistoryRowItem(
        dayNumber = "14",
        dayName = "Mon",
        title = "Feature store migration",
        subtitle = "Present · On-time",
        rawEod = eods.find { it.date.endsWith("-14") }
      ),
      HistoryRowItem(
        dayNumber = "11",
        dayName = "Fri",
        title = "No entry",
        subtitle = "",
        isMissed = true,
        rawEod = null
      ),
      HistoryRowItem(
        dayNumber = "10",
        dayName = "Thu",
        title = "Data pipeline fixes",
        subtitle = "Present · On-time",
        rawEod = eods.find { it.date.endsWith("-10") }
      ),
      HistoryRowItem(
        dayNumber = "09",
        dayName = "Wed",
        title = "Auth token validation & tests",
        subtitle = "Present · On-time",
        rawEod = eods.find { it.date.endsWith("-09") }
      ),
      HistoryRowItem(
        dayNumber = "08",
        dayName = "Tue",
        title = "API schema updates & review",
        subtitle = "Present · On-time",
        rawEod = eods.find { it.date.endsWith("-08") }
      )
    )
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
      // 1. Header Row
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Your history",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = EodTextPrimary
          )

          val initials = remember(currentEmployee) {
            val name = currentEmployee?.name ?: "Ravi Kumar"
            name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
          }
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(EodAvatarBg)
              .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = initials.ifEmpty { "RK" },
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = EodAvatarText
            )
          }
        }
      }

      // 2. Month Selector Pills (Jun | Jul | Aug | [Sep])
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          months.forEach { m ->
            val isSelected = selectedMonth == m
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) Color.White else EodDarkSurface)
                .border(1.dp, if (isSelected) Color.White else EodDarkCardBorder, RoundedCornerShape(20.dp))
                .clickable { selectedMonth = m }
                .padding(horizontal = 18.dp, vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = m,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF191A20) else EodTextSecondary
              )
            }
          }
        }
      }

      // 3. Metric Cards Side-by-Side ("Submitted 11 / 12" and "Total hours 87.5")
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Submitted Card
          Surface(
            modifier = Modifier.weight(1f),
            color = EodDarkSurface,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EodDarkCardBorder)
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "Submitted",
                fontSize = 13.sp,
                color = EodTextSecondary
              )

              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                  text = "11",
                  fontSize = 26.sp,
                  fontWeight = FontWeight.Bold,
                  color = EodTextPrimary
                )
                Text(
                  text = " / 12",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = EodTextSecondary,
                  modifier = Modifier.padding(bottom = 2.dp)
                )
              }
            }
          }

          // Current Streak Card
          Surface(
            modifier = Modifier.weight(1f),
            color = EodDarkSurface,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EodDarkCardBorder)
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "Current streak",
                fontSize = 13.sp,
                color = EodTextSecondary
              )

              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                  text = "5",
                  fontSize = 26.sp,
                  fontWeight = FontWeight.Bold,
                  color = EodTextPrimary
                )
                Text(
                  text = " days",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF60A5FA),
                  modifier = Modifier.padding(bottom = 2.dp)
                )
              }
            }
          }
        }
      }

      // 4. History List
      item {
        Spacer(modifier = Modifier.height(6.dp))
      }

      items(historyItems) { item ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedHistoryEod = item }
            .padding(vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Day column (15 Tue)
          Column(
            modifier = Modifier.width(42.dp),
            horizontalAlignment = Alignment.Start
          ) {
            Text(
              text = item.dayNumber,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = EodTextPrimary
            )
            Text(
              text = item.dayName,
              fontSize = 12.sp,
              color = EodTextSecondary
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          // Middle Column: Title & Subtitle
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Text(
              text = item.title,
              fontSize = 14.sp,
              fontWeight = if (item.isMissed) FontWeight.Normal else FontWeight.SemiBold,
              color = if (item.isMissed) EodTextSecondary else EodTextPrimary
            )

            if (item.subtitle.isNotEmpty()) {
              Text(
                text = item.subtitle,
                fontSize = 13.sp,
                color = EodTextSecondary
              )
            }
          }

          // Right: Missed Pill or Chevron
          if (item.isMissed) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(EodMissedPillBg)
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = "Missed",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = EodMissedPillText
              )
            }
          } else {
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = "View detail",
              tint = EodTextMuted,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        HorizontalDivider(
          color = Color(0xFF1E1F26),
          thickness = 0.5.dp
        )
      }
    }
  }

  // Detail Dialog for History Entry
  selectedHistoryEod?.let { item ->
    Dialog(onDismissRequest = { selectedHistoryEod = null }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = EodDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, EodDarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${item.dayName}, ${item.dayNumber} $selectedMonth 2026",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = EodTextPrimary
            )
            if (item.isMissed) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(EodMissedPillBg)
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text("Missed", color = EodMissedPillText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Text(
            text = item.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = EodTextPrimary
          )

          if (item.subtitle.isNotEmpty()) {
            Text(
              text = "Log: ${item.subtitle}",
              fontSize = 13.sp,
              color = EodTextSecondary
            )
          }

          item.rawEod?.let { eod ->
            Spacer(modifier = Modifier.height(4.dp))
            Text("Project: ${eod.project}", fontSize = 13.sp, color = Color(0xFF60A5FA))
            Text("Hours Worked: ${eod.hoursWorked}h", fontSize = 13.sp, color = EodTextSecondary)
            Text("Progress: ${eod.progressPercentage}%", fontSize = 13.sp, color = EodTextSecondary)
            Text("Work Summary: ${eod.todayWork}", fontSize = 13.sp, color = EodTextPrimary)
          }

          Spacer(modifier = Modifier.height(8.dp))

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedHistoryEod = null }
              .padding(vertical = 10.dp),
            contentColor = Color.Black
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text("Close", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
          }
        }
      }
    }
  }
}
