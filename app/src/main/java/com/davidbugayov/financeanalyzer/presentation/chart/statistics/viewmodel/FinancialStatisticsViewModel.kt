package com.davidbugayov.financeanalyzer.presentation.chart.statistics.viewmodel

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsForPeriodUseCase
import com.davidbugayov.financeanalyzer.presentation.chart.statistics.state.FinancialStatisticsContract
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.Date

// Метрики для UI
data class FinancialMetrics(
    val totalTransactions: Int = 0,
    val incomeTransactionsCount: Int = 0,
    val expenseTransactionsCount: Int = 0,
    val averageIncomePerTransaction: Money = Money.zero(),
    val averageExpensePerTransaction: Money = Money.zero(),
    val maxIncome: Money = Money.zero(),
    val maxExpense: Money = Money.zero(),
    val savingsRate: Double = 0.0,
    val monthsOfSavings: Double = 0.0,
    val averageDailyExpense: Money = Money.zero(),
    val averageMonthlyExpense: Money = Money.zero(),
    val topExpenseCategory: String = "",
    val topExpenseCategories: List<Pair<String, Money>> = emptyList(),
    val topIncomeCategory: String = "",
    val mostFrequentExpenseDay: String = "",
)

class FinancialStatisticsViewModel(
    private val resources: Resources,
    private val getTransactionsForPeriodUseCase: GetTransactionsForPeriodUseCase,
    startDate: Long,
    endDate: Long,
    private val calculateBalanceMetricsUseCase: CalculateBalanceMetricsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(FinancialStatisticsContract.State())
    val state: StateFlow<FinancialStatisticsContract.State> = _state

    private val _effect = MutableSharedFlow<FinancialStatisticsContract.Effect>()
    val effect: SharedFlow<FinancialStatisticsContract.Effect> = _effect

    // Метрики и рекомендации для UI
    private val _metrics = MutableStateFlow(FinancialMetrics())
    val metrics: StateFlow<FinancialMetrics> = _metrics

    private val periodStartDate = Date(startDate)
    private val periodEndDate = Date(endDate)

    fun handleIntent(intent: FinancialStatisticsContract.Intent) {
        when (intent) {
            is FinancialStatisticsContract.Intent.LoadData -> loadData()
            // Добавь обработку других интентов
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val transactions = getTransactionsForPeriodUseCase(periodStartDate, periodEndDate)
            val metrics = calculateBalanceMetricsUseCase(
                transactions
            )
            val income = metrics.income
            val expense = metrics.expense
            // Используем константные значения для метрик, которые больше не возвращаются из use case
            val savingsRate = 0.0
            val monthsOfSavings = 0.0
            val averageDailyExpense = Money.zero()
            val averageMonthlyExpense = averageDailyExpense * 30.toBigDecimal()

            // Остальные метрики (топ категории и т.д.) считаем как раньше
            val incomeTransactions = transactions.filter { !it.isExpense }
            val expenseTransactions = transactions.filter { it.isExpense }
            val incomeCount = incomeTransactions.size
            val expenseCount = expenseTransactions.size
            val avgIncomePerTransaction = if (incomeCount > 0) {
                income / incomeCount.toBigDecimal()
            } else {
                Money.zero()
            }
            val avgExpensePerTransaction = if (expenseCount > 0) {
                expense / expenseCount.toBigDecimal()
            } else {
                Money.zero()
            }
            val maxIncome = incomeTransactions.maxByOrNull { it.amount.amount }?.amount ?: Money.zero()
            val maxExpense = expenseTransactions.maxByOrNull { it.amount.abs().amount }?.amount ?: Money.zero()
            val noCategory = resources.getString(R.string.no_category)
            val expensesByCategory = expenseTransactions
                .groupBy { it.category.ifBlank { noCategory } }
                .mapValues { (_, transactions) ->
                    transactions.fold(Money.zero()) { acc, transaction -> acc + transaction.amount.abs() }
                }
            val topExpenseCategories = expensesByCategory.entries
                .sortedByDescending { it.value.amount }
                .take(3)
                .map { it.key to it.value }
            val topExpenseCategory = topExpenseCategories.firstOrNull()?.first ?: ""
            val incomesByCategory = incomeTransactions
                .groupBy { it.category.ifBlank { noCategory } }
                .mapValues { (_, transactions) ->
                    transactions.fold(Money.zero()) { acc, transaction -> acc + transaction.amount }
                }
            val topIncomeCategory = incomesByCategory.entries
                .maxByOrNull { it.value.amount }?.key ?: ""
            val mostFrequentExpenseDay = expenseTransactions
                .groupBy { transaction ->
                    val calendar = Calendar.getInstance().apply { time = transaction.date }
                    calendar.get(Calendar.DAY_OF_WEEK)
                }
                .maxByOrNull { it.value.size }
                ?.key
                ?.let { dayOfWeek ->
                    when (dayOfWeek) {
                        Calendar.MONDAY -> resources.getString(R.string.monday)
                        Calendar.TUESDAY -> resources.getString(R.string.tuesday)
                        Calendar.WEDNESDAY -> resources.getString(R.string.wednesday)
                        Calendar.THURSDAY -> resources.getString(R.string.thursday)
                        Calendar.FRIDAY -> resources.getString(R.string.friday)
                        Calendar.SATURDAY -> resources.getString(R.string.saturday)
                        Calendar.SUNDAY -> resources.getString(R.string.sunday)
                        else -> ""
                    }
                } ?: ""

            _metrics.value = FinancialMetrics(
                totalTransactions = transactions.size,
                incomeTransactionsCount = incomeCount,
                expenseTransactionsCount = expenseCount,
                averageIncomePerTransaction = avgIncomePerTransaction,
                averageExpensePerTransaction = avgExpensePerTransaction,
                maxIncome = maxIncome,
                maxExpense = maxExpense,
                savingsRate = savingsRate,
                monthsOfSavings = monthsOfSavings,
                averageDailyExpense = averageDailyExpense,
                averageMonthlyExpense = averageMonthlyExpense,
                topExpenseCategory = topExpenseCategory,
                topExpenseCategories = topExpenseCategories,
                topIncomeCategory = topIncomeCategory,
                mostFrequentExpenseDay = mostFrequentExpenseDay,
            )
            Timber.d(
                "Transactions for stats: %s",
                transactions.joinToString {
                    String.format(
                        "%s %s %s %s",
                        it.date,
                        it.amount,
                        it.category,
                        if (it.isExpense) "EXP" else "INC",
                    )
                },
            )
            _state.value = _state.value.copy(
                transactions = transactions,
                income = income,
                expense = expense,
                period = formatPeriod(periodStartDate, periodEndDate),
                isLoading = false,
            )
        }
    }

    private fun formatPeriod(start: Date, end: Date): String {
        val format = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
        return "${format.format(start)} - ${format.format(end)}"
    }
}
