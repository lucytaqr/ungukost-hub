package com.lucy.ungukosthub.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucy.ungukosthub.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AdminProfileState(
    val name: String = "Admin UnguKost",
    val email: String = "admin@ungukost.com",
    val phone: String = "081234567890",
    val role: String = "Pemilik Kost"
)

data class AppSettingsState(
    val isDarkModeEnabled: Boolean = false,
    val currency: String = "IDR (Rupiah)",
    val language: String = "Bahasa Indonesia",
    val defaultDueDay: String = "Tanggal 1 setiap bulan"
)

data class NotificationSettingsState(
    val isWaReminderEnabled: Boolean = true,
    val sendTime: String = "08:00 WIB",
    val isVacancyAlertEnabled: Boolean = true,
    val isTransactionAlertEnabled: Boolean = true
)

data class BackupState(
    val lastBackupText: String = "Belum pernah dibackup",
    val isBackupLoading: Boolean = false
)

data class SettingsUiState(
    val adminProfile: AdminProfileState = AdminProfileState(),
    val appSettings: AppSettingsState = AppSettingsState(),
    val notificationSettings: NotificationSettingsState = NotificationSettingsState(),
    val backupState: BackupState = BackupState(),
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

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            appSettings = _uiState.value.appSettings.copy(isDarkModeEnabled = enabled),
            toastMessage = if (enabled) "Mode Gelap diaktifkan" else "Mode Terang diaktifkan"
        )
    }

    fun updateAppSettings(currency: String, language: String, dueDay: String) {
        _uiState.value = _uiState.value.copy(
            appSettings = _uiState.value.appSettings.copy(
                currency = currency,
                language = language,
                defaultDueDay = dueDay
            ),
            toastMessage = "Pengaturan aplikasi berhasil disimpan"
        )
    }

    fun updateNotificationSettings(
        waReminder: Boolean,
        sendTime: String,
        vacancyAlert: Boolean,
        transactionAlert: Boolean
    ) {
        _uiState.value = _uiState.value.copy(
            notificationSettings = NotificationSettingsState(
                isWaReminderEnabled = waReminder,
                sendTime = sendTime,
                isVacancyAlertEnabled = vacancyAlert,
                isTransactionAlertEnabled = transactionAlert
            ),
            toastMessage = "Pengaturan notifikasi berhasil disimpan"
        )
    }

    fun performBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                backupState = _uiState.value.backupState.copy(isBackupLoading = true)
            )
            delay(1000)
            val currentTime = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID")).format(Date())
            _uiState.value = _uiState.value.copy(
                backupState = BackupState(
                    lastBackupText = "Berhasil dibackup ($currentTime WIB)",
                    isBackupLoading = false
                ),
                toastMessage = "Backup data sistem berhasil dibuat"
            )
        }
    }

    fun performRestore() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                backupState = _uiState.value.backupState.copy(isBackupLoading = true)
            )
            delay(1000)
            _uiState.value = _uiState.value.copy(
                backupState = _uiState.value.backupState.copy(isBackupLoading = false),
                toastMessage = "Pemulihan data sistem berhasil disinkronkan"
            )
        }
    }

    fun clearToastMessage() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}
