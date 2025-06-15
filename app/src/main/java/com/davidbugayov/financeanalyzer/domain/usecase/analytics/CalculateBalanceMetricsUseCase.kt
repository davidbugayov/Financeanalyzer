package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.BalanceMetrics
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionType
import javax.inject.Inject

class CalculateBalanceMetricsUseCase @Inject constructor() {

    operator fun invoke(transactions: List<Transaction>): BalanceMetrics {
        val income = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount.value }
        
        val expense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount.value }
        
        val balance = income - expense
        
        return BalanceMetrics(
            income = Money(income),
            expense = Money(expense),
            balance = Money(balance)
        )
    }
} 