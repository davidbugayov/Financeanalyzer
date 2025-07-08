package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.csv

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractBankHandler
import java.io.BufferedReader
import java.io.InputStreamReader
import timber.log.Timber

class GenericCsvHandler(
    transactionRepository: TransactionRepository,
    context: Context,
) : AbstractBankHandler(transactionRepository, context) {
    override val bankName: String = "Generic CSV"

    override fun supportsFileType(fileType: FileType): Boolean {
        return fileType == FileType.CSV
    }

    override fun createImporter(fileType: FileType): ImportTransactionsUseCase {
        if (supportsFileType(fileType)) {
            // Пытаемся определить формат файла по имени
            val defaultConfig =
                CsvParseConfig(
                    // Более гибкая конфигурация для парсинга различных форматов CSV
                    dateColumnIndex = 1, // Дата во втором столбце (индекс 1), а первый столбец - ID
                    dateFormatString = "yyyy-MM-dd_HH-mm-ss", // Формат даты с временем
                    descriptionColumnIndex = 2, // Описание в третьем столбце
                    amountColumnIndex = 3, // Сумма в четвертом столбце
                    expectedMinColumnCount = 4, // Минимум 4 столбца для валидной строки
                    hasHeader = true, // Предполагаем, что есть заголовок
                    isExpenseColumnIndex = 4, // Индекс колонки с типом транзакции (Доход/Расход)
                )
            Timber.Forest.d(
                "[$bankName Handler] Creating GenericCsvImportUseCase with config: $defaultConfig",
            )
            return GenericCsvImportUseCase(context, transactionRepository, defaultConfig)
        }
        throw IllegalArgumentException("[$bankName Handler] does not support file type: $fileType")
    }

    override fun getFileNameKeywords(): List<String> {
        // Generic CSV might not have specific keywords, or you might add some very general ones
        // like "export.csv", "data.csv". For now, let's keep it broad.
        return listOf(".csv", "export", "transactions") // Добавлены общие ключевые слова
    }

    override fun canHandle(
        fileName: String,
        uri: Uri,
        fileType: FileType,
    ): Boolean {
        // Сначала базовая проверка (тип файла и ключевые слова в имени)
        if (super.canHandle(fileName, uri, fileType)) {
            return true
        }

        // Если имя файла не совпало, но тип CSV, проверяем содержимое на общие разделители
        if (supportsFileType(fileType) && fileType == FileType.CSV) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        val firstLine = reader.readLine()
                        if (firstLine != null) {
                            // Проверяем на наличие общих CSV разделителей, таких как запятая или точка с запятой
                            if (firstLine.contains(',') || firstLine.contains(';')) {
                                Timber.Forest.d(
                                    "[$bankName Handler] Matched by CSV content (common delimiters) for file: $fileName",
                                )
                                return true
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.Forest.e(
                    e,
                    "[$bankName Handler] Error reading content from URI for canHandle: $uri",
                )
                return false
            }
        }
        Timber.Forest.d("[$bankName Handler] Did not match file: $fileName, type: $fileType")
        return false
    }
}
