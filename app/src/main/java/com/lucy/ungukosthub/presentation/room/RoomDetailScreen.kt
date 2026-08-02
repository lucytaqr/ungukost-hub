package com.lucy.ungukosthub.presentation.room

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.lucy.ungukosthub.presentation.components.DetailTabItem
import com.lucy.ungukosthub.presentation.components.InfoRow
import com.lucy.ungukosthub.presentation.tenant.TenantViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Layar Detail Kamar (RoomDetailScreen)
 * Memuat:
 * 1. Tab Detail, Penghuni, dan Riwayat Sewa
 * 2. Galeri Foto Kondisi Kamar asli dengan pratinjau zoom
 * 3. Tombol Edit Kamar dan Hapus Kamar sejajar secara horizontal (Hanya di Tab Detail)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoomDetailScreen(
    roomId: String,
    onNavigateBack: () -> Unit,
    onEditRoomClick: () -> Unit = {},
    viewModel: RoomViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel()
) {
    val roomListState by viewModel.roomListState.collectAsState()
    val tenantListState by tenantViewModel.uiState.collectAsState()

    val room = roomListState.rooms.find { it.id == roomId || it.roomNumber == roomId }
        ?: roomListState.rooms.firstOrNull()

    val brandPurple = Color(0xFF4C3BCE)
    val darkTitleColor = Color(0xFF2C1458)
    val backgroundLight = Color(0xFFFBFBFD)

    var selectedTab by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPreviewPhotoUrl by remember { mutableStateOf<String?>(null) }

    val formattedPrice = remember(room?.price) {
        val price = room?.price ?: 0.0
        val format = NumberFormat.getNumberInstance(Locale("id", "ID"))
        format.format(price)
    }

    val facilitiesList: List<String> = room?.facilities ?: emptyList()

    // Ambil daftar foto kondisi kamar asli
    val photosList: List<String> = remember(room) {
        val list = mutableListOf<String>()
        if (room?.photoUrls?.isNotEmpty() == true) {
            list.addAll(room.photoUrls)
        }
        if (room?.photoUrl?.isNotBlank() == true && !list.contains(room.photoUrl)) {
            list.add(0, room.photoUrl)
        }
        list
    }

    // Cari penghuni saat ini untuk kamar ini
    val currentTenant = remember(tenantListState.tenants, room) {
        tenantListState.tenants.find {
            it.roomId == room?.id || it.roomId == room?.roomNumber || (room != null && it.roomNumber == room.roomNumber)
        }
    }

    // Dialog Zoom Foto Kondisi Kamar
    selectedPreviewPhotoUrl?.let { photoUrl ->
        AlertDialog(
            onDismissRequest = { selectedPreviewPhotoUrl = null },
            title = { Text("Foto Kondisi Kamar", fontWeight = FontWeight.Bold, color = darkTitleColor) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Pratinjau Foto Kondisi",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPreviewPhotoUrl = null }) {
                    Text("Tutup", color = brandPurple, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Dialog Konfirmasi Hapus Kamar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Hapus Kamar?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE53935),
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus Kamar ${room?.roomNumber ?: roomId}? Data yang dihapus tidak dapat dikembalikan.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = darkTitleColor)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRoom(
                            roomId = room?.id ?: roomId,
                            onSuccess = onNavigateBack
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Ya, Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = Color(0xFF8E8E93), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Kamar",
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
                // 1. Header Summary Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEBEBF5)),
                    shadowElevation = 0.5.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kamar ${room?.roomNumber ?: "101"}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp,
                                    color = darkTitleColor
                                )
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (room?.isOccupied == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ) {
                                Text(
                                    text = if (room?.isOccupied == true) "Terisi" else "Kosong",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = if (room?.isOccupied == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kategori: ${room?.category ?: "Kamar"}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF555555),
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rp $formattedPrice",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = brandPurple
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

                        Spacer(modifier = Modifier.height(20.dp))

                        // Sub Tabs (Detail, Penghuni, Riwayat)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            DetailTabItem(
                                label = "Detail",
                                isSelected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            DetailTabItem(
                                label = "Penghuni",
                                isSelected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                            DetailTabItem(
                                label = "Riwayat",
                                isSelected = selectedTab == 2,
                                onClick = { selectedTab = 2 }
                            )
                        }
                    }
                }

                // 2. Tampilan Isi Sesuai Sub Tab
                when (selectedTab) {
                    0 -> {
                        // TAB 0: DETAIL KAMAR & FOTO KONDISI
                        Text(
                            text = "Informasi Kamar",
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
                                InfoRow(label = "Kategori Properti", value = room?.category ?: "Kamar")
                                HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(vertical = 12.dp))
                                InfoRow(label = "Harga Sewa", value = "Rp $formattedPrice / bulan")
                                HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(vertical = 12.dp))
                                InfoRow(label = "Status Ketersediaan", value = if (room?.isOccupied == true) "Terisi" else "Kosong")
                                HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(vertical = 12.dp))

                                Column {
                                    Text(
                                        text = "Fasilitas Kamar",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF8E8E93),
                                            fontSize = 14.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (facilitiesList.isEmpty()) {
                                        Text(text = "-", color = Color(0xFF8E8E93), fontSize = 14.sp)
                                    } else {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            facilitiesList.forEach { facility ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFF4F2FF),
                                                    border = BorderStroke(1.dp, brandPurple.copy(alpha = 0.2f))
                                                ) {
                                                    Text(
                                                        text = facility,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = brandPurple
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section Foto Kondisi Kamar Asli
                        Text(
                            text = if (photosList.isNotEmpty()) "Foto Kondisi (${photosList.size})" else "Foto Kondisi",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = darkTitleColor
                            )
                        )

                        if (photosList.isEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF8F7FD),
                                border = BorderStroke(1.dp, Color(0xFFEBEBF5))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = brandPurple,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Belum ada foto kondisi kamar",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                photosList.forEach { photoUrl ->
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clickable { selectedPreviewPhotoUrl = photoUrl }
                                    ) {
                                        AsyncImage(
                                            model = photoUrl,
                                            contentDescription = "Foto Kondisi Kamar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(16.dp))
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tombol Edit & Hapus Kamar Sejajar Horizontal (Hanya di Tab Detail)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onEditRoomClick,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandPurple),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Edit Kamar",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, Color(0xFFE53935)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Hapus Kamar",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFFE53935)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: PENGHUNI KAMAR SAAT INI
                        Text(
                            text = "Penghuni Kamar",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = darkTitleColor
                            )
                        )

                        if (currentTenant != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFEBEBF5))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = brandPurple.copy(alpha = 0.15f),
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = brandPurple,
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = currentTenant.name,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp,
                                                    color = darkTitleColor
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "No. HP: ${currentTenant.phone.ifBlank { currentTenant.emergencyContact }}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(vertical = 12.dp))
                                    InfoRow(label = "Tempat Asal", value = currentTenant.origin.ifBlank { "-" })
                                    HorizontalDivider(color = Color(0xFFF2F2F7), modifier = Modifier.padding(vertical = 12.dp))
                                    InfoRow(label = "Tanggal Lahir", value = currentTenant.birthDate.ifBlank { "-" })
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
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
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = brandPurple,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Kamar Saat Ini Kosong",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = darkTitleColor
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Belum ada penghuni yang menempati kamar ini.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: RIWAYAT SEWA KAMAR
                        Text(
                            text = "Riwayat Sewa",
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
                                if (currentTenant != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = currentTenant.name,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = darkTitleColor
                                                )
                                            )
                                            Text(
                                                text = "Sewa Aktif (Saat Ini)",
                                                style = MaterialTheme.typography.bodySmall.copy(color = brandPurple)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFE8F5E9)
                                        ) {
                                            Text(
                                                text = "Aktif",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E7D32)
                                                ),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = Color(0xFF8E8E93),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Belum ada riwayat transaksi sewa sebelumnya.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
