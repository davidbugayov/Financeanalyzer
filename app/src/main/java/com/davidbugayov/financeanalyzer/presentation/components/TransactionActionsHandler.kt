package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.davidbugayov.financeanalyzer.domain.model.Transaction

/**
 * Компонент для обработки действий с транзакциями.
 * Содержит в себе все необходимые диалоги и логику для работы с ними.
 * 
 * @param transactionDialogState Состояние диалогов
 * @param onEvent Обработчик событий транзакций
 * @param onNavigateToEdit Функция навигации к экрану редактирования (если требуется)
 */
@Composable
fun TransactionActionsHandler(
    transactionDialogState: TransactionDialogState,
    onEvent: (TransactionEvent) -> Unit,
    onNavigateToEdit: ((Transaction) -> Unit)? = null
) {
    // Диалог подтверждения удаления транзакции
    if (transactionDialogState.showDeleteConfirmDialog && transactionDialogState.transactionToDelete != null) {
        DeleteTransactionDialog(
            transaction = transactionDialogState.transactionToDelete,
            onConfirm = {
                onEvent(TransactionEvent.DeleteTransaction(transactionDialogState.transactionToDelete))
                onEvent(TransactionEvent.HideDeleteConfirmDialog)
            },
            onDismiss = {
                onEvent(TransactionEvent.HideDeleteConfirmDialog)
            }
        )
    }
    
    // Если транзакция для редактирования выбрана и есть функция навигации, переходим к редактированию
    if (transactionDialogState.showEditDialog && transactionDialogState.transactionToEdit != null) {
        val transaction = transactionDialogState.transactionToEdit
        if (onNavigateToEdit != null) {
            // Переходим к экрану редактирования
            onNavigateToEdit(transaction)
            // Сбрасываем состояние
            onEvent(TransactionEvent.HideEditDialog)
        }
    }
} 