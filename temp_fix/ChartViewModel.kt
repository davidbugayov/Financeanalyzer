package com.davidbugayov.financeanalyzer.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.DailyExpense
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartIntent
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartScreenState
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import com.davidbugayov.financeanalyzer.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChartViewModel : ViewModel(), KoinComponent {

    private val getTransactionsUseCase: GetTransactionsUseCase by inject()
    private val repository: TransactionRepository by inject()
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

    private var allTransactions: List<Transaction> = emptyList()

    init {
        Timber.d("Initializing with date range: ${formatDate(_state.value.startDate)} - ${formatDate(_state.value.endDate)}")
        resetDateFilter()
        handleIntent(ChartIntent.LoadTransactions)
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru")).format(date)
    }
    
    fun handleIntent(intent: ChartIntent) {
        when (intent) {
            is ChartIntent.LoadTransactions -> loadTransactions()
            is ChartIntent.ToggleExpenseView -> toggleExpenseView(intent.showExpenses)
            is ChartIntent.SetDateRange -> setDateRange(intent.startDate, intent.endDate)
            is ChartIntent.SetPeriodType -> setPeriodType(intent.periodType)
            is ChartIntent.TogglePeriodDialog -> togglePeriodDialog(intent.show)
            is ChartIntent.ToggleStartDatePicker -> toggleStartDatePicker(intent.show)
            is ChartIntent.ToggleEndDatePicker -> toggleEndDatePicker(intent.show)
            is ChartIntent.ToggleSavingsRateInfo -> toggleSavingsRateInfo(intent.show)
        }
    }
    
    // Публичный метод для загрузки транзакций, используемый в MainScreen
    fun loadTransactions() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                val transactions = getTransactionsUseCase().firstOrNull() ?: emptyList()
                allTransactions = transactions
                
                // Фильтруем транзакции по датам
                val filteredTransactions = allTransactions.filter { transaction ->
                    transaction.date >= _state.value.startDate && transaction.date <= _state.value.endDate
                }
                
                // Рассчитываем ежедневные расходы
                val dailyExpenses = calculateDailyExpenses(filteredTransactions)
                
                // Рассчитываем суммы доходов и расходов
                val (income, expense) = calculateIncomeAndExpense(filteredTransactions)
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        transactions = filteredTransactions,
                        dailyExpenses = dailyExpenses,
                        income = income,
                        expense = expense
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading transactions")
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    private fun calculateIncomeAndExpense(transactions: List<Transaction>): Pair<Money, Money> {
        val income = transactions
            .filter { transaction -> transaction.amount.amount.toFloat() > 0 }
            .fold(Money.zero()) { acc, transaction -> acc + transaction.amount }
            
        val expense = transactions
            .filter { transaction -> transaction.amount.amount.toFloat() < 0 }
            .fold(Money.zero()) { acc, transaction -> acc + transaction.amount.abs() }
            
        return income to expense
    }
    
    private fun calculateDailyExpenses(transactions: List<Transaction>): List<DailyExpense> {
        // Группируем по дате
        val expensesByDay = transactions
            .filter { transaction -> transaction.amount.amount.toFloat() < 0 }
            .groupBy { DateUtils.truncateToDay(it.date) }
        
        // Преобразуем в список DailyExpense
        return expensesByDay.entries
            .sortedBy { it.key }
            .map { (date, transactions) ->
                val total = transactions.fold(Money.zero()) { acc, transaction ->
                    acc + transaction.amount.abs()
                }
                
                DailyExpense(
                    date = date,
                    amount = total
                )
            }
    }
    
    private fun toggleExpenseView(showExpenses: Boolean) {
        _state.update { it.copy(showExpenses = showExpenses) }
    }
    
    private fun setDateRange(startDate: Date, endDate: Date) {
        _state.update { it.copy(startDate = startDate, endDate = endDate) }
        loadTransactions()
    }
    
    private fun setPeriodType(periodType: PeriodType) {
        _state.update { it.copy(periodType = periodType) }
        
        // Обновляем даты в зависимости от типа периода
        val (start, end) = calculateDatesForPeriod(periodType)
        _state.update { it.copy(startDate = start, endDate = end) }
        
        loadTransactions()
    }
    
    private fun calculateDatesForPeriod(periodType: PeriodType): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        
        val startDate = when (periodType) {
            PeriodType.DAY -> calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            PeriodType.WEEK -> calendar.apply {
                add(Calendar.DAY_OF_YEAR, -7)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            PeriodType.MONTH -> calendar.apply {
                add(Calendar.MONTH, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            PeriodType.QUARTER -> calendar.apply {
                add(Calendar.MONTH, -3)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            PeriodType.YEAR -> calendar.apply {
                add(Calendar.YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            PeriodType.ALL, PeriodType.CUSTOM -> calendar.apply {
                set(Calendar.YEAR, 2000)
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
        }
        
        return startDate to endDate
    }
    
    private fun togglePeriodDialog(show: Boolean) {
        _state.update { it.copy(showPeriodDialog = show) }
    }
    
    private fun toggleStartDatePicker(show: Boolean) {
        _state.update { it.copy(showStartDatePicker = show) }
    }
    
    private fun toggleEndDatePicker(show: Boolean) {
        _state.update { it.copy(showEndDatePicker = show) }
    }
    
    private fun toggleSavingsRateInfo(show: Boolean) {
        _state.update { it.copy(showSavingsRateInfo = show) }
    }
    
    private fun resetDateFilter() {
        setPeriodType(PeriodType.MONTH)
    }
    
    // Для совместимости со старым кодом
    fun getExpensesByCategory(transactions: List<Transaction>): Map<String, Money> {
        return transactions
            .filter { transaction -> transaction.amount.amount.toFloat() < 0 }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.fold(Money.zero()) { acc, transaction -> 
                    acc + transaction.amount.abs() 
                }
            }
    }
    
    // Для совместимости со старым кодом
    fun getIncomeByCategory(transactions: List<Transaction>): Map<String, Money> {
        return transactions
            .filter { transaction -> transaction.amount.amount.toFloat() > 0 }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.fold(Money.zero()) { acc, transaction -> acc + transaction.amount }
            }
    }
} 