package com.lucy.ungukosthub.presentation.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
 * Helper kirim pesan pengingat tagihan via WhatsApp
 */
private fun sendWhatsAppReminderMessage(
    context: Context,
    phone: String,
    tenantName: String,
    roomNumber: String,
    amount: Double
) {
    val cleanPhone = phone.replace("[^0-9]".toRegex(), "")
    val formattedPhone = if (cleanPhone.startsWith("0")) "62" + cleanPhone.substring(1) else cleanPhone

    val message = "Halo Kak $tenantName,\n\n" +
            "Ini adalah pengingat pembayaran sewa kamar (Kamar $roomNumber) sebesar ${formatRupiah(amount)}.\n" +
            "Mohon segera melakukan pembayaran sewa kos ya. Terima kasih! 🙏"

    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback URL jika app WhatsApp belum terpasang
    }
}

/**
 * Layar Dashboard Utama: 3 Card Ringkasan (Total Kamar, Terisi, Kosong), Real Income & Real Bar Chart Data
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val brandPurple = Color(0xFF4C3BCE)
    val darkTitleColor = Color(0xFF2C1458)
    val backgroundLight = Color(0xFFFBFBFD)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Halo, Admin 👋",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = darkTitleColor
                        )
                    )
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
                                        modifier = Modifier.size(100.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. 3 Stat Summary Cards (Total Kamar, Terisi, Kosong)
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
                        }
                    }

                    // 3. Pendapatan Bulan Ini (Data Real) & Bar Chart Card Real Data
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
                                        color = if (uiState.isGrowthPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                    ) {
                                        Text(
                                            text = uiState.incomeGrowthText,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (uiState.isGrowthPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Real Data Bar Chart
                                MonthlyBarChart(
                                    labels = uiState.monthlyChartLabels,
                                    incomeValues = uiState.monthlyChartIncome
                                )
                            }
                        }
                    }

                    // 4. Section List Pengingat Tagihan WhatsApp
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pengingat Tagihan WhatsApp",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = darkTitleColor
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            if (uiState.billReminders.isEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFEBEBF5))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ReceiptLong,
                                            contentDescription = null,
                                            tint = brandPurple,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Tidak Ada Tagihan Jatuh Tempo Hari Ini",
                                            fontWeight = FontWeight.Bold,
                                            color = darkTitleColor,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tidak ada penghuni yang jadwal jatuh tempo bayar tagihan sewa pada tanggal hari ini.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF8E8E93),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    uiState.billReminders.forEach { reminder ->
                                        BillReminderCard(
                                            reminder = reminder,
                                            onSendWhatsApp = {
                                                sendWhatsAppReminderMessage(
                                                    context = context,
                                                    phone = reminder.phone,
                                                    tenantName = reminder.tenantName,
                                                    roomNumber = reminder.roomNumber,
                                                    amount = reminder.amount
                                                )
                                            }
                                        )
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

/**
 * Kartu Item Pengingat Tagihan Sewa via WhatsApp dengan Tombol {Icon WhatsApp} Kirim
 */
@Composable
fun BillReminderCard(
    reminder: TenantBillReminder,
    onSendWhatsApp: () -> Unit
) {
    val brandPurple = Color(0xFF4C3BCE)
    val darkTitleColor = Color(0xFF2C1458)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEBEBF5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reminder.tenantName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = darkTitleColor,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = brandPurple.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Kamar ${reminder.roomNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandPurple,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (reminder.entryDateText.isNotBlank()) {
                    Text(
                        text = "Tanggal Masuk: ${reminder.entryDateText}",
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93)
                    )
                }

                Text(
                    text = "Sewa: ${formatRupiah(reminder.amount)} / Bulan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSendWhatsApp,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandPurple)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic__baseline_whatsapp),
                    contentDescription = "WhatsApp",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Kirim",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Mini Card Stat untuk 3 angka di Dashboard (Total Kamar, Terisi, Kosong)
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
 * Visual Bar Chart Pendapatan 6 Bulan Real Data
 */
@Composable
fun MonthlyBarChart(
    labels: List<String> = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun"),
    incomeValues: List<Double> = emptyList()
) {
    val brandPurple = Color(0xFF4C3BCE)
    val displayLabels = labels.ifEmpty { listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun") }

    val maxVal = incomeValues.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val heights = if (incomeValues.isNotEmpty()) {
        incomeValues.map { ((it / maxVal) * 0.85).coerceIn(0.12, 0.95).toFloat() }
    } else {
        listOf(0.45f, 0.65f, 0.75f, 0.55f, 0.8f, 0.95f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        displayLabels.forEachIndexed { index, month ->
            val h = heights.getOrElse(index) { 0.45f }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight(h)
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
 * Dashboard Bottom Navigation Bar (dengan icon Kamar @drawable/mdi__bedroom)
 */
@Composable
fun DashboardBottomNavigation(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val brandPurple = Color(0xFF4C3BCE)
    val inactiveColor = Color(0xFF8E8E93)

    var loadingTab by remember { mutableStateOf<Int?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEFEFEF)),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (loadingTab != null) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = brandPurple,
                    trackColor = brandPurple.copy(alpha = 0.2f)
                )
            }

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
                    isLoading = loadingTab == 0,
                    activeColor = brandPurple,
                    inactiveColor = inactiveColor,
                    onClick = {
                        if (activeTab != 0) {
                            loadingTab = 0
                            onTabSelected(0)
                        }
                    }
                )
                BottomNavItem(
                    drawableResId = R.drawable.mdi__bedroom,
                    label = "Kamar",
                    isSelected = activeTab == 1,
                    isLoading = loadingTab == 1,
                    activeColor = brandPurple,
                    inactiveColor = inactiveColor,
                    onClick = {
                        if (activeTab != 1) {
                            loadingTab = 1
                            onTabSelected(1)
                        }
                    }
                )
                BottomNavItem(
                    icon = Icons.Default.People,
                    label = "Penghuni",
                    isSelected = activeTab == 2,
                    isLoading = loadingTab == 2,
                    activeColor = brandPurple,
                    inactiveColor = inactiveColor,
                    onClick = {
                        if (activeTab != 2) {
                            loadingTab = 2
                            onTabSelected(2)
                        }
                    }
                )
                BottomNavItem(
                    icon = Icons.Default.Receipt,
                    label = "Keuangan",
                    isSelected = activeTab == 3,
                    isLoading = loadingTab == 3,
                    activeColor = brandPurple,
                    inactiveColor = inactiveColor,
                    onClick = {
                        if (activeTab != 3) {
                            loadingTab = 3
                            onTabSelected(3)
                        }
                    }
                )
                BottomNavItem(
                    icon = Icons.Default.MoreHoriz,
                    label = "Lainnya",
                    isSelected = activeTab == 4,
                    isLoading = loadingTab == 4,
                    activeColor = brandPurple,
                    inactiveColor = inactiveColor,
                    onClick = {
                        if (activeTab != 4) {
                            loadingTab = 4
                            onTabSelected(4)
                        }
                    }
                )
            }
        }
    }
}
