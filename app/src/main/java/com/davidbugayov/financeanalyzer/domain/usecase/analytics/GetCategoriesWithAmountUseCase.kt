package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.CategoryWithAmount
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionType
import javax.inject.Inject

class GetCategoriesWithAmountUseCase @Inject constructor() {

    operator fun invoke(transactions: List<Transaction>, type: TransactionType): List<CategoryWithAmount> {
        return transactions
            .filter { it.type == type && it.category != null }
            .groupBy { it.category!! }
            .map { (category, txs) ->
                val amount = txs.sumOf { it.amount.value }
                CategoryWithAmount(
                    category = category,
                    amount = Money(amount)
                )
            }
            .sortedByDescending { it.amount.value }
    }
} 