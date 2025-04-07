package com.davidbugayov.financeanalyzer.domain.model

data class BudgetCategory(
    val name: String,
    val limit: Double,
    val spent: Double,
    val id: String
) 