package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.alfabank

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.excel.AmountParseConfig
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.excel.DateFormatConfig
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.excel.ExcelColumnMapping
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.excel.ExcelParseConfig
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.excel.GenericExcelImportUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.excel.SheetSelector
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractExcelBankHandler
import com.davidbugayov.financeanalyzer.feature.transaction.R
import java.io.BufferedInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import timber.log.Timber

/**
 * Обработчик Excel-выписок Альфа-Банка
 */
class AlfaBankExcelHandler(
    transactionRepository: TransactionRepository,
    context: Context,
) : AbstractExcelBankHandler(transactionRepository, context) {
    override val bankName: String = "Альфа-Банк Excel"

    // Ключевые слова для Excel-файлов Альфа-Банка
    override val excelKeywords: List<String> =
        listOf(
            "alfabank", "альфабанк", "альфа-банк", "alfa",
            "statement", "выписка", "операци", "движени",
            "excel", "xlsx", "xls",
        )

    // Негативные ключевые слова для исключения ложных срабатываний
    private fun getNegativeKeywords(): List<String> =
        listOf(
            "sberbank",
            "сбербанк",
            "сбер",
            "sber",
            "тинькофф",
            "tinkoff",
            "ozon",
            "озон",
        )

    /**
     * Проверяет, может ли данный хендлер обработать файл по имени и содержимому
     */
    override fun canHandle(
        fileName: String,
        uri: Uri,
        fileType: FileType,
    ): Boolean {
        if (!supportsFileType(fileType)) return false

        val hasPositiveKeyword = excelKeywords.any { fileName.lowercase().contains(it.lowercase()) }
        val containsNegativeKeyword =
            getNegativeKeywords().any {
                fileName.lowercase().contains(
                    it.lowercase(),
                )
            }

        if (containsNegativeKeyword) {
            Timber.d("[$bankName Handler] Файл содержит ключевые слова других банков: $fileName")
            return false
        }

        // Дополнительная проверка по содержимому файла
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArray(2048)
                val bis = BufferedInputStream(inputStream)
                val bytesRead = bis.read(buffer, 0, buffer.size)
                if (bytesRead > 0) {
                    val content = String(buffer, 0, bytesRead)
                    val alfaBankIndicators =
                        listOf(
                            "АЛЬФА-БАНК",
                            "ALFA-BANK",
                            "Альфа-Банк",
                            "Альфа",
                            "Alfa",
                        )
                    val hasAlfaBankIndicator =
                        alfaBankIndicators.any {
                            content.contains(
                                it,
                                ignoreCase = true,
                            )
                        }
                    val otherBankIndicators =
                        listOf(
                            "СБЕРБАНК",
                            "SBERBANK",
                            "Тинькофф",
                            "ТИНЬКОФФ",
                            "OZON",
                            "ОЗОН",
                        )
                    val hasOtherBankIndicator =
                        otherBankIndicators.any {
                            content.contains(
                                it,
                                ignoreCase = true,
                            )
                        }

                    if (hasOtherBankIndicator) {
                        Timber.d("[$bankName Handler] Файл содержит указания на другой банк")
                        return false
                    }

                    if (hasAlfaBankIndicator) {
                        Timber.d(
                            "[$bankName Handler] Найден индикатор Альфа-Банка в содержимом файла",
                        )
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w("[$bankName Handler] Ошибка при чтении файла для определения: ${e.message}")
        }

        return hasPositiveKeyword
    }

    /**
     * Создаёт UseCase для импорта Excel-выписки Альфа-Банка
     */
    override fun createImporter(fileType: FileType): ImportTransactionsUseCase {
        if (supportsFileType(fileType)) {
            val transactionSource = context.getString(R.string.transaction_source_alfa)

            // Конфигурация для Альфа-Банка
            val alfaBankConfig =
                ExcelParseConfig(
                    sheetSelector = SheetSelector.ByIndex(0),
                    headerRowCount = 2, // Уменьшаем до 2, т.к. в логах видны строки заголовков
                    columnMapping =
                        ExcelColumnMapping(
                            dateColumnIndex = 0, // Дата операции
                            descriptionColumnIndex = 3, // Код операции как описание
                            amountColumnIndex = null, // Отключаем поиск суммы в файле
                            categoryColumnIndex = 4, // Категория операции
                        ),
                    defaultCurrencyCode = "RUB",
                    dateFormatConfig =
                        DateFormatConfig(
                            primaryDateFormatString = "dd.MM.yyyy",
                            locale = Locale.forLanguageTag("ru"),
                        ),
                    amountParseConfig =
                        AmountParseConfig(
                            decimalSeparator = ',',
                            currencySymbolsToRemove = listOf("₽", "руб", "RUB"),
                        ),
                    skipEmptyRows = true,
                    expectedMinValuesPerRow = 1, // Требуем только дату
                )

            // Запускаем с debug-конфигурацией для вывода подробной информации
            val debugEnabled = true
            Timber.d(
                "[$bankName Handler] Создание GenericExcelImportUseCase с конфигурацией: $alfaBankConfig",
            )
            return GenericExcelImportUseCase(
                context,
                transactionRepository,
                alfaBankConfig,
                transactionSource,
                debugEnabled,
            )
        }
        throw IllegalArgumentException("[$bankName Handler] не поддерживает тип файла: $fileType")
    }

    private fun parseDate(dateStr: String): Date? {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("ru"))

        return try {
            simpleDateFormat.parse(dateStr)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка парсинга даты: $dateStr")
            null
        }
    }
}
