package com.davidbugayov.financeanalyzer.shared.usecase.transaction

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository
import kotlinx.datetime.LocalDate

/**
 * UseCase для получения транзакций за указанный период.
 */
class GetTransactionsForPeriodUseCase(
    private val transactionRepository: TransactionRepository,
) {
    
    /**
     * Получает список транзакций за указанный период.
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Список транзакций за период
     */
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): List<Transaction> {
        val allTransactions = transactionRepository.loadTransactions()
        return allTransactions.filter { transaction ->
            transaction.date >= startDate && transaction.date <= endDate
        }
    }
}
