package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.detectors

/**
 * Интерфейс для детекторов банков по содержимому файла.
 */
interface BankDetector {

    /**
     * Название банка.
     */
    val bankName: String

    /**
     * Проверяет, соответствует ли файл формату выписки данного банка.
     *
     * @param fileName Имя файла
     * @param fileContent Содержимое файла (если доступно)
     * @return true, если файл соответствует формату выписки данного банка
     */
    fun detect(fileName: String, fileContent: String): Boolean
} 