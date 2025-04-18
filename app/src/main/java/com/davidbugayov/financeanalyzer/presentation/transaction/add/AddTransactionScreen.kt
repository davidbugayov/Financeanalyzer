package com.davidbugayov.financeanalyzer.presentation.transaction.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel

/**
 * Экран добавления новой транзакции
 * Использует BaseTransactionScreen для отображения UI
 */
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = koinViewModel(),
    categoriesViewModel: CategoriesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    // Логируем открытие экрана добавления транзакции
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "add_transaction",
            screenClass = "AddTransactionScreen"
        )

        // Устанавливаем callback для навигации назад
        viewModel.navigateBackCallback = onNavigateBack
    }
    
    // Специальная обработка для доходов при переходе с экрана бюджета
    // (устанавливается forceExpense = false)
    LaunchedEffect(viewModel.state.value.forceExpense) {
        // Если выставлен флаг forceExpense = false, но состояние isExpense = true
        // Это означает, что мы должны показать форму для дохода 
        // (обычно при переходе с экрана бюджета)
        if (!viewModel.state.value.forceExpense && viewModel.state.value.isExpense) {
            // Принудительно переключаем на тип "Доход"
            viewModel.onEvent(AddTransactionEvent.ForceSetIncomeType)
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
        screenTitle = "Новая транзакция",
        buttonText = "Добавить"
    )
}