package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.FinancialHealthMetrics
import com.davidbugayov.financeanalyzer.shared.model.FinancialRecommendation
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.RecommendationCategory
import com.davidbugayov.financeanalyzer.shared.model.RecommendationPriority
import java.math.BigDecimal
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlin.math.max

/**
 * Главный UseCase для расчета продвинутых метрик финансового здоровья.
 * Объединяет все компоненты: коэффициент здоровья, индекс дисциплины, 
 * прогноз пенсии и сравнение с пирами.
 */
class CalculateEnhancedFinancialMetricsUseCase(
    private val calculateFinancialHealthScore: CalculateFinancialHealthScoreUseCase,
    private val calculateExpenseDisciplineIndex: CalculateExpenseDisciplineIndexUseCase,
    private val calculateRetirementForecast: CalculateRetirementForecastUseCase,
    private val calculatePeerComparison: CalculatePeerComparisonUseCase,
) {
    
    suspend operator fun invoke(
        transactions: List<Transaction>,
        currentAge: Int = 30,
        retirementAge: Int = 65,
        currentSavings: Money = Money.zero(),
        desiredMonthlyPension: Money? = null,
    ): FinancialHealthMetrics {
        
        // Рассчитываем коэффициент финансового здоровья
        val (healthScore, healthBreakdown) = calculateFinancialHealthScore(transactions)
        
        // Рассчитываем индекс расходной дисциплины
        val expenseDisciplineIndex = calculateExpenseDisciplineIndex(transactions)
        
        // Рассчитываем прогноз пенсионных накоплений
        val retirementForecast = calculateRetirementForecast(
            transactions = transactions,
            targetRetirementAge = retirementAge,
            currentAge = currentAge
        )
        
        // Рассчитываем сравнение с пирами
        val peerComparison = calculatePeerComparison(transactions, healthScore)
        
        // Генерируем рекомендации
        val recommendations = generateRecommendations(
            healthScore = healthScore,
            expenseDisciplineIndex = expenseDisciplineIndex,
            retirementForecast = retirementForecast,
            peerComparison = peerComparison,
            transactions = transactions
        )
        
        return FinancialHealthMetrics(
            financialHealthScore = healthScore,
            expenseDisciplineIndex = expenseDisciplineIndex,
            retirementForecast = retirementForecast,
            peerComparison = peerComparison,
            healthScoreBreakdown = healthBreakdown,
            recommendations = recommendations
        )
    }

    /**
     * Генерирует персонализированные рекомендации по улучшению финансового здоровья
     */
    private suspend fun generateRecommendations(
        healthScore: Double,
        expenseDisciplineIndex: Double,
        retirementForecast: com.davidbugayov.financeanalyzer.shared.model.RetirementForecast,
        peerComparison: com.davidbugayov.financeanalyzer.shared.model.PeerComparison,
        transactions: List<Transaction>
    ): List<FinancialRecommendation> {
        
        val recommendations = mutableListOf<FinancialRecommendation>()
        
        // Рекомендации по коэффициенту финансового здоровья
        if (healthScore < 50) {
            recommendations.add(
                FinancialRecommendation(
                    code = "recommendation_improve_financial_health",
                    priority = RecommendationPriority.HIGH,
                    category = RecommendationCategory.SAVINGS,
                    params = mapOf("score" to healthScore.toInt().toString())
                )
            )
        } else if (healthScore < 70) {
            recommendations.add(
                FinancialRecommendation(
                    code = "recommendation_maintain_financial_health",
                    priority = RecommendationPriority.MEDIUM,
                    category = RecommendationCategory.SAVINGS,
                    params = mapOf("score" to healthScore.toInt().toString())
                )
            )
        }
        
        // Рекомендации по индексу расходной дисциплины
        if (expenseDisciplineIndex < 60) {
            recommendations.add(
                FinancialRecommendation(
                    code = "recommendation_improve_expense_control",
                    priority = RecommendationPriority.HIGH,
                    category = RecommendationCategory.EXPENSES,
                    params = mapOf("index" to expenseDisciplineIndex.toInt().toString())
                )
            )
        } else if (expenseDisciplineIndex < 80) {
            recommendations.add(
                FinancialRecommendation(
                    code = "recommendation_optimize_expenses",
                    priority = RecommendationPriority.MEDIUM,
                    category = RecommendationCategory.EXPENSES,
                    params = mapOf("index" to expenseDisciplineIndex.toInt().toString())
                )
            )
        }
        
        // Рекомендации по пенсионным накоплениям
        if (retirementForecast.savingsGap.amount > BigDecimal.ZERO) {
            recommendations.add(
                FinancialRecommendation(
                    code = "recommendation_increase_retirement_savings",
                    priority = RecommendationPriority.HIGH,
                    category = RecommendationCategory.RETIREMENT,
                    params = mapOf(
                        "gap" to retirementForecast.savingsGap.toPlainString(),
                        "requiredMonthly" to retirementForecast.monthlySavingsNeeded.toPlainString()
                    )
                )
            )
        } else if (retirementForecast.riskLevel == "HIGH") {
            recommendations.add(
                FinancialRecommendation(
                    code = "recommendation_optimize_retirement_plan",
                    priority = RecommendationPriority.MEDIUM,
                    category = RecommendationCategory.RETIREMENT,
                    params = mapOf("risk" to retirementForecast.riskLevel)
                )
            )
        }
        
        // Рекомендации по сравнению с пирами
        if (peerComparison.savingsRateVsPeers < -5) {
            recommendations.add(
                FinancialRecommendation(
                    code = "recommendation_increase_savings_rate",
                    priority = RecommendationPriority.MEDIUM,
                    category = RecommendationCategory.SAVINGS,
                    params = mapOf("gap" to (-peerComparison.savingsRateVsPeers).toInt().toString())
                )
            )
        }
        
        // Рекомендации по диверсификации доходов
        val incomeSources = transactions.filter { !it.isExpense }.map { it.source }.distinct().size
        if (incomeSources < 2) {
            recommendations.add(
                FinancialRecommendation(
                    code = "recommendation_diversify_income",
                    priority = RecommendationPriority.MEDIUM,
                    category = RecommendationCategory.INCOME,
                    params = mapOf("sources" to incomeSources.toString())
                )
            )
        }
        
        // Рекомендации по чрезмерным расходам
        val highExpenseTransactions = transactions.filter { 
            it.isExpense && it.amount.toMajorDouble() > 10000.0
        }
        if (highExpenseTransactions.size > 3) {
            recommendations.add(
                FinancialRecommendation(
                    code = "recommendation_reduce_large_expenses",
                    priority = RecommendationPriority.HIGH,
                    category = RecommendationCategory.EXPENSES,
                    params = mapOf("count" to highExpenseTransactions.size.toString())
                )
            )
        }
        
        return recommendations.sortedBy { it.priority.ordinal }
    }
}


