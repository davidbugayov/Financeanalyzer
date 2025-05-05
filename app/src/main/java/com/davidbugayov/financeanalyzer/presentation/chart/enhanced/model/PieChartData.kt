package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model

import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.domain.model.Transaction

/**
 * Данные для круговой диаграммы
 *
 * @property id идентификатор категории
 * @property name название категории
 * @property amount сумма
 * @property percentage процент от общей суммы
 * @property color цвет сектора
 */
data class PieChartData(
    val id: String,
    val name: String,
    val amount: Float,
    val percentage: Float,
    val color: Int,
    val category: Category? = null,
    val transactions: List<Transaction> = emptyList()
) 