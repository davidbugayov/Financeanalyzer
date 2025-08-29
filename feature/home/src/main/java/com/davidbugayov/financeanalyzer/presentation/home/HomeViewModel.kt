package com.davidbugayov.financeanalyzer.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.navigation.Screen
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import com.davidbugayov.financeanalyzer.shared.SharedFacade
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.paging.TransactionListItem
import com.davidbugayov.financeanalyzer.utils.CurrencyProvider
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import com.davidbugayov.financeanalyzer.utils.TestDataGenerator
import com.davidbugayov.financeanalyzer.utils.kmp.toDomain
import com.davidbugayov.financeanalyzer.utils.kmp.toLocalDateKmp
import com.davidbugayov.financeanalyzer.utils.kmp.toShared
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext
import timber.log.Timber

/**
 * ViewModel для главного экрана.
 * Следует принципам MVI и Clean Architecture.
 *
 * @property addTransactionUseCase UseCase для добавления новых транзакций
 * @property deleteTransactionUseCase UseCase для удаления транзакций
 * @property getTransactionsForPeriodFlowUseCase UseCase для получения транзакций за период с кэшированием
 * @property calculateBalanceMetricsUseCase UseCase для расчета финансовых метрик
 * @property repository Репозиторий для прямого доступа к транзакциям с поддержкой пагинации и подпиской на изменения
 * @property _state Внутренний MutableStateFlow для хранения состояния экрана
 * @property state Публичный StateFlow для наблюдения за состоянием экрана
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: TransactionRepository,
    private val updateWidgetsUseCase: UpdateWidgetsUseCase,
    private val navigationManager: NavigationManager,
    private val sharedFacade: SharedFacade,
) : ViewModel(),
    KoinComponent {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Финансовые метрики
    private val financialMetrics = FinancialMetrics.getInstance()

    // -------- Paging ------------
    private val pagerTrigger = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)

    val pagedTransactions: Flow<PagingData<Transaction>> =
        pagerTrigger
            .flatMapLatest {
                val s = _state.value
                val (startDate, endDate) = getPeriodDates(s.currentFilter)
                val pageSize = 50
                Timber.d("HomeViewModel: Loading transactions with filter: ${s.currentFilter}, period: ${startDate} - ${endDate}")
                val result = if (s.currentFilter == TransactionFilter.ALL) {
                    Timber.d("HomeViewModel: Using getAllPaged")
                    repository.getAllPaged(pageSize)
                } else {
                    Timber.d("HomeViewModel: Using getByPeriodPaged with start=$startDate, end=$endDate")
                    repository.getByPeriodPaged(startDate, endDate, pageSize)
                }
                Timber.d("HomeViewModel: Created paging flow")
                result
            }.cachedIn(viewModelScope)

    val pagedUiModels: Flow<PagingData<TransactionListItem>> =
        pagedTransactions
            .map { pagingData ->
                Timber.d("HomeViewModel: Processing paging data")
                pagingData
                    .map { tx ->
                        Timber.d("HomeViewModel: Processing transaction: ${tx.id}, amount: ${tx.amount}, category: ${tx.category}")
                        TransactionListItem.Item(tx)
                    }
                    .insertSeparators { before: TransactionListItem.Item?, after: TransactionListItem.Item? ->
                        if (after == null) return@insertSeparators null

                        val beforeDateKey = before?.transaction?.date?.let { dayKey(it) }
                        val afterDateKey = dayKey(after.transaction.date)

                        if (before == null || beforeDateKey != afterDateKey) {
                            Timber.d("HomeViewModel: Adding header for date: $afterDateKey")
                            TransactionListItem.Header(afterDateKey)
                        } else {
                            null
                        }
                    }
            }.cachedIn(viewModelScope)

    private fun reloadPaged() {
        pagerTrigger.tryEmit(Unit)
    }

    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("ru"))

    private fun dayKey(date: java.util.Date): String = dateFormatter.format(date)

    init {
        // Первичная инициализация Paging
        pagerTrigger.tryEmit(Unit)
        subscribeToRepositoryChanges() // Подписываемся на изменения в репозитории

        // Наблюдаем за изменениями балансов
        viewModelScope.launch {
            financialMetrics.balance.collect { balance ->
                // Обновляем баланс без полной перезагрузки транзакций
                _state.update { it.copy(balance = balance) }
            }
        }

        viewModelScope.launch {
            financialMetrics.totalIncome.collect { income ->
                _state.update { it.copy(income = income) }
            }
        }

        viewModelScope.launch {
            financialMetrics.totalExpense.collect { expense ->
                _state.update { it.copy(expense = expense) }
            }
        }

        // Подписываемся на изменения валюты
        viewModelScope.launch {
            CurrencyProvider.getCurrencyFlow().collect { newCurrency ->
                // Пересчитываем данные с новой валютой
                loadTransactions()
            }
        }
    }

    /**
     * Обрабатывает события экрана Home
     * @param event Событие для обработки
     */
    fun onEvent(
        event: HomeEvent,
        context: Context? = null,
    ) {
        when (event) {
            is HomeEvent.SetFilter -> {
                Timber.d("HomeViewModel: Starting filter switch to ${event.filter}")

                // Получаем даты для нового фильтра
                val (startDate, endDate) = getPeriodDates(event.filter)
                Timber.d("HomeViewModel: Filter ${event.filter} - calculated dates: start=$startDate, end=$endDate")

                // Синхронно обновляем фильтр в состоянии (без загрузки данных)
                _state.update { it.copy(currentFilter = event.filter) }

                // Асинхронно обновляем данные состояния
                viewModelScope.launch {
                    try {
                        // Получаем транзакции для нового периода
                        Timber.d("HomeViewModel: Calling sharedFacade.transactionsForPeriodFlow with start=${startDate.toLocalDateKmp()}, end=${endDate.toLocalDateKmp()}")
                        val flow = sharedFacade.transactionsForPeriodFlow(startDate.toLocalDateKmp(), endDate.toLocalDateKmp())
                        val transactions = (flow?.first() ?: emptyList()).map { it.toDomain() }
                        Timber.d("HomeViewModel: Got ${transactions.size} transactions from sharedFacade")

                        // Фильтруем транзакции по диапазону дат
                        val filteredTransactions = transactions.filter { tx ->
                            val t = tx.date
                            val inRange = !t.before(startDate) && !t.after(endDate)
                            inRange
                        }

                        Timber.d("HomeViewModel: Filter ${event.filter} - loaded ${transactions.size} total, filtered ${filteredTransactions.size} transactions")
                        Timber.d("HomeViewModel: Sample transactions: ${filteredTransactions.take(3).map { "${it.id}: ${it.amount} (${if (it.isExpense) "expense" else "income"})" }}")

                        // Вычисляем статистику
                        val currentCurrency = CurrencyProvider.getCurrency()
                        val (filteredIncome, filteredExpense, filteredBalance) =
                            if (event.filter == TransactionFilter.ALL) {
                                val income = financialMetrics.getTotalIncomeAsMoney()
                                val expense = financialMetrics.getTotalExpenseAsMoney()
                                val balance = financialMetrics.getCurrentBalance()
                                Timber.d("HomeViewModel: ALL filter stats - Income: $income, Expense: $expense, Balance: $balance")
                                Triple(income, expense, balance)
                            } else {
                                Timber.d("HomeViewModel: Calculating stats for ${event.filter} with ${filteredTransactions.size} transactions")
                                val stats = calculateStats(filteredTransactions)
                                Timber.d("HomeViewModel: ${event.filter} filter stats - Income: ${stats.first}, Expense: ${stats.second}, Balance: ${stats.third}")
                                stats
                            }

                        val transactionGroups = groupTransactionsByDate(filteredTransactions, currentCurrency)

                        // Обновляем состояние с новыми данными
                        _state.update {
                            Timber.d("HomeViewModel: Updating state for filter ${event.filter} - transactions: ${filteredTransactions.size}, income: $filteredIncome, expense: $filteredExpense")
                            it.copy(
                                filteredTransactions = filteredTransactions,
                                transactionGroups = transactionGroups,
                                filteredIncome = filteredIncome,
                                filteredExpense = filteredExpense,
                                filteredBalance = filteredBalance,
                                periodStartDate = startDate,
                                periodEndDate = endDate,
                                error = null,
                            )
                        }

                        // Обновляем виджеты
                        updateWidgetsUseCase()

                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при обновлении данных фильтра: ${e.message}")
                        _state.update { it.copy(error = e.message) }
                    }
                }

                // Обновляем paging данные ПОСЛЕ обновления состояния
                Timber.d("HomeViewModel: Calling reloadPaged for filter ${event.filter}")
                reloadPaged()
            }
            is HomeEvent.LoadTransactions -> {
                loadTransactions()
            }
            is HomeEvent.GenerateTestData -> {
                generateAndSaveTestData()
            }
            is HomeEvent.CreateTestTransaction -> {
                createTestTransaction()
            }
            is HomeEvent.SetShowGroupSummary -> {
                Timber.d("HomeViewModel: SetShowGroupSummary event received: ${event.show}")
                _state.update {
                    Timber.d("HomeViewModel: Updating showGroupSummary from ${it.showGroupSummary} to ${event.show}")
                    it.copy(showGroupSummary = event.show)
                }
            }
            is HomeEvent.ShowDeleteConfirmDialog -> {
                _state.update { it.copy(transactionToDelete = event.transaction) }
            }

            is HomeEvent.HideDeleteConfirmDialog -> {
                _state.update { it.copy(transactionToDelete = null) }
            }

            is HomeEvent.DeleteTransaction -> {
                deleteTransaction(event.transaction, context)
            }
            is HomeEvent.NavigateToChart -> {
                // Получаем даты для текущего выбранного фильтра
                val (startDate, endDate) = getPeriodDates(state.value.currentFilter)

                // Преобразуем TransactionFilter в PeriodType
                val periodType =
                    when (state.value.currentFilter) {
                        TransactionFilter.TODAY -> "DAY"
                        TransactionFilter.WEEK -> "WEEK"
                        TransactionFilter.MONTH -> "MONTH"
                        TransactionFilter.ALL -> "ALL"
                    }

                // Передаем даты и тип периода в параметрах навигации
                navigationManager.navigate(
                    NavigationManager.Command.Navigate(
                        Screen.FinancialStatistics.createRoute(startDate.time, endDate.time, periodType),
                    ),
                )
            }
            is HomeEvent.NavigateToProfile -> {
                navigationManager.navigate(NavigationManager.Command.Navigate(Screen.Profile.route))
            }
            is HomeEvent.NavigateToHistory -> {
                navigationManager.navigate(NavigationManager.Command.Navigate(Screen.History.route))
            }
            is HomeEvent.NavigateToAddTransaction -> {
                navigationManager.navigate(
                    NavigationManager.Command.Navigate(Screen.AddTransaction.createRoute(forceExpense = true)),
                )
            }
            is HomeEvent.EditTransaction -> {
                navigationManager.navigate(
                    NavigationManager.Command.Navigate(Screen.EditTransaction.createRoute(event.transaction.id)),
                )
            }
            else -> {}
        }
    }

    /**
     * Удаляет транзакцию
     * @param transaction Транзакция для удаления
     * @param context Контекст для обновления виджетов (опционально)
     */
    private fun deleteTransaction(
        transaction: Transaction,
        context: Context? = null,
    ) {
        viewModelScope.launch {
            try {
                val result = sharedFacade.deleteTransaction(transaction.toShared())
                if (result) {
                    _state.update { it.copy(transactionToDelete = null) }

                    // Логируем событие в аналитику
                    com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils.logTransactionDeleted(
                        transactionType = if (transaction.isExpense) "EXPENSE" else "INCOME",
                        amount = transaction.amount.abs().toString(),
                        category = transaction.category,
                    )

                    context?.let { ctx ->
                        updateWidgetsUseCase()
                    } ?: Timber.w(
                        "Context не предоставлен в HomeViewModel, виджеты не обновлены после удаления.",
                    )
                } else {
                    val rp: ResourceProvider = GlobalContext.get().get()
                    _state.update {
                        it.copy(
                            error = rp.getString(UiR.string.error_failed_delete_transaction),
                            transactionToDelete = null,
                        )
                    }
                }
            } catch (e: Exception) {
                val rp: ResourceProvider = GlobalContext.get().get()
                _state.update {
                    it.copy(
                        error = e.message ?: rp.getString(UiR.string.error_unknown),
                        transactionToDelete = null,
                    )
                }
                throw e
            }
        }
    }

    /**
     * Подписываемся на изменения данных в репозитории
     */
    private fun subscribeToRepositoryChanges() {
        viewModelScope.launch {
            repository.dataChangeEvents.collect { event ->
                when (event) {
                    is com.davidbugayov.financeanalyzer.domain.repository.DataChangeEvent.TransactionChanged -> {
                        event.transactionId

                        // Плавное обновление без полной перезагрузки
                        updateDataSmoothly()
                    }
                    else -> {
                        // Для других типов изменений - полная перезагрузка
                        loadTransactions()
                    }
                }
            }
        }
    }

    /**
     * Плавное обновление данных без полной перезагрузки
     * Устарело: теперь используется только paging
     */
    private fun updateDataSmoothly() {
        // Теперь используется только paging, этот метод больше не нужен
        reloadPaged()
    }

    /**
     * Плавное обновление отфильтрованных транзакций без показа индикатора загрузки
     */
    private fun updateFilteredTransactionsSmoothly(
        filter: TransactionFilter,
        transactions: List<Transaction>,
    ) {
        viewModelScope.launch {
            val (startDate, endDate) = getPeriodDates(filter)
            val currentCurrency = CurrencyProvider.getCurrency()
            // Локальная гарантированная фильтрация по диапазону дат
            val strictlyFiltered =
                transactions.filter { tx ->
                    val t = tx.date
                    !t.before(startDate) && !t.after(endDate)
                }
            val (filteredIncome, filteredExpense, filteredBalance) =
                if (filter == TransactionFilter.ALL) {
                    val income = financialMetrics.getTotalIncomeAsMoney()
                    val expense = financialMetrics.getTotalExpenseAsMoney()
                    val balance = financialMetrics.getCurrentBalance()
                    Triple(income, expense, balance)
                } else {
                    val stats = calculateStats(strictlyFiltered)
                    stats
                }

            val transactionGroups = groupTransactionsByDate(strictlyFiltered, currentCurrency)

            _state.update {
                it.copy(
                    filteredTransactions = strictlyFiltered,
                    transactionGroups = transactionGroups,
                    filteredIncome = filteredIncome,
                    filteredExpense = filteredExpense,
                    filteredBalance = filteredBalance,
                    periodStartDate = startDate,
                    periodEndDate = endDate,
                    isLoading = false, // Не показываем индикатор загрузки
                )
            }

            // Обновляем статистику по категориям в фоне
            updateCategoryStats(transactions)
        }
        // reloadPaged() больше не нужен - paging обновляется автоматически через flow
    }

    /**
     * Инициирует фоновую загрузку данных
     * Обновляет данные в фоне, не блокируя UI
     * @param showLoading Показывать ли индикатор загрузки
     */
    private fun loadTransactions(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _state.update { it.copy(isLoading = true) }
            }

            try {
                val (startDate, endDate) = getPeriodDates(_state.value.currentFilter)
                sharedFacade.transactionsForPeriodFlow(startDate.toLocalDateKmp(), endDate.toLocalDateKmp())?.first()
                updateFilteredTransactions(_state.value.currentFilter)

                // Обновляем виджеты после загрузки данных
                updateWidgetsUseCase()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                throw e
            }
        }
    }

    /**
     * Подсчитывает и обновляет данные по категориям
     */
    private fun updateCategoryStats(transactions: List<Transaction>) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                if (transactions.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _state.update { it.copy(topCategories = emptyMap()) }
                    }
                    return@launch
                }

                // Группируем транзакции по категориям и суммируем значения
                val currentCurrency = CurrencyProvider.getCurrency()
                val categoryTotals =
                    transactions
                        .filter { it.isExpense } // Считаем только расходы
                        .groupBy { it.category }
                        .mapValues { (_, txs) ->
                            // Суммируем расходы по категории с использованием Money
                            txs.fold(Money.zero(currentCurrency)) { acc, transaction ->
                                val convertedAmount =
                                    Money.fromMajor(
                                        transaction.amount.toMajorDouble(),
                                        currentCurrency,
                                    )
                                acc + convertedAmount
                            }
                        }.toList()
                        .sortedByDescending {
                                (_, amount) ->
                            kotlin.math.abs(amount.toMajorDouble())
                        } // Сортируем по убыванию сумм
                        .take(3) // Берем только топ-3 категории
                        .toMap()

                // Обновляем состояние в основном потоке
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(topCategories = categoryTotals) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обновлении статистики по категориям: ${e.message}")
                throw e
            }
        }
    }

    /**
     * Создает тестовые данные для проверки отображения
     */
    private fun createTestTransaction() {
        viewModelScope.launch {
            try {
                Timber.d("HomeViewModel: Creating test transaction")
                val testTransaction = com.davidbugayov.financeanalyzer.domain.model.Transaction(
                    id = "test_${System.currentTimeMillis()}",
                    amount = Money.fromMajor(100.0, CurrencyProvider.getCurrency()),
                    category = "Тестовая категория",
                    isExpense = true,
                    date = java.util.Date(),
                    note = "Тестовая транзакция",
                    source = "Наличные",
                    sourceColor = 0xFF4CAF50.toInt(),
                    categoryId = "",
                    title = "Тест"
                )

                val result = sharedFacade.addTransaction(testTransaction.toShared())
                if (result) {
                    Timber.d("HomeViewModel: Test transaction created successfully")
                    // Данные обновятся автоматически через flow
                } else {
                    Timber.e("HomeViewModel: Failed to create test transaction")
                }
            } catch (e: Exception) {
                Timber.e(e, "HomeViewModel: Error creating test transaction")
            }
        }
    }

    /**
     * Генерирует и сохраняет тестовые данные (только в Debug-сборке)
     */
    private fun generateAndSaveTestData() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val testTransactions = TestDataGenerator.generateTransactions(80)
                var hasError = false
                testTransactions.forEach { transaction ->
                    try {
                        val result = sharedFacade.addTransaction(transaction.toShared())
                        if (!result) {
                            hasError = true
                            Timber.e("Failed to save test transaction: ${transaction.category}")
                        }
                    } catch (exception: Exception) {
                        hasError = true
                        Timber.e(
                            exception,
                            "Failed to save test transaction: ${transaction.category}",
                        )
                    }
                }
                if (!hasError) {
                    Timber.d("Test data generation completed successfully")
                } else {
                    val rp: ResourceProvider = GlobalContext.get().get()
                    _state.update { it.copy(error = rp.getString(UiR.string.error_saving_test_transactions)) }
                }
                loadTransactions()
            } catch (e: Exception) {
                Timber.e(e, "Error generating test data")
                val rp: ResourceProvider = GlobalContext.get().get()
                _state.update { it.copy(error = e.message ?: rp.getString(UiR.string.error_generating_test_data)) }
                throw e
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Обновляет список отфильтрованных транзакций на основе выбранного фильтра
     * @param filter Выбранный фильтр транзакций
     */
    private fun updateFilteredTransactions(filter: TransactionFilter) {
        viewModelScope.launch {
            val (startDate, endDate) = getPeriodDates(filter)
            val flow = sharedFacade.transactionsForPeriodFlow(startDate.toLocalDateKmp(), endDate.toLocalDateKmp())
            val raw = (flow?.first() ?: emptyList()).map { it.toDomain() }
            // Локальная гарантированная фильтрация по диапазону дат
            val filteredTransactions =
                raw.filter { tx ->
                    val t = tx.date
                    !t.before(startDate) && !t.after(endDate)
                }
            val currentCurrency = CurrencyProvider.getCurrency()

            val (filteredIncome, filteredExpense, filteredBalance) =
                if (filter == TransactionFilter.ALL) {
                    val income = financialMetrics.getTotalIncomeAsMoney()
                    val expense = financialMetrics.getTotalExpenseAsMoney()
                    val balance = financialMetrics.getCurrentBalance()
                    Triple(income, expense, balance)
                } else {
                    val stats = calculateStats(filteredTransactions)
                    stats
                }
            val transactionGroups = groupTransactionsByDate(filteredTransactions, currentCurrency)
            val tips = sharedFacade.smartExpenseTips(filteredTransactions.map { it.toShared() })
            val recommendations =
                sharedFacade.expenseOptimizationRecommendations(
                    filteredTransactions.map { it.toShared() },
                )
            _state.update {
                it.copy(
                    filteredTransactions = filteredTransactions,
                    transactionGroups = transactionGroups,
                    filteredIncome = filteredIncome,
                    filteredExpense = filteredExpense,
                    filteredBalance = filteredBalance,
                    periodStartDate = startDate,
                    periodEndDate = endDate,
                    isLoading = false,
                    smartExpenseTips = tips, // обновляем советы
                    expenseOptimizationRecommendations = recommendations, // сохраняем рекомендации
                )
            }
            // reloadPaged() больше не нужен - paging обновляется автоматически через flow
        }
    }

    /**
     * Вычисляет основные финансовые показатели для списка транзакций:
     * доход, расход и баланс
     *
     * @param transactions Список транзакций для анализа
     * @return Triple из (доход, расход, баланс)
     */
    private fun calculateStats(transactions: List<Transaction>): Triple<Money, Money, Money> {
        // Транзакции сюда передаются уже ОТФИЛЬТРОВАННЫМИ по текущему периоду в updateFilteredTransactions*
        val currentCurrency = CurrencyProvider.getCurrency()
        Timber.d("calculateStats: Processing ${transactions.size} transactions")

        if (transactions.isEmpty()) {
            Timber.d("calculateStats: No transactions, returning zeros")
            return Triple(Money.zero(currentCurrency), Money.zero(currentCurrency), Money.zero(currentCurrency))
        }

        val income =
            transactions
                .asSequence()
                .filter { !it.isExpense }
                .fold(Money.zero(currentCurrency)) { acc, tx ->
                    Timber.d("calculateStats: Adding income ${tx.id}: ${tx.amount}")
                    acc + tx.amount
                }

        val expense =
            transactions
                .asSequence()
                .filter { it.isExpense }
                .fold(Money.zero(currentCurrency)) { acc, tx ->
                    Timber.d("calculateStats: Adding expense ${tx.id}: ${tx.amount}")
                    acc + tx.amount.abs()
                }

        val balance = income - expense
        Timber.d("calculateStats: Final result - Income: $income, Expense: $expense, Balance: $balance")
        return Triple(income, expense, balance)
    }

    /**
     * Группирует транзакции по дате
     * @param transactions Список транзакций
     * @return Список групп транзакций, сгруппированных по дате
     */
    private fun groupTransactionsByDate(
        transactions: List<Transaction>,
        currentCurrency: Currency = CurrencyProvider.getCurrency(),
    ): List<com.davidbugayov.financeanalyzer.domain.model.TransactionGroup> {
        if (transactions.isEmpty()) {
            return emptyList()
        }

        // Группируем транзакции по дате (без времени)
        val groupedTransactions =
            transactions.groupBy { transaction ->
                // Получаем только дату без времени
                val calendar = Calendar.getInstance()
                calendar.time = transaction.date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.time
            }

        // Сортируем даты по убыванию (сначала новые)
        val sortedDates = groupedTransactions.keys.sortedByDescending { it }

        // Создаем группы транзакций
        return sortedDates.map { date ->
            val transactionsForDate = groupedTransactions[date] ?: emptyList()

            // Вычисляем сумму доходов и расходов для группы
            val income =
                transactionsForDate
                    .filter { !it.isExpense }
                    .fold(Money.zero(currentCurrency)) { acc, transaction -> acc + transaction.amount }

            val expense =
                transactionsForDate
                    .filter { it.isExpense }
                    .fold(Money.zero(currentCurrency)) { acc, transaction -> acc + transaction.amount.abs() }

            // Сортируем транзакции внутри группы по времени (сначала новые)
            val sortedTransactions = transactionsForDate.sortedByDescending { it.date }

            // Форматируем дату для отображения в UI
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            dateFormat.format(date)

            // Вычисляем общий баланс (доходы - расходы)
            val balance = income - expense
            val balanceDouble = balance.amount.toDouble()

            com.davidbugayov.financeanalyzer.domain.model.TransactionGroup(
                date = date,
                transactions = sortedTransactions,
                balance = balanceDouble,
            )
        }
    }

    private fun getPeriodDates(filter: TransactionFilter): Pair<java.util.Date, java.util.Date> {
        val calendar = Calendar.getInstance()
        // Устанавливаем endDate на конец дня
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time
        val startCalendar = Calendar.getInstance()
        Timber.d("HomeViewModel: Calculating period dates for filter $filter")
        when (filter) {
            TransactionFilter.TODAY -> {
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
            }
            TransactionFilter.WEEK -> {
                startCalendar.add(Calendar.DAY_OF_MONTH, -6)
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
            }
            TransactionFilter.MONTH -> {
                // Устанавливаем начало текущего месяца
                startCalendar.set(Calendar.DAY_OF_MONTH, 1)
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
            }
            TransactionFilter.ALL -> {
                startCalendar.set(2000, 0, 1, 0, 0, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
            }
        }
        val startDate = startCalendar.time
        Timber.d("HomeViewModel: Period dates calculated - start: $startDate, end: $endDate")
        return Pair(startDate, endDate)
    }
}
