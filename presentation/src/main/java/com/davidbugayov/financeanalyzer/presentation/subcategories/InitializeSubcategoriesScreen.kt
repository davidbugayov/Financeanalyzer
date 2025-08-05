package com.davidbugayov.financeanalyzer.presentation.subcategories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.ui.R
import org.koin.compose.koinInject

/**
 * Экран для инициализации предустановленных подкатегорий
 */
@Composable
fun InitializeSubcategoriesScreen(
    onInitializationComplete: () -> Unit,
    viewModel: SubcategoriesViewModel = koinInject(),
) {
    var isInitializing by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isInitialized) {
            isInitializing = true
            try {
                // Здесь можно вызвать UseCase для инициализации
                // viewModel.initializeDefaultSubcategories()
                isInitialized = true
                onInitializationComplete()
            } catch (e: Exception) {
                // Обработка ошибки
            } finally {
                isInitializing = false
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (isInitializing) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Text(
                    text = stringResource(R.string.initializing_subcategories),
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Text(
                    text = stringResource(R.string.subcategories_ready),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                Button(
                    onClick = onInitializationComplete,
                ) {
                    Text(stringResource(R.string.continue_to_app))
                }
            }
        }
    }
}
