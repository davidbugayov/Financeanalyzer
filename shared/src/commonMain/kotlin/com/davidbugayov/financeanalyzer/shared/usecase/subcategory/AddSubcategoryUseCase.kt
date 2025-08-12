package com.davidbugayov.financeanalyzer.shared.usecase.subcategory

import com.davidbugayov.financeanalyzer.shared.model.Subcategory
import com.davidbugayov.financeanalyzer.shared.repository.SubcategoryRepository

/**
 * UseCase для добавления подкатегории
 */
class AddSubcategoryUseCase(
    private val subcategoryRepository: SubcategoryRepository,
) {

    /**
     * Добавляет новую подкатегорию
     * @param name Название подкатегории
     * @param categoryId ID родительской категории
     * @return ID добавленной подкатегории
     */
    suspend operator fun invoke(name: String, categoryId: Long): Long {
        val subcategory = Subcategory(
            id = 0, // будет присвоен при сохранении
            name = name,
            categoryId = categoryId,
            isCustom = true,
        )
        return subcategoryRepository.insertSubcategory(subcategory)
    }
}
