package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workerId"]), Index(value = ["date", "workerId"], unique = true)]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workerId: Long,
    val date: String, // "yyyy-MM-dd"
    val checkInTime: String? = null, // "09:00 AM"
    val checkOutTime: String? = null, // "06:00 PM"
    val totalWorkingHours: Double = 8.0,
    val attendanceType: String, // "Present", "Absent", "Half Day", "Night Shift", "Leave", "Holiday"
    val remarks: String? = null,
    val absenceReason: String? = null // "Sick", "Personal Work", "Village", "Family Function", "Emergency", "Other"
)
