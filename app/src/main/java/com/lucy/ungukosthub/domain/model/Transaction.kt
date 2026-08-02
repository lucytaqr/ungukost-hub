package com.lucy.ungukosthub.domain.model

enum class TransactionType {
    INCOME,
    EXPENSE
}

/**
 * Domain Model representing a Financial Transaction entity in Ungu Kost.
 */
data class Transaction(
    val id: String = "",
    val type: TransactionType = TransactionType.INCOME,
    val category: String = "", // e.g. "Sewa Kamar", "Listrik", "Air", "Internet", "Lainnya"
    val tenantName: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val proofUrl: String = ""
)
