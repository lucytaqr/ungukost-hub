package com.lucy.ungukosthub.presentation.login

import com.lucy.ungukosthub.domain.model.User

/**
 * UI State untuk screen Login pada pola MVVM.
 */
data class LoginUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)
