package com.lucy.ungukosthub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.data.remote.dto.RoomDto
import com.lucy.ungukosthub.data.remote.dto.toDomain
import com.lucy.ungukosthub.data.remote.dto.toDto
import com.lucy.ungukosthub.domain.model.Room
import com.lucy.ungukosthub.domain.repository.RoomRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Data Layer implementation of RoomRepository using Firebase Firestore ('rooms' collection).
 */
class RoomRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RoomRepository {

    private val roomsCollection = firestore.collection("rooms")

    override fun getRooms(): Flow<Resource<List<Room>>> = callbackFlow {
        trySend(Resource.Loading())
        val listenerRegistration = roomsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "An error occurred while fetching rooms"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val rooms = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(RoomDto::class.java)?.copy(id = doc.id)?.toDomain()
                }
                trySend(Resource.Success(rooms))
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun getRoomById(id: String): Resource<Room> {
        return try {
            val snapshot = roomsCollection.document(id).get().await()
            val roomDto = snapshot.toObject(RoomDto::class.java)?.copy(id = snapshot.id)
            if (roomDto != null) {
                Resource.Success(roomDto.toDomain())
            } else {
                Resource.Error("Room not found with ID: $id")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to retrieve room")
        }
    }

    override suspend fun addRoom(room: Room): Resource<Unit> {
        return try {
            val docRef = if (room.id.isNotBlank()) {
                roomsCollection.document(room.id)
            } else {
                roomsCollection.document()
            }
            val roomDto = room.toDto().copy(id = docRef.id)
            docRef.set(roomDto).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to add room")
        }
    }

    override suspend fun updateRoom(room: Room): Resource<Unit> {
        return try {
            if (room.id.isBlank()) {
                return Resource.Error("Room ID cannot be empty for update operation")
            }
            roomsCollection.document(room.id).set(room.toDto()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update room")
        }
    }

    override suspend fun deleteRoom(id: String): Resource<Unit> {
        return try {
            roomsCollection.document(id).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete room")
        }
    }
}
