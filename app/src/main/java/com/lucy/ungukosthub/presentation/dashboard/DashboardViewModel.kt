package com.lucy.ungukosthub.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucy.ungukosthub.core.util.Resource
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

data class PenghuniTunggakan(
    val id: String,
    val namaPenghuni: String,
    val nomorKamar: String,
    val statusTunggakan: String,
    val nominal: Double,
    val isCritical: Boolean
)

data class DashboardUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val totalKamar: Int = 0,
    val totalTerisi: Int = 0,
    val totalKosong: Int = 0,
    val totalEstimasiPendapatan: Double = 0.0,
    val targetPendapatan: Double = 0.0,
    val listTunggakan: List<PenghuniTunggakan> = emptyList(),
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

            _uiState.value = _uiState.value.copy(
                currentUser = user,
                isLoading = false,
                totalKamar = rooms.size,
                totalTerisi = occupiedCount,
                totalKosong = emptyCount,
                totalEstimasiPendapatan = totalIncomeEstimate,
                targetPendapatan = if (totalPossibleTarget > 0) totalPossibleTarget else 30000000.0,
                kamarList = rooms,
                listTunggakan = emptyList() // No hardcoded fallback
            )
        }.launchIn(viewModelScope)
    }

    fun logout() {
        authRepository.logout()
    }
}
