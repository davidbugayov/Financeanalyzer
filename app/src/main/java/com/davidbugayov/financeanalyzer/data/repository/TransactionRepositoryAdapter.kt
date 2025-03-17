package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

/**
 * Адаптер для связи между ITransactionRepository и TransactionRepository.
 * Реализует интерфейс ITransactionRepository, используя существующий TransactionRepository.
 * Следует паттерну Adapter из GoF.
 */
class TransactionRepositoryAdapter(
    private val transactionRepository: TransactionRepository
) : ITransactionRepository {

    /**
     * Загружает все транзакции
     * @return Список транзакций
     */
    override suspend fun loadTransactions(): List<Transaction> {
        return transactionRepository.getAllTransactions()
    }

    /**
     * Добавляет новую транзакцию
     * @param transaction Транзакция для добавления
     */
    override suspend fun addTransaction(transaction: Transaction) {
        transactionRepository.addTransaction(transaction)
    }

    /**
     * Удаляет транзакцию
     * @param transaction Транзакция для удаления
     */
    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionRepository.deleteTransaction(transaction.id)
    }

    /**
     * Обновляет существующую транзакцию
     * @param transaction Обновленная транзакция
     */
    override suspend fun updateTransaction(transaction: Transaction) {
        transactionRepository.updateTransaction(transaction)
    }

    /**
     * Получает транзакции за указанный период
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Flow со списком транзакций
     */
    override suspend fun getTransactions(startDate: Date, endDate: Date): Flow<List<Transaction>> {
        // Поскольку TransactionRepository не имеет метода для получения транзакций за период,
        // мы получаем все транзакции и фильтруем их
        return flow {
            val allTransactions = transactionRepository.getAllTransactions()
            val filteredTransactions = allTransactions.filter { transaction ->
                transaction.date >= startDate && transaction.date <= endDate
            }
            emit(filteredTransactions)
        }
    }
} 