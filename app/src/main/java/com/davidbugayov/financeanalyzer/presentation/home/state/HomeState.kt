package com.davidbugayov.financeanalyzer.presentation.home.state

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter

/**
 * Состояние экрана Home.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
data class HomeState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val balance: Double = 0.0,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val dailyIncome: Double = 0.0,
    val dailyExpense: Double = 0.0,
    val currentFilter: TransactionFilter = TransactionFilter.MONTH,
    val showGroupSummary: Boolean = true,
    val filteredIncome: Double = 0.0,
    val filteredExpense: Double = 0.0,
    val filteredBalance: Double = 0.0
) 