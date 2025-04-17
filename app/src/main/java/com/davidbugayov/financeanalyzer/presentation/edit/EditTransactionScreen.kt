package com.davidbugayov.financeanalyzer.presentation.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.davidbugayov.financeanalyzer.presentation.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel

/**
 * Экран редактирования существующей транзакции
 * Использует BaseTransactionScreen для отображения UI
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