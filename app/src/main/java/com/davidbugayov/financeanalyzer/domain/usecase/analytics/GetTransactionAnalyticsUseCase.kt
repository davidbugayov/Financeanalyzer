package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.TransactionStats
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.Calendar

class GetTransactionAnalyticsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        period: AnalyticsPeriod = AnalyticsPeriod.MONTH
    ): Flow<TransactionStats> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        calendar.add(when (period) {
            AnalyticsPeriod.WEEK -> Calendar.WEEK_OF_YEAR
            AnalyticsPeriod.MONTH -> Calendar.MONTH
            AnalyticsPeriod.YEAR -> Calendar.YEAR
            AnalyticsPeriod.ALL_TIME -> Calendar.YEAR.also { calendar.set(2000, 0, 1) }
        }, -1)
        
        val startDate = calendar.time
        return repository.getTransactionStats(startDate, endDate)
    }
}

enum class AnalyticsPeriod {
    WEEK,
    MONTH,
    YEAR,
    ALL_TIME
} 