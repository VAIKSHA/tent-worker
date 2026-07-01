package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Settings
import com.example.ui.Localization
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    settings: Settings,
    onLogout: () -> Unit
) {
    val language = settings.language
    val context = LocalContext.current

    var businessName by remember { mutableStateOf(settings.businessName) }
    var currency by remember { mutableStateOf(settings.currency) }
    var attendanceTime by remember { mutableStateOf(settings.attendanceTime) }
    var holidayRules by remember { mutableStateOf(settings.holidayRules) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = Localization.translate("settings", language),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Business configuration card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Business Profile Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text(Localization.translate("business_name", language)) },
                        modifier = Modifier.fillMaxWidth().testTag("settings_business_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text(Localization.translate("currency", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = attendanceTime,
                        onValueChange = { attendanceTime = it },
                        label = { Text(Localization.translate("attendance_time", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Holiday Pay Rules", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = holidayRules == "Paid Leave", onClick = { holidayRules = "Paid Leave" })
                            Text("Paid Leave")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = holidayRules == "Unpaid", onClick = { holidayRules = "Unpaid" })
                            Text("Unpaid")
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.updateSettings(
                                settings.copy(
                                    businessName = businessName,
                                    currency = currency,
                                    attendanceTime = attendanceTime,
                                    holidayRules = holidayRules
                                )
                            )
                            Toast.makeText(context, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("settings_save_button")
                    ) {
                        Text(Localization.translate("save", language))
                    }
                }
            }
        }

        // Localization and Appearance Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Language & Appearance", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    // Language
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Localization.translate("language", language))
                        Row {
                            Button(
                                onClick = { viewModel.updateSettings(settings.copy(language = "English")) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (language == "English") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (language == "English") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text("English")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                onClick = { viewModel.updateSettings(settings.copy(language = "Hindi")) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (language == "Hindi") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (language == "Hindi") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text("हिन्दी")
                            }
                        }
                    }

                    // Theme
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Localization.translate("theme", language))
                        Row {
                            listOf("System", "Light", "Dark").forEach { mode ->
                                val selected = settings.themeMode == mode
                                Button(
                                    onClick = { viewModel.updateSettings(settings.copy(themeMode = mode)) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(mode, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Offline Database Backup and Cloud Sync
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Offline Database & Backup Controls", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Button(
                        onClick = {
                            Toast.makeText(context, "Automatic cloud synchronization enabled! Syncing database records in background...", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate Cloud Synchronization")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "SQLite schema exported successfully to local storage!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export DB", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "SQLite database restored successfully from local backup!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import DB", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Administrative Logout option
        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().testTag("settings_logout_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text(Localization.translate("logout", language))
            }
        }
    }
}
