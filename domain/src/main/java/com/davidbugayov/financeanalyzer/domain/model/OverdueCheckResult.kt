package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.domain.model.Debt

/**
 * Результат проверки просроченных долгов.
 */
data class OverdueCheckResult(
    val overdueDebts: List<Debt>,
    val updatedCount: Int,
)
