package com.davidbugayov.financeanalyzer.domain.contracts

import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.domain.model.TransactionType
import com.davidbugayov.financeanalyzer.shared.model.Money
import kotlinx.coroutines.flow.Flow

/**
 * Contract interface for Category repository operations.
 * Manages categories and their usage statistics.
 */
interface CategoryRepositoryContract {

    /**
     * Retrieves all categories as a Flow
     */
    fun getAllCategories(): Flow<List<Category>>

    /**
     * Retrieves all categories as a suspend function
     */
    suspend fun getAllCategoriesList(): List<Category>

    /**
     * Retrieves categories by transaction type
     */
    suspend fun getCategoriesByType(type: TransactionType): List<Category>

    /**
     * Retrieves a specific category by ID
     */
    suspend fun getCategoryById(id: Long): Category?

    /**
     * Retrieves a category by name
     */
    suspend fun getCategoryByName(name: String): Category?

    /**
     * Creates a new category
     */
    suspend fun createCategory(category: Category): Long

    /**
     * Updates an existing category
     */
    suspend fun updateCategory(category: Category)

    /**
     * Deletes a category by ID
     */
    suspend fun deleteCategory(id: Long)

    /**
     * Gets categories sorted by usage frequency
     */
    suspend fun getCategoriesSortedByUsage(): List<Category>

    /**
     * Gets the most used categories (top N)
     */
    suspend fun getMostUsedCategories(limit: Int = 5): List<Category>

    /**
     * Gets categories with their total amounts for a period
     */
    suspend fun getCategoriesWithAmounts(
        startDate: java.util.Date,
        endDate: java.util.Date
    ): Map<Category, Money>

    /**
     * Increments usage count for a category
     */
    suspend fun incrementCategoryUsage(categoryId: Long)

    /**
     * Resets usage statistics for all categories
     */
    suspend fun resetCategoryUsage()

    /**
     * Checks if a category with the given name exists
     */
    suspend fun categoryExists(name: String): Boolean

    /**
     * Gets the total count of categories
     */
    suspend fun getCategoryCount(): Int

    /**
     * Gets the total count of categories by type
     */
    suspend fun getCategoryCountByType(type: TransactionType): Int

    /**
     * Validates category data before operations
     */
    suspend fun validateCategory(category: Category): Boolean

    /**
     * Gets default categories for a transaction type
     */
    suspend fun getDefaultCategories(type: TransactionType): List<Category>
}
