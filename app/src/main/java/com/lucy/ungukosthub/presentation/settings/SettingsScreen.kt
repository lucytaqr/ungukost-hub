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
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucy.ungukosthub.R
import com.lucy.ungukosthub.presentation.dashboard.DashboardBottomNavigation

/**
 * Layar Pengaturan / Lainnya (SettingsScreen) dengan menu utama:
 * 1. Profil Admin
 * 2. Tentang Aplikasi
 * 3. Keluar (Logout)
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

                        // 2. Tentang Aplikasi
                        SettingsMenuItemRow(
                            title = "Tentang Aplikasi",
                            icon = Icons.Default.Info,
                            onClick = { showAboutDialog = true }
                        )
                        HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(horizontal = 16.dp))

                        // 3. Keluar Button
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
            containerColor = Color.White,
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
                        label = { Text("Email") },
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

    // ================= 2. DIALOG TENTANG APLIKASI =================
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_ungukost),
                        contentDescription = "Logo UnguKost",
                        modifier = Modifier.size(36.dp)
                    )
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
                    Text("Pengembang: Lucyta QR / UnguKost Team", fontSize = 12.sp, color = Color(0xFF8E8E93))
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

    // ================= 3. DIALOG KELUAR (LOGOUT) =================
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
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
