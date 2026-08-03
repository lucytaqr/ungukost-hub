package com.lucy.ungukosthub.presentation.room

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.io.File

/**
 * Helper untuk menyimpan foto tangkapan kamera langsung ke berkas cache temp
 */
private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.cacheDir, "camera_room_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        null
    }
}

/**
 * Layar Form Tambah Kamar Baru (AddRoomScreen) dengan penanganan navigasi Android (navigationBarsPadding)
 * serta dukungan foto kamera langsung + galeri dengan peninjauan banyak foto & tombol hapus (X).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRoomScreen(
    onNavigateBack: () -> Unit,
    viewModel: RoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.addRoomState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val brandPurple = Color(0xFF4C3BCE)
    val darkTitleColor = Color(0xFF2C1458)
    val backgroundLight = Color(0xFFFBFBFD)

    val facilitySuggestions = uiState.facilitySuggestions

    // Launcher 1: Kamera Langsung
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val uri = saveBitmapToCache(context, bitmap)
            if (uri != null) {
                viewModel.onAddPhotoUrls(listOf(uri.toString()))
            }
        }
    }

    // Launcher 2: Galeri Foto
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val urlStrings = uris.map { it.toString() }
            viewModel.onAddPhotoUrls(urlStrings)
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetAddRoomState()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }

    val isFormDirty = remember(uiState) {
        uiState.roomNumberInput.isNotBlank() ||
        uiState.priceInput.isNotBlank() ||
        uiState.photoUrlsList.isNotEmpty() ||
        uiState.facilitiesList.isNotEmpty()
    }

    BackHandler(enabled = isFormDirty) {
        showUnsavedDialog = true
    }

    val handleBack = {
        if (isFormDirty) {
            showUnsavedDialog = true
        } else {
            onNavigateBack()
        }
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Batalkan Pengisian Form?", fontWeight = FontWeight.Bold, color = Color(0xFFE53935)) },
            text = { Text("Perubahan yang Anda masukkan belum disimpan. Apakah Anda yakin ingin keluar?", color = darkTitleColor) },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        viewModel.resetAddRoomState()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Ya, Keluar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text("Batal", color = Color(0xFF8E8E93), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tambah Kamar",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = darkTitleColor
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
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
                // Form Card Container Utama
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
                            text = "Informasi Kamar",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = darkTitleColor
                            )
                        )

                        // 1. Nomor Kamar *
                        Column {
                            LabelWithAsterisk(label = "Nomor Kamar")
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.roomNumberInput,
                                onValueChange = viewModel::onRoomNumberChanged,
                                placeholder = { Text("Contoh: 101", color = Color(0xFF9E9E9E), fontSize = 14.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
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

                        // 2. Kategori * (Pilihan: Kamar, Rumah)
                        Column {
                            LabelWithAsterisk(label = "Kategori")
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CategorySelectCard(
                                    title = "Kamar",
                                    isSelected = uiState.categoryInput == "Kamar",
                                    onClick = { viewModel.onCategoryChanged("Kamar") },
                                    modifier = Modifier.weight(1f)
                                )
                                CategorySelectCard(
                                    title = "Rumah",
                                    isSelected = uiState.categoryInput == "Rumah",
                                    onClick = { viewModel.onCategoryChanged("Rumah") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // 3. Harga Sewa *
                        Column {
                            LabelWithAsterisk(label = "Harga Sewa")
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = uiState.priceInput,
                                onValueChange = viewModel::onPriceChanged,
                                placeholder = { Text("0", color = Color(0xFF9E9E9E), fontSize = 14.sp) },
                                leadingIcon = {
                                    Surface(
                                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                                        color = Color(0xFFF0EFF6),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
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

                        // 4. Fasilitas (Chip Container dengan Silang 'X' Hapus)
                        Column {
                            Text(
                                text = "Fasilitas",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = darkTitleColor,
                                    fontSize = 14.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Outer Container Box
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFEBEBF5))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        uiState.facilitiesList.forEach { facility ->
                                            FacilityChip(
                                                label = facility,
                                                onRemove = { viewModel.onRemoveFacility(facility) }
                                            )
                                        }
                                    }

                                    if (uiState.facilitiesList.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = uiState.facilityInput,
                                            onValueChange = viewModel::onFacilityChanged,
                                            placeholder = { Text("Ketik fasilitas baru...", color = Color(0xFF9E9E9E), fontSize = 13.sp) },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                            keyboardActions = KeyboardActions(
                                                onDone = {
                                                    if (uiState.facilityInput.isNotBlank()) {
                                                        viewModel.onAddFacility(uiState.facilityInput)
                                                    }
                                                }
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = brandPurple.copy(alpha = 0.4f),
                                                unfocusedBorderColor = Color(0xFFF0EFF6),
                                                focusedContainerColor = Color(0xFFF9F9FC),
                                                unfocusedContainerColor = Color(0xFFF9F9FC)
                                            )
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(
                                            onClick = {
                                                if (uiState.facilityInput.isNotBlank()) {
                                                    viewModel.onAddFacility(uiState.facilityInput)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(brandPurple, shape = RoundedCornerShape(10.dp))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Tambah Fasilitas",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (facilitySuggestions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Rekomendasi Fasilitas:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = Color(0xFF8E8E93)
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    facilitySuggestions.forEach { suggestion ->
                                        if (!uiState.facilitiesList.contains(suggestion)) {
                                            Surface(
                                                modifier = Modifier.clickable { viewModel.onAddFacility(suggestion) },
                                                shape = RoundedCornerShape(20.dp),
                                                color = Color(0xFFF0EFF6)
                                            ) {
                                                Text(
                                                    text = "+ $suggestion",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
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





                        // Section 2: Foto Kondisi Kamar (Bisa dari Kamera Langsung & Galeri + Fitur Hapus)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.photoUrlsList.isNotEmpty()) "Foto Kondisi Kamar (${uiState.photoUrlsList.size})" else "Foto Kondisi Kamar",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = darkTitleColor
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (uiState.photoUrlsList.isEmpty()) {
                            // Dua Tombol Pilihan Utama Side-by-Side: Kamera Langsung & Galeri Foto
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
                                            .padding(vertical = 20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Ambil Foto Kamera",
                                            tint = brandPurple,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Ambil Kamera",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = brandPurple
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Foto Langsung",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = Color(0xFF8E8E93)
                                            )
                                        )
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
                                            .padding(vertical = 20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = "Pilih dari Galeri",
                                            tint = brandPurple,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Pilih Galeri",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = brandPurple
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Pilih banyak berkas",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = Color(0xFF8E8E93)
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            // Row Horizontal Foto-foto dengan tombol Hapus (X) merah di tiap sudut foto
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                uiState.photoUrlsList.forEach { photoUrl ->
                                    Box(
                                        modifier = Modifier.size(110.dp)
                                    ) {
                                        AsyncImage(
                                            model = photoUrl,
                                            contentDescription = "Foto Kondisi Kamar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(14.dp))
                                        )

                                        // Tombol Silang Merah (X) untuk Hapus Foto ini
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(24.dp)
                                                .clickable { viewModel.onRemovePhotoUrl(photoUrl) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFE53935)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hapus Foto",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Kartu Tombol (+) Tambah dari Kamera
                                Surface(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clickable { cameraLauncher.launch(null) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF8F7FD),
                                    border = BorderStroke(1.dp, Color(0xFFD4CFFE))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Kamera",
                                            tint = brandPurple,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "+ Kamera",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = brandPurple,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                // Kartu Tombol (+) Tambah dari Galeri
                                Surface(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clickable { galleryLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF8F7FD),
                                    border = BorderStroke(1.dp, Color(0xFFD4CFFE))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = "Galeri",
                                            tint = brandPurple,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "+ Galeri",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = brandPurple,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol Simpan Kamar
                Button(
                    onClick = viewModel::addRoom,
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brandPurple,
                        contentColor = Color.White,
                        disabledContainerColor = brandPurple.copy(alpha = 0.7f),
                        disabledContentColor = Color.White
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Simpan Kamar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Label dengan Asterisk Merah untuk Field Wajib
 */
