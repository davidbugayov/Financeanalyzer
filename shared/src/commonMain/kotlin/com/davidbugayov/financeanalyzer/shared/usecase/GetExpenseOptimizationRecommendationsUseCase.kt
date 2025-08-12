package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Transaction

/**
 * UseCase для генерации рекомендаций по оптимизации расходов.
 * Анализирует транзакции и возвращает список рекомендаций.
 */
class GetExpenseOptimizationRecommendationsUseCase {
    
    /**
     * Генерирует рекомендации по оптимизации расходов.
     * @param transactions Список транзакций пользователя.
     * @return Список рекомендаций по оптимизации расходов.
     */
    fun invoke(transactions: List<Transaction>): List<String> {
        val recommendations = mutableListOf<String>()
        if (transactions.isEmpty()) return recommendations

        // 1. Повторяющиеся траты (подписки)
        val subscriptionKeywords = listOf("подписк", "subscription", "services", "music", "video", "netflix", "yandex", "apple", "google")
        val subscriptions = transactions.filter { tx ->
            tx.isExpense && subscriptionKeywords.any { key ->
                tx.category.contains(key, true) || (tx.note?.contains(key, true) == true)
            }
        }
        if (subscriptions.isNotEmpty()) {
            recommendations.add("Проверьте подписки - возможно, некоторые из них не используются")
        }

        // 2. Крупные разовые расходы
        val largeExpenses = transactions.filter { it.isExpense && it.amount.toMajorDouble() > 10000.0 }
        if (largeExpenses.isNotEmpty()) {
            recommendations.add("Планируйте крупные покупки заранее и сравнивайте цены")
        }

        // 3. Частые расходы на кафе/рестораны
        val foodKeywords = listOf("кафе", "cafe", "ресторан", "restaurant", "еда", "food", "coffee", "кофе", "бар")
        val foodExpenses = transactions.filter { it.isExpense && foodKeywords.any { key -> it.category.contains(key, true) } }
        if (foodExpenses.size > 5) {
            recommendations.add("Рассмотрите возможность готовить дома чаще для экономии на питании")
        }

        // 4. Много мелких трат
        val smallExpenses = transactions.filter { it.isExpense && it.amount.toMajorDouble() < 200.0 }
        if (smallExpenses.size > 15) {
            recommendations.add("Контролируйте мелкие расходы - они могут составлять значительную сумму")
        }

        // 5. Нет регулярных накоплений
        val income = transactions.filter { !it.isExpense }.sumOf { it.amount.toMajorDouble() }
        val savings = transactions.filter { !it.isExpense && it.category.contains("накоплен", true) }
        if (income > 0.0 && savings.isEmpty()) {
            recommendations.add("Рассмотрите возможность регулярных накоплений для финансовой стабильности")
        }

        return recommendations
    }
}
