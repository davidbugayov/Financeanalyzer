package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.local.dao.BudgetCategoryDao
import com.davidbugayov.financeanalyzer.data.local.entity.BudgetCategoryEntity
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import com.davidbugayov.financeanalyzer.domain.repository.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID

/**
 * Реализация репозитория для работы с бюджетными категориями.
 * Использует Room DAO для доступа к данным.
 *
 * @param dao DAO для работы с бюджетными категориями.
 */
class BudgetRepositoryImpl(
    private val dao: BudgetCategoryDao
) : BudgetRepository {

    /**
     * Получает все бюджетные категории.
     * @return Flow со списком бюджетных категорий.
     */
    override fun getAllCategories(): Flow<List<BudgetCategory>> {
        return dao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Получает категорию по идентификатору.
     * @param id Идентификатор категории.
     * @return Категория или null, если категория не найдена.
     */
    override suspend fun getCategoryById(id: String): BudgetCategory? =
        withContext(Dispatchers.IO) {
            try {
                val entity = dao.getCategoryById(id)
                entity?.toDomain()
            } catch (e: Exception) {
                Timber.e(e, "Error getting budget category by ID '$id': ${e.message}")
                null
            }
        }

    /**
     * Добавляет новую категорию бюджета.
     * @param category Категория для добавления.
     * @return ID добавленной категории.
     */
    override suspend fun addCategory(category: BudgetCategory): String =
        withContext(Dispatchers.IO) {
            try {
                // Создаем новый ID, если он не указан или пустой
                val categoryId = if (category.id.isBlank()) {
                    UUID.randomUUID().toString()
                } else {
                    category.id
                }

                // Создаем копию с новым ID
                val categoryWithId = category.copy(id = categoryId)

                // Преобразуем и сохраняем в базу
                val entity = BudgetCategoryEntity.fromDomain(categoryWithId)
                dao.insertCategory(entity)

                categoryId
            } catch (e: Exception) {
                Timber.e(e, "Error adding budget category: ${e.message}")
                throw e
            }
        }

    /**
     * Обновляет существующую категорию.
     * @param category Категория для обновления.
     */
    override suspend fun updateCategory(category: BudgetCategory) = withContext(Dispatchers.IO) {
        try {
            val entity = BudgetCategoryEntity.fromDomain(category)
            dao.updateCategory(entity)
        } catch (e: Exception) {
            Timber.e(e, "Error updating budget category: ${e.message}")
            throw e
        }
    }

    /**
     * Удаляет категорию.
     * @param id Идентификатор категории для удаления.
     */
    override suspend fun deleteCategory(id: String) = withContext(Dispatchers.IO) {
        try {
            dao.deleteCategoryById(id)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting budget category: ${e.message}")
            throw e
        }
    }

    /**
     * Удаляет все категории.
     */
    override suspend fun deleteAllCategories() = withContext(Dispatchers.IO) {
        try {
            dao.deleteAllCategories()
        } catch (e: Exception) {
            Timber.e(e, "Error deleting all budget categories: ${e.message}")
            throw e
        }
    }
} 