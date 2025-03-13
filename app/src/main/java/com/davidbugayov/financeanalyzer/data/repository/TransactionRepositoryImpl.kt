package com.davidbugayov.financeanalyzer.data.repository

import android.content.Context
import com.davidbugayov.financeanalyzer.data.local.database.AppDatabase
import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Реализация репозитория для работы с транзакциями.
 * Использует базу данных Room для хранения данных.
 */
class TransactionRepositoryImpl(context: Context) : ITransactionRepository {

    private val database = AppDatabase.getInstance(context)
    private val transactionDao = database.transactionDao()
    
    /**
     * Загружает все транзакции из базы данных
     * @return Список транзакций
     */
    override suspend fun loadTransactions(): List<Transaction> = withContext(Dispatchers.IO) {
        transactionDao.getAllTransactions().map { it.toDomain() }
    }
    
    /**
     * Добавляет новую транзакцию в базу данных
     * @param transaction Транзакция для добавления
     */
    override suspend fun addTransaction(transaction: Transaction): Unit = withContext(Dispatchers.IO) {
        // Генерируем новый ID, если не указан
        val transactionToAdd = if (transaction.id == 0L) {
            transaction
        } else {
            transaction
        }

        val entity = TransactionEntity.fromDomain(transactionToAdd)
        transactionDao.insertTransaction(entity)
    }
    
    /**
     * Удаляет транзакцию из базы данных
     * @param transaction Транзакция для удаления
     */
    override suspend fun deleteTransaction(transaction: Transaction): Unit = withContext(Dispatchers.IO) {
        val entity = TransactionEntity.fromDomain(transaction)
        transactionDao.deleteTransaction(entity)
    }
    
    /**
     * Обновляет существующую транзакцию в базе данных
     * @param transaction Обновленная транзакция
     */
    override suspend fun updateTransaction(transaction: Transaction): Unit = withContext(Dispatchers.IO) {
        val entity = TransactionEntity.fromDomain(transaction)
        transactionDao.updateTransaction(entity)
    }

    /**
     * Получает транзакции за указанный период
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Flow со списком транзакций
     */
    override suspend fun getTransactions(startDate: Date, endDate: Date): Flow<List<Transaction>> = flow {
        val transactions = withContext(Dispatchers.IO) {
            transactionDao.getAllTransactions()
                .map { it.toDomain() }
                .filter { it.date >= startDate && it.date <= endDate }
        }
        emit(transactions)
    }
} 