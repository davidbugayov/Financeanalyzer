package com.davidbugayov.financeanalyzer.shared.usecase.subcategory

import com.davidbugayov.financeanalyzer.shared.model.Subcategory
import com.davidbugayov.financeanalyzer.shared.repository.SubcategoryRepository

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
