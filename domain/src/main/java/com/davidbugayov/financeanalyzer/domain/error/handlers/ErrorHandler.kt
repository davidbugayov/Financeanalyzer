package com.davidbugayov.financeanalyzer.domain.error.handlers

import com.davidbugayov.financeanalyzer.domain.error.DomainException

/**
 * Data class representing a user-friendly error message
 */
data class ErrorMessage(
    val title: String,
    val message: String,
    val errorCode: DomainException.ErrorCode,
    val isRecoverable: Boolean = true,
    val suggestedAction: String? = null
)

/**
 * Interface for handling domain errors and converting them to user-friendly messages.
 * This abstraction allows for different error handling strategies in different contexts.
 */
interface ErrorHandler {

    /**
     * Handles a domain exception and returns a user-friendly error message
     */
    fun handle(exception: DomainException): ErrorMessage

    /**
     * Handles a generic exception and returns a user-friendly error message
     */
    fun handle(exception: Exception): ErrorMessage

    /**
     * Handles an error by error code and returns a user-friendly error message
     */
    fun handle(errorCode: DomainException.ErrorCode, message: String? = null): ErrorMessage
}

/**
 * Default implementation of ErrorHandler
 */
class DefaultErrorHandler : ErrorHandler {

    override fun handle(exception: DomainException): ErrorMessage {
        return when (exception.errorCode) {
            DomainException.ErrorCode.TRANSACTION_NOT_FOUND ->
                ErrorMessage(
                    title = "Transaction Not Found",
                    message = "The requested transaction could not be found. It may have been deleted.",
                    errorCode = exception.errorCode,
                    isRecoverable = true,
                    suggestedAction = "Try refreshing the list or check if the transaction was deleted."
                )

            DomainException.ErrorCode.INVALID_TRANSACTION_AMOUNT ->
                ErrorMessage(
                    title = "Invalid Amount",
                    message = "The transaction amount is invalid. Please check and enter a valid amount.",
                    errorCode = exception.errorCode,
                    isRecoverable = true,
                    suggestedAction = "Enter a positive number for the transaction amount."
                )

            DomainException.ErrorCode.WALLET_NOT_FOUND ->
                ErrorMessage(
                    title = "Wallet Not Found",
                    message = "The selected wallet could not be found. It may have been deleted.",
                    errorCode = exception.errorCode,
                    isRecoverable = true,
                    suggestedAction = "Select a different wallet or create a new one."
                )

            DomainException.ErrorCode.INSUFFICIENT_BALANCE ->
                ErrorMessage(
                    title = "Insufficient Balance",
                    message = "You don't have enough funds in this wallet for this transaction.",
                    errorCode = exception.errorCode,
                    isRecoverable = true,
                    suggestedAction = "Check your wallet balance or select a different wallet."
                )

            DomainException.ErrorCode.CATEGORY_NOT_FOUND ->
                ErrorMessage(
                    title = "Category Not Found",
                    message = "The selected category could not be found. It may have been deleted.",
                    errorCode = exception.errorCode,
                    isRecoverable = true,
                    suggestedAction = "Select a different category or create a new one."
                )

            DomainException.ErrorCode.VALIDATION_ERROR ->
                ErrorMessage(
                    title = "Validation Error",
                    message = exception.message ?: "The provided data is invalid.",
                    errorCode = exception.errorCode,
                    isRecoverable = true,
                    suggestedAction = "Please check your input and try again."
                )

            DomainException.ErrorCode.NETWORK_ERROR ->
                ErrorMessage(
                    title = "Network Error",
                    message = "Unable to connect to the server. Please check your internet connection.",
                    errorCode = exception.errorCode,
                    isRecoverable = true,
                    suggestedAction = "Check your internet connection and try again."
                )

            DomainException.ErrorCode.DATABASE_ERROR ->
                ErrorMessage(
                    title = "Database Error",
                    message = "There was a problem accessing the local database.",
                    errorCode = exception.errorCode,
                    isRecoverable = false,
                    suggestedAction = "Try restarting the app. If the problem persists, contact support."
                )

            else ->
                ErrorMessage(
                    title = "Unexpected Error",
                    message = exception.message ?: "An unexpected error occurred.",
                    errorCode = exception.errorCode,
                    isRecoverable = false,
                    suggestedAction = "Please try again. If the problem persists, contact support."
                )
        }
    }

    override fun handle(exception: Exception): ErrorMessage {
        // For non-domain exceptions, create a generic error message
        return ErrorMessage(
            title = "Unexpected Error",
            message = exception.message ?: "An unexpected error occurred.",
            errorCode = DomainException.ErrorCode.UNKNOWN_ERROR,
            isRecoverable = false,
            suggestedAction = "Please try again. If the problem persists, contact support."
        )
    }

    override fun handle(errorCode: DomainException.ErrorCode, message: String?): ErrorMessage {
        val exception = DomainException(
            message = message ?: "Unknown error occurred",
            errorCode = errorCode
        )
        return handle(exception)
    }
}
