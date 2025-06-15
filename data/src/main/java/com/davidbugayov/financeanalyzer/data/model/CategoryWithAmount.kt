package com.davidbugayov.financeanalyzer.data.model

import com.davidbugayov.financeanalyzer.domain.model.Money

data class CategoryWithAmount(
    val category: String,
    val amount: Money,
) 