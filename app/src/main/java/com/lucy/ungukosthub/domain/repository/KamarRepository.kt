package com.lucy.ungukosthub.domain.repository

import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.Kamar
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak (Interface) Repository di Domain Layer.
 * Menentukan operasi data yang dapat dilakukan tanpa peduli sumber datanya darimana.
 */
interface KamarRepository {
    fun getDaftarKamar(): Flow<Resource<List<Kamar>>>
    suspend fun tambahKamar(kamar: Kamar): Resource<Unit>
}
