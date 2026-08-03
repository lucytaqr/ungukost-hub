package com.lucy.ungukosthub.presentation.finance

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.lucy.ungukosthub.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.lucy.ungukosthub.presentation.room.LabelWithAsterisk
import com.lucy.ungukosthub.presentation.tenant.TenantViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Helper simpan foto kamera ke cache temp untuk bukti pemasukan
 */
private fun saveCameraPhotoToCache(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.cacheDir, "income_proof_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        null
    }
}

/**
 * Layar Form Tambah Pemasukan (AddIncomeScreen) dengan upload bukti dari kamera & galeri.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinanceViewModel = hiltViewModel(),
    tenantViewModel: TenantViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val brandPurple = Color(0xFF4C3BCE)
    val darkTitleColor = Color(0xFF2C1458)
    val backgroundLight = Color(0xFFFBFBFD)

    val tenantListState by tenantViewModel.uiState.collectAsState()

    var tenantInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }

    val currentDateStr = remember {
        val format = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        format.format(Calendar.getInstance().time)
    }
    var dateInput by remember { mutableStateOf(currentDateStr) }
    var noteInput by remember { mutableStateOf("") }
    var proofPhotoUrl by remember { mutableStateOf("") }

    var tenantDropdownExpanded by remember { mutableStateOf(false) }

    // Launcher Kamera Langsung
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val uri = saveCameraPhotoToCache(context, bitmap)
            if (uri != null) {
                proofPhotoUrl = uri.toString()
            }
        }
    }

    // Launcher Galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            proofPhotoUrl = uri.toString()
        }
    }

    // Format daftar opsi penghuni dari data real Firestore
    val tenantOptions = remember(tenantListState.tenants) {
        if (tenantListState.tenants.isNotEmpty()) {
            tenantListState.tenants.map { tenant ->
                val roomStr = tenant.roomNumber.ifBlank { tenant.roomId }
                if (roomStr.isNotBlank()) "${tenant.name} - Kamar $roomStr" else tenant.name
            }
        } else {
            emptyList()
        }
    }

    // Dialog Kalender untuk Field Tanggal
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            R.style.PurpleDatePickerTheme,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                dateInput = dateFormat.format(selectedCalendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tambah Pemasukan",
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEBEBF5)),
                    shadowElevation = 0.5.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Informasi Pemasukan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = darkTitleColor
                            )
                        )

                        // 1. Dari Penghuni * (Searchable Real Data Firestore)
                        Column {
                            LabelWithAsterisk(label = "Dari Penghuni")
                            Spacer(modifier = Modifier.height(6.dp))
                            val filteredTenantOptions = remember(tenantInput, tenantOptions) {
                                if (tenantInput.isBlank()) {
                                    tenantOptions
                                } else {
                                    tenantOptions.filter { it.contains(tenantInput, ignoreCase = true) }
                                }
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = tenantInput,
                                    onValueChange = {
                                        tenantInput = it
                                        tenantDropdownExpanded = true
                                    },
                                    readOnly = false,
                                    placeholder = { Text("Ketik atau pilih penghuni...", color = Color(0xFF9E9E9E), fontSize = 14.sp) },
                                    trailingIcon = {
                                        IconButton(onClick = { tenantDropdownExpanded = !tenantDropdownExpanded }) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Dropdown Penghuni",
                                                tint = brandPurple
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = brandPurple,
                                        unfocusedBorderColor = Color(0xFFEBEBF5),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                DropdownMenu(
                                    expanded = tenantDropdownExpanded && filteredTenantOptions.isNotEmpty(),
                                    onDismissRequest = { tenantDropdownExpanded = false }
                                ) {
                                    if (tenantOptions.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Belum ada data penghuni", color = Color(0xFF8E8E93)) },
                                            onClick = { tenantDropdownExpanded = false }
                                        )
                                    } else {
                                        filteredTenantOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    tenantInput = option
                                                    tenantDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Jumlah *
                        Column {
                            LabelWithAsterisk(label = "Jumlah")
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = amountInput,
                                onValueChange = { amountInput = it },
                                placeholder = { Text("0", color = Color(0xFF9E9E9E), fontSize = 14.sp) },
                                leadingIcon = {
                                    Surface(
                                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                                        color = Color(0xFFF0EFF6),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Rp",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF4A4A4A)
                                                )
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = brandPurple,
                                    unfocusedBorderColor = Color(0xFFEBEBF5),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }

                        // 3. Tanggal * (Dialog Kalender)
                        Column {
                            LabelWithAsterisk(label = "Tanggal")
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = dateInput,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Pilih tanggal", color = Color(0xFF9E9E9E), fontSize = 14.sp) },
                                    trailingIcon = {
                                        IconButton(onClick = { datePickerDialog.show() }) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Pilih Tanggal",
                                                tint = brandPurple
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { datePickerDialog.show() },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = brandPurple,
                                        unfocusedBorderColor = Color(0xFFEBEBF5),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                            }
                        }

                        // 4. Keterangan
                        Column {
                            Text(
                                text = "Keterangan",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = darkTitleColor,
                                    fontSize = 14.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = noteInput,
                                onValueChange = { noteInput = it },
                                placeholder = { Text("Masukkan keterangan (opsional)", color = Color(0xFF9E9E9E), fontSize = 14.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = brandPurple,
                                    unfocusedBorderColor = Color(0xFFEBEBF5),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }

                        // 5. Bukti Pemasukan (Opsional)
                        Text(
                            text = "Bukti Pemasukan (Opsional)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = darkTitleColor,
                                fontSize = 14.sp
                            )
                        )

                        if (proofPhotoUrl.isBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { cameraLauncher.launch(null) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFF8F7FD),
                                    border = BorderStroke(1.dp, Color(0xFFD4CFFE))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Ambil Kamera",
                                            tint = brandPurple,
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Ambil Kamera", fontWeight = FontWeight.Bold, color = brandPurple, fontSize = 13.sp)
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { galleryLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFF8F7FD),
                                    border = BorderStroke(1.dp, Color(0xFFD4CFFE))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = "Pilih Galeri",
                                            tint = brandPurple,
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Pilih Galeri", fontWeight = FontWeight.Bold, color = brandPurple, fontSize = 13.sp)
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            ) {
                                AsyncImage(
                                    model = proofPhotoUrl,
                                    contentDescription = "Bukti Pemasukan",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                )

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(28.dp)
                                        .clickable { proofPhotoUrl = "" },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFE53935)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus Bukti",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 0.0
                        viewModel.addIncome("Sewa Kamar", tenantInput, amount, dateInput, noteInput, proofPhotoUrl)
                        onNavigateBack()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandPurple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Simpan Pemasukan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
