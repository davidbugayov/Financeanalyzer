package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Money

/**
 * Рассчитывает прогресс цели: процент выполнения.
 */
class GoalProgressUseCase {
    operator fun invoke(current: Money, target: Money): Double {
        val denom = target.toMajorDouble()
        if (denom <= 0.0) return 0.0
        val pct = (current.toMajorDouble() / denom) * 100.0
        return pct.coerceIn(0.0, 100.0)
    }
}


