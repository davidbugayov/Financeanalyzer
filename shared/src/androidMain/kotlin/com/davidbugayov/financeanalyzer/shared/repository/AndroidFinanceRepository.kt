package com.davidbugayov.financeanalyzer.shared.repository

import com.davidbugayov.financeanalyzer.shared.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate

/**
 * Android-specific реализация FinanceRepository.
 * Пока использует заглушки для избежания циклических зависимостей.
 * Полная интеграция с domain слоем будет добавлена позже.
 */
class AndroidFinanceRepository : FinanceRepository {

    // ===== TRANSACTIONS =====

    override fun getAllTransactions(): Flow<List<Transaction>> {
        // Заглушка - будет реализована с использованием domain репозиториев
        return flow { emit(emptyList()) }
    }

    override suspend fun getTransactionsForPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Transaction> {
        // Заглушка - будет реализована
        return emptyList()
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        // Заглушка - будет реализована
        return null
    }

    override suspend fun createTransaction(transaction: Transaction): String {
        // Заглушка - будет реализована
        return transaction.id
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        // Заглушка - будет реализована
    }

    override suspend fun deleteTransaction(id: String) {
        // Заглушка - будет реализована
    }

    override suspend fun getTransactionsByCategory(categoryId: String): List<Transaction> {
        // Заглушка - будет реализована
        return emptyList()
    }

    override suspend fun getTransactionsByType(isExpense: Boolean): List<Transaction> {
        // Заглушка - будет реализована
        return emptyList()
    }

    // ===== CATEGORIES =====

    override fun getAllCategories(): Flow<List<Category>> {
        return flow { emit(emptyList()) }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        // Заглушка - будет реализована
        return null
    }

    override suspend fun createCategory(category: Category): Long {
        // Заглушка - будет реализована
        return category.id
    }

    override suspend fun updateCategory(category: Category) {
        // Заглушка - будет реализована
    }

    override suspend fun deleteCategory(id: Long) {
        // Заглушка - будет реализована
    }

    override suspend fun getCategoriesByType(isExpense: Boolean): List<Category> {
        // Заглушка - будет реализована
        return emptyList()
    }

    // ===== WALLETS =====

    override fun getAllWallets(): Flow<List<Wallet>> {
        return flow { emit(emptyList()) }
    }

    override suspend fun getWalletById(id: String): Wallet? {
        // Заглушка - будет реализована
        return null
    }

    override suspend fun createWallet(wallet: Wallet): String {
        // Заглушка - будет реализована
        return wallet.id
    }

    override suspend fun updateWallet(wallet: Wallet) {
        // Заглушка - будет реализована
    }

    override suspend fun deleteWallet(id: String) {
        // Заглушка - будет реализована
    }

    override suspend fun getWalletsByType(type: WalletType): List<Wallet> {
        // Заглушка - будет реализована
        return emptyList()
    }

    override suspend fun updateWalletBalance(walletId: String, newBalance: Money) {
        // Заглушка - будет реализована
    }

    // ===== ANALYTICS & REPORTS =====

    override suspend fun getTotalStatsForPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): FinancialStats {
        return FinancialStats(
            totalIncome = Money.zero(),
            totalExpenses = Money.zero(),
            netIncome = Money.zero(),
            transactionCount = 0,
            averageTransaction = Money.zero(),
            period = "$startDate - $endDate"
        )
    }

    override suspend fun getCategoryStatsForPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CategoryStats> {
        return emptyList()
    }

    override suspend fun getExpenseTrends(months: Int): List<ExpenseTrend> {
        return emptyList()
    }

    // ===== UTILITIES =====

    override suspend fun clearAllData() {
        // Заглушка - будет реализована
    }

    override suspend fun isConnected(): Boolean {
        return true
    }

    override suspend fun syncData(): SyncResult {
        return SyncResult.Success
    }
}
