# Blueprint Aplikasi Manajemen "Ungu Kost" (Versi Ideal)

**Dokumentasi Perancangan Sistem Manajemen Kos Modern**  
*Project ini dirancang sebagai solusi nyata untuk manajemen bisnis kos "Ungu Kost" sekaligus sebagai portofolio pengembangan Android tingkat profesional.*

---

## 1. Latar Belakang & Tujuan
*   **Masalah:** Pencatatan manual operasional kos (pembayaran, data penghuni, keluhan) rentan terhadap kesalahan, kehilangan data, dan menyita waktu.
*   **Solusi:** Membangun aplikasi Android terintegrasi untuk admin/pemilik kos yang dapat mengelola seluruh aspek bisnis secara digital dan real-time.
*   **Tujuan Portofolio:** Mendemonstrasikan penguasaan pengembangan Android modern yang sesuai dengan standar industri saat ini.

---

## 2. Standar Teknologi (Tech Stack)
Penggunaan teknologi modern adalah kunci untuk memastikan aplikasi mudah dikembangkan, stabil, dan memiliki nilai tinggi di mata *tech recruiter*.

*   **Bahasa Pemrograman:** Kotlin (Standar industri untuk Android).
*   **User Interface (UI):** Jetpack Compose (Modern declarative UI toolkit).
*   **Asynchronous Programming:** Kotlin Coroutines & Flow.
*   **Backend & Database:** Firebase
    *   *Firebase Authentication* (Sistem Login yang aman).
    *   *Cloud Firestore* (Database NoSQL real-time).
    *   *Firebase Storage* (Penyimpanan file/foto KTP penghuni & kondisi kamar).
*   **Dependency Injection:** Hilt / Dagger.
*   **Version Control:** Git & GitHub.

---

## 3. Arsitektur Kode
*   **Pola Desain:** **MVVM (Model-View-ViewModel)** dipadukan dengan **Clean Architecture**.
*   **Manfaat:** Memisahkan logika bisnis dari tampilan antarmuka (UI). Membuat kode lebih mudah diuji (testable), di-maintenance, dan dipahami oleh developer lain.

---

## 4. Daftar Fitur Utama (Core Features)

### A. Dashboard Analitik
Halaman utama yang memberikan ringkasan status operasional secara cepat.
*   Indikator jumlah kamar (Terisi vs Kosong).
*   Total pendapatan bulan berjalan.
*   Daftar penghuni yang menunggak atau mendekati jatuh tempo.

### B. Manajemen Kamar & Fasilitas
*   **CRUD Kamar:** Tambah, edit, hapus data kamar.
*   **Kategorisasi:** Penentuan harga sewa berdasarkan fasilitas kamar (misal: AC vs Non-AC).
*   **Dokumentasi Visual:** Fitur unggah foto kondisi kamar saat kosong untuk referensi pemasaran.

### C. Manajemen Penghuni & Keamanan
*   Pencatatan data biodata penghuni (Nama, Asal, Kontak Darurat).
*   Arsip digital KTP/Identitas (diunggah ke Firebase Storage).
*   Riwayat kamar yang pernah ditempati penghuni.

### D. Sistem Keuangan & Kas
*   **Pemasukan:** Pencatatan otomatis siklus pembayaran sewa (Bulanan/Tahunan).
*   **Pengeluaran:** Pencatatan biaya operasional kos (Listrik, Air, Internet, Perbaikan, Kebersihan).
*   **Laporan Laba/Rugi:** Kalkulasi otomatis selisih pemasukan dan pengeluaran tiap bulannya.

### E. Automasi & Komunikasi
*   **Integrasi WhatsApp (Implicit Intent):** Tombol klik-langsung yang mengarahkan ke WhatsApp penghuni dengan template pesan otomatis (contoh: Penagihan sewa, pengiriman bukti pembayaran/kuitansi digital).

---

## 5. Alur Kerja Pengembangan (Best Practices)

Untuk membangun aplikasi ini dengan standar profesional, terapkan alur kerja berikut:

1.  **UI/UX Design (Figma):** Buat purwarupa (*wireframe* dan desain *high-fidelity*) sebelum menulis baris kode pertama.
2.  **Setup Repository (GitHub):** Inisialisasi proyek dan atur *branching strategy* (misal: *main*, *develop*, *feature-branches*).
3.  **Iterative Sprints:** Bagi pengembangan ke dalam modul kecil. Contoh:
    *   *Sprint 1:* Setup Firebase, Login, & Dashboard UI.
    *   *Sprint 2:* Fitur Manajemen Kamar & Penghuni.
    *   *Sprint 3:* Modul Keuangan & Integrasi WhatsApp.
4.  **Testing & QA:** Uji aplikasi di berbagai ukuran layar dan kondisi jaringan (termasuk mode offline sementara).

---

> *"Membangun aplikasi untuk memecahkan masalah di dunia nyata adalah portofolio terbaik yang bisa dimiliki oleh seorang developer perangkat lunak."*
