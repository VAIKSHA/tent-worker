package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WorkerRepository(
    private val workerDao: WorkerDao,
    private val attendanceDao: AttendanceDao,
    private val advancePaymentDao: AdvancePaymentDao,
    private val paymentDao: PaymentDao,
    private val settingsDao: SettingsDao
) {
    val allWorkers: Flow<List<Worker>> = workerDao.getAllWorkers()
    val allAttendance: Flow<List<Attendance>> = attendanceDao.getAllAttendance()
    val allAdvances: Flow<List<AdvancePayment>> = advancePaymentDao.getAllAdvances()
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val settings: Flow<Settings?> = settingsDao.getSettings()

    // Setup helper to pre-populate default settings and mock workers if database is empty
    suspend fun initializeDatabaseIfEmpty() {
        val currentSettings = settingsDao.getSettingsDirect()
        if (currentSettings == null) {
            settingsDao.insertOrUpdateSettings(Settings())
        }

        // Check if workers exist, if not, pre-populate some mock data for rich UX
        val workersList = workerDao.getAllWorkers().first()
        if (workersList.isEmpty()) {
            val joinTime = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L // 30 days ago
            val mocks = listOf(
                Worker(fullName = "Rajesh Kumar", mobileNumber = "9876543210", address = "Sector 15, Dwarka, New Delhi", emergencyContact = "9876543211", joiningDate = joinTime, dailyWage = 600.0, nightShiftWage = 800.0, halfDayWage = 300.0, skillCategory = "Tent Setup", status = "Active", notes = "Experienced in marquee setups"),
                Worker(fullName = "Amit Sharma", mobileNumber = "9812345678", address = "Chawri Bazar, Delhi", emergencyContact = "9812345679", joiningDate = joinTime + 2 * 24 * 60 * 60 * 1000L, dailyWage = 700.0, nightShiftWage = 950.0, halfDayWage = 350.0, skillCategory = "Decoration", status = "Active", notes = "Floral decoration lead"),
                Worker(fullName = "Sanjay Singh", mobileNumber = "9988776655", address = "Karol Bagh, New Delhi", emergencyContact = "9988776650", joiningDate = joinTime + 5 * 24 * 60 * 60 * 1000L, dailyWage = 650.0, nightShiftWage = 850.0, halfDayWage = 325.0, skillCategory = "Electrician", status = "Active", notes = "Handles sound systems & lighting"),
                Worker(fullName = "Sunil Yadav", mobileNumber = "9555123456", address = "Rohini, Delhi", emergencyContact = "9555123457", joiningDate = joinTime + 10 * 24 * 60 * 60 * 1000L, dailyWage = 550.0, nightShiftWage = 750.0, halfDayWage = 275.0, skillCategory = "Driver", status = "Active", notes = "Drives Tata Ace & Bolero Pickup"),
                Worker(fullName = "Vijay Pal", mobileNumber = "9333445566", address = "Uttam Nagar, Delhi", emergencyContact = "9333445567", joiningDate = joinTime + 12 * 24 * 60 * 60 * 1000L, dailyWage = 450.0, nightShiftWage = 600.0, halfDayWage = 225.0, skillCategory = "Helper", status = "Active", notes = "Energetic helper, quick learner")
            )

            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            mocks.forEach { worker ->
                val workerId = workerDao.insertWorker(worker)
                
                // Add historical attendance for last 5 days
                for (i in 1..5) {
                    calendar.timeInMillis = System.currentTimeMillis()
                    calendar.add(Calendar.DAY_OF_YEAR, -i)
                    val dateStr = sdf.format(calendar.time)
                    
                    // Mix present, half day, night shifts
                    val attType = when ((workerId + i) % 5) {
                        0L -> "Half Day"
                        1L -> "Night Shift"
                        2L, 3L -> "Present"
                        else -> "Absent"
                    }
                    val checkIn = if (attType != "Absent") "09:00 AM" else null
                    val checkOut = if (attType == "Present") "06:00 PM" else if (attType == "Half Day") "01:30 PM" else if (attType == "Night Shift") "11:00 PM" else null
                    val reason = if (attType == "Absent") "Sick" else null

                    attendanceDao.insertAttendance(
                        Attendance(
                            workerId = workerId,
                            date = dateStr,
                            attendanceType = attType,
                            checkInTime = checkIn,
                            checkOutTime = checkOut,
                            absenceReason = reason,
                            remarks = "System generated mock"
                        )
                    )
                }

                // Add a small advance and payment
                val advDateStr = sdf.format(System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000L)
                advancePaymentDao.insertAdvance(
                    AdvancePayment(
                        workerId = workerId,
                        amount = 1000.0,
                        date = advDateStr,
                        reason = "Home Expense",
                        notes = "Initial cash advance"
                    )
                )

                val payDateStr = sdf.format(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000L)
                paymentDao.insertPayment(
                    Payment(
                        workerId = workerId,
                        amountPaid = 500.0,
                        paymentDate = payDateStr,
                        paymentMethod = "Cash",
                        notes = "Weekly payout part payment"
                    )
                )
            }
        }
    }

    // Workers operations
    fun getWorkerFlow(id: Long): Flow<Worker?> = workerDao.getWorkerByIdFlow(id)
    suspend fun getWorker(id: Long): Worker? = workerDao.getWorkerById(id)
    suspend fun saveWorker(worker: Worker): Long = workerDao.insertWorker(worker)
    suspend fun deleteWorker(worker: Worker) = workerDao.deleteWorker(worker)

    // Attendance operations
    fun getAttendanceByDate(date: String): Flow<List<Attendance>> = attendanceDao.getAttendanceByDate(date)
    fun getAttendanceForWorker(workerId: Long): Flow<List<Attendance>> = attendanceDao.getAttendanceForWorker(workerId)
    suspend fun saveAttendance(attendance: Attendance): Long = attendanceDao.insertAttendance(attendance)
    suspend fun deleteAttendanceForWorkerOnDate(workerId: Long, date: String) = attendanceDao.deleteAttendanceForWorkerOnDate(workerId, date)

    // Advance operations
    fun getAdvancesForWorker(workerId: Long): Flow<List<AdvancePayment>> = advancePaymentDao.getAdvancesForWorker(workerId)
    suspend fun saveAdvance(advance: AdvancePayment): Long = advancePaymentDao.insertAdvance(advance)
    suspend fun deleteAdvance(advance: AdvancePayment) = advancePaymentDao.deleteAdvance(advance)

    // Payment operations
    fun getPaymentsForWorker(workerId: Long): Flow<List<Payment>> = paymentDao.getPaymentsForWorker(workerId)
    suspend fun savePayment(payment: Payment): Long = paymentDao.insertPayment(payment)
    suspend fun deletePayment(payment: Payment) = paymentDao.deletePayment(payment)

    // Settings operations
    suspend fun saveSettings(settings: Settings) = settingsDao.insertOrUpdateSettings(settings)
}
