package com.davidbugayov.financeanalyzer.domain.model

import java.util.Date
import java.util.UUID
import com.davidbugayov.financeanalyzer.utils.ColorUtils

/**
 * Модель данных для транзакции.
 * Представляет финансовую операцию (доход или расход).
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val category: String,
    val date: Date = Date(),
    val isExpense: Boolean,
    val note: String? = null,
    val source: String,
    val sourceColor: Int
)

/**
 * Получает эффективный цвет источника транзакции.
 * Если цвет источника не задан явно, определяет его по названию источника.
 * Если и это не удалось, использует цвет по умолчанию для расхода/дохода.
 * 
 * @return Эффективный цвет источника
 */
fun Transaction.getEffectiveSourceColor(): Int {
    return ColorUtils.getEffectiveSourceColor(source, sourceColor, isExpense)
}