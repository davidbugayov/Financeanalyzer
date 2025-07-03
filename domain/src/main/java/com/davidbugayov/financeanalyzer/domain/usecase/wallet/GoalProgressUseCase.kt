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
        if (goal.amount <= java.math.BigDecimal.ZERO) return 0
        val percent = wallet.balance.amount.divide(goal.amount, 4, java.math.RoundingMode.HALF_EVEN)
            .multiply(java.math.BigDecimal(100))
            .setScale(0, java.math.RoundingMode.FLOOR)
            .toInt()
        return percent.coerceIn(0, 100)
    }
} 