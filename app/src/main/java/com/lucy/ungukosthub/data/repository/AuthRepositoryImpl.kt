package com.lucy.ungukosthub.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.User
import com.lucy.ungukosthub.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementasi AuthRepository yang berkomunikasi langsung dengan Firebase Authentication.
 */
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    displayName = firebaseUser.displayName ?: ""
                )
                Resource.Success(user)
            } else {
                Resource.Error("Gagal mendapatkan data pengguna setelah login.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal melakukan login. Periksa email dan password Anda.")
        }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: ""
        )
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}
