package com.lucy.ungukosthub.data.remote

import com.lucy.ungukosthub.data.remote.dto.KamarDto
import kotlinx.coroutines.flow.Flow

/**
 * Interface pencapaian data mentah dari Remote (Firebase/API).
 */
interface KamarRemoteDataSource {
    fun getDaftarKamar(): Flow<List<KamarDto>>
    suspend fun tambahKamar(kamarDto: KamarDto)
}
