package com.davidbugayov.financeanalyzer.domain.model

/**
 * Группа транзакций для отображения в списке.
 * Используется для группировки транзакций по дате.
 *
 * @property date Строковое представление даты группы
 * @property transactions Список транзакций в группе
 * @property balance Общий баланс группы (доходы - расходы)
 */
data class TransactionGroup(
    val date: String,
    val transactions: List<Transaction>,
    val balance: Double
) 