package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions

import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.model.ImportProgressCallback
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

/**
 * Реализация UseCase для импорта транзакций из файла.
 * Делегирует работу импорта транзакций в ImportTransactionsManager.
 */
class ImportTransactionsUseCaseImpl(
    private val importTransactionsManager: ImportTransactionsManager
) : ImportTransactionsUseCase {

    /**
     * Импортирует транзакции из файла по указанному URI.
     *
     * @param uri URI файла для импорта
     * @param progressCallback Колбэк для отслеживания прогресса
     * @return Поток с информацией о ходе импорта и его результатах
     */
    override fun importTransactions(
        uri: Uri,
        progressCallback: ImportProgressCallback
    ): Flow<ImportResult> = flow {
        try {
            Timber.d("ImportTransactionsUseCaseImpl - начало импорта с URI: $uri")

            // Сначала отправляем сообщение о начале импорта
            emit(ImportResult.progress(0, 100, "Начало импорта..."))

            // Вызываем менеджер и передаем callback для отслеживания прогресса
            Timber.d("Вызываем ImportTransactionsManager.importFromUri")
            try {
                val result = importTransactionsManager.importFromUri(uri, progressCallback)
                Timber.d("Получен результат импорта: $result")

                // Отправляем финальный результат
                emit(result)
            } catch (e: Exception) {
                Timber.e(e, "❌ ОШИБКА при вызове ImportTransactionsManager.importFromUri: ${e.message}")
                throw e
            }
        } catch (e: Exception) {
            // В случае ошибки отправляем сообщение об ошибке
            Timber.e(e, "❌ ОШИБКА при импорте транзакций: ${e.message}")
            emit(ImportResult.error("Ошибка при импорте: ${e.message}"))
        }
    }
} 