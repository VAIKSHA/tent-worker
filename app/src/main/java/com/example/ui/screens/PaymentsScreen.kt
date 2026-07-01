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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Payment
import com.example.data.model.Settings
import com.example.data.model.Worker
import com.example.ui.Localization
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    viewModel: MainViewModel,
    settings: Settings,
    showAddFormInitially: Boolean = false,
    onFormClosed: () -> Unit = {}
) {
    val language = settings.language
    val workers by viewModel.workers.collectAsState()
    val allPayments by viewModel.allPaymentsList.collectAsState()

    var showAddDialog by remember { mutableStateOf(showAddFormInitially) }
    var selectedWorkerId by remember { mutableStateOf<Long?>(null) }
    var amountText by remember { mutableStateOf("") }
    var paymentMethodSelected by remember { mutableStateOf("Cash") }
    var noteText by remember { mutableStateOf("") }

    var paymentToDelete by remember { mutableStateOf<Payment?>(null) }

    val methods = listOf("Cash", "UPI", "Bank")

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
                    text = Localization.translate("payments", language),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_payment_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Record Payment")
                }
            }

            // List of recorded payments
            if (allPayments.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No payment history recorded yet.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allPayments) { payment ->
                        val worker = workers.find { it.id == payment.workerId }
                        if (worker != null) {
                            PaymentHistoryCard(
                                worker = worker,
                                payment = payment,
                                currency = settings.currency,
                                language = language,
                                onDelete = { paymentToDelete = payment }
                            )
                        }
                    }
                }
            }
        }

        // New Payment Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    onFormClosed()
                },
                title = { Text(Localization.translate("add_payment", language)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Select Worker", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        val activeWorkers = workers.filter { it.status == "Active" }
                        val currentWorker = activeWorkers.find { it.id == selectedWorkerId }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dropdownExpanded = !dropdownExpanded }
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                                .testTag("payment_worker_dropdown")
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
                            label = { Text(Localization.translate("paid_amount", language) + " (₹)") },
                            modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        // Payment Method Chips
                        Text(Localization.translate("payment_method", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            methods.forEach { method ->
                                FilterChip(
                                    selected = paymentMethodSelected == method,
                                    onClick = { paymentMethodSelected = method },
                                    label = { Text(Localization.translate(method, language), fontSize = 12.sp) }
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
                                viewModel.paySalary(wId, amt, sdfStr, paymentMethodSelected, noteText.ifBlank { null })
                                showAddDialog = false
                                onFormClosed()
                            }
                        },
                        modifier = Modifier.testTag("payment_save_submit")
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

        // Delete Payment Confirmation
        if (paymentToDelete != null) {
            AlertDialog(
                onDismissRequest = { paymentToDelete = null },
                title = { Text(Localization.translate("confirm_delete", language)) },
                text = { Text(Localization.translate("confirm_delete_msg", language)) },
                confirmButton = {
                    TextButton(
                        modifier = Modifier.testTag("confirm_delete_payment_yes"),
                        onClick = {
                            viewModel.deletePayment(paymentToDelete!!)
                            paymentToDelete = null
                        }
                    ) {
                        Text(Localization.translate("yes", language), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { paymentToDelete = null }) {
                        Text(Localization.translate("no", language))
                    }
                }
            )
        }
    }
}

@Composable
fun PaymentHistoryCard(
    worker: Worker,
    payment: Payment,
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
                        .background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0284C7))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(worker.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "${Localization.translate(payment.paymentMethod, language)} • ${payment.paymentDate}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (!payment.notes.isNullOrBlank()) {
                        Text(payment.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, maxLines = 1)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$currency${payment.amountPaid.toInt()}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Payment", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
