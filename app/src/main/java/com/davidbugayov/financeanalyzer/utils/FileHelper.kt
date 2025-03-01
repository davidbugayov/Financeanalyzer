package com.davidbugayov.financeanalyzer.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.davidbugayov.financeanalyzer.data.model.Transaction
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

object FileHelper {

    // Создание CSV-файла в папке Documents
    fun createCsvFile(context: Context, fileName: String): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Для Android 10 и выше используем MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                uri?.also {
                    // Убедимся, что файл создан
                    resolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write("Date,Description,Amount,Type\n".toByteArray())
                    }
                }
                uri
            } else {
                // Для старых версий Android используем прямой доступ к файловой системе
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                if (!documentsDir.exists()) {
                    documentsDir.mkdirs()
                }
                val file = File(documentsDir, fileName)
                if (!file.exists()) {
                    file.createNewFile()
                    file.writeText("Date,Description,Amount,Type\n")  // Добавляем заголовок
                }
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            Log.e("FileHelper", "Error creating CSV file", e)
            null
        }
    }

    // Чтение CSV-файла
    fun readCsv(context: Context, uri: Uri): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val resolver = context.contentResolver

        try {
            resolver.openInputStream(uri)?.use { inputStream ->
                val reader = inputStream.bufferedReader()
                val lines = reader.readLines()

                for (line in lines.drop(1)) {  // Пропускаем заголовок
                    val parts = line.split(",")
                    if (parts.size == 4) {
                        val date = parts[0]
                        val description = parts[1]
                        val amount = parts[2].toDoubleOrNull() ?: 0.0
                        val type = parts[3]
                        transactions.add(Transaction(date, description, amount, type))
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

    // Запись в CSV-файл
    fun writeCsv(context: Context, uri: Uri, transactions: List<Transaction>) {
        val resolver = context.contentResolver

        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                val writer = outputStream.bufferedWriter()
                writer.write("Date,Description,Amount,Type\n")  // Заголовок
                for (transaction in transactions) {
                    writer.write("${transaction.date},${transaction.description},${transaction.amount},${transaction.type}\n")
                }
                writer.flush()
            }
        } catch (e: Exception) {
            Log.e("FileHelper", "Error writing to CSV file", e)
        }
    }

    // Получение URI файла по имени
    fun getCsvFileUri(context: Context, fileName: String): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val projection = arrayOf(MediaStore.Files.FileColumns._ID)
                val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(fileName)

                val resolver = context.contentResolver
                val cursor = resolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    null
                )

                cursor?.use {
                    if (it.moveToFirst()) {
                        val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                        return MediaStore.Files.getContentUri("external", id)
                    }
                }
                null
            } else {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(documentsDir, fileName)
                if (file.exists()) {
                    Uri.fromFile(file)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FileHelper", "Error getting CSV file URI", e)
            null
        }
    }
}