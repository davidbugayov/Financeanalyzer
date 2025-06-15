package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.CategoryStats
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculateCategoryStatsUseCase @Inject constructor() {

    operator fun invoke(transactions: List<Transaction>): Triple<List<CategoryStats>, Money, Money> {
        val filteredTransactions = transactions.filter { it.category != null }
        
        val totalExpense = filteredTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount.value }
        
        val totalIncome = filteredTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount.value }
            
        val stats = filteredTransactions
            .groupBy { it.category!! }
            .map { (category, txs) ->
                val amount = txs.sumOf { it.amount.value }
                val percentage = if (txs.first().type == TransactionType.EXPENSE && totalExpense > BigDecimal.ZERO) {
                    amount.divide(totalExpense, 4, RoundingMode.HALF_UP).multiply(BigDecimal("100"))
                } else if (txs.first().type == TransactionType.INCOME && totalIncome > BigDecimal.ZERO) {
                    amount.divide(totalIncome, 4, RoundingMode.HALF_UP).multiply(BigDecimal("100"))
                } else {
                    BigDecimal.ZERO
                }
                
                CategoryStats(
                    category = category,
                    amount = Money(amount),
                    percentage = percentage,
                    count = txs.size,
                    type = txs.first().type
                )
            }
            .sortedByDescending { it.amount.value }
            
        return Triple(stats, Money(totalIncome), Money(totalExpense))
    }
} 