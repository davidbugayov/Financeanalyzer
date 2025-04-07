package com.davidbugayov.financeanalyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.davidbugayov.financeanalyzer.data.local.entity.BudgetCategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) для работы с бюджетными категориями в базе данных Room.
 * Предоставляет методы для выполнения CRUD операций с бюджетными категориями.
 */
@Dao
interface BudgetCategoryDao {

    /**
     * Получает все бюджетные категории
     * @return Flow со списком всех категорий
     */
    @Query("SELECT * FROM budget_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<BudgetCategoryEntity>>

    /**
     * Получает категорию по идентификатору
     * @param id Идентификатор категории
     * @return Категория с указанным идентификатором или null, если категория не найдена
     */
    @Query("SELECT * FROM budget_categories WHERE id = :id")
    suspend fun getCategoryById(id: String): BudgetCategoryEntity?

    /**
     * Добавляет новую категорию
     * @param category Категория для добавления
     * @return ID добавленной категории
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: BudgetCategoryEntity): Long

    /**
     * Обновляет существующую категорию
     * @param category Категория для обновления
     */
    @Update
    suspend fun updateCategory(category: BudgetCategoryEntity)

    /**
     * Удаляет категорию
     * @param category Категория для удаления
     */
    @Delete
    suspend fun deleteCategory(category: BudgetCategoryEntity)

    /**
     * Удаляет категорию по идентификатору
     * @param id Идентификатор категории для удаления
     */
    @Query("DELETE FROM budget_categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)

    /**
     * Удаляет все категории
     */
    @Query("DELETE FROM budget_categories")
    suspend fun deleteAllCategories()
} 