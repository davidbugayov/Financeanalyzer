package com.davidbugayov.financeanalyzer.shared.repository

import com.davidbugayov.financeanalyzer.shared.model.Wallet

/**
 * Репозиторий для работы с кошельками в KMP.
 */
interface WalletRepository {
    /**
     * Получает все кошельки.
     */
    suspend fun getAllWallets(): List<Wallet>

    /**
     * Получает кошелек по ID.
     */
    suspend fun getWalletById(id: String): Wallet?

    /**
     * Создает новый кошелек.
     */
    suspend fun createWallet(wallet: Wallet): String

    /**
     * Обновляет кошелёк.
     */
    suspend fun updateWallet(wallet: Wallet)

    /**
     * Удаляет кошелек по ID.
     */
    suspend fun deleteWallet(id: String)

    /**
     * Получает кошельки по типу.
     */
    suspend fun getWalletsByType(type: com.davidbugayov.financeanalyzer.shared.model.WalletType): List<Wallet>

    /**
     * Обновляет баланс кошелька.
     */
    suspend fun updateWalletBalance(walletId: String, newBalance: com.davidbugayov.financeanalyzer.shared.model.Money)

    /**
     * Очищает все кошельки.
     */
    suspend fun clearAllWallets()
}