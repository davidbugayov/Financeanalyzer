package com.davidbugayov.financeanalyzer.utils.kmp

import com.davidbugayov.financeanalyzer.domain.repository.UnifiedTransactionRepository
import com.davidbugayov.financeanalyzer.shared.model.Transaction as SharedTransaction
import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository as SharedRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SharedTransactionRepositoryAdapter(
    private val unifiedRepo: UnifiedTransactionRepository,
) : SharedRepo {
    override suspend fun loadTransactions(): List<SharedTransaction> = unifiedRepo.loadTransactions().toShared()

    override fun observeTransactions(): Flow<List<SharedTransaction>> = unifiedRepo.getAll().map { it.toShared() }

    override suspend fun getTransactionsForPeriod(
        startDate: kotlinx.datetime.LocalDate,
        endDate: kotlinx.datetime.LocalDate,
    ): List<SharedTransaction> = unifiedRepo.getTransactionsByDateRange(startDate, endDate).toShared()

    override suspend fun getTransactionById(id: String): SharedTransaction? =
        unifiedRepo.getTransactionById(id)?.toShared()

    override suspend fun getTransactionsByCategory(categoryId: String): List<SharedTransaction> {
        // Для простоты возвращаем пустой список - можно реализовать позже
        return emptyList()
    }

    override suspend fun getTransactionsByType(isExpense: Boolean): List<SharedTransaction> {
        // Для простоты возвращаем пустой список - можно реализовать позже
        return emptyList()
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

    override suspend fun clearAllTransactions() {
        // Для простоты - можно реализовать позже
    }
}
