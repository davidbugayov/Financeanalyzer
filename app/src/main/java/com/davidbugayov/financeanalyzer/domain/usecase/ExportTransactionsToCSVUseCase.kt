package com.davidbugayov.financeanalyzer.domain.usecase

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
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
     * Экспортирует все транзакции в CSV файл в публичную директорию загрузок (Downloads).
     * @param context Контекст приложения для доступа к директории
     * @return Flow с результатом операции (путь к файлу или ошибка)
     */
    operator fun invoke(context: Context): Flow<Result<String>> = flow {
        try {
            // Проверка разрешений для Android 9 и ниже
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                val hasWritePermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                
                if (!hasWritePermission) {
                    throw SecurityException("Отсутствует разрешение на запись во внешнее хранилище")
                }
            }
            
            val result = withContext(Dispatchers.IO) {
                // Получаем все транзакции
                val transactions = transactionRepository.getAllTransactions()

                // Создаем имя файла с текущей датой и временем
                val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                val timestamp = dateFormat.format(Date())
                val fileName = "finance_transactions_$timestamp.csv"

                // Сохраняем в публичную директорию Downloads
                val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Для Android 10+ используем Downloads директорию через getExternalStoragePublicDirectory
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    File(downloadsDir, fileName)
                } else {
                    // Для Android 9 и ниже также используем публичную директорию Downloads
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    File(downloadsDir, fileName)
                }

                Timber.d("Saving file to: ${file.absolutePath}")

                // Записываем данные в CSV
                FileWriter(file).use { writer ->
                    // Заголовок CSV
                    writer.append("ID,Дата,Категория,Сумма,Тип,Примечание,Источник\n")

                    // Данные транзакций
                    transactions.forEach { transaction ->
                        val cleanId = cleanText(transaction.id)
                        val cleanCategory = cleanText(transaction.category)
                        val cleanNote = cleanText(transaction.note ?: "")
                        val cleanSource = cleanText(transaction.source ?: "")
                        val formattedDate = dateFormat.format(transaction.date)
                        val formattedAmount = transaction.amount.toString()
                        val transactionType = if (transaction.isExpense) "Расход" else "Доход"
                        
                        // Формируем строку в правильном CSV формате
                        val csvLine = listOf(
                            cleanId,
                            formattedDate,
                            cleanCategory,
                            formattedAmount,
                            transactionType,
                            cleanNote,
                            cleanSource
                        ).joinToString(",")
                        
                        writer.append(csvLine).append("\n")
                        
                        // Логируем экспортируемую строку для отладки
                        Timber.d("ЭКСПОРТ: $csvLine")
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
    
    /**
     * Очищает текст от системных сообщений IDE и других потенциально проблемных символов для CSV
     */
    private fun cleanText(text: String): String {
        if (text.isBlank()) return ""
        
        // Проверяем на системные сообщения IDE
        if (text.contains("looks like you just edited", ignoreCase = true) ||
            text.contains("targetSdkVersion", ignoreCase = true) ||
            text.contains("Toggle info", ignoreCase = true) ||
            text.contains("⌘F1", ignoreCase = true) ||
            text.contains("Android SDK", ignoreCase = true)) {
            Timber.d("Обнаружено системное сообщение при экспорте, очищаем: ${text.take(50)}...")
            return ""
        }
        
        // Экранируем запятые и кавычки для формата CSV
        val cleanedText = text.replace("\"", "\"\"")
        return if (cleanedText.contains(",") || cleanedText.contains("\"") || cleanedText.contains("\n")) {
            "\"$cleanedText\""
        } else {
            cleanedText
        }
    }
} 