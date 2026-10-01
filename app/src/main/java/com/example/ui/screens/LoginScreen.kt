package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
  var selectedDepartment by remember { mutableStateOf("GT Team") }

  // Inputs
  var employeeId by remember { mutableStateOf("GT-001") }
  var employeePassword by remember { mutableStateOf("") }

  var adminUsername by remember { mutableStateOf("admin") }
  var adminPassword by remember { mutableStateOf("") }

  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val departmentCards = remember {
    listOf(
      DepartmentCardItem("GT Team", Icons.Default.ShowChart, "GT-001"),
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
      Role.EMPLOYEE -> {
        viewModel.loginDepartment(selectedDepartment, employeePassword) { success, err ->
          if (success) onLoginSuccess() else errorMessage = err
        }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(EodDarkBackground)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Role Switcher Header (Employee / Manager / Admin)
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF14151D),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232532)),
        modifier = Modifier.padding(bottom = 20.dp)
      ) {
        Row(modifier = Modifier.padding(4.dp)) {
          listOf(Role.EMPLOYEE to "Employee", Role.ADMIN to "Admin").forEach { (role, label) ->
            val isSelected = selectedRole == role
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) Color(0xFF5B5CE5) else Color.Transparent)
                .clickable {
                  selectedRole = role
                  errorMessage = null
                }
                .padding(horizontal = 24.dp, vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF9CA3AF)
              )
            }
          }
        }
      }

      // App Logo Badge: Purple rounded square with building icon
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFF5B5CE5)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.CorporateFare,
          contentDescription = "WorkCore Logo",
          tint = Color.White,
          modifier = Modifier.size(32.dp)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Title & Subtitle
      Text(
        text = "WorkCore",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = when (selectedRole) {
          Role.EMPLOYEE -> "Employee sign in"
          Role.ADMIN -> "Administrator sign in"
        },
        fontSize = 13.sp,
        color = Color(0xFF9CA3AF)
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
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Normal
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
          Role.EMPLOYEE -> {
            // Department Passcode field (one login per department)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "Department Passcode",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
              )
              OutlinedTextField(
                value = employeePassword,
                onValueChange = {
                  employeePassword = it
                  errorMessage = null
                },
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { performLogin() }),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("department_password_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = Color(0xFF14151D),
                  unfocusedContainerColor = Color(0xFF14151D),
                  focusedBorderColor = Color(0xFF5B5CE5),
                  unfocusedBorderColor = Color(0xFF232532),
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                )
              )
              Text(
                text = "One login per department · Dashboard opens as roster",
                fontSize = 11.sp,
                color = Color(0xFF71717A)
              )
            }
          }

          Role.ADMIN -> {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "Admin Username",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
              )
              OutlinedTextField(
                value = adminUsername,
                onValueChange = {
                  adminUsername = it
                  errorMessage = null
                },
                placeholder = { Text("admin", color = Color(0xFF71717A), fontSize = 13.sp) },
                leadingIcon = {
                  Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF9CA3AF))
                },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("admin_username_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = Color(0xFF14151D),
                  unfocusedContainerColor = Color(0xFF14151D),
                  focusedBorderColor = Color(0xFF5B5CE5),
                  unfocusedBorderColor = Color(0xFF232532),
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                )
              )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "Password",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
              )
              OutlinedTextField(
                value = adminPassword,
                onValueChange = {
                  adminPassword = it
                  errorMessage = null
                },
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { performLogin() }),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("admin_password_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = Color(0xFF14151D),
                  unfocusedContainerColor = Color(0xFF14151D),
                  focusedBorderColor = Color(0xFF5B5CE5),
                  unfocusedBorderColor = Color(0xFF232532),
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
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
            color = Color(0xFF38BDF8),
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
                Text(text = msg, fontSize = 12.sp, color = Color(0xFFFCA5A5))
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
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B5CE5))
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
            color = Color(0xFF9CA3AF),
            modifier = Modifier.align(Alignment.CenterHorizontally)
          )
        }
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
            // GT Team
            DemoChip(
              label = "GT Team",
              sub = "5 members",
              modifier = Modifier.weight(1f),
              onClick = {
                selectedRole = Role.EMPLOYEE
                selectedDepartment = "GT Team"
                employeePassword = "gt123"
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
                employeePassword = "write123"
                errorMessage = null
              }
            )

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
  }
}

@Composable
private fun DepartmentPillCard(
  card: DepartmentCardItem,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(if (isSelected) Color(0xFFEDE9FE) else Color(0xFF14151D))
      .border(
        width = 1.dp,
        color = if (isSelected) Color(0xFF818CF8) else Color(0xFF232532),
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
        tint = if (isSelected) Color(0xFF4F46E5) else Color(0xFF9CA3AF),
        modifier = Modifier.size(20.dp)
      )
      Text(
        text = card.name,
        fontSize = 13.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF9CA3AF)
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
      .background(Color(0xFF191A23))
      .border(1.dp, Color(0xFF282A38), RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .padding(vertical = 8.dp, horizontal = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White
      )
      Text(
        text = sub,
        fontSize = 9.sp,
        color = Color(0xFF71717A)
      )
    }
  }
}
