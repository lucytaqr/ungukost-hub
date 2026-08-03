package com.lucy.ungukosthub.domain.model

/**
 * Domain Model representing a Tenant entity in Ungu Kost.
 */
data class Tenant(
    val id: String = "",
    val name: String = "",
    val origin: String = "",
    val birthDate: String = "",
    val phone: String = "",
    val emergencyContact: String = "",
    val roomId: String = "",
    val roomNumber: String = "",
    val ktpUrl: String = "",
    val status: String = "Aktif",
    val entryDate: Long = System.currentTimeMillis(),
    val entryDateText: String = "",
    val exitDateText: String = ""
)
