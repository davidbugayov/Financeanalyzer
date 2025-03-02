package com.davidbugayov.financeanalyzer.domain.model

import androidx.room.ColumnInfo
import androidx.room.Ignore

data class TransactionStats(
    @ColumnInfo(name = "totalIncome")
    var totalIncome: Double = 0.0,
    
    @ColumnInfo(name = "totalExpenses")
    var totalExpenses: Double = 0.0,
    
    @ColumnInfo(name = "netAmount")
    var netAmount: Double = 0.0,
    
    @ColumnInfo(name = "transactionCount")
    var transactionCount: Int = 0,
    
    @ColumnInfo(name = "averageTransactionAmount")
    var averageTransactionAmount: Double = 0.0,
    
    @Ignore
    val categoryBreakdown: Map<String, Double> = emptyMap(),
    
    @Ignore
    val tagBreakdown: Map<String, Double> = emptyMap(),
    
    @Ignore
    val dailyTotals: Map<String, Double> = emptyMap(),
    
    @Ignore
    val monthlyTotals: Map<String, Double> = emptyMap(),
    
    @Ignore
    val topCategories: List<Pair<String, Double>> = emptyList()
) 