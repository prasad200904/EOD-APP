package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.AppVersion

@Composable
fun UpdateAvailableDialog(
    currentVersion: String,
    updateInfo: AppVersion,
    onDownloadClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = { if (!updateInfo.isMandatory) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !updateInfo.isMandatory,
            dismissOnClickOutside = !updateInfo.isMandatory
        )
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF14151D),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = if (updateInfo.isMandatory) Color(0xFFEF4444) else Color(0xFF5B5CE5),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (updateInfo.isMandatory) Icons.Default.NewReleases else Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Title
                Text(
                    text = if (updateInfo.isMandatory) "Update Required" else "Update Available",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                // Version info
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "New Version: ${updateInfo.versionName}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "Current Version: $currentVersion",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                // Release notes
                if (updateInfo.releaseNotes.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E202C)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "What's New",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = updateInfo.releaseNotes,
                                fontSize = 14.sp,
                                color = Color(0xFFD1D5DB),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Mandatory update warning
                if (updateInfo.isMandatory) {
                    Text(
                        text = "⚠️ This update is mandatory for security and compatibility reasons.",
                        fontSize = 13.sp,
                        color = Color(0xFFFBBF24),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                // Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Download button
                    Button(
                        onClick = onDownloadClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (updateInfo.isMandatory) Color(0xFFEF4444) else Color(0xFF5B5CE5)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Update Now",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Dismiss button (only for non-mandatory updates)
                    if (!updateInfo.isMandatory) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Maybe Later",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckingUpdateDialog(
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF14151D)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF5B5CE5),
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Checking for updates...",
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}
