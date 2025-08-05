package com.davidbugayov.financeanalyzer.domain.usecase.subcategory

import com.davidbugayov.financeanalyzer.domain.repository.SubcategoryRepository

/**
 * UseCase для удаления подкатегории
 */
class DeleteSubcategoryUseCase(
    private val subcategoryRepository: SubcategoryRepository,
) {

    /**
     * Удаляет подкатегорию по ID
     * @param subcategoryId ID подкатегории для удаления
     */
    suspend operator fun invoke(subcategoryId: Long) {
        subcategoryRepository.deleteSubcategoryById(subcategoryId)
    }
} 