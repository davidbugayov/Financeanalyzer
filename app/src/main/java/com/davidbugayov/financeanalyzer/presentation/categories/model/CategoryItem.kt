package com.davidbugayov.financeanalyzer.presentation.categories.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.davidbugayov.financeanalyzer.domain.model.Category

/**
 * Модель для представления категории в пользовательском интерфейсе
 *
 * @property name Название категории
 * @property count Количество использований категории (для сортировки по популярности)
 * @property isCustom Флаг, указывающий, является ли категория кастомной (созданной пользователем)
 */
data class CategoryItem(
    val name: String,
    val count: Int = 0,
    val image: ImageVector? = null,
    val isCustom: Boolean = false
)