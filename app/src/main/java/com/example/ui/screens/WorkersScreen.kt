package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Settings
import com.example.data.model.Worker
import com.example.ui.Localization
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkersScreen(
    viewModel: MainViewModel,
    settings: Settings,
    onNavigateToProfile: (Long) -> Unit,
    showAddFormInitially: Boolean = false,
    onFormClosed: () -> Unit = {}
) {
    val language = settings.language
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSkillFilter by viewModel.selectedSkillFilter.collectAsState()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsState()
    val filteredWorkers by viewModel.filteredWorkers.collectAsState()

    var showForm by remember { mutableStateOf(showAddFormInitially) }
    var editingWorker by remember { mutableStateOf<Worker?>(null) }
    var workerToDelete by remember { mutableStateOf<Worker?>(null) }

    // Categories
    val skillCategories = listOf("All", "Tent Setup", "Decoration", "Electrician", "Cook", "Driver", "Helper")
    val statusFilters = listOf("All", "Active", "Inactive")

    if (showAddFormInitially) {
        LaunchedEffect(Unit) {
            showForm = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.translate("worker_list", language),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FloatingActionButton(
                    onClick = {
                        editingWorker = null
                        showForm = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_worker_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Worker")
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("search_worker_input"),
                placeholder = { Text(Localization.translate("search_worker", language)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Skill Filter LazyRow
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(skillCategories) { skill ->
                    val isSelected = selectedSkillFilter == skill
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSkillFilter(skill) },
                        label = { Text(Localization.translate(skill, language)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter LazyRow
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusFilters) { status ->
                    val isSelected = selectedStatusFilter == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setStatusFilter(status) },
                        label = { Text(Localization.translate(status.lowercase(), language)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Worker List
            if (filteredWorkers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = "No workers found",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No workers found matching criteria",
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredWorkers, key = { it.id }) { worker ->
                        WorkerCard(
                            worker = worker,
                            currency = settings.currency,
                            language = language,
                            onCardClick = { onNavigateToProfile(worker.id) },
                            onEditClick = {
                                editingWorker = worker
                                showForm = true
                            },
                            onDeleteClick = {
                                workerToDelete = worker
                            }
                        )
                    }
                }
            }
        }

        // Add/Edit Dialog BottomSheet representation (fully custom)
        if (showForm) {
            AddEditWorkerDialog(
                worker = editingWorker,
                language = language,
                onDismiss = {
                    showForm = false
                    editingWorker = null
                    onFormClosed()
                },
                onSave = { name, phone, address, emergency, daily, night, half, skill, status, notes ->
                    if (editingWorker == null) {
                        viewModel.addWorker(name, phone, address, emergency, daily, night, half, skill, notes)
                    } else {
                        viewModel.updateWorker(
                            editingWorker!!.copy(
                                fullName = name,
                                mobileNumber = phone,
                                address = address,
                                emergencyContact = emergency,
                                dailyWage = daily,
                                nightShiftWage = night,
                                halfDayWage = half,
                                skillCategory = skill,
                                status = status,
                                notes = notes
                            )
                        )
                    }
                    showForm = false
                    editingWorker = null
                    onFormClosed()
                }
            )
        }

        // Delete confirmation dialog
        if (workerToDelete != null) {
            AlertDialog(
                onDismissRequest = { workerToDelete = null },
                title = { Text(Localization.translate("confirm_delete", language)) },
                text = { Text(Localization.translate("confirm_delete_msg", language)) },
                confirmButton = {
                    TextButton(
                        modifier = Modifier.testTag("confirm_delete_yes"),
                        onClick = {
                            viewModel.deleteWorker(workerToDelete!!)
                            workerToDelete = null
                        }
                    ) {
                        Text(Localization.translate("yes", language), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { workerToDelete = null }) {
                        Text(Localization.translate("no", language))
                    }
                }
            )
        }
    }
}

@Composable
fun WorkerCard(
    worker: Worker,
    currency: String,
    language: String,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val firstLetter = worker.fullName.firstOrNull()?.uppercase() ?: "?"
                        Text(
                            text = firstLetter,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = worker.fullName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = worker.formattedWorkerId,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = Localization.translate("skill_category", language),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = Localization.translate(worker.skillCategory, language),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = Localization.translate("daily_wage", language),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "$currency${worker.dailyWage.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = "Status",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    val statusText = Localization.translate(worker.status.lowercase(), language)
                    val statusColor = if (worker.status == "Active") Color(0xFF10B981) else Color(0xFFEF4444)
                    Text(
                        text = statusText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWorkerDialog(
    worker: Worker?,
    language: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Double, Double, Double, String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf(worker?.fullName ?: "") }
    var phone by remember { mutableStateOf(worker?.mobileNumber ?: "") }
    var address by remember { mutableStateOf(worker?.address ?: "") }
    var emergency by remember { mutableStateOf(worker?.emergencyContact ?: "") }
    var dailyWage by remember { mutableStateOf(worker?.dailyWage?.toString() ?: "500") }
    var nightWage by remember { mutableStateOf(worker?.nightShiftWage?.toString() ?: "750") }
    var halfWage by remember { mutableStateOf(worker?.halfDayWage?.toString() ?: "250") }
    var selectedSkill by remember { mutableStateOf(worker?.skillCategory ?: "Tent Setup") }
    var selectedStatus by remember { mutableStateOf(worker?.status ?: "Active") }
    var notes by remember { mutableStateOf(worker?.notes ?: "") }

    val skills = listOf("Tent Setup", "Decoration", "Electrician", "Cook", "Driver", "Helper")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (worker == null) Localization.translate("add_worker", language) else Localization.translate("edit_worker", language)) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("worker_name_input"),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.fillMaxWidth().testTag("worker_phone_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth().testTag("worker_address_input"),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = emergency,
                        onValueChange = { emergency = it },
                        label = { Text("Emergency Contact") },
                        modifier = Modifier.fillMaxWidth().testTag("worker_emergency_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dailyWage,
                            onValueChange = { dailyWage = it },
                            label = { Text("Daily (₹)") },
                            modifier = Modifier.weight(1f).testTag("worker_daily_wage_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = halfWage,
                            onValueChange = { halfWage = it },
                            label = { Text("Half (₹)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = nightWage,
                        onValueChange = { nightWage = it },
                        label = { Text("Night Shift (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                item {
                    Text("Skill Category", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        skills.forEach { skill ->
                            val selected = selectedSkill == skill
                            ElevatedFilterChip(
                                selected = selected,
                                onClick = { selectedSkill = skill },
                                label = { Text(skill, fontSize = 11.sp) }
                            )
                        }
                    }
                }
                item {
                    Text("Status", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedStatus == "Active", onClick = { selectedStatus = "Active" })
                            Text("Active")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedStatus == "Inactive", onClick = { selectedStatus = "Inactive" })
                            Text("Inactive")
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(
                            name,
                            phone,
                            address,
                            emergency,
                            dailyWage.toDoubleOrNull() ?: 500.0,
                            nightWage.toDoubleOrNull() ?: 750.0,
                            halfWage.toDoubleOrNull() ?: 250.0,
                            selectedSkill,
                            selectedStatus,
                            notes.ifBlank { null }
                        )
                    }
                },
                modifier = Modifier.testTag("worker_save_submit")
            ) {
                Text(Localization.translate("save", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.translate("cancel", language))
            }
        }
    )
}
