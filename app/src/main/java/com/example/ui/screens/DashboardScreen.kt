package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Settings
import com.example.ui.Localization
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    settings: Settings,
    onNavigateToWorkers: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToAdvances: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToReports: () -> Unit,
    onShowQuickAddWorker: () -> Unit,
    onShowQuickGiveAdvance: () -> Unit,
    onShowQuickPaySalary: () -> Unit
) {
    val language = settings.language
    val workers by viewModel.workers.collectAsState()
    val allAttendance by viewModel.allAttendanceList.collectAsState()
    val allAdvances by viewModel.allAdvancesList.collectAsState()
    val allPayments by viewModel.allPaymentsList.collectAsState()

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = sdf.format(Date())

    // Metrics calculations
    val totalWorkersCount = workers.size
    val attendanceToday = allAttendance.filter { it.date == todayStr }
    
    val presentCount = attendanceToday.count { it.attendanceType == "Present" }
    val absentCount = attendanceToday.count { it.attendanceType == "Absent" }
    val nightShiftCount = attendanceToday.count { it.attendanceType == "Night Shift" }
    val halfDayCount = attendanceToday.count { it.attendanceType == "Half Day" }

    // Today's Salary Calculation
    val todaySalary = attendanceToday.sumOf { att ->
        val worker = workers.find { it.id == att.workerId } ?: return@sumOf 0.0
        when (att.attendanceType) {
            "Present" -> worker.dailyWage
            "Half Day" -> worker.halfDayWage
            "Night Shift" -> worker.nightShiftWage
            else -> 0.0
        }
    }

    // Today's Advances Given
    val todayAdvance = allAdvances.filter { it.date == todayStr }.sumOf { it.amount }

    // Overall Outstanding Advances
    val totalOutstandingAdvance = allAdvances.sumOf { it.amount }

    // Pending salary (Cumulative earned salary - Cumulative advances - Cumulative paid)
    var cumulativeEarned = 0.0
    allAttendance.forEach { att ->
        val worker = workers.find { it.id == att.workerId }
        if (worker != null) {
            cumulativeEarned += when (att.attendanceType) {
                "Present" -> worker.dailyWage
                "Half Day" -> worker.halfDayWage
                "Night Shift" -> worker.nightShiftWage
                "Holiday" -> if (settings.holidayRules == "Paid Leave") worker.dailyWage else 0.0
                else -> 0.0
            }
        }
    }
    val cumulativeAdvance = allAdvances.sumOf { it.amount }
    val cumulativePaid = allPayments.sumOf { it.amountPaid }
    val pendingSalary = maxOf(0.0, cumulativeEarned - cumulativeAdvance - cumulativePaid)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Geometric Balance Header - White background, crisp border
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // "ST" Logo
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF005CBB), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ST",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Column {
                    Text(
                        text = settings.businessName.ifEmpty { "Sonu Tent House" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1C1E)
                    )
                    Text(
                        text = "सोनू टेंट हाउस",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Language/Admin state indicators
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language fast toggle
                Button(
                    onClick = {
                        val newLang = if (language == "English") "Hindi" else "English"
                        viewModel.updateSettings(settings.copy(language = newLang))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD3E4FF),
                        contentColor = Color(0xFF001D36)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (language == "English") "हिन्दी" else "EN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Profile Avatar "A" (Admin)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFD3E4FF), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        color = Color(0xFF005CBB),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date and Status Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayDate = SimpleDateFormat("EEEE, d MMMM yyyy", if (language == "Hindi") Locale("hi", "IN") else Locale.US).format(Date())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = displayDate,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }
            
            Box(
                modifier = Modifier
                    .background(Color(0xFFC2EFD0), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = Localization.translate("active", language).uppercase(),
                    color = Color(0xFF072711),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Actions Row
        Text(
            text = Localization.translate("quick_actions", language),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            color = Color(0xFF1A1C1E)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                title = Localization.translate("add_worker", language),
                subTitle = "नया वर्कर",
                isPrimary = true,
                testTag = "quick_add_worker_button",
                onClick = onShowQuickAddWorker,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = Localization.translate("attendance", language),
                subTitle = "हाजिरी",
                isPrimary = false,
                testTag = "quick_mark_attendance_button",
                onClick = onNavigateToAttendance,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "Advance",
                subTitle = "एडवांस",
                isPrimary = false,
                testTag = "quick_give_advance_button",
                onClick = onShowQuickGiveAdvance,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = Localization.translate("payments", language),
                subTitle = "भुगतान",
                isPrimary = false,
                testTag = "quick_pay_salary_button",
                onClick = onShowQuickPaySalary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Grid Header
        Text(
            text = Localization.translate("dashboard", language) + " " + Localization.translate("reports", language),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            color = Color(0xFF1A1C1E)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                MetricCard(
                    title = Localization.translate("total_workers", language),
                    value = totalWorkersCount.toString(),
                    icon = Icons.Default.People,
                    containerColor = Color(0xFFE2E2E9),
                    contentColor = Color(0xFF1A1C1E),
                    iconColor = Color(0xFF5A5C64),
                    onClick = onNavigateToWorkers
                )
            }
            item {
                MetricCard(
                    title = Localization.translate("present_today", language),
                    value = "$presentCount (${Localization.translate("Half Day", language)}: $halfDayCount)",
                    icon = Icons.Default.CheckCircle,
                    containerColor = Color(0xFFD3E4FF),
                    contentColor = Color(0xFF001D36),
                    iconColor = Color(0xFF005CBB),
                    onClick = onNavigateToAttendance
                )
            }
            item {
                MetricCard(
                    title = Localization.translate("absent_today", language),
                    value = absentCount.toString(),
                    icon = Icons.Default.Cancel,
                    containerColor = Color(0xFFF2B8B5),
                    contentColor = Color(0xFF410E0B),
                    iconColor = Color(0xFFB3261E),
                    onClick = onNavigateToAttendance
                )
            }
            item {
                MetricCard(
                    title = Localization.translate("workers_night_shift", language),
                    value = nightShiftCount.toString(),
                    icon = Icons.Default.Nightlight,
                    containerColor = Color(0xFFE0E0FF),
                    contentColor = Color(0xFF1F1F60),
                    iconColor = Color(0xFF6366F1),
                    onClick = onNavigateToAttendance
                )
            }
            item {
                MetricCard(
                    title = Localization.translate("todays_total_salary", language),
                    value = "${settings.currency}${todaySalary.toInt()}",
                    icon = Icons.Default.TrendingUp,
                    containerColor = Color(0xFFC2EFD0),
                    contentColor = Color(0xFF072711),
                    iconColor = Color(0xFF10B981),
                    onClick = onNavigateToReports
                )
            }
            item {
                MetricCard(
                    title = Localization.translate("todays_advance_given", language),
                    value = "${settings.currency}${todayAdvance.toInt()}",
                    icon = Icons.Default.TrendingDown,
                    containerColor = Color(0xFFFFDCC0),
                    contentColor = Color(0xFF2E1500),
                    iconColor = Color(0xFFD97706),
                    onClick = onNavigateToAdvances
                )
            }
            item {
                MetricCard(
                    title = Localization.translate("total_advance_outstanding", language),
                    value = "${settings.currency}${totalOutstandingAdvance.toInt()}",
                    icon = Icons.Default.AccountBalanceWallet,
                    containerColor = Color(0xFFFFECD6),
                    contentColor = Color(0xFF5C2D00),
                    iconColor = Color(0xFFF59E0B),
                    onClick = onNavigateToAdvances
                )
            }
            item {
                MetricCard(
                    title = Localization.translate("pending_salary", language),
                    value = "${settings.currency}${pendingSalary.toInt()}",
                    icon = Icons.Default.CurrencyRupee,
                    containerColor = Color(0xFFFFE3E3),
                    contentColor = Color(0xFF6B1111),
                    iconColor = Color(0xFFDC2626),
                    onClick = onNavigateToPayments
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    subTitle: String,
    isPrimary: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) Color(0xFF005CBB) else Color.White
        ),
        border = if (isPrimary) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF005CBB).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPrimary) Color.White else Color(0xFF005CBB),
                maxLines = 1
            )
            Text(
                text = subTitle,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPrimary) Color.White.copy(alpha = 0.8f) else Color(0xFF005CBB).copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
