package com.davidbugayov.financeanalyzer.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.FilterTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GroupTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.history.event.TransactionHistoryEvent
import com.davidbugayov.financeanalyzer.presentation.history.model.GroupingType
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import com.davidbugayov.financeanalyzer.presentation.history.state.TransactionHistoryState
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.Event
import com.davidbugayov.financeanalyzer.utils.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class TransactionHistoryViewModel @Inject constructor(
    private val loadTransactionsUseCase: LoadTransactionsUseCase,
    private val filterTransactionsUseCase: FilterTransactionsUseCase,
    private val groupTransactionsUseCase: GroupTransactionsUseCase,
    private val calculateCategoryStatsUseCase: CalculateCategoryStatsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val eventBus: EventBus,
    private val analyticsUtils: AnalyticsUtils,
    val categoriesViewModel: CategoriesViewModel
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionHistoryState())
    val state: StateFlow<TransactionHistoryState> = _state.asStateFlow()

    init {
        loadTransactions()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoriesViewModel.expenseCategories.collect { expenseCategories ->
                _state.update { it.copy(expenseCategories = expenseCategories) }
            }
        }

        viewModelScope.launch {
            categoriesViewModel.incomeCategories.collect { incomeCategories ->
                _state.update { it.copy(incomeCategories = incomeCategories) }
            }
        }
    }

    fun onEvent(event: TransactionHistoryEvent) {
        when (event) {
            is TransactionHistoryEvent.DeleteTransaction -> deleteTransaction(event.transaction)
            is TransactionHistoryEvent.SetGroupingType -> updateGroupingType(event.type)
            is TransactionHistoryEvent.SetPeriodType -> updatePeriodType(event.type)
            is TransactionHistoryEvent.SetCategories -> updateCategories(event.categories)
            is TransactionHistoryEvent.SetSources -> updateSources(event.sources)
            is TransactionHistoryEvent.SetDateRange -> updateDateRange(event.startDate, event.endDate)
            is TransactionHistoryEvent.SetStartDate -> updateStartDate(event.date)
            is TransactionHistoryEvent.SetEndDate -> updateEndDate(event.date)
            is TransactionHistoryEvent.ReloadTransactions -> loadTransactions()
            is TransactionHistoryEvent.ShowDeleteConfirmDialog -> showDeleteConfirmDialog(event.transaction)
            is TransactionHistoryEvent.HideDeleteConfirmDialog -> hideDeleteConfirmDialog()
            is TransactionHistoryEvent.DeleteCategory -> deleteCategory(event.category, event.isExpense)
            is TransactionHistoryEvent.ShowDeleteCategoryConfirmDialog -> showDeleteCategoryConfirmDialog(event.category, event.isExpense)
            is TransactionHistoryEvent.HideDeleteCategoryConfirmDialog -> hideDeleteCategoryConfirmDialog()
            is TransactionHistoryEvent.DeleteSource -> deleteSource(event.source)
            is TransactionHistoryEvent.ShowDeleteSourceConfirmDialog -> showDeleteSourceConfirmDialog(
                event.source
            )

            is TransactionHistoryEvent.HideDeleteSourceConfirmDialog -> hideDeleteSourceConfirmDialog()
            is TransactionHistoryEvent.ShowPeriodDialog -> showPeriodDialog()
            is TransactionHistoryEvent.HidePeriodDialog -> hidePeriodDialog()
            is TransactionHistoryEvent.ShowCategoryDialog -> showCategoryDialog()
            is TransactionHistoryEvent.HideCategoryDialog -> hideCategoryDialog()
            is TransactionHistoryEvent.ShowSourceDialog -> showSourceDialog()
            is TransactionHistoryEvent.HideSourceDialog -> hideSourceDialog()
            is TransactionHistoryEvent.ShowStartDatePicker -> showStartDatePicker()
            is TransactionHistoryEvent.HideStartDatePicker -> hideStartDatePicker()
            is TransactionHistoryEvent.ShowEndDatePicker -> showEndDatePicker()
            is TransactionHistoryEvent.HideEndDatePicker -> hideEndDatePicker()
        }
    }

    private fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            when (val result = deleteTransactionUseCase(transaction)) {
                is Result.Success -> {
                    eventBus.emit(Event.TransactionDeleted)
                    loadTransactions()
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Failed to delete transaction")
                    _state.update { it.copy(error = result.exception.message) }
                }
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            when (val result = loadTransactionsUseCase()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            transactions = result.data,
                            error = null
                        )
                    }
                    updateFilteredTransactions()
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Failed to load transactions")
                    _state.update { it.copy(error = result.exception.message) }
                }
            }
        }
    }

    private fun updateFilteredTransactions() {
        viewModelScope.launch {
            val currentState = _state.value
            val filteredTransactions = filterTransactionsUseCase(
                transactions = currentState.transactions,
                periodType = currentState.periodType,
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                categories = currentState.selectedCategories,
                sources = currentState.selectedSources
            )
            _state.update {
                it.copy(
                    filteredTransactions = filteredTransactions,
                    error = null
                )
            }
            updateGroupedTransactions()
            updateCategoryStats()
        }
    }

    private fun updateGroupedTransactions() {
        viewModelScope.launch {
            val currentState = _state.value
            val groupedTransactions = groupTransactionsUseCase(
                transactions = currentState.filteredTransactions,
                groupingType = currentState.groupingType
            )
            _state.update {
                it.copy(
                    groupedTransactions = groupedTransactions,
                    error = null
                )
            }
        }
    }

    private fun updateCategoryStats() {
        viewModelScope.launch {
            val currentState = _state.value

            val categoryStats = calculateCategoryStatsUseCase(
                transactions = currentState.filteredTransactions,
                categories = currentState.selectedCategories,
                periodType = currentState.periodType,
                startDate = currentState.startDate,
                endDate = currentState.endDate
            )
            _state.update {
                it.copy(
                    categoryStats = categoryStats,
                    error = null
                )
            }
        }
    }

    private fun updatePeriodType(periodType: PeriodType) {
        _state.update { it.copy(periodType = periodType) }
        updateFilteredTransactions()
    }

    private fun updateGroupingType(groupingType: GroupingType) {
        _state.update { it.copy(groupingType = groupingType) }
        updateGroupedTransactions()
    }

    private fun updateCategories(categories: List<String>) {
        _state.update { it.copy(selectedCategories = categories) }
        updateFilteredTransactions()
    }

    private fun updateSources(sources: List<String>) {
        _state.update { it.copy(selectedSources = sources) }
        updateFilteredTransactions()
    }

    private fun updateDateRange(startDate: Date, endDate: Date) {
        _state.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                periodType = PeriodType.CUSTOM
            )
        }
        updateFilteredTransactions()
    }

    private fun updateStartDate(date: Date) {
        _state.update { it.copy(startDate = date) }
        updateFilteredTransactions()
    }

    private fun updateEndDate(date: Date) {
        _state.update { it.copy(endDate = date) }
        updateFilteredTransactions()
    }

    private fun showDeleteConfirmDialog(transaction: Transaction) {
        _state.update { it.copy(transactionToDelete = transaction) }
    }

    private fun hideDeleteConfirmDialog() {
        _state.update { it.copy(transactionToDelete = null) }
    }

    private fun deleteCategory(category: String, isExpense: Boolean) {
        viewModelScope.launch {
            // Логируем удаление категории
            analyticsUtils.logCategoryDeleted(category, isExpense)

            // Обновляем состояние
            _state.update { it.copy(categoryToDelete = null) }

            // Удаляем категорию из списка через CategoriesViewModel
            if (isExpense) {
                categoriesViewModel.deleteExpenseCategory(category)
            } else {
                categoriesViewModel.deleteIncomeCategory(category)
            }

            // Обновляем отфильтрованные транзакции
            updateFilteredTransactions()
        }
    }

    private fun showDeleteCategoryConfirmDialog(category: String, isExpense: Boolean) {
        _state.update { it.copy(categoryToDelete = Pair(category, isExpense)) }
    }

    private fun hideDeleteCategoryConfirmDialog() {
        _state.update { it.copy(categoryToDelete = null) }
    }

    private fun deleteSource(source: String) {
        // Реализовать удаление источника
        viewModelScope.launch {
            // Логируем удаление источника
            analyticsUtils.logCategoryDeleted(source, false)

            // Обновляем состояние
            _state.update { it.copy(sourceToDelete = null) }

            // В данном приложении нет метода для удаления источников,
            // но мы можем добавить его позже при необходимости.
            // На данный момент просто обновляем список выбранных источников
            _state.update { currentState ->
                val updatedSources = currentState.selectedSources.filter { it != source }
                currentState.copy(selectedSources = updatedSources)
            }

            // Обновляем отфильтрованные транзакции
            updateFilteredTransactions()
        }
    }

    private fun showDeleteSourceConfirmDialog(source: String) {
        _state.update { it.copy(sourceToDelete = source) }
    }

    private fun hideDeleteSourceConfirmDialog() {
        _state.update { it.copy(sourceToDelete = null) }
    }

    private fun showPeriodDialog() {
        _state.update { it.copy(showPeriodDialog = true) }
    }

    private fun hidePeriodDialog() {
        _state.update { it.copy(showPeriodDialog = false) }
    }

    private fun showCategoryDialog() {
        _state.update { it.copy(showCategoryDialog = true) }
    }

    private fun hideCategoryDialog() {
        _state.update { it.copy(showCategoryDialog = false) }
    }

    private fun showSourceDialog() {
        _state.update { it.copy(showSourceDialog = true) }
    }

    private fun hideSourceDialog() {
        _state.update { it.copy(showSourceDialog = false) }
    }

    private fun showStartDatePicker() {
        _state.update { it.copy(showStartDatePicker = true) }
    }

    private fun hideStartDatePicker() {
        _state.update { it.copy(showStartDatePicker = false) }
    }

    private fun showEndDatePicker() {
        _state.update { it.copy(showEndDatePicker = true) }
    }

    private fun hideEndDatePicker() {
        _state.update { it.copy(showEndDatePicker = false) }
    }

    /**
     * Возвращает сгруппированные транзакции для отображения в UI
     */
    fun getGroupedTransactions(): Map<String, List<Transaction>> {
        return state.value.groupedTransactions
    }
}