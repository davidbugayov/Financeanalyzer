package com.davidbugayov.financeanalyzer.domain.usecase.subcategory

import com.davidbugayov.financeanalyzer.domain.model.Subcategory
import com.davidbugayov.financeanalyzer.domain.repository.SubcategoryRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для получения подкатегорий по ID родительской категории
 */
class GetSubcategoriesByCategoryIdUseCase(
    private val subcategoryRepository: SubcategoryRepository,
) {

    /**
     * Получает подкатегории для конкретной категории
     * @param categoryId ID родительской категории
     * @return Поток подкатегорий
     */
    operator fun invoke(categoryId: Long): Flow<List<Subcategory>> {
        return subcategoryRepository.observeSubcategoriesByCategoryId(categoryId)
    }
} 