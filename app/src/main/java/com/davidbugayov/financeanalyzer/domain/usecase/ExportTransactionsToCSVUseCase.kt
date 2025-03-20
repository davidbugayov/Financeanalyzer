package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import android.os.Build
import android.os.Environment
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
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
     * Экспортирует все транзакции в CSV файл в публичную директорию документов.
     * @param context Контекст приложения для доступа к директории документов
     * @return Flow с результатом операции (путь к файлу или ошибка)
     */
    operator fun invoke(context: Context): Flow<Result<String>> = flow {
        try {
            val result = withContext(Dispatchers.IO) {
                // Получаем все транзакции
                val transactions = transactionRepository.getAllTransactions()

                // Создаем имя файла с текущей датой и временем
                val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                val timestamp = dateFormat.format(Date())
                val fileName = "finance_transactions_$timestamp.csv"

                // Пытаемся использовать публичную директорию документов
                val documentsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Для Android 10+ используем MediaStore или SAF
                    val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        ?: context.getExternalFilesDir(null)
                        ?: context.filesDir
                    File(externalDir, "FinanceTransactions").apply {
                        if (!exists()) mkdirs()
                    }
                } else {
                    // Для Android 9 и ниже используем публичную директорию документов
                    val docsDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    File(docsDir, "FinanceTransactions").apply {
                        if (!exists()) mkdirs()
                    }
                }

                // Создаем файл
                val file = File(documentsDir, fileName)
                Timber.d("Saving file to: ${file.absolutePath}")

                // Записываем данные в CSV
                FileWriter(file).use { writer ->
                    // Заголовок CSV
                    writer.append("ID,Дата,Категория,Сумма,Тип,Примечание,Источник\n")

                    // Данные транзакций
                    transactions.forEach { transaction ->
                        writer.append(transaction.id).append(",")
                        writer.append(dateFormat.format(transaction.date)).append(",")
                        writer.append(transaction.category).append(",")
                        writer.append(transaction.amount.toString()).append(",")
                        writer.append(if (transaction.isExpense) "Расход" else "Доход").append(",")
                        writer.append(transaction.note ?: "").append(",")
                        writer.append(transaction.source ?: "")
                        writer.append("\n")
                    }
                }

                file.absolutePath
            }
            emit(Result.success(result))
        } catch (e: Exception) {
            Timber.e(e, "Error exporting transactions to CSV")
            emit(Result.failure(e))
        }
    }
} 