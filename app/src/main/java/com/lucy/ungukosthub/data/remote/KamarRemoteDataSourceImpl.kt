package com.lucy.ungukosthub.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.lucy.ungukosthub.data.remote.dto.KamarDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementasi DataSource menggunakan Cloud Firestore.
 */
class KamarRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : KamarRemoteDataSource {

    override fun getDaftarKamar(): Flow<List<KamarDto>> = callbackFlow {
        val listenerRegistration = firestore.collection("kamar")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(KamarDto::class.java)?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun tambahKamar(kamarDto: KamarDto) {
        firestore.collection("kamar").add(kamarDto).await()
    }
}
