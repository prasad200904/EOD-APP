package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.TelecallingFormData
import com.example.data.CustomFormDataSerializer

/**
 * Telecalling EOD Form
 * Captures call metrics: calls made, follow-ups, conversions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelecallingEodFormScreen(
  employeeName: String,
  onSubmit: (customFormData: String) -> Unit,
  onCancel: () -> Unit
) {
  var callsMade by remember { mutableStateOf("") }
  var followUps by remember { mutableStateOf("") }
  var conversions by remember { mutableStateOf("") }
  var status by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var showValidationError by remember { mutableStateOf(false) }
  
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Telecalling EOD - $employeeName") },
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
        "Daily Call Metrics",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      
      Spacer(modifier = Modifier.height(8.dp))
      
      Text(
        "Enter your telecalling performance for today",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      
      Spacer(modifier = Modifier.height(24.dp))
      
      // Calls Made
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            "📞 Calls Made",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = callsMade,
            onValueChange = { if (it.all { char -> char.isDigit() }) callsMade = it },
            label = { Text("Number of Calls *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            placeholder = { Text("e.g., 45") }
          )
        }
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      // Follow-ups
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            "🔄 Follow-up Calls",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = followUps,
            onValueChange = { if (it.all { char -> char.isDigit() }) followUps = it },
            label = { Text("Number of Follow-ups *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            placeholder = { Text("e.g., 12") }
          )
        }
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      // Conversions
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            "✅ Conversions",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = conversions,
            onValueChange = { if (it.all { char -> char.isDigit() }) conversions = it },
            label = { Text("Number of Conversions *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            placeholder = { Text("e.g., 3") },
            supportingText = { Text("Students who confirmed enrollment") }
          )
        }
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      // Status Dropdown
      var statusExpanded by remember { mutableStateOf(false) }
      val statuses = listOf(
        "Target Met",
        "In Progress",
        "Below Target",
        "Exceeded Target"
      )
      
      ExposedDropdownMenuBox(
        expanded = statusExpanded,
        onExpandedChange = { statusExpanded = !statusExpanded }
      ) {
        OutlinedTextField(
          value = status,
          onValueChange = {},
          readOnly = true,
          label = { Text("Status *") },
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
          statuses.forEach { statusOption ->
            DropdownMenuItem(
              text = { Text(statusOption) },
              onClick = {
                status = statusOption
                statusExpanded = false
              }
            )
          }
        }
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      // Description
      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Description / Notes *") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 4,
        maxLines = 6,
        placeholder = { 
          Text("Hot leads, challenges faced, key conversations, successful strategies, etc.") 
        }
      )
      
      // Conversion Rate Display
      if (callsMade.isNotEmpty() && conversions.isNotEmpty()) {
        val calls = callsMade.toIntOrNull() ?: 0
        val conv = conversions.toIntOrNull() ?: 0
        if (calls > 0) {
          val conversionRate = (conv.toFloat() / calls.toFloat() * 100).toInt()
          
          Spacer(modifier = Modifier.height(16.dp))
          
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
              containerColor = when {
                conversionRate >= 10 -> MaterialTheme.colorScheme.primaryContainer
                conversionRate >= 5 -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
              }
            )
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                "Conversion Rate:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                "$conversionRate%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
      
      if (showValidationError) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          "Please fill in all fields before submitting",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall
        )
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
            val allFilled = callsMade.isNotBlank() &&
                           followUps.isNotBlank() &&
                           conversions.isNotBlank() &&
                           status.isNotBlank() &&
                           description.isNotBlank()
            
            if (allFilled) {
              val formData = TelecallingFormData(
                callsMade = callsMade.toIntOrNull() ?: 0,
                followUps = followUps.toIntOrNull() ?: 0,
                conversions = conversions.toIntOrNull() ?: 0,
                status = status,
                description = description
              )
              val json = CustomFormDataSerializer.serializeTelecalling(formData)
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
