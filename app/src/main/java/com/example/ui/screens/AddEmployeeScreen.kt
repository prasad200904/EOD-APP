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
import com.example.data.DepartmentEntity
import com.example.data.EmployeeEntity
import com.example.ui.theme.EodDarkBackground
import com.example.ui.theme.EodDarkCardBorder
import com.example.ui.theme.EodDarkInputBg
import com.example.ui.theme.EodDarkInputBorder
import com.example.ui.theme.EodPrimaryPurple
import com.example.ui.theme.EodTextMuted
import com.example.ui.theme.EodTextPrimary
import com.example.ui.theme.EodTextSecondary

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
    sendEmailLink: Boolean
  ) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  // Find managed departments for this manager
  val managedCodes = remember(currentManager) {
    if (currentManager == null) setOf("ML", "DB")
    else if (currentManager.role == "ADMIN") setOf("ML", "DB", "GT", "Writing", "Cyber")
    else {
      currentManager.managedDepartments.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()
        .ifEmpty { setOf(currentManager.department) }
    }
  }

  var fullName by remember { mutableStateOf("Meera Iyer") }
  var workEmail by remember { mutableStateOf("meera.iyer@company.com") }
  var selectedDepartmentCode by remember { mutableStateOf("ML") }
  var joiningDate by remember { mutableStateOf("22 Sep") }
  var designation by remember { mutableStateOf("ML Engineer") }
  var emailSetupLinkEnabled by remember { mutableStateOf(true) }

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

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(EodDarkBackground)
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
            tint = EodTextPrimary,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = "Add employee",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = EodTextPrimary
        )
      }

      // 2. Full Name
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Full name",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = EodTextSecondary
        )
        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          placeholder = { Text("e.g. Meera Iyer", color = EodTextMuted, fontSize = 14.sp) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_full_name"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = EodDarkInputBg,
            unfocusedContainerColor = EodDarkInputBg,
            focusedBorderColor = EodPrimaryPurple,
            unfocusedBorderColor = EodDarkCardBorder,
            focusedTextColor = EodTextPrimary,
            unfocusedTextColor = EodTextPrimary
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
          color = EodTextSecondary
        )
        OutlinedTextField(
          value = workEmail,
          onValueChange = { workEmail = it },
          placeholder = { Text("e.g. meera.iyer@company.com", color = EodTextMuted, fontSize = 14.sp) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_work_email"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = EodDarkInputBg,
            unfocusedContainerColor = EodDarkInputBg,
            focusedBorderColor = EodPrimaryPurple,
            unfocusedBorderColor = EodDarkCardBorder,
            focusedTextColor = EodTextPrimary,
            unfocusedTextColor = EodTextPrimary
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
            color = EodTextSecondary
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
              focusedContainerColor = EodDarkInputBg,
              unfocusedContainerColor = EodDarkInputBg,
              focusedBorderColor = EodDarkCardBorder,
              unfocusedBorderColor = EodDarkCardBorder,
              focusedTextColor = EodTextMuted,
              unfocusedTextColor = EodTextMuted
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
            color = EodTextSecondary
          )
          OutlinedTextField(
            value = joiningDate,
            onValueChange = { joiningDate = it },
            placeholder = { Text("22 Sep", color = EodTextMuted, fontSize = 14.sp) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_joining_date"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = EodDarkInputBg,
              unfocusedContainerColor = EodDarkInputBg,
              focusedBorderColor = EodPrimaryPurple,
              unfocusedBorderColor = EodDarkCardBorder,
              focusedTextColor = EodTextPrimary,
              unfocusedTextColor = EodTextPrimary
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
          color = EodTextSecondary
        )
        OutlinedTextField(
          value = designation,
          onValueChange = { designation = it },
          placeholder = { Text("e.g. ML Engineer", color = EodTextMuted, fontSize = 14.sp) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_designation"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = EodDarkInputBg,
            unfocusedContainerColor = EodDarkInputBg,
            focusedBorderColor = EodPrimaryPurple,
            unfocusedBorderColor = EodDarkCardBorder,
            focusedTextColor = EodTextPrimary,
            unfocusedTextColor = EodTextPrimary
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
            color = EodTextSecondary
          )
          Text(
            text = "You manage ${managedCodes.size}",
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF)
          )
        }

        // Grid of department chips:
        // Row 1: ML, DB
        // Row 2: GT, Writing
        // Row 3: Cyber
        val deptRows = listOf(
          listOf("ML", "DB"),
          listOf("GT", "Writing"),
          listOf("Cyber")
        )

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
                      color = if (isSelected) Color(0xFF60A5FA) else if (isManaged) EodTextPrimary else Color(0xFF6B7280)
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
                color = EodTextPrimary
              )
              Text(
                text = "They set their own password",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
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
              uncheckedTrackColor = Color(0xFF2B2D3A)
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
          if (workEmail.isBlank()) {
            Toast.makeText(context, "Please enter work email", Toast.LENGTH_SHORT).show()
            return@Button
          }
          onCreateAccount(
            fullName,
            workEmail,
            employeeCodePreview, // Pass placeholder (repository will generate real ID)
            joiningDate,
            designation,
            selectedDepartmentCode,
            emailSetupLinkEnabled
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
