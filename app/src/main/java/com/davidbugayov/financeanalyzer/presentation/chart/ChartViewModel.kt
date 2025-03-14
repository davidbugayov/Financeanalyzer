package com.davidbugayov.financeanalyzer.presentation.chart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.DailyExpense
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.chart.model.ChartMonthlyData
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartIntent
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartScreenState
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChartViewModel : ViewModel(), KoinComponent {

    private val getTransactionsUseCase: GetTransactionsUseCase by inject()
    private val _state = MutableStateFlow(
        ChartScreenState(
            startDate = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time,
            endDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time,
            periodType = PeriodType.MONTH
        )
    )
    val state: StateFlow<ChartScreenState> = _state

    init {
        Log.d("ChartViewModel", "Initializing with date range: ${formatDate(_state.value.startDate)} - ${formatDate(_state.value.endDate)}")
        resetDateFilter()
        handleIntent(ChartIntent.LoadTransactions)
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru")).format(date)
    }

    fun handleIntent(intent: ChartIntent) {
        when (intent) {
            is ChartIntent.LoadTransactions -> loadTransactions()
            is ChartIntent.UpdateStartDate -> updateStartDate(intent.date)
            is ChartIntent.UpdateEndDate -> updateEndDate(intent.date)
            is ChartIntent.UpdateDateRange -> updateDateRange(intent.startDate, intent.endDate)
            is ChartIntent.ToggleExpenseView -> toggleExpenseView(intent.showExpenses)
            is ChartIntent.SetPeriodType -> setPeriodType(intent.periodType)
            is ChartIntent.ShowPeriodDialog -> _state.update { it.copy(showPeriodDialog = true) }
            is ChartIntent.HidePeriodDialog -> _state.update { it.copy(showPeriodDialog = false) }
            is ChartIntent.ShowStartDatePicker -> _state.update { it.copy(showStartDatePicker = true) }
            is ChartIntent.HideStartDatePicker -> _state.update { it.copy(showStartDatePicker = false) }
            is ChartIntent.ShowEndDatePicker -> _state.update { it.copy(showEndDatePicker = true) }
            is ChartIntent.HideEndDatePicker -> _state.update { it.copy(showEndDatePicker = false) }
            is ChartIntent.ResetDateFilter -> {
                Log.d("ChartViewModel", "Explicitly resetting date filter")
                resetDateFilter()
                loadTransactions()
            }
        }
    }

    private fun setPeriodType(periodType: PeriodType) {
        val calendar = Calendar.getInstance()
        val endDate = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        val startDate = calendar.apply {
            when (periodType) {
                PeriodType.ALL -> add(Calendar.YEAR, -10)
                PeriodType.DAY -> add(Calendar.DAY_OF_MONTH, -1)
                PeriodType.WEEK -> add(Calendar.WEEK_OF_YEAR, -1)
                PeriodType.MONTH -> add(Calendar.MONTH, -1)
                PeriodType.QUARTER -> add(Calendar.MONTH, -3)
                PeriodType.YEAR -> add(Calendar.YEAR, -1)
                PeriodType.CUSTOM -> Unit // Не меняем даты для пользовательского периода
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        _state.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                periodType = periodType
            )
        }

        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getTransactionsUseCase(_state.value.startDate, _state.value.endDate)
                    .catch { e ->
                        Log.e("ChartViewModel", "Error loading transactions", e)
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = e.message
                            )
                        }
                    }
                    .collectLatest { transactions ->
                        Log.d("ChartViewModel", "Loaded ${transactions.size} transactions")
                        _state.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = null,
                                transactions = transactions,
                                dailyExpenses = calculateDailyExpenses(transactions)
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("ChartViewModel", "Error loading transactions", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun updateStartDate(date: Date) {
        Log.d("ChartViewModel", "Updating start date to: ${formatDate(date)}")
        _state.update { currentState ->
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            currentState.copy(
                startDate = calendar.time,
                dailyExpenses = calculateDailyExpenses(currentState.transactions)
            )
        }
    }

    private fun updateEndDate(date: Date) {
        Log.d("ChartViewModel", "Updating end date to: ${formatDate(date)}")
        _state.update { currentState ->
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            currentState.copy(
                endDate = calendar.time,
                dailyExpenses = calculateDailyExpenses(currentState.transactions)
            )
        }
    }

    private fun updateDateRange(startDate: Date, endDate: Date) {
        Log.d("ChartViewModel", "Updating date range to: ${formatDate(startDate)} - ${formatDate(endDate)}")
        _state.update { currentState ->
            val startCalendar = Calendar.getInstance().apply {
                time = startDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCalendar = Calendar.getInstance().apply {
                time = endDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            currentState.copy(
                startDate = startCalendar.time,
                endDate = endCalendar.time,
                dailyExpenses = calculateDailyExpenses(currentState.transactions)
            )
        }
    }

    private fun toggleExpenseView(showExpenses: Boolean) {
        _state.update { it.copy(showExpenses = showExpenses) }
    }

    fun getExpensesByCategory(transactions: List<Transaction>): Map<String, Money> {
        return transactions
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions
                    .map { it.amount }
                    .reduceOrNull { acc, money -> acc + money } ?: Money.zero()
            }
    }

    fun getIncomeByCategory(transactions: List<Transaction>): Map<String, Money> {
        return transactions
            .filter { !it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions
                    .map { it.amount }
                    .reduceOrNull { acc, money -> acc + money } ?: Money.zero()
            }
    }

    fun getExpensesByDay(days: Int, transactions: List<Transaction>): Map<String, ChartMonthlyData> {
        val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        val daysInMillis = days * 24 * 60 * 60 * 1000L

        return transactions
            .filter { (currentTime - it.date.time) <= daysInMillis }
            .groupBy { dateFormat.format(it.date) }
            .mapValues { (_, transactions) ->
                val dailyExpenses = transactions.filter { it.isExpense }
                val dailyIncome = transactions.filter { !it.isExpense }
                
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

    private fun calculateDailyExpenses(transactions: List<Transaction>): List<DailyExpense> {
        Log.d("ChartViewModel", "Calculating daily expenses for ${transactions.size} transactions")
        Log.d("ChartViewModel", "Date range: ${formatDate(_state.value.startDate)} - ${formatDate(_state.value.endDate)}")

        val filteredTransactions = transactions.filter { transaction ->
            transaction.date.time >= _state.value.startDate.time &&
                    transaction.date.time <= _state.value.endDate.time
        }

        Log.d("ChartViewModel", "Filtered transactions: ${filteredTransactions.size}")

        return filteredTransactions
            .groupBy { transaction ->
                Calendar.getInstance().apply {
                    time = transaction.date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            }
            .map { (date, transactionsForDate) ->
                val expense = transactionsForDate
                    .filter { it.isExpense }
                    .map { it.amount }
                    .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

                DailyExpense(
                    date = date,
                    amount = expense.amount.toDouble()
                )
            }
            .sortedBy { it.date }
            .also { dailyExpenses ->
                Log.d("ChartViewModel", "Generated ${dailyExpenses.size} daily expenses")
                dailyExpenses.forEach { expense ->
                    Log.d("ChartViewModel", "Daily expense: ${formatDate(expense.date)} - Amount: ${expense.amount}")
                }
            }
    }

    /**
     * Сбрасывает фильтр дат на значение по умолчанию (месяц)
     */
    private fun resetDateFilter() {
        val calendar = Calendar.getInstance()
        val endDate = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        val startDate = calendar.apply {
            add(Calendar.MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        _state.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                periodType = PeriodType.MONTH
            )
        }
    }

    /**
     * Вызывается при уничтожении ViewModel (когда пользователь выходит с экрана)
     * Сбрасывает выбор даты к значениям по умолчанию
     */
    override fun onCleared() {
        super.onCleared()
        Log.d("ChartViewModel", "onCleared: Resetting date filter")
        resetDateFilter()
    }
} 
