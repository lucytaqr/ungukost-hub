package com.lucy.ungukosthub.presentation.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Format angka ke mata uang Rupiah
 */
private fun formatPrice(amount: Double): String {
    val formatter = NumberFormat.getInstance(Locale("in", "ID"))
    return "Rp " + formatter.format(amount.toLong())
}

/**
 * Layar Detail Laporan Keuangan (FinanceReportScreen) yang disesuaikan presisi dengan 11_finance_report.png
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val brandPurple = Color(0xFF4C3BCE)
    val darkTitleColor = Color(0xFF2C1458)
    val backgroundLight = Color(0xFFFBFBFD)

    val expenseBreakdown = listOf(
        Triple("Listrik", 800000.0, "34%"),
        Triple("Air", 300000.0, "13%"),
        Triple("Internet", 350000.0, "15%"),
        Triple("Kebersihan", 400000.0, "17%"),
        Triple("Perbaikan", 500000.0, "21%")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Laporan Keuangan",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                // Month Selector Dropdown
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEBEBF5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.selectedMonth,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = darkTitleColor,
                                fontSize = 15.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Pilih Bulan",
                            tint = brandPurple
                        )
                    }
                }

                // Finance Summary Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEBEBF5)),
                    shadowElevation = 0.5.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        FinanceSummaryItem(
                            label = "Pemasukan",
                            amount = formatPrice(uiState.totalIncome),
                            color = Color(0xFF2E7D32)
                        )

                        HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(vertical = 14.dp))

                        FinanceSummaryItem(
                            label = "Pengeluaran",
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

                // Rincian Kategori Pengeluaran Section
                Text(
                    text = "Rincian Kategori Pengeluaran",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = darkTitleColor
                    )
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEBEBF5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        expenseBreakdown.forEachIndexed { index, (category, amount, percentage) ->
                            ExpenseBreakdownRow(
                                category = category,
                                amount = formatPrice(amount),
                                percentage = percentage
                            )
                            if (index < expenseBreakdown.size - 1) {
                                HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(vertical = 10.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Button: Unduh Laporan
                OutlinedButton(
                    onClick = { /* Download Report PDF */ },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, brandPurple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = brandPurple,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Unduh Laporan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = brandPurple
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ExpenseBreakdownRow(
    category: String,
    amount: String,
    percentage: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4A4A4A),
                fontSize = 14.sp
            )
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C1458),
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = percentage,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8E8E93),
                    fontSize = 13.sp
                ),
                modifier = Modifier.width(36.dp)
            )
        }
    }
}
