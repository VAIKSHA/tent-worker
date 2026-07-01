package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val photoUri: String? = null,
    val mobileNumber: String,
    val address: String,
    val emergencyContact: String,
    val joiningDate: Long,
    val dailyWage: Double,
    val nightShiftWage: Double,
    val halfDayWage: Double,
    val skillCategory: String, // e.g. "Tent Setup", "Decoration", "Electrician", "Cook", "Driver", "Helper"
    val status: String = "Active", // "Active", "Inactive"
    val notes: String? = null,
    val isArchived: Boolean = false
) {
    val formattedWorkerId: String
        get() = "STH-W-${id.toString().padStart(4, '0')}"
}
