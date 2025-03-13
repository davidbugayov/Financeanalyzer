package com.davidbugayov.financeanalyzer.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartMonthlyData
import com.davidbugayov.financeanalyzer.utils.Event
import com.davidbugayov.financeanalyzer.utils.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel для экрана с графиками.
 * Отвечает за подготовку данных для отображения на графиках.
 */
class ChartViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChartViewState())
    val state: StateFlow<ChartViewState> = _state.asStateFlow()

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

    fun handleIntent(intent: ChartIntent) {
        when (intent) {
            is ChartIntent.UpdateStartDate -> {
                _state.update { it.copy(startDate = intent.date) }
                loadTransactions()
            }
            is ChartIntent.UpdateEndDate -> {
                _state.update { it.copy(endDate = intent.date) }
                loadTransactions()
            }
            is ChartIntent.UpdateDateRange -> {
                _state.update { it.copy(startDate = intent.startDate, endDate = intent.endDate) }
                loadTransactions()
            }
            is ChartIntent.ToggleExpenseView -> {
                _state.update { it.copy(showExpenses = intent.showExpenses) }
            }
            ChartIntent.LoadTransactions -> {
                loadTransactions()
            }
        }
    }

    /**
     * Загружает транзакции из репозитория
     */
    fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val transactions = getTransactionsUseCase(
                    startDate = state.value.startDate,
                    endDate = state.value.endDate
                ).first()
                _state.update { it.copy(transactions = transactions, isLoading = false) }
                updateChartData(transactions)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    /**
     * Обновляет данные для графиков
     */
    private fun updateChartData(transactions: List<Transaction>) {
        try {
            _state.update { it.copy(transactions = transactions) }
        } catch (e: Exception) {
            Timber.e(e, "Error updating chart data")
            _state.update { it.copy(error = "Ошибка при обновлении данных графиков: ${e.message}") }
        }
    }

    /**
     * Возвращает данные для графика расходов по категориям для указанных транзакций
     * @param transactions Список транзакций для анализа
     * @return Карта категорий и сумм расходов
     */
    fun getExpensesByCategory(transactions: List<Transaction>): Map<String, Money> {
        return transactions
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.fold(Money.zero()) { acc, transaction ->
                    acc + transaction.amount
                }
            }
    }

    /**
     * Возвращает данные для графика доходов по категориям для указанных транзакций
     * @param transactions Список транзакций для анализа
     * @return Карта категорий и сумм доходов
     */
    fun getIncomeByCategory(transactions: List<Transaction>): Map<String, Money> {
        return transactions
            .filter { !it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.fold(Money.zero()) { acc, transaction ->
                    acc + transaction.amount
                }
            }
    }

    /**
     * Возвращает данные для графика расходов по дням для указанных транзакций
     * @param days Количество дней для отображения
     * @param transactions Список транзакций для анализа
     * @return Карта дней и данных о расходах
     */
    fun getExpensesByDay(days: Int = 7, transactions: List<Transaction>): Map<String, ChartMonthlyData> {
        val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        val daysInMillis = days * 24 * 60 * 60 * 1000L

        return transactions
            .filter { (currentTime - it.date.time) <= daysInMillis }
            .groupBy { dateFormat.format(it.date) }
            .mapValues { (_, txs) ->
                val dailyExpenses = txs.filter { it.isExpense }
                val dailyIncome = txs.filter { !it.isExpense }

                // Группируем расходы по категориям
                val categoryBreakdown = dailyExpenses
                    .groupBy { it.category }
                    .mapValues { (_, categoryTransactions) ->
                        categoryTransactions
                            .map { it.amount }
                            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()
                    }

                ChartMonthlyData(
                    totalIncome = dailyIncome
                        .map { it.amount }
                        .reduceOrNull { acc, money -> acc + money } ?: Money.zero(),
                    totalExpense = dailyExpenses
                        .map { it.amount }
                        .reduceOrNull { acc, money -> acc + money } ?: Money.zero(),
                    categoryBreakdown = categoryBreakdown
                )
            }
            .toSortedMap()
    }
}
