package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common

import androidx.lifecycle.MutableLiveData

/**
 * Класс для представления результата операции импорта.
 */
sealed class TransactionImportResult {
    /**
     * Успешный результат импорта.
     *
     * @param importedCount Количество успешно импортированных транзакций
     * @param skippedCount Количество пропущенных транзакций
     * @param bankName Название банка, из которого импортированы транзакции (опционально)
     */
    data class Success(
        val importedCount: Int,
        val skippedCount: Int,
        val bankName: String? = null
    ) : TransactionImportResult() {
        companion object {
            /**
             * LiveData для передачи результатов импорта напрямую между компонентами.
             * Используется для обратной совместимости с предыдущей архитектурой.
             */
            val directResultLiveData = MutableLiveData<Success?>()
        }
    }

    /**
     * Ошибка при импорте.
     *
     * @param error Сообщение об ошибке
     * @param exception Исключение, вызвавшее ошибку (опционально)
     */
    data class Error(
        val error: String,
        val exception: Throwable? = null
    ) : TransactionImportResult()

    /**
     * Прогресс импорта.
     *
     * @param progress Процент выполнения (0-100)
     * @param message Сообщение о текущем этапе импорта
     */
    data class Progress(
        val progress: Int,
        val message: String
    ) : TransactionImportResult()
} 