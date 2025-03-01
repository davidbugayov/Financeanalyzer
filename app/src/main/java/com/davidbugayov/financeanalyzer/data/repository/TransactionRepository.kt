package com.davidbugayov.financeanalyzer.data.repository

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.data.model.Transaction
import com.davidbugayov.financeanalyzer.utils.CsvParser
import com.davidbugayov.financeanalyzer.utils.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class TransactionRepository(private val context: Context) {
    private val fileName = "transactions.csv"
    private var uri: Uri? = null

    init {
        // Создаем или получаем существующий файл при инициализации
        uri = FileHelper.getCsvFileUri(context, fileName) ?: FileHelper.createCsvFile(context, fileName)
    }

    fun getAllTransactions(): Flow<List<Transaction>> = flow {
        uri?.let { fileUri ->
            val inputStream = context.contentResolver.openInputStream(fileUri)
            inputStream?.use { stream ->
                val transactions = CsvParser.readCsv(stream)
                emit(transactions)
            } ?: emit(emptyList<Transaction>())
        } ?: emit(emptyList<Transaction>())
    }.flowOn(Dispatchers.IO)

    suspend fun addTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            uri?.let { fileUri ->
                // Читаем текущие транзакции
                val inputStream = context.contentResolver.openInputStream(fileUri)
                val currentTransactions = inputStream?.use { stream ->
                    CsvParser.readCsv(stream)
                } ?: emptyList()
                
                // Добавляем новую транзакцию
                val updatedTransactions = currentTransactions + transaction
                
                // Записываем обновленный список
                val outputStream = context.contentResolver.openOutputStream(fileUri)
                outputStream?.use { stream ->
                    CsvParser.writeCsv(stream, updatedTransactions)
                }
            }
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            uri?.let { fileUri ->
                val inputStream = context.contentResolver.openInputStream(fileUri)
                val currentTransactions = inputStream?.use { stream ->
                    CsvParser.readCsv(stream)
                } ?: emptyList()
                
                val updatedTransactions = currentTransactions.filter { it != transaction }
                
                val outputStream = context.contentResolver.openOutputStream(fileUri)
                outputStream?.use { stream ->
                    CsvParser.writeCsv(stream, updatedTransactions)
                }
            }
        }
    }

    suspend fun updateTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            uri?.let { fileUri ->
                val inputStream = context.contentResolver.openInputStream(fileUri)
                val currentTransactions = inputStream?.use { stream ->
                    CsvParser.readCsv(stream)
                } ?: emptyList()
                
                val updatedTransactions = currentTransactions.map { 
                    if (it.date == transaction.date) transaction else it 
                }
                
                val outputStream = context.contentResolver.openOutputStream(fileUri)
                outputStream?.use { stream ->
                    CsvParser.writeCsv(stream, updatedTransactions)
                }
            }
        }
    }
}