@Composable
fun LabelWithAsterisk(label: String) {
    val darkTitleColor = Color(0xFF2C1458)
    Text(
        text = buildAnnotatedString {
            append(label)
            withStyle(style = SpanStyle(color = Color(0xFFE53935), fontWeight = FontWeight.Bold)) {
                append(" *")
            }
        },
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = darkTitleColor,
            fontSize = 14.sp
        )
    )
}

/**
 * Kartu Pilihan Segment Kategori (Kamar / Rumah)
 */
@Composable
fun CategorySelectCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandPurple = Color(0xFF4C3BCE)
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) brandPurple else Color(0xFFF0EFF6),
        border = if (isSelected) BorderStroke(1.dp, brandPurple) else BorderStroke(1.dp, Color(0xFFEBEBF5))
    ) {
        Box(
            modifier = Modifier.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isSelected) Color.White else Color(0xFF4A4A4A)
                )
            )
        }
    }
}

/**
 * Chip Kotak Fasilitas yang dapat di-silang (x) untuk dihapus
 */
@Composable
fun FacilityChip(
    label: String,
    onRemove: () -> Unit
) {
    val brandPurple = Color(0xFF4C3BCE)
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF4F2FF),
        border = BorderStroke(1.dp, brandPurple.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = brandPurple
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onRemove),
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFFFEBEE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus $label",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
