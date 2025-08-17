package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import java.math.BigDecimal

class PredictFutureExpensesUseCase {
    operator fun invoke(transactions: List<Transaction>, months: Int = 3): Money {
        if (transactions.isEmpty()) return Money.zero()

        val expenses = transactions.filter { it.isExpense }
        if (expenses.isEmpty()) return Money.zero()

        val monthlyExpenses = expenses
            .groupBy { t -> "${t.date.year}-${t.date.month}" }
            .mapValues { (_, txs) -> txs.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) } }

        if (monthlyExpenses.isEmpty()) return Money.zero()

        val averageMonthlyExpense = monthlyExpenses.values.fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount) }.divide(BigDecimal.valueOf(monthlyExpenses.size.toDouble()), 10, java.math.RoundingMode.HALF_EVEN)

        val predictedAmount = averageMonthlyExpense.multiply(BigDecimal.valueOf(months.toDouble()))

        return Money(predictedAmount)
    }
}


