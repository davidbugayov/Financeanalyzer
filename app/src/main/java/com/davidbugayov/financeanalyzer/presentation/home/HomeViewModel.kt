package com.davidbugayov.financeanalyzer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import com.davidbugayov.financeanalyzer.utils.Event
import com.davidbugayov.financeanalyzer.utils.EventBus
import com.davidbugayov.financeanalyzer.utils.TestDataGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.util.Calendar

/**
 * ViewModel для главного экрана.
 * Следует принципам MVI и Clean Architecture.
 *
 * @property loadTransactionsUseCase UseCase для загрузки транзакций
 * @property addTransactionUseCase UseCase для добавления новых транзакций
 * @property _state Внутренний MutableStateFlow для хранения состояния экрана
 * @property state Публичный StateFlow для наблюдения за состоянием экрана
 */
class HomeViewModel(
    private val loadTransactionsUseCase: LoadTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        Timber.d("HomeViewModel initialized")
        loadTransactions()
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
                    is Event.TransactionUpdated -> loadTransactions()
                }
            }
        }
    }

    /**
     * Загружает транзакции из репозитория и обновляет состояние
     */
    private fun loadTransactions() {
        viewModelScope.launch {
            loadTransactionsUseCase().fold(
                onSuccess = { transactions: List<Transaction> ->
                    _state.update { it.copy(transactions = transactions) }
                    updateFilteredTransactions()
                    calculateTotalStats()
                },
                onFailure = { exception: Throwable ->
                    Timber.e(exception, "Failed to load transactions")
                    _state.update { it.copy(error = exception.message ?: "Failed to load transactions") }
                }
            )
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
        val filtered = when (currentState.currentFilter) {
            TransactionFilter.TODAY -> getTodayTransactions(currentState.transactions)
            TransactionFilter.WEEK -> getLastWeekTransactions(currentState.transactions)
            TransactionFilter.MONTH -> getLastMonthTransactions(currentState.transactions)
        }

        // Рассчитываем суммы для отфильтрованных транзакций
        val filteredIncome = filtered.filter { !it.isExpense }.sumOf { it.amount }
        val filteredExpense = filtered.filter { it.isExpense }.sumOf { it.amount }
        val filteredBalance = filteredIncome - filteredExpense

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
     * Рассчитывает общую статистику по всем транзакциям
     */
    private fun calculateTotalStats() {
        Timber.d("Calculating total stats")
        val transactions = _state.value.transactions

        val totalIncome = transactions
            .filter { !it.isExpense }
            .sumOf { it.amount }

        val totalExpense = transactions
            .filter { it.isExpense }
            .sumOf { it.amount }

        val balance = totalIncome - totalExpense

        _state.update {
            it.copy(
                income = totalIncome,
                expense = totalExpense,
                balance = balance
            )
        }

        Timber.d("Total stats calculated: Income=$totalIncome, Expense=$totalExpense, Balance=$balance")
    }

    /**
     * Добавляет новую транзакцию
     * @param transaction Транзакция для добавления
     */
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            addTransactionUseCase(transaction).fold(
                onSuccess = {
                    loadTransactions()
                },
                onFailure = { exception: Throwable ->
                    Timber.e(exception, "Failed to add transaction")
                    _state.update { it.copy(error = exception.message ?: "Failed to add transaction") }
                }
            )
        }
    }
}

/**
 * Перечисление для фильтров транзакций
 */
enum class TransactionFilter {

    /** Фильтр для отображения транзакций за сегодня */
    TODAY,

    /** Фильтр для отображения транзакций за неделю */
    WEEK,

    /** Фильтр для отображения транзакций за месяц */
    MONTH
} 