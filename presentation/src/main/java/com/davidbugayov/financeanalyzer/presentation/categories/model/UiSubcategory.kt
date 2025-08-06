package com.davidbugayov.financeanalyzer.presentation.categories.model

import androidx.compose.ui.graphics.Color
import com.davidbugayov.financeanalyzer.domain.model.Subcategory

/**
 * UI модель для представления подкатегории
 */
data class UiSubcategory(
    val id: Long,
    val name: String,
    val categoryId: Long,
    val count: Int = 0,
    val isCustom: Boolean = false,
    val color: Color = Color.Gray,
    val original: Subcategory? = null,
) {
    companion object {
        /**
         * Создает UI подкатегорию из доменной модели
         */
        fun fromDomain(
            subcategory: Subcategory,
            color: Color = Color.Gray,
        ): UiSubcategory {
            return UiSubcategory(
                id = subcategory.id,
                name = subcategory.name,
                categoryId = subcategory.categoryId,
                count = subcategory.count,
                isCustom = subcategory.isCustom,
                color = color,
                original = subcategory,
            )
        }

        /**
         * Создает кастомную UI подкатегорию
         */
        fun custom(
            name: String,
            categoryId: Long,
            color: Color = Color.Gray,
        ): UiSubcategory {
            return UiSubcategory(
                id = System.currentTimeMillis(),
                name = name,
                categoryId = categoryId,
                isCustom = true,
                color = color,
            )
        }
    }
}
