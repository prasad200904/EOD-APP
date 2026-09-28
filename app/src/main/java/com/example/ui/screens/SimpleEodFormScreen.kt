package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmployeeEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SimpleEodFormScreen(
    currentEmployee: EmployeeEntity?,
    onSubmit: (project: String, description: String, status: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-fill name from current employee
    val employeeName = currentEmployee?.name ?: "Employee"
    
    // Auto-fill current date using actual system date
    val currentDate = remember {
        mutableStateOf(
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        )
    }
    
    // Auto-fill current time using actual system time
    val currentTime = remember {
        mutableStateOf(
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        )
    }
    
    // Update time every minute
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000) // Update every minute
            currentTime.value = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        }
    }
    
    // Form fields
    var projectTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("In Progress") }
    
    val statusOptions = listOf("In Progress", "Completed", "Blocked")
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B14))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF14151D),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Submit EOD",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${currentDate.value} • ${currentTime.value}",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
            
            // Form Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Employee Name (Read-only)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Employee Name",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF)
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E202C)
                    ) {
                        Text(
                            text = employeeName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                
                // Date (Read-only)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Date",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF)
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E202C)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentDate.value,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                // Project Title
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Project Title",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF)
                    )
                    OutlinedTextField(
                        value = projectTitle,
                        onValueChange = { projectTitle = it },
                        placeholder = { 
                            Text(
                                "Enter project name",
                                color = Color(0xFF6B7280)
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedContainerColor = Color(0xFF1E202C),
                            unfocusedContainerColor = Color(0xFF1E202C)
                        ),
                        singleLine = true
                    )
                }
                
                // Description
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Work Description",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { 
                            Text(
                                "Describe what you worked on today...",
                                color = Color(0xFF6B7280)
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedContainerColor = Color(0xFF1E202C),
                            unfocusedContainerColor = Color(0xFF1E202C)
                        ),
                        maxLines = 6
                    )
                }
                
                // Status Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Status",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        statusOptions.forEach { status ->
                            val isSelected = selectedStatus == status
                            val bgColor = when {
                                isSelected && status == "Completed" -> Color(0xFF10B981)
                                isSelected && status == "In Progress" -> Color(0xFF818CF8)
                                isSelected && status == "Blocked" -> Color(0xFFEF4444)
                                else -> Color(0xFF1E202C)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .border(
                                        width = if (isSelected) 0.dp else 1.dp,
                                        color = Color(0xFF374151),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedStatus = status },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = status,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Submit Button
                Button(
                    onClick = {
                        if (projectTitle.isNotBlank() && description.isNotBlank()) {
                            onSubmit(projectTitle, description, selectedStatus)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B5CE5),
                        disabledContainerColor = Color(0xFF374151)
                    ),
                    enabled = projectTitle.isNotBlank() && description.isNotBlank()
                ) {
                    Text(
                        text = "Submit EOD",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
