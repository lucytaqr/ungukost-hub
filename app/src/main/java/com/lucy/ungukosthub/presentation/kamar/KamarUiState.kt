package com.lucy.ungukosthub.presentation.kamar

import com.lucy.ungukosthub.domain.model.Kamar

/**
 * State UI untuk halaman Kamar pada pola MVVM.
 */
sealed interface KamarUiState {
    object Loading : KamarUiState
    data class Success(val kamarList: List<Kamar>) : KamarUiState
    data class Error(val message: String) : KamarUiState
}
