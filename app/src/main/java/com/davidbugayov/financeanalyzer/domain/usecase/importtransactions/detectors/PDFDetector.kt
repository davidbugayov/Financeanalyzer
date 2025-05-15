package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.detectors

import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType

/**
 * Детектор для PDF файлов.
 */
class PDFDetector : FileDetector {

    /**
     * Проверяет, является ли файл PDF-документом.
     *
     * @param extension Расширение файла (без точки)
     * @return true, если расширение файла .pdf
     */
    override fun canHandle(extension: String): Boolean {
        return extension.equals("pdf", ignoreCase = true)
    }

    /**
     * Возвращает тип файла PDF.
     *
     * @return FileType.PDF
     */
    override fun getFileType(): FileType {
        return FileType.PDF
    }
} 