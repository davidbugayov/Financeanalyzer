package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsForPeriodUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.state.EnhancedFinanceChartEffect
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.state.EnhancedFinanceChartIntent
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.state.EnhancedFinanceChartState
import com.davidbugayov.financeanalyzer.presentation.util.UiUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.math.BigDecimal

class EnhancedFinanceChartViewModel : ViewModel(), KoinComponent {
    private val getTransactionsForPeriodUseCase: GetTransactionsForPeriodUseCase by inject()
    private val calculateBalanceMetricsUseCase: CalculateBalanceMetricsUseCase by inject()
    private val categoriesViewModel: CategoriesViewModel by inject()
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
                loadData()
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

                Timber.d("%s транзакций", "EnhancedFinanceChartViewModel: После фильтрации по дате осталось: " + filteredTransactions.size)
                Timber.d(
                    "[DEBUG] Все транзакции за период: %s",
                    filteredTransactions.map { "${'$'}{it.date}: ${'$'}{it.amount} (${if (it.isExpense) "расход" else "доход"}), категория: ${'$'}{it.category}" })

                val metrics = calculateBalanceMetricsUseCase(filteredTransactions, _state.value.startDate, _state.value.endDate)
                val income = metrics.income
                val expense = metrics.expense
                val savingsRate = metrics.savingsRate
                val averageDailyExpense = metrics.averageDailyExpense
                val monthsOfSavings = metrics.monthsOfSavings

                Timber.d("[DEBUG] Сумма доходов: $income, сумма расходов: $expense, баланс: ${income - expense}")

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
                    isIncome = true
                )
                val expenseLineChartData = createLineChartData(
                    transactions = filteredTransactions,
                    isIncome = false
                )
                // --- конец нового ---

                // --- Новое: подготовка данных для PieChart ---
                val showExpenses = _state.value.showExpenses
                val categoryData = if (showExpenses) expensesByCategory else incomeByCategory
                val pieChartData = preparePieChartData(categoryData, showExpenses)

                Timber.d(
                    "%s%s",
                    "EnhancedFinanceChartViewModel: Категорий расходов: " + expensesByCategory.size + ", доходов: ",
                    incomeByCategory.size
                )
                if (!showExpenses) {
                    Timber.d("[DEBUG] incomeByCategory: %s", incomeByCategory.map { it.key + ": " + it.value.amount })
                    Timber.d("[DEBUG] pieChartData (income): %s", pieChartData.map { it.name + ": " + it.money.amount + ", %: " + it.percentage })
                }

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
    ): List<UiCategory> {
        val filteredData = if (showExpenses) {
            data.filter { !it.value.isZero() }
        } else {
            // Для доходов фильтруем только положительные суммы
            data.filter { it.value.amount > BigDecimal.ZERO }
        }
        val categories = if (showExpenses) categoriesViewModel.expenseCategories.value else categoriesViewModel.incomeCategories.value
        val pieChartDataList = filteredData.entries.mapIndexed { index, entry ->
            val categoryName = entry.key
            val moneyValue = entry.value
            val category = categories.find { it.name == categoryName }
            val color = category?.color ?: Color.Gray
            UiCategory(
                id = index.toLong(),
                name = categoryName,
                isExpense = showExpenses,
                isCustom = category?.isCustom == true,
                count = 0,
                money = moneyValue,
                percentage = 0f,
                transactions = emptyList(),
                original = category?.original,
                color = color,
                icon = category?.icon
            )
        }
        val totalMoney = if (showExpenses) {
            pieChartDataList.sumOf { it.money.amount.toDouble() }
        } else {
            // Для доходов считаем сумму только по положительным значениям
            pieChartDataList.sumOf { it.money.amount.toDouble().coerceAtLeast(0.0) }
        }
        return if (pieChartDataList.isNotEmpty() && totalMoney != 0.0) {
            pieChartDataList.map { item ->
                val percent = if (showExpenses) {
                    (item.money.amount.toDouble() / totalMoney * 100.0).toFloat()
                } else {
                    (item.money.amount.toDouble().coerceAtLeast(0.0) / totalMoney * 100.0).toFloat()
                }
                item.copy(percentage = percent)
            }
        } else emptyList()
    }
} 