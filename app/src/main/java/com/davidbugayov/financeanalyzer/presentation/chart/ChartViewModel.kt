package com.davidbugayov.financeanalyzer.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartMonthlyData
import com.davidbugayov.financeanalyzer.utils.Event
import com.davidbugayov.financeanalyzer.utils.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel для экрана с графиками.
 * Отвечает за подготовку данных для отображения на графиках.
 */
class ChartViewModel(
    private val loadTransactionsUseCase: LoadTransactionsUseCase
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> get() = _transactions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    init {
        loadTransactions()
        subscribeToEvents()
    }

    /**
     * Подписываемся на события изменения транзакций
     */
    private fun subscribeToEvents() {
        viewModelScope.launch {
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
     * Загружает транзакции из репозитория
     */
    fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                when (val result = loadTransactionsUseCase()) {
                    is Result.Success -> {
                        val transactions = result.data
                        _transactions.value = transactions
                        updateChartData(transactions)
                    }
                    is Result.Error -> {
                        val exception = result.exception
                        Timber.e("Failed to load transactions: ${exception.message}")
                        _error.value = "Ошибка при загрузке транзакций: ${exception.message}"
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading transactions")
                _error.value = "Ошибка при загрузке транзакций: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Обновляет данные для графиков
     */
    private fun updateChartData(transactions: List<Transaction>) {
        try {
            // Обновляем данные для всех графиков
            getExpensesByCategory()
            getIncomeByCategory()
            getTransactionsByMonth()
            getExpensesByDay()
        } catch (e: Exception) {
            Timber.e(e, "Error updating chart data")
            _error.value = "Ошибка при обновлении данных графиков: ${e.message}"
        }
    }

    /**
     * Возвращает данные для графика расходов по категориям
     * @return Карта категорий и сумм расходов
     */
    fun getExpensesByCategory(): Map<String, Double> {
        return _transactions.value
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }

    /**
     * Возвращает данные для графика доходов по категориям
     * @return Карта категорий и сумм доходов
     */
    fun getIncomeByCategory(): Map<String, Double> {
        return _transactions.value
            .filter { !it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }

    /**
     * Возвращает данные для графика транзакций по месяцам с разбивкой по категориям
     * @return Карта месяцев и транзакций по категориям
     */
    fun getTransactionsByMonth(): Map<String, ChartMonthlyData> {
        val dateFormat = SimpleDateFormat("MM.yyyy", Locale.getDefault())
        
        return _transactions.value
            .groupBy { dateFormat.format(it.date) }
            .mapValues { (_, transactions) ->
                val income = transactions.filter { !it.isExpense }.sumOf { it.amount }
                val expense = transactions.filter { it.isExpense }.sumOf { it.amount }
                
                // Группируем расходы по категориям
                val categoryBreakdown = transactions
                    .filter { it.isExpense }
                    .groupBy { it.category }
                    .mapValues { (_, categoryTransactions) ->
                        categoryTransactions.sumOf { it.amount }
                    }

                ChartMonthlyData(
                    totalIncome = income,
                    totalExpense = expense,
                    categoryBreakdown = categoryBreakdown
                )
            }
            .toSortedMap()
    }

    /**
     * Возвращает данные для графика расходов по дням
     * @param days Количество дней для отображения
     * @return Карта дней и данных о расходах
     */
    fun getExpensesByDay(days: Int = 7): Map<String, ChartMonthlyData> {
        val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        val daysInMillis = days * 24 * 60 * 60 * 1000L
        
        return _transactions.value
            .filter { (currentTime - it.date.time) <= daysInMillis }
            .groupBy { dateFormat.format(it.date) }
            .mapValues { (_, transactions) ->
                val dailyExpenses = transactions.filter { it.isExpense }
                val dailyIncome = transactions.filter { !it.isExpense }
                
                // Группируем расходы по категориям
                val categoryBreakdown = dailyExpenses
                    .groupBy { it.category }
                    .mapValues { (_, categoryTransactions) ->
                        categoryTransactions.sumOf { it.amount }
                    }

                ChartMonthlyData(
                    totalIncome = dailyIncome.sumOf { it.amount },
                    totalExpense = dailyExpenses.sumOf { it.amount },
                    categoryBreakdown = categoryBreakdown
                )
            }
            .toSortedMap()
    }
} 