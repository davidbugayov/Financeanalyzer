package com.davidbugayov.financeanalyzer.presentation.transaction.edit.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.EditTransactionEvent
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.platform.LocalContext

/**
 * Экран редактирования существующей транзакции
 */
@Composable
fun EditTransactionScreen(
    viewModel: EditTransactionViewModel = koinViewModel(),
    categoriesViewModel: CategoriesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    transactionId: String? = null
) {
    val context = LocalContext.current
    // Логируем открытие экрана редактирования транзакции
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "edit_transaction",
            screenClass = "EditTransactionScreen"
        )
        // Загружаем транзакцию для редактирования, если передан ID
        transactionId?.let { id ->
            if (id.isNotEmpty()) {
                viewModel.loadTransactionForEdit(id)
            }
        }
    }
    // Используем BaseTransactionScreen для отображения UI
    BaseTransactionScreen(
        viewModel = viewModel,
        categoriesViewModel = categoriesViewModel,
        onNavigateBack = onNavigateBack,
        screenTitle = "Редактирование транзакции",
        buttonText = "Сохранить",
        isEditMode = true,
        eventFactory = { eventName ->
            // Маппинг строкового имени на событие
            when (eventName) {
                "SubmitEdit" -> EditTransactionEvent.SubmitEdit
                "ShowDatePicker" -> EditTransactionEvent.ShowDatePicker
                "HideDatePicker" -> EditTransactionEvent.HideDatePicker
                "SetDate(date)" -> EditTransactionEvent.SetDate(java.util.Date()) // date будет передан внутри коллбэка
                "ShowSourcePicker" -> EditTransactionEvent.ShowSourcePicker
                "HideSourcePicker" -> EditTransactionEvent.HideSourcePicker
                "ShowCustomSourceDialog" -> EditTransactionEvent.ShowCustomSourceDialog
                "HideCustomSourceDialog" -> EditTransactionEvent.HideCustomSourceDialog
                "SetSource(source.name)" -> EditTransactionEvent.SetSource("") // значение будет заменено
                "SetSourceColor(source.color)" -> EditTransactionEvent.SetSourceColor(0) // значение будет заменено
                "ShowDeleteSourceConfirmDialog(source.name)" -> EditTransactionEvent.ShowDeleteSourceConfirmDialog("")
                "HideDeleteSourceConfirmDialog" -> EditTransactionEvent.HideDeleteSourceConfirmDialog
                "DeleteSource(state.sourceToDelete ?: \"\")" -> EditTransactionEvent.DeleteSource("")
                "ShowCustomCategoryDialog" -> EditTransactionEvent.ShowCustomCategoryDialog
                "HideCustomCategoryDialog" -> EditTransactionEvent.HideCustomCategoryDialog
                "AddCustomCategory(state.customCategory)" -> EditTransactionEvent.AddCustomCategory("")
                "SetCustomCategory(name)" -> EditTransactionEvent.SetCustomCategory("")
                "SetCustomSource(name)" -> EditTransactionEvent.SetCustomSource("")
                "AddCustomSource(state.customSource, state.sourceColor)" -> EditTransactionEvent.AddCustomSource("", 0)
                "ToggleTransactionType" -> EditTransactionEvent.ToggleTransactionType
                "SetAmount(it)" -> EditTransactionEvent.SetAmount("") // значение будет заменено
                "SetNote(note)" -> EditTransactionEvent.SetNote("") // значение будет заменено
                "SetCategory(category.name)" -> EditTransactionEvent.SetCategory("") // значение будет заменено
                else -> EditTransactionEvent.SubmitEdit // fallback
            }
        },
        submitEvent = EditTransactionEvent.SubmitEdit
    )
} 