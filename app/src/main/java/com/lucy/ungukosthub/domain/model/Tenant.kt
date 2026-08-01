package com.lucy.ungukosthub.domain.model

/**
 * Domain Model representing a Tenant entity in Ungu Kost.
 */
data class Tenant(
    val id: String = "",
    val name: String = "",
    val emergencyContact: String = "",
    val roomId: String = "",
    val ktpUrl: String = "",
    val entryDate: Long = 0L
)
