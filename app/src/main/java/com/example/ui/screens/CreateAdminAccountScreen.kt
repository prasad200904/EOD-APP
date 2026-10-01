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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EodDarkBackground
import com.example.ui.viewmodel.WorkCoreViewModel

/**
 * First-time setup screen for creating the initial admin account
 * Only shown when no admin exists in the system
 */
@Composable
fun CreateAdminAccountScreen(
  viewModel: WorkCoreViewModel,
  onAdminCreated: () -> Unit
) {
  var username by remember { mutableStateOf("admin") }
  var fullName by remember { mutableStateOf("System Administrator") }
  var email by remember { mutableStateOf("admin@workcore.internal") }
  var phone by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  
  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }
  var showError by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  var isCreating by remember { mutableStateOf(false) }

  fun validateAndCreateAdmin() {
    when {
      username.isBlank() -> {
        errorMessage = "Username is required"
        showError = true
      }
      username.length < 3 -> {
        errorMessage = "Username must be at least 3 characters"
        showError = true
      }
      fullName.isBlank() -> {
        errorMessage = "Full name is required"
        showError = true
      }
      email.isBlank() -> {
        errorMessage = "Email is required"
        showError = true
      }
      !email.contains("@") -> {
        errorMessage = "Please enter a valid email address"
        showError = true
      }
      password.isBlank() -> {
        errorMessage = "Password is required"
        showError = true
      }
      password.length < 8 -> {
        errorMessage = "Password must be at least 8 characters"
        showError = true
      }
      password != confirmPassword -> {
        errorMessage = "Passwords do not match"
        showError = true
      }
      else -> {
        showError = false
        isCreating = true
        viewModel.createAdminAccount(
          username = username,
          fullName = fullName,
          email = email,
          phone = phone,
          password = password
        ) { success, error ->
          isCreating = false
          if (success) {
            onAdminCreated()
          } else {
            errorMessage = error ?: "Failed to create admin account"
            showError = true
          }
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
        .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // App Logo Badge
      Box(
        modifier = Modifier
          .size(64.dp)
          .background(Color(0xFF5B5CE5), shape = RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AdminPanelSettings,
          contentDescription = "Admin Setup",
          tint = Color.White,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Header
      Text(
        text = "Welcome to WorkCore",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "First-Time Setup",
        fontSize = 14.sp,
        color = Color(0xFF9CA3AF),
        fontWeight = FontWeight.Medium
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Info Card
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E3A5F).copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.Top,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF60A5FA),
            modifier = Modifier.size(20.dp)
          )
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "No admin account found",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF93C5FD)
            )
            Text(
              text = "Create the first administrator account to manage teams, employees, and system settings.",
              fontSize = 12.sp,
              color = Color(0xFF9CA3AF),
              lineHeight = 16.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Form Section
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Account Information",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        // Username
        OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("Username", fontSize = 13.sp) },
          placeholder = { Text("admin", fontSize = 13.sp, color = Color(0xFF71717A)) },
          leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF9CA3AF))
          },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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

        // Full Name
        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = { Text("Full Name", fontSize = 13.sp) },
          placeholder = { Text("System Administrator", fontSize = 13.sp, color = Color(0xFF71717A)) },
          leadingIcon = {
            Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF9CA3AF))
          },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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

        // Email
        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email Address", fontSize = 13.sp) },
          placeholder = { Text("admin@company.com", fontSize = 13.sp, color = Color(0xFF71717A)) },
          leadingIcon = {
            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF9CA3AF))
          },
          singleLine = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
          ),
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

        // Phone (Optional)
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number (Optional)", fontSize = 13.sp) },
          placeholder = { Text("+1 (555) 000-0001", fontSize = 13.sp, color = Color(0xFF71717A)) },
          leadingIcon = {
            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF9CA3AF))
          },
          singleLine = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
          ),
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Security",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        Text(
          text = "Create a strong password with at least 8 characters.",
          fontSize = 12.sp,
          color = Color(0xFF9CA3AF)
        )

        // Password
        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Password", fontSize = 13.sp) },
          placeholder = { Text("Minimum 8 characters", fontSize = 13.sp, color = Color(0xFF71717A)) },
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
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
          ),
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

        // Confirm Password
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
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
          ),
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

        Spacer(modifier = Modifier.height(12.dp))

        // Create Admin Button
        Button(
          onClick = { validateAndCreateAdmin() },
          enabled = !isCreating,
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5B5CE5),
            disabledContainerColor = Color(0xFF3B3C55)
          )
        ) {
          if (isCreating) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
          } else {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Create Admin Account",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info text
        Text(
          text = "You'll use this account to log in and manage the entire system.",
          fontSize = 11.sp,
          color = Color(0xFF71717A),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
