package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * UseCase для экспорта транзакций в CSV файл.
 * Следует принципам Clean Architecture и Single Responsibility.
 */
class ExportTransactionsToCSVUseCase(
    private val transactionRepository: TransactionRepository
) {
    /**
     * Экспортирует все транзакции в CSV файл.
     * @param directory Директория для сохранения файла
     * @return Flow с результатом операции (путь к файлу или ошибка)
     */
    operator fun invoke(directory: File): Flow<Result<String>> = flow {
        try {
            // Получаем все транзакции
            val transactions = transactionRepository.getAllTransactions()
            
            // Создаем имя файла с текущей датой и временем
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val fileName = "transactions_$timestamp.csv"
            
            // Создаем файл
            val file = File(directory, fileName)
            
            // Записываем данные в CSV
            FileWriter(file).use { writer ->
                // Заголовок CSV
                writer.append("ID,Дата,Категория,Сумма,Тип,Примечание,Источник\n")
                
                // Данные транзакций
                transactions.forEach { transaction ->
                    writer.append(transaction.id.toString()).append(",")
                    writer.append(dateFormat.format(transaction.date)).append(",")
                    writer.append(transaction.category).append(",")
                    writer.append(transaction.amount.toString()).append(",")
                    writer.append(if (transaction.isExpense) "Расход" else "Доход").append(",")
                    writer.append(transaction.note ?: "").append(",")
                    writer.append(transaction.source ?: "")
                    writer.append("\n")
                }
            }
            
            emit(Result.success(file.absolutePath))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
} 