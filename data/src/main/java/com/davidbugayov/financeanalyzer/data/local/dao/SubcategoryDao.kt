package com.davidbugayov.financeanalyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.davidbugayov.financeanalyzer.data.local.entity.SubcategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) для работы с подкатегориями в базе данных Room.
 * Предоставляет методы для выполнения CRUD операций с подкатегориями.
 */
@Dao
interface SubcategoryDao {

    /**
     * Получает все подкатегории из базы данных
     * @return Список всех подкатегорий
     */
    @Query("SELECT * FROM subcategories ORDER BY count DESC, name ASC")
    suspend fun getAllSubcategories(): List<SubcategoryEntity>

    /**
     * Поток всех подкатегорий из базы данных
     */
    @Query("SELECT * FROM subcategories ORDER BY count DESC, name ASC")
    fun observeAllSubcategories(): Flow<List<SubcategoryEntity>>

    /**
     * Получает подкатегории для конкретной категории
     * @param categoryId ID родительской категории
     * @return Список подкатегорий для указанной категории
     */
    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId ORDER BY count DESC, name ASC")
    suspend fun getSubcategoriesByCategoryId(categoryId: Long): List<SubcategoryEntity>

    /**
     * Поток подкатегорий для конкретной категории
     * @param categoryId ID родительской категории
     */
    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId ORDER BY count DESC, name ASC")
    fun observeSubcategoriesByCategoryId(categoryId: Long): Flow<List<SubcategoryEntity>>

    /**
     * Получает подкатегорию по ID
     * @param id ID подкатегории
     * @return Подкатегория с указанным ID или null, если не найдена
     */
    @Query("SELECT * FROM subcategories WHERE id = :id")
    suspend fun getSubcategoryById(id: Long): SubcategoryEntity?

    /**
     * Добавляет новую подкатегорию в базу данных
     * @param subcategory Подкатегория для добавления
     * @return ID добавленной подкатегории
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubcategory(subcategory: SubcategoryEntity): Long

    /**
     * Обновляет существующую подкатегорию в базе данных
     * @param subcategory Обновленная подкатегория
     */
    @Update
    suspend fun updateSubcategory(subcategory: SubcategoryEntity)

    /**
     * Удаляет подкатегорию из базы данных
     * @param subcategory Подкатегория для удаления
     */
    @Delete
    suspend fun deleteSubcategory(subcategory: SubcategoryEntity)

    /**
     * Удаляет подкатегорию по ID
     * @param id ID подкатегории для удаления
     */
    @Query("DELETE FROM subcategories WHERE id = :id")
    suspend fun deleteSubcategoryById(id: Long)

    /**
     * Удаляет все подкатегории для конкретной категории
     * @param categoryId ID родительской категории
     */
    @Query("DELETE FROM subcategories WHERE categoryId = :categoryId")
    suspend fun deleteSubcategoriesByCategoryId(categoryId: Long)

    /**
     * Увеличивает счетчик использования подкатегории
     * @param id ID подкатегории
     */
    @Query("UPDATE subcategories SET count = count + 1 WHERE id = :id")
    suspend fun incrementSubcategoryCount(id: Long)
} 