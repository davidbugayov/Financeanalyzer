package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.FinancialGoal
import com.davidbugayov.financeanalyzer.domain.repository.FinancialGoalRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для управления финансовыми целями.
 * Следует принципам Clean Architecture и Single Responsibility.
 */
class ManageFinancialGoalUseCase(
    private val repository: FinancialGoalRepository
) {
    /**
     * Получает финансовую цель по идентификатору.
     * @param id Идентификатор цели.
     * @return Flow с финансовой целью или null, если цель не найдена.
     */
    fun getGoalById(id: String): Flow<FinancialGoal?> {
        return repository.getGoalById(id)
    }
    
    /**
     * Добавляет новую финансовую цель.
     * @param goal Финансовая цель для добавления.
     */
    suspend fun addGoal(goal: FinancialGoal) {
        repository.addGoal(goal)
    }
    
    /**
     * Обновляет существующую финансовую цель.
     * @param goal Финансовая цель для обновления.
     */
    suspend fun updateGoal(goal: FinancialGoal) {
        repository.updateGoal(goal)
    }
    
    /**
     * Удаляет финансовую цель.
     * @param id Идентификатор цели для удаления.
     */
    suspend fun deleteGoal(id: String) {
        repository.deleteGoal(id)
    }
    
    /**
     * Обновляет текущую сумму финансовой цели.
     * @param id Идентификатор цели.
     * @param amount Новая текущая сумма.
     */
    suspend fun updateGoalAmount(id: String, amount: Double) {
        val goalFlow = repository.getGoalById(id)
        goalFlow.collect { goal ->
            goal?.let {
                val updatedGoal = it.copy(
                    currentAmount = amount,
                    updatedAt = java.util.Date(),
                    isCompleted = amount >= it.targetAmount
                )
                repository.updateGoal(updatedGoal)
            }
        }
    }
    
    /**
     * Добавляет сумму к текущей сумме финансовой цели.
     * @param id Идентификатор цели.
     * @param amount Сумма для добавления.
     */
    suspend fun addAmountToGoal(id: String, amount: Double) {
        val goalFlow = repository.getGoalById(id)
        goalFlow.collect { goal ->
            goal?.let {
                val newAmount = it.currentAmount + amount
                val updatedGoal = it.copy(
                    currentAmount = newAmount,
                    updatedAt = java.util.Date(),
                    isCompleted = newAmount >= it.targetAmount
                )
                repository.updateGoal(updatedGoal)
            }
        }
    }
} 