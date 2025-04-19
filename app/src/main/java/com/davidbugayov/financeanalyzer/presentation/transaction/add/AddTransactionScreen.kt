package com.davidbugayov.financeanalyzer.presentation.transaction.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import java.util.Date
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

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
        eventFactory = { eventData ->
            when (eventData) {
                is Source -> AddTransactionEvent.SetSourceColor(eventData.color)
                is CategoryItem -> AddTransactionEvent.SetCategory(eventData.name)
                is Date -> AddTransactionEvent.SetDate(eventData)
                is String -> when (eventData) {
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
                    else -> AddTransactionEvent.Submit
                }
                is Pair<*, *> -> when (eventData.first as? String) {
                    "DeleteSourceConfirm" -> {
                        val source = eventData.second as? Source
                        AddTransactionEvent.ShowDeleteSourceConfirmDialog(source?.name ?: "")
                    }
                    "DeleteCategoryConfirm" -> {
                        val category = eventData.second as? CategoryItem
                        AddTransactionEvent.ShowDeleteCategoryConfirmDialog(category?.name ?: "")
                    }
                    "SetAmount" -> AddTransactionEvent.SetAmount(eventData.second as String)
                    "SetNote" -> AddTransactionEvent.SetNote(eventData.second as String)
                    "SetCustomCategoryText" -> AddTransactionEvent.SetCustomCategory(eventData.second as String)
                    "AddCustomCategoryConfirm" -> AddTransactionEvent.AddCustomCategory(eventData.second as String)
                    "DeleteCategoryConfirmActual" -> AddTransactionEvent.DeleteCategory(eventData.second as String)
                    "DeleteSourceConfirmActual" -> AddTransactionEvent.DeleteSource(eventData.second as String)
                    "SetCustomSourceName" -> AddTransactionEvent.SetCustomSource(eventData.second as String)
                    "SetCustomSourceColor" -> AddTransactionEvent.SetSourceColor(eventData.second as Int)
                    "SetSourceColor" -> AddTransactionEvent.SetSourceColor(eventData.second as Int)
                    else -> AddTransactionEvent.Submit
                }
                is Triple<*, *, *> -> when (eventData.first as? String) {
                    "AddCustomSourceConfirm" -> {
                        val name = eventData.second as String
                        val color = eventData.third as Int
                        AddTransactionEvent.AddCustomSource(name, color)
                    }
                    "SelectWallet" -> {
                        val walletId = eventData.second as String
                        val selected = eventData.third as Boolean
                        AddTransactionEvent.SelectWallet(walletId, selected)
                    }
                    else -> AddTransactionEvent.Submit
                }
                else -> AddTransactionEvent.Submit
            }
        },
        submitEvent = AddTransactionEvent.Submit
    )
}