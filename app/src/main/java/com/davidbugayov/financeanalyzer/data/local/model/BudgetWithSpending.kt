package com.davidbugayov.financeanalyzer.data.local.model

import androidx.room.Embedded
import com.davidbugayov.financeanalyzer.domain.model.Budget

data class BudgetWithSpending(
    @Embedded val budget: Budget,
    val spent: Double
) {
    val remainingAmount: Double
        get() = budget.limit - spent

    val spentPercentage: Double
        get() = (spent / budget.limit) * 100

    val isOverBudget: Boolean
        get() = spent > budget.limit
} 