package com.lucy.ungukosthub.domain.repository

import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Domain Repository interface for managing Financial Transaction operations.
 */
interface TransactionRepository {
    fun getTransactions(): Flow<Resource<List<Transaction>>>
    suspend fun addTransaction(transaction: Transaction): Resource<Unit>
    suspend fun deleteTransaction(id: String): Resource<Unit>
}
