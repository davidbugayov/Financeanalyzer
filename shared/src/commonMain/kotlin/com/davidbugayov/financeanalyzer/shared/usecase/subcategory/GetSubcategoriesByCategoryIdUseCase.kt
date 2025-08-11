package com.davidbugayov.financeanalyzer.shared.usecase.subcategory

import com.davidbugayov.financeanalyzer.shared.model.Subcategory
import com.davidbugayov.financeanalyzer.shared.repository.SubcategoryRepository
import kotlinx.coroutines.flow.Flow

class GetSubcategoriesByCategoryIdUseCase(
    private val subcategoryRepository: SubcategoryRepository,
) {
    operator fun invoke(categoryId: Long): Flow<List<Subcategory>> =
        subcategoryRepository.observeSubcategoriesByCategoryId(categoryId)
}


