package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction

/**
 * UseCase для генерации умных советов по экономии на основе истории трат пользователя.
 * Анализирует транзакции и возвращает список советов.
 *
 * @param transactions Список транзакций пользователя.
 * @return Список советов по экономии.
 */
class GetSmartExpenseTipsUseCase {
    fun invoke(transactions: List<Transaction>): List<String> {
        val tips = mutableListOf<String>()
        if (transactions.isEmpty()) return tips

        // 1. Совет: Категории с наибольшими расходами
        val expenseByCategory = transactions
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount.amount } }
            .toList()
            .sortedByDescending { it.second }

        if (expenseByCategory.isNotEmpty()) {
            val (topCategory, topAmount) = expenseByCategory.first()
            if (topAmount > 0.toBigDecimal()) {
                tips.add("Вы тратите больше всего на категорию: '$topCategory'. Подумайте, можно ли сократить эти расходы.")
            }
        }

        // 2. Совет: Повторяющиеся траты (подписки)
        val subscriptions = transactions
            .filter { it.isExpense && (it.note?.contains("подписк", true) == true || it.title.contains("подписк", true)) }
        if (subscriptions.isNotEmpty()) {
            tips.add("У вас есть регулярные подписки. Проверьте, все ли из них вам действительно нужны.")
        }

        // 3. Совет: Крупные разовые расходы
        val largeExpenses = transactions
            .filter { it.isExpense && it.amount.amount > 5000.toBigDecimal() }
        if (largeExpenses.isNotEmpty()) {
            tips.add("Обнаружены крупные разовые расходы. Планируйте такие траты заранее, чтобы избежать финансового стресса.")
        }

        // 4. Совет: Много мелких трат
        val smallExpenses = transactions
            .filter { it.isExpense && it.amount.amount < 300.toBigDecimal() }
        if (smallExpenses.size > 10) {
            tips.add("У вас много мелких трат. Попробуйте объединять покупки или отказаться от лишнего.")
        }

        // 5. Совет: Нет накоплений
        val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount.amount }
        val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount.amount }
        if (totalIncome > 0.toBigDecimal() && totalExpense >= totalIncome) {
            tips.add("Ваши расходы равны или превышают доходы. Попробуйте откладывать хотя бы 10% от дохода.")
        }

        return tips
    }
} 