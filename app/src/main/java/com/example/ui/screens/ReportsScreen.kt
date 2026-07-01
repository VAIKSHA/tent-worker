package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
fun ReportsScreen(
    viewModel: MainViewModel,
    settings: Settings
) {
    val language = settings.language
    val context = LocalContext.current
    val workers by viewModel.workers.collectAsState()
    val allAttendance by viewModel.allAttendanceList.collectAsState()
    val allAdvances by viewModel.allAdvancesList.collectAsState()
    val allPayments by viewModel.allPaymentsList.collectAsState()

    var selectedPeriod by remember { mutableStateOf("monthly") } // "today", "weekly", "monthly", "yearly"

    // Statistics calculations
    val totalWages = remember(workers, allAttendance, selectedPeriod) {
        var sum = 0.0
        allAttendance.forEach { att ->
            val worker = workers.find { it.id == att.workerId } ?: return@forEach
            sum += when (att.attendanceType) {
                "Present" -> worker.dailyWage
                "Half Day" -> worker.halfDayWage
                "Night Shift" -> worker.nightShiftWage
                "Holiday" -> if (settings.holidayRules == "Paid Leave") worker.dailyWage else 0.0
                else -> 0.0
            }
        }
        sum
    }

    val totalAdvances = remember(allAdvances, selectedPeriod) {
        allAdvances.sumOf { it.amount }
    }

    val totalPaid = remember(allPayments, selectedPeriod) {
        allPayments.sumOf { it.amountPaid }
    }

    val pendingSalary = maxOf(0.0, totalWages - totalAdvances - totalPaid)

    // Calculate skill categories density for chart
    val skillCount = remember(workers) {
        val map = mutableMapOf<String, Int>()
        workers.forEach {
            map[it.skillCategory] = (map[it.skillCategory] ?: 0) + 1
        }
        map
    }

    // Export function utilizing real Android Sharing intent
    val exportAndShareReport = {
        val reportBuilder = StringBuilder()
        reportBuilder.append("=========================================\n")
        reportBuilder.append("  ${settings.businessName} - WORKER REPORT  \n")
        reportBuilder.append("  Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n")
        reportBuilder.append("=========================================\n\n")
        reportBuilder.append("SUMMARY STATS:\n")
        reportBuilder.append("- Total Workers: ${workers.size}\n")
        reportBuilder.append("- Total Earned Wages: ${settings.currency}${totalWages.toInt()}\n")
        reportBuilder.append("- Total Advances Given: ${settings.currency}${totalAdvances.toInt()}\n")
        reportBuilder.append("- Total Payouts Cleared: ${settings.currency}${totalPaid.toInt()}\n")
        reportBuilder.append("- Remaining Pending Balance: ${settings.currency}${pendingSalary.toInt()}\n\n")
        reportBuilder.append("WORKER LEDGER:\n")
        workers.forEach { worker ->
            reportBuilder.append("${worker.formattedWorkerId} | ${worker.fullName} | ${worker.skillCategory} | Wage: ${worker.dailyWage}\n")
        }
        reportBuilder.append("\n=========================================")

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${settings.businessName} Worker Management Report")
            putExtra(Intent.EXTRA_TEXT, reportBuilder.toString())
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Salary Report via"))
        Toast.makeText(context, "Report generated successfully!", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.translate("reports", language),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Button(
                    onClick = { exportAndShareReport() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("export_report_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Export")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Localization.translate("export", language))
                }
            }
        }

        // Time Period Filter chips
        item {
            val periods = listOf("today", "weekly", "monthly", "yearly")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(periods) { period ->
                    val selected = selectedPeriod == period
                    FilterChip(
                        selected = selected,
                        onClick = { selectedPeriod = period },
                        label = { Text(Localization.translate(period, language)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Financial summary cards
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Financial Analytics Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ReportStatItem(label = Localization.translate("total_wages", language), value = "${settings.currency}${totalWages.toInt()}", color = MaterialTheme.colorScheme.primary)
                        ReportStatItem(label = Localization.translate("total_advances", language), value = "${settings.currency}${totalAdvances.toInt()}", color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ReportStatItem(label = Localization.translate("total_paid", language), value = "${settings.currency}${totalPaid.toInt()}", color = Color(0xFF10B981))
                        ReportStatItem(label = Localization.translate("remaining_salary", language), value = "${settings.currency}${pendingSalary.toInt()}", color = Color(0xFFF59E0B))
                    }
                }
            }
        }

        // Beautiful Interactive Canvas chart for skill density representation
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Worker Skill Density Chart", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw custom bars via Canvas
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val totalWorkers = workers.size.toFloat()

                    if (totalWorkers > 0) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val barWidth = 40.dp.toPx()
                            val spacing = 20.dp.toPx()

                            var currentX = spacing
                            skillCount.forEach { (skill, count) ->
                                val heightPct = count.toFloat() / totalWorkers
                                val barHeight = canvasHeight * heightPct * 0.7f

                                // Draw bar
                                drawRect(
                                    color = primaryColor,
                                    topLeft = Offset(currentX, canvasHeight - barHeight),
                                    size = Size(barWidth, barHeight)
                                )

                                currentX += barWidth + spacing
                            }
                        }
                    }

                    // Legends
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        skillCount.forEach { (skill, count) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.primary))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("$skill: $count workers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // Top working workers list
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = Localization.translate("top_workers", language),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(workers.take(3)) { worker ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(worker.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(worker.skillCategory, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Active Streak", color = Color(0xFF0369A1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(modifier = Modifier.width(130.dp)) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, maxLines = 1)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
