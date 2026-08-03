package com.lucy.ungukosthub.presentation.finance

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import com.lucy.ungukosthub.R
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.lucy.ungukosthub.domain.model.Transaction
import com.lucy.ungukosthub.domain.model.TransactionType
import com.lucy.ungukosthub.presentation.dashboard.DashboardBottomNavigation
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Format angka ke mata uang Rupiah
 */
private fun formatPrice(amount: Double): String {
    val formatter = NumberFormat.getInstance(Locale("id", "ID"))
    return "Rp " + formatter.format(amount.toLong())
}

/**
 * Layar Ringkasan Keuangan (FinanceSummaryScreen)
 * Fitur Utama:
 * 1. Filter Range Tanggal (Start Date - End Date)
 * 2. Detail Riwayat Transaksi (Modal Bottom Sheet / Dialog)
 * 3. Real-time Firestore Chart & Month Filter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceSummaryScreen(
    onNavigateBack: () -> Unit,
    onAddIncomeClick: () -> Unit = {},
    onAddExpenseClick: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToRooms: () -> Unit = {},
    onNavigateToTenants: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val brandPurple = Color(0xFF4C3BCE)
    val darkTitleColor = Color(0xFF2C1458)
    val backgroundLight = Color(0xFFFBFBFD)

    var selectedTopTab by remember { mutableIntStateOf(0) }
    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var selectedProofPhotoUrl by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<String?>(null) }

    // Date Range Picker State
    var showDateRangeDialog by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Dialog Range Tanggal Custom
    if (showDateRangeDialog) {
        val calendar = Calendar.getInstance()
        var tempStartMillis by remember { mutableStateOf<Long?>(uiState.startDate) }
        var tempEndMillis by remember { mutableStateOf<Long?>(uiState.endDate) }
        var tempStartText by remember { mutableStateOf(uiState.startDateText) }
        var tempEndText by remember { mutableStateOf(uiState.endDateText) }

        val startDatePicker = remember {
            DatePickerDialog(
                context,
                R.style.PurpleDatePickerTheme,
                { _, year, month, dayOfMonth ->
                    val sel = Calendar.getInstance().apply { set(year, month, dayOfMonth, 0, 0, 0) }
                    tempStartMillis = sel.timeInMillis
                    val fmt = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                    tempStartText = fmt.format(sel.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        val endDatePicker = remember {
            DatePickerDialog(
                context,
                R.style.PurpleDatePickerTheme,
                { _, year, month, dayOfMonth ->
                    val sel = Calendar.getInstance().apply { set(year, month, dayOfMonth, 23, 59, 59) }
                    tempEndMillis = sel.timeInMillis
                    val fmt = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                    tempEndText = fmt.format(sel.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        AlertDialog(
            onDismissRequest = { showDateRangeDialog = false },
            title = { Text("Filter Range Tanggal", fontWeight = FontWeight.Bold, color = darkTitleColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Pilih rentang tanggal transaksi yang ingin ditampilkan:", fontSize = 13.sp, color = Color(0xFF666666))

                    // Tanggal Mulai
                    Column {
                        Text("Tanggal Mulai", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = darkTitleColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { startDatePicker.show() },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8F7FD),
                            border = BorderStroke(1.dp, Color(0xFFD4CFFE))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tempStartText.ifBlank { "Pilih Tanggal Mulai" },
                                    fontSize = 14.sp,
                                    color = if (tempStartText.isNotBlank()) darkTitleColor else Color(0xFF9E9E9E)
                                )
                                Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = brandPurple, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // Tanggal Selesai
                    Column {
                        Text("Tanggal Selesai", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = darkTitleColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { endDatePicker.show() },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8F7FD),
                            border = BorderStroke(1.dp, Color(0xFFD4CFFE))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tempEndText.ifBlank { "Pilih Tanggal Selesai" },
                                    fontSize = 14.sp,
                                    color = if (tempEndText.isNotBlank()) darkTitleColor else Color(0xFF9E9E9E)
                                )
                                Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = brandPurple, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val start = tempStartMillis
                        val end = tempEndMillis
                        if (start != null && end != null) {
                            viewModel.setDateRangeFilter(start, end, tempStartText, tempEndText)
                        }
                        showDateRangeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = brandPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Terapkan Filter", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangeDialog = false }) {
                    Text("Batal", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // Dialog Konfirmasi Hapus Transaksi
    showDeleteConfirmDialog?.let { transId ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Hapus Transaksi", fontWeight = FontWeight.Bold, color = Color(0xFFE53935)) },
            text = { Text("Apakah Anda yakin ingin menghapus data transaksi ini dari Firestore?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(transId)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ya, Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Batal", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // Dialog Pratinjau Foto Bukti Transaksi (Full Size)
    selectedProofPhotoUrl?.let { proofUrl ->
        AlertDialog(
            onDismissRequest = { selectedProofPhotoUrl = null },
            title = { Text("Bukti Transaksi", fontWeight = FontWeight.Bold, color = darkTitleColor) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    AsyncImage(
                        model = proofUrl,
                        contentDescription = "Pratinjau Bukti Transaksi",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedProofPhotoUrl = null }) {
                    Text("Tutup", color = brandPurple, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Modal Bottom Sheet Detail Riwayat Transaksi
    uiState.selectedTransactionDetail?.let { transaction ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectTransactionDetail(null) },
            sheetState = bottomSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            val isIncome = transaction.type == TransactionType.INCOME
            val accentColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFE53935)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Detail Transaksi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Detail ${if (isIncome) "Pemasukan" else "Pengeluaran"}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = darkTitleColor,
                                fontSize = 18.sp
                            )
                        )
                    }

                    IconButton(onClick = { viewModel.selectTransactionDetail(null) }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF8E8E93))
                    }
                }

                // Card Nominal Besar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = accentColor.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Jumlah Nominal", fontSize = 12.sp, color = Color(0xFF666666), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${if (isIncome) "+" else "-"} ${formatPrice(transaction.amount)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }

                // Informasi Lengkap Transaksi
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFBFBFD),
                    border = BorderStroke(1.dp, Color(0xFFEBEBF5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailRowItem(label = "Jenis Transaksi", value = if (isIncome) "Pemasukan" else "Pengeluaran")
                        DetailRowItem(label = "Kategori", value = transaction.category.ifBlank { "-" })

                        if (transaction.tenantName.isNotBlank()) {
                            DetailRowItem(label = "Dari Penghuni", value = transaction.tenantName)
                        }

                        DetailRowItem(label = "Tanggal", value = transaction.date.ifBlank { "-" })
                        DetailRowItem(label = "Keterangan", value = transaction.note.ifBlank { "Tidak ada keterangan" })
                    }
                }

                // Proof Photo Section in Detail
                if (transaction.proofUrl.isNotBlank()) {
                    Text("Bukti Pembayaran / Pengeluaran", fontWeight = FontWeight.Bold, color = darkTitleColor, fontSize = 14.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedProofPhotoUrl = transaction.proofUrl }
                    ) {
                        AsyncImage(
                            model = transaction.proofUrl,
                            contentDescription = "Bukti Transaksi",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Action Delete Button
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = transaction.id },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFE53935)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Transaksi Ini", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Keuangan",
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
                activeTab = 3,
                onTabSelected = { index ->
                    when (index) {
                        0 -> onNavigateToDashboard()
                        1 -> onNavigateToRooms()
                        2 -> onNavigateToTenants()
                        3 -> {} // Already on Finance
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Top Tabs (Ringkasan, + Pemasukan, + Pengeluaran)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinanceTopTab(
                        label = "Ringkasan",
                        isSelected = selectedTopTab == 0,
                        onClick = { selectedTopTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    FinanceTopTab(
                        label = "+ Pemasukan",
                        isSelected = selectedTopTab == 1,
                        onClick = {
                            selectedTopTab = 0
                            onAddIncomeClick()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FinanceTopTab(
                        label = "+ Pengeluaran",
                        isSelected = selectedTopTab == 2,
                        onClick = {
                            selectedTopTab = 0
                            onAddExpenseClick()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 2. Filter Bar: Dropdown Bulan & Filter Range Tanggal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Month Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { monthDropdownExpanded = !monthDropdownExpanded },
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFEBEBF5))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.selectedMonth,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = darkTitleColor,
                                        fontSize = 14.sp
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Pilih Bulan",
                                    tint = brandPurple
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false }
                        ) {
                            uiState.availableMonths.forEach { month ->
                                DropdownMenuItem(
                                    text = { Text(month) },
                                    onClick = {
                                        viewModel.onMonthChanged(month)
                                        monthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Button Range Tanggal
                    Surface(
                        modifier = Modifier
                            .clickable { showDateRangeDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        color = if (uiState.startDate != null) brandPurple.copy(alpha = 0.1f) else Color.White,
                        border = BorderStroke(1.dp, if (uiState.startDate != null) brandPurple else Color(0xFFEBEBF5))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Range Tanggal",
                                tint = brandPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Range Tanggal",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = brandPurple,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }

                // Display Active Date Range Filter Badge (If Applied)
                if (uiState.startDate != null && uiState.endDate != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = brandPurple.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, brandPurple.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filter Tanggal: ${uiState.startDateText} - ${uiState.endDateText}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = brandPurple,
                                    fontSize = 12.sp
                                )
                            )
                            IconButton(
                                onClick = { viewModel.clearDateRangeFilter() },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus Filter",
                                    tint = brandPurple
                                )
                            }
                        }
                    }
                }

                // 3. Finance Summary Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEBEBF5)),
                    shadowElevation = 0.5.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        FinanceSummaryItem(
                            label = "Total Pemasukan",
                            amount = formatPrice(uiState.totalIncome),
                            color = Color(0xFF2E7D32)
                        )

                        HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(vertical = 14.dp))

                        FinanceSummaryItem(
                            label = "Total Pengeluaran",
                            amount = formatPrice(uiState.totalExpense),
                            color = Color(0xFFE53935)
                        )

                        HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(vertical = 14.dp))

                        FinanceSummaryItem(
                            label = "Laba Bersih",
                            amount = formatPrice(uiState.netProfit),
                            color = brandPurple
                        )
                    }
                }

                // 4. Chart Section (Grafik Pemasukan vs Pengeluaran Real Data)
                Text(
                    text = "Grafik Pemasukan vs Pengeluaran",
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
                    border = BorderStroke(1.dp, Color(0xFFEBEBF5))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Legend
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(brandPurple, shape = RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Pemasukan", fontSize = 12.sp, color = Color(0xFF555555))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(0xFFEF5350), shape = RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Pengeluaran", fontSize = 12.sp, color = Color(0xFF555555))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Dynamic Real Data Bar Chart
                        DynamicStackedBarChart(chartItems = uiState.chartItems)
                    }
                }

                // 5. Section Riwayat Pemasukan dan Pengeluaran di Bawah Grafik
                Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = darkTitleColor
                    )
                )

                if (uiState.filteredTransactions.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFEBEBF5))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = brandPurple,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Belum Ada Riwayat Transaksi",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = darkTitleColor
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Belum terdapat transaksi pemasukan atau pengeluaran pada rentang ini.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93)),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.filteredTransactions.forEach { transaction ->
                            TransactionItemCard(
                                transaction = transaction,
                                onItemClick = { viewModel.selectTransactionDetail(transaction) },
                                onProofClick = { proofUrl -> selectedProofPhotoUrl = proofUrl }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DetailRowItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF8E8E93), fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 13.sp, color = Color(0xFF2C1458), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FinanceTopTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandPurple = Color(0xFF4C3BCE)
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) brandPurple else Color(0xFFF3F2F8)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else Color(0xFF4A4A4A)
                )
            )
        }
    }
}

@Composable
fun FinanceSummaryItem(
    label: String,
    amount: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF8E8E93)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = color
            )
        )
    }
}

@Composable
fun DynamicStackedBarChart(chartItems: List<MonthlyChartItem>) {
    val brandPurple = Color(0xFF4C3BCE)
    val expenseColor = Color(0xFFEF5350)

    if (chartItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada data grafik transaksi",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
            )
        }
        return
    }

    val maxAmount = chartItems.maxOfOrNull { maxOf(it.incomeAmount, it.expenseAmount) }?.takeIf { it > 0 } ?: 1.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        chartItems.forEach { item ->
            val incomeRatio = if (item.incomeAmount > 0) ((item.incomeAmount / maxAmount) * 0.85).coerceIn(0.12, 0.95).toFloat() else 0f
            val expenseRatio = if (item.expenseAmount > 0) ((item.expenseAmount / maxAmount) * 0.85).coerceIn(0.12, 0.95).toFloat() else 0f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Batang Pemasukan (Ungu)
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .fillMaxHeight(incomeRatio)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(brandPurple)
                    )

                    // Batang Pengeluaran (Merah)
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .fillMaxHeight(expenseRatio)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(expenseColor)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.monthLabel,
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
 * Kartu Item Riwayat Transaksi Pemasukan / Pengeluaran dengan onClick untuk melihat detail
 */
@Composable
fun TransactionItemCard(
    transaction: Transaction,
    onItemClick: () -> Unit,
    onProofClick: (String) -> Unit
) {
    val brandPurple = Color(0xFF4C3BCE)
    val isIncome = transaction.type == TransactionType.INCOME
    val accentColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFE53935)
    val darkTitleColor = Color(0xFF2C1458)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.category.ifBlank { if (isIncome) "Pemasukan" else "Pengeluaran" },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = darkTitleColor,
                            fontSize = 15.sp
                        )
                    )

                    if (transaction.tenantName.isNotBlank()) {
                        Text(
                            text = "Dari: ${transaction.tenantName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = brandPurple,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }

                    if (transaction.date.isNotBlank()) {
                        Text(
                            text = transaction.date,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF8E8E93),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else "-"} ${formatPrice(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontSize = 15.sp
                    )
                )

                if (transaction.proofUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = brandPurple.copy(alpha = 0.1f),
                        modifier = Modifier.clickable { onProofClick(transaction.proofUrl) }
                    ) {
                        Text(
                            text = "Lihat Bukti",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = brandPurple,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
