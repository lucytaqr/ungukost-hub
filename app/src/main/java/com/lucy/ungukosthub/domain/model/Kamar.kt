package com.lucy.ungukosthub.domain.model

/**
 * Domain Model (Entity) yang murni merepresentasikan objek bisnis Kamar.
 * Tidak tergantung pada database (Firebase/Room) atau UI (Android SDK).
 */
data class Kamar(
    val id: String = "",
    val nomorKamar: String,
    val tipeKamar: String,
    val hargaSewa: Double,
    val status: String, // "Terisi" / "Kosong"
    val fasilitas: List<String> = emptyList(),
    val fotoUrl: String = ""
)
