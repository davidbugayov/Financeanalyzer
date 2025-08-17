package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.CategoryWithAmount
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import java.math.BigDecimal

class GetCategoriesWithAmountUseCase {
    operator fun invoke(transactions: List<Transaction>): List<CategoryWithAmount> {
        val total = transactions.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) }
        if (total == BigDecimal.ZERO) return emptyList()

        return transactions
            .groupBy { it.category }
            .map { (category, txs) ->
                val amount = txs.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) }
                CategoryWithAmount(
                    category = category,
                    amount = Money(amount, txs.first().amount.currency),
                )
            }
            .sortedByDescending { it.amount.amount }
    }
}


