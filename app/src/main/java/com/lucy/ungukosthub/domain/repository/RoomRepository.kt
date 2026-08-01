package com.lucy.ungukosthub.domain.repository

import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.Room
import kotlinx.coroutines.flow.Flow

/**
 * Domain Repository interface for managing Room data operations.
 */
interface RoomRepository {
    fun getRooms(): Flow<Resource<List<Room>>>
    suspend fun getRoomById(id: String): Resource<Room>
    suspend fun addRoom(room: Room): Resource<Unit>
    suspend fun updateRoom(room: Room): Resource<Unit>
    suspend fun deleteRoom(id: String): Resource<Unit>
}
