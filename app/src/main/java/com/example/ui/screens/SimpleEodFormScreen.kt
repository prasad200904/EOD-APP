package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.SimpleFormData
import com.example.data.CustomFormDataSerializer

/**
 * Simple EOD Form
 * Only requires a description - for "Other" team members
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleEodFormScreen(
  employeeName: String,
  onSubmit: (customFormData: String) -> Unit,
  onCancel: () -> Unit
) {
  var description by remember { mutableStateOf("") }
  var showValidationError by remember { mutableStateOf(false) }
  
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Daily EOD - $employeeName") },
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
        .verticalScroll(rememberScrollState())
    ) {
      Text(
        "Today's Work Summary",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      
      Spacer(modifier = Modifier.height(8.dp))
      
      Text(
        "Provide a summary of your work today",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      
      Spacer(modifier = Modifier.height(24.dp))
      
      // Description Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            "📝 Work Description",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          
          Spacer(modifier = Modifier.height(12.dp))
          
          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 10,
            maxLines = 15,
            placeholder = { 
              Text(
                "Describe your work today:\n" +
                "• Tasks completed\n" +
                "• Progress made\n" +
                "• Challenges faced\n" +
                "• Next steps\n" +
                "• Any important updates"
              ) 
            }
          )
          
          Spacer(modifier = Modifier.height(8.dp))
          
          Text(
            "Minimum 50 characters required",
            style = MaterialTheme.typography.bodySmall,
            color = if (description.length >= 50) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            }
          )
        }
      }
      
      if (showValidationError) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          "Please provide a detailed description (minimum 50 characters)",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall
        )
      }
      
      Spacer(modifier = Modifier.height(24.dp))
      
      // Tips Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            "💡 Tips for a Good EOD",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          
          Spacer(modifier = Modifier.height(8.dp))
          
          Text(
            "• Be specific about what you accomplished\n" +
            "• Mention any blockers or challenges\n" +
            "• Note any help or resources needed\n" +
            "• Include measurable outcomes if applicable\n" +
            "• Mention collaboration with other teams",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
        }
      }
      
      Spacer(modifier = Modifier.height(24.dp))
      
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
            if (description.length >= 50) {
              val formData = SimpleFormData(description = description.trim())
              val json = CustomFormDataSerializer.serializeSimple(formData)
              onSubmit(json)
            } else {
              showValidationError = true
            }
          },
          modifier = Modifier.weight(1f),
          enabled = description.length >= 50
        ) {
          Text("Submit EOD")
        }
      }
    }
  }
}
