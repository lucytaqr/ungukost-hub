package com.lucy.ungukosthub.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.lucy.ungukosthub.domain.model.Tenant

/**
 * Data Transfer Object for Tenant model in Firestore.
 */
data class TenantDto(
    val id: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("origin") @set:PropertyName("origin") var origin: String = "",
    @get:PropertyName("birthDate") @set:PropertyName("birthDate") var birthDate: String = "",
    @get:PropertyName("phone") @set:PropertyName("phone") var phone: String = "",
    @get:PropertyName("emergencyContact") @set:PropertyName("emergencyContact") var emergencyContact: String = "",
    @get:PropertyName("roomId") @set:PropertyName("roomId") var roomId: String = "",
    @get:PropertyName("roomNumber") @set:PropertyName("roomNumber") var roomNumber: String = "",
    @get:PropertyName("ktpUrl") @set:PropertyName("ktpUrl") var ktpUrl: String = "",
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "Aktif",
    @get:PropertyName("entryDate") @set:PropertyName("entryDate") var entryDate: Long = System.currentTimeMillis()
)

fun TenantDto.toDomain(): Tenant {
    return Tenant(
        id = id,
        name = name,
        origin = origin,
        birthDate = birthDate,
        phone = phone,
        emergencyContact = emergencyContact,
        roomId = roomId,
        roomNumber = roomNumber,
        ktpUrl = ktpUrl,
        status = status,
        entryDate = entryDate
    )
}

fun Tenant.toDto(): TenantDto {
    return TenantDto(
        id = id,
        name = name,
        origin = origin,
        birthDate = birthDate,
        phone = phone,
        emergencyContact = emergencyContact,
        roomId = roomId,
        roomNumber = roomNumber,
        ktpUrl = ktpUrl,
        status = status,
        entryDate = entryDate
    )
}
