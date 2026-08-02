package com.lucy.ungukosthub.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.lucy.ungukosthub.domain.model.Room

/**
 * Data Transfer Object for Room model in Firestore.
 */
data class RoomDto(
    val id: String = "",
    @get:PropertyName("roomNumber") @set:PropertyName("roomNumber") var roomNumber: String = "",
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "",
    @get:PropertyName("price") @set:PropertyName("price") var price: Double = 0.0,
    @get:PropertyName("isOccupied") @set:PropertyName("isOccupied") var isOccupied: Boolean = false,
    @get:PropertyName("photoUrl") @set:PropertyName("photoUrl") var photoUrl: String = "",
    @get:PropertyName("photoUrls") @set:PropertyName("photoUrls") var photoUrls: List<String> = emptyList(),
    @get:PropertyName("facilities") @set:PropertyName("facilities") var facilities: List<String> = emptyList()
)

fun RoomDto.toDomain(): Room {
    return Room(
        id = id,
        roomNumber = roomNumber,
        category = category,
        price = price,
        isOccupied = isOccupied,
        photoUrl = photoUrl,
        photoUrls = photoUrls,
        facilities = facilities
    )
}

fun Room.toDto(): RoomDto {
    return RoomDto(
        id = id,
        roomNumber = roomNumber,
        category = category,
        price = price,
        isOccupied = isOccupied,
        photoUrl = photoUrl,
        photoUrls = photoUrls,
        facilities = facilities
    )
}
