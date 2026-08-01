package com.lucy.ungukosthub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.data.remote.dto.TenantDto
import com.lucy.ungukosthub.data.remote.dto.toDomain
import com.lucy.ungukosthub.data.remote.dto.toDto
import com.lucy.ungukosthub.domain.model.Tenant
import com.lucy.ungukosthub.domain.repository.TenantRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Data Layer implementation of TenantRepository using Firebase Firestore ('tenants' collection).
 */
class TenantRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TenantRepository {

    private val tenantsCollection = firestore.collection("tenants")

    override fun getTenants(): Flow<Resource<List<Tenant>>> = callbackFlow {
        trySend(Resource.Loading())
        val listenerRegistration = tenantsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "An error occurred while fetching tenants"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val tenants = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(TenantDto::class.java)?.copy(id = doc.id)?.toDomain()
                }
                trySend(Resource.Success(tenants))
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    override fun getTenantsByRoomId(roomId: String): Flow<Resource<List<Tenant>>> = callbackFlow {
        trySend(Resource.Loading())
        val listenerRegistration = tenantsCollection
            .whereEqualTo("roomId", roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "An error occurred while fetching tenants for room"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tenants = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(TenantDto::class.java)?.copy(id = doc.id)?.toDomain()
                    }
                    trySend(Resource.Success(tenants))
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun getTenantById(id: String): Resource<Tenant> {
        return try {
            val snapshot = tenantsCollection.document(id).get().await()
            val tenantDto = snapshot.toObject(TenantDto::class.java)?.copy(id = snapshot.id)
            if (tenantDto != null) {
                Resource.Success(tenantDto.toDomain())
            } else {
                Resource.Error("Tenant not found with ID: $id")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to retrieve tenant")
        }
    }

    override suspend fun addTenant(tenant: Tenant): Resource<Unit> {
        return try {
            val docRef = if (tenant.id.isNotBlank()) {
                tenantsCollection.document(tenant.id)
            } else {
                tenantsCollection.document()
            }
            val tenantDto = tenant.toDto().copy(id = docRef.id)
            docRef.set(tenantDto).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to add tenant")
        }
    }

    override suspend fun updateTenant(tenant: Tenant): Resource<Unit> {
        return try {
            if (tenant.id.isBlank()) {
                return Resource.Error("Tenant ID cannot be empty for update operation")
            }
            tenantsCollection.document(tenant.id).set(tenant.toDto()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update tenant")
        }
    }

    override suspend fun deleteTenant(id: String): Resource<Unit> {
        return try {
            tenantsCollection.document(id).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete tenant")
        }
    }
}
