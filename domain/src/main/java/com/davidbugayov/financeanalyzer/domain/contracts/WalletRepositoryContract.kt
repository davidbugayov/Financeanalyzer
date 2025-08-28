package com.davidbugayov.financeanalyzer.domain.contracts

import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.model.WalletType
import com.davidbugayov.financeanalyzer.shared.model.Money
import kotlinx.coroutines.flow.Flow

/**
 * Contract interface for Wallet repository operations.
 * Provides a clean API for wallet management across the application.
 */
interface WalletRepositoryContract {

    /**
     * Retrieves all wallets as a Flow for reactive updates
     */
    fun getAllWallets(): Flow<List<Wallet>>

    /**
     * Retrieves all wallets as a suspend function
     */
    suspend fun getAllWalletsList(): List<Wallet>

    /**
     * Retrieves a specific wallet by ID
     */
    suspend fun getWalletById(id: Long): Wallet?

    /**
     * Retrieves wallets by type
     */
    suspend fun getWalletsByType(type: WalletType): List<Wallet>

    /**
     * Retrieves the default wallet (usually the first one or marked as default)
     */
    suspend fun getDefaultWallet(): Wallet?

    /**
     * Creates a new wallet
     */
    suspend fun createWallet(wallet: Wallet): Long

    /**
     * Updates an existing wallet
     */
    suspend fun updateWallet(wallet: Wallet)

    /**
     * Deletes a wallet by ID
     */
    suspend fun deleteWallet(id: Long)

    /**
     * Updates the balance of a specific wallet
     */
    suspend fun updateWalletBalance(walletId: Long, newBalance: Money)

    /**
     * Calculates the total balance across all wallets
     */
    suspend fun getTotalBalance(): Money

    /**
     * Calculates the total balance for wallets of a specific type
     */
    suspend fun getTotalBalanceByType(type: WalletType): Money

    /**
     * Sets a wallet as the default wallet
     */
    suspend fun setDefaultWallet(walletId: Long)

    /**
     * Checks if a wallet with the given name exists
     */
    suspend fun walletExists(name: String): Boolean

    /**
     * Gets the count of wallets
     */
    suspend fun getWalletCount(): Int

    /**
     * Validates wallet data before operations
     */
    suspend fun validateWallet(wallet: Wallet): Boolean

    /**
     * Gets wallets sorted by balance (descending)
     */
    suspend fun getWalletsSortedByBalance(): List<Wallet>

    /**
     * Gets wallets sorted by name (ascending)
     */
    suspend fun getWalletsSortedByName(): List<Wallet>
}
