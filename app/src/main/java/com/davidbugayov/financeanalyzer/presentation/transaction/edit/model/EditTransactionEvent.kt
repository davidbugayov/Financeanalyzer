package com.davidbugayov.financeanalyzer.presentation.transaction.edit.model

sealed class EditTransactionEvent {
    data class LoadTransaction(val id: String) : EditTransactionEvent()
    object SubmitEdit : EditTransactionEvent()
    // ...другие специфичные для редактирования события
} 