package com.davidbugayov.financeanalyzer.domain.model

import java.util.Date

data class BudgetAlert(
    val budgetId: Long,
    val category: String,
    val currentAmount: Double,
    val limit: Double,
    val percentage: Double,
    val date: Date = Date(),
    val severity: AlertSeverity = AlertSeverity.WARNING
)

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
} 