package com.davidbugayov.financeanalyzer.domain.usecase

import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import kotlinx.coroutines.flow.Flow

/**
 * Use case для импорта транзакций из разных источников.
 * Определяет общий интерфейс для всех типов импорта.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
interface ImportTransactionsUseCase {

    /**
     * Импортирует транзакции из указанного источника.
     * Возвращает поток результатов импорта для обновления UI в реальном времени.
     *
     * @param uri URI источника данных (файл, URL и т.д.)
     * @return Flow с результатами импорта, включая прогресс и итоги
     */
    suspend operator fun invoke(uri: Uri): Flow<ImportResult>
} 