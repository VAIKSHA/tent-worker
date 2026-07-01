package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "advance_payments",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workerId"])]
)
data class AdvancePayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workerId: Long,
    val amount: Double,
    val date: String, // "yyyy-MM-dd"
    val reason: String, // "Medical", "Travel", "Home Expense", "Festival", "Personal", "Food", "Other"
    val notes: String? = null
)
