package com.lucy.ungukosthub.presentation.tenant

import com.lucy.ungukosthub.domain.model.computedStatus
import com.lucy.ungukosthub.domain.model.isActive

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucy.ungukosthub.domain.model.Tenant
import com.lucy.ungukosthub.presentation.dashboard.DashboardBottomNavigation

/**
 * Layar Daftar Penghuni (TenantListScreen) yang disesuaikan presisi dengan 05_tenants_list.png
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantListScreen(
    onNavigateBack: () -> Unit,
    onTenantClick: (String) -> Unit,
    onAddTenantClick: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToRooms: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: TenantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val brandPurple = Color(0xFF4C3BCE)
    val darkTitleColor = Color(0xFF2C1458)
    val backgroundLight = Color(0xFFFBFBFD)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Penghuni",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = darkTitleColor
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = darkTitleColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            DashboardBottomNavigation(
                activeTab = 2,
                onTabSelected = { index ->
                    when (index) {
                        0 -> onNavigateToDashboard()
                        1 -> onNavigateToRooms()
                        2 -> {} // Already on Tenants
                        3 -> onNavigateToFinance()
                        4 -> onNavigateToSettings()
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
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = brandPurple
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    // 1. Search Bar & Filter
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = viewModel::onSearchQueryChanged,
                                placeholder = {
                                    Text(
                                        text = "Cari nama / kamar / no. hp",
                                        color = Color(0xFF9E9E9E),
                                        fontSize = 14.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Cari",
                                        tint = Color(0xFF9E9E9E)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = brandPurple.copy(alpha = 0.5f),
                                    unfocusedBorderColor = Color(0xFFEBEBF5),
                                    focusedContainerColor = Color(0xFFF7F7FA),
                                    unfocusedContainerColor = Color(0xFFF7F7FA)
                                )
                            )

                            Surface(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clickable { /* Filter */ },
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF7F7FA),
                                border = BorderStroke(1.dp, Color(0xFFEBEBF5))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter",
                                        tint = brandPurple,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Tombol + Tambah Penghuni
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Button(
                                onClick = onAddTenantClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandPurple)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tambah Penghuni",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // 3. List Container Card
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFEBEBF5)),
                            shadowElevation = 0.5.dp
                        ) {
                            if (uiState.filteredTenants.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFF4F2FF),
                                        modifier = Modifier.size(72.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.PersonOff,
                                                contentDescription = null,
                                                tint = brandPurple,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Belum Ada Data Penghuni",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = Color(0xFF2C1458)
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Silakan klik tombol \"Tambah Penghuni\" di atas untuk menambahkan data penghuni kost baru.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 13.sp,
                                            color = Color(0xFF8E8E93),
                                            lineHeight = 18.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column {
                                    uiState.filteredTenants.forEachIndexed { index, tenant ->
                                        TenantRowItem(
                                            tenant = tenant,
                                            onClick = { onTenantClick(tenant.id) }
                                        )
                                        if (index < uiState.filteredTenants.size - 1) {
                                            HorizontalDivider(
                                                color = Color(0xFFF2F2F7),
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
fun TenantRowItem(
    tenant: Tenant,
    onClick: () -> Unit
) {
    val brandPurple = Color(0xFF4C3BCE)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = brandPurple.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = brandPurple,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = tenant.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF2C1458)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                val displayRoom = tenant.roomNumber.ifBlank { tenant.roomId }
                Text(
                    text = if (displayRoom.startsWith("Kamar", ignoreCase = true)) displayRoom else "Kamar $displayRoom",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color(0xFF555555)
                    )
                )
                Text(
                    text = "Asal: ${tenant.origin.ifBlank { "-" }}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93)
                    )
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            val isTenantActive = tenant.isActive()
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isTenantActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            ) {
                Text(
                    text = tenant.computedStatus(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isTenantActive) Color(0xFF2E7D32) else Color(0xFFE53935)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Detail",
                tint = brandPurple,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
