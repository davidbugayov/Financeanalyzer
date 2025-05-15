package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.detectors

import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType

/**
 * Детектор для Excel файлов.
 */
class ExcelDetector : FileDetector {

    /**
     * Проверяет, является ли файл Excel-документом.
     *
     * @param extension Расширение файла (без точки)
     * @return true, если расширение файла .xlsx или .xls
     */
    override fun canHandle(extension: String): Boolean {
        return extension.equals("xlsx", ignoreCase = true) ||
                extension.equals("xls", ignoreCase = true)
    }

    /**
     * Возвращает тип файла Excel.
     *
     * @return FileType.EXCEL
     */
    override fun getFileType(): FileType {
        return FileType.EXCEL
    }
} 