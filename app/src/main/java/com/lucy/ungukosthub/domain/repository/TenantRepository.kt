package com.lucy.ungukosthub.domain.repository

import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.Tenant
import kotlinx.coroutines.flow.Flow

/**
 * Domain Repository interface for managing Tenant data operations.
 */
interface TenantRepository {
    fun getTenants(): Flow<Resource<List<Tenant>>>
    fun getTenantsByRoomId(roomId: String): Flow<Resource<List<Tenant>>>
    suspend fun getTenantById(id: String): Resource<Tenant>
    suspend fun addTenant(tenant: Tenant): Resource<Unit>
    suspend fun updateTenant(tenant: Tenant): Resource<Unit>
    suspend fun deleteTenant(id: String): Resource<Unit>
}
