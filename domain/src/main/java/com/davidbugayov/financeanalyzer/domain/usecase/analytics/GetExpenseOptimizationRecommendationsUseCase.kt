package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.Transaction

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
        val subscriptionKeywords = listOf("подписк", "subscription", "сервис", "music", "video", "netflix", "yandex", "apple", "google")
        val subscriptions = transactions.filter { tx ->
            tx.isExpense && subscriptionKeywords.any { key ->
                tx.category.contains(key, true) || tx.title.contains(key, true) || (tx.note?.contains(key, true) == true)
            }
        }
        if (subscriptions.isNotEmpty()) {
            recommendations.add("Проверьте ваши подписки: возможно, некоторые из них можно отключить или заменить на более выгодные.")
        }

        // 2. Крупные разовые расходы
        val largeExpenses = transactions.filter { it.isExpense && it.amount.amount > 10000.toBigDecimal() }
        if (largeExpenses.isNotEmpty()) {
            recommendations.add("Обнаружены крупные разовые расходы. Планируйте такие траты заранее и ищите альтернативы.")
        }

        // 3. Частые расходы на кафе/рестораны
        val foodKeywords = listOf("кафе", "ресторан", "еда", "food", "coffee", "кофе", "бар")
        val foodExpenses = transactions.filter { it.isExpense && foodKeywords.any { key -> it.category.contains(key, true) } }
        if (foodExpenses.size > 5) {
            recommendations.add("Вы часто тратите на кафе и рестораны. Попробуйте готовить дома чаще — это поможет сэкономить.")
        }

        // 4. Много мелких трат
        val smallExpenses = transactions.filter { it.isExpense && it.amount.amount < 200.toBigDecimal() }
        if (smallExpenses.size > 15) {
            recommendations.add("У вас много мелких трат. Ведите учёт и попробуйте отказаться от ненужных покупок.")
        }

        // 5. Нет регулярных накоплений
        val income = transactions.filter { !it.isExpense }.sumOf { it.amount.amount }
        val savings = transactions.filter { !it.isExpense && (it.category.contains("накоплен", true) || it.title.contains("накоплен", true)) }
        if (income > 0.toBigDecimal() && savings.isEmpty()) {
            recommendations.add("Вы не делаете регулярных накоплений. Откладывайте хотя бы 10% от дохода каждый месяц.")
        }

        return recommendations
    }
} 