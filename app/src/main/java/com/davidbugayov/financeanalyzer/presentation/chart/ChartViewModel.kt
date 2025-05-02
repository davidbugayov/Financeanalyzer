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
            is ChartIntent.LoadTransactions -> {
                Timber.d("ChartViewModel: Получен intent LoadTransactions")
                loadTransactions()
            }
            is ChartIntent.ToggleExpenseView -> {
                Timber.d("ChartViewModel: Получен intent ToggleExpenseView: ${intent.showExpenses}")
                toggleExpenseView(intent.showExpenses)
            }
            is ChartIntent.SetDateRange -> {
                Timber.d("ChartViewModel: Получен intent SetDateRange: ${formatDate(intent.startDate)} - ${formatDate(intent.endDate)}")
                setDateRange(intent.startDate, intent.endDate)
            }
            is ChartIntent.SetPeriodType -> {
                Timber.d("ChartViewModel: Получен intent SetPeriodType: ${intent.periodType}")
                setPeriodType(intent.periodType)
            }
            is ChartIntent.TogglePeriodDialog -> {
                Timber.d("ChartViewModel: Получен intent TogglePeriodDialog: ${intent.show}")
                togglePeriodDialog(intent.show)
            }
            is ChartIntent.ToggleStartDatePicker -> {
                Timber.d("ChartViewModel: Получен intent ToggleStartDatePicker: ${intent.show}")
                toggleStartDatePicker(intent.show)
            }
            is ChartIntent.ToggleEndDatePicker -> {
                Timber.d("ChartViewModel: Получен intent ToggleEndDatePicker: ${intent.show}")
                toggleEndDatePicker(intent.show)
            }
            is ChartIntent.ToggleSavingsRateInfo -> {
                Timber.d("ChartViewModel: Получен intent ToggleSavingsRateInfo: ${intent.show}")
                toggleSavingsRateInfo(intent.show)
            }
        }
    }
    
    // Публичный метод для загрузки транзакций, используемый в MainScreen
    fun loadTransactions() {
        viewModelScope.launch {
            try {
                Timber.d("ChartViewModel: Начало загрузки транзакций для графиков")
                _state.update { it.copy(isLoading = true, error = null) }
                
                val transactions = getTransactionsUseCase.getAllTransactions()
                allTransactions = transactions
                
                Timber.d("ChartViewModel: Получено всего транзакций: ${transactions.size}")
                
                // Фильтруем транзакции по датам
                val filteredTransactions = allTransactions.filter { transaction ->
                    transaction.date >= _state.value.startDate && transaction.date <= _state.value.endDate
                }
                
                Timber.d("ChartViewModel: После фильтрации по дате (${formatDate(_state.value.startDate)} - ${formatDate(_state.value.endDate)}) осталось: ${filteredTransactions.size} транзакций")
                
                // Рассчитываем ежедневные расходы
                val dailyExpenses = calculateDailyExpenses(filteredTransactions)
                
                // Рассчитываем суммы доходов и расходов
                val (income, expense) = calculateIncomeAndExpense(filteredTransactions)
                
                Timber.d("ChartViewModel: Обновление состояния. Доход: ${income.amount}, Расход: ${expense.amount}, Кол-во транзакций: ${filteredTransactions.size}")
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        transactions = filteredTransactions,
                        dailyExpenses = dailyExpenses,
                        income = income,
                        expense = expense
                    )
                }
                Timber.d("ChartViewModel: Загрузка транзакций для графиков завершена успешно")
            } catch (e: Exception) {
                Timber.e(e, "ChartViewModel: Ошибка загрузки транзакций для графиков")
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
        
        // Обновляем даты в зависимости от типа периода, используя общую логику
        val currentState = _state.value
        val (start, end) = DateUtils.updatePeriodDates(
            periodType = periodType,
            currentStartDate = currentState.startDate,
            currentEndDate = currentState.endDate
        )
        _state.update { it.copy(startDate = start, endDate = end) }
        
        loadTransactions()
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
} 
