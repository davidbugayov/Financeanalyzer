package com.davidbugayov.financeanalyzer.shared.model

/**
 * Тип кошелька для KMP.
 * Используется для упрощённой фильтрации и отображения иконок.
 */
enum class WalletType {
    /** Наличные деньги */
    CASH,

    /** Банковская карта / расчётный счёт */
    CARD,

    /** Сбережения (накопительный, депозит) */
    SAVINGS,

    /** Инвестиции (брокерский счёт, ИИС) */
    INVESTMENT,

    /** Целевой кошелёк (goal) — содержит цель и дату */
    GOAL,

    /** Прочее (долги, кредиты и т. д.) */
    OTHER;

    /**
     * Получить эмодзи иконку для типа кошелька
     */
    fun getIcon(): String = when (this) {
        CASH -> "💵"
        CARD -> "💳"
        SAVINGS -> "🏦"
        INVESTMENT -> "📈"
        GOAL -> "🎯"
        OTHER -> "📊"
    }
}

/**
 * Унифицированная KMP-модель кошелька для всех платформ.
 * Содержит все необходимые поля для работы с кошельками.
 */
data class Wallet(
    val id: String,
    val name: String,
    val type: WalletType = WalletType.CARD,
    val balance: Money = Money.zero(),
    val limit: Money = Money.zero(),
    val spent: Money = Money.zero(),
    val periodDuration: Int = 14,
    val periodStartDate: Long = 0L,
    val linkedCategories: List<String> = emptyList(),
    val color: Int? = null,
    val goalAmount: Money? = null,
    val goalDate: Long? = null,
    val parentWalletId: String? = null,
)


