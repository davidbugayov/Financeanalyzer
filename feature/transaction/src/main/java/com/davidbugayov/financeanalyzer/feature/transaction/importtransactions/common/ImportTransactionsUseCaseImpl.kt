package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common

import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.manager.ImportTransactionsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

/**
 * Реализация UseCase для импорта транзакций из файла.
 * Делегирует работу импорта транзакций в ImportTransactionsManager.
 */
class ImportTransactionsUseCaseImpl(
    private val importTransactionsManager: ImportTransactionsManager,
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
        progressCallback: ImportProgressCallback,
    ): Flow<ImportResult> =
        flow {
            Timber.d("ImportTransactionsUseCaseImpl - начало импорта с URI: $uri")

            // ImportTransactionsManager.importFromUri теперь возвращает Flow.
            // Мы используем flowOn перед emitAll, чтобы контекст эмиссии совпадал с контекстом текущего flow
            val managerFlow =
                importTransactionsManager
                    .importFromUri(uri, progressCallback)
                    .onStart {
                        Timber.d("ImportTransactionsUseCaseImpl - Flow от менеджера стартовал")
                        // Можно эмитить начальный прогресс здесь, если менеджер или BankImportUseCase этого не делают первыми
                        // emit(ImportResult.progress(0, 100, "Подготовка к импорту..."))
                    }.catch { e ->
                        Timber.e(e, "❌ ОШИБКА в Flow импорта транзакций: ${e.message}")
                        emit(ImportResult.error("Ошибка при импорте: ${e.message}"))
                    }.flowOn(Dispatchers.IO) // Убедимся, что эмиссии от managerFlow происходят в правильном контексте

            emitAll(managerFlow)
        }.flowOn(
            Dispatchers.IO,
        ) // Применяем flowOn ко всему Flow, чтобы гарантировать правильный контекст для всех операций
}
