package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.domain.model.Category
import java.math.BigDecimal

/**
 * Класс для хранения статистики по категории
 */
data class CategoryStatistics(
    val category: Category,
    val amount: BigDecimal,
    val percentage: Float,
    val count: Int
) 