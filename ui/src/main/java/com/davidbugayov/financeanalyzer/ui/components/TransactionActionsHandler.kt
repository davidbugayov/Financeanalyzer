package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.runtime.Composable
import com.davidbugayov.financeanalyzer.ui.components.TransactionDialogState
import com.davidbugayov.financeanalyzer.ui.components.TransactionEvent
import com.davidbugayov.financeanalyzer.ui.components.DeleteTransactionDialog

/**
 * Компонент для обработки действий с транзакциями.
 * Содержит в себе все необходимые диалоги и логику для работы с ними.
 * * @param transactionDialogState Состояние диалогов
 * @param onEvent Обработчик событий транзакций
 * @param onNavigateToEdit Функция навигации к экрану редактирования (если требуется)
 */
@Composable
fun TransactionActionsHandler(
    transactionDialogState: TransactionDialogState,
    onEvent: (TransactionEvent) -> Unit,
    onNavigateToEdit: ((String) -> Unit)? = null,
) {
    // Диалог подтверждения удаления транзакции
    if (transactionDialogState.showDeleteConfirmDialog && transactionDialogState.transactionToDelete != null) {
        DeleteTransactionDialog(
            transaction = transactionDialogState.transactionToDelete,
            onConfirm = {
                onEvent(
                    TransactionEvent.DeleteTransaction(transactionDialogState.transactionToDelete),
                )
                onEvent(TransactionEvent.HideDeleteConfirmDialog)
            },
            onDismiss = {
                onEvent(TransactionEvent.HideDeleteConfirmDialog)
            },
        )
    }

    // Если ID транзакции для редактирования выбран и есть функция навигации, переходим к редактированию
    if (transactionDialogState.showEditDialog && transactionDialogState.transactionToEdit != null) {
        val transactionId = transactionDialogState.transactionToEdit
        if (onNavigateToEdit != null) {
            // Переходим к экрану редактирования
            onNavigateToEdit(transactionId)
            // Сбрасываем состояние
            onEvent(TransactionEvent.HideEditDialog)
        }
    }
}
