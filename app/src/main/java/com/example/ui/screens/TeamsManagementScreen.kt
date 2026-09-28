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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyEodEntity
import com.example.data.EmployeeEntity
import com.example.data.Role
import com.example.data.TeamEntity
import com.example.ui.components.UserAvatar
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.WorkBackground
import com.example.ui.theme.WorkOutline
import com.example.ui.theme.WorkOutlineVariant
import com.example.ui.theme.WorkPrimary
import com.example.ui.theme.WorkPrimaryContainer
import com.example.ui.theme.WorkSurface

@Composable
fun TeamsManagementScreen(
  teams: List<TeamEntity>,
  employees: List<EmployeeEntity>,
  dailyEods: List<DailyEodEntity>,
  currentRole: Role,
  onCreateTeamClick: () -> Unit,
  onEditTeamClick: (TeamEntity) -> Unit,
  onMoveEmployeeClick: (EmployeeEntity?) -> Unit,
  onToggleTeamStatus: (teamId: String, currentStatus: String) -> Unit,
  onAddEmployeeClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var statusFilter by remember { mutableStateOf("All") }

  val filteredTeams = teams.filter { team ->
    val matchesStatus = statusFilter == "All" || team.status.equals(statusFilter, ignoreCase = true)
    val matchesSearch = searchQuery.isBlank() ||
      team.name.contains(searchQuery, ignoreCase = true) ||
      team.department.contains(searchQuery, ignoreCase = true) ||
      team.projects.contains(searchQuery, ignoreCase = true)
    matchesStatus && matchesSearch
  }

  val activeTeamsCount = teams.count { it.status == "Active" }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(WorkBackground)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Bar & Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (currentRole == Role.ADMIN) "Company Teams & Departments" else "Team Directory",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF21005D)
        )
        Text(
          text = "$activeTeamsCount Active Teams • ${employees.size} Total Members",
          fontSize = 12.sp,
          color = Color(0xFF49454F)
        )
      }

      if (currentRole == Role.ADMIN) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedButton(
            onClick = { onMoveEmployeeClick(null) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("move_employee_button")
          ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Transfer", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }

          Button(
            onClick = onCreateTeamClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WorkPrimary),
            modifier = Modifier.testTag("create_team_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Team", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Search and filter chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search team, department, manager...", fontSize = 12.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF49454F)) },
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .testTag("team_search_input"),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color.White,
          unfocusedContainerColor = Color.White,
          unfocusedBorderColor = WorkOutlineVariant,
          focusedBorderColor = WorkPrimary
        )
      )

      listOf("All", "Active", "Inactive").forEach { status ->
        FilterChip(
          selected = statusFilter == status,
          onClick = { statusFilter = status },
          label = { Text(status, fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = WorkPrimaryContainer,
            selectedLabelColor = Color(0xFF21005D)
          )
        )
      }
    }

    // Teams List
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("teams_list"),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(filteredTeams, key = { it.teamId }) { team ->
        val teamMembers = employees.filter { it.team.equals(team.name, ignoreCase = true) }
        val teamEods = dailyEods.filter { eod -> teamMembers.any { it.employeeId == eod.employeeId } }
        val submittedCount = teamEods.map { it.employeeId }.distinct().size

        TeamCard(
          team = team,
          members = teamMembers,
          allEmployees = employees,
          submittedTodayCount = submittedCount,
          currentRole = currentRole,
          onEditClick = { onEditTeamClick(team) },
          onToggleStatus = { onToggleTeamStatus(team.teamId, team.status) },
          onTransferMember = { onMoveEmployeeClick(null) },
          onAddMemberClick = onAddEmployeeClick
        )
      }

      if (filteredTeams.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No teams matching criteria",
              fontSize = 14.sp,
              color = Color(0xFF79747E)
            )
          }
        }
      }
    }
  }
}

@Composable
fun TeamCard(
  team: TeamEntity,
  members: List<EmployeeEntity>,
  allEmployees: List<EmployeeEntity>,
  submittedTodayCount: Int,
  currentRole: Role,
  onEditClick: () -> Unit,
  onToggleStatus: () -> Unit,
  onTransferMember: () -> Unit,
  onAddMemberClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var menuExpanded by remember { mutableStateOf(false) }
  val isAdmin = currentRole == Role.ADMIN

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("team_card_${team.teamId}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = WorkSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header: Team Name & Status Badge & Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(WorkPrimaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.Group,
              contentDescription = null,
              tint = Color(0xFF21005D),
              modifier = Modifier.size(22.dp)
            )
          }

          Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = team.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20)
              )
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (team.status == "Active") StatusSuccessBg else Color(0xFFEEEEEE)
              ) {
                Text(
                  text = team.status,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (team.status == "Active") StatusSuccessText else Color(0xFF757575),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "${team.department} • ID: ${team.teamId}",
              fontSize = 11.sp,
              color = Color(0xFF49454F)
            )
          }
        }

        if (isAdmin) {
          Box {
            IconButton(onClick = { menuExpanded = true }) {
              Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color(0xFF49454F))
            }

            DropdownMenu(
              expanded = menuExpanded,
              onDismissRequest = { menuExpanded = false }
            ) {
              DropdownMenuItem(
                text = { Text("Edit Team") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                onClick = {
                  menuExpanded = false
                  onEditClick()
                }
              )
              DropdownMenuItem(
                text = { Text("Transfer Member") },
                leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp)) },
                onClick = {
                  menuExpanded = false
                  onTransferMember()
                }
              )
              DropdownMenuItem(
                text = { Text(if (team.status == "Active") "Deactivate Team" else "Activate Team") },
                leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) },
                onClick = {
                  menuExpanded = false
                  onToggleStatus()
                }
              )
            }
          }
        }
      }

      HorizontalDivider(color = WorkOutlineVariant)

      // Team summary details
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Folder, contentDescription = null, tint = WorkPrimary, modifier = Modifier.size(20.dp))
          Column {
            Text(
              text = "Department: ${team.department}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFF1D1B20)
            )
            Text(
              text = "Team Workspace",
              fontSize = 10.sp,
              color = Color(0xFF49454F)
            )
          }
        }

        // Member Count and today's submissions
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF7F2FA),
            border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${members.size} Members",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF21005D)
              )
              Text(
                text = "• $submittedTodayCount Logged",
                fontSize = 11.sp,
                color = Color(0xFF49454F)
              )
            }
          }
        }
      }

      // Projects / Work Areas
      if (team.projects.isNotBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Projects / Work Areas:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF49454F)
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            team.projects.split(",").take(3).forEach { project ->
              val trimmed = project.trim()
              if (trimmed.isNotBlank()) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = Color(0xFFF3EDF7),
                  border = androidx.compose.foundation.BorderStroke(1.dp, WorkOutlineVariant)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(10.dp), tint = WorkPrimary)
                    Text(
                      text = trimmed,
                      fontSize = 10.sp,
                      color = Color(0xFF21005D),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Team members preview
      if (members.isNotEmpty()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Team Members:",
            fontSize = 11.sp,
            color = Color(0xFF79747E)
          )
          members.take(4).forEach { member ->
            Text(
              text = member.name.split(" ").firstOrNull() ?: member.name,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFF49454F),
              modifier = Modifier
                .background(Color(0xFFE8DEF8), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          if (members.size > 4) {
            Text(
              text = "+${members.size - 4} more",
              fontSize = 11.sp,
              color = Color(0xFF79747E)
            )
          }
        }
      }
    }
  }
}
