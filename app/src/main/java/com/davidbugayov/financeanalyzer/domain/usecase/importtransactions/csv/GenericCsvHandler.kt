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
import java.util.Locale

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
            // Проверяем, является ли файл экспортированным из приложения
            val appExportConfig = CsvParseConfig(
                // Настройки для файлов, экспортированных из приложения
                delimiter = ',',
                dateFormatString = "yyyy-MM-dd_HH-mm-ss",
                locale = Locale("ru", "RU"),
                hasHeader = true,
                dateColumnIndex = 1, // Дата во втором столбце (ID,Дата,Категория,...)
                descriptionColumnIndex = 2, // Категория в третьем столбце
                amountColumnIndex = 3, // Сумма в четвертом столбце
                isExpenseColumnIndex = 4, // Тип (Расход/Доход) в пятом столбце
                isExpenseTrueValue = "Расход",
                amountCharsToRemoveRegexPattern = "[^0-9.,\\-]",
                defaultCurrencyCode = "RUB",
                expectedMinColumnCount = 5 // Минимум 5 колонок для валидной строки
            )

            // Для других форматов CSV
            CsvParseConfig(
                // Обновляем конфигурацию для соответствия другим форматам данных
                dateColumnIndex = 1, // Дата во втором столбце (индекс 1)
                dateFormatString = "yyyy-MM-dd_HH-mm-ss", // Формат даты: 2025-04-20_18-38-43
                locale = Locale("ru", "RU"),
                amountColumnIndex = 3, // Сумма в четвертом столбце (индекс 3)
                descriptionColumnIndex = 2, // Описание в третьем столбце (индекс 2)
                amountCharsToRemoveRegexPattern = "[^0-9.,\\-]", // Удаляем все, кроме цифр, точек, запятой и минуса
                amountDecimalSeparator = ' ', // Разделитель групп разрядов - пробел
                defaultCurrencyCode = "RUB", // Валюта по умолчанию - рубли
                expectedMinColumnCount = 4 // Минимум 4 столбца для валидной строки
            )

            Timber.d("[$bankName Handler] Creating GenericCsvImportUseCase with app export config: $appExportConfig")
            return GenericCsvImportUseCase(context, transactionRepository, appExportConfig)
        }
        throw IllegalArgumentException("[$bankName Handler] does not support file type: $fileType")
    }

    override fun getFileNameKeywords(): List<String> {
        // Добавляем ключевые слова для файлов, экспортированных из приложения
        return listOf(".csv", "export", "transactions", "деньги_под_контролем_транзакции")
    }

    override fun canHandle(fileName: String, uri: Uri, fileType: FileType): Boolean {
        // Проверяем, является ли файл экспортированным из приложения
        if (fileName.contains("деньги_под_контролем_транзакции")) {
            Timber.d("[$bankName Handler] Matched app-exported CSV file: $fileName")
            return true
        }
        
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
                            // Проверяем, является ли это заголовком экспортированного файла
                            if (firstLine.startsWith("ID,Дата,Категория,Сумма,Тип")) {
                                Timber.d("[$bankName Handler] Matched by app-exported CSV header: $fileName")
                                return true
                            }
                            
                            // Проверяем на наличие общих CSV разделителей, таких как запятая или точка с запятой
                            if (firstLine.contains(',') || firstLine.contains(';')) {
                                Timber.d("[$bankName Handler] Matched by CSV content (common delimiters) for file: $fileName")
                                return true
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "[$bankName Handler] Error reading content from URI for canHandle: $uri")
                return false
            }
        }
        Timber.d("[$bankName Handler] Did not match file: $fileName, type: $fileType")
        return false
    }
}