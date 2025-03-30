package com.davidbugayov.financeanalyzer.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.DailyExpense
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartIntent
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartScreenState
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                Timber.d("Explicitly resetting date filter")
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
                // Сначала запрашиваем только первую страницу (макс. 100 транзакций) для быстрого отображения
                val initialTransactions = withContext(Dispatchers.IO) {
                    try {
                        repository.getTransactionsByDateRangePaginated(
                            _state.value.startDate,
                            _state.value.endDate,
                            PAGE_SIZE,
                            0
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при загрузке первой страницы транзакций")
                        emptyList()
                    }
                }
                
                // Обновляем состояние с первой порцией данных для быстрого отображения графиков
                if (initialTransactions.isNotEmpty()) {
                    Timber.d("Быстрая загрузка: получено ${initialTransactions.size} транзакций")
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            error = null,
                            transactions = initialTransactions,
                            dailyExpenses = calculateDailyExpenses(initialTransactions)
                        )
                    }
                }
                
                // Затем запускаем полную загрузку всех транзакций в фоновом режиме
                withContext(Dispatchers.IO) {
                    try {
                        val totalTransactions = repository.getTransactionsByDateRangePaginated(
                            _state.value.startDate,
                            _state.value.endDate,
                            Int.MAX_VALUE,
                            0
                        )
                        
                        if (totalTransactions.size > initialTransactions.size) {
                            Timber.d("Полная загрузка: получено ${totalTransactions.size} транзакций")
                            withContext(Dispatchers.Main) {
                                _state.update { currentState ->
                                    currentState.copy(
                                        isLoading = false,
                                        error = null,
                                        transactions = totalTransactions,
                                        dailyExpenses = calculateDailyExpenses(totalTransactions)
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при полной загрузке транзакций")
                        // Не обновляем состояние ошибки, если у нас уже есть начальные данные
                        if (initialTransactions.isEmpty()) {
                            withContext(Dispatchers.Main) {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        error = e.message
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Критическая ошибка при загрузке транзакций")
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
        Timber.d("Updating start date to: ${formatDate(date)}")
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
        Timber.d("Updating end date to: ${formatDate(date)}")
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
        Timber.d("Updating date range to: ${formatDate(startDate)} - ${formatDate(endDate)}")
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
        val expensesByCategory = transactions
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions
                    .map { it.amount }
                    .reduceOrNull { acc, amount -> acc + amount } ?: 0.0
            }
            
        return expensesByCategory.mapValues { (_, amount) -> Money(amount) }
    }

    fun getIncomeByCategory(transactions: List<Transaction>): Map<String, Money> {
        val incomeByCategory = transactions
            .filter { !it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions
                    .map { it.amount }
                    .reduceOrNull { acc, amount -> acc + amount } ?: 0.0
            }
            
        return incomeByCategory.mapValues { (_, amount) -> Money(amount) }
    }

    private fun calculateDailyExpenses(transactions: List<Transaction>): List<DailyExpense> {
        // Используем IO диспатчер для тяжелых вычислений
        return try {
            val filteredTransactions = transactions.filter { transaction ->
                transaction.date.time >= _state.value.startDate.time &&
                        transaction.date.time <= _state.value.endDate.time
            }

            Timber.d("Отфильтровано ${filteredTransactions.size} транзакций для графиков")

            filteredTransactions
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
                    val expenseAmount = transactionsForDate
                        .filter { it.isExpense }
                        .map { it.amount }
                        .reduceOrNull { acc, amount -> acc + amount } ?: 0.0

                    DailyExpense(
                        date = date,
                        amount = expenseAmount
                    )
                }
                .filter { it.amount.amount.toDouble() > 0 }
                .sortedBy { it.date }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при расчете ежедневных расходов")
            emptyList()
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
        Timber.d("onCleared: Resetting date filter")
        resetDateFilter()
    }

    companion object {
        private const val PAGE_SIZE = 100 // Размер страницы для начальной загрузки
    }
} 
