package com.lucy.ungukosthub.presentation.finance

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucy.ungukosthub.presentation.dashboard.DashboardBottomNavigation
import com.lucy.ungukosthub.presentation.room.DetailTabItem
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
 * Layar Ringkasan Keuangan (FinanceSummaryScreen) yang disesuaikan presisi dengan 07_finance_summary.png
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
    val uiState by viewModel.uiState.collectAsState()

    val brandPurple = Color(0xFF4C3BCE)
    val darkTitleColor = Color(0xFF2C1458)
    val backgroundLight = Color(0xFFFBFBFD)

    var selectedTopTab by remember { mutableIntStateOf(0) }

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
                // 1. Top Tabs (Ringkasan, Pemasukan, Pengeluaran)
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
                        label = "Pemasukan",
                        isSelected = selectedTopTab == 1,
                        onClick = {
                            selectedTopTab = 1
                            onAddIncomeClick()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FinanceTopTab(
                        label = "Pengeluaran",
                        isSelected = selectedTopTab == 2,
                        onClick = {
                            selectedTopTab = 2
                            onAddExpenseClick()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 2. Month Selector Dropdown
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

                // 4. Chart Section (Grafik Pemasukan vs Pengeluaran)
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

                        // Stacked Bar Chart
                        StackedBarChart()
                    }
                }

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddIncomeClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brandPurple),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("+ Pemasukan", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onAddExpenseClick,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, brandPurple),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("+ Pengeluaran", fontWeight = FontWeight.Bold, color = brandPurple)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
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
fun StackedBarChart() {
    val brandPurple = Color(0xFF4C3BCE)
    val expenseColor = Color(0xFFEF5350)
    val months = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun")
    val incomeHeights = listOf(0.45f, 0.65f, 0.75f, 0.6f, 0.8f, 0.95f)
    val expenseHeights = listOf(0.1f, 0.15f, 0.2f, 0.18f, 0.22f, 0.25f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
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
                        .width(18.dp)
                        .fillMaxHeight(incomeHeights[index])
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(brandPurple)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(expenseHeights[index])
                            .align(Alignment.BottomCenter)
                            .background(expenseColor)
                    )
                }
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
