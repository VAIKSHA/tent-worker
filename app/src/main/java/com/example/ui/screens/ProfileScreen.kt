package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Settings
import com.example.data.model.Worker
import com.example.ui.Localization
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    workerId: Long,
    viewModel: MainViewModel,
    settings: Settings,
    onNavigateBack: () -> Unit
) {
    val language = settings.language
    val context = LocalContext.current
    val workers by viewModel.workers.collectAsState()
    val allAttendance by viewModel.allAttendanceList.collectAsState()
    val allAdvances by viewModel.allAdvancesList.collectAsState()
    val allPayments by viewModel.allPaymentsList.collectAsState()

    val worker = workers.find { it.id == workerId }

    if (worker == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Worker not found.")
        }
        return
    }

    // Filter data specifically for this worker
    val workerAttendance = allAttendance.filter { it.workerId == workerId }
    val workerAdvances = allAdvances.filter { it.workerId == workerId }
    val workerPayments = allPayments.filter { it.workerId == workerId }

    // Summary calculations
    val summary = viewModel.getWorkerWagesSummary(worker, workerAttendance, workerAdvances, workerPayments)

    // Current Month Calendar Grid Setup
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysList = (1..daysInMonth).toList()

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // WhatsApp Slip sharing function
    val shareSalarySlip = {
        val slipBuilder = StringBuilder()
        slipBuilder.append("=========================================\n")
        slipBuilder.append("    ${settings.businessName} - SALARY SLIP    \n")
        slipBuilder.append("=========================================\n")
        slipBuilder.append("Worker ID: ${worker.formattedWorkerId}\n")
        slipBuilder.append("Name: ${worker.fullName}\n")
        slipBuilder.append("Role: ${worker.skillCategory}\n")
        slipBuilder.append("Daily Wage Rate: ${settings.currency}${worker.dailyWage.toInt()}\n")
        slipBuilder.append("-----------------------------------------\n")
        slipBuilder.append("FINANCIAL OVERVIEW:\n")
        slipBuilder.append("- Gross Wages Earned: ${settings.currency}${summary.totalWages.toInt()}\n")
        slipBuilder.append("- Cash Advances Received: -${settings.currency}${summary.totalAdvance.toInt()}\n")
        slipBuilder.append("- Salary Paid: -${settings.currency}${summary.totalPaid.toInt()}\n")
        slipBuilder.append("-----------------------------------------\n")
        slipBuilder.append("NET PENDING PAYABLE: ${settings.currency}${summary.remainingSalary.toInt()}\n")
        slipBuilder.append("=========================================\n")
        slipBuilder.append("Thank you for your hard work!")

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Salary Slip - ${worker.fullName}")
            putExtra(Intent.EXTRA_TEXT, slipBuilder.toString())
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Salary Slip via"))
        Toast.makeText(context, "Slip generated for sharing!", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Button and Name Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = worker.fullName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${worker.formattedWorkerId} • ${Localization.translate(worker.skillCategory, language)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Basic Profile card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(Localization.translate("worker_details", language), fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(Localization.translate("mobile_no", language), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(worker.mobileNumber, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                        Column {
                            Text(Localization.translate("emergency_contact", language), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(worker.emergencyContact, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }

                    Column {
                        Text(Localization.translate("address", language), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(worker.address, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }

                    if (!worker.notes.isNullOrBlank()) {
                        Column {
                            Text(Localization.translate("notes", language), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(worker.notes, fontWeight = FontWeight.Normal, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Financial ledger card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Earnings & Deductions Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        
                        // WhatsApp slip generator button
                        Button(
                            onClick = { shareSalarySlip() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("share_slip_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share on WhatsApp", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp Slip", fontSize = 10.sp)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Earned", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text("${settings.currency}${summary.totalWages.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column {
                            Text("Total Advance", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text("-${settings.currency}${summary.totalAdvance.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                        }
                        Column {
                            Text("Total Paid", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text("-${settings.currency}${summary.totalPaid.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF10B981))
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Localization.translate("remaining_salary", language), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "${settings.currency}${summary.remainingSalary.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = if (summary.remainingSalary > 0) Color(0xFFD97706) else Color(0xFF10B981)
                        )
                    }
                }
            }
        }

        // Attendance Calendar Card (Custom monthly color grid)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Localization.translate("calendar_view", language), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date()),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Draw Days of the week header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayName ->
                            Text(
                                text = dayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.width(32.dp),
                                style = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simplified Calendar Days representation
                    val chunkedDays = daysList.chunked(7)
                    chunkedDays.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            week.forEach { dayNum ->
                                // Determine day color based on attendance
                                val calInstance = Calendar.getInstance()
                                calInstance.set(currentYear, currentMonth, dayNum)
                                val dateStr = sdf.format(calInstance.time)

                                val dayAttendance = workerAttendance.find { it.date == dateStr }
                                val dayColor = when (dayAttendance?.attendanceType) {
                                    "Present" -> Color(0xFF10B981) // Green
                                    "Absent" -> Color(0xFFEF4444) // Red
                                    "Half Day" -> Color(0xFFF59E0B) // Yellow
                                    "Night Shift" -> Color(0xFF6366F1) // Blue
                                    "Leave" -> Color(0xFF8B5CF6) // Purple
                                    "Holiday" -> Color(0xFF6B7280) // Gray
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(dayColor.copy(alpha = if (dayColor == Color.Transparent) 0f else 0.2f))
                                        .clickable {
                                            if (dayAttendance != null) {
                                                Toast.makeText(context, "Day $dayNum: ${dayAttendance.attendanceType} ${dayAttendance.remarks ?: ""}", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Day $dayNum: Unmarked", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNum.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayColor == Color.Transparent) MaterialTheme.colorScheme.onSurface else dayColor
                                    )
                                }
                            }

                            // Pad empty slots if week is short
                            if (week.size < 7) {
                                repeat(7 - week.size) {
                                    Spacer(modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Legend
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CalendarLegendItem("Present", Color(0xFF10B981))
                        CalendarLegendItem("Absent", Color(0xFFEF4444))
                        CalendarLegendItem("Half Day", Color(0xFFF59E0B))
                        CalendarLegendItem("Night", Color(0xFF6366F1))
                    }
                }
            }
        }

        // Ledger history headers
        item {
            Text("Advances & Payments History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        // History list
        if (workerAdvances.isEmpty() && workerPayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No advances or payments recorded.", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        } else {
            items(workerAdvances) { adv ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Advance: ${Localization.translate(adv.reason, language)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(adv.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Text("-${settings.currency}${adv.amount.toInt()}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(workerPayments) { pay ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Payment: ${Localization.translate(pay.paymentMethod, language)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(pay.paymentDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Text("+${settings.currency}${pay.amountPaid.toInt()}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
    }
}
