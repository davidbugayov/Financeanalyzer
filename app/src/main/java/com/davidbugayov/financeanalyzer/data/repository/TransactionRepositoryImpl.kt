package com.davidbugayov.financeanalyzer.data.repository

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.data.model.TransactionEntity
import com.davidbugayov.financeanalyzer.data.model.toDomainList
import com.davidbugayov.financeanalyzer.data.model.toEntity
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.utils.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Реализация репозитория для работы с транзакциями.
 * Использует FileHelper для сохранения и загрузки данных из CSV файла.
 */
class TransactionRepositoryImpl(private val context: Context) : ITransactionRepository {

    private val csvFileName = "transactions.csv"
    private var csvFileUri: Uri? = null

    init {
        // Проверяем, существует ли файл, и используем его URI
        csvFileUri = FileHelper.getCsvFileUri(context, csvFileName)
        if (csvFileUri == null) {
            // Если файл не существует, создаем новый
            csvFileUri = FileHelper.createCsvFile(context, csvFileName)
        }
    }

    /**
     * Загружает все транзакции из CSV файла
     */
    override suspend fun loadTransactions(): List<Transaction> = withContext(Dispatchers.IO) {
        if (csvFileUri == null) {
            // Если файл не создан, создаем его
            csvFileUri = FileHelper.createCsvFile(context, csvFileName)
            emptyList()  // Возвращаем пустой список, так как файл только что создан
        } else {
            FileHelper.readCsv(context, csvFileUri!!).toDomainList()
        }
    }

    /**
     * Добавляет новую транзакцию в CSV файл
     */
    override suspend fun addTransaction(transaction: Transaction): Unit = withContext(Dispatchers.IO) {
        csvFileUri?.let { uri ->
            val currentTransactions = loadTransactions().toMutableList()
            currentTransactions.add(transaction)
            FileHelper.writeCsv(context, uri, currentTransactions.map { it.toEntity() })
        }
    }

    /**
     * Удаляет транзакцию из CSV файла
     */
    override suspend fun deleteTransaction(transaction: Transaction): Unit = withContext(Dispatchers.IO) {
        csvFileUri?.let { uri ->
            val currentTransactions = loadTransactions().toMutableList()
            currentTransactions.removeIf { it.id == transaction.id }
            FileHelper.writeCsv(context, uri, currentTransactions.map { it.toEntity() })
        }
    }

    /**
     * Обновляет существующую транзакцию в CSV файле
     */
    override suspend fun updateTransaction(transaction: Transaction): Unit = withContext(Dispatchers.IO) {
        csvFileUri?.let { uri ->
            val currentTransactions = loadTransactions().toMutableList()
            val index = currentTransactions.indexOfFirst { it.id == transaction.id }
            if (index != -1) {
                currentTransactions[index] = transaction
                FileHelper.writeCsv(context, uri, currentTransactions.map { it.toEntity() })
            }
        }
    }
} 