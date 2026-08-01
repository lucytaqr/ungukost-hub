package com.lucy.ungukosthub.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.lucy.ungukosthub.domain.model.Tenant

/**
 * Data Transfer Object for Tenant model in Firestore.
 */
data class TenantDto(
    val id: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("emergencyContact") @set:PropertyName("emergencyContact") var emergencyContact: String = "",
    @get:PropertyName("roomId") @set:PropertyName("roomId") var roomId: String = "",
    @get:PropertyName("ktpUrl") @set:PropertyName("ktpUrl") var ktpUrl: String = "",
    @get:PropertyName("entryDate") @set:PropertyName("entryDate") var entryDate: Long = 0L
)

fun TenantDto.toDomain(): Tenant {
    return Tenant(
        id = id,
        name = name,
        emergencyContact = emergencyContact,
        roomId = roomId,
        ktpUrl = ktpUrl,
        entryDate = entryDate
    )
}

fun Tenant.toDto(): TenantDto {
    return TenantDto(
        id = id,
        name = name,
        emergencyContact = emergencyContact,
        roomId = roomId,
        ktpUrl = ktpUrl,
        entryDate = entryDate
    )
}
