package com.davidbugayov.financeanalyzer.domain.usecase.subcategory

import com.davidbugayov.financeanalyzer.domain.model.Subcategory
import com.davidbugayov.financeanalyzer.domain.repository.SubcategoryRepository

/**
 * UseCase для получения подкатегории по ID
 */
class GetSubcategoryByIdUseCase(
    private val subcategoryRepository: SubcategoryRepository,
) {

    /**
     * Получает подкатегорию по ID
     * @param id ID подкатегории
     * @return Подкатегория или null, если не найдена
     */
    suspend operator fun invoke(id: Long): Subcategory? {
        return subcategoryRepository.getSubcategoryById(id)
    }
} 