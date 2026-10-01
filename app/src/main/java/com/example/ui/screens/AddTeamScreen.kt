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
import com.example.ui.theme.EodDarkBackground
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
  var teamPassword by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var projects by remember { mutableStateOf("") }
  
  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }
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
      teamPassword.isBlank() -> {
        errorMessage = "Team password is required"
        showError = true
      }
      teamPassword.length < 5 -> {
        errorMessage = "Password must be at least 5 characters"
        showError = true
      }
      teamPassword != confirmPassword -> {
        errorMessage = "Passwords do not match"
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
          teamPassword = teamPassword,
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
        title = { Text("Create New Team", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color(0xFF14151D),
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White
        )
      )
    },
    containerColor = EodDarkBackground
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
        color = Color.White
      )

      Text(
        text = "Create a new team with a unique password for department-based access control.",
        fontSize = 13.sp,
        color = Color(0xFF9CA3AF),
        lineHeight = 18.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Team Name
      OutlinedTextField(
        value = teamName,
        onValueChange = { teamName = it },
        label = { Text("Team Name", fontSize = 13.sp) },
        placeholder = { Text("e.g., ML Team, GT Team", fontSize = 13.sp, color = Color(0xFF71717A)) },
        leadingIcon = {
          Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF9CA3AF))
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF14151D),
          unfocusedContainerColor = Color(0xFF14151D),
          focusedBorderColor = Color(0xFF5B5CE5),
          unfocusedBorderColor = Color(0xFF232532),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedLabelColor = Color(0xFF9CA3AF),
          unfocusedLabelColor = Color(0xFF71717A)
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
          placeholder = { Text("Select department", fontSize = 13.sp, color = Color(0xFF71717A)) },
          leadingIcon = {
            Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF9CA3AF))
          },
          trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDepartmentDropdown)
          },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF14151D),
            unfocusedContainerColor = Color(0xFF14151D),
            focusedBorderColor = Color(0xFF5B5CE5),
            unfocusedBorderColor = Color(0xFF232532),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color(0xFF9CA3AF),
            unfocusedLabelColor = Color(0xFF71717A)
          )
        )
        ExposedDropdownMenu(
          expanded = showDepartmentDropdown,
          onDismissRequest = { showDepartmentDropdown = false },
          modifier = Modifier.background(Color(0xFF14151D))
        ) {
          departments.forEach { dept ->
            DropdownMenuItem(
              text = { Text("${dept.name} (${dept.code})", color = Color.White) },
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
        placeholder = { Text("e.g., ML, GT, DB", fontSize = 13.sp, color = Color(0xFF71717A)) },
        leadingIcon = {
          Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF9CA3AF))
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF14151D),
          unfocusedContainerColor = Color(0xFF14151D),
          focusedBorderColor = Color(0xFF5B5CE5),
          unfocusedBorderColor = Color(0xFF232532),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedLabelColor = Color(0xFF9CA3AF),
          unfocusedLabelColor = Color(0xFF71717A)
        )
      )

      Divider(color = Color(0xFF232532), thickness = 1.dp)

      // Manager Information
      Text(
        text = "Manager Information",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )

      OutlinedTextField(
        value = managerId,
        onValueChange = { managerId = it },
        label = { Text("Manager Employee ID", fontSize = 13.sp) },
        placeholder = { Text("e.g., EMP001", fontSize = 13.sp, color = Color(0xFF71717A)) },
        leadingIcon = {
          Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF9CA3AF))
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF14151D),
          unfocusedContainerColor = Color(0xFF14151D),
          focusedBorderColor = Color(0xFF5B5CE5),
          unfocusedBorderColor = Color(0xFF232532),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedLabelColor = Color(0xFF9CA3AF),
          unfocusedLabelColor = Color(0xFF71717A)
        )
      )

      OutlinedTextField(
        value = managerName,
        onValueChange = { managerName = it },
        label = { Text("Manager Name", fontSize = 13.sp) },
        placeholder = { Text("e.g., John Doe", fontSize = 13.sp, color = Color(0xFF71717A)) },
        leadingIcon = {
          Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF9CA3AF))
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF14151D),
          unfocusedContainerColor = Color(0xFF14151D),
          focusedBorderColor = Color(0xFF5B5CE5),
          unfocusedBorderColor = Color(0xFF232532),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedLabelColor = Color(0xFF9CA3AF),
          unfocusedLabelColor = Color(0xFF71717A)
        )
      )

      Divider(color = Color(0xFF232532), thickness = 1.dp)

      // Security
      Text(
        text = "Security",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )

      Text(
        text = "Set a unique password for this team. Team members will use this password to access their department dashboard.",
        fontSize = 12.sp,
        color = Color(0xFF9CA3AF),
        lineHeight = 16.sp
      )

      OutlinedTextField(
        value = teamPassword,
        onValueChange = { teamPassword = it },
        label = { Text("Team Password", fontSize = 13.sp) },
        placeholder = { Text("Minimum 5 characters", fontSize = 13.sp, color = Color(0xFF71717A)) },
        leadingIcon = {
          Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF9CA3AF))
        },
        trailingIcon = {
          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
              if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = "Toggle password visibility",
              tint = Color(0xFF71717A)
            )
          }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF14151D),
          unfocusedContainerColor = Color(0xFF14151D),
          focusedBorderColor = Color(0xFF5B5CE5),
          unfocusedBorderColor = Color(0xFF232532),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedLabelColor = Color(0xFF9CA3AF),
          unfocusedLabelColor = Color(0xFF71717A)
        )
      )

      OutlinedTextField(
        value = confirmPassword,
        onValueChange = { confirmPassword = it },
        label = { Text("Confirm Password", fontSize = 13.sp) },
        placeholder = { Text("Re-enter password", fontSize = 13.sp, color = Color(0xFF71717A)) },
        leadingIcon = {
          Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF9CA3AF))
        },
        trailingIcon = {
          IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
            Icon(
              if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = "Toggle password visibility",
              tint = Color(0xFF71717A)
            )
          }
        },
        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF14151D),
          unfocusedContainerColor = Color(0xFF14151D),
          focusedBorderColor = Color(0xFF5B5CE5),
          unfocusedBorderColor = Color(0xFF232532),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedLabelColor = Color(0xFF9CA3AF),
          unfocusedLabelColor = Color(0xFF71717A)
        )
      )

      Divider(color = Color(0xFF232532), thickness = 1.dp)

      // Optional Fields
      Text(
        text = "Additional Information (Optional)",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )

      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Team Description", fontSize = 13.sp) },
        placeholder = { Text("Brief description of team purpose", fontSize = 13.sp, color = Color(0xFF71717A)) },
        leadingIcon = {
          Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF9CA3AF))
        },
        minLines = 2,
        maxLines = 4,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF14151D),
          unfocusedContainerColor = Color(0xFF14151D),
          focusedBorderColor = Color(0xFF5B5CE5),
          unfocusedBorderColor = Color(0xFF232532),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedLabelColor = Color(0xFF9CA3AF),
          unfocusedLabelColor = Color(0xFF71717A)
        )
      )

      OutlinedTextField(
        value = projects,
        onValueChange = { projects = it },
        label = { Text("Projects", fontSize = 13.sp) },
        placeholder = { Text("Comma-separated project names", fontSize = 13.sp, color = Color(0xFF71717A)) },
        leadingIcon = {
          Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF9CA3AF))
        },
        minLines = 2,
        maxLines = 4,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF14151D),
          unfocusedContainerColor = Color(0xFF14151D),
          focusedBorderColor = Color(0xFF5B5CE5),
          unfocusedBorderColor = Color(0xFF232532),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedLabelColor = Color(0xFF9CA3AF),
          unfocusedLabelColor = Color(0xFF71717A)
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
            Text(text = errorMessage, fontSize = 13.sp, color = Color(0xFFFCA5A5))
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
          containerColor = Color(0xFF5B5CE5),
          disabledContainerColor = Color(0xFF3B3C55)
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
