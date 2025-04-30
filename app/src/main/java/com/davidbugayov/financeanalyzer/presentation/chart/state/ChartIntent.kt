package com.davidbugayov.financeanalyzer.presentation.chart.state

import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.util.Date

/**
 * Интенты (намерения) для экрана графиков.
 */
sealed class ChartIntent {
    /**
     * Загрузить список транзакций.
     */
    object LoadTransactions : ChartIntent()
    
    /**
     * Переключиться между отображением расходов и доходов.
     *
     * @property showExpenses true для отображения расходов, false для доходов
     */
    data class ToggleExpenseView(val showExpenses: Boolean) : ChartIntent()
    
    /**
     * Установить диапазон дат для фильтрации.
     *
     * @property startDate начальная дата диапазона
     * @property endDate конечная дата диапазона
     */
    data class SetDateRange(val startDate: Date, val endDate: Date) : ChartIntent()
    
    /**
     * Установить тип периода.
     *
     * @property periodType тип периода (день, неделя, месяц и т.д.)
     */
    data class SetPeriodType(val periodType: PeriodType) : ChartIntent()
    
    /**
     * Показать/скрыть диалог выбора периода.
     *
     * @property show флаг отображения
     */
    data class TogglePeriodDialog(val show: Boolean) : ChartIntent()
    
    /**
     * Показать/скрыть выбор начальной даты.
     *
     * @property show флаг отображения
     */
    data class ToggleStartDatePicker(val show: Boolean) : ChartIntent()
    
    /**
     * Показать/скрыть выбор конечной даты.
     *
     * @property show флаг отображения
     */
    data class ToggleEndDatePicker(val show: Boolean) : ChartIntent()
    
    /**
     * Показать/скрыть информацию о норме сбережений.
     *
     * @property show флаг отображения
     */
    data class ToggleSavingsRateInfo(val show: Boolean) : ChartIntent()
} 