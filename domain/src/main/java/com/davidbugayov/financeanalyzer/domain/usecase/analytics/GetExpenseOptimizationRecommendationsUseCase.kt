package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.util.StringProvider

/**
 * UseCase для генерации рекомендаций по оптимизации расходов.
 * Анализирует транзакции и возвращает список рекомендаций.
 *
 * @param transactions Список транзакций пользователя.
 * @return Список рекомендаций по оптимизации расходов.
 */
class GetExpenseOptimizationRecommendationsUseCase {
    fun invoke(transactions: List<Transaction>): List<String> {
        val recommendations = mutableListOf<String>()
        if (transactions.isEmpty()) return recommendations

        // 1. Повторяющиеся траты (подписки)
        val subscriptionKeywords = listOf("подписк", "subscription", StringProvider.categoryServices, "music", "video", "netflix", "yandex", "apple", "google")
        val subscriptions = transactions.filter { tx ->
            tx.isExpense && subscriptionKeywords.any { key ->
                tx.category.contains(key, true) || tx.title.contains(key, true) || (tx.note?.contains(key, true) == true)
            }
        }
        if (subscriptions.isNotEmpty()) {
            recommendations.add(StringProvider.recommendationCheckSubscriptions)
        }

        // 2. Крупные разовые расходы
        val largeExpenses = transactions.filter { it.isExpense && it.amount.amount > 10000.toBigDecimal() }
        if (largeExpenses.isNotEmpty()) {
            recommendations.add(StringProvider.recommendationLargeExpenses)
        }

        // 3. Частые расходы на кафе/рестораны
        val foodKeywords = listOf(StringProvider.categoryCafe, StringProvider.categoryRestaurant, "еда", "food", "coffee", "кофе", "бар")
        val foodExpenses = transactions.filter { it.isExpense && foodKeywords.any { key -> it.category.contains(key, true) } }
        if (foodExpenses.size > 5) {
            recommendations.add(StringProvider.recommendationCafeSpending)
        }

        // 4. Много мелких трат
        val smallExpenses = transactions.filter { it.isExpense && it.amount.amount < 200.toBigDecimal() }
        if (smallExpenses.size > 15) {
            recommendations.add(StringProvider.recommendationSmallExpenses)
        }

        // 5. Нет регулярных накоплений
        val income = transactions.filter { !it.isExpense }.sumOf { it.amount.amount }
        val savings = transactions.filter { !it.isExpense && (it.category.contains("накоплен", true) || it.title.contains("накоплен", true)) }
        if (income > 0.toBigDecimal() && savings.isEmpty()) {
            recommendations.add(StringProvider.recommendationNoSavings)
        }

        return recommendations
    }
} 