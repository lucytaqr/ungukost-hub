package com.lucy.ungukosthub.data.remote.dto

import com.lucy.ungukosthub.domain.model.Kamar

/**
 * Data Transfer Object (DTO) untuk Firebase Firestore.
 * Memiliki konstruktor kosong dan field yang cocok dengan struktur dokumen Firestore.
 */
data class KamarDto(
    val id: String = "",
    val nomorKamar: String = "",
    val tipeKamar: String = "",
    val hargaSewa: Double = 0.0,
    val status: String = "",
    val fasilitas: List<String> = emptyList(),
    val fotoUrl: String = ""
)

/**
 * Extension function untuk mengonversi DTO ke Domain Model.
 */
fun KamarDto.toDomain(): Kamar {
    return Kamar(
        id = id,
        nomorKamar = nomorKamar,
        tipeKamar = tipeKamar,
        hargaSewa = hargaSewa,
        status = status,
        fasilitas = fasilitas,
        fotoUrl = fotoUrl
    )
}

/**
 * Extension function untuk mengonversi Domain Model ke DTO.
 */
fun Kamar.toDto(): KamarDto {
    return KamarDto(
        id = id,
        nomorKamar = nomorKamar,
        tipeKamar = tipeKamar,
        hargaSewa = hargaSewa,
        status = status,
        fasilitas = fasilitas,
        fotoUrl = fotoUrl
    )
}
