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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyEodEntity
import com.example.data.EmployeeBehaviorMetrics
import com.example.data.EmployeeEntity
import com.example.data.TeamAnalyticsSummary
import com.example.ui.components.IndicatorBadge
import com.example.ui.components.MetricCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WorkBackground
import com.example.ui.theme.WorkOutline
import com.example.ui.theme.WorkOutlineVariant
import com.example.ui.theme.WorkPrimary
import com.example.ui.theme.WorkPrimaryContainer
import com.example.ui.theme.WorkSurface

@Composable
fun AnalyticsScreen(
  behaviorMetrics: List<EmployeeBehaviorMetrics>,
  teamSummaries: List<TeamAnalyticsSummary>,
  departmentFilter: String,
  onDepartmentFilterChange: (String) -> Unit,
  onSelectEmployee: (EmployeeEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0 = Employee Patterns & Insights, 1 = Team Performance

  val departments = listOf("All", "Engineering", "Design", "Quality Assurance", "Product")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(WorkBackground)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Title & Subtitle
    Column {
      Text(
        text = "Work Activity & Performance Analytics",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF21005D)
      )
      Text(
        text = "Objective project metrics and completion rates derived directly from daily EOD logs",
        fontSize = 12.sp,
        color = Color(0xFF49454F)
      )
    }

    // Analytics Sub-Tabs: Employee Behavior vs Team Performance
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = Color.White,
      contentColor = WorkPrimary,
      modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .border(1.dp, WorkOutlineVariant, RoundedCornerShape(12.dp))
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
            Text("Employee Patterns", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
            Text("Team Analytics", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      )
    }

    // Department Filter Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      departments.forEach { dept ->
        val selected = departmentFilter == dept
        FilterChip(
          selected = selected,
          onClick = { onDepartmentFilterChange(dept) },
          label = { Text(dept, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFEADDFF),
            selectedLabelColor = Color(0xFF21005D),
            containerColor = Color.White
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) WorkPrimary else WorkOutlineVariant
          )
        )
      }
    }

    // Tab 0: Employee Behavior Patterns & Insight Engine
    if (selectedTab == 0) {
      val filteredMetrics = behaviorMetrics.filter {
        departmentFilter == "All" || it.employee.department.equals(departmentFilter, ignoreCase = true)
      }

      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(filteredMetrics, key = { it.employee.employeeId }) { m ->
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .border(1.dp, WorkOutlineVariant, RoundedCornerShape(16.dp))
              .clickable { onSelectEmployee(m.employee) },
            color = Color.White
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              // Header: Avatar, Name, Dept, and Attention tag if any
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  UserAvatar(name = m.employee.name, size = 38)
                  Column {
                    Text(m.employee.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                    Text("${m.employee.designation} · ${m.employee.department}", fontSize = 11.sp, color = Color(0xFF49454F))
                  }
                }

                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF79747E))
              }

              // Section 6: Measurable Indicators Row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("EOD CONSISTENCY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF79747E))
                  Spacer(modifier = Modifier.height(2.dp))
                  IndicatorBadge(indicator = m.eodConsistencyStatus)
                }

                Column {
                  Text("WORK CONSISTENCY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF79747E))
                  Spacer(modifier = Modifier.height(2.dp))
                  IndicatorBadge(indicator = m.workConsistencyStatus)
                }

                Column {
                  Text("PROGRESS PATTERN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF79747E))
                  Spacer(modifier = Modifier.height(2.dp))
                  IndicatorBadge(indicator = m.progressConsistencyStatus)
                }
              }

              HorizontalDivider(color = WorkOutlineVariant)

              // Metrics Row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text("Submission Rate", fontSize = 10.sp, color = Color(0xFF79747E))
                  Text("${m.submissionRate}% (${m.submittedEods}/${m.totalExpectedEods})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                }
                Column {
                  Text("Daily Avg Hours", fontSize = 10.sp, color = Color(0xFF79747E))
                  Text("${m.avgHours}h/day", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                }
                Column {
                  Text("Avg Progress", fontSize = 10.sp, color = Color(0xFF79747E))
                  Text("${m.avgProgress}% (${m.progressTrend})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                }
                Column {
                  Text("Blocked Days", fontSize = 10.sp, color = Color(0xFF79747E))
                  Text("${m.blockedDaysCount} days", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (m.blockedDaysCount >= 2) Color(0xFFB3261E) else Color(0xFF21005D))
                }
              }

              // Objective rule-based explanation snippet
              if (m.objectiveInsights.isNotEmpty()) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF7F2FA))
                    .padding(8.dp)
                ) {
                  Text(
                    text = "“${m.objectiveInsights.first()}”",
                    fontSize = 11.sp,
                    color = Color(0xFF49454F),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }
      }
    } else {
      // Tab 1: Section 10 Team Performance Analytics
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(teamSummaries) { team ->
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .border(1.dp, WorkOutlineVariant, RoundedCornerShape(16.dp)),
            color = Color.White
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              // Team Title
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = team.teamName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21005D)
                  )
                  Text(
                    text = "${team.department} · ${team.totalMembers} Members",
                    fontSize = 11.sp,
                    color = Color(0xFF49454F)
                  )
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEADDFF))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = "${team.teamSubmissionRate}% Today",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21005D)
                  )
                }
              }

              // Metrics 2x2
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                MetricCard(
                  title = "Avg Hours Logged",
                  count = "${team.avgWorkingHours}h",
                  subtitle = "Per member average",
                  backgroundColor = Color(0xFFF3E7FF),
                  textColor = Color(0xFF21005D),
                  borderColor = WorkOutlineVariant,
                  modifier = Modifier.weight(1f)
                )
                MetricCard(
                  title = "Avg Progress",
                  count = "${team.avgProgress}%",
                  subtitle = "${team.completedCount} completed items",
                  backgroundColor = Color(0xFFE8F5E9),
                  textColor = Color(0xFF1B5E20),
                  borderColor = WorkOutlineVariant,
                  modifier = Modifier.weight(1f)
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                MetricCard(
                  title = "On-Time Rate",
                  count = "${team.teamOnTimeRate}%",
                  subtitle = "Cutoff 18:30 standard",
                  backgroundColor = Color(0xFFE3F2FD),
                  textColor = Color(0xFF0D47A1),
                  borderColor = WorkOutlineVariant,
                  modifier = Modifier.weight(1f)
                )
                MetricCard(
                  title = "Blocked Days",
                  count = "${team.totalBlockedDays}",
                  subtitle = "Cumulative blockers",
                  backgroundColor = if (team.totalBlockedDays >= 3) Color(0xFFFFEBEE) else Color(0xFFF5F5F5),
                  textColor = if (team.totalBlockedDays >= 3) Color(0xFFB71C1C) else Color(0xFF49454F),
                  borderColor = WorkOutlineVariant,
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }
      }
    }
  }
}
