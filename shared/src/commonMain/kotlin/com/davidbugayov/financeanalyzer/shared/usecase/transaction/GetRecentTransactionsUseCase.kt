package com.davidbugayov.financeanalyzer.shared.usecase.transaction

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * UseCase для получения недавних транзакций.
 * Упрощенная версия без Clock для KMP совместимости.
 */
class GetRecentTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
) {
    
    /**
     * Получает список транзакций за последние N дней от указанной даты.
     * @param days Количество дней для получения транзакций
     * @param endDate Конечная дата (по умолчанию будет установлена извне)
     * @return Список транзакций за указанный период
     */
    suspend operator fun invoke(days: Int, endDate: LocalDate? = null): List<Transaction> {
        val actualEndDate = endDate ?: LocalDate(2024, 1, 1) // заглушка, должна быть передана извне
        val startDate = actualEndDate.minus(days, DateTimeUnit.DAY)
        
        val allTransactions = transactionRepository.loadTransactions()
        return allTransactions.filter { transaction ->
            transaction.date >= startDate && transaction.date <= actualEndDate
        }
    }
}
