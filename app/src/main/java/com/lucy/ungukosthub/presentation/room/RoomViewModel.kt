package com.lucy.ungukosthub.presentation.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.Room
import com.lucy.ungukosthub.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RoomFilterCategory {
    ALL, OCCUPIED, VACANT
}

/**
 * UI State for Room List presentation with search, filter tabs, and counts.
 */
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

/**
 * UI State for Add Room form presentation.
 */
data class AddRoomUiState(
    val roomNumberInput: String = "",
    val categoryInput: String = "Kamar",
    val priceInput: String = "",
    val facilityInput: String = "",
    val facilitiesList: List<String> = emptyList(),
    val facilitySuggestions: List<String> = emptyList(),
    val descriptionInput: String = "",
    val isOccupiedInput: Boolean = false,
    val photoUrlInput: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel managing Room List and Add Room state & business operations.
 */
@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val facilityRepository: com.lucy.ungukosthub.domain.repository.FacilityRepository
) : ViewModel() {

    private val _roomListState = MutableStateFlow(RoomListUiState())
    val roomListState: StateFlow<RoomListUiState> = _roomListState.asStateFlow()

    private val _addRoomState = MutableStateFlow(AddRoomUiState())
    val addRoomState: StateFlow<AddRoomUiState> = _addRoomState.asStateFlow()

    init {
        observeRooms()
        observeFacilities()
    }

    fun observeFacilities() {
        facilityRepository.getFacilities().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val names = result.data?.map { it.name } ?: emptyList()
                    _addRoomState.value = _addRoomState.value.copy(facilitySuggestions = names)
                }
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun observeRooms() {
        roomRepository.getRooms().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _roomListState.value = _roomListState.value.copy(isLoading = true)
                }
                is Resource.Success -> {
                    val rawList = result.data ?: emptyList()
                    updateListState(allRooms = rawList)
                }
                is Resource.Error -> {
                    updateListState(allRooms = emptyList())
                    _roomListState.value = _roomListState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _roomListState.value = _roomListState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onFilterSelected(filter: RoomFilterCategory) {
        _roomListState.value = _roomListState.value.copy(selectedFilter = filter)
        applyFilters()
    }

    private fun updateListState(allRooms: List<Room>) {
        val total = allRooms.size
        val occupied = allRooms.count { it.isOccupied }
        val vacant = allRooms.count { !it.isOccupied }

        _roomListState.value = _roomListState.value.copy(
            rooms = allRooms,
            totalCount = total,
            occupiedCount = occupied,
            vacantCount = vacant,
            isLoading = false,
            errorMessage = null
        )
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _roomListState.value
        val query = currentState.searchQuery.trim().lowercase()

        val filtered = currentState.rooms.filter { room ->
            val matchesQuery = query.isEmpty() ||
                    room.roomNumber.lowercase().contains(query) ||
                    room.category.lowercase().contains(query)

            val matchesTab = when (currentState.selectedFilter) {
                RoomFilterCategory.ALL -> true
                RoomFilterCategory.OCCUPIED -> room.isOccupied
                RoomFilterCategory.VACANT -> !room.isOccupied
            }

            matchesQuery && matchesTab
        }

        _roomListState.value = _roomListState.value.copy(filteredRooms = filtered)
    }

    fun onRoomNumberChanged(value: String) {
        _addRoomState.value = _addRoomState.value.copy(roomNumberInput = value)
    }

    fun onCategoryChanged(value: String) {
        _addRoomState.value = _addRoomState.value.copy(categoryInput = value)
    }

    fun onPriceChanged(value: String) {
        _addRoomState.value = _addRoomState.value.copy(priceInput = value)
    }

    fun onFacilityChanged(value: String) {
        _addRoomState.value = _addRoomState.value.copy(facilityInput = value)
    }

    fun onAddFacility(facility: String) {
        val trimmed = facility.trim()
        if (trimmed.isNotBlank() && !_addRoomState.value.facilitiesList.contains(trimmed)) {
            val updated = _addRoomState.value.facilitiesList + trimmed
            _addRoomState.value = _addRoomState.value.copy(
                facilitiesList = updated,
                facilityInput = ""
            )
        }
    }

    fun onRemoveFacility(facility: String) {
        val updated = _addRoomState.value.facilitiesList.filter { it != facility }
        _addRoomState.value = _addRoomState.value.copy(facilitiesList = updated)
    }

    fun onDescriptionChanged(value: String) {
        _addRoomState.value = _addRoomState.value.copy(descriptionInput = value)
    }

    fun onOccupiedStatusChanged(value: Boolean) {
        _addRoomState.value = _addRoomState.value.copy(isOccupiedInput = value)
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
            val newRoom = Room(
                roomNumber = roomNumber,
                category = category,
                price = price,
                isOccupied = state.isOccupiedInput,
                photoUrl = state.photoUrlInput
            )
            when (val result = roomRepository.addRoom(newRoom)) {
                is Resource.Success -> {
                    // Update local state as well
                    val updatedList = _roomListState.value.rooms + newRoom
                    updateListState(updatedList)
                    _addRoomState.value = AddRoomUiState(isSuccess = true)
                }
                is Resource.Error -> {
                    // Even if offline, update local list for UI responsiveness
                    val updatedList = _roomListState.value.rooms + newRoom
                    updateListState(updatedList)
                    _addRoomState.value = AddRoomUiState(isSuccess = true)
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

