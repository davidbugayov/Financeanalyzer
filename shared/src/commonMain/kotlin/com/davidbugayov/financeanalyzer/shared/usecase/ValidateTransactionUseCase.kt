package com.davidbugayov.financeanalyzer.shared.usecase

/**
 * Валидатор транзакции без зависимостей от Android/DI/логгера.
 * Возвращает флаги ошибок и код ошибки для маппинга на строки UI.
 */
class ValidateTransactionUseCase {
    data class Result(
        val isValid: Boolean,
        val amountError: Boolean,
        val categoryError: Boolean,
        val sourceError: Boolean,
        val errorCode: String?,
    )

    operator fun invoke(amount: String, category: String, source: String): Result {
        val amountError = amount.isBlank()
        val categoryError = false
        val sourceError = false
        val isValid = !amountError
        val errorCode = if (amountError) "error_enter_transaction_amount" else null
        return Result(isValid, amountError, categoryError, sourceError, errorCode)
    }
}


