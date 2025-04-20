package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.util.Date

class PrepareTransactionUseCase {
    operator fun invoke(
        id: String?,
        title: String,
        amount: String,
        category: String,
        note: String,
        date: Date,
        isExpense: Boolean,
        source: String,
        sourceColor: Int,
        isTransfer: Boolean
    ): Transaction? {
        val parsedAmount = amount.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        if (category.isBlank() || source.isBlank() || parsedAmount == 0.0) return null
        val finalAmount = if (isExpense) -parsedAmount else parsedAmount
        return Transaction(
            id = id ?: "",
            amount = Money(finalAmount),
            date = date,
            note = note,
            category = category,
            source = source,
            isExpense = isExpense,
            sourceColor = sourceColor,
            isTransfer = isTransfer
        )
    }
} 