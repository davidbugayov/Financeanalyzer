package com.davidbugayov.financeanalyzer.shared.usecase.subcategory

import com.davidbugayov.financeanalyzer.shared.repository.SubcategoryRepository

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
