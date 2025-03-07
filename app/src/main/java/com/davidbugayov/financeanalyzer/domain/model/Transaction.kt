package com.davidbugayov.financeanalyzer.domain.model

import java.math.BigDecimal
import java.util.Date

/**
 * Доменная модель транзакции.
 * Представляет собой финансовую операцию (доход или расход).
 * Чистая модель данных, не зависящая от фреймворков и библиотек.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 *
 * @property id Уникальный идентификатор транзакции (0 для новых транзакций)
 * @property title Название или описание транзакции
 * @property amount Сумма транзакции (Money)
 * @property category Категория транзакции (например, "Продукты", "Зарплата")
 * @property isExpense Тип транзакции (true - расход, false - доход)
 * @property date Дата совершения транзакции
 * @property note Дополнительное примечание к транзакции (опционально)
 */
data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Money,
    val category: String,
    val isExpense: Boolean,
    val date: Date,
    val note: String? = null
) {

    /**
     * Конструктор для обратной совместимости с Double
     */
    constructor(
        id: Long = 0,
        title: String,
        amount: Double,
        category: String,
        isExpense: Boolean,
        date: Date,
        note: String? = null,
        currency: Currency = Currency.RUB
    ) : this(
        id = id,
        title = title,
        amount = Money(amount, currency),
        category = category,
        isExpense = isExpense,
        date = date,
        note = note
    )

    /**
     * Конструктор для обратной совместимости с BigDecimal
     */
    constructor(
        id: Long = 0,
        title: String,
        amount: BigDecimal,
        category: String,
        isExpense: Boolean,
        date: Date,
        note: String? = null,
        currency: Currency = Currency.RUB
    ) : this(
        id = id,
        title = title,
        amount = Money(amount, currency),
        category = category,
        isExpense = isExpense,
        date = date,
        note = note
    )

    /**
     * Получает абсолютное значение суммы транзакции
     * @return Абсолютное значение суммы
     */
    fun getAbsoluteAmount(): Money {
        return amount.abs()
    }

    /**
     * Получает сумму транзакции с учетом знака (положительная для доходов, отрицательная для расходов)
     * @return Сумма со знаком
     */
    fun getSignedAmount(): Money {
        return if (isExpense) amount.abs() * -1 else amount.abs()
    }
} 