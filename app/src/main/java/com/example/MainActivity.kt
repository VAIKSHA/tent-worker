package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Settings
import com.example.ui.Localization
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer() {
    val viewModel: MainViewModel = viewModel()
    val settings by viewModel.settingsState.collectAsState()
    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    if (!isLoggedIn) {
        AdminLoginScreen(viewModel = viewModel, settings = settings)
    } else {
        AppNavigationFrame(viewModel = viewModel, settings = settings)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    viewModel: MainViewModel,
    settings: Settings
) {
    val language = settings.language
    var email by remember { mutableStateOf("admin@sonutent.com") }
    var password by remember { mutableStateOf("admin123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginErrorMsg by remember { mutableStateOf<String?>(null) }
    
    var showForgotPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("admin_login_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin lock icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = Localization.translate("admin_login", language),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Sonu Tent House - Admin Access Only",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )

                // Fields
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(Localization.translate("email", language)) },
                    modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(Localization.translate("password", language)) },
                    modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (loginErrorMsg != null) {
                    Text(
                        text = loginErrorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                // Login Button
                Button(
                    onClick = {
                        val success = viewModel.login(email, password)
                        if (!success) {
                            loginErrorMsg = Localization.translate("invalid_credentials", language)
                        } else {
                            loginErrorMsg = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("login_submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = Localization.translate("login", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Localization.translate("forgot_password", language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { showForgotPassword = true }
                            .padding(4.dp)
                    )
                    
                    Text(
                        text = "Demo default: admin123",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Forgot password dialog
        if (showForgotPassword) {
            AlertDialog(
                onDismissRequest = { showForgotPassword = false },
                title = { Text("Password Recovery") },
                text = {
                    Text("For security purposes, your password has been reset to default 'admin123'. You can change this later in settings.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetPassword()
                            showForgotPassword = false
                        }
                    ) {
                        Text("Reset Password")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPassword = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun AppNavigationFrame(
    viewModel: MainViewModel,
    settings: Settings
) {
    var currentRoute by remember { mutableStateOf("dashboard") }
    var selectedProfileId by remember { mutableStateOf<Long?>(null) }

    // Quick trigger navigation states
    var quickShowWorkerForm by remember { mutableStateOf(false) }
    var quickShowAdvanceForm by remember { mutableStateOf(false) }
    var quickShowPaymentForm by remember { mutableStateOf(false) }

    val language = settings.language

    val items = listOf(
        NavigationItem("dashboard", Localization.translate("dashboard", language), Icons.Default.Dashboard),
        NavigationItem("workers", Localization.translate("workers", language), Icons.Default.People),
        NavigationItem("attendance", Localization.translate("attendance", language), Icons.Default.FactCheck),
        NavigationItem("advances", Localization.translate("advances", language), Icons.Default.AccountBalanceWallet),
        NavigationItem("payments", Localization.translate("payments", language), Icons.Default.Payments),
        NavigationItem("reports", Localization.translate("reports", language), Icons.Default.Analytics),
        NavigationItem("settings", Localization.translate("settings", language), Icons.Default.Settings)
    )

    Scaffold(
        bottomBar = {
            if (currentRoute != "profile") {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    items.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentRoute = item.route },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 9.sp, maxLines = 1) },
                            alwaysShowLabel = true,
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                currentRoute == "dashboard" -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        settings = settings,
                        onNavigateToWorkers = { currentRoute = "workers" },
                        onNavigateToAttendance = { currentRoute = "attendance" },
                        onNavigateToAdvances = { currentRoute = "advances" },
                        onNavigateToPayments = { currentRoute = "payments" },
                        onNavigateToReports = { currentRoute = "reports" },
                        onShowQuickAddWorker = {
                            quickShowWorkerForm = true
                            currentRoute = "workers"
                        },
                        onShowQuickGiveAdvance = {
                            quickShowAdvanceForm = true
                            currentRoute = "advances"
                        },
                        onShowQuickPaySalary = {
                            quickShowPaymentForm = true
                            currentRoute = "payments"
                        }
                    )
                }
                currentRoute == "workers" -> {
                    WorkersScreen(
                        viewModel = viewModel,
                        settings = settings,
                        onNavigateToProfile = { id ->
                            selectedProfileId = id
                            currentRoute = "profile"
                        },
                        showAddFormInitially = quickShowWorkerForm,
                        onFormClosed = { quickShowWorkerForm = false }
                    )
                }
                currentRoute == "attendance" -> {
                    AttendanceScreen(
                        viewModel = viewModel,
                        settings = settings
                    )
                }
                currentRoute == "advances" -> {
                    AdvancesScreen(
                        viewModel = viewModel,
                        settings = settings,
                        showAddFormInitially = quickShowAdvanceForm,
                        onFormClosed = { quickShowAdvanceForm = false }
                    )
                }
                currentRoute == "payments" -> {
                    PaymentsScreen(
                        viewModel = viewModel,
                        settings = settings,
                        showAddFormInitially = quickShowPaymentForm,
                        onFormClosed = { quickShowPaymentForm = false }
                    )
                }
                currentRoute == "reports" -> {
                    ReportsScreen(
                        viewModel = viewModel,
                        settings = settings
                    )
                }
                currentRoute == "settings" -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        settings = settings,
                        onLogout = { viewModel.logout() }
                    )
                }
                currentRoute == "profile" && selectedProfileId != null -> {
                    ProfileScreen(
                        workerId = selectedProfileId!!,
                        viewModel = viewModel,
                        settings = settings,
                        onNavigateBack = {
                            currentRoute = "workers"
                            selectedProfileId = null
                        }
                    )
                }
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
