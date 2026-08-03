package com.lucy.ungukosthub.presentation.settings

import androidx.lifecycle.ViewModel
import com.lucy.ungukosthub.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AdminProfileState(
    val name: String = "Admin UnguKost",
    val email: String = "admin@ungukost.com",
    val phone: String = "081330950655",
    val role: String = "Pemilik Kost"
)

data class SettingsUiState(
    val adminProfile: AdminProfileState = AdminProfileState(),
    val toastMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadAdminData()
    }

    private fun loadAdminData() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            _uiState.value = _uiState.value.copy(
                adminProfile = _uiState.value.adminProfile.copy(
                    name = user.displayName.ifBlank { "Admin UnguKost" },
                    email = user.email.ifBlank { "admin@ungukost.com" }
                )
            )
        }
    }

    fun updateAdminProfile(name: String, phone: String) {
        _uiState.value = _uiState.value.copy(
            adminProfile = _uiState.value.adminProfile.copy(name = name, phone = phone),
            toastMessage = "Profil Admin berhasil diperbarui"
        )
    }

    fun clearToastMessage() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}
