package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction

class PredictFutureExpensesUseCase {
    operator fun invoke(transactions: List<Transaction>, months: Int = 3): Money {
        if (transactions.isEmpty()) return Money.zero()

        val expenses = transactions.filter { it.isExpense }
        if (expenses.isEmpty()) return Money.zero()

        val monthlyExpenses = expenses
            .groupBy { t -> "${t.date.year}-${t.date.month}" }
            .mapValues { (_, txs) -> txs.sumOf { it.amount.amount } }

        if (monthlyExpenses.isEmpty()) return Money.zero()

        val averageMonthlyExpense = monthlyExpenses.values.average()
        val predictedAmount = averageMonthlyExpense * months

        return Money(predictedAmount, expenses.firstOrNull()?.amount?.currency ?: Currency.RUB)
    }
}


