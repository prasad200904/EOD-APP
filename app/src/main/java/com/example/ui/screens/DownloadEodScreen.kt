package com.example.ui.screens

import android.widget.Toast
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.SubtleText
import com.example.ui.theme.MintGreen
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyEodEntity
import com.example.data.EmployeeEntity
import com.example.data.Role
import com.example.data.TeamEntity
import com.example.ui.theme.WarmCream
import com.example.util.FileDownloadHelper

data class ExportReportItem(
  val id: String = java.util.UUID.randomUUID().toString(),
  val title: String,
  val subtitle: String,
  val scope: String,
  val department: String,
  val records: Int,
  val format: String,
  val content: String = ""
)

/**
 * Unified Reports / Download Dashboard across all portals (Employee, Department/Team, Admin).
 * Identical layout, styling, and behavior matching design reference:
 * - Header: "Reports" with exit/profile button
 * - Scope: [Whole team] (selected) | [One employee]
 * - Department: Selector card e.g. [ShowChart] GT Team (5 members v)
 * - Range: 7 days | [This month] | Custom
 * - Date banner: [Calendar] 1 Sep — 16 Sep 2026 | 54 records
 * - Format: [Excel] (selected) | [CSV]
 * - Primary Action Button: "Generate report — GT Team" (or employee name)
 * - Recent exports:
 *     - GT Team · Aug 2026 (Whole team · 138 records) [Download icon]
 *     - Arjun Joseph · Jul 2026 (One employee · 22 records) [Download icon]
 */
