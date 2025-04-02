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
import java.util.Calendar

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
        Timber.d("Инициализация TransactionHistoryViewModel с начальным периодом ALL")
        // Принудительно устанавливаем период на ALL
        val initialPeriod = PeriodType.ALL
        val startDate = Calendar.getInstance().apply { add(Calendar.YEAR, -5) }.time
        val endDate = Calendar.getInstance().time
        
        // Сразу установим правильное начальное состояние
        _state.update { 
            it.copy(
                periodType = initialPeriod,
                startDate = startDate,
                endDate = endDate
            ) 
        }
        
        // Загружаем транзакции и категории
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
                
                Timber.d("Начинаем загрузку транзакций с периодом: ${currentState.periodType}")
                
                // Создаем вспомогательную корутину для получения общего количества транзакций
                val totalCountDeferred = viewModelScope.async(Dispatchers.IO) {
                    try {
                        val count = repository.getTransactionsCount()
                        Timber.d("Общее количество транзакций: $count")
                        count
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при получении количества транзакций: ${e.message}")
                        0 // По умолчанию считаем, что транзакций нет
                    }
                }
                
                // Запускаем асинхронную загрузку транзакций
                var transactions = withContext(Dispatchers.IO) {
                    try {
                        Timber.d("Загрузка транзакций с периодом: ${currentState.periodType}")
                        when (currentState.periodType) {
                            PeriodType.ALL -> {
                                // Для периода ALL всегда загружаем все транзакции
                                Timber.d("Загружаем ВСЕ транзакции напрямую из репозитория")
                                repository.getAllTransactions()
                            }
                            PeriodType.CUSTOM -> {
                                // Для пользовательского периода используем фильтрацию по диапазону дат
                                Timber.d("Загружаем транзакции по ПОЛЬЗОВАТЕЛЬСКОМУ периоду: ${currentState.startDate} - ${currentState.endDate}")
                                repository.getTransactionsByDateRangePaginated(
                                    currentState.startDate,
                                    currentState.endDate,
                                    pageSize,
                                    0
                                )
                            }
                            PeriodType.MONTH -> {
                                // Используем оптимизированный метод для загрузки по месяцам
                                val calendar = Calendar.getInstance()
                                calendar.time = currentState.endDate
                                val year = calendar.get(Calendar.YEAR)
                                val month = calendar.get(Calendar.MONTH) + 1 // +1 т.к. Calendar.MONTH начинается с 0
                                Timber.d("Загружаем транзакции за МЕСЯЦ: $year-$month")
                                repository.getTransactionsByMonth(year, month)
                            }
                            PeriodType.WEEK -> {
                                // Используем оптимизированный метод для загрузки по неделям
                                val calendar = Calendar.getInstance()
                                calendar.time = currentState.endDate
                                val year = calendar.get(Calendar.YEAR)
                                val week = calendar.get(Calendar.WEEK_OF_YEAR)
                                Timber.d("Загружаем транзакции за НЕДЕЛЮ: $year-$week")
                                repository.getTransactionsByWeek(year, week)
                            }
                            else -> {
                                // Для остальных периодов (DAY, QUARTER, YEAR) используем стандартный метод
                                Timber.d("Загружаем транзакции за период ${currentState.periodType}: ${currentState.startDate} - ${currentState.endDate}")
                                repository.getTransactionsByDateRangePaginated(
                                    currentState.startDate,
                                    currentState.endDate,
                                    pageSize,
                                    0
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при загрузке первой страницы транзакций: ${e.message}")
                        emptyList()
                    }
                }
                
                // Получаем общее количество транзакций из отложенного вычисления
                val totalCount = totalCountDeferred.await()
                
                Timber.d("Загружено ${transactions.size} транзакций из $totalCount всего")
                
                // Применяем фильтры по категориям и источникам, если они выбраны
                if (currentState.selectedCategories.isNotEmpty() || currentState.selectedSources.isNotEmpty()) {
                    val before = transactions.size
                    transactions = filterTransactions(transactions, currentState)
                    Timber.d("Применены фильтры: ${before} -> ${transactions.size} транзакций")
                }
                
                // Обновляем состояние с загруженными данными
                _state.update {
                    it.copy(
                        transactions = transactions,
                        currentPage = 1,
                        hasMoreData = transactions.size < totalCount && currentState.periodType != PeriodType.ALL,
                        isLoading = false,
                        error = null
                    )
                }
                
                // Оптимизация: лёгкая задержка перед запуском обновления фильтрованных данных,
                // чтобы UI успел отрисоваться после обновления состояния
                delay(100)
                
                // Асинхронно обновляем отфильтрованные данные в отдельной корутине
                updateFilteredAndGroupedTransactions()
                
                // Если у нас выбрана одна категория, загружаем статистику по ней
                if (currentState.selectedCategories.size == 1) {
                    calculateCategoryStats(currentState.selectedCategories.first())
                } else {
                    _state.update { it.copy(categoryStats = null) }
                }
            } catch (e: Exception) {
                // Логируем и устанавливаем ошибку в состояние
                Timber.e(e, "Ошибка при загрузке транзакций: ${e.message}")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Ошибка при загрузке транзакций"
                    ) 
                }
            }
        }
    }

    /**
     * Загружает следующую страницу транзакций
     */
    private fun loadMoreTransactions() {
        val currentState = _state.value
        
        // Проверяем, можно ли загрузить больше данных
        if (!currentState.hasMoreData || currentState.isLoadingMore) {
            return
        }
        
        // Устанавливаем флаг загрузки
        _state.update { it.copy(isLoadingMore = true) }
        
        viewModelScope.launch {
            try {
                val pageSize = currentState.pageSize
                val currentPage = currentState.currentPage
                val offset = currentPage * pageSize
                
                // Загружаем следующую страницу в IO потоке
                var nextPageTransactions = withContext(Dispatchers.IO) {
                    try {
                        when (currentState.periodType) {
                            PeriodType.ALL -> {
                                // Загружаем все транзакции с пагинацией
                                repository.getTransactionsPaginated(pageSize, offset)
                            }
                            PeriodType.CUSTOM -> {
                                // Для пользовательского периода используем фильтрацию по диапазону дат
                                repository.getTransactionsByDateRangePaginated(
                                    currentState.startDate,
                                    currentState.endDate,
                                    pageSize,
                                    offset
                                )
                            }
                            else -> {
                                // Для остальных периодов используем стандартный метод с диапазоном дат
                                repository.getTransactionsByDateRangePaginated(
                                    currentState.startDate,
                                    currentState.endDate,
                                    pageSize,
                                    offset
                                )
                            }
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
                
                // Применяем фильтры по категориям и источникам, если они выбраны
                if (currentState.selectedCategories.isNotEmpty() || currentState.selectedSources.isNotEmpty()) {
                    nextPageTransactions = filterTransactions(nextPageTransactions, currentState)
                }
                
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
                
                // Обновляем отфильтрованные и сгруппированные транзакции
                updateFilteredAndGroupedTransactions()
                
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке дополнительных транзакций: ${e.message}")
                _state.update { 
                    it.copy(
                        isLoadingMore = false,
                        error = e.message
                    ) 
                }
            }
        }
    }

    /**
     * Рассчитывает статистику для выбранной категории
     */
    private fun calculateCategoryStats(category: String) {
        viewModelScope.launch {
            try {
                val currentState = _state.value
                val periodType = currentState.periodType
                val startDate = currentState.startDate
                val endDate = currentState.endDate
                val transactions = currentState.transactions
                
                val result = calculateCategoryStatsUseCase(
                    transactions = transactions,
                    categories = listOf(category),
                    periodType = periodType,
                    startDate = startDate,
                    endDate = endDate
                )
                
                _state.update { it.copy(categoryStats = result) }
                
                Timber.d("Статистика по категории $category рассчитана: $result")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при расчете статистики по категории: ${e.message}")
                _state.update { it.copy(categoryStats = null) }
            }
        }
    }

    private fun loadTransactions() {
        resetAndReloadTransactions()
    }

    /**
     * Применяет фильтры по категориям и источникам к списку транзакций
     */
    private fun filterTransactions(transactions: List<Transaction>, state: TransactionHistoryState): List<Transaction> {
        return filterTransactionsUseCase(
            transactions = transactions,
            periodType = state.periodType,
            startDate = state.startDate,
            endDate = state.endDate,
            categories = state.selectedCategories,
            sources = state.selectedSources
        )
    }

    /**
     * Обновляет отфильтрованные и сгруппированные транзакции
     * Оптимизирован для более эффективной работы с большими списками
     */
    private fun updateFilteredAndGroupedTransactions() {
        viewModelScope.launch(Dispatchers.Default) {
            val currentState = _state.value
            val transactions = currentState.transactions
            
            // Проверяем, нужно ли выполнять фильтрацию
            if (transactions.isEmpty()) {
                withContext(Dispatchers.Main) {
                    _state.update { 
                        it.copy(
                            filteredTransactions = emptyList(),
                            groupedTransactions = emptyMap()
                        ) 
                    }
                }
                return@launch
            }
            
            Timber.d("Начало обработки ${transactions.size} транзакций")
            
            // Фильтруем транзакции только если есть фильтры
            val hasFilters = currentState.selectedCategories.isNotEmpty() || 
                             currentState.selectedSources.isNotEmpty()
            
            val filteredTransactions = if (hasFilters) {
                // Применяем фильтрацию только если заданы фильтры
                val before = transactions.size
                val filtered = filterTransactions(transactions, currentState)
                Timber.d("Фильтрация: $before -> ${filtered.size} транзакций")
                filtered
            } else {
                // Если нет фильтров, используем исходный список
                transactions
            }
            
            // Вычисляем группы транзакций, если нужно
            val groupedTransactions = if (filteredTransactions.isNotEmpty()) {
                val startTime = System.currentTimeMillis()
                
                // Используем более эффективный алгоритм группировки
                val groups = groupTransactionsUseCase(
                    transactions = filteredTransactions,
                    groupingType = currentState.groupingType
                )
                
                val endTime = System.currentTimeMillis()
                Timber.d("Группировка ${filteredTransactions.size} транзакций заняла ${endTime - startTime} мс")
                
                groups
            } else {
                emptyMap()
            }
            
            // Обновляем состояние в основном потоке
            withContext(Dispatchers.Main) {
                _state.update { it.copy(
                    filteredTransactions = filteredTransactions,
                    groupedTransactions = groupedTransactions
                )}
            }
        }
    }

    /**
     * Обновляет тип периода
     */
    private fun updatePeriodType(periodType: PeriodType) {
        Timber.d("Обновляем тип периода на: $periodType")
        
        // Получаем текущее состояние
        val currentState = _state.value
        
        // Вычисляем новые даты начала и конца периода
        val (startDate, endDate) = when (periodType) {
            PeriodType.ALL -> {
                val end = Calendar.getInstance().time
                val start = Calendar.getInstance().apply { add(Calendar.YEAR, -5) }.time
                Timber.d("Период ALL: $start - $end")
                Pair(start, end)
            }
            PeriodType.DAY -> {
                val start = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val end = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                Timber.d("Период DAY: $start - $end")
                Pair(start, end)
            }
            PeriodType.WEEK -> {
                val start = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -7)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val end = Calendar.getInstance().time
                Timber.d("Период WEEK: $start - $end")
                Pair(start, end)
            }
            PeriodType.MONTH -> {
                val start = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val end = Calendar.getInstance().time
                Timber.d("Период MONTH: $start - $end")
                Pair(start, end)
            }
            PeriodType.QUARTER -> {
                val start = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -3)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val end = Calendar.getInstance().time
                Timber.d("Период QUARTER: $start - $end")
                Pair(start, end)
            }
            PeriodType.YEAR -> {
                val start = Calendar.getInstance().apply {
                    add(Calendar.YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val end = Calendar.getInstance().time
                Timber.d("Период YEAR: $start - $end")
                Pair(start, end)
            }
            PeriodType.CUSTOM -> {
                // Для кастомного периода сохраняем текущие даты
                Timber.d("Период CUSTOM: ${currentState.startDate} - ${currentState.endDate}")
                Pair(currentState.startDate, currentState.endDate)
            }
        }
        
        // Обновляем состояние
        _state.update { 
            it.copy(
                periodType = periodType,
                startDate = startDate,
                endDate = endDate,
                // Сбрасываем состояние пагинации при изменении периода
                currentPage = 0,
                hasMoreData = true,
                // Сбрасываем списки транзакций
                transactions = emptyList(),
                filteredTransactions = emptyList(),
                groupedTransactions = emptyMap()
            ) 
        }
        
        // Загружаем транзакции для нового периода
        Timber.d("Перезагружаем транзакции для периода $periodType")
        loadTransactionsFirstPage()
    }

    private fun updateGroupingType(groupingType: GroupingType) {
        _state.update { it.copy(groupingType = groupingType) }
        updateFilteredAndGroupedTransactions()
    }

    private fun updateCategories(categories: List<String>) {
        _state.update { it.copy(selectedCategories = categories) }
        updateFilteredAndGroupedTransactions()
    }

    private fun updateSources(sources: List<String>) {
        _state.update { it.copy(selectedSources = sources) }
        updateFilteredAndGroupedTransactions()
    }

    private fun updateDateRange(startDate: Date, endDate: Date) {
        _state.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                periodType = PeriodType.CUSTOM
            )
        }
        updateFilteredAndGroupedTransactions()
    }

    private fun updateStartDate(date: Date) {
        _state.update { it.copy(startDate = date) }
        updateFilteredAndGroupedTransactions()
    }

    private fun updateEndDate(date: Date) {
        _state.update { it.copy(endDate = date) }
        updateFilteredAndGroupedTransactions()
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
            updateFilteredAndGroupedTransactions()
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
            updateFilteredAndGroupedTransactions()
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