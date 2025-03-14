package com.davidbugayov.financeanalyzer.presentation.chart

import com.davidbugayov.financeanalyzer.domain.model.DailyExpense
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.util.Date

/**
 * Состояние экрана с графиками.
 *
 * @property isLoading Флаг загрузки данных
 * @property error Сообщение об ошибке
 * @property transactions Список транзакций
 * @property startDate Начальная дата периода
 * @property endDate Конечная дата периода
 * @property showPeriodDialog Флаг отображения диалога выбора периода
 * @property showStartDatePicker Флаг отображения выбора начальной даты
 * @property showEndDatePicker Флаг отображения выбора конечной даты
 * @property showSavingsRateInfo Флаг отображения информации о норме сбережений
 * @property dailyExpenses Список ежедневных расходов
 * @property showExpenses Флаг отображения расходов (true) или доходов (false)
 */
data class ChartScreenState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val transactions: List<Transaction> = emptyList(),
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val showPeriodDialog: Boolean = false,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val showSavingsRateInfo: Boolean = false,
    val dailyExpenses: List<DailyExpense> = emptyList(),
    val showExpenses: Boolean = true
) 