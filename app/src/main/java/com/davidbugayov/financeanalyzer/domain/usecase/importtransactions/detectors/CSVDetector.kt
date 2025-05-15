package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.detectors

import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType

/**
 * Детектор для CSV файлов.
 */
class CSVDetector : FileDetector {

    /**
     * Проверяет, является ли файл CSV-документом.
     *
     * @param extension Расширение файла (без точки)
     * @return true, если расширение файла .csv
     */
    override fun canHandle(extension: String): Boolean {
        return extension.equals("csv", ignoreCase = true)
    }

    /**
     * Возвращает тип файла CSV.
     *
     * @return FileType.CSV
     */
    override fun getFileType(): FileType {
        return FileType.CSV
    }
} 