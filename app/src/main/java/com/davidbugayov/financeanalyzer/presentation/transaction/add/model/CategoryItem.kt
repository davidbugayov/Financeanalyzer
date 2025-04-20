package com.davidbugayov.financeanalyzer.presentation.transaction.add.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Модель для представления категории с иконкой.
 */
data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val wasSelected: Boolean = false
) 