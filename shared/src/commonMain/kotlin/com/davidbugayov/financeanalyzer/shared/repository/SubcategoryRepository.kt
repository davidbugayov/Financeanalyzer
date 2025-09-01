package com.davidbugayov.financeanalyzer.shared.repository

import com.davidbugayov.financeanalyzer.shared.model.Subcategory
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с подкатегориями в KMP.
 */
interface SubcategoryRepository {
    /**
     * Получает все категории.
     */
    fun getAllCategories(): kotlinx.coroutines.flow.Flow<List<com.davidbugayov.financeanalyzer.shared.model.Category>>

    /**
     * Получает категорию по ID.
     */
    suspend fun getCategoryById(id: Long): com.davidbugayov.financeanalyzer.shared.model.Category?

    /**
     * Создает новую категорию.
     */
    suspend fun createCategory(category: com.davidbugayov.financeanalyzer.shared.model.Category): Long

    /**
     * Обновляет категорию.
     */
    suspend fun updateCategory(category: com.davidbugayov.financeanalyzer.shared.model.Category)

    /**
     * Удаляет категорию по ID.
     */
    suspend fun deleteCategory(id: Long)

    /**
     * Получает категории по типу (доходы/расходы).
     */
    suspend fun getCategoriesByType(isExpense: Boolean): List<com.davidbugayov.financeanalyzer.shared.model.Category>

    /**
     * Очищает все категории.
     */
    suspend fun clearAllCategories()

    // Legacy methods for backward compatibility
    /**
     * Получает подкатегорию по ID.
     */
    suspend fun getSubcategoryById(id: Long): Subcategory?

    /**
     * Получает все подкатегории для указанной категории.
     */
    fun getSubcategoriesByCategoryId(categoryId: Long): Flow<List<Subcategory>>

    /**
     * Добавляет новую подкатегорию.
     * @return ID добавленной подкатегории
     */
    suspend fun insertSubcategory(subcategory: Subcategory): Long

    /**
     * Удаляет подкатегорию по ID.
     */
    suspend fun deleteSubcategoryById(subcategoryId: Long)
}