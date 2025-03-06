package com.davidbugayov.financeanalyzer.presentation.add.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Модель для элемента категории.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
data class CategoryItem(
    val name: String,
    val icon: ImageVector
) 