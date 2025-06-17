package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common

import androidx.lifecycle.MutableLiveData

/**
 * Класс, представляющий результат операции импорта транзакций.
 * Может содержать информацию об успехе, прогрессе или ошибке.
 */
sealed class ImportResult {
    /**
     * Прогресс выполнения импорта.
     *
     * @param current Текущий прогресс
     * @param total Общее количество для обработки
     * @param message Сообщение о прогрессе
     */
    data class Progress(
        val current: Int,
        val total: Int,
        val message: String,
    ) : ImportResult()

    /**
     * Успешный результат импорта.
     *
     * @param importedCount Количество успешно импортированных транзакций
     * @param skippedCount Количество пропущенных транзакций
     * @param message Дополнительное сообщение
     */
    data class Success(
        val importedCount: Int,
        val skippedCount: Int,
        val message: String = "",
    ) : ImportResult()

    /**
     * Ошибка при импорте.
     *
     * @param exception Исключение, вызвавшее ошибку (если есть)
     * @param message Сообщение об ошибке
     */
    data class Error(
        val exception: Exception? = null,
        val message: String,
    ) : ImportResult()

    companion object {

        /**
         * LiveData для прямой передачи результата импорта.
         * Используется для обхода проблем с реактивностью Flow.
         */
        val directResultLiveData = MutableLiveData<Success?>()

        /**
         * Создает объект ошибки с указанным сообщением.
         *
         * @param message Сообщение об ошибке
         * @return Объект ошибки
         */
        fun error(message: String): Error {
            return Error(message = message)
        }

        /**
         * Создает объект ошибки с указанным исключением.
         *
         * @param exception Исключение
         * @return Объект ошибки
         */
        fun error(exception: Exception): Error {
            return Error(exception, exception.message ?: "Неизвестная ошибка")
        }

        /**
         * Создает объект успеха с указанными параметрами.
         *
         * @param importedCount Количество успешно импортированных транзакций
         * @param skippedCount Количество пропущенных транзакций
         * @param message Дополнительное сообщение
         * @return Объект успеха
         */
        fun success(importedCount: Int, skippedCount: Int, message: String = ""): Success {
            val result = Success(importedCount, skippedCount, message)
            // Отправляем результат через LiveData для гарантированного обновления UI
            directResultLiveData.postValue(result)
            return result
        }

        /**
         * Создает объект прогресса с указанными параметрами.
         *
         * @param current Текущий прогресс
         * @param total Общее количество для обработки
         * @param message Сообщение о прогрессе
         * @return Объект прогресса
         */
        fun progress(current: Int, total: Int, message: String = ""): Progress {
            return Progress(current, total, message)
        }
    }
}
