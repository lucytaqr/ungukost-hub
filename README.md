# 🏢 UnguKost Hub - Modern Boarding House & Property Management

<p align="center">
  <img src="app/src/main/res/drawable/logo_ungukost.xml" alt="UnguKost Hub Logo" width="120" height="120" />
</p>

<p align="center">
  <b>Sistem Manajemen Kost & Keuangan Modern Berarsitektur Clean Architecture & Jetpack Compose</b>
</p>

<p align="center">
  <a href="#-fitur-utama"><img src="https://img.shields.io/badge/Platform-Android-green.svg?style=flat-square&logo=android" alt="Platform"></a>
  <a href="#-teknologi--arsitektur"><img src="https://img.shields.io/badge/Language-Kotlin-7F52FF.svg?style=flat-square&logo=kotlin" alt="Language"></a>
  <a href="#-teknologi--arsitektur"><img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat-square&logo=jetpackcompose" alt="UI"></a>
  <a href="#-teknologi--arsitektur"><img src="https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-blue.svg?style=flat-square" alt="Architecture"></a>
  <a href="#-backend--database"><img src="https://img.shields.io/badge/Backend-Firebase%20Firestore-FFCA28.svg?style=flat-square&logo=firebase" alt="Backend"></a>
</p>

---

## 📌 Tentang Aplikasi

**UnguKost Hub** adalah aplikasi manajemen rumah kost dan properti sewa berbasis Android modern yang dirancang untuk memudahkan pemilik dan pengelola kost dalam mencatat data kamar/rumah, mengelola data penghuni, serta memantau arus kas keuangan (*cashflow*) secara *real-time*.

Aplikasi ini dikembangkan mengikuti standar **Modern Android Development (MAD)** dengan implementasi murni **Jetpack Compose** dan arsitektur **Clean Architecture (MVVM)** yang aman, *scalable*, serta responsif.

---

## ✨ Fitur Utama

### 📊 1. Dashboard Analytic & Executive Summary
- **Ringkasan Arus Kas**: Pantau total pendapatan bersih, pemasukan, dan pengeluaran bulan berjalan.
- **Okupansi Real-time**: Indikator status jumlah unit terisi vs kosong secara otomatis.
- **Transaksi & Tagihan Terbaru**: Pengingat tagihan sewa yang akan/sudah jatuh tempo hari ini.

### 🛏️ 2. Manajemen Kamar & Rumah Sewa
- **Dukungan Kategori**: Mengelola unit tipe **Kamar** maupun **Rumah** (bisa diisi lebih dari 1 penghuni).
- **Galeri Foto Kondisi**: Unggah dan pratinjau foto unit kamar/rumah.
- **Filter Status**: Filter cepat (*Semua*, *Terisi*, *Kosong*).
- **Detail Lengkap**: Fasilitas, harga sewa, status ketersediaan, serta daftar penghuni aktif & riwayat sewa per unit.

### 👥 3. Manajemen Penghuni
- **Biodata Penghuni**: Pencatatan asal, tanggal lahir, kontak darurat, foto KTP, serta penempatan unit.
- **Automatisasi Status**: Status penghuni (*Aktif* vs *Non-Aktif*) otomatis diperbarui berdasarkan perhitungan tanggal masuk & keluar.
- **Smart Room Picker**: Pilihan unit pada form registrasi secara cerdas hanya menampilkan unit yang **kosong** atau ber-kategori **Rumah**.
- **Filter Status**: Filter status penghuni (*Semua*, *Aktif*, *Non-Aktif*).

### 💰 4. Keuangan & Riwayat Transaksi
- **Pencatatan Transaksi**: Catat Pemasukan (Sewa, Deposit, Pembayaran Listrik) dan Pengeluaran (Maintenance, Air, Listrik, Operasional).
- **Lampiran Bukti**: Simpan foto bukti transfer atau nota belanja fisik.
- **Filter Range Tanggal**: Filter riwayat transaksi berdasarkan rentang tanggal tertentu secara fleksibel.

### ⚙️ 5. Pengaturan & Akses Profil Admin
- **Profil Admin**: Pengelolaan informasi nama dan nomor kontak pengelola.
- **Keamanan Akun**: Fitur integrasi autentikasi dan logout aman via Firebase Auth.
- **Tampilan Konsisten**: Antarmuka bersih (*Glassmorphism & Clean White Dialogs*).

---

## 🏗️ Teknologi & Arsitektur

Aplikasi dikembangkan menggunakan ekosistem teknologi Android paling mutakhir:

```
app/src/main/java/com/lucy/ungukosthub/
├── core/                  # Utilities, Helper Classes, & Result Wrapper (Resource)
├── data/                  # Data Layer (DTOs, Mappers, Remote Data Sources & Repositories Impl)
│   ├── local/
│   ├── remote/dto/
│   └── repository/
├── di/                    # Dependency Injection Modules (Dagger Hilt)
├── domain/                # Domain Layer (Pure Kotlin Models, Repository Interfaces & Use Cases)
│   ├── model/
│   ├── repository/
│   └── usecase/
└── presentation/          # Presentation Layer (Jetpack Compose UI & ViewModels)
    ├── components/        # Reusable UI Widgets
    ├── dashboard/
    ├── finance/
    ├── login/
    ├── navigation/        # NavHost Routing Central
    ├── room/
    ├── settings/
    ├── tenant/
    └── theme/             # Design System Tokens, Colors & Typography
```

### 🛠️ Tech Stack & Library
- **Programming Language**: [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & Material 3
- **Architecture**: Clean Architecture (Data, Domain, Presentation) + MVVM
- **State Management**: Unidirectional Data Flow (UDF) via `StateFlow` & `collectAsState()`
- **Dependency Injection**: [Hilt / Dagger Hilt](https://dagger.dev/hilt/)
- **Asynchronous / Reactive**: Kotlin Coroutines & `Flow` (`callbackFlow` untuk Firestore real-time updates)
- **Navigation**: Jetpack Navigation Compose
- **Backend & Cloud**: Firebase Authentication & Firebase Cloud Firestore
- **Image Loader**: Coil Compose (`io.coil-kt:coil-compose`)

---

## 🚀 Cara Menjalankan Proyek (Setup Guide)

### Prerequisites
1. **Android Studio** (Ladybug / Jellyfish / 2024.1+ direkomendasikan).
2. **JDK**: Version 17.
3. **Android SDK**: Compile SDK 35, Min SDK 24.

### Langkah Instalasi
1. **Clone Repository**:
   ```bash
   git clone https://github.com/lucytaqr/ungukost-hub.git
   cd ungukost-hub
   ```

2. **Konfigurasi Firebase**:
   - Pastikan file `google-services.json` sudah berada di direktori `app/google-services.json`.

3. **Build & Run**:
   - Buka proyek di **Android Studio**.
   - Jalankan Gradle Sync.
   - Buka emulator atau sambungkan perangkat Android fisik via USB Debugging.
   - Klik **Run (`Shift + F10`)** atau jalankan perintah Gradle via terminal:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 📄 Lisensi & Hak Cipta

Hak Cipta © 2026 **UnguKost Hub Team / Lucyta QR**. Hak Cipta Dilindungi Undang-Undang.
