package com.davidbugayov.financeanalyzer.shared.usecase.transaction

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class GetTransactionsForPeriodFlowUseCase(
    private val repository: TransactionRepository,
) {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> =
        flow { 
            emitAll(
                repository.observeTransactions().map { transactions ->
                    // Сортируем по дате: самые последние записи выше
                    transactions.sortedByDescending { it.date }
                }
            ) 
        }
}


