package com.davidbugayov.financeanalyzer.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TransactionHistoryState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val groupingType: GroupingType = GroupingType.MONTH,
    val periodType: PeriodType = PeriodType.MONTH,
    val startDate: Date = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time,
    val endDate: Date = Date(),
    val categoryStats: Triple<Double, Double, Int?>? = null
)

sealed class TransactionHistoryEvent {
    data class SetGroupingType(val type: GroupingType) : TransactionHistoryEvent()
    data class SetPeriodType(val type: PeriodType) : TransactionHistoryEvent()
    data class SetCategory(val category: String?) : TransactionHistoryEvent()
    data class SetDateRange(val startDate: Date, val endDate: Date) : TransactionHistoryEvent()
    data class SetStartDate(val date: Date) : TransactionHistoryEvent()
    data class SetEndDate(val date: Date) : TransactionHistoryEvent()
    object ReloadTransactions : TransactionHistoryEvent()
}

class TransactionHistoryViewModel(
    private val loadTransactionsUseCase: LoadTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionHistoryState())
    val state: StateFlow<TransactionHistoryState> = _state.asStateFlow()

    init {
        loadTransactions()
    }

    fun onEvent(event: TransactionHistoryEvent) {
        when (event) {
            is TransactionHistoryEvent.SetGroupingType -> {
                _state.update { it.copy(groupingType = event.type) }
            }
            is TransactionHistoryEvent.SetPeriodType -> {
                _state.update { it.copy(periodType = event.type) }
                updateFilteredTransactions()
            }
            is TransactionHistoryEvent.SetCategory -> {
                _state.update { it.copy(selectedCategory = event.category) }
                updateFilteredTransactions()
                updateCategoryStats()
            }
            is TransactionHistoryEvent.SetDateRange -> {
                _state.update {
                    it.copy(
                        startDate = event.startDate,
                        endDate = event.endDate,
                        periodType = PeriodType.CUSTOM
                    )
                }
                updateFilteredTransactions()
            }
            is TransactionHistoryEvent.SetStartDate -> {
                _state.update { it.copy(startDate = event.date) }
                updateFilteredTransactions()
            }
            is TransactionHistoryEvent.SetEndDate -> {
                _state.update { it.copy(endDate = event.date) }
                updateFilteredTransactions()
            }
            TransactionHistoryEvent.ReloadTransactions -> {
                loadTransactions()
            }
        }
    }

    private fun loadTransactions() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val transactions = loadTransactionsUseCase()
                _state.update {
                    it.copy(
                        transactions = transactions,
                        isLoading = false,
                        error = null
                    )
                }
                updateFilteredTransactions()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }

    private fun updateFilteredTransactions() {
        val currentState = _state.value
        val filtered = filterTransactionsByPeriod(
            currentState.transactions,
            currentState.periodType,
            currentState.startDate,
            currentState.endDate
        ).filter { transaction ->
            currentState.selectedCategory == null || transaction.category == currentState.selectedCategory
        }
        _state.update { it.copy(filteredTransactions = filtered) }
        updateCategoryStats()
    }

    private fun updateCategoryStats() {
        val currentState = _state.value
        val selectedCategory = currentState.selectedCategory

        if (selectedCategory != null) {
            val currentPeriodTransactions = currentState.filteredTransactions
                .filter { it.category == selectedCategory }
            val currentPeriodTotal = currentPeriodTransactions.sumOf { it.amount }

            val periodDuration = currentState.endDate.time - currentState.startDate.time
            val previousStartDate = Date(currentState.startDate.time - periodDuration)
            val previousEndDate = Date(currentState.endDate.time - periodDuration)

            val previousPeriodTransactions = filterTransactionsByPeriod(
                currentState.transactions.filter { it.category == selectedCategory },
                PeriodType.CUSTOM,
                previousStartDate,
                previousEndDate
            )
            val previousPeriodTotal = previousPeriodTransactions.sumOf { it.amount }

            val percentChange = if (previousPeriodTotal != 0.0) {
                ((currentPeriodTotal - previousPeriodTotal) / kotlin.math.abs(previousPeriodTotal) * 100).toInt()
            } else null

            _state.update {
                it.copy(categoryStats = Triple(currentPeriodTotal, previousPeriodTotal, percentChange))
            }
        } else {
            _state.update { it.copy(categoryStats = null) }
        }
    }

    fun getGroupedTransactions(): Map<String, List<Transaction>> {
        return when (_state.value.groupingType) {
            GroupingType.DAY -> groupTransactionsByDay(_state.value.filteredTransactions)
            GroupingType.WEEK -> groupTransactionsByWeek(_state.value.filteredTransactions)
            GroupingType.MONTH -> groupTransactionsByMonth(_state.value.filteredTransactions)
        }
    }

    private fun filterTransactionsByPeriod(
        transactions: List<Transaction>,
        periodType: PeriodType,
        startDate: Date? = null,
        endDate: Date? = null
    ): List<Transaction> {
        val calendar = Calendar.getInstance()

        return when (periodType) {
            PeriodType.ALL -> transactions
            PeriodType.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                val monthAgo = calendar.time
                transactions.filter { it.date.after(monthAgo) || it.date == monthAgo }
            }
            PeriodType.QUARTER -> {
                calendar.add(Calendar.MONTH, -3)
                val quarterAgo = calendar.time
                transactions.filter { it.date.after(quarterAgo) || it.date == quarterAgo }
            }
            PeriodType.HALF_YEAR -> {
                calendar.add(Calendar.MONTH, -6)
                val halfYearAgo = calendar.time
                transactions.filter { it.date.after(halfYearAgo) || it.date == halfYearAgo }
            }
            PeriodType.YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                val yearAgo = calendar.time
                transactions.filter { it.date.after(yearAgo) || it.date == yearAgo }
            }
            PeriodType.CUSTOM -> {
                if (startDate != null && endDate != null) {
                    val endCalendar = Calendar.getInstance()
                    endCalendar.time = endDate
                    endCalendar.set(Calendar.HOUR_OF_DAY, 23)
                    endCalendar.set(Calendar.MINUTE, 59)
                    endCalendar.set(Calendar.SECOND, 59)

                    transactions.filter {
                        (it.date.after(startDate) || it.date == startDate) &&
                                (it.date.before(endCalendar.time) || it.date == endCalendar.time)
                    }
                } else {
                    transactions
                }
            }
        }
    }

    private fun groupTransactionsByDay(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
        return transactions
            .sortedByDescending { it.date }
            .groupBy { dateFormat.format(it.date).replaceFirstChar { it.uppercase() } }
    }

    private fun groupTransactionsByWeek(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val calendar = Calendar.getInstance()
        val result = mutableMapOf<String, MutableList<Transaction>>()
        val sortedTransactions = transactions.sortedByDescending { it.date }

        for (transaction in sortedTransactions) {
            calendar.time = transaction.date
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            val firstDay = SimpleDateFormat("dd.MM", Locale("ru")).format(calendar.time)
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val lastDay = SimpleDateFormat("dd.MM", Locale("ru")).format(calendar.time)
            val year = calendar.get(Calendar.YEAR)
            val weekKey = "$firstDay - $lastDay $year"

            if (!result.containsKey(weekKey)) {
                result[weekKey] = mutableListOf()
            }
            result[weekKey]?.add(transaction)
        }

        return result
    }

    private fun groupTransactionsByMonth(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val format = SimpleDateFormat("MMMM yyyy", Locale("ru"))
        return transactions
            .sortedByDescending { it.date }
            .groupBy { format.format(it.date).replaceFirstChar { it.uppercase() } }
    }
} 