package com.davidbugayov.financeanalyzer.shared

import com.davidbugayov.financeanalyzer.shared.model.BalanceMetrics
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetCategoriesWithAmountUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateExpenseDisciplineIndexUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculatePeerComparisonUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.PredictFutureExpensesUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateRetirementForecastUseCase
import kotlinx.datetime.LocalDate

/**
 * Простой фасад KMP для вызова из iOS/Android.
 */
class SharedFacade {
    private val calculateBalanceMetrics = CalculateBalanceMetricsUseCase()
    private val calculateCategoryStats = CalculateCategoryStatsUseCase()
    private val getCategoriesWithAmount = GetCategoriesWithAmountUseCase()
    private val calculateExpenseDisciplineIndex = CalculateExpenseDisciplineIndexUseCase()
    private val calculatePeerComparison = CalculatePeerComparisonUseCase()
    private val predictFutureExpenses = PredictFutureExpensesUseCase()
    private val calculateRetirementForecast = CalculateRetirementForecastUseCase()

    /**
     * Считает метрики по списку транзакций.
     */
    fun calculateMetrics(
        transactions: List<Transaction>,
        currencyCode: String,
        start: LocalDate?,
        end: LocalDate?,
    ): BalanceMetrics {
        val currency = Currency.fromCode(currencyCode)
        return calculateBalanceMetrics(transactions, currency, start, end)
    }

    fun calculateCategoryStats(transactions: List<Transaction>): Triple<List<com.davidbugayov.financeanalyzer.shared.model.CategoryStats>, Money, Money> =
        calculateCategoryStats(transactions)

    fun getCategoriesWithAmount(transactions: List<Transaction>, isExpense: Boolean): List<com.davidbugayov.financeanalyzer.shared.model.CategoryWithAmount> =
        getCategoriesWithAmount(transactions, isExpense)

    fun expenseDisciplineIndex(transactions: List<Transaction>, periodMonths: Int = 6): Double =
        calculateExpenseDisciplineIndex(transactions, periodMonths)

    fun peerComparison(transactions: List<Transaction>, healthScore: Double): com.davidbugayov.financeanalyzer.shared.model.PeerComparison =
        calculatePeerComparison(transactions, healthScore)

    fun predictExpenses(transactions: List<Transaction>, months: Int = 1): Money =
        predictFutureExpenses(transactions, months)

    fun retirementForecast(
        transactions: List<Transaction>,
        currentAge: Int = 30,
        retirementAge: Int = 65,
        currentSavings: Money = Money.zero(),
        desiredMonthlyPension: Money? = null,
    ): com.davidbugayov.financeanalyzer.shared.model.RetirementForecast =
        calculateRetirementForecast(transactions, currentAge, retirementAge, currentSavings, desiredMonthlyPension)

    /**
     * Утилита создания суммы из double (для удобства Swift-клиента).
     */
    fun moneyFromDouble(value: Double, currencyCode: String): Money {
        val currency = Currency.fromCode(currencyCode)
        return Money.fromMajor(value, currency)
    }
}


