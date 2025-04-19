package com.davidbugayov.financeanalyzer.presentation.transaction.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.platform.LocalContext

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
    val context = LocalContext.current
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
            viewModel.onEvent(AddTransactionEvent.ForceSetIncomeType, context)
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
        buttonText = "Добавить",
        isEditMode = false,
        eventFactory = { eventName ->
            // Маппинг строкового имени на событие (пример для AddTransactionEvent)
            when (eventName) {
                "ShowDatePicker" -> AddTransactionEvent.ShowDatePicker
                "ToggleTransactionType" -> AddTransactionEvent.ToggleTransactionType
                "ShowCustomSourceDialog" -> AddTransactionEvent.ShowCustomSourceDialog
                "ShowCustomCategoryDialog" -> AddTransactionEvent.ShowCustomCategoryDialog
                "ToggleAddToWallet" -> AddTransactionEvent.ToggleAddToWallet
                "ShowWalletSelector" -> AddTransactionEvent.ShowWalletSelector
                "ClearError" -> AddTransactionEvent.ClearError
                "HideSuccessDialog" -> AddTransactionEvent.HideSuccessDialog
                "HideDatePicker" -> AddTransactionEvent.HideDatePicker
                "HideCategoryPicker" -> AddTransactionEvent.HideCategoryPicker
                "HideCustomCategoryDialog" -> AddTransactionEvent.HideCustomCategoryDialog
                "HideDeleteCategoryConfirmDialog" -> AddTransactionEvent.HideDeleteCategoryConfirmDialog
                "HideDeleteSourceConfirmDialog" -> AddTransactionEvent.HideDeleteSourceConfirmDialog
                "HideSourcePicker" -> AddTransactionEvent.HideSourcePicker
                "HideCustomSourceDialog" -> AddTransactionEvent.HideCustomSourceDialog
                "HideColorPicker" -> AddTransactionEvent.HideColorPicker
                "HideWalletSelector" -> AddTransactionEvent.HideWalletSelector
                else -> AddTransactionEvent.ShowDatePicker // fallback, для теста
            }
        },
        submitEvent = AddTransactionEvent.Submit
    )
}