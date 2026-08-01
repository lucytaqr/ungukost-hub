package com.lucy.ungukosthub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.data.remote.dto.FacilityDto
import com.lucy.ungukosthub.data.remote.dto.toDto
import com.lucy.ungukosthub.data.remote.dto.toDomain
import com.lucy.ungukosthub.domain.model.Facility
import com.lucy.ungukosthub.domain.repository.FacilityRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementasi FacilityRepository yang terhubung ke Cloud Firestore koleksi 'facilities'.
 */
class FacilityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FacilityRepository {

    private val facilitiesCollection = firestore.collection("facilities")

    override fun getFacilities(): Flow<Resource<List<Facility>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = facilitiesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Gagal mengambil data fasilitas"))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val facilitiesList = snapshot.documents.mapNotNull { doc ->
                    val dto = doc.toObject(FacilityDto::class.java)
                    dto?.copy(id = doc.id)?.toDomain()
                }
                trySend(Resource.Success(facilitiesList))
            }
        }

        awaitClose { listener.remove() }
    }

    override suspend fun addFacility(facility: Facility): Resource<Unit> {
        return try {
            val docRef = facilitiesCollection.document()
            val dto = facility.toDto().copy(id = docRef.id)
            docRef.set(dto).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menambah fasilitas")
        }
    }
}
