package com.davidbugayov.financeanalyzer.domain.usecase.validation

import timber.log.Timber

class ValidateTransactionUseCase {
    data class Result(
        val isValid: Boolean,
        val amountError: Boolean,
        val categoryError: Boolean,
        val sourceError: Boolean,
        val errorMessage: String?,
    )

    operator fun invoke(amount: String, category: String, source: String): Result {
        Timber.d("Validating transaction: amount='$amount', category='$category', source='$source'")

        val amountError = amount.isBlank()
        val categoryError = false
        val sourceError = false
        val isValid = !amountError

        val errorMsg = if (amountError) "Введите сумму транзакции" else null

        Timber.d(
            "Validation result: isValid=$isValid, amountError=$amountError, categoryError=$categoryError, sourceError=$sourceError, errorMsg=$errorMsg",
        )

        return Result(isValid, amountError, categoryError, sourceError, errorMsg)
    }
}
