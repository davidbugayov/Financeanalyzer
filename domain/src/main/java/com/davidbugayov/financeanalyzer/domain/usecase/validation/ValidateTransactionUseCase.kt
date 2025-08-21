package com.davidbugayov.financeanalyzer.domain.usecase.validation

import timber.log.Timber
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider

import org.koin.core.context.GlobalContext

class ValidateTransactionUseCase {
    private val resourceProvider: ResourceProvider
        get() = GlobalContext.get().get()
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

        val errorMsg = if (amountError) resourceProvider.getStringByName("error_enter_transaction_amount") else null

        Timber.d(
            "Validation result: isValid=$isValid, amountError=$amountError, categoryError=$categoryError, sourceError=$sourceError, errorMsg=$errorMsg",
        )

        return Result(isValid, amountError, categoryError, sourceError, errorMsg)
    }
} 