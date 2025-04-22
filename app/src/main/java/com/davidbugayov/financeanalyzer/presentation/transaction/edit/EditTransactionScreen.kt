package com.davidbugayov.financeanalyzer.presentation.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.base.defaultTransactionEventFactory
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel

/**
 * Экран редактирования существующей транзакции
 */
@Composable
fun EditTransactionScreen(
    viewModel: EditTransactionViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    transactionId: String? = null
) {

    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "edit_transaction",
            screenClass = "EditTransactionScreen"
        )
        transactionId?.let {
            if (it.isNotEmpty()) {
                viewModel.loadTransactionForEditById(it)
            }
        }
    }

    BaseTransactionScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        screenTitle = "Редактирование транзакции",
        buttonText = "Сохранить",
        isEditMode = true,
        eventFactory = defaultTransactionEventFactory(true),
        submitEvent = BaseTransactionEvent.SubmitEdit
    )
} 