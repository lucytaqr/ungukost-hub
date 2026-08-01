package com.lucy.ungukosthub.presentation.kamar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.usecase.GetDaftarKamarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * ViewModel pada pola MVVM.
 * Mengelola UI state dan berkomunikasi dengan UseCase (Domain Layer).
 */
@HiltViewModel
class KamarViewModel @Inject constructor(
    private val getDaftarKamarUseCase: GetDaftarKamarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<KamarUiState>(KamarUiState.Loading)
    val uiState: StateFlow<KamarUiState> = _uiState.asStateFlow()

    init {
        loadDaftarKamar()
    }

    fun loadDaftarKamar() {
        getDaftarKamarUseCase().onEach { result ->
            _uiState.value = when (result) {
                is Resource.Loading -> KamarUiState.Loading
                is Resource.Success -> KamarUiState.Success(result.data ?: emptyList())
                is Resource.Error -> KamarUiState.Error(result.message ?: "Terjadi kesalahan")
            }
        }.launchIn(viewModelScope)
    }
}
