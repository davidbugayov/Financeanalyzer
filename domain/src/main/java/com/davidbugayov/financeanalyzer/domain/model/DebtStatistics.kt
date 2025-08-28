package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.shared.model.Money

/**
 * Модель статистики долгов.
 */
data class DebtStatistics(
    val totalActiveDebts: Int,
    val totalBorrowed: Money,
    val totalLent: Money,
    val netDebt: Money,
    val overdueDebtsCount: Int,
    val overdueAmount: Money,
    val paidDebtsCount: Int,
    val totalDebtsEver: Int,
)
