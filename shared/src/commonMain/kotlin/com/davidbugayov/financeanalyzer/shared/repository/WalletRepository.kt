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
     * Обновляет кошелёк.
     */
    suspend fun updateWallet(wallet: Wallet)
}