package com.lucy.ungukosthub.data.repository

import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.data.remote.KamarRemoteDataSource
import com.lucy.ungukosthub.data.remote.dto.toDto
import com.lucy.ungukosthub.data.remote.dto.toDomain
import com.lucy.ungukosthub.domain.model.Kamar
import com.lucy.ungukosthub.domain.repository.KamarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementasi Repository yang menjembatani DataSource dengan Domain.
 * Mengubah DTO dari Data Layer menjadi Entity domain.
 */
class KamarRepositoryImpl @Inject constructor(
    private val remoteDataSource: KamarRemoteDataSource
) : KamarRepository {

    override fun getDaftarKamar(): Flow<Resource<List<Kamar>>> {
        return remoteDataSource.getDaftarKamar()
            .map { dtoList ->
                val domainList = dtoList.map { it.toDomain() }
                Resource.Success(domainList) as Resource<List<Kamar>>
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan pada server"))
            }
    }

    override suspend fun tambahKamar(kamar: Kamar): Resource<Unit> {
        return try {
            remoteDataSource.tambahKamar(kamar.toDto())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menambah kamar")
        }
    }
}
