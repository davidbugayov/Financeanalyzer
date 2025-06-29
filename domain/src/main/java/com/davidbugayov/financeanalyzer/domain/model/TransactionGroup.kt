package com.davidbugayov.financeanalyzer.domain.model

import java.util.Date

/**
 * Группа транзакций для отображения в списке.
 * Используется для группировки транзакций по дате.
 *
 * @property date Строковое представление даты группы
 * @property transactions Список транзакций в группе
 * @property balance Общий баланс группы (доходы - расходы)
 * @property name Название группы (обычно категория или дата)
 * @property total Общая сумма транзакций в группе
 * @property displayPeriod Строковое представление диапазона дат
 */
data class TransactionGroup(
    val date: Date,
    val transactions: List<Transaction>,
    val balance: Double = 0.0,
    val name: String = date.toString(),
    val total: Double = balance,
    val displayPeriod: String = ""
) 