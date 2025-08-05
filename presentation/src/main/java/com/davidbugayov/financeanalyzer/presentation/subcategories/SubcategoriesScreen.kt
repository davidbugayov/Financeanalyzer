package com.davidbugayov.financeanalyzer.presentation.subcategories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiSubcategory
import com.davidbugayov.financeanalyzer.ui.R
import org.koin.compose.koinInject

/**
 * Экран для отображения подкатегорий
 */
@Composable
fun SubcategoriesScreen(
    categoryId: Long,
    categoryName: String,
    viewModel: SubcategoriesViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryId) {
        viewModel.loadSubcategories(categoryId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.subcategories_for_category, categoryName),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Поле для добавления новой подкатегории
        var newSubcategoryName by remember { mutableStateOf("") }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = newSubcategoryName,
                onValueChange = { newSubcategoryName = it },
                label = { Text(stringResource(R.string.subcategory_name)) },
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = {
                    if (newSubcategoryName.isNotBlank()) {
                        viewModel.addSubcategory(newSubcategoryName)
                        newSubcategoryName = ""
                    }
                },
                enabled = newSubcategoryName.isNotBlank(),
            ) {
                Text(stringResource(R.string.add_subcategory))
            }
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }
            uiState.subcategories.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_subcategories),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.subcategories) { subcategory ->
                        SubcategoryItem(
                            subcategory = subcategory,
                            onDelete = { viewModel.deleteSubcategory(subcategory.id) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Элемент подкатегории
 */
@Composable
private fun SubcategoryItem(
    subcategory: UiSubcategory,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = subcategory.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (subcategory.count > 0) {
                    Text(
                        text = stringResource(R.string.usage_count, subcategory.count),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (subcategory.isCustom) {
                Text(
                    text = stringResource(R.string.custom),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
