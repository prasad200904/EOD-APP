package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WarmCream
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSlateDeep
import com.example.ui.theme.SubtleText
import com.example.ui.theme.WarmCardBorder
import com.example.ui.viewmodel.WorkCoreViewModel

/**
 * Screen for Admin to create new teams with unique passwords
 * Accessible only by Admin role
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeamScreen(
  viewModel: WorkCoreViewModel,
  onNavigateBack: () -> Unit
) {
  var teamName by remember { mutableStateOf("") }
  var department by remember { mutableStateOf("") }
  var departmentCode by remember { mutableStateOf("") }
  var managerId by remember { mutableStateOf("") }
  var managerName by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var projects by remember { mutableStateOf("") }
  
  var showError by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  var isCreating by remember { mutableStateOf(false) }

  val departments by viewModel.departments.collectAsState()
  var showDepartmentDropdown by remember { mutableStateOf(false) }

  fun validateAndCreateTeam() {
    when {
      teamName.isBlank() -> {
        errorMessage = "Team name is required"
        showError = true
      }
      department.isBlank() -> {
        errorMessage = "Department is required"
        showError = true
      }
      departmentCode.isBlank() -> {
        errorMessage = "Department code is required"
        showError = true
      }
      managerId.isBlank() -> {
        errorMessage = "Manager ID is required"
        showError = true
      }
      managerName.isBlank() -> {
        errorMessage = "Manager name is required"
        showError = true
      }
      else -> {
        showError = false
        isCreating = true
        viewModel.createTeam(
          teamName = teamName,
          department = department,
          departmentCode = departmentCode,
          managerId = managerId,
          managerName = managerName,
          teamPassword = "",
          description = description,
          projects = projects
        ) { success, error ->
          isCreating = false
          if (success) {
            onNavigateBack()
          } else {
            errorMessage = error ?: "Failed to create team"
            showError = true
          }
        }
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Create New Team", fontWeight = FontWeight.Bold, color = DarkSlateDeep) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkSlateDeep)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color.White,
          titleContentColor = DarkSlateDeep,
          navigationIconContentColor = DarkSlateDeep
        )
      )
    },
    containerColor = WarmCream
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header
      Text(
        text = "Team Information",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = DarkSlateDeep
      )

      Text(
        text = "Create a new team with a unique password for department-based access control.",
        fontSize = 13.sp,
        color = SubtleText,
        lineHeight = 18.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Team Name
      OutlinedTextField(
        value = teamName,
        onValueChange = { teamName = it },
        label = { Text("Team Name", fontSize = 13.sp) },
        placeholder = { Text("e.g., ML Team, GT Team", fontSize = 13.sp, color = SubtleText) },
        leadingIcon = {
          Icon(Icons.Default.Group, contentDescription = null, tint = SubtleText)
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color.White,
          unfocusedContainerColor = Color.White,
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = Color(0xFFD1D5DB),
          focusedTextColor = DarkSlateDeep,
          unfocusedTextColor = DarkSlateDeep,
          focusedLabelColor = SubtleText,
          unfocusedLabelColor = SubtleText
        )
      )

      // Department Selection
      ExposedDropdownMenuBox(
        expanded = showDepartmentDropdown,
        onExpandedChange = { showDepartmentDropdown = it }
      ) {
        OutlinedTextField(
          value = department,
          onValueChange = {},
          readOnly = true,
          label = { Text("Department", fontSize = 13.sp) },
          placeholder = { Text("Select department", fontSize = 13.sp, color = SubtleText) },
          leadingIcon = {
            Icon(Icons.Default.Business, contentDescription = null, tint = SubtleText)
          },
          trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDepartmentDropdown)
          },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = CoralPrimary,
            unfocusedBorderColor = Color(0xFFD1D5DB),
            focusedTextColor = DarkSlateDeep,
            unfocusedTextColor = DarkSlateDeep,
            focusedLabelColor = SubtleText,
            unfocusedLabelColor = SubtleText
          )
        )
        ExposedDropdownMenu(
          expanded = showDepartmentDropdown,
          onDismissRequest = { showDepartmentDropdown = false },
          modifier = Modifier.background(Color.White)
        ) {
          departments.forEach { dept ->
            DropdownMenuItem(
              text = { Text("${dept.name} (${dept.code})", color = DarkSlateDeep) },
              onClick = {
                department = dept.code
                departmentCode = dept.code
                showDepartmentDropdown = false
              }
            )
          }
        }
      }

      // Department Code
      OutlinedTextField(
        value = departmentCode,
        onValueChange = { departmentCode = it },
        label = { Text("Department Code", fontSize = 13.sp) },
        placeholder = { Text("e.g., ML, GT, DB", fontSize = 13.sp, color = SubtleText) },
        leadingIcon = {
          Icon(Icons.Default.Code, contentDescription = null, tint = SubtleText)
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color.White,
          unfocusedContainerColor = Color.White,
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = Color(0xFFD1D5DB),
          focusedTextColor = DarkSlateDeep,
          unfocusedTextColor = DarkSlateDeep,
          focusedLabelColor = SubtleText,
          unfocusedLabelColor = SubtleText
        )
      )

      Divider(color = WarmCardBorder, thickness = 1.dp)

      // Manager Information
      Text(
        text = "Manager Information",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = DarkSlateDeep
      )

      OutlinedTextField(
        value = managerId,
        onValueChange = { managerId = it },
        label = { Text("Manager Employee ID", fontSize = 13.sp) },
        placeholder = { Text("e.g., EMP001", fontSize = 13.sp, color = SubtleText) },
        leadingIcon = {
          Icon(Icons.Default.Badge, contentDescription = null, tint = SubtleText)
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color.White,
          unfocusedContainerColor = Color.White,
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = Color(0xFFD1D5DB),
          focusedTextColor = DarkSlateDeep,
          unfocusedTextColor = DarkSlateDeep,
          focusedLabelColor = SubtleText,
          unfocusedLabelColor = SubtleText
        )
      )

      OutlinedTextField(
        value = managerName,
        onValueChange = { managerName = it },
        label = { Text("Manager Name", fontSize = 13.sp) },
        placeholder = { Text("e.g., John Doe", fontSize = 13.sp, color = SubtleText) },
        leadingIcon = {
          Icon(Icons.Default.Person, contentDescription = null, tint = SubtleText)
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color.White,
          unfocusedContainerColor = Color.White,
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = Color(0xFFD1D5DB),
          focusedTextColor = DarkSlateDeep,
          unfocusedTextColor = DarkSlateDeep,
          focusedLabelColor = SubtleText,
          unfocusedLabelColor = SubtleText
        )
      )

      Divider(color = WarmCardBorder, thickness = 1.dp)

      // Optional Fields
      Text(
        text = "Additional Information (Optional)",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = DarkSlateDeep
      )

      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Team Description", fontSize = 13.sp) },
        placeholder = { Text("Brief description of team purpose", fontSize = 13.sp, color = SubtleText) },
        leadingIcon = {
          Icon(Icons.Default.Description, contentDescription = null, tint = SubtleText)
        },
        minLines = 2,
        maxLines = 4,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color.White,
          unfocusedContainerColor = Color.White,
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = Color(0xFFD1D5DB),
          focusedTextColor = DarkSlateDeep,
          unfocusedTextColor = DarkSlateDeep,
          focusedLabelColor = SubtleText,
          unfocusedLabelColor = SubtleText
        )
      )

      OutlinedTextField(
        value = projects,
        onValueChange = { projects = it },
        label = { Text("Projects", fontSize = 13.sp) },
        placeholder = { Text("Comma-separated project names", fontSize = 13.sp, color = SubtleText) },
        leadingIcon = {
          Icon(Icons.Default.Work, contentDescription = null, tint = SubtleText)
        },
        minLines = 2,
        maxLines = 4,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color.White,
          unfocusedContainerColor = Color.White,
          focusedBorderColor = CoralPrimary,
          unfocusedBorderColor = Color(0xFFD1D5DB),
          focusedTextColor = DarkSlateDeep,
          unfocusedTextColor = DarkSlateDeep,
          focusedLabelColor = SubtleText,
          unfocusedLabelColor = SubtleText
        )
      )

      // Error Message
      if (showError) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          color = Color(0x33EF4444),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
            Text(text = errorMessage, fontSize = 13.sp, color = Color(0xFFB91C1C))
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Create Team Button
      Button(
        onClick = { validateAndCreateTeam() },
        enabled = !isCreating,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = CoralPrimary,
          disabledContainerColor = CoralPrimary.copy(alpha = 0.5f)
        )
      ) {
        if (isCreating) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color.White,
            strokeWidth = 2.dp
          )
        } else {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Create Team",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
