package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WarmCream
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSlateDeep
import com.example.ui.theme.SubtleText
import com.example.ui.theme.WarmCardBorder
import com.example.ui.theme.MintGreen

@Composable
fun AcademicOverseasSignInScreen(
  onNavigateToRoleDashboard: (role: String, passcode: String) -> Unit,
  onNavigateBack: () -> Unit
) {
  var selectedRole by remember { mutableStateOf("Marketing") }
  var passcode by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Warm light theme colors
  val backgroundColor = WarmCream
  val cardColor = Color.White
  val primaryColor = CoralPrimary
  val textPrimary = DarkSlateDeep
  val textSecondary = SubtleText

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(backgroundColor)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Tab Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
      ) {
        TabButton(
          text = "Employee",
          isSelected = false,
          onClick = onNavigateBack,
          backgroundColor = cardColor,
          selectedColor = primaryColor,
          textColor = textSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        TabButton(
          text = "Admin",
          isSelected = false,
          onClick = onNavigateBack,
          backgroundColor = cardColor,
          selectedColor = primaryColor,
          textColor = textSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        TabButton(
          text = "AO",
          isSelected = true,
          onClick = {},
          backgroundColor = cardColor,
          selectedColor = primaryColor,
          textColor = textPrimary
        )
      }

      Spacer(modifier = Modifier.height(40.dp))

      // Building Icon
      Box(
        modifier = Modifier
          .size(80.dp)
          .background(primaryColor, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Business,
          contentDescription = "Building",
          tint = Color.White,
          modifier = Modifier.size(40.dp)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Title
      Text(
        text = "WorkCore",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = textPrimary
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Subtitle
      Text(
        text = "Academic Overseas sign in",
        fontSize = 16.sp,
        color = textSecondary
      )

      Spacer(modifier = Modifier.height(40.dp))

      // Select your role label
      Text(
        text = "Select your role",
        fontSize = 14.sp,
        color = textSecondary,
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 4.dp, bottom = 12.dp)
      )

      // Role Selection Buttons - Row 1
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        RoleButton(
          role = "Marketing",
          icon = Icons.Default.Campaign,
          isSelected = selectedRole == "Marketing",
          onClick = { selectedRole = "Marketing" },
          modifier = Modifier.weight(1f),
          backgroundColor = cardColor,
          selectedColor = primaryColor,
          textColor = textPrimary
        )
        RoleButton(
          role = "Counsellor",
          icon = Icons.Default.Chat,
          isSelected = selectedRole == "Counsellor",
          onClick = { selectedRole = "Counsellor" },
          modifier = Modifier.weight(1f),
          backgroundColor = cardColor,
          selectedColor = primaryColor,
          textColor = textPrimary
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Role Selection Buttons - Row 2
      RoleButton(
        role = "Tele Caller",
        icon = Icons.Default.Phone,
        isSelected = selectedRole == "Tele Caller",
        onClick = { selectedRole = "Tele Caller" },
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardColor,
        selectedColor = primaryColor,
        textColor = textPrimary
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Role Passcode label
      Text(
        text = "Role Passcode",
        fontSize = 14.sp,
        color = textSecondary,
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 4.dp, bottom = 12.dp)
      )

      // Passcode Input
      OutlinedTextField(
        value = passcode,
        onValueChange = {
          passcode = it
          errorMessage = null
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(64.dp),
        placeholder = {
          Text(
            text = "Enter passcode",
            color = textSecondary.copy(alpha = 0.6f)
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Lock",
            tint = textSecondary
          )
        },
        trailingIcon = {
          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
              imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = if (passwordVisible) "Hide password" else "Show password",
              tint = textSecondary
            )
          }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = textPrimary,
          unfocusedTextColor = textPrimary,
          focusedContainerColor = cardColor,
          unfocusedContainerColor = cardColor,
          focusedBorderColor = primaryColor,
          unfocusedBorderColor = WarmCardBorder
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Helper text
      Text(
        text = "One login per role · Dashboard opens as roster",
        fontSize = 12.sp,
        color = textSecondary,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Forgot password link
      TextButton(
        onClick = { /* Handle forgot password */ },
        modifier = Modifier.align(Alignment.End)
      ) {
        Text(
          text = "Forgot passcode?",
          color = primaryColor,
          fontSize = 14.sp
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Error message
      if (errorMessage != null) {
        Text(
          text = errorMessage ?: "",
          color = Color(0xFFEF4444),
          fontSize = 14.sp,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
          textAlign = TextAlign.Center
        )
      }

      // Sign in button
      Button(
        onClick = {
          if (passcode.isBlank()) {
            errorMessage = "Please enter passcode"
          } else {
            // Validate passcode
            val validPasscodes = mapOf(
              "Marketing" to "marketing123",
              "Counsellor" to "counsellor123",
              "Tele Caller" to "telecaller123"
            )
            
            if (validPasscodes[selectedRole] == passcode) {
              onNavigateToRoleDashboard(selectedRole, passcode)
            } else {
              errorMessage = "Invalid passcode for $selectedRole"
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = primaryColor
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(
          text = "Sign in to $selectedRole",
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color.White
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Footer text
      Text(
        text = "You'll see all $selectedRole members",
        fontSize = 13.sp,
        color = textSecondary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun TabButton(
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  backgroundColor: Color,
  selectedColor: Color,
  textColor: Color
) {
  Button(
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(
      containerColor = if (isSelected) selectedColor else backgroundColor,
      contentColor = if (isSelected) Color.White else textColor
    ),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .width(100.dp)
      .height(48.dp)
  ) {
    Text(
      text = text,
      fontSize = 14.sp,
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
    )
  }
}

@Composable
private fun RoleButton(
  role: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  backgroundColor: Color,
  selectedColor: Color,
  textColor: Color
) {
  Button(
    onClick = onClick,
    modifier = modifier
      .height(64.dp)
      .then(
        if (isSelected) {
          Modifier.border(2.dp, selectedColor, RoundedCornerShape(12.dp))
        } else {
          Modifier
        }
      ),
    colors = ButtonDefaults.buttonColors(
      containerColor = if (isSelected) selectedColor.copy(alpha = 0.15f) else backgroundColor,
      contentColor = if (isSelected) selectedColor else textColor
    ),
    shape = RoundedCornerShape(12.dp),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
  ) {
    Row(
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(
        imageVector = icon,
        contentDescription = role,
        modifier = Modifier.size(24.dp),
        tint = if (isSelected) selectedColor else textColor.copy(alpha = 0.7f)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = role,
        fontSize = 15.sp,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
      )
    }
  }
}
