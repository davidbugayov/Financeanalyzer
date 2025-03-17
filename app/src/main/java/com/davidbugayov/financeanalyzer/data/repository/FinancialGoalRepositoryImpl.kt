package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.local.dao.FinancialGoalDao
import com.davidbugayov.financeanalyzer.data.local.entity.FinancialGoalEntity
import com.davidbugayov.financeanalyzer.domain.model.FinancialGoal
import com.davidbugayov.financeanalyzer.domain.repository.FinancialGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Реализация репозитория для работы с финансовыми целями.
 * Использует Room DAO для доступа к данным.
 * @param dao DAO для работы с финансовыми целями.
 */
class FinancialGoalRepositoryImpl(
    private val dao: FinancialGoalDao
) : FinancialGoalRepository {

    /**
     * Получает все финансовые цели.
     * @return Flow со списком финансовых целей.
     */
    override fun getAllGoals(): Flow<List<FinancialGoal>> {
        return dao.getAllGoals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Получает финансовую цель по идентификатору.
     * @param id Идентификатор цели.
     * @return Flow с финансовой целью или null, если цель не найдена.
     */
    override fun getGoalById(id: String): Flow<FinancialGoal?> {
        return dao.getGoalById(id).map { entity ->
            entity?.toDomain()
        }
    }

    /**
     * Добавляет новую финансовую цель.
     * @param goal Финансовая цель для добавления.
     */
    override suspend fun addGoal(goal: FinancialGoal) {
        dao.insertGoal(FinancialGoalEntity.fromDomain(goal))
    }

    /**
     * Обновляет существующую финансовую цель.
     * @param goal Финансовая цель для обновления.
     */
    override suspend fun updateGoal(goal: FinancialGoal) {
        dao.updateGoal(FinancialGoalEntity.fromDomain(goal))
    }

    /**
     * Удаляет финансовую цель.
     * @param id Идентификатор цели для удаления.
     */
    override suspend fun deleteGoal(id: String) {
        dao.getGoalById(id).collect { entity ->
            entity?.let {
                dao.deleteGoal(it)
            }
        }
    }

    /**
     * Получает активные (незавершенные) финансовые цели.
     * @return Flow со списком активных финансовых целей.
     */
    override fun getActiveGoals(): Flow<List<FinancialGoal>> {
        return dao.getActiveGoals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Получает завершенные финансовые цели.
     * @return Flow со списком завершенных финансовых целей.
     */
    override fun getCompletedGoals(): Flow<List<FinancialGoal>> {
        return dao.getCompletedGoals().map { entities ->
            entities.map { it.toDomain() }
        }
    }
} 