package com.davidbugayov.financeanalyzer.presentation.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.davidbugayov.financeanalyzer.presentation.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * Экран редактирования существующей транзакции
 * Временная имплементация для устранения ошибок компиляции
 */
@Composable
fun EditTransactionScreen(
    viewModel: AddTransactionViewModel = koinViewModel(),
    categoriesViewModel: CategoriesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    transactionId: String? = null
) {
    // Логируем открытие экрана редактирования транзакции
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "edit_transaction",
            screenClass = "EditTransactionScreen"
        )

        // Устанавливаем callback для навигации назад
        viewModel.navigateBackCallback = onNavigateBack
        
        // Загружаем транзакцию для редактирования, если передан ID
        transactionId?.let { id ->
            if (id.isNotEmpty()) {
                viewModel.loadTransactionForEdit(id)
            }
        }
    }
    
    // Очищаем callback при выходе из композиции
    DisposableEffect(Unit) {
        onDispose {
            viewModel.navigateBackCallback = null
        }
    }

    // Используем BaseTransactionScreen для отображения UI
    BaseTransactionScreen(
        viewModel = viewModel,
        categoriesViewModel = categoriesViewModel,
        onNavigateBack = onNavigateBack,
        screenTitle = "Редактирование транзакции",
        buttonText = "Сохранить",
        isEditMode = true
    )
} 