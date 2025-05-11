package com.davidbugayov.financeanalyzer.presentation.transaction.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.base.defaultTransactionEventFactory
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * Экран добавления новой транзакции
 * Использует BaseTransactionScreen для отображения UI
 */
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToImport: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // Логируем открытие экрана добавления транзакции
    LaunchedEffect(Unit) {
        Timber.d("AddTransactionScreen: Screen opened, onNavigateToImport is ${if (onNavigateToImport != null) "provided" else "null"}")
        
        AnalyticsUtils.logScreenView(
            screenName = "add_transaction",
            screenClass = "AddTransactionScreen"
        )
    }

    // Обработка типа транзакции на основе forceExpense
    // Теперь по умолчанию forceExpense = true (расход)
    LaunchedEffect(state.forceExpense) {
        // Если forceExpense = true, устанавливаем расход
        // Если forceExpense = false, устанавливаем доход
        if (state.forceExpense) {
            viewModel.onEvent(BaseTransactionEvent.ForceSetExpenseType, context)
        } else {
            viewModel.onEvent(BaseTransactionEvent.ForceSetIncomeType, context)
        }
    }

    // Очищаем callback при выходе из композиции
    DisposableEffect(Unit) {
        onDispose {
            // No need to reset navigateBackCallback as it's not used in the new implementation
        }
    }

    // Используем BaseTransactionScreen для отображения UI
    BaseTransactionScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        screenTitle = stringResource(R.string.new_transaction_title),
        buttonText = stringResource(R.string.add_button_text),
        isEditMode = false,
        eventFactory = defaultTransactionEventFactory(false),
        submitEvent = BaseTransactionEvent.Submit,
        onNavigateToImport = onNavigateToImport
    )
}