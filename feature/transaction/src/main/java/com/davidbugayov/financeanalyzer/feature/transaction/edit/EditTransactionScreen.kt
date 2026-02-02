package com.davidbugayov.financeanalyzer.feature.transaction.edit
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    LaunchedEffect(transactionId) {
        com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsProviderBridge.getProvider()
            ?.logScreenView(
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
    LaunchedEffect(state.transactionToEdit, state.editMode) {
        Timber.d(
            "ТРАНЗАКЦИЯ-ЭКРАН: editMode=%b, transactionToEdit=%s, amount=%s, category=%s, isLoading=%b",
            state.editMode,
            state.transactionToEdit?.id,
            state.amount,
            state.category,
            state.isLoading,
        )
    }

    // Показываем индикатор загрузки или ошибку, пока данные не загружены
    when {
        state.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = state.error ?: "Неизвестная ошибка")
            }
        }
        state.isLoading && !state.editMode -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        state.editMode && state.transactionToEdit != null -> {
            // Данные загружены, показываем форму редактирования
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
        else -> {
            // Неожиданное состояние - показываем индикатор загрузки
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
