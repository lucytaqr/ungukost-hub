package com.lucy.ungukosthub.domain.usecase

import android.util.Patterns
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.User
import com.lucy.ungukosthub.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase untuk proses Login.
 * Melakukan validasi awal (email & password) dan memanggil AuthRepository.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isEmpty()) {
            return Resource.Error("Email tidak boleh kosong")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Resource.Error("Format email tidak valid")
        }

        if (trimmedPassword.isEmpty()) {
            return Resource.Error("Password tidak boleh kosong")
        }

        if (trimmedPassword.length < 6) {
            return Resource.Error("Password minimal 6 karakter")
        }

        return authRepository.login(trimmedEmail, trimmedPassword)
    }
}
