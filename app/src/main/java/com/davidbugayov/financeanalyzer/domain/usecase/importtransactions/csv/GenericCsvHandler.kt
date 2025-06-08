package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.csv

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractBankHandler
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader

class GenericCsvHandler(
    transactionRepository: TransactionRepository,
    context: Context
) : AbstractBankHandler(transactionRepository, context) {

    override val bankName: String = "Generic CSV"

    override fun supportsFileType(fileType: FileType): Boolean {
        return fileType == FileType.CSV
    }

    override fun createImporter(fileType: FileType): ImportTransactionsUseCase {
        if (supportsFileType(fileType)) {
            val defaultConfig = CsvParseConfig(
                // Здесь можно оставить значения по умолчанию из CsvParseConfig,
                // или определить специфичные "общие" настройки, если это имеет смысл.
                // Для примера, оставим как есть, т.е. запятая-разделитель, формат YYYY-MM-DD и т.д.
            )
            Timber.Forest.d(
                "[$bankName Handler] Creating GenericCsvImportUseCase with default config: $defaultConfig"
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

    override fun canHandle(fileName: String, uri: Uri, fileType: FileType): Boolean {
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
                                    "[$bankName Handler] Matched by CSV content (common delimiters) for file: $fileName"
                                )
                                return true
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.Forest.e(
                    e,
                    "[$bankName Handler] Error reading content from URI for canHandle: $uri"
                )
                return false
            }
        }
        Timber.Forest.d("[$bankName Handler] Did not match file: $fileName, type: $fileType")
        return false
    }
}
