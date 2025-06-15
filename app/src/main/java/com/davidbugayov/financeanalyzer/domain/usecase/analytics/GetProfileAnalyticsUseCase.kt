package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.ProfileAnalytics
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetProfileAnalyticsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) {

    suspend operator fun invoke(): Result<ProfileAnalytics> {
        return try {
            val transactions = transactionRepository.getAllTransactions().first()
            val wallets = walletRepository.getAllWallets().first()
            
            val totalTransactions = transactions.size
            val totalWallets = wallets.size
            
            Result.Success(
                ProfileAnalytics(
                    totalTransactions = totalTransactions,
                    totalWallets = totalWallets
                )
            )
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 