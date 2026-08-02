package com.lucy.ungukosthub.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucy.ungukosthub.domain.model.Room
import com.lucy.ungukosthub.domain.model.TransactionType
import com.lucy.ungukosthub.domain.model.User
import com.lucy.ungukosthub.domain.repository.AuthRepository
import com.lucy.ungukosthub.domain.repository.RoomRepository
import com.lucy.ungukosthub.domain.repository.TenantRepository
import com.lucy.ungukosthub.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
    val monthlyChartLabels: List<String> = emptyList(),
    val monthlyChartIncome: List<Double> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val roomRepository: RoomRepository,
    private val tenantRepository: TenantRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun formatMonthYear(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        return format.format(date)
    }

    private fun loadDashboardData() {
        val authUser = authRepository.getCurrentUser()
        val user = authUser ?: User(
            uid = "owner_uid",
            email = "admin@ungukost.com",
            displayName = "Pemilik Kost"
        )

        _uiState.value = _uiState.value.copy(currentUser = user, isLoading = true)

        val currentMonthStr = formatMonthYear(System.currentTimeMillis())

        combine(
            roomRepository.getRooms(),
            tenantRepository.getTenants(),
            transactionRepository.getTransactions()
        ) { roomsRes, tenantsRes, transactionsRes ->
            val rooms = roomsRes.data ?: emptyList()
            val tenants = tenantsRes.data ?: emptyList()
            val transactions = transactionsRes.data ?: emptyList()

            val occupiedCount = rooms.count { room ->
                tenants.any { t -> t.roomId == room.id || t.roomNumber == room.roomNumber }
            }
            val emptyCount = (rooms.size - occupiedCount).coerceAtLeast(0)

            // Hitung pendapatan bulan ini dari transaksi Firestore (atau sewa kamar terisi jika belum ada transaksi)
            val currentMonthIncomeTransactions = transactions
                .filter { it.type == TransactionType.INCOME && formatMonthYear(it.timestamp) == currentMonthStr }
                .sumOf { it.amount }

            val totalOccupiedRoomValue = rooms.filter { room ->
                tenants.any { t -> t.roomId == room.id || t.roomNumber == room.roomNumber }
            }.sumOf { it.price }

            val realMonthIncome = if (currentMonthIncomeTransactions > 0) {
                currentMonthIncomeTransactions
            } else {
                totalOccupiedRoomValue
            }

            val totalPossibleTarget = rooms.sumOf { it.price }

            // Build 6 Bulan Terakhir untuk Grafik Pendapatan Real
            val cal = Calendar.getInstance()
            val monthLabels = mutableListOf<String>()
            val monthIncomeValues = mutableListOf<Double>()
            val shortMonthFormat = SimpleDateFormat("MMM", Locale("id", "ID"))
            val fullMonthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))

            // Ambil 6 bulan terakhir ke belakang hingga bulan ini
            for (i in 5 downTo 0) {
                val tempCal = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -i)
                }
                val label = shortMonthFormat.format(tempCal.time)
                val fullKey = fullMonthFormat.format(tempCal.time)

                val monthIncome = transactions
                    .filter { it.type == TransactionType.INCOME && formatMonthYear(it.timestamp) == fullKey }
                    .sumOf { it.amount }

                monthLabels.add(label)
                monthIncomeValues.add(monthIncome)
            }

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
                totalEstimasiPendapatan = realMonthIncome,
                targetPendapatan = if (totalPossibleTarget > 0) totalPossibleTarget else 30000000.0,
                kamarList = rooms,
                billReminders = reminders,
                monthlyChartLabels = monthLabels,
                monthlyChartIncome = monthIncomeValues
            )
        }.launchIn(viewModelScope)
    }

    fun logout() {
        authRepository.logout()
    }
}
