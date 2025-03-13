package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface GetTransactionsUseCase {

    suspend operator fun invoke(startDate: Date, endDate: Date): Flow<List<Transaction>>
} 