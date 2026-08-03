package com.lucy.ungukosthub.presentation.room

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.res.painterResource
import com.lucy.ungukosthub.R
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucy.ungukosthub.domain.model.Room
import java.text.NumberFormat
import java.util.Locale

/**
 * Formatter angka tanpa desimal (contoh: 1.200.000)
 */
private fun formatPriceNumber(amount: Double): String {
    val localeID = Locale("in", "ID")
    val formatter = NumberFormat.getInstance(localeID)
    return formatter.format(amount.toLong())
}

/**
 * Layar Daftar Kamar (RoomListScreen) yang disesuaikan presisi dengan desain 02_rooms_list.png
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListScreen(
    onNavigateBack: () -> Unit,
    onAddRoomClick: () -> Unit,
    onRoomClick: (String) -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToTenants: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: RoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.roomListState.collectAsState()

    val brandPurple = Color(0xFF4C3BCE)
    val darkTitleColor = Color(0xFF2C1458)
    val backgroundLight = Color(0xFFFBFBFD)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Kamar",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = darkTitleColor
                            )
                        )
                    }
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
                actions = {
                    IconButton(onClick = { /* More Action */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Lainnya",
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
            RoomListBottomNavigation(
                activeTab = 1,
                onTabSelected = { index ->
                    when (index) {
                        0 -> onNavigateToDashboard()
                        1 -> {} // Already on Kamar
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

                    // 1. Search Bar & Filter Button
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
                                        text = "Cari kamar...",
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
                                    .clickable { /* Filter action */ },
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

                    // 2. Filter Status Chips (Semua, Terisi, Kosong)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChipItem(
                                label = "Semua (${uiState.totalCount})",
                                isSelected = uiState.selectedFilter == RoomFilterCategory.ALL,
                                onClick = { viewModel.onFilterSelected(RoomFilterCategory.ALL) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChipItem(
                                label = "Terisi (${uiState.occupiedCount})",
                                isSelected = uiState.selectedFilter == RoomFilterCategory.OCCUPIED,
                                onClick = { viewModel.onFilterSelected(RoomFilterCategory.OCCUPIED) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChipItem(
                                label = "Kosong (${uiState.vacantCount})",
                                isSelected = uiState.selectedFilter == RoomFilterCategory.VACANT,
                                onClick = { viewModel.onFilterSelected(RoomFilterCategory.VACANT) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 3. Section Header (Daftar Kamar & + Tambah Kamar Button)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Kamar",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = darkTitleColor
                                )
                            )

                            Button(
                                onClick = onAddRoomClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = brandPurple
                                )
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
                                        text = "Tambah Kamar",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // 4. White Container Card for Rooms List
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
                            if (uiState.filteredRooms.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Tidak ada kamar yang sesuai.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            } else {
                                Column {
                                    uiState.filteredRooms.forEachIndexed { index, room ->
                                        RoomListItemRow(
                                            room = room,
                                            onClick = { onRoomClick(room.id.ifBlank { room.roomNumber }) }
                                        )
                                        if (index < uiState.filteredRooms.size - 1) {
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

/**
 * Filter Chip Item
 */
@Composable
fun FilterChipItem(
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

/**
 * Row item individual kamar dalam daftar
 */
@Composable
fun RoomListItemRow(
    room: Room,
    onClick: () -> Unit = {}
) {
    val purpleRoomColor = Color(0xFF3F2B96)
    val redRoomColor = Color(0xFFE53935)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Room Number (Left)
        Text(
            text = room.roomNumber,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (room.isOccupied) purpleRoomColor else redRoomColor
            ),
            modifier = Modifier.width(48.dp)
        )

        // Details (Middle)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = room.category,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFF555555)
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Rp ${formatPriceNumber(room.price)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1F1F1F)
                    )
                )
                Text(
                    text = " / bulan",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93)
                    )
                )
            }
        }

        // Status Badge Pill (Right)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (room.isOccupied) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ) {
            Text(
                text = if (room.isOccupied) "Terisi" else "Kosong",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = if (room.isOccupied) Color(0xFF2E7D32) else Color(0xFFC62828),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/**
 * Bottom Navigation Bar untuk Ungu Kost
 */
@Composable
fun RoomListBottomNavigation(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    com.lucy.ungukosthub.presentation.dashboard.DashboardBottomNavigation(
        activeTab = activeTab,
        onTabSelected = onTabSelected
    )
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    drawableResId: Int? = null,
    label: String,
    isSelected: Boolean,
    isLoading: Boolean = false,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val tint = if (isSelected || isLoading) activeColor else inactiveColor
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = activeColor,
                strokeWidth = 2.dp
            )
        } else if (drawableResId != null) {
            Icon(
                painter = painterResource(id = drawableResId),
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isSelected || isLoading) FontWeight.Bold else FontWeight.Normal
            ),
            color = tint
        )
    }
}
