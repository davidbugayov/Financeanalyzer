package com.davidbugayov.financeanalyzer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
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
     * Подписываемся на события изменения транзакций
     */
    private fun subscribeToEvents() {
        viewModelScope.launch {
            Timber.d("Subscribing to transaction events")
            EventBus.events.collect { event ->
                when (event) {
                    is Event.TransactionAdded -> {
                        Timber.d("Transaction added event received")
                        loadTransactions()
                    }
                    is Event.TransactionDeleted -> {
                        Timber.d("Transaction deleted event received")
                        loadTransactions()
                    }
                    is Event.TransactionUpdated -> {
                        Timber.d("Transaction updated event received")
                        loadTransactions()
                    }
                }
            }
        }
    }

    /**
     * Загружает транзакции из репозитория
     */
    private fun loadTransactions() {
        viewModelScope.launch {
            Timber.d("Loading transactions")
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = loadTransactionsUseCase()
                _state.update { it.copy(transactions = result) }
                Timber.d("Loaded ${result.size} transactions")
                calculateTotalStats()
                calculateDailyStats()
                updateFilteredTransactions()
            } catch (e: Exception) {
                Timber.e(e, "Error loading transactions")
                _state.update { it.copy(error = e.message) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Генерирует и сохраняет тестовые данные
     */
    private fun generateAndSaveTestData() {
        viewModelScope.launch {
            try {
                Timber.d("Generating test data")
                val testTransactions = TestDataGenerator.generateTransactions(20)
                
                testTransactions.forEach { transaction ->
                    Timber.d("Saving test transaction: ${transaction.title}")
                    addTransactionUseCase(transaction)
                }
                
                EventBus.emit(Event.TransactionAdded)
                Timber.d("Test data generation completed")
            } catch (e: Exception) {
                Timber.e(e, "Error generating test data")
                _state.update { it.copy(error = "Ошибка при генерации тестовых данных: ${e.message}") }
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
        _state.update { it.copy(filteredTransactions = filtered) }
    }

    /**
     * Возвращает транзакции за последний месяц
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
     * Рассчитывает общую статистику
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
     * Рассчитывает ежедневную статистику
     */
    private fun calculateDailyStats() {
        Timber.d("Calculating daily stats")
        val today = Calendar.getInstance()
        val transactions = _state.value.transactions

        val todayTransactions = transactions.filter { transaction ->
            val transactionDate = Calendar.getInstance().apply { time = transaction.date }
            transactionDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            transactionDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }

        val dailyIncome = todayTransactions
            .filter { !it.isExpense }
            .sumOf { it.amount }

        val dailyExpense = todayTransactions
            .filter { it.isExpense }
            .sumOf { it.amount }

        _state.update {
            it.copy(
                dailyIncome = dailyIncome,
                dailyExpense = dailyExpense
            )
        }

        Timber.d("Daily stats calculated: Income=$dailyIncome, Expense=$dailyExpense")
    }
}

/**
 * Перечисление для фильтров транзакций
 */
enum class TransactionFilter {
    TODAY, WEEK, MONTH
} 