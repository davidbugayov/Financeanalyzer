package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction

class PredictFutureExpensesUseCase {
    operator fun invoke(transactions: List<Transaction>, months: Int = 1): Money {
        if (transactions.isEmpty()) return Money.zero()

        val cleaned = transactions.asSequence()
            .filter { it.isExpense }
            .filter { tx ->
                val label = (tx.category + " " + (tx.note ?: "") + " " + tx.source).lowercase()
                val isTransfer = label.contains("перевод") || label.contains("transfer")
                val isRefund = label.contains("возврат") || label.contains("refund")
                val isZero = tx.amount.minor == 0L
                !(isTransfer || isRefund || isZero)
            }.toList()

        if (cleaned.isEmpty()) return Money.zero()

        val monthlyExpenses = cleaned.groupBy { tx -> "${tx.date.year}-${tx.date.month}" }
            .mapValues { entry -> entry.value.sumOf { it.amount.minor } }

        if (monthlyExpenses.isEmpty()) return Money.zero()

        val sorted = monthlyExpenses.toList().sortedBy { it.first }.takeLast(6).map { it.second }

        val trimmed = if (sorted.size >= 3) {
            val values = sorted.sorted()
            val p10 = values[(values.lastIndex * 0.10).toInt()]
            val p90 = values[(values.lastIndex * 0.90).toInt()]
            values.map { it.coerceIn(p10, p90) }
        } else sorted

        val sum = trimmed.sum()
        val avg = if (trimmed.isNotEmpty()) sum / trimmed.size else 0L
        val total = avg * months
        return Money(total)
    }
}


