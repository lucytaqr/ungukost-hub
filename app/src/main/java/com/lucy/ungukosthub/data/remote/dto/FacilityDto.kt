package com.lucy.ungukosthub.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.lucy.ungukosthub.domain.model.Facility

/**
 * Data Transfer Object (DTO) untuk koleksi 'facilities' di Cloud Firestore.
 */
data class FacilityDto(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = ""
)

fun FacilityDto.toDomain(): Facility {
    return Facility(
        id = id,
        name = name
    )
}

fun Facility.toDto(): FacilityDto {
    return FacilityDto(
        id = id,
        name = name
    )
}
