package com.davidbugayov.financeanalyzer.shared.repository

import com.davidbugayov.financeanalyzer.shared.model.Subcategory
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с подкатегориями в KMP.
 */
interface SubcategoryRepository {
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