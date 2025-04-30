package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model

import androidx.compose.ui.graphics.Color
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.domain.model.Transaction

/**
 * Data class representing a pie chart segment
 */
data class PieChartItemData(
    val id: String,
    val name: String,
    val amount: Float,
    val percentage: Float,
    val color: Color,
    val category: Category? = null,
    val count: Int = 0,
    val transactions: List<Transaction> = emptyList()
) {
    /**
     * Flag indicating whether this item represents an income category
     */
    val isIncome: Boolean
        get() = category?.let { !it.isExpense } ?: false
        
    /**
     * Formatted percentage string with % symbol 
     */
    val formattedPercentage: String
        get() = String.format("%.1f%%", percentage)
    
    /**
     * Formatted amount string with 2 decimal places
     */
    val formattedAmount: String
        get() = String.format("%.2f", amount)
} 