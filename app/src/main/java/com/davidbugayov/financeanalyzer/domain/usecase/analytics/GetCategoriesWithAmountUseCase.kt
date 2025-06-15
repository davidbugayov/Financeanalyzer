package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.CategoryWithAmount
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction

class GetCategoriesWithAmountUseCase {

    operator fun invoke(transactions: List<Transaction>, isExpense: Boolean): List<CategoryWithAmount> {
        return transactions
            .filter { it.isExpense == isExpense && it.category.isNotBlank() }
            .groupBy { it.category }
            .map { (category, txs) ->
                val amount = txs.sumOf { it.amount.amount }
                CategoryWithAmount(
                    category = category,
                    amount = Money(amount),
                )
            }
            .sortedByDescending { it.amount.amount }
    }
} 
