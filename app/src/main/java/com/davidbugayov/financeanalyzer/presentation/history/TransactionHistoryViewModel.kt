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
import com.davidbugayov.financeanalyzer.presentation.add.model.CategoryItem
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.history.event.TransactionHistoryEvent
import com.davidbugayov.financeanalyzer.presentation.history.model.GroupingType
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import com.davidbugayov.financeanalyzer.presentation.history.state.TransactionHistoryState
import com.davidbugayov.financeanalyzer.utils.Event
import com.davidbugayov.financeanalyzer.utils.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.util.Date

/**
 * ViewModel для экрана истории транзакций.
 * Следует принципам MVI и Clean Architecture.
 */
class TransactionHistoryViewModel(
    private val loadTransactionsUseCase: LoadTransactionsUseCase,
    private val filterTransactionsUseCase: FilterTransactionsUseCase,
    private val groupTransactionsUseCase: GroupTransactionsUseCase,
    private val calculateCategoryStatsUseCase: CalculateCategoryStatsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    val categoriesViewModel: CategoriesViewModel
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(TransactionHistoryState())
    val state: StateFlow<TransactionHistoryState> = _state.asStateFlow()

    // Категории
    private val _expenseCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val expenseCategories = _expenseCategories.asStateFlow()

    private val _incomeCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val incomeCategories = _incomeCategories.asStateFlow()

    // Кэш для хранения результатов вычислений
    private val filteredTransactionsCache = mutableMapOf<FilterCacheKey, List<Transaction>>()
    private val groupedTransactionsCache = mutableMapOf<GroupCacheKey, Map<String, List<Transaction>>>()
    private val categoryStatsCache =
        mutableMapOf<StatsCacheKey, Triple<com.davidbugayov.financeanalyzer.domain.model.Money, com.davidbugayov.financeanalyzer.domain.model.Money, Int?>>()

    init {
        loadTransactions()

        // Подписываемся на изменения категорий
        viewModelScope.launch {
            categoriesViewModel.expenseCategories.collect { categories ->
                _expenseCategories.value = categories
            }
        }

        viewModelScope.launch {
            categoriesViewModel.incomeCategories.collect { categories ->
                _incomeCategories.value = categories
            }
        }
    }

    /**
     * Обрабатывает события экрана истории транзакций
     */
    fun onEvent(event: TransactionHistoryEvent) {
        when (event) {
            is TransactionHistoryEvent.SetGroupingType -> {
                _state.update { it.copy(groupingType = event.type) }
                // Очищаем кэш группировки при изменении типа группировки
                clearGroupingCache()
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
                // Очищаем все кэши при перезагрузке транзакций
                clearAllCaches()
                loadTransactions()
            }
            is TransactionHistoryEvent.DeleteTransaction -> {
                deleteTransaction(event.transaction)
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
            is TransactionHistoryEvent.ShowDeleteConfirmDialog -> {
                _state.update { it.copy(transactionToDelete = event.transaction) }
            }
            is TransactionHistoryEvent.HideDeleteConfirmDialog -> {
                _state.update { it.copy(transactionToDelete = null) }
            }
            is TransactionHistoryEvent.ShowDeleteCategoryConfirmDialog -> {
                _state.update { it.copy(categoryToDelete = Pair(event.category, event.isExpense)) }
            }
            is TransactionHistoryEvent.HideDeleteCategoryConfirmDialog -> {
                _state.update { it.copy(categoryToDelete = null) }
            }
            is TransactionHistoryEvent.DeleteCategory -> {
                deleteCategory(event.category, event.isExpense)
            }
        }
    }

    /**
     * Удаляет транзакцию
     */
    private fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                when (val result = deleteTransactionUseCase(transaction)) {
                    is Result.Success -> {
                        // Отправляем событие об удалении транзакции
                        EventBus.emit(Event.TransactionDeleted)

                        // Очищаем все кэши и перезагружаем данные
                        clearAllCaches()
                        loadTransactions()

                        _state.update { it.copy(transactionToDelete = null) }
                    }
                    is Result.Error -> {
                        val exception = result.exception
                        Timber.e("Failed to delete transaction: ${exception.message}")
                        _state.update {
                            it.copy(
                                error = exception.message ?: "Failed to delete transaction",
                                isLoading = false,
                                transactionToDelete = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error deleting transaction")
                _state.update {
                    it.copy(
                        error = "Error deleting transaction: ${e.message}",
                        isLoading = false,
                        transactionToDelete = null
                    )
                }
            }
        }
    }

    /**
     * Загружает транзакции из репозитория
     */
    private fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                when (val result = loadTransactionsUseCase()) {
                    is Result.Success -> {
                        val transactions = result.data
                        _state.update { it.copy(transactions = transactions, isLoading = false) }
                        updateFilteredTransactions()
                    }
                    is Result.Error -> {
                        val exception = result.exception
                        Timber.e("Failed to load transactions: ${exception.message}")
                        _state.update { it.copy(error = exception.message ?: "Failed to load transactions", isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading transactions")
                _state.update { it.copy(error = "Error loading transactions: ${e.message}", isLoading = false) }
            }
        }
    }

    /**
     * Обновляет отфильтрованные транзакции на основе текущих фильтров
     */
    private fun updateFilteredTransactions() {
        val currentState = _state.value

        // Создаем ключ для кэша
        val cacheKey = FilterCacheKey(
            transactions = currentState.transactions,
            periodType = currentState.periodType,
            startDate = currentState.startDate,
            endDate = currentState.endDate,
            category = currentState.selectedCategory
        )

        // Проверяем, есть ли результат в кэше
        val filtered = filteredTransactionsCache.getOrPut(cacheKey) {
            filterTransactionsUseCase(
                transactions = currentState.transactions,
                periodType = currentState.periodType,
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                category = currentState.selectedCategory
            )
        }
        
        _state.update { it.copy(filteredTransactions = filtered) }

        // Очищаем кэш группировки при изменении фильтрованных транзакций
        clearGroupingCache()
        
        updateCategoryStats()
    }

    /**
     * Обновляет статистику по выбранной категории
     */
    private fun updateCategoryStats() {
        val currentState = _state.value
        val selectedCategory = currentState.selectedCategory

        if (selectedCategory != null) {
            // Создаем ключ для кэша
            val cacheKey = StatsCacheKey(
                transactions = currentState.transactions,
                category = selectedCategory,
                periodType = currentState.periodType,
                startDate = currentState.startDate,
                endDate = currentState.endDate
            )

            // Проверяем, есть ли результат в кэше
            val stats = categoryStatsCache.getOrPut(cacheKey) {
                calculateCategoryStatsUseCase(
                    transactions = currentState.transactions,
                    category = selectedCategory,
                    periodType = currentState.periodType,
                    startDate = currentState.startDate,
                    endDate = currentState.endDate
                )
            }
            
            _state.update { it.copy(categoryStats = stats) }
        } else {
            _state.update { it.copy(categoryStats = null) }
        }
    }

    /**
     * Возвращает сгруппированные транзакции на основе текущего типа группировки
     */
    fun getGroupedTransactions(): Map<String, List<Transaction>> {
        val currentState = _state.value

        // Создаем ключ для кэша
        val cacheKey = GroupCacheKey(
            transactions = currentState.filteredTransactions,
            groupingType = currentState.groupingType
        )

        // Проверяем, есть ли результат в кэше
        return groupedTransactionsCache.getOrPut(cacheKey) {
            groupTransactionsUseCase(
                transactions = currentState.filteredTransactions,
                groupingType = currentState.groupingType
            )
        }
    }

    /**
     * Очищает кэш группировки
     */
    private fun clearGroupingCache() {
        groupedTransactionsCache.clear()
    }

    /**
     * Очищает все кэши
     */
    private fun clearAllCaches() {
        filteredTransactionsCache.clear()
        groupedTransactionsCache.clear()
        categoryStatsCache.clear()
    }

    /**
     * Удаляет категорию
     */
    private fun deleteCategory(category: String, isExpense: Boolean) {
        // Не позволяем удалять категорию "Другое"
        if (category == "Другое") {
            _state.update { it.copy(categoryToDelete = null) }
            return
        }
        
        viewModelScope.launch {
            try {
                // Удаляем категорию через CategoriesViewModel
                categoriesViewModel.removeCategory(category, isExpense)

                // Если удаляемая категория была выбрана, сбрасываем фильтр
                if (_state.value.selectedCategory == category) {
                    _state.update { it.copy(selectedCategory = null) }
                    updateFilteredTransactions()
                }

                // Скрываем диалог подтверждения
                _state.update { it.copy(categoryToDelete = null) }

                // Перезагружаем транзакции, чтобы обновить список
                loadTransactions()
            } catch (e: Exception) {
                Timber.e(e, "Error deleting category")
                _state.update { it.copy(error = "Ошибка при удалении категории: ${e.message}") }
            }
        }
    }

    /**
     * Ключ для кэша фильтрованных транзакций
     */
    private data class FilterCacheKey(
        val transactions: List<Transaction>,
        val periodType: PeriodType,
        val startDate: Date,
        val endDate: Date,
        val category: String?
    )

    /**
     * Ключ для кэша сгруппированных транзакций
     */
    private data class GroupCacheKey(
        val transactions: List<Transaction>,
        val groupingType: GroupingType
    )

    /**
     * Ключ для кэша статистики по категории
     */
    private data class StatsCacheKey(
        val transactions: List<Transaction>,
        val category: String,
        val periodType: PeriodType,
        val startDate: Date,
        val endDate: Date
    )
} 