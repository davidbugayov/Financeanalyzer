package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.CategoryWithAmount
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction

class GetCategoriesWithAmountUseCase {
    operator fun invoke(transactions: List<Transaction>, isExpense: Boolean): List<CategoryWithAmount> {
        return transactions
            .filter { it.isExpense == isExpense && it.category.isNotBlank() }
            .groupBy { it.category }
            .map { (category, txs) ->
                val minor = txs.sumOf { it.amount.minor }
                CategoryWithAmount(
                    category = category,
                    amount = Money(minor, txs.first().amount.currency),
                )
            }
            .sortedByDescending { it.amount.minor }
    }
}


