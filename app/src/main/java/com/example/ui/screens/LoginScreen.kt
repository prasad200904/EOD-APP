package com.example.ui.screens

import android.widget.Toast
import com.example.ui.theme.WarmCream
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSlateDeep
import com.example.ui.theme.SubtleText
import com.example.ui.theme.MintGreen
import com.example.ui.theme.SelectedBorder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Role
import com.example.ui.theme.EodDarkBackground
import com.example.ui.viewmodel.WorkCoreViewModel

data class DepartmentCardItem(
  val name: String,
  val icon: ImageVector,
  val defaultEmployeeId: String
)

/**
 * Screen 0: WorkCore Unified Login
 * Matches user's uploaded mockup (Image 1) with exact fidelity:
 * - Obsidian dark background
 * - CorporateFare icon in purple rounded badge
 * - "WorkCore" / "Employee sign in"
 * - "Select your department" with 5 cards:
 *   [ ML Team ] [ DB Team ]
 *   [ GT Team ] [ Cyber Security ]
 *   [ Writing Team ]
 * - Selected card has soft lavender background & purple icon/text
 * - Employee ID input field with person icon
 * - Password input field with lock icon and eye toggle
 * - "Forgot password?" link
 * - "Sign in to [Dept] Team" button
 * - "You'll only see [Dept] data" caption
 * - Seamless Role Switcher to toggle Manager / Admin login for testing
 */
