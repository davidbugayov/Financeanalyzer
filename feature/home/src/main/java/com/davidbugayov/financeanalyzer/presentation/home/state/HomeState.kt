package com.davidbugayov.financeanalyzer.presentation.home.state

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.utils.CurrencyProvider

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
    val balance: Money = Money.zero(CurrencyProvider.getCurrency()),
    val income: Money = Money.zero(CurrencyProvider.getCurrency()),
    val expense: Money = Money.zero(CurrencyProvider.getCurrency()),
    val dailyIncome: Money = Money.zero(CurrencyProvider.getCurrency()),
    val dailyExpense: Money = Money.zero(CurrencyProvider.getCurrency()),
    val currentFilter: TransactionFilter = TransactionFilter.MONTH,
    val showGroupSummary: Boolean = true,
    val filteredIncome: Money = Money.zero(CurrencyProvider.getCurrency()),
    val filteredExpense: Money = Money.zero(CurrencyProvider.getCurrency()),
    val filteredBalance: Money = Money.zero(CurrencyProvider.getCurrency()),
    val periodStartDate: java.util.Date? = null,
    val periodEndDate: java.util.Date? = null,
    val transactionToDelete: Transaction? = null,
    val topCategories: Map<String, Money> = emptyMap(),
    val smartExpenseTips: List<String> = emptyList(),
    val expenseOptimizationRecommendations: List<String> = emptyList(),
)
