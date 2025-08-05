package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.Subcategory
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с подкатегориями
 */
interface SubcategoryRepository {

    /**
     * Получает все подкатегории
     */
    suspend fun getAllSubcategories(): List<Subcategory>

    /**
     * Поток всех подкатегорий
     */
    fun observeAllSubcategories(): Flow<List<Subcategory>>

    /**
     * Получает подкатегории для конкретной категории
     * @param categoryId ID родительской категории
     */
    suspend fun getSubcategoriesByCategoryId(categoryId: Long): List<Subcategory>

    /**
     * Поток подкатегорий для конкретной категории
     * @param categoryId ID родительской категории
     */
    fun observeSubcategoriesByCategoryId(categoryId: Long): Flow<List<Subcategory>>

    /**
     * Получает подкатегорию по ID
     * @param id ID подкатегории
     */
    suspend fun getSubcategoryById(id: Long): Subcategory?

    /**
     * Добавляет новую подкатегорию
     * @param subcategory Подкатегория для добавления
     */
    suspend fun insertSubcategory(subcategory: Subcategory): Long

    /**
     * Обновляет подкатегорию
     * @param subcategory Подкатегория для обновления
     */
    suspend fun updateSubcategory(subcategory: Subcategory)

    /**
     * Удаляет подкатегорию
     * @param subcategory Подкатегория для удаления
     */
    suspend fun deleteSubcategory(subcategory: Subcategory)

    /**
     * Удаляет подкатегорию по ID
     * @param id ID подкатегории для удаления
     */
    suspend fun deleteSubcategoryById(id: Long)

    /**
     * Удаляет все подкатегории для конкретной категории
     * @param categoryId ID родительской категории
     */
    suspend fun deleteSubcategoriesByCategoryId(categoryId: Long)

    /**
     * Увеличивает счетчик использования подкатегории
     * @param id ID подкатегории
     */
    suspend fun incrementSubcategoryCount(id: Long)
} 