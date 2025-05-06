package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import java.util.Date

class GetTransactionsForPeriodWithCacheUseCase(
    private val repository: ITransactionRepository
) {

    private val cache = mutableMapOf<Pair<Long, Long>, List<Transaction>>()
    private val cacheTimestamps = mutableMapOf<Pair<Long, Long>, Long>()
    private val cacheTtlMs = 60_000L // 1 минута TTL

    suspend operator fun invoke(startDate: Date, endDate: Date): List<Transaction> {
        val key = startDate.time to endDate.time
        val now = System.currentTimeMillis()
        val cached = cache[key]
        val cachedAt = cacheTimestamps[key] ?: 0L
        return if (cached != null && now - cachedAt < cacheTtlMs) {
            cached
        } else {
            val all = repository.loadTransactions()
            val fresh = all.filter { it.date >= startDate && it.date <= endDate }
            cache[key] = fresh
            cacheTimestamps[key] = now
            fresh
        }
    }

    fun clearCache() {
        cache.clear()
        cacheTimestamps.clear()
    }
} 