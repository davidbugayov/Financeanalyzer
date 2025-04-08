package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import com.davidbugayov.financeanalyzer.domain.model.Money
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для работы с бюджетными категориями
 */
interface BudgetRepository {

    /**
     * Получает все бюджетные категории
     * @return Flow со списком всех бюджетных категорий
     */
    suspend fun getAllBudgetCategories(): List<BudgetCategory>

    /**
     * Получает бюджетную категорию по ID
     * @param id ID бюджетной категории
     * @return Бюджетная категория с указанным ID или null, если не найдена
     */
    suspend fun getBudgetCategoryById(id: String): BudgetCategory?

    /**
     * Добавляет новую бюджетную категорию
     * @param budgetCategory Бюджетная категория для добавления
     */
    suspend fun addBudgetCategory(budgetCategory: BudgetCategory)

    /**
     * Обновляет существующую бюджетную категорию
     * @param budgetCategory Обновленная бюджетная категория
     */
    suspend fun updateBudgetCategory(budgetCategory: BudgetCategory)

    /**
     * Удаляет бюджетную категорию
     * @param budgetCategory Бюджетная категория для удаления
     */
    suspend fun deleteBudgetCategory(budgetCategory: BudgetCategory)

    /**
     * Удаляет бюджетную категорию по ID
     * @param id ID бюджетной категории для удаления
     */
    suspend fun deleteBudgetCategoryById(id: String)

    /**
     * Удаляет все бюджетные категории
     */
    suspend fun deleteAllBudgetCategories()

    /**
     * Обновляет потраченную сумму для бюджетной категории
     * @param id ID бюджетной категории
     * @param spent Новая потраченная сумма
     */
    suspend fun updateSpentAmount(id: String, spent: Money)

    /**
     * Проверяет, есть ли бюджетные категории
     * @return true, если есть хотя бы одна бюджетная категория, иначе false
     */
    suspend fun hasBudgetCategories(): Boolean
} 