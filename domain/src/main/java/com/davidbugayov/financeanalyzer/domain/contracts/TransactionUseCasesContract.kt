package com.davidbugayov.financeanalyzer.domain.contracts

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.shared.model.Money
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Contract interface for transaction-related use cases.
 * Provides high-level business operations for transaction management.
 */
interface TransactionUseCasesContract {

    /**
     * Use case for adding a new transaction
     */
    interface AddTransactionUseCase {
        suspend operator fun invoke(transaction: Transaction): Result<Long>
    }

    /**
     * Use case for updating an existing transaction
     */
    interface UpdateTransactionUseCase {
        suspend operator fun invoke(transaction: Transaction): Result<Unit>
    }

    /**
     * Use case for deleting a transaction
     */
    interface DeleteTransactionUseCase {
        suspend operator fun invoke(id: Long): Result<Unit>
    }

    /**
     * Use case for getting a transaction by ID
     */
    interface GetTransactionByIdUseCase {
        suspend operator fun invoke(id: Long): Result<Transaction?>
    }

    /**
     * Use case for getting all transactions
     */
    interface GetTransactionsUseCase {
        operator fun invoke(): Flow<Result<List<Transaction>>>
    }

    /**
     * Use case for getting transactions for a specific period
     */
    interface GetTransactionsForPeriodUseCase {
        suspend operator fun invoke(
            startDate: Date,
            endDate: Date
        ): Result<List<Transaction>>
    }

    /**
     * Use case for getting transactions as Flow for a period
     */
    interface GetTransactionsForPeriodFlowUseCase {
        operator fun invoke(
            startDate: Date,
            endDate: Date
        ): Flow<Result<List<Transaction>>>
    }

    /**
     * Use case for filtering transactions
     */
    interface FilterTransactionsUseCase {
        suspend operator fun invoke(filter: TransactionFilter): Result<List<Transaction>>
    }

    /**
     * Use case for getting filtered transactions as Flow
     */
    interface FilterTransactionsFlowUseCase {
        operator fun invoke(filter: TransactionFilter): Flow<Result<List<Transaction>>>
    }

    /**
     * Use case for calculating total amount for a period
     */
    interface CalculateTotalAmountUseCase {
        suspend operator fun invoke(
            startDate: Date,
            endDate: Date
        ): Result<Money>
    }

    /**
     * Use case for grouping transactions
     */
    interface GroupTransactionsUseCase {
        suspend operator fun invoke(
            transactions: List<Transaction>
        ): Result<Map<Date, List<Transaction>>>
    }

    /**
     * Use case for validating transaction data
     */
    interface ValidateTransactionUseCase {
        suspend operator fun invoke(transaction: Transaction): Result<Boolean>
    }

    /**
     * Use case for exporting transactions to CSV
     */
    interface ExportTransactionsToCSVUseCase {
        suspend operator fun invoke(
            transactions: List<Transaction>,
            fileName: String
        ): Result<String>
    }
}
