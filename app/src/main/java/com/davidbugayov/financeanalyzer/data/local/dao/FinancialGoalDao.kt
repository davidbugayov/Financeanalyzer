package com.davidbugayov.financeanalyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.davidbugayov.financeanalyzer.data.local.entity.FinancialGoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с финансовыми целями в базе данных.
 */
@Dao
interface FinancialGoalDao {
    /**
     * Получает все финансовые цели.
     * @return Flow со списком финансовых целей.
     */
    @Query("SELECT * FROM financial_goals ORDER BY created_at DESC")
    fun getAllGoals(): Flow<List<FinancialGoalEntity>>
    
    /**
     * Получает финансовую цель по идентификатору.
     * @param id Идентификатор цели.
     * @return Flow с финансовой целью или null, если цель не найдена.
     */
    @Query("SELECT * FROM financial_goals WHERE id = :id")
    fun getGoalById(id: String): Flow<FinancialGoalEntity?>
    
    /**
     * Добавляет новую финансовую цель.
     * @param goal Финансовая цель для добавления.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: FinancialGoalEntity)
    
    /**
     * Обновляет существующую финансовую цель.
     * @param goal Финансовая цель для обновления.
     */
    @Update
    suspend fun updateGoal(goal: FinancialGoalEntity)
    
    /**
     * Удаляет финансовую цель.
     * @param goal Финансовая цель для удаления.
     */
    @Delete
    suspend fun deleteGoal(goal: FinancialGoalEntity)
    
    /**
     * Получает активные (незавершенные) финансовые цели.
     * @return Flow со списком активных финансовых целей.
     */
    @Query("SELECT * FROM financial_goals WHERE is_completed = 0 ORDER BY created_at DESC")
    fun getActiveGoals(): Flow<List<FinancialGoalEntity>>
    
    /**
     * Получает завершенные финансовые цели.
     * @return Flow со списком завершенных финансовых целей.
     */
    @Query("SELECT * FROM financial_goals WHERE is_completed = 1 ORDER BY created_at DESC")
    fun getCompletedGoals(): Flow<List<FinancialGoalEntity>>
} 