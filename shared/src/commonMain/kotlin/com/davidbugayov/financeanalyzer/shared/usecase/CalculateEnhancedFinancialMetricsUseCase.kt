package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.FinancialHealthMetrics
import com.davidbugayov.financeanalyzer.shared.model.FinancialRecommendation
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.RecommendationCategory
import com.davidbugayov.financeanalyzer.shared.model.RecommendationPriority
import com.davidbugayov.financeanalyzer.shared.model.Transaction

/**
 * Композитный расчет продвинутых метрик и рекомендаций.
 * Локализация строк вынесена на уровень UI: здесь только коды и параметры.
 */
class CalculateEnhancedFinancialMetricsUseCase(
    private val calculateFinancialHealthScore: CalculateFinancialHealthScoreUseCase,
    private val calculateExpenseDisciplineIndex: CalculateExpenseDisciplineIndexUseCase,
    private val calculateRetirementForecast: CalculateRetirementForecastUseCase,
    private val calculatePeerComparison: CalculatePeerComparisonUseCase,
) {
    operator fun invoke(
        transactions: List<Transaction>,
        currentAge: Int = 30,
        retirementAge: Int = 65,
        currentSavings: Money = Money.zero(),
        desiredMonthlyPension: Money? = null,
    ): FinancialHealthMetrics {
        val (healthScore, breakdown) = calculateFinancialHealthScore(transactions)
        val discipline = calculateExpenseDisciplineIndex(transactions)
        val forecast = calculateRetirementForecast(transactions, currentAge, retirementAge, currentSavings, desiredMonthlyPension)
        val peers = calculatePeerComparison(transactions, healthScore)

        val recommendations = buildRecommendations(
            healthScore = healthScore,
            expenseDisciplineIndex = discipline,
            forecast = forecast,
            peerComparison = peers,
            transactions = transactions,
        )

        return FinancialHealthMetrics(
            financialHealthScore = healthScore,
            expenseDisciplineIndex = discipline,
            retirementForecast = forecast,
            peerComparison = peers,
            healthScoreBreakdown = breakdown,
            recommendations = recommendations,
        )
    }

    private fun buildRecommendations(
        healthScore: Double,
        expenseDisciplineIndex: Double,
        forecast: com.davidbugayov.financeanalyzer.shared.model.RetirementForecast,
        peerComparison: com.davidbugayov.financeanalyzer.shared.model.PeerComparison,
        transactions: List<Transaction>,
    ): List<FinancialRecommendation> {
        val items = mutableListOf<FinancialRecommendation>()

        if (healthScore < 50) {
            items += FinancialRecommendation(
                code = "recommendation_improve_financial_health",
                priority = RecommendationPriority.HIGH,
                category = RecommendationCategory.SAVINGS,
                params = mapOf("score" to healthScore.toInt().toString()),
            )
        }

        if (expenseDisciplineIndex < 60) {
            items += FinancialRecommendation(
                code = "recommendation_improve_expense_control",
                priority = RecommendationPriority.HIGH,
                category = RecommendationCategory.EXPENSES,
                params = mapOf("index" to expenseDisciplineIndex.toInt().toString()),
            )
        }

        if (forecast.retirementGoalProgress < 50) {
            items += FinancialRecommendation(
                code = "recommendation_increase_retirement_savings",
                priority = RecommendationPriority.MEDIUM,
                category = RecommendationCategory.RETIREMENT,
                params = mapOf(
                    "progress" to forecast.retirementGoalProgress.toInt().toString(),
                    "requiredMonthly" to forecast.requiredMonthlySavings.toPlainString(),
                ),
            )
        }

        if (peerComparison.savingsRateVsPeers < -5) {
            items += FinancialRecommendation(
                code = "recommendation_increase_savings_rate",
                priority = RecommendationPriority.MEDIUM,
                category = RecommendationCategory.SAVINGS,
                params = mapOf("gap" to (-peerComparison.savingsRateVsPeers).toInt().toString()),
            )
        }

        val incomeSources = transactions.filter { !it.isExpense }.map { it.source }.distinct().size
        if (incomeSources < 2) {
            items += FinancialRecommendation(
                code = "recommendation_diversify_income",
                priority = RecommendationPriority.LOW,
                category = RecommendationCategory.INCOME,
                params = mapOf("sources" to incomeSources.toString()),
            )
        }

        // Резерв: экстренный фонд (просчет на основе грубой оценки)
        val months = estimateEmergencyFundMonths(transactions)
        if (months < 3.0) {
            items += FinancialRecommendation(
                code = "recommendation_create_emergency_fund",
                priority = RecommendationPriority.HIGH,
                category = RecommendationCategory.EMERGENCY_FUND,
                params = mapOf("months" to months.toInt().toString()),
            )
        }

        return items.sortedWith(
            compareByDescending<FinancialRecommendation> {
                when (it.priority) {
                    RecommendationPriority.HIGH -> 3
                    RecommendationPriority.MEDIUM -> 2
                    RecommendationPriority.LOW -> 1
                }
            }.thenByDescending { it.code }
        )
    }

    private fun estimateEmergencyFundMonths(transactions: List<Transaction>): Double {
        val months = transactions.groupBy { "${it.date.year}-${it.date.month}" }
        if (months.isEmpty()) return 0.0
        val avgIncome = months.values.map { it.filter { !it.isExpense }.sumOf { it.amount.toMajorDouble() } }.average()
        val avgExpense = months.values.map { it.filter { it.isExpense }.sumOf { it.amount.toMajorDouble() } }.average()
        val monthlySavings = (avgIncome - avgExpense).coerceAtLeast(0.0)
        return if (avgExpense > 0.0) monthlySavings / avgExpense * 12.0 else 0.0
    }
}


