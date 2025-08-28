package com.davidbugayov.financeanalyzer.domain.error

/**
 * Base exception class for domain layer errors.
 * All domain-specific exceptions should extend this class.
 */
open class DomainException(
    message: String,
    cause: Throwable? = null,
    val errorCode: ErrorCode = ErrorCode.UNKNOWN_ERROR
) : Exception(message, cause) {

    /**
     * Error codes for different types of domain errors
     */
    enum class ErrorCode {
        // General errors
        UNKNOWN_ERROR,
        VALIDATION_ERROR,
        NETWORK_ERROR,
        DATABASE_ERROR,
        PERMISSION_DENIED,

        // Transaction specific errors
        TRANSACTION_NOT_FOUND,
        INVALID_TRANSACTION_AMOUNT,
        INVALID_TRANSACTION_DATE,
        DUPLICATE_TRANSACTION,

        // Wallet specific errors
        WALLET_NOT_FOUND,
        INSUFFICIENT_BALANCE,
        INVALID_WALLET_NAME,
        WALLET_ALREADY_EXISTS,

        // Category specific errors
        CATEGORY_NOT_FOUND,
        INVALID_CATEGORY_NAME,
        CATEGORY_ALREADY_EXISTS,
        CATEGORY_IN_USE,

        // Authentication errors
        UNAUTHORIZED,
        SESSION_EXPIRED,

        // Data validation errors
        INVALID_DATA_FORMAT,
        REQUIRED_FIELD_MISSING,
        DATA_TOO_LONG,
        INVALID_RANGE,

        // Business logic errors
        BUSINESS_RULE_VIOLATION,
        OPERATION_NOT_ALLOWED,
        INSUFFICIENT_PERMISSIONS
    }

    companion object {
        /**
         * Factory method for creating validation errors
         */
        fun validation(message: String, field: String? = null) = DomainException(
            message = if (field != null) "Validation failed for field '$field': $message" else message,
            errorCode = ErrorCode.VALIDATION_ERROR
        )

        /**
         * Factory method for creating not found errors
         */
        fun notFound(resourceType: String, resourceId: Any) = DomainException(
            message = "$resourceType with id '$resourceId' not found",
            errorCode = ErrorCode.TRANSACTION_NOT_FOUND // Default, can be overridden
        )
    }
}

/**
 * Exception thrown when a transaction operation fails
 */
class TransactionException(
    message: String,
    cause: Throwable? = null,
    errorCode: ErrorCode = ErrorCode.UNKNOWN_ERROR
) : DomainException(message, cause, errorCode) {

    companion object {
        fun notFound(id: Long) = TransactionException(
            "Transaction with id $id not found",
            errorCode = ErrorCode.TRANSACTION_NOT_FOUND
        )

        fun invalidAmount(amount: String) = TransactionException(
            "Invalid transaction amount: $amount",
            errorCode = ErrorCode.INVALID_TRANSACTION_AMOUNT
        )

        fun invalidDate(date: String) = TransactionException(
            "Invalid transaction date: $date",
            errorCode = ErrorCode.INVALID_TRANSACTION_DATE
        )
    }
}

/**
 * Exception thrown when a wallet operation fails
 */
class WalletException(
    message: String,
    cause: Throwable? = null,
    errorCode: ErrorCode = ErrorCode.UNKNOWN_ERROR
) : DomainException(message, cause, errorCode) {

    companion object {
        fun notFound(id: Long) = WalletException(
            "Wallet with id $id not found",
            errorCode = ErrorCode.WALLET_NOT_FOUND
        )

        fun insufficientBalance(current: String, required: String) = WalletException(
            "Insufficient balance. Current: $current, Required: $required",
            errorCode = ErrorCode.INSUFFICIENT_BALANCE
        )

        fun invalidName(name: String) = WalletException(
            "Invalid wallet name: $name",
            errorCode = ErrorCode.INVALID_WALLET_NAME
        )

        fun alreadyExists(name: String) = WalletException(
            "Wallet with name '$name' already exists",
            errorCode = ErrorCode.WALLET_ALREADY_EXISTS
        )
    }
}

/**
 * Exception thrown when a category operation fails
 */
class CategoryException(
    message: String,
    cause: Throwable? = null,
    errorCode: ErrorCode = ErrorCode.UNKNOWN_ERROR
) : DomainException(message, cause, errorCode) {

    companion object {
        fun notFound(id: Long) = CategoryException(
            "Category with id $id not found",
            errorCode = ErrorCode.CATEGORY_NOT_FOUND
        )

        fun invalidName(name: String) = CategoryException(
            "Invalid category name: $name",
            errorCode = ErrorCode.INVALID_CATEGORY_NAME
        )

        fun alreadyExists(name: String) = CategoryException(
            "Category with name '$name' already exists",
            errorCode = ErrorCode.CATEGORY_ALREADY_EXISTS
        )

        fun inUse(name: String) = CategoryException(
            "Category '$name' is currently in use and cannot be deleted",
            errorCode = ErrorCode.CATEGORY_IN_USE
        )
    }
}

/**
 * Exception thrown when a validation operation fails
 */
class ValidationException(
    message: String,
    cause: Throwable? = null,
    val field: String? = null
) : DomainException(message, cause, ErrorCode.VALIDATION_ERROR) {

    companion object {
        fun required(field: String) = ValidationException(
            "Field '$field' is required",
            field = field
        )

        fun invalidFormat(field: String, expectedFormat: String) = ValidationException(
            "Field '$field' has invalid format. Expected: $expectedFormat",
            field = field
        )

        fun outOfRange(field: String, min: Any, max: Any, actual: Any) = ValidationException(
            "Field '$field' value $actual is out of range [$min, $max]",
            field = field
        )

        fun tooLong(field: String, maxLength: Int, actualLength: Int) = ValidationException(
            "Field '$field' is too long. Max length: $maxLength, actual: $actualLength",
            field = field
        )
    }
}
