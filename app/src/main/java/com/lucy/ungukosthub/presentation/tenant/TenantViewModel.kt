package com.lucy.ungukosthub.presentation.tenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.Tenant
import com.lucy.ungukosthub.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class TenantListUiState(
    val tenants: List<Tenant> = emptyList(),
    val filteredTenants: List<Tenant> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TenantViewModel @Inject constructor(
    private val tenantRepository: TenantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantListUiState())
    val uiState: StateFlow<TenantListUiState> = _uiState.asStateFlow()

    // Sample fallback tenant dataset matching 05_tenants_list.png
    private val sampleTenants = listOf(
        Tenant(id = "1", name = "Dinda Aulia", emergencyContact = "0812-3456-7890", roomId = "101"),
        Tenant(id = "2", name = "Riky Ramadhan", emergencyContact = "0821-1234-5678", roomId = "102"),
        Tenant(id = "3", name = "Siti Nurhaliza", emergencyContact = "0815-5656-7768", roomId = "103"),
        Tenant(id = "4", name = "Andi Setiawan", emergencyContact = "0857-8899-0011", roomId = "104"),
        Tenant(id = "5", name = "Budi Santoso", emergencyContact = "0812-9988-7766", roomId = "105")
    )

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
                    val resultList = if (list.isEmpty()) sampleTenants else list
                    _uiState.value = _uiState.value.copy(
                        tenants = resultList,
                        filteredTenants = resultList,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        tenants = sampleTenants,
                        filteredTenants = sampleTenants,
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
                    it.emergencyContact.contains(query, ignoreCase = true)
        }
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredTenants = filtered
        )
    }
}
