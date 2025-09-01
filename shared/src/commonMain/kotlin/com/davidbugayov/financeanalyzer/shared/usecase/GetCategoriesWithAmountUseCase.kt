package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.CategoryWithAmount
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction

class GetCategoriesWithAmountUseCase {
    operator fun invoke(transactions: List<Transaction>): List<CategoryWithAmount> {
        val total = transactions.sumOf { it.amount.amount }
        if (total == 0.0) return emptyList()

        return transactions
            .groupBy { it.category }
            .map { (category, txs) ->
                val amount = txs.sumOf { it.amount.amount }
                CategoryWithAmount(
                    category = category,
                    amount = Money(amount, txs.first().amount.currency),
                )
            }
            .sortedByDescending { it.amount.amount }
    }
}