@Composable
fun LoginScreen(
  viewModel: WorkCoreViewModel,
  onLoginSuccess: () -> Unit
) {
  val context = LocalContext.current
  val isAuthenticating by viewModel.isAuthenticating.collectAsState()

  var selectedRole by remember { mutableStateOf(Role.EMPLOYEE) }
  var selectedDepartment by remember { mutableStateOf("General Team") }
  var showAOScreen by remember { mutableStateOf(false) }

  // Inputs
  var employeeId by remember { mutableStateOf("GT-001") }
  var employeePassword by remember { mutableStateOf("") }

  var adminUsername by remember { mutableStateOf("admin") }
  var adminPassword by remember { mutableStateOf("") }

  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showResetPasswordDialog by remember { mutableStateOf(false) }
  var resetEmail by remember { mutableStateOf("") }
  var resetMessage by remember { mutableStateOf<String?>(null) }

  val departmentCards = remember {
    listOf(
      DepartmentCardItem("General Team", Icons.Default.ShowChart, "GT-001"),
      DepartmentCardItem("ML Team", Icons.Default.Psychology, "ML-002"),
      DepartmentCardItem("DB Team", Icons.Default.Storage, "DB-001"),
      DepartmentCardItem("Cyber Security", Icons.Default.Security, "EMP010"),
      DepartmentCardItem("Writing Team", Icons.Default.Edit, "EMP005")
    )
  }

  fun performLogin() {
    errorMessage = null
    when (selectedRole) {
      Role.ADMIN -> {
        viewModel.login(
          role = Role.ADMIN,
          teamName = null,
          identifier = adminUsername,
          passwordInput = adminPassword
        ) { success, err ->
          if (success) onLoginSuccess() else errorMessage = err
        }
      }
      Role.MANAGER, Role.EMPLOYEE -> {
        viewModel.loginDepartment(selectedDepartment, employeePassword) { success, err ->
          if (success) onLoginSuccess() else errorMessage = err
        }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        androidx.compose.ui.graphics.Brush.verticalGradient(
          colors = listOf(
            com.example.ui.theme.WarmCream,
            com.example.ui.theme.WarmBlue
          )
        )
      )
  ) {
    // Show AO sign-in screen when AO tab is selected
    if (showAOScreen) {
      AcademicOverseasSignInScreen(
        onNavigateToRoleDashboard = { role, passcode ->
          // Authenticate with the AO role
          // We'll use a special authentication flow for AO
          errorMessage = null
          viewModel.loginAORole(role, passcode) { success, err ->
            if (success) {
              onLoginSuccess()
            } else {
              errorMessage = err
              showAOScreen = false // Stay on AO screen to show error
            }
          }
        },
        onNavigateBack = { showAOScreen = false }
      )
    } else {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Role Switcher Header (Employee / Admin / AO)
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE8F2E8),
        border = androidx.compose.foundation.BorderStroke(2.dp, com.example.ui.theme.SelectedBorder),
        modifier = Modifier.padding(bottom = 20.dp)
      ) {
        Row(modifier = Modifier.padding(4.dp)) {
          listOf(
            Triple(Role.EMPLOYEE, "Employee", false),
            Triple(Role.ADMIN, "Admin", false),
            Triple(Role.EMPLOYEE, "AO", true) // Using Role.EMPLOYEE as placeholder for AO
          ).forEach { (role, label, isAO) ->
            val isSelected = if (isAO) showAOScreen else (selectedRole == role && !showAOScreen)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) com.example.ui.theme.MintGreen else Color.Transparent)
                .clickable {
                  if (isAO) {
                    showAOScreen = true
                  } else {
                    showAOScreen = false
                    selectedRole = role
                  }
                  errorMessage = null
                }
                .padding(horizontal = 24.dp, vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) com.example.ui.theme.DarkSlateDeep else com.example.ui.theme.SubtleText
              )
            }
          }
        }
      }

      // App Logo Badge: Mint green rounded square with building icon
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(com.example.ui.theme.MintGreen),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.CorporateFare,
          contentDescription = "WorkCore Logo",
          tint = com.example.ui.theme.DarkSlateDeep,
          modifier = Modifier.size(32.dp)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Title & Subtitle
      Text(
        text = "WorkCore",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = com.example.ui.theme.DarkSlateDeep
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = when (selectedRole) {
          Role.EMPLOYEE -> "Employee sign in"
          Role.MANAGER -> "Manager sign in"
          Role.ADMIN -> "Administrator sign in"
        },
        fontSize = 13.sp,
        color = com.example.ui.theme.SubtleText
      )

      Spacer(modifier = Modifier.height(26.dp))

      // Department Selection (shown for Employee & Manager)
      if (selectedRole != Role.ADMIN) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Select your department",
            fontSize = 13.sp,
            color = com.example.ui.theme.DarkSlateDeep,
            fontWeight = FontWeight.Medium
          )

          // 2x2 Grid + 1 full-width row
          // Row 1: ML Team & DB Team
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            DepartmentPillCard(
              card = departmentCards[0],
              isSelected = selectedDepartment == departmentCards[0].name,
              modifier = Modifier.weight(1f),
              onClick = {
                selectedDepartment = departmentCards[0].name
                if (selectedRole == Role.EMPLOYEE) employeeId = departmentCards[0].defaultEmployeeId
              }
            )
            DepartmentPillCard(
              card = departmentCards[1],
              isSelected = selectedDepartment == departmentCards[1].name,
              modifier = Modifier.weight(1f),
              onClick = {
                selectedDepartment = departmentCards[1].name
                if (selectedRole == Role.EMPLOYEE) employeeId = departmentCards[1].defaultEmployeeId
              }
            )
          }

          // Row 2: GT Team & Cyber Security
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            DepartmentPillCard(
              card = departmentCards[2],
              isSelected = selectedDepartment == departmentCards[2].name,
              modifier = Modifier.weight(1f),
              onClick = {
                selectedDepartment = departmentCards[2].name
                if (selectedRole == Role.EMPLOYEE) employeeId = departmentCards[2].defaultEmployeeId
              }
            )
            DepartmentPillCard(
              card = departmentCards[3],
              isSelected = selectedDepartment == departmentCards[3].name,
              modifier = Modifier.weight(1f),
              onClick = {
                selectedDepartment = departmentCards[3].name
                if (selectedRole == Role.EMPLOYEE) employeeId = departmentCards[3].defaultEmployeeId
              }
            )
          }

          // Row 3: Writing Team (Full Width)
          DepartmentPillCard(
            card = departmentCards[4],
            isSelected = selectedDepartment == departmentCards[4].name,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              selectedDepartment = departmentCards[4].name
              if (selectedRole == Role.EMPLOYEE) employeeId = departmentCards[4].defaultEmployeeId
            }
          )
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Input Form Fields
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        when (selectedRole) {
          Role.MANAGER, Role.EMPLOYEE -> {
            // Department Passcode field (one login per department)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "Department Passcode",
                fontSize = 13.sp,
                color = com.example.ui.theme.DarkSlateDeep
              )
              OutlinedTextField(
                value = employeePassword,
                onValueChange = {
                  employeePassword = it
                  errorMessage = null
                },
                leadingIcon = {
                  Icon(Icons.Default.Lock, contentDescription = null, tint = com.example.ui.theme.SubtleText)
                },
                trailingIcon = {
                  IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                      if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                      contentDescription = "Toggle password visibility",
                      tint = com.example.ui.theme.SubtleText
                    )
                  }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { performLogin() }),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("department_password_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                  focusedBorderColor = com.example.ui.theme.CoralPrimary,
                  unfocusedBorderColor = Color(0xFFD1D5DB),
                  focusedTextColor = com.example.ui.theme.DarkSlateDeep,
                  unfocusedTextColor = com.example.ui.theme.DarkSlateDeep
                )
              )
              Text(
                text = "One login per department · Dashboard opens as roster",
                fontSize = 11.sp,
                color = com.example.ui.theme.SubtleText
              )
            }
          }

          Role.ADMIN -> {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "Admin Username",
                fontSize = 13.sp,
                color = com.example.ui.theme.DarkSlateDeep
              )
              OutlinedTextField(
                value = adminUsername,
                onValueChange = {
                  adminUsername = it
                  errorMessage = null
                },
                placeholder = { Text("admin", color = com.example.ui.theme.SubtleText, fontSize = 13.sp) },
                leadingIcon = {
                  Icon(Icons.Default.Person, contentDescription = null, tint = com.example.ui.theme.SubtleText)
                },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("admin_username_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                  focusedBorderColor = com.example.ui.theme.CoralPrimary,
                  unfocusedBorderColor = Color(0xFFD1D5DB),
                  focusedTextColor = com.example.ui.theme.DarkSlateDeep,
                  unfocusedTextColor = com.example.ui.theme.DarkSlateDeep
                )
              )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "Password",
                fontSize = 13.sp,
                color = com.example.ui.theme.DarkSlateDeep
              )
              OutlinedTextField(
                value = adminPassword,
                onValueChange = {
                  adminPassword = it
                  errorMessage = null
                },
                leadingIcon = {
                  Icon(Icons.Default.Lock, contentDescription = null, tint = com.example.ui.theme.SubtleText)
                },
                trailingIcon = {
                  IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                      if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                      contentDescription = "Toggle password visibility",
                      tint = com.example.ui.theme.SubtleText
                    )
                  }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { performLogin() }),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("admin_password_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                  focusedBorderColor = com.example.ui.theme.CoralPrimary,
                  unfocusedBorderColor = Color(0xFFD1D5DB),
                  focusedTextColor = com.example.ui.theme.DarkSlateDeep,
                  unfocusedTextColor = com.example.ui.theme.DarkSlateDeep
                )
              )
            }
          }
        }

        // Forgot password link
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          Text(
            text = "Forgot password?",
            fontSize = 12.sp,
            color = com.example.ui.theme.WarmBlue,
            modifier = Modifier
              .clickable {
                Toast.makeText(context, "Default demo password is 'password123' (or 'admin123')", Toast.LENGTH_LONG).show()
              }
              .padding(vertical = 4.dp)
          )
        }

        // Error message banner
        AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
          errorMessage?.let { msg ->
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              color = Color(0x33EF4444),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                Text(text = msg, fontSize = 12.sp, color = Color(0xFFDC2626))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Big Primary Button: "Sign in to [Dept] Team"
        Button(
          onClick = { performLogin() },
          enabled = !isAuthenticating,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("login_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.CoralPrimary)
        ) {
          if (isAuthenticating) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
          } else {
            Text(
              text = when (selectedRole) {
                Role.EMPLOYEE -> "Sign in to $selectedDepartment"
                Role.MANAGER -> "Sign in as Manager ($selectedDepartment)"
                Role.ADMIN -> "Sign in as Admin"
              },
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        // Caption below button: "You'll see all [Dept] members"
        if (selectedRole == Role.EMPLOYEE) {
          Text(
            text = "You'll see all $selectedDepartment members",
            fontSize = 12.sp,
            color = com.example.ui.theme.SubtleText,
            modifier = Modifier.align(Alignment.CenterHorizontally)
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // "Forgot password / Set password" button
        TextButton(
          onClick = {
            resetEmail = if (adminUsername.contains("@")) adminUsername else ""
            resetMessage = null
            showResetPasswordDialog = true
          },
          modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
          Text(
            text = "Forgot password / Set password",
            fontSize = 13.sp,
            color = com.example.ui.theme.CoralPrimary,
            fontWeight = FontWeight.Medium
          )
        }
      }

      if (showResetPasswordDialog) {
        AlertDialog(
          onDismissRequest = { showResetPasswordDialog = false },
          title = {
            Text(
              text = "Reset / Set Password",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = com.example.ui.theme.DarkSlateDeep
            )
          },
          text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = "Enter your registered work email. We'll send you a secure link to set or reset your password.",
                fontSize = 13.sp,
                color = com.example.ui.theme.SubtleText
              )
              OutlinedTextField(
                value = resetEmail,
                onValueChange = { resetEmail = it },
                label = { Text("Work Email") },
                placeholder = { Text("e.g. name@company.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              )
              if (resetMessage != null) {
                Text(
                  text = resetMessage ?: "",
                  fontSize = 12.sp,
                  color = Color(0xFF1D6C2F),
                  fontWeight = FontWeight.Medium
                )
              }
            }
          },
          confirmButton = {
            Button(
              onClick = {
                if (resetEmail.isNotBlank()) {
                  viewModel.sendPasswordReset(resetEmail.trim()) { success, msg ->
                    resetMessage = msg
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.CoralPrimary),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Send Link", color = Color.White)
            }
          },
          dismissButton = {
            TextButton(onClick = { showResetPasswordDialog = false }) {
              Text("Close", color = com.example.ui.theme.SubtleText)
            }
          }
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Quick 1-Tap Demo Switcher (Instant test testing)
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF14151D),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232532))
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Demo Accounts (1-Tap Fast Switch)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9CA3AF)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // General Team
            DemoChip(
              label = "General Team",
              sub = "5 members",
              modifier = Modifier.weight(1f),
              onClick = {
                selectedRole = Role.EMPLOYEE
                selectedDepartment = "General Team"
                employeePassword = "general123"
                errorMessage = null
              }
            )

            // ML Team
            DemoChip(
              label = "ML Team",
              sub = "5 members",
              modifier = Modifier.weight(1f),
              onClick = {
                selectedRole = Role.EMPLOYEE
                selectedDepartment = "ML Team"
                employeePassword = "ml123"
                errorMessage = null
              }
            )

            // DB Team
            DemoChip(
              label = "DB Team",
              sub = "4 members",
              modifier = Modifier.weight(1f),
              onClick = {
                selectedRole = Role.EMPLOYEE
                selectedDepartment = "DB Team"
                employeePassword = "db123"
                errorMessage = null
              }
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Cyber Security
            DemoChip(
              label = "Cyber Security",
              sub = "4 members",
              modifier = Modifier.weight(1f),
              onClick = {
                selectedRole = Role.EMPLOYEE
                selectedDepartment = "Cyber Security"
                employeePassword = "cyber123"
                errorMessage = null
              }
            )

            // Writing Team
            DemoChip(
              label = "Writing Team",
              sub = "4 members",
              modifier = Modifier.weight(1f),
              onClick = {
                selectedRole = Role.EMPLOYEE
                selectedDepartment = "Writing Team"
                employeePassword = "writing123"
                errorMessage = null
              }
            )

            // TEGA Team
            DemoChip(
              label = "TEGA",
              sub = "7 members",
              modifier = Modifier.weight(1f),
              onClick = {
                selectedRole = Role.EMPLOYEE
                selectedDepartment = "TEGA"
                employeePassword = "tega123"
                errorMessage = null
              }
            )
          }

          // Row 4: Admin
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Admin
            DemoChip(
              label = "Admin",
              sub = "admin123",
              modifier = Modifier.weight(1f),
              onClick = {
                selectedRole = Role.ADMIN
                adminUsername = "admin"
                adminPassword = "admin123"
                errorMessage = null
              }
            )
          }
        }
      }
    }
    } // End of else block for showAOScreen
  }
}

@Composable
private fun DepartmentPillCard(
  card: DepartmentCardItem,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  // Special border for Cyber Security when selected
  val borderColor = if (isSelected && card.name == "Cyber Security") {
    com.example.ui.theme.LightCoralBorder
  } else if (isSelected) {
    com.example.ui.theme.SelectedBorder
  } else {
    Color(0xFFD1D5DB)
  }
  
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(if (isSelected) com.example.ui.theme.MintGreen else com.example.ui.theme.DarkSlate)
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(12.dp)
      )
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 12.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Icon(
        imageVector = card.icon,
        contentDescription = card.name,
        tint = if (isSelected) com.example.ui.theme.DarkSlateDeep else Color.White,
        modifier = Modifier.size(20.dp)
      )
      Text(
        text = card.name,
        fontSize = 13.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) com.example.ui.theme.DarkSlateDeep else Color.White
      )
    }
  }
}

@Composable
private fun DemoChip(
  label: String,
  sub: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color.White.copy(alpha = 0.9f))
      .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .padding(vertical = 8.dp, horizontal = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = com.example.ui.theme.DarkSlateDeep
      )
      Text(
        text = sub,
        fontSize = 9.sp,
        color = com.example.ui.theme.SubtleText
      )
    }
  }
}
