package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import org.koin.core.context.GlobalContext
import com.davidbugayov.financeanalyzer.domain.R

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
        val rp: ResourceProvider = GlobalContext.get().get()
        val subscriptionKeywords = listOf("подписк", "subscription", rp.getString(R.string.category_services), "music", "video", "netflix", "yandex", "apple", "google")
        val subscriptions = transactions.filter { tx ->
            tx.isExpense && subscriptionKeywords.any { key ->
                tx.category.contains(key, true) || tx.title.contains(key, true) || (tx.note?.contains(key, true) == true)
            }
        }
        if (subscriptions.isNotEmpty()) {
            recommendations.add(rp.getString(R.string.recommendation_check_subscriptions))
        }

        // 2. Крупные разовые расходы
        val largeExpenses = transactions.filter { it.isExpense && it.amount.amount > 10000.toBigDecimal() }
        if (largeExpenses.isNotEmpty()) {
            recommendations.add(rp.getString(R.string.recommendation_large_expenses))
        }

        // 3. Частые расходы на кафе/рестораны
        val foodKeywords = listOf(rp.getString(R.string.category_cafe), rp.getString(R.string.category_restaurant), "еда", "food", "coffee", "кофе", "бар")
        val foodExpenses = transactions.filter { it.isExpense && foodKeywords.any { key -> it.category.contains(key, true) } }
        if (foodExpenses.size > 5) {
            recommendations.add(rp.getString(R.string.recommendation_cafe_spending))
        }

        // 4. Много мелких трат
        val smallExpenses = transactions.filter { it.isExpense && it.amount.amount < 200.toBigDecimal() }
        if (smallExpenses.size > 15) {
            recommendations.add(rp.getString(R.string.recommendation_small_expenses))
        }

        // 5. Нет регулярных накоплений
        val income = transactions.filter { !it.isExpense }.sumOf { it.amount.amount }
        val savings = transactions.filter { !it.isExpense && (it.category.contains("накоплен", true) || it.title.contains("накоплен", true)) }
        if (income > 0.toBigDecimal() && savings.isEmpty()) {
            recommendations.add(rp.getString(R.string.recommendation_no_savings))
        }

        return recommendations
    }
} 