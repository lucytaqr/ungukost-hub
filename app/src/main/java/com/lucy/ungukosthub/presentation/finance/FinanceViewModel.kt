package com.lucy.ungukosthub.presentation.finance

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class FinanceSummaryUiState(
    val selectedMonth: String = "Juni 2024",
    val totalIncome: Double = 12450000.0,
    val totalExpense: Double = 2350000.0,
    val netProfit: Double = 10100000.0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FinanceViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceSummaryUiState())
    val uiState: StateFlow<FinanceSummaryUiState> = _uiState.asStateFlow()

    fun onMonthChanged(month: String) {
        _uiState.value = _uiState.value.copy(selectedMonth = month)
    }

    fun addIncome(type: String, tenant: String, amount: Double, date: String, note: String) {
        _uiState.value = _uiState.value.copy(
            totalIncome = _uiState.value.totalIncome + amount,
            netProfit = (_uiState.value.totalIncome + amount) - _uiState.value.totalExpense,
            isSuccess = true
        )
    }

    fun addExpense(category: String, amount: Double, date: String, note: String) {
        _uiState.value = _uiState.value.copy(
            totalExpense = _uiState.value.totalExpense + amount,
            netProfit = _uiState.value.totalIncome - (_uiState.value.totalExpense + amount),
            isSuccess = true
        )
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}
