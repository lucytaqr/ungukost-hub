package com.lucy.ungukosthub.domain.model

/**
 * Domain Model representing a Room entity in Ungu Kost.
 */
data class Room(
    val id: String = "",
    val roomNumber: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val isOccupied: Boolean = false,
    val photoUrl: String = ""
)
