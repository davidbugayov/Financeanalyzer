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
    override suspend fun getSubcategoryById(id: Long): SharedSubcategory? {
        return domainRepo.getSubcategoryById(id)?.toShared()
    }

    override fun getSubcategoriesByCategoryId(categoryId: Long): Flow<List<SharedSubcategory>> {
        return domainRepo.observeSubcategoriesByCategoryId(categoryId).map { list ->
            list.map { it.toShared() }
        }
    }

    override suspend fun insertSubcategory(subcategory: SharedSubcategory): Long {
        return domainRepo.insertSubcategory(subcategory.toDomain())
    }

    override suspend fun deleteSubcategoryById(subcategoryId: Long) {
        domainRepo.deleteSubcategoryById(subcategoryId)
    }
}

private fun DomainSubcategory.toShared(): SharedSubcategory {
    return SharedSubcategory(
        id = this.id,
        categoryId = this.categoryId,
        name = this.name,
        isCustom = this.isCustom,
    )
}

private fun SharedSubcategory.toDomain(): DomainSubcategory {
    return DomainSubcategory(
        id = this.id,
        categoryId = this.categoryId,
        name = this.name,
        isCustom = this.isCustom,
    )
}
