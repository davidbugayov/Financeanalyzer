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
) {

    /**
     * Конструктор для обратной совместимости с Double
     */
    constructor(date: Date, amount: Double) : this(
        date = date,
        amount = Money(amount)
    )
}