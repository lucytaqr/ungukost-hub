package com.lucy.ungukosthub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.data.remote.dto.TransactionDto
import com.lucy.ungukosthub.data.remote.dto.toDomain
import com.lucy.ungukosthub.data.remote.dto.toDto
import com.lucy.ungukosthub.domain.model.Transaction
import com.lucy.ungukosthub.domain.repository.TransactionRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TransactionRepository interfacing directly with Cloud Firestore.
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TransactionRepository {

    private val collectionRef = firestore.collection("transactions")

    override fun getTransactions(): Flow<Resource<List<Transaction>>> = callbackFlow {
        trySend(Resource.Loading())

        val listenerRegistration = collectionRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Gagal mengambil data transaksi"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val transactions = snapshot.documents.mapNotNull { doc ->
                        val dto = doc.toObject(TransactionDto::class.java)?.copy(id = doc.id)
                        dto?.toDomain()
                    }
                    trySend(Resource.Success(transactions))
                } else {
                    trySend(Resource.Success(emptyList()))
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun addTransaction(transaction: Transaction): Resource<Unit> {
        return try {
            val docRef = collectionRef.document()
            val dto = transaction.copy(id = docRef.id).toDto()
            docRef.set(dto).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menyimpan transaksi ke Cloud Firestore")
        }
    }

    override suspend fun deleteTransaction(id: String): Resource<Unit> {
        return try {
            collectionRef.document(id).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menghapus transaksi dari Cloud Firestore")
        }
    }
}
