package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        startDate: Date? = null,
        endDate: Date? = null,
        category: String? = null,
        tags: List<String>? = null
    ): Flow<List<Transaction>> {
        return when {
            startDate != null && endDate != null -> 
                repository.getTransactionsByDateRange(startDate, endDate)
            category != null -> 
                repository.getTransactionsByCategory(category)
            tags != null -> 
                repository.getTransactionsByTags(tags)
            else -> 
                repository.getAllTransactions()
        }
    }
} 