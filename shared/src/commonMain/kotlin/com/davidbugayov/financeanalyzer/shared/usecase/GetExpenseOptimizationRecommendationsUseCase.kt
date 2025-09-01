package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction

/**
 * Use case для получения рекомендаций по оптимизации расходов на основе прогноза
 */
class GetExpenseOptimizationRecommendationsUseCase {

    operator fun invoke(
        transactions: List<Transaction>,
        predictedExpenses: Map<String, Money>
    ): List<ExpenseOptimizationRecommendation> {
        val recommendations = mutableListOf<ExpenseOptimizationRecommendation>()

        // Анализируем текущие расходы по категориям
        val currentExpenses = transactions
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, txs) ->
                txs.sumOf { it.amount.amount }
            }

        // Сравниваем прогнозы с текущими расходами
        predictedExpenses.forEach { (category, predictedAmount) ->
            val currentAmount = currentExpenses[category] ?: 0.0

            if (currentAmount > 0) {
                val growthRate = (predictedAmount.amount - currentAmount) / currentAmount

                if (growthRate > 0.2) { // Рост более 20%
                    recommendations.add(
                        ExpenseOptimizationRecommendation(
                            category = category,
                            currentAmount = Money(currentAmount, predictedAmount.currency),
                            predictedAmount = predictedAmount,
                            growthRate = growthRate,
                            priority = if (growthRate > 0.5) RecommendationPriority.HIGH else RecommendationPriority.MEDIUM,
                            suggestion = generateSuggestion(category, growthRate)
                        )
                    )
                }
            }
        }

        return recommendations.sortedByDescending { it.growthRate }
    }

    private fun generateSuggestion(category: String, growthRate: Double): String {
        return when {
            growthRate > 0.5 -> "Критический рост расходов в категории '$category'. Рекомендуется немедленно пересмотреть бюджет."
            growthRate > 0.3 -> "Значительный рост расходов в категории '$category'. Рассмотрите возможности экономии."
            else -> "Умеренный рост расходов в категории '$category'. Следите за трендом."
        }
    }
}

/**
 * Рекомендация по оптимизации расходов
 */
data class ExpenseOptimizationRecommendation(
    val category: String,
    val currentAmount: Money,
    val predictedAmount: Money,
    val growthRate: Double,
    val priority: RecommendationPriority,
    val suggestion: String
)

/**
 * Приоритет рекомендации
 */
enum class RecommendationPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}