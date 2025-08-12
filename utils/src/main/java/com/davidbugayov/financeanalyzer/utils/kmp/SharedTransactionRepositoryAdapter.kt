package com.davidbugayov.financeanalyzer.utils.kmp

import com.davidbugayov.financeanalyzer.domain.repository.UnifiedTransactionRepository
import com.davidbugayov.financeanalyzer.shared.model.Transaction as SharedTransaction
import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository as SharedRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SharedTransactionRepositoryAdapter(
    private val unifiedRepo: UnifiedTransactionRepository,
) : SharedRepo {
    override suspend fun loadTransactions(): List<SharedTransaction> {
        return unifiedRepo.loadTransactions().toShared()
    }

    override fun observeTransactions(): Flow<List<SharedTransaction>> {
        return unifiedRepo.getAll().map { it.toShared() }
    }

    override suspend fun addTransaction(transaction: SharedTransaction) {
        unifiedRepo.add(transaction.toDomain())
    }

    override suspend fun updateTransaction(transaction: SharedTransaction) {
        unifiedRepo.update(transaction.toDomain())
    }

    override suspend fun deleteTransaction(id: String) {
        unifiedRepo.delete(id)
    }
}


