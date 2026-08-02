package com.lucy.ungukosthub.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucy.ungukosthub.R
import com.lucy.ungukosthub.presentation.room.BottomNavItem
import java.text.NumberFormat
import java.util.Locale

/**
 * Format angka ke mata uang Rupiah
 */
private fun formatRupiah(amount: Double): String {
    val localeID = Locale("in", "ID")
    val formatter = NumberFormat.getInstance(localeID)
    return "Rp " + formatter.format(amount.toLong())
}

/**
 * Layar Dashboard Utama yang disesuaikan presisi dengan desain 01_dashboard.png
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogoutClick: () -> Unit,
    onNavigateToRooms: () -> Unit = {},
    onNavigateToTenants: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
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
                        text = "Halo, ${uiState.currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Admin"} 👋",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = darkTitleColor
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifikasi",
                            tint = darkTitleColor,
                            modifier = Modifier.size(26.dp)
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
                activeTab = 0,
                onTabSelected = { index ->
                    when (index) {
                        0 -> {} // Already on Dashboard
                        1 -> onNavigateToRooms()
                        2 -> onNavigateToTenants()
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

                    // 1. Hero Purple Banner Card
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = brandPurple
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF3F2B96), Color(0xFF6B50F6))
                                        )
                                    )
                                    .padding(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Ungu Kost",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 24.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Kelola kos jadi mudah,\nnyaman dan terhubung.",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            )
                                        )
                                    }

                                    Image(
                                        painter = painterResource(id = R.drawable.boarding_house_hero),
                                        contentDescription = "Ungu Kost Hero",
                                        modifier = Modifier.size(90.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. 4 Stat Summary Cards (Total Kamar, Terisi, Kosong, Menunggak)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatMiniCard(
                                value = "${uiState.totalKamar}",
                                label = "Total Kamar",
                                valueColor = Color(0xFF3F2B96),
                                containerColor = Color(0xFFF4F2FF),
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                value = "${uiState.totalTerisi}",
                                label = "Terisi",
                                valueColor = Color(0xFF2E7D32),
                                containerColor = Color(0xFFE8F5E9),
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                value = "${uiState.totalKosong}",
                                label = "Kosong",
                                valueColor = Color(0xFF2C1458),
                                containerColor = Color(0xFFF7F7FA),
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                value = "${uiState.listTunggakan.size}",
                                label = "Menunggak",
                                valueColor = Color(0xFFE53935),
                                containerColor = Color(0xFFFFEBEE),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 3. Pendapatan Bulan Ini & Bar Chart Card
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
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Pendapatan Bulan Ini",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF4A4A4A)
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatRupiah(uiState.totalEstimasiPendapatan),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = Color(0xFF2C1458)
                                        )
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFE8F5E9)
                                    ) {
                                        Text(
                                            text = "+12%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF2E7D32)
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Visual Bar Chart
                                MonthlyBarChart()
                            }
                        }
                    }

                    // 4. Section Aksi Cepat
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Aksi Cepat",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = darkTitleColor
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                QuickActionCard(
                                    icon = Icons.Outlined.Home,
                                    label = "Kamar",
                                    onClick = onNavigateToRooms,
                                    modifier = Modifier.weight(1f)
                                )
                                QuickActionCard(
                                    icon = Icons.Outlined.People,
                                    label = "Penghuni",
                                    onClick = onNavigateToTenants,
                                    modifier = Modifier.weight(1f)
                                )
                                QuickActionCard(
                                    icon = Icons.Outlined.Wallet,
                                    label = "Keuangan",
                                    onClick = onNavigateToFinance,
                                    modifier = Modifier.weight(1f)
                                )
                                QuickActionCard(
                                    icon = Icons.Outlined.Assessment,
                                    label = "Laporan",
                                    onClick = onNavigateToReports,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

/**
 * Mini Card Stat untuk 4 angka di Dashboard
 */
@Composable
fun StatMiniCard(
    value: String,
    label: String,
    valueColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = valueColor
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color(0xFF6E6E73)
                )
            )
        }
    }
}

/**
 * Visual Bar Chart Pendapatan 6 Bulan
 */
@Composable
fun MonthlyBarChart() {
    val brandPurple = Color(0xFF4C3BCE)
    val months = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun")
    val heights = listOf(0.45f, 0.85f, 0.75f, 0.55f, 0.8f, 0.95f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        months.forEachIndexed { index, month ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight(heights[index])
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(brandPurple)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = month,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                )
            }
        }
    }
}

/**
 * Quick Action Card
 */
@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandPurple = Color(0xFF4C3BCE)
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEBEBF5))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = brandPurple.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = brandPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color(0xFF2C1458)
                )
            )
        }
    }
}

/**
 * Dashboard Bottom Navigation Bar
 */
@Composable
fun DashboardBottomNavigation(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val brandPurple = Color(0xFF4C3BCE)
    val inactiveColor = Color(0xFF8E8E93)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEFEFEF)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Dashboard",
                isSelected = activeTab == 0,
                activeColor = brandPurple,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(0) }
            )
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Kamar",
                isSelected = activeTab == 1,
                activeColor = brandPurple,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(1) }
            )
            BottomNavItem(
                icon = Icons.Default.People,
                label = "Penghuni",
                isSelected = activeTab == 2,
                activeColor = brandPurple,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(2) }
            )
            BottomNavItem(
                icon = Icons.Default.Receipt,
                label = "Keuangan",
                isSelected = activeTab == 3,
                activeColor = brandPurple,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(3) }
            )
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Lainnya",
                isSelected = activeTab == 4,
                activeColor = brandPurple,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(4) }
            )
        }
    }
}
