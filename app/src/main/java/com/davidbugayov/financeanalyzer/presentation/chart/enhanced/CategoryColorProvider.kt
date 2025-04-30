package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import androidx.compose.ui.graphics.Color

interface CategoryColorProvider {
    fun getColorForCategory(categoryId: String, isIncome: Boolean): Color
} 