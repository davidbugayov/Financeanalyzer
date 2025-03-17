package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.FinancialGoal
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с финансовыми целями.
 * Следует принципам Clean Architecture.
 */
interface FinancialGoalRepository {
    /**
     * Получает все финансовые цели.
     * @return Flow со списком финансовых целей.
     */
    fun getAllGoals(): Flow<List<FinancialGoal>>
    
    /**
     * Получает финансовую цель по идентификатору.
     * @param id Идентификатор цели.
     * @return Flow с финансовой целью или null, если цель не найдена.
     */
    fun getGoalById(id: String): Flow<FinancialGoal?>
    
    /**
     * Добавляет новую финансовую цель.
     * @param goal Финансовая цель для добавления.
     */
    suspend fun addGoal(goal: FinancialGoal)
    
    /**
     * Обновляет существующую финансовую цель.
     * @param goal Финансовая цель для обновления.
     */
    suspend fun updateGoal(goal: FinancialGoal)
    
    /**
     * Удаляет финансовую цель.
     * @param id Идентификатор цели для удаления.
     */
    suspend fun deleteGoal(id: String)
    
    /**
     * Получает активные (незавершенные) финансовые цели.
     * @return Flow со списком активных финансовых целей.
     */
    fun getActiveGoals(): Flow<List<FinancialGoal>>
    
    /**
     * Получает завершенные финансовые цели.
     * @return Flow со списком завершенных финансовых целей.
     */
    fun getCompletedGoals(): Flow<List<FinancialGoal>>
} 