package com.davidbugayov.financeanalyzer.domain.model

import java.math.BigDecimal

data class CategoryStats(
    val category: Category,
    val amount: Money,
    val percentage: BigDecimal,
    val count: Int,
    val type: TransactionType
) 