package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import androidx.compose.ui.graphics.Color
import com.davidbugayov.financeanalyzer.presentation.theme.CategoryColors
import com.davidbugayov.financeanalyzer.presentation.theme.ExpenseColors
import com.davidbugayov.financeanalyzer.presentation.theme.IncomeColors

class CategoryColorProviderImpl : CategoryColorProvider {
    
    private val categoryToColorMap = mutableMapOf<Pair<String, Boolean>, Color>()
    
    override fun getColorForCategory(categoryId: String, isIncome: Boolean): Color {
        val key = categoryId to isIncome
        return categoryToColorMap.getOrPut(key) {
            if (isIncome) {
                // Try to get predefined category color or use a color from income palette
                CategoryColors.incomeCategoryColors[categoryId] 
                    ?: IncomeColors.values().random().color
            } else {
                // Try to get predefined category color or use a color from expense palette
                CategoryColors.expenseCategoryColors[categoryId]
                    ?: ExpenseColors.values().random().color
            }
        }
    }
} 