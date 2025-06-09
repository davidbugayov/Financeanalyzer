package com.davidbugayov.financeanalyzer.domain.repository

/**
 * Событие изменения данных в репозитории
 */
sealed class DataChangeEvent {
    /**
     * Событие изменения транзакции
     * @param transactionId ID измененной транзакции или null для массовых изменений
     */
    data class TransactionChanged(val transactionId: String?) : DataChangeEvent()
} 