package com.davidbugayov.financeanalyzer.domain.repository

/**
 * Sealed class для представления событий изменения данных в репозитории.
 * Используется для уведомления подписчиков о различных типах изменений.
 */
sealed class DataChangeEvent {
    /**
     * Событие изменения транзакции.
     * @param transactionId ID измененной транзакции или null, если изменено несколько транзакций.
     */
    data class TransactionChanged(val transactionId: String? = null) : DataChangeEvent()

    /**
     * Событие изменения кошелька.
     * @param walletId ID измененного кошелька или null, если изменено несколько кошельков.
     */
    data class WalletChanged(val walletId: String? = null) : DataChangeEvent()

    /**
     * Событие изменения категории.
     * @param categoryId ID измененной категории или null, если изменено несколько категорий.
     */
    data class CategoryChanged(val categoryId: String? = null) : DataChangeEvent()
}
