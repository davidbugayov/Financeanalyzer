package com.davidbugayov.financeanalyzer.domain.model

import java.util.Date

/**
 * Модель данных для представления ежедневных расходов.
 *
 * @property date Дата расходов
 * @property amount Сумма расходов в виде объекта Money
 */
data class DailyExpense(
    val date: Date,
    val amount: Money
)