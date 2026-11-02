package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CollegeVisit
import com.example.data.MarketingFormData
import com.example.data.CustomFormDataSerializer

/**
 * Marketing EOD Form
 * Allows adding multiple college visit entries per submission
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingEodFormScreen(
  employeeName: String,
  onSubmit: (customFormData: String) -> Unit,
  onCancel: () -> Unit
) {
  var visits by remember { mutableStateOf(listOf(CollegeVisit("", "", "", ""))) }
  var showValidationError by remember { mutableStateOf(false) }
  
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Marketing EOD - $employeeName") },
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
        "College Visit Records",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      
      Spacer(modifier = Modifier.height(8.dp))
      
      Text(
        "Add details for each college/institution you visited today",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      
      Spacer(modifier = Modifier.height(16.dp))
      
      // College visit entries list
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        itemsIndexed(visits) { index, visit ->
          CollegeVisitCard(
            index = index + 1,
            visit = visit,
            onUpdate = { updatedVisit ->
              visits = visits.toMutableList().apply {
                this[index] = updatedVisit
              }
            },
            onDelete = {
              if (visits.size > 1) {
                visits = visits.toMutableList().apply { removeAt(index) }
              }
            },
            canDelete = visits.size > 1
          )
        }
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      // Add Visit Button
      OutlinedButton(
        onClick = {
          visits = visits + CollegeVisit("", "", "", "")
        },
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Visit")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Another Visit")
      }
      
      if (showValidationError) {
        Text(
          "Please fill in all visit details before submitting",
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
            val allFilled = visits.all {
              it.collegeName.isNotBlank() &&
              it.personVisited.isNotBlank() &&
              it.designation.isNotBlank() &&
              it.description.isNotBlank()
            }
            
            if (allFilled) {
              val formData = MarketingFormData(visits)
              val json = CustomFormDataSerializer.serializeMarketing(formData)
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

@Composable
fun CollegeVisitCard(
  index: Int,
  visit: CollegeVisit,
  onUpdate: (CollegeVisit) -> Unit,
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
          "Visit #$index",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        
        if (canDelete) {
          IconButton(onClick = onDelete) {
            Icon(
              Icons.Default.Delete,
              contentDescription = "Remove Visit",
              tint = MaterialTheme.colorScheme.error
            )
          }
        }
      }
      
      Spacer(modifier = Modifier.height(12.dp))
      
      // College Name
      OutlinedTextField(
        value = visit.collegeName,
        onValueChange = { onUpdate(visit.copy(collegeName = it)) },
        label = { Text("College / Institution Name *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("e.g., Delhi University, IIT Bombay") }
      )
      
      Spacer(modifier = Modifier.height(8.dp))
      
      // Person Visited
      OutlinedTextField(
        value = visit.personVisited,
        onValueChange = { onUpdate(visit.copy(personVisited = it)) },
        label = { Text("Person Visited *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("e.g., Dr. Rajesh Kumar") }
      )
      
      Spacer(modifier = Modifier.height(8.dp))
      
      // Designation
      OutlinedTextField(
        value = visit.designation,
        onValueChange = { onUpdate(visit.copy(designation = it)) },
        label = { Text("Designation *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("e.g., Dean of Student Affairs, HOD") }
      )
      
      Spacer(modifier = Modifier.height(8.dp))
      
      // Description
      OutlinedTextField(
        value = visit.description,
        onValueChange = { onUpdate(visit.copy(description = it)) },
        label = { Text("Description / Notes *") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5,
        placeholder = { 
          Text("What was discussed, partnership opportunities, follow-up plans, etc.") 
        }
      )
    }
  }
}
