package com.lucy.ungukosthub.presentation.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucy.ungukosthub.core.util.Resource
import com.lucy.ungukosthub.domain.model.Transaction
import com.lucy.ungukosthub.domain.model.TransactionType
import com.lucy.ungukosthub.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class MonthlyChartItem(
    val monthLabel: String,
    val incomeAmount: Double,
    val expenseAmount: Double
)

data class FinanceSummaryUiState(
    val selectedMonth: String = "Semua Bulan",
    val availableMonths: List<String> = listOf("Semua Bulan"),
    val startDate: Long? = null,
    val endDate: Long? = null,
    val startDateText: String = "",
    val endDateText: String = "",
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netProfit: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val chartItems: List<MonthlyChartItem> = emptyList(),
    val selectedTransactionDetail: Transaction? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceSummaryUiState())
    val uiState: StateFlow<FinanceSummaryUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    private fun parseDateToTimestamp(dateStr: String): Long {
        if (dateStr.isBlank()) return 0L
        val formats = listOf(
            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd MMM yyyy", Locale.US),
            SimpleDateFormat("dd MMMM yyyy", Locale.US)
        )
        for (fmt in formats) {
            try {
                val d = fmt.parse(dateStr)
                if (d != null) return d.time
            } catch (_: Exception) {}
        }
        return 0L
    }

    private fun Transaction.getEffectiveTimestamp(): Long {
        val parsed = parseDateToTimestamp(this.date)
        return if (parsed > 0L) parsed else this.timestamp
    }

    private fun formatMonthYear(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        return format.format(date)
    }

    private fun formatMonthShort(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM", Locale("id", "ID"))
        return format.format(date)
    }

    private fun observeTransactions() {
        transactionRepository.getTransactions().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Resource.Success -> {
                    val list = result.data ?: emptyList()
                    recalculateState(
                        allTransactions = list,
                        targetMonth = _uiState.value.selectedMonth,
                        start = _uiState.value.startDate,
                        end = _uiState.value.endDate,
                        startText = _uiState.value.startDateText,
                        endText = _uiState.value.endDateText
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun recalculateState(
        allTransactions: List<Transaction>,
        targetMonth: String,
        start: Long? = null,
        end: Long? = null,
        startText: String = "",
        endText: String = ""
    ) {
        val monthSet = allTransactions
            .map { formatMonthYear(it.getEffectiveTimestamp()) }
            .filter { it.isNotBlank() }
            .distinct()

        val available = if (monthSet.isNotEmpty()) {
            listOf("Semua Bulan") + monthSet
        } else {
            listOf("Semua Bulan")
        }

        val activeMonth = if (available.contains(targetMonth)) targetMonth else available.first()

        val filteredList = if (start != null && end != null) {
            val endDayInclusive = end + (24 * 60 * 60 * 1000 - 1)
            allTransactions.filter { it.getEffectiveTimestamp() in start..endDayInclusive }
        } else if (activeMonth == "Semua Bulan") {
            allTransactions
        } else {
            allTransactions.filter { formatMonthYear(it.getEffectiveTimestamp()) == activeMonth }
        }

        val incomeSum = filteredList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenseSum = filteredList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val net = incomeSum - expenseSum

        // Build 6-Month Chronological Ascending Chart Data based on transaction date
        val sortedAscending = allTransactions.sortedBy { it.getEffectiveTimestamp() }
        val chartData = sortedAscending
            .groupBy { formatMonthShort(it.getEffectiveTimestamp()) }
            .map { (monthLabel, items) ->
                val inc = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val exp = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                MonthlyChartItem(
                    monthLabel = monthLabel.ifBlank { "Lain" },
                    incomeAmount = inc,
                    expenseAmount = exp
                )
            }
            .takeLast(6)

        _uiState.value = _uiState.value.copy(
            selectedMonth = activeMonth,
            availableMonths = available,
            startDate = start,
            endDate = end,
            startDateText = startText,
            endDateText = endText,
            totalIncome = incomeSum,
            totalExpense = expenseSum,
            netProfit = net,
            recentTransactions = allTransactions,
            filteredTransactions = filteredList,
            chartItems = chartData,
            isLoading = false,
            errorMessage = null
        )
    }

    fun onMonthChanged(month: String) {
        recalculateState(
            allTransactions = _uiState.value.recentTransactions,
            targetMonth = month,
            start = null,
            end = null,
            startText = "",
            endText = ""
        )
    }

    fun setDateRangeFilter(start: Long, end: Long, startText: String, endText: String) {
        recalculateState(
            allTransactions = _uiState.value.recentTransactions,
            targetMonth = "Semua Bulan",
            start = start,
            end = end,
            startText = startText,
            endText = endText
        )
    }

    fun clearDateRangeFilter() {
        recalculateState(
            allTransactions = _uiState.value.recentTransactions,
            targetMonth = "Semua Bulan",
            start = null,
            end = null,
            startText = "",
            endText = ""
        )
    }

    fun selectTransactionDetail(transaction: Transaction?) {
        _uiState.value = _uiState.value.copy(selectedTransactionDetail = transaction)
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedTransactionDetail = null)
            transactionRepository.deleteTransaction(transactionId)
        }
    }

    fun addIncome(type: String, tenant: String, amount: Double, date: String, note: String, proofUrl: String = "") {
        val parsedTime = parseDateToTimestamp(date)
        val finalTimestamp = if (parsedTime > 0L) parsedTime else System.currentTimeMillis()

        val newTransaction = Transaction(
            type = TransactionType.INCOME,
            category = type.ifBlank { "Sewa Kamar" },
            tenantName = tenant,
            amount = amount,
            date = date,
            timestamp = finalTimestamp,
            note = note,
            proofUrl = proofUrl
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = transactionRepository.addTransaction(newTransaction)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    fun addExpense(category: String, amount: Double, date: String, note: String, proofUrl: String = "") {
        val parsedTime = parseDateToTimestamp(date)
        val finalTimestamp = if (parsedTime > 0L) parsedTime else System.currentTimeMillis()

        val newTransaction = Transaction(
            type = TransactionType.EXPENSE,
            category = category.ifBlank { "Lainnya" },
            amount = amount,
            date = date,
            timestamp = finalTimestamp,
            note = note,
            proofUrl = proofUrl
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = transactionRepository.addTransaction(newTransaction)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}
