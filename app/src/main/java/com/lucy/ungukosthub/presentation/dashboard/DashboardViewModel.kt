package com.lucy.ungukosthub.presentation.dashboard

import androidx.lifecycle.ViewModel
import com.lucy.ungukosthub.domain.model.Kamar
import com.lucy.ungukosthub.domain.model.User
import com.lucy.ungukosthub.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PenghuniTunggakan(
    val id: String,
    val namaPenghuni: String,
    val nomorKamar: String,
    val statusTunggakan: String,
    val nominal: Double,
    val isCritical: Boolean // true: Menunggak (Error Red), false: Jatuh Tempo (Warning Amber)
)

data class DashboardUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val totalKamar: Int = 20,
    val totalTerisi: Int = 15,
    val totalKosong: Int = 5,
    val totalEstimasiPendapatan: Double = 22500000.0,
    val targetPendapatan: Double = 30000000.0,
    val listTunggakan: List<PenghuniTunggakan> = emptyList(),
    val kamarList: List<Kamar> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadStaticDummyData()
    }

    fun loadStaticDummyData() {
        val authUser = authRepository.getCurrentUser()
        val user = authUser ?: User(
            uid = "dummy_owner_01",
            email = "admin@ungukost.com",
            displayName = "Pemilik Ungu Kost"
        )

        val dummyTunggakan = listOf(
            PenghuniTunggakan(
                id = "t1",
                namaPenghuni = "Rian Hidayat",
                nomorKamar = "102",
                statusTunggakan = "Menunggak 5 Hari",
                nominal = 1500000.0,
                isCritical = true
            ),
            PenghuniTunggakan(
                id = "t2",
                namaPenghuni = "Siti Rahmawati",
                nomorKamar = "205",
                statusTunggakan = "Menunggak 2 Hari",
                nominal = 1750000.0,
                isCritical = true
            ),
            PenghuniTunggakan(
                id = "t3",
                namaPenghuni = "Budi Santoso",
                nomorKamar = "108",
                statusTunggakan = "Jatuh Tempo Besok",
                nominal = 1500000.0,
                isCritical = false
            )
        )

        val dummyKamarList = listOf(
            Kamar(id = "k1", nomorKamar = "101", tipeKamar = "Deluxe AC", hargaSewa = 1800000.0, status = "Terisi"),
            Kamar(id = "k2", nomorKamar = "102", tipeKamar = "Standard", hargaSewa = 1500000.0, status = "Terisi"),
            Kamar(id = "k3", nomorKamar = "103", tipeKamar = "Standard", hargaSewa = 1500000.0, status = "Kosong"),
            Kamar(id = "k4", nomorKamar = "104", tipeKamar = "VIP Balon", hargaSewa = 2200000.0, status = "Terisi"),
            Kamar(id = "k5", nomorKamar = "105", tipeKamar = "Standard", hargaSewa = 1500000.0, status = "Kosong")
        )

        _uiState.value = DashboardUiState(
            currentUser = user,
            isLoading = false,
            totalKamar = 20,
            totalTerisi = 15,
            totalKosong = 5,
            totalEstimasiPendapatan = 22500000.0,
            targetPendapatan = 30000000.0,
            listTunggakan = dummyTunggakan,
            kamarList = dummyKamarList
        )
    }

    fun logout() {
        authRepository.logout()
    }
}

