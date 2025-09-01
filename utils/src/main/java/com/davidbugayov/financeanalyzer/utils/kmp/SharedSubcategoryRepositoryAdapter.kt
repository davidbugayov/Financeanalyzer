package com.davidbugayov.financeanalyzer.utils.kmp

import com.davidbugayov.financeanalyzer.domain.model.Subcategory as DomainSubcategory
import com.davidbugayov.financeanalyzer.domain.repository.SubcategoryRepository as DomainSubcategoryRepository
import com.davidbugayov.financeanalyzer.shared.model.Subcategory as SharedSubcategory
import com.davidbugayov.financeanalyzer.shared.repository.SubcategoryRepository as SharedSubcategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Адаптер domain SubcategoryRepository под KMP SharedSubcategoryRepository.
 */
class SharedSubcategoryRepositoryAdapter(
    private val domainRepo: DomainSubcategoryRepository,
) : SharedSubcategoryRepository {
    // Category methods - simplified implementations
    override fun getAllCategories(): kotlinx.coroutines.flow.Flow<List<com.davidbugayov.financeanalyzer.shared.model.Category>> =
        kotlinx.coroutines.flow.flow { emit(emptyList()) }

    override suspend fun getCategoryById(id: Long): com.davidbugayov.financeanalyzer.shared.model.Category? = null

    override suspend fun createCategory(category: com.davidbugayov.financeanalyzer.shared.model.Category): Long = category.id

    override suspend fun updateCategory(category: com.davidbugayov.financeanalyzer.shared.model.Category) {
        // Not implemented
    }

    override suspend fun deleteCategory(id: Long) {
        // Not implemented
    }

    override suspend fun getCategoriesByType(isExpense: Boolean): List<com.davidbugayov.financeanalyzer.shared.model.Category> = emptyList()

    override suspend fun clearAllCategories() {
        // Not implemented
    }

    // Legacy subcategory methods
    override suspend fun getSubcategoryById(id: Long): SharedSubcategory? =
        domainRepo.getSubcategoryById(id)?.toShared()

    override fun getSubcategoriesByCategoryId(categoryId: Long): Flow<List<SharedSubcategory>> =
        domainRepo.observeSubcategoriesByCategoryId(categoryId).map { list ->
            list.map { it.toShared() }
        }

    override suspend fun insertSubcategory(subcategory: SharedSubcategory): Long =
        domainRepo.insertSubcategory(subcategory.toDomain())

    override suspend fun deleteSubcategoryById(subcategoryId: Long) {
        domainRepo.deleteSubcategoryById(subcategoryId)
    }
}

private fun DomainSubcategory.toShared(): SharedSubcategory =
    SharedSubcategory(
        id = this.id,
        categoryId = this.categoryId,
        name = this.name,
        isCustom = this.isCustom,
    )

private fun SharedSubcategory.toDomain(): DomainSubcategory =
    DomainSubcategory(
        id = this.id,
        categoryId = this.categoryId,
        name = this.name,
        isCustom = this.isCustom,
    )
