package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.utils

import androidx.compose.ui.graphics.Color

/**
 * Утилитный класс для работы с круговыми диаграммами
 */
object PieChartUtils {

    /**
     * Возвращает список цветов для категорий в зависимости от их количества и типа
     *
     * @param count количество категорий
     * @param isIncome true, если категории доходов, false - расходов
     * @return список цветов для отображения в круговой диаграмме
     */
    fun getCategoryColors(count: Int, isIncome: Boolean): List<Color> {
        return if (isIncome) {
            getIncomeColors(count)
        } else {
            getExpenseColors(count)
        }
    }
    
    /**
     * Возвращает список цветов для категорий доходов
     */
    private fun getIncomeColors(count: Int): List<Color> {
        val baseColors = listOf(
            Color(0xFF4CAF50), // Green
            Color(0xFF8BC34A), // Light Green
            Color(0xFF009688), // Teal
            Color(0xFF00BCD4), // Cyan
            Color(0xFF03A9F4), // Light Blue
            Color(0xFF3F51B5), // Indigo
            Color(0xFF673AB7)  // Deep Purple
        )
        
        return generateColors(baseColors, count)
    }
    
    /**
     * Возвращает список цветов для категорий расходов
     */
    private fun getExpenseColors(count: Int): List<Color> {
        val baseColors = listOf(
            Color(0xFFE53935), // Red
            Color(0xFFEC407A), // Pink
            Color(0xFFAB47BC), // Purple
            Color(0xFF7E57C2), // Deep Purple
            Color(0xFFF44336), // Red
            Color(0xFFFF9800), // Orange
            Color(0xFFFF5722), // Deep Orange
            Color(0xFFFFEB3B), // Yellow
            Color(0xFFFF9100)  // Amber
        )
        
        return generateColors(baseColors, count)
    }
    
    /**
     * Генерирует нужное количество цветов на основе базовых цветов
     */
    private fun generateColors(baseColors: List<Color>, count: Int): List<Color> {
        if (count <= baseColors.size) {
            return baseColors.take(count)
        }
        
        val result = baseColors.toMutableList()
        
        // Если категорий больше, чем у нас базовых цветов, генерируем дополнительные оттенки
        while (result.size < count) {
            val index = result.size % baseColors.size
            val baseColor = baseColors[index]
            
            // Создаем вариацию базового цвета с другой насыщенностью
            val variation = if (result.size / baseColors.size % 2 == 0) {
                // Более светлый оттенок
                baseColor.copy(
                    red = (baseColor.red + 0.1f).coerceAtMost(1.0f),
                    green = (baseColor.green + 0.1f).coerceAtMost(1.0f),
                    blue = (baseColor.blue + 0.1f).coerceAtMost(1.0f)
                )
            } else {
                // Более темный оттенок
                baseColor.copy(
                    red = (baseColor.red - 0.1f).coerceAtLeast(0.0f),
                    green = (baseColor.green - 0.1f).coerceAtLeast(0.0f),
                    blue = (baseColor.blue - 0.1f).coerceAtLeast(0.0f)
                )
            }
            
            result.add(variation)
        }
        
        return result
    }
} 