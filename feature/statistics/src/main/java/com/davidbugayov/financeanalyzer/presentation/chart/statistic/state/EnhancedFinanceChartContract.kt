package com.davidbugayov.financeanalyzer.presentation.chart.statistic.state
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.domain.model.FinancialRecommendation
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.navigation.model.PeriodType
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.model.LineChartDisplayMode
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.model.LineChartPoint
import java.util.Calendar
import java.util.Date
import kotlin.collections.emptyList

sealed class EnhancedFinanceChartIntent {
    object LoadData : EnhancedFinanceChartIntent()

    data class ChangePeriod(
        val periodType: PeriodType,
        val startDate: Date,
        val endDate: Date,
    ) : EnhancedFinanceChartIntent()

    data class SelectCategory(
        val category: Category?,
    ) : EnhancedFinanceChartIntent()

    data class ChangeChartTab(
        val tabIndex: Int,
    ) : EnhancedFinanceChartIntent()

    data class ChangeLineChartMode(
        val mode: LineChartDisplayMode,
    ) : EnhancedFinanceChartIntent()

    data class ToggleExpenseView(
        val showExpenses: Boolean,
    ) : EnhancedFinanceChartIntent()

    object AddTransactionClicked : EnhancedFinanceChartIntent()
    // Добавляй другие интенты по мере необходимости
}

sealed class EnhancedFinanceChartEffect {
    data class ShowError(
        val message: String,
    ) : EnhancedFinanceChartEffect()

    object ScrollToSummary : EnhancedFinanceChartEffect()

    object NavigateToAddTransaction : EnhancedFinanceChartEffect()
    // Добавляй другие эффекты по необходимости
}

data class EnhancedFinanceChartState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val transactions: List<Transaction> = emptyList(),
    val income: Money? = null,
    val expense: Money? = null,
    val startDate: Date =
        Calendar
            .getInstance()
            .apply {
                add(Calendar.MONTH, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time,
    val endDate: Date =
        Calendar
            .getInstance()
            .apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time,
    val periodType: PeriodType = PeriodType.MONTH,
    val selectedCategory: Category? = null,
    val selectedTab: Int = 0,
    val lineChartMode: LineChartDisplayMode = LineChartDisplayMode.BOTH,
    val showExpenses: Boolean = true,
    val expensesByCategory: Map<String, Money> = emptyMap(),
    val incomeByCategory: Map<String, Money> = emptyMap(),
    val incomeLineChartData: List<LineChartPoint> = emptyList(),
    val expenseLineChartData: List<LineChartPoint> = emptyList(),
    val periodText: String = "",
    val savingsRate: Double = 0.0,
    val averageDailyExpense: Money = Money.zero(),
    val monthsOfSavings: Double = 0.0,
    val pieChartData: List<UiCategory> = emptyList(),
    val recommendations: List<FinancialRecommendation> = emptyList(),
    // Добавляй другие поля по необходимости
)
