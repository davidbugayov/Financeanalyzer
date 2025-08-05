package com.davidbugayov.financeanalyzer.presentation.subcategories

import android.content.Context
import com.davidbugayov.financeanalyzer.domain.repository.SubcategoryRepository
import com.davidbugayov.financeanalyzer.presentation.categories.model.SubcategoryProvider

/**
 * UseCase для инициализации предустановленных подкатегорий
 */
class InitializeDefaultSubcategoriesUseCase(
    private val subcategoryRepository: SubcategoryRepository,
) {

    /**
     * Инициализирует предустановленные подкатегории
     * @param context Контекст приложения
     */
    suspend operator fun invoke(context: Context) {
        val defaultSubcategories = SubcategoryProvider.getAllDefaultSubcategories(context)

        defaultSubcategories.forEach { subcategory ->
            try {
                subcategoryRepository.insertSubcategory(subcategory)
            } catch (e: Exception) {
                // Игнорируем ошибки дублирования
            }
        }
    }
}
