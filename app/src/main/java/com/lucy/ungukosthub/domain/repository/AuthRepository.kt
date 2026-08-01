package com.lucy.ungukosthub.domain.repository

import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.User

/**
 * Kontrak Repository untuk Autentikasi Pengguna di Domain Layer.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<User>
    fun getCurrentUser(): User?
    fun logout()
}
