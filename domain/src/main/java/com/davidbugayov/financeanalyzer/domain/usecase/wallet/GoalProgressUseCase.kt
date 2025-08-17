package com.davidbugayov.financeanalyzer.domain.usecase.wallet

import com.davidbugayov.financeanalyzer.domain.model.Wallet

/**
 * Вычисляет процент выполнения цели для целевого кошелька (goal wallet).
 * Возвращает значение от 0 до 100 (целое).
 * Если goalAmount не задан или <= 0, возвращает 0.
 */
class GoalProgressUseCase {
    fun invoke(wallet: Wallet): Int {
        val goal = wallet.goalAmount ?: return 0
        if (goal.isZero()) return 0
        val percent = (wallet.balance.toMajorDouble() / goal.toMajorDouble() * 100).toInt()
        return percent.coerceIn(0, 100)
    }
} 