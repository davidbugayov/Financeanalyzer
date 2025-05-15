package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.detectors

import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType

/**
 * Интерфейс для детекторов типов файлов.
 * Определяет, может ли детектор обрабатывать данный формат файла.
 */
interface FileDetector {

    /**
     * Проверяет, поддерживается ли данное расширение файла.
     *
     * @param extension Расширение файла (без точки)
     * @return true, если детектор может обработать данный тип файла
     */
    fun canHandle(extension: String): Boolean

    /**
     * Возвращает тип файла, который обрабатывает данный детектор.
     *
     * @return Тип файла из перечисления FileType
     */
    fun getFileType(): FileType
} 