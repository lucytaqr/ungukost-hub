package com.lucy.ungukosthub.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.lucy.ungukosthub.domain.model.Transaction
import com.lucy.ungukosthub.domain.model.TransactionType

/**
 * Data Transfer Object for Transaction model in Firestore.
 */
data class TransactionDto(
    val id: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "INCOME",
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "",
    @get:PropertyName("tenantName") @set:PropertyName("tenantName") var tenantName: String = "",
    @get:PropertyName("amount") @set:PropertyName("amount") var amount: Double = 0.0,
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = System.currentTimeMillis(),
    @get:PropertyName("note") @set:PropertyName("note") var note: String = "",
    @get:PropertyName("proofUrl") @set:PropertyName("proofUrl") var proofUrl: String = ""
)

fun TransactionDto.toDomain(): Transaction {
    return Transaction(
        id = id,
        type = if (type == "EXPENSE") TransactionType.EXPENSE else TransactionType.INCOME,
        category = category,
        tenantName = tenantName,
        amount = amount,
        date = date,
        timestamp = timestamp,
        note = note,
        proofUrl = proofUrl
    )
}

fun Transaction.toDto(): TransactionDto {
    return TransactionDto(
        id = id,
        type = type.name,
        category = category,
        tenantName = tenantName,
        amount = amount,
        date = date,
        timestamp = timestamp,
        note = note,
        proofUrl = proofUrl
    )
}
