package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsForPeriodUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.state.EnhancedFinanceChartIntent
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.state.EnhancedFinanceChartState
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.state.EnhancedFinanceChartEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.Date
import com.davidbugayov.financeanalyzer.presentation.util.UiUtils
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.PieChartItemData
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.utils.PieChartUtils
import androidx.compose.ui.graphics.Color

class EnhancedFinanceChartViewModel : ViewModel(), KoinComponent {
    private val getTransactionsUseCase: GetTransactionsUseCase by inject()
    private val getTransactionsForPeriodUseCase: GetTransactionsForPeriodUseCase by inject()
    private val calculateBalanceMetricsUseCase: CalculateBalanceMetricsUseCase by inject()
    private val _state = MutableStateFlow(EnhancedFinanceChartState())
    val state: StateFlow<EnhancedFinanceChartState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EnhancedFinanceChartEffect>()
    val effect: SharedFlow<EnhancedFinanceChartEffect> = _effect.asSharedFlow()

    private var allTransactions: List<Transaction> = emptyList()

    fun handleIntent(intent: EnhancedFinanceChartIntent) {
        when (intent) {
            is EnhancedFinanceChartIntent.LoadData -> {
                loadData()
            }
            is EnhancedFinanceChartIntent.ChangePeriod -> {
                _state.update {
                    it.copy(
                        periodType = intent.periodType,
                        startDate = intent.startDate,
                        endDate = intent.endDate
                    )
                }
                updatePeriodText()
                loadData()
            }
            is EnhancedFinanceChartIntent.SelectCategory -> {
                _state.update { it.copy(selectedCategory = intent.category) }
            }
            is EnhancedFinanceChartIntent.ChangeChartTab -> {
                _state.update { it.copy(selectedTab = intent.tabIndex) }
            }
            is EnhancedFinanceChartIntent.ChangeLineChartMode -> {
                _state.update { it.copy(lineChartMode = intent.mode) }
            }
            is EnhancedFinanceChartIntent.ToggleExpenseView -> {
                _state.update { it.copy(showExpenses = intent.showExpenses) }
            }
            is EnhancedFinanceChartIntent.AddTransactionClicked -> {
                viewModelScope.launch {
                    _effect.emit(EnhancedFinanceChartEffect.NavigateToAddTransaction)
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                Timber.d("EnhancedFinanceChartViewModel: Начало загрузки транзакций для графиков")
                _state.update { it.copy(isLoading = true, error = null) }

                val filteredTransactions = getTransactionsForPeriodUseCase(_state.value.startDate, _state.value.endDate)
                allTransactions = filteredTransactions

                Timber.d("EnhancedFinanceChartViewModel: После фильтрации по дате осталось: "+filteredTransactions.size+" транзакций")

                val metrics = calculateBalanceMetricsUseCase(filteredTransactions, _state.value.startDate, _state.value.endDate)
                val income = metrics.income
                val expense = metrics.expense
                val savingsRate = metrics.savingsRate
                val averageDailyExpense = metrics.averageDailyExpense
                val monthsOfSavings = metrics.monthsOfSavings

                // Агрегируем по категориям
                val expensesByCategory = filteredTransactions
                    .filter { it.isExpense }
                    .groupBy { it.category.ifBlank { "Без категории" } }
                    .mapValues { (_, transactions) ->
                        transactions.map { it.amount.abs() }.reduceOrNull { acc, money -> acc + money } ?: Money.zero()
                    }
                val incomeByCategory = filteredTransactions
                    .filter { !it.isExpense }
                    .groupBy { it.category.ifBlank { "Без категории" } }
                    .mapValues { (_, transactions) ->
                        transactions.map { it.amount }.reduceOrNull { acc, money -> acc + money } ?: Money.zero()
                    }

                // --- Новое: подготовка данных для линейного графика ---
                val incomeLineChartData = createLineChartData(
                    transactions = filteredTransactions,
                    isIncome = true,
                    startDate = _state.value.startDate,
                    endDate = _state.value.endDate
                )
                val expenseLineChartData = createLineChartData(
                    transactions = filteredTransactions,
                    isIncome = false,
                    startDate = _state.value.startDate,
                    endDate = _state.value.endDate
                )
                // --- конец нового ---

                // --- Новое: подготовка данных для PieChart ---
                val showExpenses = _state.value.showExpenses
                val categoryData = if (showExpenses) expensesByCategory else incomeByCategory
                val pieChartData = preparePieChartData(categoryData, showExpenses)

                Timber.d("EnhancedFinanceChartViewModel: Категорий расходов: "+expensesByCategory.size+", доходов: "+incomeByCategory.size)

                _state.update {
                    it.copy(
                        isLoading = false,
                        transactions = filteredTransactions,
                        income = income,
                        expense = expense,
                        expensesByCategory = expensesByCategory,
                        incomeByCategory = incomeByCategory,
                        incomeLineChartData = incomeLineChartData,
                        expenseLineChartData = expenseLineChartData,
                        error = null,
                        periodText = UiUtils.formatPeriod(
                            it.periodType,
                            it.startDate,
                            it.endDate
                        ),
                        savingsRate = savingsRate,
                        averageDailyExpense = averageDailyExpense,
                        monthsOfSavings = monthsOfSavings,
                        pieChartData = pieChartData
                    )
                }
                Timber.d("EnhancedFinanceChartViewModel: Загрузка транзакций для графиков завершена успешно")
            } catch (e: Exception) {
                Timber.e(e, "EnhancedFinanceChartViewModel: Ошибка загрузки транзакций для графиков")
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effect.emit(EnhancedFinanceChartEffect.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    private fun createLineChartData(
        transactions: List<Transaction>,
        isIncome: Boolean,
        startDate: Date,
        endDate: Date
    ): List<com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.LineChartPoint> {
        // Фильтруем транзакции по типу (доход/расход)
        val filteredTransactions = transactions.filter {
            (isIncome && !it.isExpense) || (!isIncome && it.isExpense)
        }
        if (filteredTransactions.isEmpty()) return emptyList()
        // Группируем по дате (без времени)
        val aggregatedData = filteredTransactions
            .groupBy {
                val calendar = java.util.Calendar.getInstance()
                calendar.time = it.date
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                calendar.time
            }
            .mapValues { (_, transactions) ->
                transactions.fold(Money.zero()) { acc, transaction ->
                    val value = if (isIncome) transaction.amount else transaction.amount.abs()
                    acc + value
                }
            }
        // Сортируем по дате и формируем точки
        return aggregatedData.entries
            .sortedBy { it.key }
            .map { (date, value) ->
                com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.LineChartPoint(
                    date = date,
                    value = value
                )
            }
    }

    private fun updatePeriodText() {
        _state.update {
            it.copy(
                periodText = UiUtils.formatPeriod(
                    it.periodType,
                    it.startDate,
                    it.endDate
                )
            )
        }
    }

    private fun preparePieChartData(
        data: Map<String, Money>,
        showExpenses: Boolean
    ): List<PieChartItemData> {
        val filteredData = data.filter { !it.value.isZero() }
        val colors = PieChartUtils.getCategoryColors(filteredData.size, !showExpenses)
        val pieChartDataList = filteredData.entries.mapIndexed { index, entry ->
            val categoryName = entry.key
            val moneyValue = entry.value
            val category = if (!showExpenses) {
                Category.income(name = categoryName)
            } else {
                Category.expense(name = categoryName)
            }
            val color = colors.getOrElse(index) {
                if (!showExpenses) Color(0xFF66BB6A + (index * 1000))
                else Color(0xFFEF5350 + (index * 1000))
            }
            PieChartItemData(
                id = index.toString(),
                name = categoryName,
                money = moneyValue,
                percentage = 0f, // будет рассчитано ниже
                color = color,
                category = category,
                transactions = emptyList()
            )
        }
        val totalMoney = pieChartDataList.sumOf { it.money.amount.toDouble() }
        return if (pieChartDataList.isNotEmpty() && totalMoney != 0.0) {
            pieChartDataList.map { item ->
                val percentage = (item.money.amount.toDouble() / totalMoney * 100.0).toFloat()
                item.copy(percentage = percentage)
            }
        } else emptyList()
    }
} 