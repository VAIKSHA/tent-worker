package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "Sonu Tent House",
    val currency: String = "₹",
    val attendanceTime: String = "09:00 AM",
    val salaryRules: String = "Present=Full Daily Wage, Half Day=50% Wage, Night Shift=Night Wage, Leave/Holiday=Configurable",
    val holidayRules: String = "Paid Leave", // "Paid", "Unpaid"
    val language: String = "English", // "English", "Hindi"
    val themeMode: String = "System" // "System", "Light", "Dark"
)
