package com.davidbugayov.financeanalyzer.domain.usecase

class ValidateTransactionUseCase {
    data class Result(
        val isValid: Boolean,
        val amountError: Boolean,
        val categoryError: Boolean,
        val sourceError: Boolean,
        val errorMessage: String?
    )

    operator fun invoke(amount: String, category: String, source: String): Result {
        val amountError = amount.isBlank()
        val categoryError = category.isBlank()
        val sourceError = source.isBlank()
        val isValid = !amountError && !categoryError && !sourceError
        val errorMsg = when {
            amountError && categoryError && sourceError -> "Заполните сумму, категорию и источник"
            amountError && categoryError -> "Заполните сумму и категорию"
            amountError && sourceError -> "Заполните сумму и источник"
            categoryError && sourceError -> "Заполните категорию и источник"
            amountError -> "Введите сумму транзакции"
            categoryError -> "Выберите категорию"
            sourceError -> "Выберите источник"
            else -> null
        }
        return Result(isValid, amountError, categoryError, sourceError, errorMsg)
    }
} 