package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.Attendance
import com.example.data.model.Settings
import com.example.data.model.Worker
import com.example.ui.Localization
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceScreen(
    viewModel: MainViewModel,
    settings: Settings
) {
    val language = settings.language
    val context = LocalContext.current
    val workers by viewModel.workers.collectAsState()
    val attendanceList by viewModel.attendanceForSelectedDate.collectAsState()
    val selectedDateStr by viewModel.selectedDate.collectAsState()

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val parsedDate = sdf.parse(selectedDateStr) ?: Date()
    val displayDate = SimpleDateFormat("dd MMMM yyyy (EEEE)", if (language == "Hindi") Locale("hi", "IN") else Locale.US).format(parsedDate)

    // Form states for attendance details
    var selectedWorkerForDetails by remember { mutableStateOf<Worker?>(null) }
    var detailCheckIn by remember { mutableStateOf("09:00 AM") }
    var detailCheckOut by remember { mutableStateOf("06:00 PM") }
    var detailHours by remember { mutableStateOf("8.0") }
    var detailRemarks by remember { mutableStateOf("") }
    var detailType by remember { mutableStateOf("Present") }
    
    // Absence reason selector dialog state
    var workerForAbsenceReason by remember { mutableStateOf<Worker?>(null) }

    // Active workers only for marking attendance
    val activeWorkers = workers.filter { it.status == "Active" }

    // Date Picker Dialog setup
    val calendar = Calendar.getInstance()
    calendar.time = parsedDate
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selYear, selMonth, selDay ->
            val newCalendar = Calendar.getInstance()
            newCalendar.set(selYear, selMonth, selDay)
            viewModel.setSelectedDate(sdf.format(newCalendar.time))
        },
        year, month, day
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Date Header scroller / chooser
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev Date
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.time = parsedDate
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    viewModel.setSelectedDate(sdf.format(cal.time))
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                }

                // Selected Date
                Row(
                    modifier = Modifier
                        .clickable { datePickerDialog.show() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayDate,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Next Date
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.time = parsedDate
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    viewModel.setSelectedDate(sdf.format(cal.time))
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                }
            }
        }

        // Title Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${Localization.translate("mark_attendance", language)} (${activeWorkers.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Tap worker to edit hours",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        if (activeWorkers.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Please add active workers first.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeWorkers, key = { it.id }) { worker ->
                    val existingAttendance = attendanceList.find { it.workerId == worker.id }

                    AttendanceWorkerRow(
                        worker = worker,
                        attendance = existingAttendance,
                        language = language,
                        onMarkType = { type ->
                            if (type == "Absent") {
                                workerForAbsenceReason = worker
                            } else {
                                // Default mark
                                viewModel.markAttendance(
                                    workerId = worker.id,
                                    type = type,
                                    checkIn = if (type == "Present" || type == "Half Day" || type == "Night Shift") "09:00 AM" else null,
                                    checkOut = if (type == "Present") "06:00 PM" else if (type == "Half Day") "01:30 PM" else if (type == "Night Shift") "11:00 PM" else null,
                                    hours = if (type == "Present" || type == "Night Shift" || type == "Holiday") 8.0 else if (type == "Half Day") 4.0 else 0.0,
                                    absenceReason = null
                                )
                            }
                        },
                        onRowClick = {
                            selectedWorkerForDetails = worker
                            detailType = existingAttendance?.attendanceType ?: "Present"
                            detailCheckIn = existingAttendance?.checkInTime ?: "09:00 AM"
                            detailCheckOut = existingAttendance?.checkOutTime ?: "06:00 PM"
                            detailHours = (existingAttendance?.totalWorkingHours ?: 8.0).toString()
                            detailRemarks = existingAttendance?.remarks ?: ""
                        }
                    )
                }
            }
        }

        // Absence Reason Selector Popup Dialog
        if (workerForAbsenceReason != null) {
            val reasons = listOf("Sick", "Personal Work", "Village", "Family Function", "Emergency", "Other")
            AlertDialog(
                onDismissRequest = { workerForAbsenceReason = null },
                title = { Text("Reason for Absence: ${workerForAbsenceReason?.fullName}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        reasons.forEach { reason ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.markAttendance(
                                            workerId = workerForAbsenceReason!!.id,
                                            type = "Absent",
                                            absenceReason = reason
                                        )
                                        workerForAbsenceReason = null
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Text(Localization.translate(reason, language), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { workerForAbsenceReason = null }) {
                        Text(Localization.translate("cancel", language))
                    }
                }
            )
        }

        // Custom hours / check-in check-out detailed edit dialog
        if (selectedWorkerForDetails != null) {
            AlertDialog(
                onDismissRequest = { selectedWorkerForDetails = null },
                title = { Text("Attendance Details: ${selectedWorkerForDetails?.fullName}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Check In / Check Out Fields
                        OutlinedTextField(
                            value = detailCheckIn,
                            onValueChange = { detailCheckIn = it },
                            label = { Text(Localization.translate("check_in", language)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = detailCheckOut,
                            onValueChange = { detailCheckOut = it },
                            label = { Text(Localization.translate("check_out", language)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = detailHours,
                            onValueChange = { detailHours = it },
                            label = { Text(Localization.translate("hours", language)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = detailRemarks,
                            onValueChange = { detailRemarks = it },
                            label = { Text(Localization.translate("remarks", language)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.markAttendance(
                                workerId = selectedWorkerForDetails!!.id,
                                type = detailType,
                                checkIn = detailCheckIn.ifBlank { null },
                                checkOut = detailCheckOut.ifBlank { null },
                                hours = detailHours.toDoubleOrNull() ?: 8.0,
                                remarks = detailRemarks.ifBlank { null }
                            )
                            selectedWorkerForDetails = null
                        }
                    ) {
                        Text(Localization.translate("save", language))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedWorkerForDetails = null }) {
                        Text(Localization.translate("cancel", language))
                    }
                }
            )
        }
    }
}

@Composable
fun AttendanceWorkerRow(
    worker: Worker,
    attendance: Attendance?,
    language: String,
    onMarkType: (String) -> Unit,
    onRowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRowClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = worker.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${Localization.translate(worker.skillCategory, language)} • ${worker.formattedWorkerId}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Status Pill
                if (attendance != null) {
                    val (color, label) = when (attendance.attendanceType) {
                        "Present" -> Color(0xFF10B981) to "Present"
                        "Absent" -> Color(0xFFEF4444) to "Absent"
                        "Half Day" -> Color(0xFFF59E0B) to "Half Day"
                        "Night Shift" -> Color(0xFF6366F1) to "Night Shift"
                        "Leave" -> Color(0xFF8B5CF6) to "Leave"
                        "Holiday" -> Color(0xFF6B7280) to "Holiday"
                        else -> Color.Gray to "Unmarked"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = Localization.translate(label, language) + (if (attendance.absenceReason != null) " (${Localization.translate(attendance.absenceReason, language)})" else ""),
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Unmarked",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Mark Button Selector
            val options = listOf("Present", "Absent", "Half Day", "Night Shift", "Leave", "Holiday")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(options) { type ->
                    val isSelected = attendance?.attendanceType == type
                    val (activeBg, activeFg) = when (type) {
                        "Present" -> Color(0xFF10B981) to Color.White
                        "Absent" -> Color(0xFFEF4444) to Color.White
                        "Half Day" -> Color(0xFFF59E0B) to Color.White
                        "Night Shift" -> Color(0xFF6366F1) to Color.White
                        "Leave" -> Color(0xFF8B5CF6) to Color.White
                        else -> Color(0xFF6B7280) to Color.White
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) activeBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { onMarkType(type) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("attendance_${worker.id}_$type")
                    ) {
                        Text(
                            text = Localization.translate(type, language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) activeFg else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
