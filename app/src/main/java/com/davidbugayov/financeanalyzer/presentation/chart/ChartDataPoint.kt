package com.davidbugayov.financeanalyzer.presentation.chart

import com.davidbugayov.financeanalyzer.domain.model.Money
import java.util.Date

/**
 * Интерфейс для хранения точки данных на графике
 */
interface ChartDataPoint {
    /**
     * Дата точки на графике
     */
    val date: Date
    
    /**
     * Значение точки (сумма)
     */
    val value: Money
} 