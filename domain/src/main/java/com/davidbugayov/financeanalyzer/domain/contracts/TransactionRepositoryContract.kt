package com.davidbugayov.financeanalyzer.domain.contracts

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.shared.model.Money
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Simple transaction filter for basic filtering operations
 */
data class TransactionFilter(
    val startDate: Date? = null,
    val endDate: Date? = null,
    val categoryId: Long? = null,
    val walletId: Long? = null,
    val minAmount: Money? = null,
    val maxAmount: Money? = null
)

/**
 * Transaction type enum
 */
enum class TransactionType {
    INCOME, EXPENSE
}

/**
 * Contract interface for Transaction repository operations.
 * Defines the API that feature modules should use to interact with transaction data.
 * This abstraction allows for better testability and flexibility in implementation.
 *
 * Note: This is a simplified contract for the initial architectural improvements.
 * It will be expanded as the codebase is refactored to support all methods.
 */
interface TransactionRepositoryContract {

    /**
     * Retrieves all transactions as a list
     */
    suspend fun getAllTransactions(): List<Transaction>

    /**
     * Retrieves transactions for a specific period
     * @param startDate Start date (inclusive)
     * @param endDate End date (exclusive)
     */
    suspend fun getTransactionsForPeriod(
        startDate: Date,
        endDate: Date
    ): List<Transaction>
}
