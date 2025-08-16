package com.davidbugayov.financeanalyzer.presentation.chart.statistic.viewmodel
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateEnhancedFinancialMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsForPeriodUseCase
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.navigation.Screen
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.state.EnhancedFinanceChartEffect
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.state.EnhancedFinanceChartIntent
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.state.EnhancedFinanceChartState
import com.davidbugayov.financeanalyzer.shared.SharedFacade
import com.davidbugayov.financeanalyzer.ui.theme.DefaultCategoryColor
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseChartPalette
import com.davidbugayov.financeanalyzer.ui.theme.IncomeChartPalette
import com.davidbugayov.financeanalyzer.utils.CurrencyProvider
import com.davidbugayov.financeanalyzer.utils.kmp.toShared
import com.davidbugayov.financeanalyzer.utils.kmp.toLocalDateKmp
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EnhancedFinanceChartViewModel :
    ViewModel(),
    KoinComponent {
    private val getTransactionsForPeriodUseCase: GetTransactionsForPeriodUseCase by inject()
    private val calculateBalanceMetricsUseCase: CalculateBalanceMetricsUseCase by inject()
    private val calculateEnhancedFinancialMetricsUseCase: CalculateEnhancedFinancialMetricsUseCase by inject()
    private val sharedFacade: SharedFacade by inject()
    private val categoriesViewModel: CategoriesViewModel by inject()
    private val navigationManager: NavigationManager by inject()
    private val _state = MutableStateFlow(EnhancedFinanceChartState())
    val state: StateFlow<EnhancedFinanceChartState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EnhancedFinanceChartEffect>()
    val effect: SharedFlow<EnhancedFinanceChartEffect> = _effect.asSharedFlow()

    private var allTransactions: List<Transaction> = emptyList()

    init {
        // Подписываемся на изменения валюты
        viewModelScope.launch {
            CurrencyProvider.getCurrencyFlow().collect { newCurrency ->
                // Пересчитываем данные с новой валютой
                if (allTransactions.isNotEmpty()) {
                    loadData()
                }
            }
        }
    }

    // Форматирование периода перенесено в UI-слой; в состоянии храним короткую форму
    private fun formatPeriodCompact(
        periodType: com.davidbugayov.financeanalyzer.navigation.model.PeriodType,
        startDate: java.util.Date,
        endDate: java.util.Date,
    ): String {
        val df = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.forLanguageTag("ru"))
        return when (periodType) {
            com.davidbugayov.financeanalyzer.navigation.model.PeriodType.ALL -> ""
            else -> df.format(startDate) + " - " + df.format(endDate)
        }
    }

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
                        endDate = intent.endDate,
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
            _state.update { it.copy(isLoading = true, error = null) }

            val flow =
                sharedFacade.transactionsForPeriodFlow(
                    _state.value.startDate.toLocalDateKmp(),
                    _state.value.endDate.toLocalDateKmp(),
                )
            val filteredTransactions = (flow?.first() ?: emptyList())
            allTransactions = filteredTransactions

            val currentCurrency = CurrencyProvider.getCurrency()
            val metrics =
                calculateBalanceMetricsUseCase(
                    filteredTransactions.map { it.toDomain() },
                    currentCurrency,
                    _state.value.startDate,
                    _state.value.endDate,
                )
            val income = metrics.income.toShared()
            val expense = metrics.expense.toShared()
            val savingsRate = metrics.savingsRate
            val averageDailyExpense = metrics.averageDailyExpense
            val monthsOfSavings = metrics.monthsOfSavings

            // Агрегируем по категориям
            val expensesByCategory =
                filteredTransactions
                    .filter { it.isExpense }
                    .groupBy { it.category.ifBlank { "Без категории" } }
                    .mapValues { (_, transactions) ->
                        transactions.fold(Money(0L, currentCurrency)) { acc, transaction ->
                            // Приводим каждую транзакцию к текущей валюте
                            val convertedAmount = Money(transaction.amount.minor, currentCurrency)
                            acc + convertedAmount.abs()
                        }
                    }
            val incomeByCategory =
                filteredTransactions
                    .filter { !it.isExpense }
                    .groupBy { it.category.ifBlank { "Без категории" } }
                    .mapValues { (_, transactions) ->
                        transactions.fold(Money(0L, currentCurrency)) { acc, transaction ->
                            // Приводим каждую транзакцию к текущей валюте
                            val convertedAmount = Money(transaction.amount.minor, currentCurrency)
                            acc + convertedAmount
                        }
                    }

            // --- Новое: подготовка данных для линейного графика ---
            val incomeLineChartData =
                createLineChartData(
                    transactions = filteredTransactions,
                    isIncome = true,
                )
            val expenseLineChartData =
                createLineChartData(
                    transactions = filteredTransactions,
                    isIncome = false,
                )
            // --- конец нового ---

            // --- Новое: подготовка данных для PieChart ---
            val showExpenses = _state.value.showExpenses
            val categoryData = if (showExpenses) expensesByCategory else incomeByCategory
            val pieChartData = preparePieChartData(categoryData, showExpenses)

            // --- Новое: получаем рекомендации ---
            val financialMetrics = calculateEnhancedFinancialMetricsUseCase.invoke(filteredTransactions)
            val recommendations = financialMetrics.recommendations

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
                    periodText = formatPeriodCompact(it.periodType, it.startDate, it.endDate),
                    savingsRate = savingsRate,
                    averageDailyExpense = averageDailyExpense,
                    monthsOfSavings = monthsOfSavings,
                    pieChartData = pieChartData,
                    recommendations = recommendations,
                )
            }
        }
    }

    private fun createLineChartData(
        transactions: List<Transaction>,
        isIncome: Boolean,
    ): List<com.davidbugayov.financeanalyzer.presentation.chart.statistic.model.LineChartPoint> {
        // Фильтруем транзакции по типу (доход/расход)
        val filteredTransactions =
            transactions.filter {
                (isIncome && !it.isExpense) || (!isIncome && it.isExpense)
            }
        if (filteredTransactions.isEmpty()) return emptyList()

        val currentCurrency = CurrencyProvider.getCurrency().toShared()

        // Группируем по дате (без времени)
        val aggregatedData =
            filteredTransactions
                .groupBy {
                    it.date
                }.mapValues { (_, transactions) ->
                    transactions.fold(Money(0L, currentCurrency)) { acc, transaction ->
                        // Приводим транзакцию к текущей валюте
                        val convertedAmount = Money(transaction.amount.minor, currentCurrency)
                        val value = if (isIncome) convertedAmount else convertedAmount.abs()
                        acc + value
                    }
                }
        // Сортируем по дате и формируем точки
        return aggregatedData.entries
            .sortedBy { it.key }
            .map { (date, value) ->
                com.davidbugayov.financeanalyzer.presentation.chart.statistic.model.LineChartPoint(
                    date = date,
                    value = value,
                )
            }
    }

    private fun updatePeriodText() {
        _state.update { st ->
            st.copy(periodText = formatPeriodCompact(st.periodType, st.startDate, st.endDate))
        }
    }

    private fun preparePieChartData(
        data: Map<String, com.davidbugayov.financeanalyzer.shared.model.Money>,
        showExpenses: Boolean,
    ): List<UiCategory> {
        val filteredData =
            if (showExpenses) {
                data.filter { it.value.minor != 0L }
            } else {
                // Для доходов фильтруем только положительные суммы
                data.filter { it.value.minor > 0 }
            }
        val categories = if (showExpenses) categoriesViewModel.expenseCategories.value else categoriesViewModel.incomeCategories.value
        val palette = if (showExpenses) ExpenseChartPalette else IncomeChartPalette
        val pieChartDataList =
            filteredData.entries.mapIndexed { index, entry ->
                val categoryName = entry.key
                val moneyValue = entry.value
                val category = categories.find { it.name == categoryName }
                var color = category?.color ?: Color.Gray
                if (color == Color.Gray || color == DefaultCategoryColor) {
                    color = palette[index % palette.size]
                }
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
                    icon = category?.icon,
                )
            }
        val totalMoney =
            if (showExpenses) {
                pieChartDataList.fold(0L) { acc, item -> acc + item.money.minor }
            } else {
                // Для доходов считаем сумму только по положительным значениям
                pieChartDataList.fold(0L) { acc, item ->
                    acc + maxOf(item.money.minor, 0L)
                }
            }
        return if (pieChartDataList.isNotEmpty() && totalMoney != 0L) {
            pieChartDataList.map { item ->
                val percent =
                    if (showExpenses) {
                        (item.money.minor.toFloat() / totalMoney.toFloat()) * 100f
                    } else {
                        (maxOf(item.money.minor, 0L).toFloat() / totalMoney.toFloat()) * 100f
                    }
                item.copy(percentage = percent)
            }
        } else {
            emptyList()
        }
    }

    /**
     * Переход на экран подробной финансовой статистики
     */
    fun navigateToDetailedStatistics() {
        val currentState = _state.value
        // Используем новый экран DetailedFinancialStatistics вместо параметра DETAILED
        navigationManager.navigate(
            NavigationManager.Command.Navigate(
                Screen.DetailedFinancialStatistics.createRoute(
                    currentState.startDate.time,
                    currentState.endDate.time,
                ),
            ),
        )
    }
}
