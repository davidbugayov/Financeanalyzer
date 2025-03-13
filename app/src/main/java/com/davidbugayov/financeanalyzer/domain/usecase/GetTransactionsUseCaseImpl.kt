package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

class GetTransactionsUseCaseImpl(
    private val repository: ITransactionRepository
) : GetTransactionsUseCase {

    override suspend fun invoke(startDate: Date, endDate: Date): Flow<List<Transaction>> {
        return repository.getTransactions(startDate, endDate)
    }
} 