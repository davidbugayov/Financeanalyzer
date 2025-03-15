package com.davidbugayov.financeanalyzer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import com.davidbugayov.financeanalyzer.utils.Event
import com.davidbugayov.financeanalyzer.utils.EventBus
import com.davidbugayov.financeanalyzer.utils.TestDataGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.util.Calendar

/**
 * ViewModel для главного экрана.
 * Следует принципам MVI и Clean Architecture.
 *
 * @property getTransactionsUseCase UseCase для загрузки транзакций
 * @property addTransactionUseCase UseCase для добавления новых транзакций
 * @property _state Внутренний MutableStateFlow для хранения состояния экрана
 * @property state Публичный StateFlow для наблюдения за состоянием экрана
 */
class HomeViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Кэши для хранения результатов вычислений
    private val filteredTransactionsCache = mutableMapOf<FilterCacheKey, List<Transaction>>()
    private val statsCache = mutableMapOf<List<Transaction>, Triple<Money, Money, Money>>()

    init {
        Timber.d("HomeViewModel initialized")
        loadTransactions()

        // TODO: УДАЛИТЬ ПЕРЕД РЕЛИЗОМ - Тестовый код для проверки Crashlytics
        // testCrashlytics()
        subscribeToEvents()
    }

    /**
     * Обрабатывает события экрана Home
     * @param event Событие для обработки
     */
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SetFilter -> {
                _state.update { it.copy(currentFilter = event.filter) }
                updateFilteredTransactions()
            }
            is HomeEvent.LoadTransactions -> {
                loadTransactions()
            }
            is HomeEvent.GenerateTestData -> {
                generateAndSaveTestData()
            }
            is HomeEvent.SetShowGroupSummary -> {
                _state.update { it.copy(showGroupSummary = event.show) }
            }
        }
    }

    /**
     * Подписываемся на события изменения транзакций через EventBus
     */
    private fun subscribeToEvents() {
        viewModelScope.launch {
            Timber.d("Subscribing to transaction events")
            EventBus.events.collect { event ->
                when (event) {
                    is Event.TransactionAdded,
                    is Event.TransactionDeleted,
                    is Event.TransactionUpdated -> {
                        // Очищаем кэши при изменении данных
                        clearCaches()
                        loadTransactions()
                    }
                }
            }
        }
    }

    /**
     * Загружает транзакции из репозитория и обновляет состояние
     */
    private fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val calendar = Calendar.getInstance()
                val endDate = calendar.time
                calendar.add(Calendar.MONTH, -1)
                val startDate = calendar.time

                getTransactionsUseCase(startDate, endDate)
                    .catch { exception ->
                        Timber.e(exception, "Failed to load transactions")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to load transactions"
                            )
                        }
                    }
                    .collectLatest { transactions ->
                        val income = calculateTotalIncome(transactions)
                        val expense = calculateTotalExpenses(transactions)
                        val balance = income - expense

                        _state.update {
                            it.copy(
                                transactions = transactions,
                                isLoading = false,
                                income = income,
                                expense = expense,
                                balance = balance
                            )
                        }
                        updateFilteredTransactions()
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading transactions")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error loading transactions"
                    )
                }
            }
        }
    }
    
    /**
     * Генерирует и сохраняет тестовые данные
     */
    private fun generateAndSaveTestData() {
        viewModelScope.launch {
            Timber.d("Generating test data")
            val testTransactions = TestDataGenerator.generateTransactions(20)

            var hasError = false
            testTransactions.forEach { transaction ->
                Timber.d("Saving test transaction: ${transaction.title}")
                addTransactionUseCase(transaction).fold(
                    onSuccess = { /* Transaction saved successfully */ },
                    onFailure = { exception: Throwable ->
                        hasError = true
                        Timber.e(exception, "Failed to save test transaction: ${transaction.title}")
                    }
                )
            }

            if (!hasError) {
                // Очищаем кэши при добавлении тестовых данных
                clearCaches()
                EventBus.emit(Event.TransactionAdded)
                Timber.d("Test data generation completed successfully")
            } else {
                _state.update { it.copy(error = "Ошибка при сохранении некоторых тестовых транзакций") }
            }
        }
    }

    /**
     * Обновляет отфильтрованные транзакции на основе текущего фильтра
     */
    private fun updateFilteredTransactions() {
        val currentState = _state.value

        // Создаем ключ для кэша
        val cacheKey = FilterCacheKey(
            transactions = currentState.transactions,
            filter = currentState.currentFilter.toString()
        )

        // Получаем отфильтрованные транзакции из кэша или вычисляем их
        val filtered = filteredTransactionsCache.getOrPut(cacheKey) {
            when (currentState.currentFilter) {
                TransactionFilter.TODAY -> getTodayTransactions(currentState.transactions)
                TransactionFilter.WEEK -> getLastWeekTransactions(currentState.transactions)
                TransactionFilter.MONTH -> getLastMonthTransactions(currentState.transactions)
            }
        }

        // Получаем статистику из кэша или вычисляем её
        val (filteredIncome, filteredExpense, filteredBalance) = statsCache.getOrPut(filtered) {
            // Рассчитываем суммы для отфильтрованных транзакций
            val income = filtered
                .filter { !it.isExpense }
                .map { it.amount }
                .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

            val expense = filtered
                .filter { it.isExpense }
                .map { it.amount }
                .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

            val balance = income - expense

            Triple(income, expense, balance)
        }

        _state.update {
            it.copy(
                filteredTransactions = filtered,
                filteredIncome = filteredIncome,
                filteredExpense = filteredExpense,
                filteredBalance = filteredBalance
            )
        }
    }

    /**
     * Возвращает транзакции за последний месяц
     * @param transactions Список всех транзакций
     * @return Отфильтрованный список транзакций за последний месяц
     */
    private fun getLastMonthTransactions(transactions: List<Transaction>): List<Transaction> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val monthAgo = calendar.time

        return transactions
            .filter { it.date.after(monthAgo) || it.date == monthAgo }
            .sortedByDescending { it.date }
    }
    
    /**
     * Возвращает транзакции за сегодня
     * @param transactions Список всех транзакций
     * @return Отфильтрованный список транзакций за сегодня
     */
    private fun getTodayTransactions(transactions: List<Transaction>): List<Transaction> {
        return transactions
            .filter { 
                val transactionDate = Calendar.getInstance().apply { time = it.date }
                val today = Calendar.getInstance()
                
                transactionDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                transactionDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            }
            .sortedByDescending { it.date }
    }
    
    /**
     * Возвращает транзакции за последнюю неделю
     * @param transactions Список всех транзакций
     * @return Отфильтрованный список транзакций за последнюю неделю
     */
    private fun getLastWeekTransactions(transactions: List<Transaction>): List<Transaction> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -7)
        val weekAgo = calendar.time

        return transactions
            .filter { it.date.after(weekAgo) || it.date == weekAgo }
            .sortedByDescending { it.date }
    }

    /**
     * Очищает все кэши
     */
    private fun clearCaches() {
        filteredTransactionsCache.clear()
        statsCache.clear()
    }

    /**
     * Ключ для кэша фильтрованных транзакций
     */
    private data class FilterCacheKey(
        val transactions: List<Transaction>,
        val filter: String
    )

    private fun calculateTotalIncome(transactions: List<Transaction>): Money {
        return transactions
            .filter { !it.isExpense }
            .map { it.amount }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()
    }

    private fun calculateTotalExpenses(transactions: List<Transaction>): Money {
        return transactions
            .filter { it.isExpense }
            .map { it.amount }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()
    }
} 