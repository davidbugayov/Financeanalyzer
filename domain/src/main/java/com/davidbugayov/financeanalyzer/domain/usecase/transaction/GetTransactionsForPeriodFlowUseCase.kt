package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.util.Date

/**
 * Реактивная версия use-case получения транзакций за период.
 * Вместо in-memory кэша опирается на Flow из репозитория (Room will emit automatically).
 */
class GetTransactionsForPeriodFlowUseCase(
    private val repository: ITransactionRepository,
) {

    operator fun invoke(startDate: Date, endDate: Date): Flow<List<Transaction>> =
        flow {
            emitAll(repository.getTransactions(startDate, endDate))
        }
} 