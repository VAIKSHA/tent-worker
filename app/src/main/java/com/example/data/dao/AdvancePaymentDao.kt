package com.example.data.dao

import androidx.room.*
import com.example.data.model.AdvancePayment
import kotlinx.coroutines.flow.Flow

@Dao
interface AdvancePaymentDao {
    @Query("SELECT * FROM advance_payments ORDER BY date DESC")
    fun getAllAdvances(): Flow<List<AdvancePayment>>

    @Query("SELECT * FROM advance_payments WHERE workerId = :workerId ORDER BY date DESC")
    fun getAdvancesForWorker(workerId: Long): Flow<List<AdvancePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvance(advance: AdvancePayment): Long

    @Delete
    suspend fun deleteAdvance(advance: AdvancePayment)
}
