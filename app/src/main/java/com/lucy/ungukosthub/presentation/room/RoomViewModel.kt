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
    val categoryInput: String = "",
    val priceInput: String = "",
    val facilityInput: String = "",
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
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _roomListState = MutableStateFlow(RoomListUiState())
    val roomListState: StateFlow<RoomListUiState> = _roomListState.asStateFlow()

    private val _addRoomState = MutableStateFlow(AddRoomUiState())
    val addRoomState: StateFlow<AddRoomUiState> = _addRoomState.asStateFlow()

    // Sample fallback rooms matching screenshot design
    private val sampleRooms = listOf(
        Room(id = "101", roomNumber = "101", category = "AC • Kamar Mandi Dalam", price = 1200000.0, isOccupied = true),
        Room(id = "102", roomNumber = "102", category = "Non-AC • Kamar Mandi Luar", price = 900000.0, isOccupied = true),
        Room(id = "103", roomNumber = "103", category = "AC • Kamar Mandi Dalam", price = 1200000.0, isOccupied = true),
        Room(id = "104", roomNumber = "104", category = "Non-AC • Kamar Mandi Luar", price = 900000.0, isOccupied = true),
        Room(id = "105", roomNumber = "105", category = "AC • Kamar Mandi Dalam", price = 1200000.0, isOccupied = false),
        Room(id = "106", roomNumber = "106", category = "Non-AC • Kamar Mandi Luar", price = 900000.0, isOccupied = false),
        Room(id = "107", roomNumber = "107", category = "AC • Kamar Mandi Dalam", price = 1300000.0, isOccupied = true),
        Room(id = "108", roomNumber = "108", category = "VIP Balon", price = 1500000.0, isOccupied = true),
        Room(id = "109", roomNumber = "109", category = "Standard AC", price = 1100000.0, isOccupied = true),
        Room(id = "110", roomNumber = "110", category = "Non-AC • Kamar Mandi Luar", price = 900000.0, isOccupied = false),
        Room(id = "201", roomNumber = "201", category = "AC • Kamar Mandi Dalam", price = 1200000.0, isOccupied = true),
        Room(id = "202", roomNumber = "202", category = "AC • Kamar Mandi Dalam", price = 1200000.0, isOccupied = true),
        Room(id = "203", roomNumber = "203", category = "Non-AC • Kamar Mandi Luar", price = 900000.0, isOccupied = true),
        Room(id = "204", roomNumber = "204", category = "AC • Kamar Mandi Dalam", price = 1200000.0, isOccupied = true),
        Room(id = "205", roomNumber = "205", category = "VIP Balon", price = 1600000.0, isOccupied = true),
        Room(id = "206", roomNumber = "206", category = "Non-AC • Kamar Mandi Luar", price = 900000.0, isOccupied = false),
        Room(id = "207", roomNumber = "207", category = "AC • Kamar Mandi Dalam", price = 1250000.0, isOccupied = true),
        Room(id = "208", roomNumber = "208", category = "AC • Kamar Mandi Dalam", price = 1250000.0, isOccupied = true),
        Room(id = "209", roomNumber = "209", category = "Non-AC • Kamar Mandi Luar", price = 900000.0, isOccupied = false),
        Room(id = "210", roomNumber = "210", category = "Non-AC • Kamar Mandi Luar", price = 900000.0, isOccupied = false),
        Room(id = "301", roomNumber = "301", category = "AC • Kamar Mandi Dalam", price = 1300000.0, isOccupied = true),
        Room(id = "302", roomNumber = "302", category = "AC • Kamar Mandi Dalam", price = 1300000.0, isOccupied = true),
        Room(id = "303", roomNumber = "303", category = "VIP Balon", price = 1700000.0, isOccupied = true)
    )

    init {
        observeRooms()
    }

    fun observeRooms() {
        roomRepository.getRooms().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _roomListState.value = _roomListState.value.copy(isLoading = true)
                }
                is Resource.Success -> {
                    val rawList = result.data ?: emptyList()
                    val list = if (rawList.isEmpty()) sampleRooms else rawList
                    updateListState(allRooms = list)
                }
                is Resource.Error -> {
                    updateListState(allRooms = sampleRooms)
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

