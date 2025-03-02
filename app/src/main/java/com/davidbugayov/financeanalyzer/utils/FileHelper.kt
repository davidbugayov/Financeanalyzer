package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.davidbugayov.financeanalyzer.data.model.TransactionEntity
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Вспомогательный класс для работы с файлами.
 * Отвечает за создание, чтение и запись CSV файлов с транзакциями.
 */
object FileHelper {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    /**
     * Создает CSV-файл во внутреннем хранилище приложения
     * @param context Контекст приложения
     * @param fileName Имя файла
     * @return Uri созданного файла или null в случае ошибки
     */
    fun createCsvFile(context: Context, fileName: String): Uri? {
        return try {
            // Используем внутреннее хранилище приложения
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                file.createNewFile()
                file.writeText("Id,Date,Title,Amount,Category,IsExpense,Note\n")  // Добавляем заголовок
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e("FileHelper", "Error creating CSV file", e)
            null
        }
    }

    /**
     * Читает данные из CSV-файла
     * @param context Контекст приложения
     * @param uri Uri файла
     * @return Список транзакций
     */
    fun readCsv(context: Context, uri: Uri): List<TransactionEntity> {
        val transactions = mutableListOf<TransactionEntity>()

        try {
            val file = File(uri.path ?: "")
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
                        transactions.add(TransactionEntity(
                            id = id,
                            date = date,
                            title = title,
                            amount = amount,
                            category = category,
                            isExpense = isExpense,
                            note = note
                        ))
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            Log.e("FileHelper", "CSV file not found", e)
        } catch (e: Exception) {
            Log.e("FileHelper", "Error reading CSV file", e)
        }

        return transactions
    }

    /**
     * Записывает данные в CSV-файл
     * @param context Контекст приложения
     * @param uri Uri файла
     * @param transactions Список транзакций для записи
     */
    fun writeCsv(context: Context, uri: Uri, transactions: List<TransactionEntity>) {
        try {
            val file = File(uri.path ?: "")
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
            Log.e("FileHelper", "Error writing to CSV file", e)
        }
    }

    /**
     * Получает Uri файла по имени
     * @param context Контекст приложения
     * @param fileName Имя файла
     * @return Uri файла или null, если файл не существует
     */
    fun getCsvFileUri(context: Context, fileName: String): Uri? {
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                Uri.fromFile(file)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FileHelper", "Error getting CSV file URI", e)
            null
        }
    }
}