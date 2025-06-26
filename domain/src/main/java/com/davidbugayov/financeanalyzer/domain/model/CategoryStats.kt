package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.core.model.Money
import java.math.BigDecimal

data class CategoryStats(
    val category: String,
    val amount: Money,
    val percentage: BigDecimal,
    val count: Int,
    val isExpense: Boolean,
)
