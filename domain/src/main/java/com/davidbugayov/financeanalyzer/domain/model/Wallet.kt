package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.core.model.Money

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
 * @property type Тип кошелька (наличные, карта, сбережения...)
 * @property goalAmount Целевая сумма (используется, если кошелёк ‒ цель). null, если цель не задана
 * @property goalDate Дата, к которой нужно достичь цели (мс с эпохи). null, если не задана
 * @property parentWalletId ID родительского кошелька для подпулов (sub-wallets)
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
    val color: Int? = null,
    /** Тип кошелька (наличные, карта, сбережения...) */
    val type: WalletType = WalletType.CARD,
    /** Целевая сумма (используется, если кошелёк ‒ цель). null, если цель не задана */
    val goalAmount: Money? = null,
    /** Дата, к которой нужно достичь цели (мс с эпохи). null, если не задана */
    val goalDate: Long? = null,
    /** ID родительского кошелька для подпулов (sub-wallets) */
    val parentWalletId: String? = null,
)
