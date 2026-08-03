package com.lucy.ungukosthub.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucy.ungukosthub.presentation.dashboard.DashboardBottomNavigation

/**
 * Layar Pengaturan / Lainnya (SettingsScreen) dengan fitur lengkap:
 * 1. Profil Admin
 * 2. Pengaturan Aplikasi
 * 3. Notifikasi
 * 4. Backup & Restore
 * 5. Tentang Aplikasi
 * 6. Keluar (Logout)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogoutClick: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToRooms: () -> Unit = {},
    onNavigateToTenants: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val brandPurple = Color(0xFF4C3BCE)
    val darkTitleColor = Color(0xFF2C1458)
    val backgroundLight = Color(0xFFFBFBFD)

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog state controllers
    var showProfileDialog by remember { mutableStateOf(false) }
    var showAppSettingsDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToastMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lainnya",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = darkTitleColor
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            DashboardBottomNavigation(
                activeTab = 4,
                onTabSelected = { index ->
                    when (index) {
                        0 -> onNavigateToDashboard()
                        1 -> onNavigateToRooms()
                        2 -> onNavigateToTenants()
                        3 -> onNavigateToFinance()
                        4 -> {} // Already on Settings / Lainnya
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Pengaturan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = darkTitleColor
                    )
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEBEBF5)),
                    shadowElevation = 0.5.dp
                ) {
                    Column {
                        // 1. Profil Admin
                        SettingsMenuItemRow(
                            title = "Profil Admin",
                            icon = Icons.Default.Person,
                            onClick = { showProfileDialog = true }
                        )
                        HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(horizontal = 16.dp))

                        // 2. Pengaturan Aplikasi
                        SettingsMenuItemRow(
                            title = "Pengaturan Aplikasi",
                            icon = Icons.Default.Settings,
                            onClick = { showAppSettingsDialog = true }
                        )
                        HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(horizontal = 16.dp))

                        // 3. Notifikasi
                        SettingsMenuItemRow(
                            title = "Notifikasi",
                            icon = Icons.Default.Notifications,
                            onClick = { showNotificationDialog = true }
                        )
                        HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(horizontal = 16.dp))

                        // 4. Backup & Restore
                        SettingsMenuItemRow(
                            title = "Backup & Restore",
                            icon = Icons.Default.Backup,
                            onClick = { showBackupDialog = true }
                        )
                        HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(horizontal = 16.dp))

                        // 5. Tentang Aplikasi
                        SettingsMenuItemRow(
                            title = "Tentang Aplikasi",
                            icon = Icons.Default.Info,
                            onClick = { showAboutDialog = true }
                        )
                        HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(horizontal = 16.dp))

                        // 6. Keluar Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = { showLogoutDialog = true })
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Keluar",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Keluar",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFFE53935)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ================= 1. DIALOG PROFIL ADMIN =================
    if (showProfileDialog) {
        var editName by remember { mutableStateOf(uiState.adminProfile.name) }
        var editPhone by remember { mutableStateOf(uiState.adminProfile.phone) }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Text(
                    text = "Profil Admin",
                    fontWeight = FontWeight.Bold,
                    color = darkTitleColor,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nama Admin") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brandPurple)
                    )
                    OutlinedTextField(
                        value = uiState.adminProfile.email,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Email (Tergembok)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Nomor Telepon") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brandPurple)
                    )
                    OutlinedTextField(
                        value = uiState.adminProfile.role,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Jabatan / Role") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAdminProfile(editName, editPhone)
                        showProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = brandPurple)
                ) {
                    Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Batal", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // ================= 2. DIALOG PENGATURAN APLIKASI =================
    if (showAppSettingsDialog) {
        var isDark by remember { mutableStateOf(uiState.appSettings.isDarkModeEnabled) }
        var currency by remember { mutableStateOf(uiState.appSettings.currency) }
        var language by remember { mutableStateOf(uiState.appSettings.language) }

        AlertDialog(
            onDismissRequest = { showAppSettingsDialog = false },
            title = {
                Text(
                    text = "Pengaturan Aplikasi",
                    fontWeight = FontWeight.Bold,
                    color = darkTitleColor,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Mode Gelap (Dark Mode)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tampilan tema aplikasi", fontSize = 12.sp, color = Color(0xFF8E8E93))
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { isDark = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = brandPurple)
                        )
                    }

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Mata Uang") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brandPurple)
                    )

                    OutlinedTextField(
                        value = language,
                        onValueChange = { language = it },
                        label = { Text("Bahasa Aplikasi") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brandPurple)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleDarkMode(isDark)
                        viewModel.updateAppSettings(currency, language, uiState.appSettings.defaultDueDay)
                        showAppSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = brandPurple)
                ) {
                    Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppSettingsDialog = false }) {
                    Text("Tutup", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // ================= 3. DIALOG NOTIFIKASI =================
    if (showNotificationDialog) {
        var waEnable by remember { mutableStateOf(uiState.notificationSettings.isWaReminderEnabled) }
        var timeInput by remember { mutableStateOf(uiState.notificationSettings.sendTime) }
        var vacancyEnable by remember { mutableStateOf(uiState.notificationSettings.isVacancyAlertEnabled) }
        var txEnable by remember { mutableStateOf(uiState.notificationSettings.isTransactionAlertEnabled) }

        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = {
                Text(
                    text = "Pengaturan Notifikasi",
                    fontWeight = FontWeight.Bold,
                    color = darkTitleColor,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pengingat WA Jatuh Tempo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Kirim draf WhatsApp otomatis", fontSize = 12.sp, color = Color(0xFF8E8E93))
                        }
                        Switch(
                            checked = waEnable,
                            onCheckedChange = { waEnable = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = brandPurple)
                        )
                    }

                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        label = { Text("Waktu Pengiriman Harian") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brandPurple)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notifikasi Kamar Kosong", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(
                            checked = vacancyEnable,
                            onCheckedChange = { vacancyEnable = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = brandPurple)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Alert Transaksi Baru", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(
                            checked = txEnable,
                            onCheckedChange = { txEnable = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = brandPurple)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateNotificationSettings(waEnable, timeInput, vacancyEnable, txEnable)
                        showNotificationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = brandPurple)
                ) {
                    Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Tutup", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // ================= 4. DIALOG BACKUP & RESTORE =================
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Text(
                    text = "Backup & Restore Data",
                    fontWeight = FontWeight.Bold,
                    color = darkTitleColor,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Cadangkan atau pulihkan seluruh data kamar, penghuni, dan transaksi sistem secara aman.")
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF3F2F8),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = brandPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.backupState.lastBackupText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkTitleColor
                            )
                        }
                    }

                    if (uiState.backupState.isBackupLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = brandPurple, modifier = Modifier.size(28.dp))
                        }
                    } else {
                        Button(
                            onClick = viewModel::performBackup,
                            colors = ButtonDefaults.buttonColors(containerColor = brandPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cadangkan Data Sekarang (Export JSON)", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = viewModel::performRestore,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pulihkan Data (Restore System)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Tutup", color = brandPurple, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ================= 5. DIALOG TENTANG APLIKASI =================
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = brandPurple,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "UnguKost Hub",
                        fontWeight = FontWeight.Bold,
                        color = darkTitleColor,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Versi Aplikasi: 1.0.0 (Build 2026)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Sistem Manajemen & Keuangan Kost Modern berarsitektur Clean Architecture MVVM & Firebase Cloud Integration.")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pengembang: Lucy Taqr / UnguKost Team", fontSize = 12.sp, color = Color(0xFF8E8E93))
                    Text("Hak Cipta © 2026 UnguKost. All rights reserved.", fontSize = 12.sp, color = Color(0xFF8E8E93))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = brandPurple)
                ) {
                    Text("Tutup", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ================= 6. DIALOG KELUAR (LOGOUT) =================
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Keluar dari Akun?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE53935),
                    fontSize = 18.sp
                )
            },
            text = {
                Text("Apakah Anda yakin ingin keluar dari akun admin UnguKost Hub?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Ya, Keluar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal", color = Color(0xFF8E8E93), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun SettingsMenuItemRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF555555),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF2C1458)
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF8E8E93),
            modifier = Modifier.size(20.dp)
        )
    }
}
