package com.davidbugayov.financeanalyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.davidbugayov.financeanalyzer.data.local.entity.BudgetCategoryEntity

/**
 * DAO (Data Access Object) для работы с бюджетными категориями в базе данных Room.
 * Предоставляет методы для выполнения CRUD операций с бюджетными категориями.
 */
@Dao
interface BudgetCategoryDao {

    /**
     * Получает все бюджетные категории из базы данных
     * @return Список всех бюджетных категорий
     */
    @Query("SELECT * FROM budget_categories ORDER BY name ASC")
    suspend fun getAllBudgetCategories(): List<BudgetCategoryEntity>

    /**
     * Получает бюджетную категорию по ID
     * @param id ID бюджетной категории
     * @return Бюджетная категория с указанным ID или null, если не найдена
     */
    @Query("SELECT * FROM budget_categories WHERE id = :id")
    suspend fun getBudgetCategoryById(id: String): BudgetCategoryEntity?

    /**
     * Добавляет новую бюджетную категорию в базу данных
     * @param budgetCategory Бюджетная категория для добавления
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetCategory(budgetCategory: BudgetCategoryEntity)

    /**
     * Обновляет существующую бюджетную категорию в базе данных
     * @param budgetCategory Обновленная бюджетная категория
     */
    @Update
    suspend fun updateBudgetCategory(budgetCategory: BudgetCategoryEntity)

    /**
     * Удаляет бюджетную категорию из базы данных
     * @param budgetCategory Бюджетная категория для удаления
     */
    @Delete
    suspend fun deleteBudgetCategory(budgetCategory: BudgetCategoryEntity)

    /**
     * Удаляет бюджетную категорию по ID
     * @param id ID бюджетной категории для удаления
     */
    @Query("DELETE FROM budget_categories WHERE id = :id")
    suspend fun deleteBudgetCategoryById(id: String)

    /**
     * Удаляет все бюджетные категории из базы данных
     */
    @Query("DELETE FROM budget_categories")
    suspend fun deleteAllBudgetCategories()

    /**
     * Обновляет потраченную сумму для указанной категории
     * @param id ID бюджетной категории
     * @param spent Новое значение потраченной суммы
     */
    @Query("UPDATE budget_categories SET spent = :spent WHERE id = :id")
    suspend fun updateSpentAmount(id: String, spent: Double)

    /**
     * Проверяет, существуют ли бюджетные категории в базе данных
     * @return true, если есть хотя бы одна категория, иначе false
     */
    @Query("SELECT EXISTS(SELECT 1 FROM budget_categories LIMIT 1)")
    suspend fun hasBudgetCategories(): Boolean
} 