package com.davidbugayov.financeanalyzer.data.model

import com.davidbugayov.financeanalyzer.domain.model.Money
import java.math.BigDecimal

data class CategoryStats(
    val category: String,
    val amount: Money,
    val percentage: BigDecimal,
    val count: Int,
    val isExpense: Boolean,
) 