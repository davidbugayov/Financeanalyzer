package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Use case для обновления существующей транзакции.
 */
class UpdateTransactionUseCase(
    private val repository: TransactionRepository
) {
    /**
     * Обновляет транзакцию в базе данных.
     *
     * @param transaction Обновленная транзакция
     * @return Unit при успешном выполнении или выбрасывает исключение
     */
    suspend operator fun invoke(transaction: Transaction) = withContext(Dispatchers.IO) {
        try {
            Timber.d("Обновление транзакции: $transaction")
            repository.updateTransaction(transaction)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при обновлении транзакции")
            throw e
        }
    }
} 