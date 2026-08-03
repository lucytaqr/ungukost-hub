package com.lucy.ungukosthub.presentation.tenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.Tenant
import com.lucy.ungukosthub.domain.model.isActive
import com.lucy.ungukosthub.domain.repository.RoomRepository
import com.lucy.ungukosthub.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class TenantFilterCategory {
    ALL,
    ACTIVE,
    INACTIVE
}

data class TenantListUiState(
    val tenants: List<Tenant> = emptyList(),
    val filteredTenants: List<Tenant> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: TenantFilterCategory = TenantFilterCategory.ALL,
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val inactiveCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class AddTenantUiState(
    val nameInput: String = "",
    val originInput: String = "",
    val birthDateInput: String = "",
    val entryDateInput: String = "",
    val exitDateInput: String = "",
    val phoneInput: String = "",
    val roomIdInput: String = "",
    val roomNumberInput: String = "",
    val ktpPhotoUrlInput: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class EditTenantUiState(
    val tenantId: String = "",
    val nameInput: String = "",
    val originInput: String = "",
    val birthDateInput: String = "",
    val entryDateInput: String = "",
    val exitDateInput: String = "",
    val phoneInput: String = "",
    val roomIdInput: String = "",
    val roomNumberInput: String = "",
    val ktpPhotoUrlInput: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TenantViewModel @Inject constructor(
    private val tenantRepository: TenantRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantListUiState())
    val uiState: StateFlow<TenantListUiState> = _uiState.asStateFlow()

    private val _addTenantState = MutableStateFlow(AddTenantUiState())
    val addTenantState: StateFlow<AddTenantUiState> = _addTenantState.asStateFlow()

    private val _editTenantState = MutableStateFlow(EditTenantUiState())
    val editTenantState: StateFlow<EditTenantUiState> = _editTenantState.asStateFlow()

    init {
        observeTenants()
    }

    private fun isTenantActiveByDate(exitDateText: String): Boolean {
        if (exitDateText.isBlank()) return true
        return try {
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val exitDate = sdf.parse(exitDateText) ?: return true
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            !exitDate.before(todayCal.time)
        } catch (e: Exception) {
            true
        }
    }

    private fun applyTenantFilters(
        allTenants: List<Tenant>,
        query: String,
        filter: TenantFilterCategory
    ) {
        val sortedList = allTenants.sortedBy { it.name.lowercase() }
        val total = sortedList.size
        val active = sortedList.count { it.isActive() }
        val inactive = sortedList.count { !it.isActive() }

        val searched = if (query.isBlank()) {
            sortedList
        } else {
            sortedList.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.roomId.contains(query, ignoreCase = true) ||
                        it.roomNumber.contains(query, ignoreCase = true) ||
                        it.origin.contains(query, ignoreCase = true)
            }
        }

        val filtered = when (filter) {
            TenantFilterCategory.ALL -> searched
            TenantFilterCategory.ACTIVE -> searched.filter { it.isActive() }
            TenantFilterCategory.INACTIVE -> searched.filter { !it.isActive() }
        }

        _uiState.value = _uiState.value.copy(
            tenants = sortedList,
            filteredTenants = filtered,
            searchQuery = query,
            selectedFilter = filter,
            totalCount = total,
            activeCount = active,
            inactiveCount = inactive,
            isLoading = false,
            errorMessage = null
        )
    }

    fun observeTenants() {
        tenantRepository.getTenants().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Resource.Success -> {
                    val rawList = result.data ?: emptyList()
                    applyTenantFilters(
                        allTenants = rawList,
                        query = _uiState.value.searchQuery,
                        filter = _uiState.value.selectedFilter
                    )

                    // Auto-populate edit state if waiting for data
                    val pendingId = _editTenantState.value.tenantId
                    if (pendingId.isNotBlank() && _editTenantState.value.nameInput.isBlank()) {
                        val t = rawList.find { it.id == pendingId }
                        if (t != null) {
                            _editTenantState.value = EditTenantUiState(
                                tenantId = t.id,
                                nameInput = t.name,
                                originInput = t.origin,
                                birthDateInput = t.birthDate,
                                entryDateInput = t.entryDateText,
                                exitDateInput = t.exitDateText,
                                phoneInput = t.phone.ifBlank { t.emergencyContact },
                                roomIdInput = t.roomId,
                                roomNumberInput = t.roomNumber.ifBlank { t.roomId },
                                ktpPhotoUrlInput = t.ktpUrl,
                                isLoading = false
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        tenants = emptyList(),
                        filteredTenants = emptyList(),
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        applyTenantFilters(
            allTenants = _uiState.value.tenants,
            query = query,
            filter = _uiState.value.selectedFilter
        )
    }

    fun onFilterSelected(filter: TenantFilterCategory) {
        applyTenantFilters(
            allTenants = _uiState.value.tenants,
            query = _uiState.value.searchQuery,
            filter = filter
        )
    }

    fun onNameChanged(value: String) { _addTenantState.value = _addTenantState.value.copy(nameInput = value) }
    fun onOriginChanged(value: String) { _addTenantState.value = _addTenantState.value.copy(originInput = value) }
    fun onBirthDateChanged(value: String) { _addTenantState.value = _addTenantState.value.copy(birthDateInput = value) }
    fun onEntryDateChanged(value: String) { _addTenantState.value = _addTenantState.value.copy(entryDateInput = value) }
    fun onExitDateChanged(value: String) { _addTenantState.value = _addTenantState.value.copy(exitDateInput = value) }
    fun onPhoneChanged(value: String) { _addTenantState.value = _addTenantState.value.copy(phoneInput = value) }
    fun onRoomSelected(roomId: String, roomNumber: String) {
        _addTenantState.value = _addTenantState.value.copy(roomIdInput = roomId, roomNumberInput = roomNumber)
    }
    fun onKtpPhotoUrlChanged(url: String) { _addTenantState.value = _addTenantState.value.copy(ktpPhotoUrlInput = url) }

    fun addTenant() {
        val state = _addTenantState.value
        if (state.nameInput.isBlank()) {
            _addTenantState.value = state.copy(errorMessage = "Nama Lengkap tidak boleh kosong")
            return
        }
        if (state.roomNumberInput.isBlank() && state.roomIdInput.isBlank()) {
            _addTenantState.value = state.copy(errorMessage = "Kamar yang Ditempati tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _addTenantState.value = state.copy(isLoading = true, errorMessage = null)
            val assignedRoomKey = state.roomIdInput.ifBlank { state.roomNumberInput }
            val isStillOccupied = isTenantActiveByDate(state.exitDateInput)

            val newTenant = Tenant(
                name = state.nameInput.trim(),
                origin = state.originInput.trim(),
                birthDate = state.birthDateInput.trim(),
                phone = state.phoneInput.trim(),
                emergencyContact = state.phoneInput.trim(),
                roomId = assignedRoomKey,
                roomNumber = state.roomNumberInput.ifBlank { state.roomIdInput },
                ktpUrl = state.ktpPhotoUrlInput,
                status = if (isStillOccupied) "Aktif" else "Keluar",
                entryDate = System.currentTimeMillis(),
                entryDateText = state.entryDateInput.trim(),
                exitDateText = state.exitDateInput.trim()
            )

            tenantRepository.addTenant(newTenant)
            val updatedList = _uiState.value.tenants + newTenant
            applyTenantFilters(
                allTenants = updatedList,
                query = _uiState.value.searchQuery,
                filter = _uiState.value.selectedFilter
            )

            _addTenantState.value = AddTenantUiState(isSuccess = true)
        }
    }

    fun resetAddTenantState() {
        _addTenantState.value = AddTenantUiState()
    }

    fun loadTenantForEdit(tenantId: String) {
        val t = _uiState.value.tenants.find { it.id == tenantId }
        if (t != null) {
            _editTenantState.value = EditTenantUiState(
                tenantId = t.id,
                nameInput = t.name,
                originInput = t.origin,
                birthDateInput = t.birthDate,
                entryDateInput = t.entryDateText,
                exitDateInput = t.exitDateText,
                phoneInput = t.phone.ifBlank { t.emergencyContact },
                roomIdInput = t.roomId,
                roomNumberInput = t.roomNumber.ifBlank { t.roomId },
                ktpPhotoUrlInput = t.ktpUrl,
                isLoading = false
            )
        } else {
            _editTenantState.value = EditTenantUiState(tenantId = tenantId)
        }
    }

    fun onEditNameChanged(value: String) { _editTenantState.value = _editTenantState.value.copy(nameInput = value) }
    fun onEditOriginChanged(value: String) { _editTenantState.value = _editTenantState.value.copy(originInput = value) }
    fun onEditBirthDateChanged(value: String) { _editTenantState.value = _editTenantState.value.copy(birthDateInput = value) }
    fun onEditEntryDateChanged(value: String) { _editTenantState.value = _editTenantState.value.copy(entryDateInput = value) }
    fun onEditExitDateChanged(value: String) { _editTenantState.value = _editTenantState.value.copy(exitDateInput = value) }
    fun onEditPhoneChanged(value: String) { _editTenantState.value = _editTenantState.value.copy(phoneInput = value) }
    fun onEditRoomSelected(roomId: String, roomNumber: String) {
        _editTenantState.value = _editTenantState.value.copy(roomIdInput = roomId, roomNumberInput = roomNumber)
    }
    fun onEditKtpPhotoUrlChanged(url: String) { _editTenantState.value = _editTenantState.value.copy(ktpPhotoUrlInput = url) }

    fun updateTenant() {
        val state = _editTenantState.value
        if (state.nameInput.isBlank()) {
            _editTenantState.value = state.copy(errorMessage = "Nama Lengkap tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _editTenantState.value = state.copy(isLoading = true, errorMessage = null)
            val assignedRoomKey = state.roomIdInput.ifBlank { state.roomNumberInput }
            val isStillOccupied = isTenantActiveByDate(state.exitDateInput)

            val updatedTenant = Tenant(
                id = state.tenantId,
                name = state.nameInput.trim(),
                origin = state.originInput.trim(),
                birthDate = state.birthDateInput.trim(),
                phone = state.phoneInput.trim(),
                emergencyContact = state.phoneInput.trim(),
                roomId = assignedRoomKey,
                roomNumber = state.roomNumberInput.ifBlank { state.roomIdInput },
                ktpUrl = state.ktpPhotoUrlInput,
                status = if (isStillOccupied) "Aktif" else "Keluar",
                entryDateText = state.entryDateInput.trim(),
                exitDateText = state.exitDateInput.trim()
            )

            tenantRepository.updateTenant(updatedTenant)

            val newList = _uiState.value.tenants.map { if (it.id == updatedTenant.id) updatedTenant else it }
            applyTenantFilters(
                allTenants = newList,
                query = _uiState.value.searchQuery,
                filter = _uiState.value.selectedFilter
            )

            _editTenantState.value = EditTenantUiState(isSuccess = true)
        }
    }

    fun resetEditTenantState() {
        _editTenantState.value = EditTenantUiState()
    }

    fun deleteTenant(tenantId: String) {
        viewModelScope.launch {
            val t = _uiState.value.tenants.find { it.id == tenantId }
            tenantRepository.deleteTenant(tenantId)

            val updated = _uiState.value.tenants.filter { it.id != tenantId }
            applyTenantFilters(
                allTenants = updated,
                query = _uiState.value.searchQuery,
                filter = _uiState.value.selectedFilter
            )
        }
    }
}
