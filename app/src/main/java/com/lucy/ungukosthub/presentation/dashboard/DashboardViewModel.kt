package com.lucy.ungukosthub.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucy.ungukosthub.domain.model.Room
import com.lucy.ungukosthub.domain.model.Tenant
import com.lucy.ungukosthub.domain.model.User
import com.lucy.ungukosthub.domain.repository.AuthRepository
import com.lucy.ungukosthub.domain.repository.RoomRepository
import com.lucy.ungukosthub.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

data class TenantBillReminder(
    val tenantId: String = "",
    val tenantName: String = "",
    val roomNumber: String = "",
    val phone: String = "",
    val entryDateText: String = "",
    val amount: Double = 0.0
)

data class DashboardUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val totalKamar: Int = 0,
    val totalTerisi: Int = 0,
    val totalKosong: Int = 0,
    val totalEstimasiPendapatan: Double = 0.0,
    val targetPendapatan: Double = 0.0,
    val billReminders: List<TenantBillReminder> = emptyList(),
    val kamarList: List<Room> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val roomRepository: RoomRepository,
    private val tenantRepository: TenantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val authUser = authRepository.getCurrentUser()
        val user = authUser ?: User(
            uid = "owner_uid",
            email = "admin@ungukost.com",
            displayName = "Pemilik Kost"
        )

        _uiState.value = _uiState.value.copy(currentUser = user, isLoading = true)

        combine(
            roomRepository.getRooms(),
            tenantRepository.getTenants()
        ) { roomsRes, tenantsRes ->
            val rooms = roomsRes.data ?: emptyList()
            val tenants = tenantsRes.data ?: emptyList()

            val occupiedCount = rooms.count { room ->
                tenants.any { t -> t.roomId == room.id || t.roomNumber == room.roomNumber }
            }
            val emptyCount = (rooms.size - occupiedCount).coerceAtLeast(0)

            val totalIncomeEstimate = rooms.filter { room ->
                tenants.any { t -> t.roomId == room.id || t.roomNumber == room.roomNumber }
            }.sumOf { it.price }

            val totalPossibleTarget = rooms.sumOf { it.price }

            // Build Bill Reminders list for active tenants
            val reminders = tenants.map { tenant ->
                val matchedRoom = rooms.find { it.id == tenant.roomId || it.roomNumber == tenant.roomNumber }
                val roomPrice = matchedRoom?.price ?: 0.0
                val roomNum = tenant.roomNumber.ifBlank { tenant.roomId }

                TenantBillReminder(
                    tenantId = tenant.id,
                    tenantName = tenant.name,
                    roomNumber = roomNum,
                    phone = tenant.phone.ifBlank { tenant.emergencyContact },
                    entryDateText = tenant.entryDateText,
                    amount = roomPrice
                )
            }

            _uiState.value = _uiState.value.copy(
                currentUser = user,
                isLoading = false,
                totalKamar = rooms.size,
                totalTerisi = occupiedCount,
                totalKosong = emptyCount,
                totalEstimasiPendapatan = totalIncomeEstimate,
                targetPendapatan = if (totalPossibleTarget > 0) totalPossibleTarget else 30000000.0,
                kamarList = rooms,
                billReminders = reminders
            )
        }.launchIn(viewModelScope)
    }

    fun logout() {
        authRepository.logout()
    }
}
