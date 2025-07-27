package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.domain.util.StringProvider

/**
 * Тип кошелька.
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
     * Получить локализованное название типа кошелька
     */
    fun getDisplayName(): String = when (this) {
        CASH -> StringProvider.walletTypeCash
        CARD -> StringProvider.walletTypeCard
        SAVINGS -> StringProvider.walletTypeSavings
        INVESTMENT -> StringProvider.walletTypeInvestment
        GOAL -> StringProvider.walletTypeGoal
        OTHER -> StringProvider.walletTypeOther
    }

    /**
     * Получить описание назначения типа кошелька
     */
    fun getDescription(): String = when (this) {
        CASH -> StringProvider.walletTypeCashDescription
        CARD -> StringProvider.walletTypeCardDescription
        SAVINGS -> StringProvider.walletTypeSavingsDescription
        INVESTMENT -> StringProvider.walletTypeInvestmentDescription
        GOAL -> StringProvider.walletTypeGoalDescription
        OTHER -> StringProvider.walletTypeOtherDescription
    }

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