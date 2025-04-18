package com.davidbugayov.financeanalyzer.presentation.transaction.base.model

sealed class EditTransactionEvent : BaseTransactionEvent() {
    data class LoadTransaction(val id: String) : EditTransactionEvent()
    object SubmitEdit : EditTransactionEvent()
    // ...другие специфичные для редактирования события
} 