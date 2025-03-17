package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.FinancialGoal
import com.davidbugayov.financeanalyzer.domain.repository.FinancialGoalRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для получения финансовых целей.
 * Следует принципам Clean Architecture и Single Responsibility.
 */
class GetFinancialGoalsUseCase(
    private val repository: FinancialGoalRepository
) {
    /**
     * Получает все финансовые цели.
     * @return Flow со списком финансовых целей.
     */
    operator fun invoke(): Flow<List<FinancialGoal>> {
        return repository.getAllGoals()
    }
    
    /**
     * Получает активные (незавершенные) финансовые цели.
     * @return Flow со списком активных финансовых целей.
     */
    fun getActiveGoals(): Flow<List<FinancialGoal>> {
        return repository.getActiveGoals()
    }
    
    /**
     * Получает завершенные финансовые цели.
     * @return Flow со списком завершенных финансовых целей.
     */
    fun getCompletedGoals(): Flow<List<FinancialGoal>> {
        return repository.getCompletedGoals()
    }
} 