package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common

import android.net.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс для реализации импорта транзакций из различных источников.
 */
interface TransactionImportUseCase {
    /**
     * Импортирует транзакции из файла.
     *
     * @param uri URI файла для импорта
     * @return Flow с результатами импорта (прогресс, успех или ошибка)
     */
    fun importTransactions(uri: Uri): Flow<TransactionImportResult>
} 