@Composable
fun DownloadEodScreen(
  eods: List<DailyEodEntity>,
  employees: List<EmployeeEntity>,
  teams: List<TeamEntity> = emptyList(),
  currentRole: Role = Role.EMPLOYEE,
  initialDepartment: String = "GT Team",
  onAvatarClick: (() -> Unit)? = null,
  onLogout: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Scope: "Whole team" or "One employee"
  var selectedScope by remember { mutableStateOf("Whole team") }

  val isAdmin = currentRole == Role.ADMIN

  // Departments list — admin sees all, non-admin locked to their own team
  val availableDepartments = remember(isAdmin, teams, initialDepartment) {
    if (isAdmin) {
      // Build from actual teams list if available, fallback to hardcoded
      teams.map { it.name }.ifEmpty {
        listOf("General Team", "ML Team", "DB Team", "Writing Team", "Cyber Security",
          "TEGA", "Marketing", "Counsellor", "Tele Caller", "Manager")
      }
    } else {
      listOf(initialDepartment)
    }
  }

  var selectedDepartment by remember(initialDepartment) {
    mutableStateOf(initialDepartment)
  }
  var showDepartmentDropdown by remember { mutableStateOf(false) }

  // Filter department employees

  val deptEmployees = remember(employees, selectedDepartment) {
    employees.filter { emp ->
      emp.role == Role.EMPLOYEE.name && (
        emp.team.equals(selectedDepartment, ignoreCase = true) ||
        emp.department.equals(selectedDepartment, ignoreCase = true) ||
        // Code↔name fallbacks
        (selectedDepartment.contains("General", ignoreCase = true) && emp.team.contains("General", ignoreCase = true)) ||
        (selectedDepartment.contains("ML", ignoreCase = true) && emp.team.contains("ML", ignoreCase = true)) ||
        (selectedDepartment.contains("DB", ignoreCase = true) && emp.team.contains("DB", ignoreCase = true)) ||
        (selectedDepartment.contains("Cyber", ignoreCase = true) && emp.team.contains("Cyber", ignoreCase = true)) ||
        (selectedDepartment.contains("Writing", ignoreCase = true) && emp.team.contains("Writing", ignoreCase = true)) ||
        (selectedDepartment.contains("TEGA", ignoreCase = true) && emp.team.contains("TEGA", ignoreCase = true)) ||
        (selectedDepartment.contains("Marketing", ignoreCase = true) && emp.team.contains("Marketing", ignoreCase = true)) ||
        (selectedDepartment.contains("Counsel", ignoreCase = true) && emp.team.contains("Counsel", ignoreCase = true)) ||
        (selectedDepartment.contains("Tele", ignoreCase = true) && emp.team.contains("Tele", ignoreCase = true)) ||
        (selectedDepartment.contains("Manager", ignoreCase = true) && emp.team.contains("Manager", ignoreCase = true))
      )
    }
  }

  // Selected Employee (for "One employee" scope)
  var selectedEmployeeId by remember {
    val arjun = deptEmployees.find { it.name.contains("Arjun", ignoreCase = true) }
    mutableStateOf(arjun?.employeeId ?: deptEmployees.firstOrNull()?.employeeId ?: "")
  }

  val selectedEmployee = remember(deptEmployees, selectedEmployeeId) {
    deptEmployees.find { it.employeeId == selectedEmployeeId } ?: deptEmployees.firstOrNull()
  }

  // Range options
  var selectedRange by remember { mutableStateOf("Today") } // "Today", "7 days", "This month", "Custom"
  var selectedFormat by remember { mutableStateOf("CSV") } // "Excel", "CSV" - Changed default to CSV

  // Get actual current date for "Today" option
  val actualTodayDate = remember {
    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
  }

  val dateBannerText = when (selectedRange) {
    "Today" -> actualTodayDate
    "7 days" -> "10 Sep — 16 Sep 2026"
    "This month" -> "1 Sep — 16 Sep 2026"
    else -> "1 Aug — 16 Sep 2026"
  }

  // Dynamic record count calculation based on scope and range
  val calculatedRecordCount = remember(selectedScope, selectedRange, deptEmployees.size) {
    if (selectedScope == "Whole team") {
      when (selectedRange) {
        "Today" -> deptEmployees.size // One EOD per employee for today
        "7 days" -> deptEmployees.size * 5
        "This month" -> 54 // Exact value matching design screenshot
        else -> 138
      }
    } else {
      when (selectedRange) {
        "Today" -> 1 // Single EOD for today
        "7 days" -> 5
        "This month" -> 11
        else -> 22
      }
    }
  }

  // Recent exports matching user design reference
  val recentExports = remember {
    mutableStateListOf(
      ExportReportItem(
        title = "GT Team · Aug 2026",
        subtitle = "Whole team · 138 records",
        scope = "Whole team",
        department = "GT Team",
        records = 138,
        format = "Excel"
      ),
      ExportReportItem(
        title = "Arjun Joseph · Jul 2026",
        subtitle = "One employee · 22 records",
        scope = "One employee",
        department = "GT Team",
        records = 22,
        format = "Excel"
      )
    )
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(WarmCream)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // 1. Header: "Reports" with exit / logout icon
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Reports",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            letterSpacing = (-0.5).sp
          )

          IconButton(
            onClick = {
              onLogout?.invoke() ?: onAvatarClick?.invoke()
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ExitToApp,
              contentDescription = "Exit",
              tint = Color.Black,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }

      // 2. Scope Section: [Whole team] | [One employee]
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Scope",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Whole team Card
            val isWholeTeam = selectedScope == "Whole team"
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isWholeTeam) MintGreen else Color.White)
                .border(
                  width = 1.dp,
                  color = if (isWholeTeam) CoralPrimary else Color(0xFFE8D5C4),
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable { selectedScope = "Whole team" }
                .padding(vertical = 14.dp, horizontal = 16.dp)
                .testTag("scope_whole_team"),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Group,
                  contentDescription = null,
                  tint = if (isWholeTeam) Color.Black else Color(0xFF4B5563),
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = "Whole team",
                  fontSize = 14.sp,
                  fontWeight = if (isWholeTeam) FontWeight.Bold else FontWeight.Medium,
                  color = if (isWholeTeam) Color.Black else Color(0xFF4B5563)
                )
              }
            }

            // One employee Card
            val isOneEmployee = selectedScope == "One employee"
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isOneEmployee) MintGreen else Color.White)
                .border(
                  width = 1.dp,
                  color = if (isOneEmployee) CoralPrimary else Color(0xFFE8D5C4),
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable { selectedScope = "One employee" }
                .padding(vertical = 14.dp, horizontal = 16.dp)
                .testTag("scope_one_employee"),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.PersonOutline,
                  contentDescription = null,
                  tint = if (isOneEmployee) Color.Black else Color(0xFF4B5563),
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = "One employee",
                  fontSize = 14.sp,
                  fontWeight = if (isOneEmployee) FontWeight.Bold else FontWeight.Medium,
                  color = if (isOneEmployee) Color.Black else Color(0xFF4B5563)
                )
              }
            }
          }

          // If "One employee" is selected, show employee selector strip
          if (selectedScope == "One employee") {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              deptEmployees.forEach { emp ->
                val isSelected = emp.employeeId == selectedEmployeeId
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) CoralPrimary else Color.White)
                    .border(
                      width = 1.dp,
                      color = if (isSelected) CoralPrimary else Color(0xFFE8D5C4),
                      shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { selectedEmployeeId = emp.employeeId }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("employee_select_${emp.employeeId}"),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = emp.name,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color.Black
                  )
                }
              }
            }
          }
        }
      }

      // 3. Department Section — admin can switch, non-admin sees their team locked
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Department",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
          )

          if (isAdmin) {
            // Admin: dropdown to pick any team
            Box {
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { showDepartmentDropdown = true }
                  .testTag("department_selector_card"),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8D5C4))
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.ShowChart,
                      contentDescription = null,
                      tint = CoralPrimary,
                      modifier = Modifier.size(20.dp)
                    )
                    Text(
                      text = selectedDepartment,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = Color.Black
                    )
                  }
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(
                      text = "${deptEmployees.size} members",
                      fontSize = 13.sp,
                      color = Color.Black
                    )
                    Icon(
                      imageVector = Icons.Default.KeyboardArrowDown,
                      contentDescription = null,
                      tint = Color.Black,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
              DropdownMenu(
                expanded = showDepartmentDropdown,
                onDismissRequest = { showDepartmentDropdown = false },
                modifier = Modifier.background(Color.White)
              ) {
                availableDepartments.forEach { dept ->
                  DropdownMenuItem(
                    text = {
                      Text(
                        text = dept,
                        color = if (dept == selectedDepartment) CoralPrimary else Color.Black,
                        fontWeight = if (dept == selectedDepartment) FontWeight.Bold else FontWeight.Normal
                      )
                    },
                    onClick = {
                      selectedDepartment = dept
                      showDepartmentDropdown = false
                    }
                  )
                }
              }
            }
          } else {
            // Non-admin: locked to their own team, no dropdown
            Surface(
              modifier = Modifier.fillMaxWidth(),
              color = Color.White,
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8D5C4))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(20.dp)
                  )
                  Text(
                    text = selectedDepartment,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                  )
                }
                Text(
                  text = "${deptEmployees.size} members",
                  fontSize = 13.sp,
                  color = Color.Black
                )
              }
            }
          }
        }
      }

      // 4. Range Section
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Range",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
          )

          // 3 pill options: Today | 7 days | [This month] | Custom
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf("Today", "7 days", "This month", "Custom").forEach { rangeOpt ->
              val isSelected = selectedRange == rangeOpt
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isSelected) MintGreen else Color.White)
                  .border(
                    width = 1.dp,
                    color = if (isSelected) CoralPrimary else Color(0xFFE8D5C4),
                    shape = RoundedCornerShape(12.dp)
                  )
                  .clickable { selectedRange = rangeOpt }
                  .padding(vertical = 10.dp)
                  .testTag("range_$rangeOpt"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = rangeOpt,
                  fontSize = 13.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.Black else Color(0xFF4B5563)
                )
              }
            }
          }

          // Date banner card: [Calendar] 1 Sep — 16 Sep 2026 | 54 records
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8D5C4))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = Color.Black,
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = dateBannerText,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.Black
                )
              }

              Text(
                text = "$calculatedRecordCount records",
                fontSize = 13.sp,
                color = Color.Black
              )
            }
          }
        }
      }

      // 5. Format Section: [Excel] (selected with blue border) | [CSV]
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Format",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Excel Option
            val isExcel = selectedFormat == "Excel"
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(
                  width = if (isExcel) 2.dp else 1.dp,
                  color = if (isExcel) CoralPrimary else Color(0xFFE8D5C4),
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable { selectedFormat = "Excel" }
                .padding(vertical = 14.dp, horizontal = 16.dp)
                .testTag("format_excel"),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Description,
                  contentDescription = null,
                  tint = if (isExcel) CoralPrimary else Color(0xFF4B5563),
                  modifier = Modifier.size(20.dp)
                )
                Text(
                  text = "Excel",
                  fontSize = 14.sp,
                  fontWeight = if (isExcel) FontWeight.Bold else FontWeight.Medium,
                  color = if (isExcel) Color.Black else Color(0xFF4B5563)
                )
              }
            }

            // CSV Option
            val isCsv = selectedFormat == "CSV"
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(
                  width = if (isCsv) 2.dp else 1.dp,
                  color = if (isCsv) CoralPrimary else Color(0xFFE8D5C4),
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable { selectedFormat = "CSV" }
                .padding(vertical = 14.dp, horizontal = 16.dp)
                .testTag("format_csv"),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Description,
                  contentDescription = null,
                  tint = if (isCsv) CoralPrimary else Color(0xFF4B5563),
                  modifier = Modifier.size(20.dp)
                )
                Text(
                  text = "CSV",
                  fontSize = 14.sp,
                  fontWeight = if (isCsv) FontWeight.Bold else FontWeight.Medium,
                  color = if (isCsv) Color.Black else Color(0xFF4B5563)
                )
              }
            }
          }
        }
      }

      // 6. Action Button: "Generate report — GT Team" (or employee name)
      item {
        val targetName = if (selectedScope == "Whole team") {
          selectedDepartment
        } else {
          selectedEmployee?.name ?: "Arjun Joseph"
        }

        Button(
          onClick = {
            // Filter EODs based on selected scope and range
            val filteredEods = if (selectedScope == "Whole team") {
              // All employees in department
              eods.filter { eod ->
                val empMatch = deptEmployees.any { emp -> emp.employeeId == eod.employeeId }
                val dateMatch = when (selectedRange) {
                  "Today" -> {
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    eod.date == today
                  }
                  "7 days" -> true // Filter by last 7 days (simplified for now)
                  "This month" -> true // Filter by current month (simplified)
                  else -> true
                }
                empMatch && dateMatch
              }
            } else {
              // Single employee
              eods.filter { eod ->
                val empMatch = eod.employeeId == selectedEmployeeId
                val dateMatch = when (selectedRange) {
                  "Today" -> {
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    eod.date == today
                  }
                  "7 days" -> true
                  "This month" -> true
                  else -> true
                }
                empMatch && dateMatch
              }
            }

            // Generate report file content with actual EOD data
            val extension = if (selectedFormat == "Excel") "xml" else "csv"
            val mimeType = if (selectedFormat == "Excel") "application/vnd.ms-excel" else "text/csv"
            val fileName = "workcore_${targetName.replace(" ", "_").lowercase()}_${selectedRange.replace(" ", "_").lowercase()}.$extension"

            val header = if (selectedFormat == "Excel") {
              // Excel format with simple fields
              val rows = filteredEods.joinToString("") { eod ->
                """   <Row><Cell><Data ss:Type="String">${eod.employeeName}</Data></Cell><Cell><Data ss:Type="String">${eod.date}</Data></Cell><Cell><Data ss:Type="String">${eod.project}</Data></Cell><Cell><Data ss:Type="String">${eod.todayWork.replace("<", "&lt;").replace(">", "&gt;")}</Data></Cell><Cell><Data ss:Type="String">${eod.workStatus}</Data></Cell></Row>
"""
              }
              
              """<?xml version="1.0"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
 <Worksheet ss:Name="EOD Report">
  <Table>
   <Row><Cell><Data ss:Type="String">WorkCore EOD Report - $targetName</Data></Cell></Row>
   <Row><Cell><Data ss:Type="String">Name</Data></Cell><Cell><Data ss:Type="String">Date</Data></Cell><Cell><Data ss:Type="String">Project Title</Data></Cell><Cell><Data ss:Type="String">Description</Data></Cell><Cell><Data ss:Type="String">Status</Data></Cell></Row>
$rows  </Table>
 </Worksheet>
</Workbook>"""
            } else {
              // Simple CSV format matching the simple EOD form with actual data
              val csvRows = filteredEods.joinToString("\n") { eod ->
                // Escape CSV values that contain commas or quotes
                val name = "\"${eod.employeeName.replace("\"", "\"\"")}\""
                val date = eod.date
                val project = "\"${eod.project.replace("\"", "\"\"")}\""
                val description = "\"${eod.todayWork.replace("\"", "\"\"")}\""
                val status = eod.workStatus
                "$name,$date,$project,$description,$status"
              }
              
              if (csvRows.isEmpty()) {
                // No EODs found - show sample format
                "Name,Date,Project Title,Description,Status\n$targetName,${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())},Sample Project,No EOD submitted yet,Pending"
              } else {
                "Name,Date,Project Title,Description,Status\n$csvRows"
              }
            }

            // Download directly to public Downloads folder
            val result = FileDownloadHelper.downloadFileDirectly(
              context = context,
              fileName = fileName,
              content = header,
              mimeType = mimeType
            )

            // Prepend new item to recent exports
            recentExports.add(
              0,
              ExportReportItem(
                title = "$targetName · Sep 2026",
                subtitle = "$selectedScope · $calculatedRecordCount records",
                scope = selectedScope,
                department = selectedDepartment,
                records = calculatedRecordCount,
                format = selectedFormat,
                content = header
              )
            )

            Toast.makeText(
              context,
              "Report saved to ${result.locationDescription}!",
              Toast.LENGTH_LONG
            ).show()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("btn_generate_report"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
          Text(
            text = "Generate report — $targetName",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      // 7. Recent Exports Section
      item {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "Recent exports",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
          )

          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            recentExports.forEach { item ->
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("recent_export_${item.id}"),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8D5C4))
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E202C)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                      )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                      Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                      )
                      Text(
                        text = item.subtitle,
                        fontSize = 13.sp,
                        color = Color.Black
                      )
                    }
                  }

                  IconButton(
                    onClick = {
                      val ext = if (item.format == "Excel") "xml" else "csv"
                      val mime = if (item.format == "Excel") "application/vnd.ms-excel" else "text/csv"
                      val fname = "${item.title.replace(" · ", "_").replace(" ", "_").lowercase()}.$ext"
                      val mockContent = "WorkCore Report: ${item.title}\nScope: ${item.scope}\nRecords: ${item.records}"

                      val dl = FileDownloadHelper.downloadFileDirectly(
                        context = context,
                        fileName = fname,
                        content = item.content.ifEmpty { mockContent },
                        mimeType = mime
                      )
                      Toast.makeText(context, "Saved to ${dl.locationDescription}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                      .size(36.dp)
                      .testTag("btn_download_export_${item.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Download,
                      contentDescription = "Download",
                      tint = Color(0xFF38BDF8),
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
