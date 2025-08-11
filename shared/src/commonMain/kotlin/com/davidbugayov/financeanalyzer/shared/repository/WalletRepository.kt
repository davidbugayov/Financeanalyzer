package com.davidbugayov.financeanalyzer.shared.repository

import com.davidbugayov.financeanalyzer.shared.model.Wallet
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observeWallets(): Flow<List<Wallet>>
    suspend fun getAllWallets(): List<Wallet>
    suspend fun updateWallet(wallet: Wallet)
}


