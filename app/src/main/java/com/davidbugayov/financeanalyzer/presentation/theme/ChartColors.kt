package com.davidbugayov.financeanalyzer.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Цвета для графиков
 */
@Immutable
object ChartColors {
    // Базовые цвета графиков
    val grid = Color(0xFFE0E0E0)
    val axisText = Color(0xFF757575)
    val text = Color(0xFF212121)
    val background = Color.White
    val selectedPointOutline = Color.White
    
    // Цвета для доходов и расходов
    object Income {
        val primary = Color(0xFF4CAF50)
        val secondary = Color(0xFF81C784)
        val light = Color(0xFFC8E6C9)
        val line = Color(0xFF4CAF50)
        val point = Color(0xFF4CAF50)
        val selectedPoint = Color(0xFF388E3C)
        val area = Color(0x804CAF50)
    }
    
    object Expense {
        val primary = Color(0xFFF44336)
        val secondary = Color(0xFFE57373)
        val light = Color(0xFFFFCDD2)
        val line = Color(0xFFF44336)
        val point = Color(0xFFF44336)
        val selectedPoint = Color(0xFFD32F2F)
        val area = Color(0x80F44336)
    }
    
    // Цвета для легенды
    object Legend {
        val text = Color(0xFF757575)
        val background = Color(0xFFF5F5F5)
    }
    
    // Цвета для пустого состояния
    val emptyStateText = Color(0xFF9E9E9E)
}

/**
 * Цвета для категорий доходов
 */
@Immutable
enum class IncomeColors(val color: Color) {
    GREEN_LIGHT(Color(0xFF81C784)),
    GREEN(Color(0xFF4CAF50)),
    GREEN_DARK(Color(0xFF388E3C)),
    LIME(Color(0xFFCDDC39)),
    YELLOW(Color(0xFFFFEB3B)),
    AMBER(Color(0xFFFFC107)),
    BLUE_LIGHT(Color(0xFF03A9F4)),
    BLUE(Color(0xFF2196F3)),
    TEAL(Color(0xFF009688))
}

/**
 * Цвета для категорий расходов
 */
@Immutable
enum class ExpenseColors(val color: Color) {
    RED_LIGHT(Color(0xFFE57373)),
    RED(Color(0xFFF44336)),
    RED_DARK(Color(0xFFD32F2F)),
    ORANGE(Color(0xFFFF9800)),
    DEEP_ORANGE(Color(0xFFFF5722)),
    PURPLE(Color(0xFF9C27B0)),
    DEEP_PURPLE(Color(0xFF673AB7)),
    INDIGO(Color(0xFF3F51B5)),
    BROWN(Color(0xFF795548))
}

/**
 * Предопределенные цвета для категорий
 */
@Immutable
object CategoryColors {
    // Карта цветов для категорий доходов
    val incomeCategoryColors = mapOf(
        "salary" to IncomeColors.GREEN.color,
        "business" to IncomeColors.TEAL.color,
        "investments" to IncomeColors.AMBER.color,
        "rental" to IncomeColors.YELLOW.color,
        "gifts" to IncomeColors.BLUE_LIGHT.color,
        "other_income" to IncomeColors.LIME.color
    )
    
    // Карта цветов для категорий расходов
    val expenseCategoryColors = mapOf(
        "food" to ExpenseColors.ORANGE.color,
        "transport" to ExpenseColors.DEEP_ORANGE.color,
        "housing" to ExpenseColors.PURPLE.color,
        "entertainment" to ExpenseColors.INDIGO.color,
        "health" to ExpenseColors.RED.color,
        "education" to ExpenseColors.DEEP_PURPLE.color,
        "shopping" to ExpenseColors.RED_LIGHT.color,
        "utilities" to ExpenseColors.BROWN.color,
        "other_expense" to ExpenseColors.RED_DARK.color
    )
} 