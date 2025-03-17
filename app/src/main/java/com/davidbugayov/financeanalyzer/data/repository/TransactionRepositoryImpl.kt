package com.davidbugayov.financeanalyzer.data.repository

import android.content.Context
import com.davidbugayov.financeanalyzer.data.local.database.AppDatabase
import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Реализация репозитория для работы с транзакциями.
 * Использует Room DAO для доступа к данным.
 * @param dao DAO для работы с транзакциями.
 */
class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {
    
    /**
     * Получает все транзакции.
     * @return Список всех транзакций.
     */
    override suspend fun getAllTransactions(): List<Transaction> = withContext(Dispatchers.IO) {
        dao.getAllTransactions().map { mapEntityToDomain(it) }
    }
    
    /**
     * Получает транзакцию по идентификатору.
     * @param id Идентификатор транзакции.
     * @return Транзакция или null, если не найдена.
     */
    override suspend fun getTransactionById(id: String): Transaction? = withContext(Dispatchers.IO) {
        try {
            val transactionId = id.toLong()
            dao.getTransactionById(transactionId)?.let { mapEntityToDomain(it) }
        } catch (e: NumberFormatException) {
            null
        }
    }
    
    /**
     * Добавляет новую транзакцию.
     * @param transaction Транзакция для добавления.
     */
    override suspend fun addTransaction(transaction: Transaction): Unit = withContext(Dispatchers.IO) {
        dao.insertTransaction(mapDomainToEntity(transaction))
    }
    
    /**
     * Обновляет существующую транзакцию.
     * @param transaction Транзакция для обновления.
     */
    override suspend fun updateTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        dao.updateTransaction(mapDomainToEntity(transaction))
    }
    
    /**
     * Удаляет транзакцию.
     * @param id Идентификатор транзакции для удаления.
     */
    override suspend fun deleteTransaction(id: String): Unit = withContext(Dispatchers.IO) {
        try {
            val transactionId = id.toLong()
            dao.getTransactionById(transactionId)?.let {
                dao.deleteTransaction(it)
            }
        } catch (e: NumberFormatException) {
            // Игнорируем ошибку, если id не является числом
        }
    }
    
    /**
     * Преобразует сущность в доменную модель.
     * @param entity Сущность транзакции.
     * @return Доменная модель транзакции.
     */
    private fun mapEntityToDomain(entity: TransactionEntity): Transaction {
        return Transaction(
            id = entity.id.toString(),
            amount = entity.amount.toDouble(),
            category = entity.category,
            date = entity.date,
            isExpense = entity.isExpense,
            note = entity.note,
            source = entity.source
        )
    }
    
    /**
     * Преобразует доменную модель в сущность.
     * @param domain Доменная модель транзакции.
     * @return Сущность транзакции.
     */
    private fun mapDomainToEntity(domain: Transaction): TransactionEntity {
        val id = try {
            domain.id.toLong()
        } catch (e: NumberFormatException) {
            0L
        }
        
        return TransactionEntity(
            id = id,
            amount = domain.amount.toString(),
            category = domain.category,
            date = domain.date,
            isExpense = domain.isExpense,
            note = domain.note,
            source = domain.source ?: "Наличные"
        )
    }
} 