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
 * Главный экран для демонстрации функциональности подкатегорий
 */
@Composable
fun SubcategoriesMainScreen() {
    var currentScreen by remember { mutableStateOf(SubcategoriesScreenType.DEMO) }
    var selectedCategoryId by remember { mutableStateOf(1L) }
    var selectedCategoryName by remember { mutableStateOf("Продукты") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Заголовок
        Text(
            text = stringResource(R.string.subcategories_main_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        // Навигационные кнопки
        SubcategoriesNavigationButtons(
            currentScreen = currentScreen,
            onScreenChange = { currentScreen = it },
            onCategorySelect = { id, name ->
                selectedCategoryId = id
                selectedCategoryName = name
                currentScreen = SubcategoriesScreenType.DETAILS
            },
        )

        // Контент экрана
        when (currentScreen) {
            SubcategoriesScreenType.DEMO -> {
                SubcategoriesDemoContent(
                    onCategorySelect = { id, name ->
                        selectedCategoryId = id
                        selectedCategoryName = name
                        currentScreen = SubcategoriesScreenType.DETAILS
                    },
                )
            }
            SubcategoriesScreenType.DETAILS -> {
                SubcategoriesScreen(
                    categoryId = selectedCategoryId,
                    categoryName = selectedCategoryName,
                )
            }
            SubcategoriesScreenType.INIT -> {
                InitializeSubcategoriesScreen(
                    onInitializationComplete = {
                        currentScreen = SubcategoriesScreenType.DEMO
                    },
                )
            }
        }
    }
}

@Composable
private fun SubcategoriesNavigationButtons(
    currentScreen: SubcategoriesScreenType,
    onScreenChange: (SubcategoriesScreenType) -> Unit,
    onCategorySelect: (Long, String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 16.dp),
    ) {
        androidx.compose.material3.Button(
            onClick = { onScreenChange(SubcategoriesScreenType.DEMO) },
            enabled = currentScreen != SubcategoriesScreenType.DEMO,
        ) {
            Text(stringResource(R.string.subcategories_nav_demo))
        }

        androidx.compose.material3.Button(
            onClick = { onScreenChange(SubcategoriesScreenType.INIT) },
            enabled = currentScreen != SubcategoriesScreenType.INIT,
        ) {
            Text(stringResource(R.string.subcategories_nav_init))
        }
    }
}

@Composable
private fun SubcategoriesDemoContent(
    onCategorySelect: (Long, String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.subcategories_demo_description),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        SubcategoryDemoButton(
            text = "Продукты",
            onClick = { onCategorySelect(1L, "Продукты") },
        )

        SubcategoryDemoButton(
            text = "Рестораны",
            onClick = { onCategorySelect(4L, "Рестораны") },
        )

        SubcategoryDemoButton(
            text = "Транспорт",
            onClick = { onCategorySelect(2L, "Транспорт") },
        )

        SubcategoryDemoButton(
            text = "Развлечения",
            onClick = { onCategorySelect(3L, "Развлечения") },
        )
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

enum class SubcategoriesScreenType {
    DEMO,
    DETAILS,
    INIT,
}
