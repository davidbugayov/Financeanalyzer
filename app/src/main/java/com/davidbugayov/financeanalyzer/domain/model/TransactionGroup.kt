package com.davidbugayov.financeanalyzer.domain.model

data class TransactionGroup(
    val date: String,
    val transactions: List<Transaction>,
    val balance: Double
) 