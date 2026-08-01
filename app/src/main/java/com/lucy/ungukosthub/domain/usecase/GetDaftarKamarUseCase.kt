package com.lucy.ungukosthub.domain.usecase

import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.Kamar
import com.lucy.ungukosthub.domain.repository.KamarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase (Business Logic) untuk mengambil daftar kamar.
 * Hanya fokus pada 1 tanggung jawab spesifik (Single Responsibility Principle).
 */
class GetDaftarKamarUseCase @Inject constructor(
    private val repository: KamarRepository
) {
    operator fun invoke(): Flow<Resource<List<Kamar>>> {
        return repository.getDaftarKamar()
    }
}
