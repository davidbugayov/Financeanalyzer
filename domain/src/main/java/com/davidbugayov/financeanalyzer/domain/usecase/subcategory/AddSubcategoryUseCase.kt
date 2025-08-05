package com.davidbugayov.financeanalyzer.domain.usecase.subcategory

import com.davidbugayov.financeanalyzer.domain.model.Subcategory
import com.davidbugayov.financeanalyzer.domain.repository.SubcategoryRepository

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
        val subcategory = Subcategory.create(
            name = name,
            categoryId = categoryId,
            isCustom = true,
        )
        return subcategoryRepository.insertSubcategory(subcategory)
    }
} 