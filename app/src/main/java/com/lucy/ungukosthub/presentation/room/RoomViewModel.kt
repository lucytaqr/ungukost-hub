package com.lucy.ungukosthub.presentation.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.Facility
import com.lucy.ungukosthub.domain.model.Room
import com.lucy.ungukosthub.domain.model.isActive
import com.lucy.ungukosthub.domain.repository.FacilityRepository
import com.lucy.ungukosthub.domain.repository.RoomRepository
import com.lucy.ungukosthub.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RoomFilterCategory {
    ALL,
    OCCUPIED,
    VACANT
}

data class RoomListUiState(
    val rooms: List<Room> = emptyList(),
    val filteredRooms: List<Room> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: RoomFilterCategory = RoomFilterCategory.ALL,
    val totalCount: Int = 0,
    val occupiedCount: Int = 0,
    val vacantCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class AddRoomUiState(
    val roomNumberInput: String = "",
    val categoryInput: String = "Kamar",
    val priceInput: String = "",
    val isOccupiedInput: Boolean = false,
    val photoUrlInput: String = "",
    val photoUrlsList: List<String> = emptyList(),
    val facilityInput: String = "",
    val facilitiesList: List<String> = emptyList(),
    val facilitySuggestions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class EditRoomUiState(
    val roomId: String = "",
    val roomNumberInput: String = "",
    val categoryInput: String = "Kamar",
    val priceInput: String = "",
    val isOccupiedInput: Boolean = false,
    val photoUrlInput: String = "",
    val photoUrlsList: List<String> = emptyList(),
    val facilityInput: String = "",
    val facilitiesList: List<String> = emptyList(),
    val facilitySuggestions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val facilityRepository: FacilityRepository,
    private val tenantRepository: TenantRepository
) : ViewModel() {

    private val _roomListState = MutableStateFlow(RoomListUiState())
    val roomListState: StateFlow<RoomListUiState> = _roomListState.asStateFlow()

    private val _addRoomState = MutableStateFlow(AddRoomUiState())
    val addRoomState: StateFlow<AddRoomUiState> = _addRoomState.asStateFlow()

    private val _editRoomState = MutableStateFlow(EditRoomUiState())
    val editRoomState: StateFlow<EditRoomUiState> = _editRoomState.asStateFlow()

    private var allFacilities: List<Facility> = emptyList()

    init {
        observeRoomsAndTenants()
        observeFacilities()
    }

    private fun extractRoomNumberDigits(roomNumber: String): Int {
        val digits = roomNumber.filter { it.isDigit() }
        return digits.toIntOrNull() ?: Int.MAX_VALUE
    }

    private fun observeRoomsAndTenants() {
        combine(
            roomRepository.getRooms(),
            tenantRepository.getTenants()
        ) { roomResult, tenantResult ->
            val rooms = roomResult.data ?: emptyList()
            val tenants = tenantResult.data ?: emptyList()

            rooms.map { room ->
                val hasActiveTenant = tenants.any { tenant ->
                    tenant.isActive() && (
                        (tenant.roomId.isNotBlank() && (tenant.roomId == room.id || tenant.roomId == room.roomNumber)) ||
                        (tenant.roomNumber.isNotBlank() && (tenant.roomNumber == room.roomNumber || tenant.roomNumber == room.id))
                    )
                }
                room.copy(isOccupied = hasActiveTenant)
            }
        }.onEach { updatedRooms ->
            updateListState(updatedRooms)

            // Auto populate edit state if waiting
            val pendingId = _editRoomState.value.roomId
            if (pendingId.isNotBlank() && _editRoomState.value.roomNumberInput.isBlank()) {
                val t = updatedRooms.find { it.id == pendingId || it.roomNumber == pendingId }
                if (t != null) {
                    val priceStr = if (t.price > 0) t.price.toLong().toString() else ""
                    val photos = if (t.photoUrls.isNotEmpty()) t.photoUrls else if (t.photoUrl.isNotBlank()) listOf(t.photoUrl) else emptyList()
                    _editRoomState.value = EditRoomUiState(
                        roomId = t.id,
                        roomNumberInput = t.roomNumber,
                        categoryInput = t.category.ifBlank { "Kamar" },
                        priceInput = priceStr,
                        isOccupiedInput = t.isOccupied,
                        photoUrlInput = t.photoUrl,
                        photoUrlsList = photos,
                        facilitiesList = t.facilities,
                        facilitySuggestions = allFacilities.map { it.name }
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun observeFacilities() {
        facilityRepository.getFacilities().onEach { result ->
            if (result is Resource.Success) {
                allFacilities = result.data ?: emptyList()
                val suggestionNames = allFacilities.map { it.name }
                _addRoomState.value = _addRoomState.value.copy(facilitySuggestions = suggestionNames)
                _editRoomState.value = _editRoomState.value.copy(facilitySuggestions = suggestionNames)
            }
        }.launchIn(viewModelScope)
    }

    private fun updateListState(rooms: List<Room>) {
        val sortedRooms = rooms.sortedWith(
            compareBy<Room> { extractRoomNumberDigits(it.roomNumber) }
                .thenBy { it.roomNumber }
        )
        val total = sortedRooms.size
        val occupied = sortedRooms.count { it.isOccupied }
        val vacant = total - occupied

        _roomListState.value = _roomListState.value.copy(
            rooms = sortedRooms,
            totalCount = total,
            occupiedCount = occupied,
            vacantCount = vacant,
            isLoading = false
        )
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _roomListState.value
        var filtered = currentState.rooms

        // Search Filter
        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.lowercase().trim()
            filtered = filtered.filter { room ->
                room.roomNumber.lowercase().contains(query) ||
                        room.category.lowercase().contains(query)
            }
        }

        // Category Filter
        filtered = when (currentState.selectedFilter) {
            RoomFilterCategory.ALL -> filtered
            RoomFilterCategory.OCCUPIED -> filtered.filter { it.isOccupied }
            RoomFilterCategory.VACANT -> filtered.filter { !it.isOccupied }
        }

        _roomListState.value = currentState.copy(filteredRooms = filtered)
    }

    fun onSearchQueryChanged(query: String) {
        _roomListState.value = _roomListState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onFilterSelected(filter: RoomFilterCategory) {
        _roomListState.value = _roomListState.value.copy(selectedFilter = filter)
        applyFilters()
    }

    fun onRoomNumberChanged(value: String) {
        _addRoomState.value = _addRoomState.value.copy(roomNumberInput = value)
    }

    fun onCategoryChanged(value: String) {
        _addRoomState.value = _addRoomState.value.copy(categoryInput = value)
    }

    fun onPriceChanged(value: String) {
        _addRoomState.value = _addRoomState.value.copy(priceInput = value.filter { it.isDigit() })
    }

    fun onOccupiedStatusChanged(isOccupied: Boolean) {
        _addRoomState.value = _addRoomState.value.copy(isOccupiedInput = isOccupied)
    }

    fun onFacilityChanged(value: String) {
        _addRoomState.value = _addRoomState.value.copy(facilityInput = value)
    }

    fun onAddFacility(facility: String) {
        val trimmed = facility.trim()
        if (trimmed.isNotBlank() && !_addRoomState.value.facilitiesList.contains(trimmed)) {
            val updatedList = _addRoomState.value.facilitiesList + trimmed
            _addRoomState.value = _addRoomState.value.copy(
                facilitiesList = updatedList,
                facilityInput = ""
            )
        }
    }

    fun onRemoveFacility(facility: String) {
        val updatedList = _addRoomState.value.facilitiesList.filter { it != facility }
        _addRoomState.value = _addRoomState.value.copy(facilitiesList = updatedList)
    }

    fun onAddPhotoUrls(urls: List<String>) {
        val newUrls = urls.filter { it.isNotBlank() && !_addRoomState.value.photoUrlsList.contains(it) }
        val updated = _addRoomState.value.photoUrlsList + newUrls
        val primaryPhoto = updated.firstOrNull() ?: ""
        _addRoomState.value = _addRoomState.value.copy(
            photoUrlsList = updated,
            photoUrlInput = primaryPhoto
        )
    }

    fun onRemovePhotoUrl(url: String) {
        val updated = _addRoomState.value.photoUrlsList.filter { it != url }
        val primaryPhoto = updated.firstOrNull() ?: ""
        _addRoomState.value = _addRoomState.value.copy(
            photoUrlsList = updated,
            photoUrlInput = primaryPhoto
        )
    }

    fun addRoom() {
        val state = _addRoomState.value
        val roomNumber = state.roomNumberInput.trim()
        val category = state.categoryInput.trim()
        val price = state.priceInput.toDoubleOrNull() ?: 0.0

        if (roomNumber.isBlank()) {
            _addRoomState.value = _addRoomState.value.copy(errorMessage = "Nomor Kamar tidak boleh kosong")
            return
        }
        if (category.isBlank()) {
            _addRoomState.value = _addRoomState.value.copy(errorMessage = "Kategori tidak boleh kosong")
            return
        }
        if (price <= 0) {
            _addRoomState.value = _addRoomState.value.copy(errorMessage = "Harga sewa harus lebih dari 0")
            return
        }

        viewModelScope.launch {
            _addRoomState.value = _addRoomState.value.copy(isLoading = true, errorMessage = null)
            val primaryPhoto = state.photoUrlsList.firstOrNull() ?: state.photoUrlInput
            val newRoom = Room(
                roomNumber = roomNumber,
                category = category,
                price = price,
                isOccupied = state.isOccupiedInput,
                photoUrl = primaryPhoto,
                photoUrls = state.photoUrlsList,
                facilities = state.facilitiesList
            )
            when (val result = roomRepository.addRoom(newRoom)) {
                is Resource.Success -> {
                    val updatedList = _roomListState.value.rooms + newRoom
                    updateListState(updatedList)
                    _addRoomState.value = AddRoomUiState(isSuccess = true)
                }
                is Resource.Error -> {
                    val updatedList = _roomListState.value.rooms + newRoom
                    updateListState(updatedList)
                    _addRoomState.value = AddRoomUiState(isSuccess = true)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadRoomForEdit(roomId: String) {
        if (roomId.isBlank()) return
        _editRoomState.value = _editRoomState.value.copy(roomId = roomId, isLoading = true)

        val target = _roomListState.value.rooms.find { it.id == roomId || it.roomNumber == roomId }
        if (target != null) {
            val priceStr = if (target.price > 0) target.price.toLong().toString() else ""
            val photos = if (target.photoUrls.isNotEmpty()) target.photoUrls else if (target.photoUrl.isNotBlank()) listOf(target.photoUrl) else emptyList()
            _editRoomState.value = EditRoomUiState(
                roomId = target.id,
                roomNumberInput = target.roomNumber,
                categoryInput = target.category.ifBlank { "Kamar" },
                priceInput = priceStr,
                isOccupiedInput = target.isOccupied,
                photoUrlInput = target.photoUrl,
                photoUrlsList = photos,
                facilitiesList = target.facilities,
                facilitySuggestions = allFacilities.map { it.name },
                isLoading = false
            )
            return
        }

        viewModelScope.launch {
            when (val result = roomRepository.getRoomById(roomId)) {
                is Resource.Success -> {
                    val t = result.data
                    if (t != null) {
                        val priceStr = if (t.price > 0) t.price.toLong().toString() else ""
                        val photos = if (t.photoUrls.isNotEmpty()) t.photoUrls else if (t.photoUrl.isNotBlank()) listOf(t.photoUrl) else emptyList()
                        _editRoomState.value = EditRoomUiState(
                            roomId = t.id,
                            roomNumberInput = t.roomNumber,
                            categoryInput = t.category.ifBlank { "Kamar" },
                            priceInput = priceStr,
                            isOccupiedInput = t.isOccupied,
                            photoUrlInput = t.photoUrl,
                            photoUrlsList = photos,
                            facilitiesList = t.facilities,
                            facilitySuggestions = allFacilities.map { it.name },
                            isLoading = false
                        )
                    } else {
                        _editRoomState.value = _editRoomState.value.copy(isLoading = false)
                    }
                }
                is Resource.Error -> {
                    _editRoomState.value = _editRoomState.value.copy(isLoading = false)
                }
                else -> {}
            }
        }
    }

    fun onEditRoomNumberChanged(value: String) { _editRoomState.value = _editRoomState.value.copy(roomNumberInput = value) }
    fun onEditCategoryChanged(value: String) { _editRoomState.value = _editRoomState.value.copy(categoryInput = value) }
    fun onEditPriceChanged(value: String) { _editRoomState.value = _editRoomState.value.copy(priceInput = value.filter { it.isDigit() }) }
    fun onEditFacilityChanged(value: String) { _editRoomState.value = _editRoomState.value.copy(facilityInput = value) }

    fun onEditAddFacility(facility: String) {
        val trimmed = facility.trim()
        if (trimmed.isNotBlank() && !_editRoomState.value.facilitiesList.contains(trimmed)) {
            val updated = _editRoomState.value.facilitiesList + trimmed
            _editRoomState.value = _editRoomState.value.copy(facilitiesList = updated, facilityInput = "")
        }
    }

    fun onEditRemoveFacility(facility: String) {
        val updated = _editRoomState.value.facilitiesList.filter { it != facility }
        _editRoomState.value = _editRoomState.value.copy(facilitiesList = updated)
    }

    fun onEditAddPhotoUrls(urls: List<String>) {
        val newUrls = urls.filter { it.isNotBlank() && !_editRoomState.value.photoUrlsList.contains(it) }
        val updated = _editRoomState.value.photoUrlsList + newUrls
        val primaryPhoto = updated.firstOrNull() ?: ""
        _editRoomState.value = _editRoomState.value.copy(photoUrlsList = updated, photoUrlInput = primaryPhoto)
    }

    fun onEditRemovePhotoUrl(url: String) {
        val updated = _editRoomState.value.photoUrlsList.filter { it != url }
        val primaryPhoto = updated.firstOrNull() ?: ""
        _editRoomState.value = _editRoomState.value.copy(photoUrlsList = updated, photoUrlInput = primaryPhoto)
    }

    fun updateRoom() {
        val state = _editRoomState.value
        val roomNumber = state.roomNumberInput.trim()
        val category = state.categoryInput.trim()
        val price = state.priceInput.toDoubleOrNull() ?: 0.0

        if (roomNumber.isBlank()) {
            _editRoomState.value = state.copy(errorMessage = "Nomor Kamar tidak boleh kosong")
            return
        }
        if (price <= 0) {
            _editRoomState.value = state.copy(errorMessage = "Harga sewa harus lebih dari 0")
            return
        }

        viewModelScope.launch {
            _editRoomState.value = state.copy(isLoading = true, errorMessage = null)
            val primaryPhoto = state.photoUrlsList.firstOrNull() ?: state.photoUrlInput
            val updatedRoom = Room(
                id = state.roomId,
                roomNumber = roomNumber,
                category = category,
                price = price,
                isOccupied = state.isOccupiedInput,
                photoUrl = primaryPhoto,
                photoUrls = state.photoUrlsList,
                facilities = state.facilitiesList
            )

            roomRepository.updateRoom(updatedRoom)
            val updatedList = _roomListState.value.rooms.map {
                if (it.id == state.roomId || it.roomNumber == state.roomNumberInput) updatedRoom else it
            }
            updateListState(updatedList)
            _editRoomState.value = EditRoomUiState(isSuccess = true)
        }
    }

    fun resetEditRoomState() {
        _editRoomState.value = EditRoomUiState()
    }

    fun deleteRoom(roomId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            when (val result = roomRepository.deleteRoom(roomId)) {
                is Resource.Success -> {
                    val updatedList = _roomListState.value.rooms.filter { it.id != roomId && it.roomNumber != roomId }
                    updateListState(updatedList)
                    onSuccess()
                }
                is Resource.Error -> {
                    val updatedList = _roomListState.value.rooms.filter { it.id != roomId && it.roomNumber != roomId }
                    updateListState(updatedList)
                    onSuccess()
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun resetAddRoomState() {
        _addRoomState.value = AddRoomUiState()
    }

    fun clearErrorMessage() {
        _addRoomState.value = _addRoomState.value.copy(errorMessage = null)
    }
}
