package com.davidbugayov.financeanalyzer.feature.transaction.edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.feature.transaction.base.baseTransactionScreen
import com.davidbugayov.financeanalyzer.feature.transaction.base.defaultTransactionEventFactory
import com.davidbugayov.financeanalyzer.feature.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.ui.R as UiR
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * Экран редактирования существующей транзакции
 */
@Composable
fun editTransactionScreen(
    viewModel: EditTransactionViewModel = koinViewModel(),
    transactionId: String? = null,
) {
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "edit_transaction",
            screenClass = "EditTransactionScreen",
        )

        // Проверяем ID транзакции и загружаем её если ID валидный
        if (!transactionId.isNullOrBlank()) {
            Timber.d("ТРАНЗАКЦИЯ-ЭКРАН: Загрузка транзакции с ID: $transactionId")
            viewModel.loadTransactionForEditById(transactionId)
        } else {
            Timber.e("ТРАНЗАКЦИЯ-ЭКРАН: Ошибка - пустой ID транзакции")
            // Показываем сообщение об ошибке
            viewModel.setError("Не указан ID транзакции для редактирования")
        }
    }

    // Логируем состояние для отладки
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.transactionToEdit) {
        Timber.d(
            "ТРАНЗАКЦИЯ-ЭКРАН: editMode=${state.editMode}, transactionToEdit=${state.transactionToEdit?.id}, amount=${state.amount}",
        )
    }

    baseTransactionScreen(
        viewModel = viewModel,
        onNavigateBack = viewModel::onNavigateBack,
        screenTitle = stringResource(UiR.string.edit),
        buttonText = stringResource(UiR.string.save),
        isEditMode = true,
        eventFactory = defaultTransactionEventFactory(true),
        submitEvent = BaseTransactionEvent.SubmitEdit,
    )
}
