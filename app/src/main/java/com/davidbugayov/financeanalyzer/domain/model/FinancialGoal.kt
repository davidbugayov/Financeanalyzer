package com.davidbugayov.financeanalyzer.domain.model

import java.util.Date
import java.util.UUID

/**
 * Модель данных для финансовой цели.
 * Представляет цель накопления определенной суммы к определенной дате.
 */
data class FinancialGoal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Date? = null,
    val category: String? = null,
    val description: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isCompleted: Boolean = false
) {
    /**
     * Вычисляет процент выполнения цели.
     * @return Процент выполнения от 0 до 100.
     */
    fun getProgressPercentage(): Int {
        if (targetAmount <= 0) return 0
        val percentage = (currentAmount / targetAmount) * 100
        return percentage.toInt().coerceIn(0, 100)
    }
    
    /**
     * Проверяет, достигнута ли цель.
     * @return true, если текущая сумма больше или равна целевой.
     */
    fun isGoalReached(): Boolean {
        return currentAmount >= targetAmount
    }
    
    /**
     * Вычисляет оставшуюся сумму до достижения цели.
     * @return Оставшаяся сумма (не может быть отрицательной).
     */
    fun getRemainingAmount(): Double {
        val remaining = targetAmount - currentAmount
        return if (remaining < 0) 0.0 else remaining
    }
} 