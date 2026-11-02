package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.StudentEntry
import com.example.data.CounsellorFormData
import com.example.data.CustomFormDataSerializer

/**
 * Counsellor EOD Form
 * Allows adding multiple student entries per submission
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounsellorEodFormScreen(
  employeeName: String,
  onSubmit: (customFormData: String) -> Unit,
  onCancel: () -> Unit
) {
  var students by remember { mutableStateOf(listOf(StudentEntry("", "", "", "", ""))) }
  var showValidationError by remember { mutableStateOf(false) }
  
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Counsellor EOD - $employeeName") },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
    ) {
      Text(
        "Student Counselling Records",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      
      Spacer(modifier = Modifier.height(8.dp))
      
      Text(
        "Add details for each student you counselled today",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      
      Spacer(modifier = Modifier.height(16.dp))
      
      // Student entries list
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        itemsIndexed(students) { index, student ->
          StudentEntryCard(
            index = index + 1,
            student = student,
            onUpdate = { updatedStudent ->
              students = students.toMutableList().apply {
                this[index] = updatedStudent
              }
            },
            onDelete = {
              if (students.size > 1) {
                students = students.toMutableList().apply { removeAt(index) }
              }
            },
            canDelete = students.size > 1
          )
        }
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      // Add Student Button
      OutlinedButton(
        onClick = {
          students = students + StudentEntry("", "", "", "", "")
        },
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Student")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Another Student")
      }
      
      if (showValidationError) {
        Text(
          "Please fill in all student details before submitting",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(vertical = 8.dp)
        )
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onCancel,
          modifier = Modifier.weight(1f)
        ) {
          Text("Cancel")
        }
        
        Button(
          onClick = {
            val allFilled = students.all {
              it.studentName.isNotBlank() &&
              it.universityName.isNotBlank() &&
              it.country.isNotBlank() &&
              it.status.isNotBlank() &&
              it.description.isNotBlank()
            }
            
            if (allFilled) {
              val formData = CounsellorFormData(students)
              val json = CustomFormDataSerializer.serializeCounsellor(formData)
              onSubmit(json)
            } else {
              showValidationError = true
            }
          },
          modifier = Modifier.weight(1f)
        ) {
          Text("Submit EOD")
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentEntryCard(
  index: Int,
  student: StudentEntry,
  onUpdate: (StudentEntry) -> Unit,
  onDelete: () -> Unit,
  canDelete: Boolean
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          "Student #$index",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        
        if (canDelete) {
          IconButton(onClick = onDelete) {
            Icon(
              Icons.Default.Delete,
              contentDescription = "Remove Student",
              tint = MaterialTheme.colorScheme.error
            )
          }
        }
      }
      
      Spacer(modifier = Modifier.height(12.dp))
      
      // Student Name
      OutlinedTextField(
        value = student.studentName,
        onValueChange = { onUpdate(student.copy(studentName = it)) },
        label = { Text("Student Name *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )
      
      Spacer(modifier = Modifier.height(8.dp))
      
      // University Name
      OutlinedTextField(
        value = student.universityName,
        onValueChange = { onUpdate(student.copy(universityName = it)) },
        label = { Text("University Name *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )
      
      Spacer(modifier = Modifier.height(8.dp))
      
      // Country Dropdown
      var expanded by remember { mutableStateOf(false) }
      val countries = listOf("USA", "UK", "Canada", "Australia", "Germany", "France", "Other")
      
      ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
      ) {
        OutlinedTextField(
          value = student.country,
          onValueChange = {},
          readOnly = true,
          label = { Text("Country *") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(),
          colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        
        ExposedDropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false }
        ) {
          countries.forEach { country ->
            DropdownMenuItem(
              text = { Text(country) },
              onClick = {
                onUpdate(student.copy(country = country))
                expanded = false
              }
            )
          }
        }
      }
      
      Spacer(modifier = Modifier.height(8.dp))
      
      // Status Dropdown
      var statusExpanded by remember { mutableStateOf(false) }
      val statuses = listOf(
        "Applied",
        "Interview Scheduled",
        "Accepted",
        "Rejected",
        "In Progress",
        "Documents Pending",
        "Visa Applied"
      )
      
      ExposedDropdownMenuBox(
        expanded = statusExpanded,
        onExpandedChange = { statusExpanded = !statusExpanded }
      ) {
        OutlinedTextField(
          value = student.status,
          onValueChange = {},
          readOnly = true,
          label = { Text("Application Status *") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(),
          colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        
        ExposedDropdownMenu(
          expanded = statusExpanded,
          onDismissRequest = { statusExpanded = false }
        ) {
          statuses.forEach { status ->
            DropdownMenuItem(
              text = { Text(status) },
              onClick = {
                onUpdate(student.copy(status = status))
                statusExpanded = false
              }
            )
          }
        }
      }
      
      Spacer(modifier = Modifier.height(8.dp))
      
      // Description
      OutlinedTextField(
        value = student.description,
        onValueChange = { onUpdate(student.copy(description = it)) },
        label = { Text("Description / Notes *") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5,
        placeholder = { Text("What was discussed, next steps, documents needed, etc.") }
      )
    }
  }
}
