package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdvancePayment
import com.example.data.model.Settings
import com.example.data.model.Worker
import com.example.ui.Localization
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancesScreen(
    viewModel: MainViewModel,
    settings: Settings,
    showAddFormInitially: Boolean = false,
    onFormClosed: () -> Unit = {}
) {
    val language = settings.language
    val workers by viewModel.workers.collectAsState()
    val allAdvances by viewModel.allAdvancesList.collectAsState()

    var showAddDialog by remember { mutableStateOf(showAddFormInitially) }
    var selectedWorkerId by remember { mutableStateOf<Long?>(null) }
    var amountText by remember { mutableStateOf("") }
    var reasonSelected by remember { mutableStateOf("Home Expense") }
    var noteText by remember { mutableStateOf("") }

    var advanceToDelete by remember { mutableStateOf<AdvancePayment?>(null) }

    val reasons = listOf("Medical", "Travel", "Home Expense", "Festival", "Personal", "Food", "Other")

    if (showAddFormInitially) {
        LaunchedEffect(Unit) {
            showAddDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.translate("advances", language),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_advance_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Record Advance")
                }
            }

            // List of recorded advances
            if (allAdvances.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No advances recorded yet.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allAdvances) { advance ->
                        val worker = workers.find { it.id == advance.workerId }
                        if (worker != null) {
                            AdvanceHistoryCard(
                                worker = worker,
                                advance = advance,
                                currency = settings.currency,
                                language = language,
                                onDelete = { advanceToDelete = advance }
                            )
                        }
                    }
                }
            }
        }

        // New Advance Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    onFormClosed()
                },
                title = { Text(Localization.translate("give_advance", language)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Select Worker", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        // Simple worker dropdown representation (LazyColumn list or Box selector)
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        val activeWorkers = workers.filter { it.status == "Active" }
                        val currentWorker = activeWorkers.find { it.id == selectedWorkerId }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dropdownExpanded = !dropdownExpanded }
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                                .testTag("advance_worker_dropdown")
                        ) {
                            Text(
                                text = currentWorker?.fullName ?: "Select active worker...",
                                color = if (currentWorker != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                        }

                        if (dropdownExpanded) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 120.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                LazyColumn {
                                    items(activeWorkers) { worker ->
                                        DropdownMenuItem(
                                            text = { Text(worker.fullName) },
                                            onClick = {
                                                selectedWorkerId = worker.id
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Amount Text Field
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text(Localization.translate("amount", language) + " (₹)") },
                            modifier = Modifier.fillMaxWidth().testTag("advance_amount_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        // Reason Chips
                        Text(Localization.translate("reason", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            reasons.take(4).forEach { reason ->
                                FilterChip(
                                    selected = reasonSelected == reason,
                                    onClick = { reasonSelected = reason },
                                    label = { Text(Localization.translate(reason, language), fontSize = 10.sp) }
                                )
                            }
                        }

                        // Note text field
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text(Localization.translate("notes", language)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val wId = selectedWorkerId
                            val amt = amountText.toDoubleOrNull()
                            if (wId != null && amt != null && amt > 0) {
                                val sdfStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                viewModel.giveAdvance(wId, amt, sdfStr, reasonSelected, noteText.ifBlank { null })
                                showAddDialog = false
                                onFormClosed()
                            }
                        },
                        modifier = Modifier.testTag("advance_save_submit")
                    ) {
                        Text(Localization.translate("save", language))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddDialog = false
                        onFormClosed()
                    }) {
                        Text(Localization.translate("cancel", language))
                    }
                }
            )
        }

        // Delete Advance Confirmation
        if (advanceToDelete != null) {
            AlertDialog(
                onDismissRequest = { advanceToDelete = null },
                title = { Text(Localization.translate("confirm_delete", language)) },
                text = { Text(Localization.translate("confirm_delete_msg", language)) },
                confirmButton = {
                    TextButton(
                        modifier = Modifier.testTag("confirm_delete_advance_yes"),
                        onClick = {
                            viewModel.deleteAdvance(advanceToDelete!!)
                            advanceToDelete = null
                        }
                    ) {
                        Text(Localization.translate("yes", language), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { advanceToDelete = null }) {
                        Text(Localization.translate("no", language))
                    }
                }
            )
        }
    }
}

@Composable
fun AdvanceHistoryCard(
    worker: Worker,
    advance: AdvancePayment,
    currency: String,
    language: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(worker.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "${Localization.translate(advance.reason, language)} • ${advance.date}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (!advance.notes.isNullOrBlank()) {
                        Text(advance.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, maxLines = 1)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-$currency${advance.amount.toInt()}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Advance", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
