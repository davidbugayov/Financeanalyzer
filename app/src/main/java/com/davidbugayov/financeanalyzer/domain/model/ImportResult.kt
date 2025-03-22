package com.davidbugayov.financeanalyzer.domain.model

/**
 * Представляет результат операции импорта транзакций.
 * Используется для отслеживания прогресса и результатов импорта.
 */
sealed class ImportResult {
    /**
     * Прогресс импорта.
     * @property current Текущее количество обработанных транзакций
     * @property total Общее количество транзакций для импорта
     * @property message Сообщение о текущем шаге импорта
     */
    data class Progress(
        val current: Int,
        val total: Int,
        val message: String
    ) : ImportResult()

    /**
     * Успешное завершение импорта.
     * @property importedCount Количество успешно импортированных транзакций
     * @property skippedCount Количество пропущенных транзакций
     * @property totalAmount Общая сумма импортированных транзакций
     */
    data class Success(
        val importedCount: Int,
        val skippedCount: Int,
        val totalAmount: Double
    ) : ImportResult()

    /**
     * Ошибка при импорте.
     * @property message Сообщение об ошибке
     * @property exception Исключение, вызвавшее ошибку (если доступно)
     */
    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : ImportResult()
} 