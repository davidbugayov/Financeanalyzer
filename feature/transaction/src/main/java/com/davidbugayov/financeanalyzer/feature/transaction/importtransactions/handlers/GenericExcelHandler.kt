package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.excel.ExcelParseConfig
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.excel.GenericExcelImportUseCase
import timber.log.Timber

class GenericExcelHandler(
    transactionRepository: TransactionRepository,
    context: Context,
) : AbstractExcelBankHandler(transactionRepository, context) {
    override val bankName: String = "Generic Excel"

    // Ключевые слова для общих Excel-файлов
    override val excelKeywords: List<String> =
        listOf(
            ".xlsx",
            ".xls",
            "excel_export",
            "workbook",
            "excel",
            "spreadsheet",
        )

    override fun createImporter(fileType: FileType): ImportTransactionsUseCase {
        if (supportsFileType(fileType)) {
            val defaultConfig =
                ExcelParseConfig(
                    // Можно оставить значения по умолчанию из ExcelParseConfig,
                    // или определить специфичные "общие" настройки для Excel, если это имеет смысл.
                    // Например, если большинство Excel-файлов, которые не определяются как банковские,
                    // имеют заголовок в 2 строки, можно указать headerRowCount = 2.
                    // Для примера, оставим полностью дефолтный конфиг.
                )
            Timber.d(
                "[$bankName Handler] Создание GenericExcelImportUseCase с дефолтной конфигурацией: $defaultConfig",
            )
            return GenericExcelImportUseCase(context, transactionRepository, defaultConfig)
        }
        throw IllegalArgumentException("[$bankName Handler] не поддерживает тип файла: $fileType")
    }

    override fun getFileNameKeywords(): List<String> {
        return listOf(".xlsx", ".xls", "excel_export", "workbook")
    }

    override fun canHandle(
        fileName: String,
        uri: Uri,
        fileType: FileType,
    ): Boolean {
        // Базовая проверка (тип файла и ключевые слова в имени)
        if (super.canHandle(fileName, uri, fileType)) {
            return true
        }

        // Для Excel файлов, если проверка по имени не прошла,
        // проверка по содержимому через uri без парсинга бинарной структуры малоэффективна.
        // Поэтому, если super.canHandle() вернул false, мы тоже вернем false.
        // В будущем здесь можно добавить проверку "магических чисел" файла,
        // если будет реализовано чтение начальных байт из uri.
        Timber.d(
            "[$bankName Handler] Did not match file by name: $fileName. Content check for Excel is typically not done on string preview or simple URI read.",
        )
        return false // Если дошли сюда, значит super.canHandle() был false.
    }
}
