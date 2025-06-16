package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.core.model.Money

data class CategoryWithAmount(
    val category: String,
    val amount: Money,
)
