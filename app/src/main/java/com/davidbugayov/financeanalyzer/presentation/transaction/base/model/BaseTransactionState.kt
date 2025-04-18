package com.davidbugayov.financeanalyzer.presentation.transaction.base.model

import com.davidbugayov.financeanalyzer.domain.model.Source
import java.util.Date

open class BaseTransactionState(
    val title: String = "",
    val amount: String = "",
    val category: String = "",
    val note: String = "",
    val selectedDate: Date = Date(),
    val isExpense: Boolean = true,
    val sources: List<Source> = emptyList(),
    // ...другие общие поля, если потребуется
) 