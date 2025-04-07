package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с бюджетными категориями
 */
interface BudgetRepository {
    /**
     * Получает все бюджетные категории
     * @return Flow со списком бюджетных категорий
     */
    fun getAllCategories(): Flow<List<BudgetCategory>>

    /**
     * Получает категорию по идентификатору
     * @param id Идентификатор категории
     * @return Категория или null, если категория не найдена
     */
    suspend fun getCategoryById(id: String): BudgetCategory?

    /**
     * Добавляет новую категорию бюджета
     * @param category Категория для добавления
     * @return ID добавленной категории
     */
    suspend fun addCategory(category: BudgetCategory): String

    /**
     * Обновляет существующую категорию
     * @param category Категория для обновления
     */
    suspend fun updateCategory(category: BudgetCategory)

    /**
     * Удаляет категорию
     * @param id Идентификатор категории для удаления
     */
    suspend fun deleteCategory(id: String)

    /**
     * Удаляет все категории
     */
    suspend fun deleteAllCategories()
} 