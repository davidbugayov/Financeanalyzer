package com.davidbugayov.financeanalyzer.data.repository

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Реализация репозитория для работы с транзакциями.
 * Использует CSV файл для хранения данных.
 */
class TransactionRepositoryImpl(private val context: Context) : ITransactionRepository {
    
    private val fileName = "transactions.csv"
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var fileUri: Uri? = null
    
    init {
        // Создаем файл, если он не существует
        fileUri = createCsvFile()
    }
    
    /**
     * Загружает все транзакции из CSV файла
     * @return Список транзакций
     */
    override suspend fun loadTransactions(): List<Transaction> = withContext(Dispatchers.IO) {
        val transactions = mutableListOf<Transaction>()
        
        try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val lines = file.readLines()
                
                for (line in lines.drop(1)) {  // Пропускаем заголовок
                    val parts = line.split(",")
                    if (parts.size >= 6) {
                        val id = parts[0].toLongOrNull() ?: 0L
                        val date = try {
                            dateFormat.parse(parts[1]) ?: Date()
                        } catch (e: Exception) {
                            Date()
                        }
                        val title = parts[2]
                        val amount = parts[3].toDoubleOrNull() ?: 0.0
                        val category = parts[4]
                        val isExpense = parts[5].toBoolean()
                        val note = if (parts.size > 6) parts[6] else null
                        
                        transactions.add(
                            Transaction(
                                id = id,
                                date = date,
                                title = title,
                                amount = amount,
                                category = category,
                                isExpense = isExpense,
                                note = note
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        transactions
    }
    
    /**
     * Добавляет новую транзакцию в CSV файл
     * @param transaction Транзакция для добавления
     */
    override suspend fun addTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        val transactions = loadTransactions().toMutableList()
        
        // Генерируем новый ID, если не указан
        val newTransaction = if (transaction.id == 0L) {
            val newId = (transactions.maxOfOrNull { it.id } ?: 0) + 1
            transaction.copy(id = newId)
        } else {
            transaction
        }
        
        transactions.add(newTransaction)
        saveTransactions(transactions)
    }
    
    /**
     * Удаляет транзакцию из CSV файла
     * @param transaction Транзакция для удаления
     */
    override suspend fun deleteTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        val transactions = loadTransactions().toMutableList()
        transactions.removeIf { it.id == transaction.id }
        saveTransactions(transactions)
    }
    
    /**
     * Обновляет существующую транзакцию в CSV файле
     * @param transaction Обновленная транзакция
     */
    override suspend fun updateTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        val transactions = loadTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions[index] = transaction
            saveTransactions(transactions)
        }
    }
    
    /**
     * Сохраняет список транзакций в CSV файл
     * @param transactions Список транзакций для сохранения
     */
    private fun saveTransactions(transactions: List<Transaction>) {
        try {
            val file = File(context.filesDir, fileName)
            file.writeText("Id,Date,Title,Amount,Category,IsExpense,Note\n")  // Заголовок
            for (transaction in transactions) {
                file.appendText(
                    "${transaction.id}," +
                    "${dateFormat.format(transaction.date)}," +
                    "${transaction.title}," +
                    "${transaction.amount}," +
                    "${transaction.category}," +
                    "${transaction.isExpense}," +
                    "${transaction.note ?: ""}\n"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Создает CSV файл, если он не существует
     * @return Uri созданного файла
     */
    private fun createCsvFile(): Uri? {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                file.createNewFile()
                file.writeText("Id,Date,Title,Amount,Category,IsExpense,Note\n")  // Добавляем заголовок
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
} 