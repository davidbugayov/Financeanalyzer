package com.davidbugayov.financeanalyzer.domain.model

/**
 * Модель бюджетной категории
 *
 * @property name Название категории
 * @property limit Лимит расходов на период
 * @property spent Потраченная сумма
 * @property id Уникальный идентификатор категории
 * @property walletBalance Текущий баланс в "виртуальном кошельке"
 * @property periodDuration Продолжительность расчетного периода в днях (по умолчанию 14 дней)
 * @property periodStartDate Дата начала текущего расчетного периода (в миллисекундах)
 */
data class BudgetCategory(
    val name: String,
    val limit: Double,
    val spent: Double,
    val id: String,
    val walletBalance: Double = 0.0,
    val periodDuration: Int = 14,
    val periodStartDate: Long = System.currentTimeMillis()
) 