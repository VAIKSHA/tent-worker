package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.data.repository.WorkerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkerRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WorkerRepository(
            workerDao = database.workerDao(),
            attendanceDao = database.attendanceDao(),
            advancePaymentDao = database.advancePaymentDao(),
            paymentDao = database.paymentDao(),
            settingsDao = database.settingsDao()
        )
        
        // Initializing mock database for rich aesthetic view and seamless offline testing
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty()
        }
    }

    // Settings State
    val settingsState: StateFlow<Settings> = repository.settings
        .map { it ?: Settings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Settings())

    // Admin Authentication State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminEmail = MutableStateFlow("admin@sonutent.com")
    val adminEmail: StateFlow<String> = _adminEmail.asStateFlow()

    private val _adminPassword = MutableStateFlow("admin123") // Default credentials

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun login(email: String, word: String): Boolean {
        if (email.trim().lowercase() == _adminEmail.value && word == _adminPassword.value) {
            _isAdminLoggedIn.value = true
            _authError.value = null
            return true
        } else {
            _authError.value = "Invalid credentials"
            return false
        }
    }

    fun logout() {
        _isAdminLoggedIn.value = false
    }

    fun changePassword(old: String, new: String): Boolean {
        if (old == _adminPassword.value && new.isNotBlank()) {
            _adminPassword.value = new
            return true
        }
        return false
    }

    fun resetPassword() {
        // Reset password to default for demo recovery
        _adminPassword.value = "admin123"
    }

    // Workers State
    val workers: StateFlow<List<Worker>> = repository.allWorkers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filters State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSkillFilter = MutableStateFlow("All")
    val selectedSkillFilter: StateFlow<String> = _selectedSkillFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("All")
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    val filteredWorkers: StateFlow<List<Worker>> = combine(
        workers,
        _searchQuery,
        _selectedSkillFilter,
        _selectedStatusFilter
    ) { workerList, query, skill, status ->
        workerList.filter { worker ->
            val matchesQuery = query.isBlank() ||
                    worker.fullName.contains(query, ignoreCase = true) ||
                    worker.mobileNumber.contains(query) ||
                    worker.formattedWorkerId.contains(query, ignoreCase = true)

            val matchesSkill = skill == "All" || worker.skillCategory == skill
            val matchesStatus = status == "All" || worker.status == status

            matchesQuery && matchesSkill && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSkillFilter(skill: String) {
        _selectedSkillFilter.value = skill
    }

    fun setStatusFilter(status: String) {
        _selectedStatusFilter.value = status
    }

    // Core Attendance State
    private val _selectedDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    val attendanceForSelectedDate: StateFlow<List<Attendance>> = _selectedDate
        .flatMapLatest { date -> repository.getAttendanceByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendanceList: StateFlow<List<Attendance>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdvancesList: StateFlow<List<AdvancePayment>> = repository.allAdvances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPaymentsList: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Worker operations
    fun addWorker(
        name: String,
        phone: String,
        address: String,
        emergency: String,
        dailyWage: Double,
        nightWage: Double,
        halfWage: Double,
        skill: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val newWorker = Worker(
                fullName = name,
                mobileNumber = phone,
                address = address,
                emergencyContact = emergency,
                joiningDate = System.currentTimeMillis(),
                dailyWage = dailyWage,
                nightShiftWage = nightWage,
                halfDayWage = halfWage,
                skillCategory = skill,
                notes = notes
            )
            repository.saveWorker(newWorker)
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch {
            repository.saveWorker(worker)
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            repository.deleteWorker(worker)
        }
    }

    // Attendance marking
    fun markAttendance(
        workerId: Long,
        type: String,
        checkIn: String? = null,
        checkOut: String? = null,
        hours: Double = 8.0,
        remarks: String? = null,
        absenceReason: String? = null
    ) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value
            val existing = repository.getAttendanceForWorker(workerId).first().find { it.date == dateStr }
            val record = Attendance(
                id = existing?.id ?: 0,
                workerId = workerId,
                date = dateStr,
                attendanceType = type,
                checkInTime = checkIn,
                checkOutTime = checkOut,
                totalWorkingHours = hours,
                remarks = remarks,
                absenceReason = absenceReason
            )
            repository.saveAttendance(record)
        }
    }

    // Advance Payment operations
    fun giveAdvance(workerId: Long, amount: Double, date: String, reason: String, notes: String?) {
        viewModelScope.launch {
            val advance = AdvancePayment(
                workerId = workerId,
                amount = amount,
                date = date,
                reason = reason,
                notes = notes
            )
            repository.saveAdvance(advance)
        }
    }

    fun deleteAdvance(advance: AdvancePayment) {
        viewModelScope.launch {
            repository.deleteAdvance(advance)
        }
    }

    // Salary Payment operations
    fun paySalary(workerId: Long, amount: Double, date: String, method: String, notes: String?) {
        viewModelScope.launch {
            val payment = Payment(
                workerId = workerId,
                amountPaid = amount,
                paymentDate = date,
                paymentMethod = method,
                notes = notes
            )
            repository.savePayment(payment)
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
        }
    }

    // Settings saving
    fun updateSettings(settings: Settings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
        }
    }

    // Calculations & Metrics per worker (Full Lifecycle salary computation)
    fun getWorkerWagesSummary(worker: Worker, workerAttendance: List<Attendance>, workerAdvances: List<AdvancePayment>, workerPayments: List<Payment>): WorkerWagesSummary {
        var totalWages = 0.0
        
        workerAttendance.forEach { att ->
            totalWages += when (att.attendanceType) {
                "Present" -> worker.dailyWage
                "Half Day" -> worker.halfDayWage
                "Night Shift" -> worker.nightShiftWage
                "Holiday" -> if (settingsState.value.holidayRules == "Paid Leave") worker.dailyWage else 0.0
                else -> 0.0 // Absent or unpaid Leave
            }
        }

        val totalAdvance = workerAdvances.sumOf { it.amount }
        val totalPaid = workerPayments.sumOf { it.amountPaid }
        val remainingSalary = totalWages - totalAdvance - totalPaid

        return WorkerWagesSummary(
            totalWages = totalWages,
            totalAdvance = totalAdvance,
            totalPaid = totalPaid,
            remainingSalary = remainingSalary
        )
    }
}

data class WorkerWagesSummary(
    val totalWages: Double,
    val totalAdvance: Double,
    val totalPaid: Double,
    val remainingSalary: Double
)
