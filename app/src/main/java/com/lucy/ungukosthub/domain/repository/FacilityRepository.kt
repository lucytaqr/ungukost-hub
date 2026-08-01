package com.lucy.ungukosthub.domain.repository

import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.Facility
import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository untuk mengelola data Fasilitas di Cloud Firestore.
 */
interface FacilityRepository {
    fun getFacilities(): Flow<Resource<List<Facility>>>
    suspend fun addFacility(facility: Facility): Resource<Unit>
}
