package com.davidbugayov.financeanalyzer.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class TransactionHistoryViewModel @Inject constructor(
    private val loadTransactionsUseCase: LoadTransactionsUseCase,
    private val filterTransactionsUseCase: FilterTransactionsUseCase,
    private val groupTransactionsUseCase: GroupTransactionsUseCase,
    private val calculateCategoryStatsUseCase: CalculateCategoryStatsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val repository: TransactionRepository,
    private val eventBus: EventBus,
    private val analyticsUtils: AnalyticsUtils,
    val categoriesViewModel: CategoriesViewModel
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionHistoryState())
    val state: StateFlow<TransactionHistoryState> = _state.asStateFlow()

    init {
        loadTransactionsFirstPage()
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
            is TransactionHistoryEvent.ReloadTransactions -> resetAndReloadTransactions()
            is TransactionHistoryEvent.LoadMoreTransactions -> loadMoreTransactions()
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
                    resetAndReloadTransactions()
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Failed to delete transaction")
                    _state.update { it.copy(error = result.exception.message) }
                }
            }
        }
    }

    /**
     * Сбрасывает состояние пагинации и загружает первую страницу транзакций
     */
    private fun resetAndReloadTransactions() {
        _state.update { 
            it.copy(
                currentPage = 0,
                hasMoreData = true,
                transactions = emptyList(),
                filteredTransactions = emptyList(),
                groupedTransactions = emptyMap()
            ) 
        }
        loadTransactionsFirstPage()
    }

    /**
     * Загружает первую страницу транзакций
     */
    private fun loadTransactionsFirstPage() {
        viewModelScope.launch {
            // Устанавливаем флаг загрузки
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Получаем текущие параметры
                val currentState = _state.value
                val pageSize = currentState.pageSize
                
                // Создаем вспомогательную корутину для получения общего количества транзакций
                val totalCountDeferred = viewModelScope.async(Dispatchers.IO) {
                    try {
                        if (currentState.periodType == PeriodType.CUSTOM || 
                            currentState.periodType == PeriodType.ALL) {
                            repository.getTransactionsCountByDateRange(
                                currentState.startDate,
                                currentState.endDate
                            )
                        } else {
                            repository.getTransactionsCount()
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при получении количества транзакций: ${e.message}")
                        0 // По умолчанию считаем, что транзакций нет
                    }
                }
                
                // Запускаем асинхронную загрузку первой страницы транзакций
                val transactions = withContext(Dispatchers.IO) {
                    try {
                        if (currentState.periodType == PeriodType.CUSTOM || 
                            currentState.periodType == PeriodType.ALL) {
                            repository.getTransactionsByDateRangePaginated(
                                currentState.startDate,
                                currentState.endDate,
                                pageSize,
                                0
                            )
                        } else {
                            repository.getTransactionsPaginated(pageSize, 0)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при загрузке первой страницы транзакций: ${e.message}")
                        emptyList()
                    }
                }
                
                // Получаем общее количество транзакций из отложенного вычисления
                val totalCount = totalCountDeferred.await()
                
                Timber.d("Загружено ${transactions.size} транзакций (первая страница из $totalCount всего)")
                
                // Обновляем состояние с загруженными данными
                _state.update {
                    it.copy(
                        transactions = transactions,
                        currentPage = 1,
                        hasMoreData = transactions.size < totalCount,
                        isLoading = false,
                        error = null
                    )
                }
                
                // Оптимизация: лёгкая задержка перед запуском обновления фильтрованных данных,
                // чтобы UI успел отрисоваться после обновления состояния
                delay(100)
                
                // Асинхронно обновляем отфильтрованные данные в отдельной корутине
                launch(Dispatchers.Default) {
                    updateFilteredTransactions()
                }
            } catch (e: Exception) {
                Timber.e(e, "Критическая ошибка при загрузке транзакций: ${e.message}")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Неизвестная ошибка при загрузке данных"
                    ) 
                }
            }
        }
    }

    /**
     * Загружает следующую страницу транзакций
     */
    private fun loadMoreTransactions() {
        viewModelScope.launch {
            val currentState = _state.value
            
            // Проверяем, есть ли ещё данные для загрузки
            if (!currentState.hasMoreData || currentState.isLoadingMore) {
                return@launch
            }
            
            _state.update { it.copy(isLoadingMore = true) }
            
            try {
                val pageSize = currentState.pageSize
                val currentPage = currentState.currentPage
                val offset = currentPage * pageSize
                
                // Загружаем следующую страницу в IO потоке
                val nextPageTransactions = withContext(Dispatchers.IO) {
                    try {
                        if (currentState.periodType == PeriodType.CUSTOM || 
                            currentState.periodType == PeriodType.ALL) {
                            repository.getTransactionsByDateRangePaginated(
                                currentState.startDate,
                                currentState.endDate,
                                pageSize,
                                offset
                            )
                        } else {
                            repository.getTransactionsPaginated(pageSize, offset)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при загрузке следующей страницы: ${e.message}")
                        emptyList()
                    }
                }
                
                // Если новых транзакций нет - больше нет данных
                if (nextPageTransactions.isEmpty()) {
                    _state.update { 
                        it.copy(
                            hasMoreData = false,
                            isLoadingMore = false
                        ) 
                    }
                    return@launch
                }
                
                Timber.d("Загружено ${nextPageTransactions.size} дополнительных транзакций")
                
                // Оптимизация: добавляем задержку перед обновлением UI
                // Это предотвращает заикание при прокрутке большого количества данных
                delay(100)
                
                // Используем отдельную временную переменную для нового списка транзакций
                // чтобы минимизировать время блокировки UI потока
                val updatedTransactions = currentState.transactions + nextPageTransactions
                
                // Обновляем список транзакций одним атомарным обновлением
                _state.update {
                    it.copy(
                        transactions = updatedTransactions,
                        currentPage = currentPage + 1,
                        isLoadingMore = false
                    )
                }
                
                // Оптимизация: выполняем фильтрацию в отдельном потоке с небольшой задержкой,
                // чтобы не блокировать UI при обработке больших списков
                launch(Dispatchers.Default) {
                    delay(150) // Даем время UI для отрисовки основных данных
                    updateFilteredTransactions()
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке дополнительных транзакций: ${e.message}")
                _state.update { 
                    it.copy(
                        isLoadingMore = false,
                        error = e.message ?: "Ошибка при загрузке данных"
                    ) 
                }
            }
        }
    }

    private fun loadTransactions() {
        resetAndReloadTransactions()
    }

    /**
     * Оптимизированная фильтрация транзакций с асинхронными операциями
     */
    private fun updateFilteredTransactions() {
        viewModelScope.launch {
            val currentState = _state.value
            
            if (currentState.transactions.isEmpty()) {
                _state.update {
                    it.copy(
                        filteredTransactions = emptyList(),
                        groupedTransactions = emptyMap(),
                        categoryStats = null
                    )
                }
                return@launch
            }
            
            // Выполняем операции фильтрации и группировки асинхронно в IO-контексте
            val filteredTransactions = withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    filterTransactionsUseCase(
                        transactions = currentState.transactions,
                        periodType = currentState.periodType,
                        startDate = currentState.startDate,
                        endDate = currentState.endDate,
                        categories = currentState.selectedCategories,
                        sources = currentState.selectedSources
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Ошибка при фильтрации транзакций: ${e.message}")
                    emptyList()
                }
            }
            
            // Выполняем группировку отфильтрованных транзакций
            val groupedTransactions = withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    groupTransactionsUseCase(
                        transactions = filteredTransactions,
                        groupingType = currentState.groupingType
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Ошибка при группировке транзакций: ${e.message}")
                    emptyMap()
                }
            }
            
            // Выполняем расчет статистики по категориям
            val categoryStats = if (filteredTransactions.isNotEmpty() && currentState.selectedCategories.isNotEmpty()) {
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        calculateCategoryStatsUseCase(
                            transactions = filteredTransactions,
                            categories = currentState.selectedCategories,
                            periodType = currentState.periodType,
                            startDate = currentState.startDate,
                            endDate = currentState.endDate
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при расчете статистики: ${e.message}")
                        null
                    }
                }
            } else {
                null
            }
            
            // Обновляем состояние одним вызовом для оптимизации перерисовки
            _state.update {
                it.copy(
                    filteredTransactions = filteredTransactions,
                    groupedTransactions = groupedTransactions,
                    categoryStats = categoryStats,
                    error = null
                )
            }
            
            Timber.d("Обработано ${filteredTransactions.size} транзакций, создано ${groupedTransactions.size} групп")
        }
    }

    private fun updatePeriodType(periodType: PeriodType) {
        _state.update { it.copy(periodType = periodType) }
        updateFilteredTransactions()
    }

    private fun updateGroupingType(groupingType: GroupingType) {
        _state.update { it.copy(groupingType = groupingType) }
        updateFilteredTransactions()
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