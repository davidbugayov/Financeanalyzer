package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common

/**
 * Функциональный интерфейс для отслеживания прогресса импорта транзакций.
 * Позволяет получать обновления о ходе выполнения операции импорта.
 */
fun interface ImportProgressCallback {

    /**
     * Вызывается для обновления прогресса операции импорта.
     *
     * @param current Текущее количество обработанных элементов
     * @param total Общее количество элементов для обработки
     * @param message Дополнительное сообщение о прогрессе
     */
    fun onProgress(current: Int, total: Int, message: String)
} 
