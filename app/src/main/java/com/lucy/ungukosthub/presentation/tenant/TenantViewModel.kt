package com.lucy.ungukosthub.presentation.tenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.Tenant
import com.lucy.ungukosthub.domain.repository.RoomRepository
import com.lucy.ungukosthub.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TenantListUiState(
    val tenants: List<Tenant> = emptyList(),
    val filteredTenants: List<Tenant> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class AddTenantUiState(
    val nameInput: String = "",
    val originInput: String = "",
    val birthDateInput: String = "",
    val entryDateInput: String = "",
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

    fun observeTenants() {
        tenantRepository.getTenants().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Resource.Success -> {
                    val list = result.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        tenants = list,
                        filteredTenants = list,
                        isLoading = false,
                        errorMessage = null
                    )

                    // Auto-populate edit state if waiting for data
                    val pendingId = _editTenantState.value.tenantId
                    if (pendingId.isNotBlank() && _editTenantState.value.nameInput.isBlank()) {
                        val t = list.find { it.id == pendingId }
                        if (t != null) {
                            _editTenantState.value = EditTenantUiState(
                                tenantId = t.id,
                                nameInput = t.name,
                                originInput = t.origin,
                                birthDateInput = t.birthDate,
                                entryDateInput = t.entryDateText,
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
        val filtered = _uiState.value.tenants.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.roomId.contains(query, ignoreCase = true) ||
                    it.roomNumber.contains(query, ignoreCase = true) ||
                    it.origin.contains(query, ignoreCase = true)
        }
        _uiState.value = _uiState.value.copy(searchQuery = query, filteredTenants = filtered)
    }

    fun onNameChanged(value: String) { _addTenantState.value = _addTenantState.value.copy(nameInput = value) }
    fun onOriginChanged(value: String) { _addTenantState.value = _addTenantState.value.copy(originInput = value) }
    fun onBirthDateChanged(value: String) { _addTenantState.value = _addTenantState.value.copy(birthDateInput = value) }
    fun onEntryDateChanged(value: String) { _addTenantState.value = _addTenantState.value.copy(entryDateInput = value) }
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
        if (state.phoneInput.isBlank()) {
            _addTenantState.value = state.copy(errorMessage = "No. HP tidak boleh kosong")
            return
        }
        if (state.roomNumberInput.isBlank() && state.roomIdInput.isBlank()) {
            _addTenantState.value = state.copy(errorMessage = "Kamar yang Ditempati tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _addTenantState.value = state.copy(isLoading = true, errorMessage = null)
            val assignedRoomKey = state.roomIdInput.ifBlank { state.roomNumberInput }
            val newTenant = Tenant(
                name = state.nameInput.trim(),
                origin = state.originInput.trim(),
                birthDate = state.birthDateInput.trim(),
                phone = state.phoneInput.trim(),
                emergencyContact = state.phoneInput.trim(),
                roomId = assignedRoomKey,
                roomNumber = state.roomNumberInput.ifBlank { state.roomIdInput },
                ktpUrl = state.ktpPhotoUrlInput,
                status = "Aktif",
                entryDate = System.currentTimeMillis(),
                entryDateText = state.entryDateInput.trim()
            )

            tenantRepository.addTenant(newTenant)
            val updated = _uiState.value.tenants + newTenant
            _uiState.value = _uiState.value.copy(tenants = updated, filteredTenants = updated)

            // Sync room occupancy status to true
            if (assignedRoomKey.isNotBlank()) {
                when (val roomRes = roomRepository.getRoomById(assignedRoomKey)) {
                    is Resource.Success -> {
                        roomRes.data?.let { roomRepository.updateRoom(it.copy(isOccupied = true)) }
                    }
                    else -> {}
                }
            }

            _addTenantState.value = AddTenantUiState(isSuccess = true)
        }
    }

    fun resetAddTenantState() {
        _addTenantState.value = AddTenantUiState()
    }

    fun loadTenantForEdit(tenantId: String) {
        if (tenantId.isBlank()) return

        val existing = _uiState.value.tenants.find { it.id == tenantId }
        if (existing != null) {
            _editTenantState.value = EditTenantUiState(
                tenantId = existing.id,
                nameInput = existing.name,
                originInput = existing.origin,
                birthDateInput = existing.birthDate,
                entryDateInput = existing.entryDateText,
                phoneInput = existing.phone.ifBlank { existing.emergencyContact },
                roomIdInput = existing.roomId,
                roomNumberInput = existing.roomNumber.ifBlank { existing.roomId },
                ktpPhotoUrlInput = existing.ktpUrl,
                isLoading = false
            )
            return
        }

        viewModelScope.launch {
            _editTenantState.value = _editTenantState.value.copy(tenantId = tenantId, isLoading = true)
            when (val result = tenantRepository.getTenantById(tenantId)) {
                is Resource.Success -> {
                    val t = result.data
                    if (t != null) {
                        _editTenantState.value = EditTenantUiState(
                            tenantId = t.id,
                            nameInput = t.name,
                            originInput = t.origin,
                            birthDateInput = t.birthDate,
                            entryDateInput = t.entryDateText,
                            phoneInput = t.phone.ifBlank { t.emergencyContact },
                            roomIdInput = t.roomId,
                            roomNumberInput = t.roomNumber.ifBlank { t.roomId },
                            ktpPhotoUrlInput = t.ktpUrl,
                            isLoading = false
                        )
                    } else {
                        _editTenantState.value = _editTenantState.value.copy(isLoading = false)
                    }
                }
                is Resource.Error -> {
                    val fallback = _uiState.value.tenants.find { it.id == tenantId }
                    if (fallback != null) {
                        _editTenantState.value = EditTenantUiState(
                            tenantId = fallback.id,
                            nameInput = fallback.name,
                            originInput = fallback.origin,
                            birthDateInput = fallback.birthDate,
                            entryDateInput = fallback.entryDateText,
                            phoneInput = fallback.phone.ifBlank { fallback.emergencyContact },
                            roomIdInput = fallback.roomId,
                            roomNumberInput = fallback.roomNumber.ifBlank { fallback.roomId },
                            ktpPhotoUrlInput = fallback.ktpUrl,
                            isLoading = false
                        )
                    } else {
                        _editTenantState.value = _editTenantState.value.copy(isLoading = false)
                    }
                }
                else -> {
                    _editTenantState.value = _editTenantState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun onEditNameChanged(value: String) { _editTenantState.value = _editTenantState.value.copy(nameInput = value) }
    fun onEditOriginChanged(value: String) { _editTenantState.value = _editTenantState.value.copy(originInput = value) }
    fun onEditBirthDateChanged(value: String) { _editTenantState.value = _editTenantState.value.copy(birthDateInput = value) }
    fun onEditEntryDateChanged(value: String) { _editTenantState.value = _editTenantState.value.copy(entryDateInput = value) }
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
        if (state.phoneInput.isBlank()) {
            _editTenantState.value = state.copy(errorMessage = "No. HP tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _editTenantState.value = state.copy(isLoading = true, errorMessage = null)
            val oldTenant = _uiState.value.tenants.find { it.id == state.tenantId }
            val oldRoomKey = oldTenant?.roomId?.ifBlank { oldTenant.roomNumber } ?: ""
            val newRoomKey = state.roomIdInput.ifBlank { state.roomNumberInput }

            val updatedTenant = Tenant(
                id = state.tenantId,
                name = state.nameInput.trim(),
                origin = state.originInput.trim(),
                birthDate = state.birthDateInput.trim(),
                phone = state.phoneInput.trim(),
                emergencyContact = state.phoneInput.trim(),
                roomId = newRoomKey,
                roomNumber = state.roomNumberInput.ifBlank { state.roomIdInput },
                ktpUrl = state.ktpPhotoUrlInput,
                status = "Aktif",
                entryDateText = state.entryDateInput.trim()
            )

            tenantRepository.updateTenant(updatedTenant)
            val updatedList = _uiState.value.tenants.map {
                if (it.id == state.tenantId) updatedTenant else it
            }
            _uiState.value = _uiState.value.copy(tenants = updatedList, filteredTenants = updatedList)

            // Update old room if changed
            if (oldRoomKey.isNotBlank() && oldRoomKey != newRoomKey) {
                val remaining = updatedList.filter { (it.roomId == oldRoomKey || it.roomNumber == oldRoomKey) && it.id != state.tenantId }
                if (remaining.isEmpty()) {
                    when (val oldRes = roomRepository.getRoomById(oldRoomKey)) {
                        is Resource.Success -> oldRes.data?.let { roomRepository.updateRoom(it.copy(isOccupied = false)) }
                        else -> {}
                    }
                }
            }

            // Update new room
            if (newRoomKey.isNotBlank()) {
                when (val newRes = roomRepository.getRoomById(newRoomKey)) {
                    is Resource.Success -> newRes.data?.let { roomRepository.updateRoom(it.copy(isOccupied = true)) }
                    else -> {}
                }
            }

            _editTenantState.value = EditTenantUiState(isSuccess = true)
        }
    }

    fun resetEditTenantState() {
        _editTenantState.value = EditTenantUiState()
    }

    fun deleteTenant(tenantId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val oldTenant = _uiState.value.tenants.find { it.id == tenantId }
            val roomKey = oldTenant?.roomId?.ifBlank { oldTenant.roomNumber } ?: ""

            tenantRepository.deleteTenant(tenantId)
            val updated = _uiState.value.tenants.filter { it.id != tenantId }
            _uiState.value = _uiState.value.copy(tenants = updated, filteredTenants = updated)

            if (roomKey.isNotBlank()) {
                val remaining = updated.filter { it.roomId == roomKey || it.roomNumber == roomKey }
                if (remaining.isEmpty()) {
                    when (val roomRes = roomRepository.getRoomById(roomKey)) {
                        is Resource.Success -> roomRes.data?.let { roomRepository.updateRoom(it.copy(isOccupied = false)) }
                        else -> {}
                    }
                }
            }

            onSuccess()
        }
    }
}
