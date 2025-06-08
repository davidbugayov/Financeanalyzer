package com.davidbugayov.financeanalyzer.domain.model

/**
 * Модель кошелька
 *
 * @property name Название кошелька
 * @property limit Лимит расходов на период
 * @property spent Потраченная сумма
 * @property id Уникальный идентификатор кошелька
 * @property balance Текущий баланс в кошельке
 * @property periodDuration Продолжительность расчетного периода в днях (по умолчанию 14 дней)
 * @property periodStartDate Дата начала текущего расчетного периода (в миллисекундах)
 * @property linkedCategories Список категорий, транзакции которых учитываются в этом кошельке
 * @property color Цвет кошелька
 */
data class Wallet(
    val name: String,
    val limit: Money,
    val spent: Money,
    val id: String,
    val balance: Money = Money.zero(),
    val periodDuration: Int = 14,
    val periodStartDate: Long = System.currentTimeMillis(),
    val linkedCategories: List<String> = emptyList(),
    val color: Int? = null
)
