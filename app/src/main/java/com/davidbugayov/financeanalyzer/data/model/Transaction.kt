package com.davidbugayov.financeanalyzer.data.model

data class Transaction(
    val date: String,
    val description: String,
    val amount: Double,
    val type: String // "Income" или "Expense"
)