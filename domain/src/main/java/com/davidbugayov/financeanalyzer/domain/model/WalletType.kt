package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import org.koin.core.context.GlobalContext
import com.davidbugayov.financeanalyzer.ui.R

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
    fun getDisplayName(): String {
        val rp: ResourceProvider = GlobalContext.get().get()
        return when (this) {
            CASH -> rp.getString(R.string.wallet_type_cash)
            CARD -> rp.getString(R.string.wallet_type_card)
            SAVINGS -> rp.getString(R.string.wallet_type_savings)
            INVESTMENT -> rp.getString(R.string.wallet_type_investment)
            GOAL -> rp.getString(R.string.wallet_type_goal)
            OTHER -> rp.getString(R.string.wallet_type_other)
        }
    }

    /**
     * Получить описание назначения типа кошелька
     */
    fun getDescription(): String {
        val rp: ResourceProvider = GlobalContext.get().get()
        return when (this) {
            CASH -> rp.getString(R.string.wallet_type_cash_description)
            CARD -> rp.getString(R.string.wallet_type_card_description)
            SAVINGS -> rp.getString(R.string.wallet_type_savings_description)
            INVESTMENT -> rp.getString(R.string.wallet_type_investment_description)
            GOAL -> rp.getString(R.string.wallet_type_goal_description)
            OTHER -> rp.getString(R.string.wallet_type_other_description)
        }
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