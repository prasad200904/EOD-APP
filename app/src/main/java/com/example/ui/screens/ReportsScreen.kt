package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyEodEntity
import com.example.data.EmployeeEntity
import com.example.data.TeamEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.StatusErrorBg
import com.example.ui.theme.StatusErrorText
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.WorkBackground
import com.example.ui.theme.WorkOutline
import com.example.ui.theme.WorkOutlineVariant
import com.example.ui.theme.WorkPrimary
import com.example.ui.theme.WorkPrimaryContainer
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ReportsScreen(
  eods: List<DailyEodEntity>,
  employees: List<EmployeeEntity>,
  teams: List<TeamEntity> = emptyList(),
  onExport: (String) -> Unit,
  onExportAdvanced: ((scope: String, format: String, targetTeam: String?, targetEmpId: String?) -> Unit)? = null,
  onSelectEmployee: (EmployeeEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  var searchQuery by remember { mutableStateOf("") }
  var statusFilter by remember { mutableStateOf("All") }
  var selectedTeamFilter by remember { mutableStateOf("All") }
  var selectedEmployeeFilter by remember { mutableStateOf("All") }
  var selectedProjectFilter by remember { mutableStateOf("All") }

  // Date range filters
  var filterStartDate by remember { mutableStateOf("") }
  var filterEndDate by remember { mutableStateOf("") }

  // Dropdown states
  var teamFilterMenuOpen by remember { mutableStateOf(false) }
  var empFilterMenuOpen by remember { mutableStateOf(false) }
  var projectFilterMenuOpen by remember { mutableStateOf(false) }

  // Custom export panel toggle
  var showCustomExportConfig by remember { mutableStateOf(false) }
  var selectedScope by remember { mutableStateOf("COMPANY") } // "COMPANY", "TEAM", "INDIVIDUAL"
  var selectedFormat by remember { mutableStateOf("CSV") } // "EXCEL", "CSV" - Changed default to CSV
  var selectedTeamName by remember { mutableStateOf(teams.firstOrNull()?.name ?: "All") }
  var selectedEmpId by remember { mutableStateOf(employees.firstOrNull()?.employeeId ?: "EMP001") }
  var teamExportMenuOpen by remember { mutableStateOf(false) }
  var empExportMenuOpen by remember { mutableStateOf(false) }

  val statusOptions = listOf("All", "Completed", "In Progress", "Blocked", "No Work", "On Leave")

  val empMap = remember(employees) { employees.associateBy { it.employeeId } }

  // Dynamic distinct project options from EODs
  val projectOptions = remember(eods) {
    listOf("All") + eods.map { it.project.trim() }.filter { it.isNotBlank() }.distinct().sorted()
  }

  // Filtered EOD records matching all criteria: date range, team, employee, project, status, text search
  val filteredEods = remember(eods, statusFilter, selectedTeamFilter, selectedEmployeeFilter, selectedProjectFilter, filterStartDate, filterEndDate, searchQuery, empMap) {
    eods.filter { eod ->
      val matchesStatus = statusFilter == "All" || eod.workStatus.equals(statusFilter, ignoreCase = true)
      val emp = empMap[eod.employeeId]
      val matchesTeam = selectedTeamFilter == "All" || (emp?.team?.equals(selectedTeamFilter, ignoreCase = true) == true)
      val matchesEmployee = selectedEmployeeFilter == "All" || eod.employeeId == selectedEmployeeFilter
      val matchesProject = selectedProjectFilter == "All" || eod.project.equals(selectedProjectFilter, ignoreCase = true)
      val matchesStart = filterStartDate.isBlank() || eod.date >= filterStartDate
      val matchesEnd = filterEndDate.isBlank() || eod.date <= filterEndDate
      val matchesSearch = searchQuery.isBlank() ||
        eod.employeeName.contains(searchQuery, ignoreCase = true) ||
        eod.employeeId.contains(searchQuery, ignoreCase = true) ||
        eod.project.contains(searchQuery, ignoreCase = true) ||
        eod.todayWork.contains(searchQuery, ignoreCase = true) ||
        eod.date.contains(searchQuery, ignoreCase = true)

      matchesStatus && matchesTeam && matchesEmployee && matchesProject && matchesStart && matchesEnd && matchesSearch
    }.sortedWith(compareByDescending<DailyEodEntity> { it.date }.thenBy { it.employeeName })
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(WorkBackground)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Title & Metrics
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "EOD History & Reports",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF21005D)
        )
        Text(
          text = "${filteredEods.size} of ${eods.size} records matching active filters",
          fontSize = 12.sp,
          color = Color(0xFF49454F)
        )
      }

      // Quick Reset Filters Button
      if (statusFilter != "All" || selectedTeamFilter != "All" || selectedEmployeeFilter != "All" ||
        selectedProjectFilter != "All" || filterStartDate.isNotBlank() || filterEndDate.isNotBlank() || searchQuery.isNotBlank()
      ) {
        OutlinedButton(
          onClick = {
            statusFilter = "All"
            selectedTeamFilter = "All"
            selectedEmployeeFilter = "All"
            selectedProjectFilter = "All"
            filterStartDate = ""
            filterEndDate = ""
            searchQuery = ""
          },
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Reset", fontSize = 11.sp)
        }
      }
    }

    // Export & Download Card
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
          Column {
            Text(
              text = "DOWNLOAD & EXPORT REPORT",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF21005D),
              letterSpacing = 0.5.sp
            )
            Text(
              text = "Filtered EOD CSV / Excel Workbook Export",
              fontSize = 11.sp,
              color = Color(0xFF79747E)
            )
          }

          OutlinedButton(
            onClick = { showCustomExportConfig = !showCustomExportConfig },
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(if (showCustomExportConfig) "Hide Scope" else "Advanced Scope", fontSize = 11.sp)
          }
        }

        // Direct download button for currently filtered records
        Button(
          onClick = {
            val startTag = if (filterStartDate.isNotBlank()) filterStartDate else "all"
            val endTag = if (filterEndDate.isNotBlank()) filterEndDate else "latest"
            val fileName = "eod-report_${startTag}_to_${endTag}.csv"
            val csvContent = buildString {
              append("Employee ID,Employee Name,Date,Team,Project,Today's Work,Hours Worked,Progress Percentage,Work Status,Blockers,Remarks,Tomorrow Plan,Submission Time,On Time\n")
              for (r in filteredEods) {
                val emp = empMap[r.employeeId]
                val safeName = "\"" + r.employeeName.replace("\"", "\"\"") + "\""
                val safeTeam = "\"" + (emp?.team ?: "Engineering").replace("\"", "\"\"") + "\""
                val safeProj = "\"" + r.project.replace("\"", "\"\"") + "\""
                val safeTask = "\"" + r.todayWork.replace("\"", "\"\"") + "\""
                val safeBlocker = "\"" + r.blockers.replace("\"", "\"\"") + "\""
                val safeRemarks = "\"" + r.remarks.replace("\"", "\"\"") + "\""
                val safeTomorrow = "\"" + r.tomorrowPlan.replace("\"", "\"\"") + "\""
                append("\"${r.employeeId}\",$safeName,\"${r.date}\",$safeTeam,$safeProj,$safeTask,${r.hoursWorked},${r.progressPercentage}%,\"${r.workStatus}\",$safeBlocker,$safeRemarks,$safeTomorrow,\"${r.submissionTime}\",${if (r.isOnTime) "Yes" else "No"}\n")
              }
            }

            try {
              val file = File(context.getExternalFilesDir(null) ?: context.cacheDir, fileName)
              file.writeText(csvContent)
              val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_TEXT, csvContent)
              }
              context.startActivity(Intent.createChooser(shareIntent, "Download $fileName"))
              Toast.makeText(context, "Exported $fileName (${filteredEods.size} records)", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText(fileName, csvContent)
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "CSV copied to clipboard ($fileName)", Toast.LENGTH_LONG).show()
            }
          },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21005D)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("download_filtered_csv_btn")
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          val startTag = if (filterStartDate.isNotBlank()) filterStartDate else "start"
          val endTag = if (filterEndDate.isNotBlank()) filterEndDate else "end"
          Text(
            text = "Download CSV (eod-report_${startTag}_to_${endTag}.csv)",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        if (showCustomExportConfig) {
          HorizontalDivider(color = WorkOutlineVariant)

          // Format selection
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Format:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20))
            listOf("EXCEL" to "Excel (.xlsx)", "CSV" to "CSV (.csv)").forEach { (fmt, label) ->
              FilterChip(
                selected = selectedFormat == fmt,
                onClick = { selectedFormat = fmt },
                label = { Text(label, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = WorkPrimaryContainer,
                  selectedLabelColor = Color(0xFF21005D)
                )
              )
            }
          }

          // Scope selection
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Scope:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20))
            listOf("COMPANY" to "Company", "TEAM" to "Team", "INDIVIDUAL" to "Staff").forEach { (sc, label) ->
              FilterChip(
                selected = selectedScope == sc,
                onClick = { selectedScope = sc },
                label = { Text(label, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = WorkPrimaryContainer,
                  selectedLabelColor = Color(0xFF21005D)
                )
              )
            }
          }

          // Dynamic filter depending on scope
          if (selectedScope == "TEAM" && teams.isNotEmpty()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Select Team:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20))
              Box {
                OutlinedButton(
                  onClick = { teamExportMenuOpen = true },
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(selectedTeamName, fontSize = 12.sp)
                }
                DropdownMenu(expanded = teamExportMenuOpen, onDismissRequest = { teamExportMenuOpen = false }) {
                  teams.forEach { tm ->
                    DropdownMenuItem(
                      text = { Text(tm.name) },
                      onClick = {
                        selectedTeamName = tm.name
                        teamExportMenuOpen = false
                      }
                    )
                  }
                }
              }
            }
          }

          if (selectedScope == "INDIVIDUAL" && employees.isNotEmpty()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Employee:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20))
              val targetEmp = employees.find { it.employeeId == selectedEmpId }
              Box {
                OutlinedButton(
                  onClick = { empExportMenuOpen = true },
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(targetEmp?.name ?: selectedEmpId, fontSize = 12.sp)
                }
                DropdownMenu(expanded = empExportMenuOpen, onDismissRequest = { empExportMenuOpen = false }) {
                  employees.forEach { emp ->
                    DropdownMenuItem(
                      text = { Text("${emp.name} (${emp.team})") },
                      onClick = {
                        selectedEmpId = emp.employeeId
                        empExportMenuOpen = false
                      }
                    )
                  }
                }
              }
            }
          }

          // Generate Custom Export CTA
          Button(
            onClick = {
              if (onExportAdvanced != null) {
                onExportAdvanced(
                  selectedScope,
                  selectedFormat,
                  if (selectedScope == "TEAM") selectedTeamName else null,
                  if (selectedScope == "INDIVIDUAL") selectedEmpId else null
                )
              } else {
                onExport(if (selectedScope == "TEAM") "TEAM" else if (selectedScope == "INDIVIDUAL") "EOD" else "COMPANY")
              }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WorkPrimary)
          ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Generate & Export ${if (selectedFormat == "EXCEL") "Excel Workbook" else "CSV"}", fontWeight = FontWeight.Bold)
          }
        } else {
          // Quick Export Standard Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { onExport("EOD") },
              modifier = Modifier
                .weight(1f)
                .testTag("export_eod_button"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = WorkPrimary)
            ) {
              Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("EOD Log", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
              onClick = { onExport("PERFORMANCE") },
              modifier = Modifier
                .weight(1f)
                .testTag("export_performance_button"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Performance", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
              onClick = { onExport("TEAM") },
              modifier = Modifier
                .weight(1f)
                .testTag("export_team_button"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Team", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }

    // -----------------------------------------------------------------
    // FILTER CONTROLS: Date Range, Team, Employee, Project
    // -----------------------------------------------------------------
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
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.FilterAlt, contentDescription = null, tint = WorkPrimary, modifier = Modifier.size(16.dp))
            Text(
              text = "FILTER EOD DATABASE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF21005D),
              letterSpacing = 0.5.sp
            )
          }

          Text(
            text = "${filteredEods.size} Records",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = WorkPrimary
          )
        }

        // Date Pickers Row (Start Date - End Date)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = filterStartDate,
            onValueChange = { filterStartDate = it },
            label = { Text("From (YYYY-MM-DD)", fontSize = 11.sp) },
            singleLine = true,
            trailingIcon = {
              IconButton(onClick = {
                val c = Calendar.getInstance()
                DatePickerDialog(
                  context,
                  { _, y, m, d ->
                    filterStartDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                  },
                  c.get(Calendar.YEAR),
                  c.get(Calendar.MONTH),
                  c.get(Calendar.DAY_OF_MONTH)
                ).show()
              }) {
                Icon(Icons.Default.DateRange, contentDescription = "From Date", modifier = Modifier.size(16.dp))
              }
            },
            modifier = Modifier.weight(1f).testTag("reports_start_date"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = WorkPrimary,
              unfocusedBorderColor = WorkOutlineVariant
            )
          )

          OutlinedTextField(
            value = filterEndDate,
            onValueChange = { filterEndDate = it },
            label = { Text("To (YYYY-MM-DD)", fontSize = 11.sp) },
            singleLine = true,
            trailingIcon = {
              IconButton(onClick = {
                val c = Calendar.getInstance()
                DatePickerDialog(
                  context,
                  { _, y, m, d ->
                    filterEndDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                  },
                  c.get(Calendar.YEAR),
                  c.get(Calendar.MONTH),
                  c.get(Calendar.DAY_OF_MONTH)
                ).show()
              }) {
                Icon(Icons.Default.DateRange, contentDescription = "To Date", modifier = Modifier.size(16.dp))
              }
            },
            modifier = Modifier.weight(1f).testTag("reports_end_date"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = WorkPrimary,
              unfocusedBorderColor = WorkOutlineVariant
            )
          )
        }

        // Filter Dropdowns: Team, Employee, Project
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Team Selector Dropdown
          Box {
            OutlinedButton(
              onClick = { teamFilterMenuOpen = true },
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = if (selectedTeamFilter == "All") "Team: All" else selectedTeamFilter,
                fontSize = 11.sp,
                color = Color(0xFF1D1B20)
              )
              Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(expanded = teamFilterMenuOpen, onDismissRequest = { teamFilterMenuOpen = false }) {
              DropdownMenuItem(text = { Text("All Teams") }, onClick = { selectedTeamFilter = "All"; teamFilterMenuOpen = false })
              teams.forEach { tm ->
                DropdownMenuItem(text = { Text(tm.name) }, onClick = { selectedTeamFilter = tm.name; teamFilterMenuOpen = false })
              }
            }
          }

          // Employee Selector Dropdown
          Box {
            OutlinedButton(
              onClick = { empFilterMenuOpen = true },
              shape = RoundedCornerShape(8.dp)
            ) {
              val empName = employees.find { it.employeeId == selectedEmployeeFilter }?.name ?: "All"
              Text(
                text = if (selectedEmployeeFilter == "All") "Staff: All" else empName,
                fontSize = 11.sp,
                color = Color(0xFF1D1B20)
              )
              Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(expanded = empFilterMenuOpen, onDismissRequest = { empFilterMenuOpen = false }) {
              DropdownMenuItem(text = { Text("All Employees") }, onClick = { selectedEmployeeFilter = "All"; empFilterMenuOpen = false })
              employees.forEach { emp ->
                DropdownMenuItem(
                  text = { Text("${emp.name} (${emp.employeeId})") },
                  onClick = { selectedEmployeeFilter = emp.employeeId; empFilterMenuOpen = false }
                )
              }
            }
          }

          // Project Selector Dropdown
          Box {
            OutlinedButton(
              onClick = { projectFilterMenuOpen = true },
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = if (selectedProjectFilter == "All") "Project: All" else selectedProjectFilter,
                fontSize = 11.sp,
                color = Color(0xFF1D1B20)
              )
              Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(expanded = projectFilterMenuOpen, onDismissRequest = { projectFilterMenuOpen = false }) {
              projectOptions.forEach { proj ->
                DropdownMenuItem(text = { Text(proj) }, onClick = { selectedProjectFilter = proj; projectFilterMenuOpen = false })
              }
            }
          }
        }
      }
    }

    // Search Input
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("Search by date, employee, or project...", fontSize = 13.sp) },
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

    // Status Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      statusOptions.forEach { status ->
        val selected = statusFilter == status
        FilterChip(
          selected = selected,
          onClick = { statusFilter = status },
          label = { Text(status, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
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

    // Historical Records List
    if (filteredEods.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text("No EOD records found matching current filters", color = Color(0xFF79747E), fontSize = 13.sp)
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredEods, key = { it.id }) { eod ->
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, WorkOutlineVariant, RoundedCornerShape(14.dp))
              .clickable {
                empMap[eod.employeeId]?.let { onSelectEmployee(it) }
              },
            color = Color.White
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              // Row 1: Employee, Date, Status Badge
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  UserAvatar(name = eod.employeeName, size = 30)
                  Column {
                    Text(eod.employeeName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                    val teamName = empMap[eod.employeeId]?.team ?: "Engineering"
                    Text("${eod.date} · ${eod.submissionTime} · $teamName", fontSize = 11.sp, color = Color(0xFF79747E))
                  }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                  StatusBadge(status = eod.workStatus)
                  if (!eod.isOnTime) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text("Late", fontSize = 10.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }

              // Row 2: Project
              Text(
                text = "Project: ${eod.project}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = WorkPrimary
              )

              // Row 3: Work Description
              Text(
                text = eod.todayWork,
                fontSize = 12.sp,
                color = Color(0xFF1D1B20),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )

              // Row 4: Hours & Progress
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Hours: ${eod.hoursWorked}h  ·  Progress: ${eod.progressPercentage}%",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color(0xFF49454F)
                )

                if (eod.blockers.isNotBlank()) {
                  Text(
                    text = "Blockers reported",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB3261E)
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
