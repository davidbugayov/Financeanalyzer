package com.davidbugayov.financeanalyzer.presentation.components

import com.davidbugayov.financeanalyzer.domain.model.Transaction

/**
 * События для работы с транзакциями.
 * Используется во всех экранах, где требуется редактирование транзакций.
 */
sealed class TransactionEvent {
    /**
     * Показать диалог подтверждения удаления транзакции
     */
    data class ShowDeleteConfirmDialog(val transaction: Transaction) : TransactionEvent()
    
    /**
     * Скрыть диалог подтверждения удаления транзакции
     */
    data object HideDeleteConfirmDialog : TransactionEvent()
    
    /**
     * Показать диалог редактирования транзакции
     */
    data class ShowEditDialog(val transaction: Transaction) : TransactionEvent()
    
    /**
     * Скрыть диалог редактирования транзакции
     */
    data object HideEditDialog : TransactionEvent()
    
    /**
     * Удалить транзакцию
     */
    data class DeleteTransaction(val transaction: Transaction) : TransactionEvent()
    
    /**
     * Обновить транзакцию
     */
    data class UpdateTransaction(val transaction: Transaction) : TransactionEvent()
} 