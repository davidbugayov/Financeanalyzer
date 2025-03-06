package com.davidbugayov.financeanalyzer.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.usecase.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.FilterTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GroupTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.history.event.TransactionHistoryEvent
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import com.davidbugayov.financeanalyzer.presentation.history.state.TransactionHistoryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

/**
 * ViewModel для экрана истории транзакций.
 * Следует принципам MVI и Clean Architecture.
 */
class TransactionHistoryViewModel(
    private val loadTransactionsUseCase: LoadTransactionsUseCase,
    private val filterTransactionsUseCase: FilterTransactionsUseCase,
    private val groupTransactionsUseCase: GroupTransactionsUseCase,
    private val calculateCategoryStatsUseCase: CalculateCategoryStatsUseCase
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(TransactionHistoryState())
    val state: StateFlow<TransactionHistoryState> = _state.asStateFlow()

    init {
        loadTransactions()
    }

    /**
     * Обрабатывает события экрана истории транзакций
     */
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
            is TransactionHistoryEvent.ReloadTransactions -> {
                loadTransactions()
            }
            // События для управления диалогами
            is TransactionHistoryEvent.ShowPeriodDialog -> {
                _state.update { it.copy(showPeriodDialog = true) }
            }
            is TransactionHistoryEvent.HidePeriodDialog -> {
                _state.update { it.copy(showPeriodDialog = false) }
            }
            is TransactionHistoryEvent.ShowCategoryDialog -> {
                _state.update { it.copy(showCategoryDialog = true) }
            }
            is TransactionHistoryEvent.HideCategoryDialog -> {
                _state.update { it.copy(showCategoryDialog = false) }
            }
            is TransactionHistoryEvent.ShowStartDatePicker -> {
                _state.update { it.copy(showStartDatePicker = true) }
            }
            is TransactionHistoryEvent.HideStartDatePicker -> {
                _state.update { it.copy(showStartDatePicker = false) }
            }
            is TransactionHistoryEvent.ShowEndDatePicker -> {
                _state.update { it.copy(showEndDatePicker = true) }
            }
            is TransactionHistoryEvent.HideEndDatePicker -> {
                _state.update { it.copy(showEndDatePicker = false) }
            }
        }
    }

    /**
     * Загружает транзакции из репозитория
     */
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

    /**
     * Обновляет отфильтрованные транзакции на основе текущих фильтров
     */
    private fun updateFilteredTransactions() {
        val currentState = _state.value
        val filtered = filterTransactionsUseCase(
            transactions = currentState.transactions,
            periodType = currentState.periodType,
            startDate = currentState.startDate,
            endDate = currentState.endDate,
            category = currentState.selectedCategory
        )
        _state.update { it.copy(filteredTransactions = filtered) }
        updateCategoryStats()
    }

    /**
     * Обновляет статистику по выбранной категории
     */
    private fun updateCategoryStats() {
        val currentState = _state.value
        val selectedCategory = currentState.selectedCategory

        if (selectedCategory != null) {
            val stats = calculateCategoryStatsUseCase(
                transactions = currentState.transactions,
                category = selectedCategory,
                periodType = currentState.periodType,
                startDate = currentState.startDate,
                endDate = currentState.endDate
            )
            _state.update { it.copy(categoryStats = stats) }
        } else {
            _state.update { it.copy(categoryStats = null) }
        }
    }

    /**
     * Возвращает сгруппированные транзакции на основе текущего типа группировки
     */
    fun getGroupedTransactions(): Map<String, List<com.davidbugayov.financeanalyzer.domain.model.Transaction>> {
        return groupTransactionsUseCase(
            transactions = _state.value.filteredTransactions,
            groupingType = _state.value.groupingType
        )
    }
} 