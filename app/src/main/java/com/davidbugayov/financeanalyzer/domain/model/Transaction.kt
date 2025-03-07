package com.davidbugayov.financeanalyzer.domain.model

import java.util.Date

/**
 * Доменная модель транзакции.
 * Представляет собой финансовую операцию (доход или расход).
 * Чистая модель данных, не зависящая от фреймворков и библиотек.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 *
 * @property id Уникальный идентификатор транзакции (0 для новых транзакций)
 * @property title Название или описание транзакции
 * @property amount Сумма транзакции (положительное число)
 * @property category Категория транзакции (например, "Продукты", "Зарплата")
 * @property isExpense Тип транзакции (true - расход, false - доход)
 * @property date Дата совершения транзакции
 * @property note Дополнительное примечание к транзакции (опционально)
 */
data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val isExpense: Boolean,
    val date: Date,
    val note: String? = null
) 