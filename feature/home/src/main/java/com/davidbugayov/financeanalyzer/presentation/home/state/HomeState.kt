package com.davidbugayov.financeanalyzer.presentation.home.state

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.shared.model.Money

/**
 * Состояние экрана Home.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
data class HomeState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val transactionGroups: List<TransactionGroup> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val balance: Money = Money.zero(),
    val income: Money = Money.zero(),
    val expense: Money = Money.zero(),
    val dailyIncome: Money = Money.zero(),
    val dailyExpense: Money = Money.zero(),
    val currentFilter: TransactionFilter = TransactionFilter.MONTH,
    val showGroupSummary: Boolean = true,
    val filteredIncome: Money = Money.zero(),
    val filteredExpense: Money = Money.zero(),
    val filteredBalance: Money = Money.zero(),
    val transactionToDelete: Transaction? = null,
    val topCategories: Map<String, Money> = emptyMap(),
    val smartExpenseTips: List<String> = emptyList(),
    val expenseOptimizationRecommendations: List<String> = emptyList(),
)
