package com.davidbugayov.financeanalyzer.presentation.transaction.base.model

import java.util.Date

sealed class BaseTransactionEvent {
    data class SetTitle(val title: String) : BaseTransactionEvent()
    data class SetAmount(val amount: String) : BaseTransactionEvent()
    data class SetCategory(val category: String) : BaseTransactionEvent()
    data class SetNote(val note: String) : BaseTransactionEvent()
    data class SetDate(val date: Date) : BaseTransactionEvent()
    object ToggleTransactionType : BaseTransactionEvent()
    // ...другие общие события, если потребуется
} 