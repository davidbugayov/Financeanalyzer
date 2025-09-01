package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction

/**
 * Use case для прогнозирования расходов по категориям
 */
class PredictExpensesByCategoryUseCase {

    operator fun invoke(
        transactions: List<Transaction>,
        monthsAhead: Int = 1
    ): Map<String, Money> {
        if (transactions.isEmpty()) return emptyMap()

        val expenses = transactions.filter { it.isExpense }
        if (expenses.isEmpty()) return emptyMap()

        // Группируем расходы по категориям
        val categoryExpenses = expenses.groupBy { it.category }

        val predictions = mutableMapOf<String, Money>()

        categoryExpenses.forEach { (category, categoryTransactions) ->
            if (categoryTransactions.isNotEmpty()) {
                // Используем основной алгоритм для каждой категории
                val predictor = PredictFutureExpensesUseCase()
                val predictedAmount = predictor(categoryTransactions, monthsAhead)
                predictions[category] = predictedAmount
            }
        }

        return predictions
    }
}
