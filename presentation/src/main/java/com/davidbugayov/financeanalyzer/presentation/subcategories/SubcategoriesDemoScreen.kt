package com.davidbugayov.financeanalyzer.presentation.subcategories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Демо-экран для показа функциональности подкатегорий
 */
@Composable
fun SubcategoriesDemoScreen(
    onNavigateToCategories: () -> Unit,
) {
    var selectedCategoryId by remember { mutableStateOf(1L) }
    var selectedCategoryName by remember { mutableStateOf("Продукты") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.subcategories_demo_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp),
        )

        Text(
            text = stringResource(R.string.subcategories_demo_description),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        // Быстрый доступ к подкатегориям популярных категорий
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SubcategoryDemoButton(
                text = "Продукты",
                onClick = {
                    selectedCategoryId = 1L
                    selectedCategoryName = "Продукты"
                },
            )

            SubcategoryDemoButton(
                text = "Рестораны",
                onClick = {
                    selectedCategoryId = 4L
                    selectedCategoryName = "Рестораны"
                },
            )

            SubcategoryDemoButton(
                text = "Транспорт",
                onClick = {
                    selectedCategoryId = 2L
                    selectedCategoryName = "Транспорт"
                },
            )

            SubcategoryDemoButton(
                text = "Развлечения",
                onClick = {
                    selectedCategoryId = 3L
                    selectedCategoryName = "Развлечения"
                },
            )
        }

        // Показываем подкатегории выбранной категории
        if (selectedCategoryId > 0) {
            SubcategoriesScreen(
                categoryId = selectedCategoryId,
                categoryName = selectedCategoryName,
            )
        }
    }
}

@Composable
private fun SubcategoryDemoButton(
    text: String,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Text(text = text)
    }
}
