package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.data.DepartmentEntity
import com.example.data.EmployeeEntity
import com.example.data.TeamMemberDataEntity
import com.example.data.TeamType
import com.example.data.Country
import com.example.data.CounsellingApplicationStatus
import com.example.data.TelecallingStatus
import com.example.ui.theme.WarmCream
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSlateDeep
import com.example.ui.theme.SubtleText
import com.example.ui.theme.WarmCardBorder
import com.example.ui.theme.MintGreen

/**
 * Screen 2: "Add employee"
 * Exact visual representation from user screenshot 2:
 * - Back button + Title "Add employee"
 * - Full name (e.g. Meera Iyer)
 * - Work email (e.g. meera.iyer@company.com)
 * - Employee code (ML-014, auto-generated based on selected department prefix) + Joining date (22 Sep)
 * - Designation (e.g. ML Engineer)
 * - Department selection:
 *   Header: "Department" | "You manage 2"
 *   Grid: [ ML ] [ DB ]
 *         [ GT ] [ Writing ]
 *         [ Cyber ]
 *   Greyed departments disabled ("Greyed departments need admin access")
 * - Email setup link card:
 *   Mail icon, "Email setup link", "They set their own password" + Switch
 * - Purple "Create account" button
 */
@Composable
fun AddEmployeeScreen(
  currentManager: EmployeeEntity?,
  allDepartments: List<DepartmentEntity>,
  onBackClick: () -> Unit,
  onCreateAccount: (
    name: String,
    email: String,
    employeeCode: String,
    joiningDate: String,
    designation: String,
    departmentCode: String,
    sendEmailLink: Boolean,
    teamSpecificData: Map<TeamType, TeamMemberDataEntity?>
  ) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  // Find managed departments for this manager
  val managedCodes = remember(currentManager) {
    if (currentManager?.role == "ADMIN") allDepartments.map { it.code }.toSet()
    else {
      (currentManager?.managedDepartments?.split(",")
        .orEmpty()
        .map { it.trim() }
        .filter { it.isNotEmpty() })
        .toSet().ifEmpty { currentManager?.department?.let { setOf(it) } ?: emptySet() }
    }
  }

  // Department grid rows (all departments from SeedData)
  val deptCodesList = allDepartments.map { it.code }.distinct()
  val deptRows = deptCodesList.chunked(2)

  var fullName by remember { mutableStateOf("Meera Iyer") }
  var workEmail by remember { mutableStateOf("meera.iyer@company.com") }
  var selectedDepartmentCode by remember { mutableStateOf("ML") }
  var joiningDate by remember { mutableStateOf("22 Sep") }
  var designation by remember { mutableStateOf("ML Engineer") }
  var emailSetupLinkEnabled by remember { mutableStateOf(true) }

  var selectedTeamType by remember { mutableStateOf<TeamType?>(null) }
  var teamSpecificData by remember { mutableStateOf<MutableMap<TeamType, TeamMemberDataEntity>>(mutableMapOf()) }

  // Counselling fields
  var counsellingStudentName by remember { mutableStateOf("") }
  var counsellingUniversityName by remember { mutableStateOf("") }
  var counsellingCountry by remember { mutableStateOf(Country.USA) }
  var counsellingAppStatus by remember { mutableStateOf(CounsellingApplicationStatus.APPLICATION_SUBMITTED) }
  var counsellingDescription by remember { mutableStateOf("") }

  // Telecalling fields
  var telecallingCalls by remember { mutableStateOf("0") }
  var telecallingFollowUps by remember { mutableStateOf("0") }
  var telecallingConversions by remember { mutableStateOf("0") }
  var telecallingStatus by remember { mutableStateOf(TelecallingStatus.IN_PROGRESS) }
  var telecallingDescription by remember { mutableStateOf("") }

  // Marketing fields
  var marketingCollegeName by remember { mutableStateOf("") }
  var marketingPersonNameVisited by remember { mutableStateOf("") }
  var marketingPersonDesignation by remember { mutableStateOf("") }
  var marketingDescription by remember { mutableStateOf("") }

  // Other fields
  var otherDescription by remember { mutableStateOf("") }

  // Auto-generate employee code preview (actual ID assigned on save)
  val employeeCodePreview by remember(selectedDepartmentCode) {
    derivedStateOf {
      val prefix = when (selectedDepartmentCode) {
        "ML" -> "ML"
        "DB" -> "DB"
        "GT" -> "GT"
        "Writing" -> "WR"
        "Cyber" -> "CYBER"
        else -> selectedDepartmentCode.take(3).uppercase()
      }
      "$prefix-XXX (assigned on save)"
    }
  }

  fun buildTeamSpecificData(): Map<TeamType, TeamMemberDataEntity?> {
    return selectedTeamType?.let { teamType ->
      val data = when (teamType) {
        TeamType.COUNSELLING -> TeamMemberDataEntity(
          teamType = teamType.departmentCode,
          studentName = counsellingStudentName,
          universityName = counsellingUniversityName,
          country = counsellingCountry.name,
          applicationStatus = counsellingAppStatus.name,
          description = counsellingDescription
        )

        TeamType.TELECALLING -> TeamMemberDataEntity(
          teamType = teamType.departmentCode,
          telecallingStatus = telecallingStatus.name,
          calls = telecallingCalls.toIntOrNull() ?: 0,
          followUps = telecallingFollowUps.toIntOrNull() ?: 0,
          conversions = telecallingConversions.toIntOrNull() ?: 0,
          description = telecallingDescription
        )

        TeamType.MARKETING -> TeamMemberDataEntity(
          teamType = teamType.departmentCode,
          collegeName = marketingCollegeName,
          personNameVisited = marketingPersonNameVisited,
          personDesignation = marketingPersonDesignation,
          description = marketingDescription
        )

        TeamType.OTHER -> TeamMemberDataEntity(
          teamType = teamType.departmentCode,
          description = otherDescription
        )
      }
      // Include the entry only if at least one meaningful field is filled
      if (data.studentName.isNotBlank() || data.universityName.isNotBlank() ||
        data.country.isNotBlank() || data.applicationStatus.isNotBlank() ||
        data.telecallingStatus.isNotBlank() || data.calls > 0 || data.followUps > 0 ||
        data.conversions > 0 || data.collegeName.isNotBlank() ||
        data.personNameVisited.isNotBlank() || data.personDesignation.isNotBlank() ||
        data.description.isNotBlank()) {
        mapOf(teamType to data)
      } else {
        emptyMap()
      }
    } ?: emptyMap()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(WarmCream)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // 1. Top Bar: Back button + Title
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = DarkSlateDeep,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = "Add employee",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = DarkSlateDeep
        )
      }

      // 2. Full Name
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Full name",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = SubtleText
        )
        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          placeholder = { Text("e.g. Meera Iyer", color = SubtleText, fontSize = 14.sp) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_full_name"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = CoralPrimary,
            unfocusedBorderColor = WarmCardBorder,
            focusedTextColor = DarkSlateDeep,
            unfocusedTextColor = DarkSlateDeep
          ),
          singleLine = true
        )
      }

      // 3. Work Email
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Work email",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = SubtleText
        )
        OutlinedTextField(
          value = workEmail,
          onValueChange = { workEmail = it },
          placeholder = { Text("e.g. meera.iyer@company.com", color = SubtleText, fontSize = 14.sp) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_work_email"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = CoralPrimary,
            unfocusedBorderColor = WarmCardBorder,
            focusedTextColor = DarkSlateDeep,
            unfocusedTextColor = DarkSlateDeep
          ),
          singleLine = true
        )
      }

      // 4. Row: Employee Code + Joining Date
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Employee Code (auto-generated)
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Employee code",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = SubtleText
          )
          OutlinedTextField(
            value = employeeCodePreview,
            onValueChange = { /* auto-generated, read only */ },
            readOnly = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_employee_code"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedBorderColor = WarmCardBorder,
              unfocusedBorderColor = WarmCardBorder,
              focusedTextColor = SubtleText,
              unfocusedTextColor = SubtleText
            ),
            singleLine = true
          )
        }

        // Joining Date
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Joining date",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = SubtleText
          )
          OutlinedTextField(
            value = joiningDate,
            onValueChange = { joiningDate = it },
            placeholder = { Text("22 Sep", color = SubtleText, fontSize = 14.sp) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_joining_date"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedBorderColor = CoralPrimary,
              unfocusedBorderColor = WarmCardBorder,
              focusedTextColor = DarkSlateDeep,
              unfocusedTextColor = DarkSlateDeep
            ),
            singleLine = true
          )
        }
      }

      // 5. Designation
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Designation",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = SubtleText
        )
        OutlinedTextField(
          value = designation,
          onValueChange = { designation = it },
          placeholder = { Text("e.g. ML Engineer", color = SubtleText, fontSize = 14.sp) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_designation"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = CoralPrimary,
            unfocusedBorderColor = WarmCardBorder,
            focusedTextColor = DarkSlateDeep,
            unfocusedTextColor = DarkSlateDeep
          ),
          singleLine = true
        )
      }

      // 6. Department Section
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Department",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = SubtleText
          )
          Text(
            text = "You manage ${managedCodes.size}",
            fontSize = 13.sp,
            color = SubtleText
          )
        }

        // Grid of department chips from all available departments
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          deptRows.forEach { rowDepts ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              rowDepts.forEach { deptCode ->
                val isManaged = managedCodes.contains(deptCode) || currentManager?.role == "ADMIN"
                val isSelected = selectedDepartmentCode == deptCode

                Surface(
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .alpha(if (isManaged) 1.0f else 0.4f)
                    .clickable(enabled = isManaged) {
                      selectedDepartmentCode = deptCode
                    },
                  color = if (isSelected) Color(0xFF172554) else Color(0xFF181922),
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFF3B82F6) else Color(0xFF262837)
                  )
                ) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = deptCode,
                      fontSize = 14.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) CoralPrimary else if (isManaged) DarkSlateDeep else Color(0xFF6B7280)
                    )
                  }
                }
              }
              // If row has 1 item, add a placeholder spacer to balance the grid
              if (rowDepts.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }

        Text(
          text = "Greyed departments need admin access",
          fontSize = 12.sp,
          color = Color(0xFF6B7280),
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      // Team type section
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Team type",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = SubtleText
          )
          Text(
            text = "Select for team-specific tracking",
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          TeamType.entries.forEach { teamType ->
            val isSelected = selectedTeamType == teamType

            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable {
                  selectedTeamType = teamType
                },
              color = if (isSelected) Color(0xFF172554) else Color(0xFF181922),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(
                1.dp,
                if (isSelected) Color(0xFF7C3AED) else Color(0xFF262837)
              )
            ) {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = teamType.displayName,
                  fontSize = 14.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color(0xFF9D6BCB) else DarkSlateDeep
                )
              }
            }
          }
        }

        // Team-specific fields
        if (selectedTeamType != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF181922),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF262837))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
            ) {
              Text(
                text = "Team-specific fields",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkSlateDeep
              )
              Spacer(modifier = Modifier.height(8.dp))

              when (selectedTeamType) {
                TeamType.COUNSELLING -> {
                  OutlinedTextField(
                    value = counsellingStudentName,
                    onValueChange = { counsellingStudentName = it },
                    label = { Text("Student Name", fontSize = 12.sp) },
                    placeholder = { Text("e.g., Rohan Mehta", fontSize = 12.sp, color = SubtleText) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedContainerColor = Color.White,
                      unfocusedContainerColor = Color.White,
                      focusedBorderColor = CoralPrimary,
                      unfocusedBorderColor = Color(0xFFD1D5DB),
                      focusedTextColor = DarkSlateDeep,
                      unfocusedTextColor = DarkSlateDeep
                    )
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  OutlinedTextField(
                    value = counsellingUniversityName,
                    onValueChange = { counsellingUniversityName = it },
                    label = { Text("University Name", fontSize = 12.sp) },
                    placeholder = { Text("e.g., University of Toronto", fontSize = 12.sp, color = SubtleText) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedContainerColor = Color.White,
                      unfocusedContainerColor = Color.White,
                      focusedBorderColor = CoralPrimary,
                      unfocusedBorderColor = Color(0xFFD1D5DB),
                      focusedTextColor = DarkSlateDeep,
                      unfocusedTextColor = DarkSlateDeep
                    )
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Column {
                    Text("Country", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SubtleText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      Country.entries.forEach { country ->
                        val countrySelected = counsellingCountry == country
                        Surface(
                          modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { counsellingCountry = country },
                          color = if (countrySelected) Color(0xFF172554) else Color(0xFF262837),
                          shape = RoundedCornerShape(8.dp),
                          border = BorderStroke(1.dp, if (countrySelected) Color(0xFF7C3AED) else Color(0xFF3F4157))
                        ) {
                          Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                          ) {
                            Text(
                              text = country.displayName,
                              fontSize = 12.sp,
                              fontWeight = if (countrySelected) FontWeight.Bold else FontWeight.Medium,
                              color = if (countrySelected) Color(0xFF9D6BCB) else Color(0xFFCBD5E1)
                            )
                          }
                        }
                      }
                    }
                  }
                  Spacer(modifier = Modifier.height(10.dp))
                  Column {
                    Text("Application Status", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SubtleText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      CounsellingApplicationStatus.entries.forEach { status ->
                        val statusSelected = counsellingAppStatus == status
                        Surface(
                          modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { counsellingAppStatus = status },
                          color = if (statusSelected) Color(0xFF172554) else Color(0xFF262837),
                          shape = RoundedCornerShape(8.dp),
                          border = BorderStroke(1.dp, if (statusSelected) Color(0xFF7C3AED) else Color(0xFF3F4157))
                        ) {
                          Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                          ) {
                            Text(
                              text = status.displayName,
                              fontSize = 11.sp,
                              fontWeight = if (statusSelected) FontWeight.Bold else FontWeight.Medium,
                              color = if (statusSelected) Color(0xFF9D6BCB) else Color(0xFFCBD5E1)
                            )
                          }
                        }
                      }
                    }
                  }
                  Spacer(modifier = Modifier.height(10.dp))
                  OutlinedTextField(
                    value = counsellingDescription,
                    onValueChange = { counsellingDescription = it },
                    label = { Text("Description", fontSize = 12.sp) },
                    placeholder = { Text("Additional notes about counselling case", fontSize = 12.sp, color = SubtleText) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedContainerColor = Color.White,
                      unfocusedContainerColor = Color.White,
                      focusedBorderColor = CoralPrimary,
                      unfocusedBorderColor = Color(0xFFD1D5DB),
                      focusedTextColor = DarkSlateDeep,
                      unfocusedTextColor = DarkSlateDeep
                    )
                  )
                }

                TeamType.TELECALLING -> {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    OutlinedTextField(
                      value = telecallingCalls,
                      onValueChange = { telecallingCalls = it },
                      label = { Text("Calls", fontSize = 12.sp) },
                      placeholder = { Text("0", fontSize = 12.sp, color = SubtleText) },
                      modifier = Modifier.weight(1f),
                      singleLine = true,
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                      shape = RoundedCornerShape(8.dp),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        focusedTextColor = DarkSlateDeep,
                        unfocusedTextColor = DarkSlateDeep
                      )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                      value = telecallingFollowUps,
                      onValueChange = { telecallingFollowUps = it },
                      label = { Text("Follow-ups", fontSize = 12.sp) },
                      placeholder = { Text("0", fontSize = 12.sp, color = SubtleText) },
                      modifier = Modifier.weight(1f),
                      singleLine = true,
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                      shape = RoundedCornerShape(8.dp),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        focusedTextColor = DarkSlateDeep,
                        unfocusedTextColor = DarkSlateDeep
                      )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                      value = telecallingConversions,
                      onValueChange = { telecallingConversions = it },
                      label = { Text("Conversions", fontSize = 12.sp) },
                      placeholder = { Text("0", fontSize = 12.sp, color = SubtleText) },
                      modifier = Modifier.weight(1f),
                      singleLine = true,
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                      shape = RoundedCornerShape(8.dp),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        focusedTextColor = DarkSlateDeep,
                        unfocusedTextColor = DarkSlateDeep
                      )
                    )
                  }
                  Spacer(modifier = Modifier.height(10.dp))
                  Column {
                    Text("Telecalling Status", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SubtleText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      TelecallingStatus.entries.forEach { status ->
                        val statusSelected = telecallingStatus == status
                        Surface(
                          modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { telecallingStatus = status },
                          color = if (statusSelected) Color(0xFF172554) else Color(0xFF262837),
                          shape = RoundedCornerShape(8.dp),
                          border = BorderStroke(1.dp, if (statusSelected) Color(0xFF7C3AED) else Color(0xFF3F4157))
                        ) {
                          Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                          ) {
                            Text(
                              text = status.displayName,
                              fontSize = 11.sp,
                              fontWeight = if (statusSelected) FontWeight.Bold else FontWeight.Medium,
                              color = if (statusSelected) Color(0xFF9D6BCB) else Color(0xFFCBD5E1)
                            )
                          }
                        }
                      }
                    }
                  }
                  Spacer(modifier = Modifier.height(10.dp))
                  OutlinedTextField(
                    value = telecallingDescription,
                    onValueChange = { telecallingDescription = it },
                    label = { Text("Description", fontSize = 12.sp) },
                    placeholder = { Text("Notes about telecalling activity", fontSize = 12.sp, color = SubtleText) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedContainerColor = Color.White,
                      unfocusedContainerColor = Color.White,
                      focusedBorderColor = CoralPrimary,
                      unfocusedBorderColor = Color(0xFFD1D5DB),
                      focusedTextColor = DarkSlateDeep,
                      unfocusedTextColor = DarkSlateDeep
                    )
                  )
                }

                TeamType.MARKETING -> {
                  OutlinedTextField(
                    value = marketingCollegeName,
                    onValueChange = { marketingCollegeName = it },
                    label = { Text("College Name", fontSize = 12.sp) },
                    placeholder = { Text("e.g., Delhi University", fontSize = 12.sp, color = SubtleText) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedContainerColor = Color.White,
                      unfocusedContainerColor = Color.White,
                      focusedBorderColor = CoralPrimary,
                      unfocusedBorderColor = Color(0xFFD1D5DB),
                      focusedTextColor = DarkSlateDeep,
                      unfocusedTextColor = DarkSlateDeep
                    )
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  OutlinedTextField(
                    value = marketingPersonNameVisited,
                    onValueChange = { marketingPersonNameVisited = it },
                    label = { Text("Person Name Visited", fontSize = 12.sp) },
                    placeholder = { Text("e.g., Prof. Suresh Kumar", fontSize = 12.sp, color = SubtleText) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedContainerColor = Color.White,
                      unfocusedContainerColor = Color.White,
                      focusedBorderColor = CoralPrimary,
                      unfocusedBorderColor = Color(0xFFD1D5DB),
                      focusedTextColor = DarkSlateDeep,
                      unfocusedTextColor = DarkSlateDeep
                    )
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  OutlinedTextField(
                    value = marketingPersonDesignation,
                    onValueChange = { marketingPersonDesignation = it },
                    label = { Text("Person Designation", fontSize = 12.sp) },
                    placeholder = { Text("e.g., Dean of Admissions", fontSize = 12.sp, color = SubtleText) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedContainerColor = Color.White,
                      unfocusedContainerColor = Color.White,
                      focusedBorderColor = CoralPrimary,
                      unfocusedBorderColor = Color(0xFFD1D5DB),
                      focusedTextColor = DarkSlateDeep,
                      unfocusedTextColor = DarkSlateDeep
                    )
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  OutlinedTextField(
                    value = marketingDescription,
                    onValueChange = { marketingDescription = it },
                    label = { Text("Description", fontSize = 12.sp) },
                    placeholder = { Text("Notes about the marketing visit", fontSize = 12.sp, color = SubtleText) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedContainerColor = Color.White,
                      unfocusedContainerColor = Color.White,
                      focusedBorderColor = CoralPrimary,
                      unfocusedBorderColor = Color(0xFFD1D5DB),
                      focusedTextColor = DarkSlateDeep,
                      unfocusedTextColor = DarkSlateDeep
                    )
                  )
                }

                TeamType.OTHER -> {
                  OutlinedTextField(
                    value = otherDescription,
                    onValueChange = { otherDescription = it },
                    label = { Text("Description", fontSize = 12.sp) },
                    placeholder = { Text("Describe the other team activity", fontSize = 12.sp, color = SubtleText) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedContainerColor = Color.White,
                      unfocusedContainerColor = Color.White,
                      focusedBorderColor = CoralPrimary,
                      unfocusedBorderColor = Color(0xFFD1D5DB),
                      focusedTextColor = DarkSlateDeep,
                      unfocusedTextColor = DarkSlateDeep
                    )
                  )
                }
                null -> {}
              }
            }
          }
        }
      }

      // 7. Toggle Card: Email setup link
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF181922),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF262837))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF252838)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.MailOutline,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(20.dp)
              )
            }

            Column {
              Text(
                text = "Email setup link",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkSlateDeep
              )
              Text(
                text = "They set their own password",
                fontSize = 12.sp,
                color = SubtleText
              )
            }
          }

          Switch(
            checked = emailSetupLinkEnabled,
            onCheckedChange = { emailSetupLinkEnabled = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = Color(0xFF7C3AED),
              uncheckedThumbColor = Color(0xFF9CA3AF),
              uncheckedTrackColor = Color(0xFFE8D5C4)
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 8. Action Button: "Create account"
      Button(
        onClick = {
          if (fullName.isBlank()) {
            Toast.makeText(context, "Please enter full name", Toast.LENGTH_SHORT).show()
            return@Button
          }
          if (workEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(workEmail.trim()).matches()) {
            Toast.makeText(context, "Please enter a valid work email address", Toast.LENGTH_SHORT).show()
            return@Button
          }
          // Build team-specific data map from the selected team type inputs
          val teamSpecificData = buildTeamSpecificData()
          onCreateAccount(
            fullName,
            workEmail,
            employeeCodePreview, // Pass placeholder (repository will generate real ID)
            joiningDate,
            designation,
            selectedDepartmentCode,
            emailSetupLinkEnabled,
            teamSpecificData
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_create_account"),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Text(
          text = "Create account",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
