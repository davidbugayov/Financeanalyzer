package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import androidx.paging.PagingData
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

class GetPagedTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(pageSize: Int): Flow<PagingData<Transaction>> =
        repository.getAllPaged(pageSize)

    fun byPeriod(start: Date, end: Date, pageSize: Int): Flow<PagingData<Transaction>> =
        repository.getByPeriodPaged(start, end, pageSize)
} 