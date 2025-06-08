package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import java.util.Calendar
import java.util.Date

class GetTransactionsForPeriodUseCase(
    private val transactionRepository: ITransactionRepository,
) {
    suspend operator fun invoke(startDate: Date, endDate: Date): List<Transaction> {
        val allTransactions = transactionRepository.loadTransactions()
        val calendarStart = Calendar.getInstance().apply {
            time = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calendarEnd = Calendar.getInstance().apply {
            time = endDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val start = calendarStart.time
        val end = calendarEnd.time
        return allTransactions.filter { it.date >= start && it.date <= end }
    }
}
