package com.davidbugayov.financeanalyzer.domain.model

/**
 * Группа транзакций для отображения в списке.
 * Используется для группировки транзакций по дате.
 *
 * @property date Строковое представление даты группы
 * @property transactions Список транзакций в группе
 * @property balance Общий баланс группы (доходы - расходы)
 * @property name Название группы (обычно категория или дата)
 * @property total Общая сумма транзакций в группе
 */
data class TransactionGroup(
    val date: String,
    val transactions: List<Transaction>,
    val balance: Money,
    val name: String = date,
    val total: Money = balance
) 