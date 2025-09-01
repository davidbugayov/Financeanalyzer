package com.davidbugayov.financeanalyzer.shared.repository

import com.davidbugayov.financeanalyzer.shared.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate

/**
 * Адаптер, который связывает KMP FinanceRepository с реальными репозиториями.
 * Работает в commonMain для совместимости с KMP.
 */
class FinanceRepositoryAdapter(
    private val transactionRepository: TransactionRepository?,
    private val walletRepository: WalletRepository?,
    private val subcategoryRepository: SubcategoryRepository?,
    private val achievementsRepository: AchievementsRepository?
) : FinanceRepository {

    // ===== TRANSACTIONS =====

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionRepository?.observeTransactions()
            ?: flow { emit(emptyList()) }
    }

    override suspend fun getTransactionsForPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Transaction> {
        return transactionRepository?.getTransactionsForPeriod(startDate, endDate)
            ?: emptyList()
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return transactionRepository?.getTransactionById(id)
    }

    override suspend fun createTransaction(transaction: Transaction): String {
        transactionRepository?.addTransaction(transaction)
        return transaction.id
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionRepository?.updateTransaction(transaction)
    }

    override suspend fun deleteTransaction(id: String) {
        transactionRepository?.deleteTransaction(id)
    }

    override suspend fun getTransactionsByCategory(categoryId: String): List<Transaction> {
        return transactionRepository?.getTransactionsByCategory(categoryId)
            ?: emptyList()
    }

    override suspend fun getTransactionsByType(isExpense: Boolean): List<Transaction> {
        return transactionRepository?.getTransactionsByType(isExpense)
            ?: emptyList()
    }

    // ===== CATEGORIES =====

    override fun getAllCategories(): Flow<List<Category>> {
        return subcategoryRepository?.getAllCategories()
            ?: flow { emit(emptyList()) }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return subcategoryRepository?.getCategoryById(id)
    }

    override suspend fun createCategory(category: Category): Long {
        return subcategoryRepository?.createCategory(category) ?: category.id
    }

    override suspend fun updateCategory(category: Category) {
        subcategoryRepository?.updateCategory(category)
    }

    override suspend fun deleteCategory(id: Long) {
        subcategoryRepository?.deleteCategory(id)
    }

    override suspend fun getCategoriesByType(isExpense: Boolean): List<Category> {
        return subcategoryRepository?.getCategoriesByType(isExpense)
            ?: emptyList()
    }

    // ===== WALLETS =====

    override fun getAllWallets(): Flow<List<Wallet>> {
        return flow {
            val wallets = walletRepository?.getAllWallets() ?: emptyList()
            emit(wallets)
        }
    }

    override suspend fun getWalletById(id: String): Wallet? {
        return walletRepository?.getWalletById(id)
    }

    override suspend fun createWallet(wallet: Wallet): String {
        return walletRepository?.createWallet(wallet) ?: wallet.id
    }

    override suspend fun updateWallet(wallet: Wallet) {
        walletRepository?.updateWallet(wallet)
    }

    override suspend fun deleteWallet(id: String) {
        walletRepository?.deleteWallet(id)
    }

    override suspend fun getWalletsByType(type: WalletType): List<Wallet> {
        return walletRepository?.getWalletsByType(type)
            ?: emptyList()
    }

    override suspend fun updateWalletBalance(walletId: String, newBalance: Money) {
        walletRepository?.updateWalletBalance(walletId, newBalance)
    }

    // ===== ANALYTICS & REPORTS =====

    override suspend fun getTotalStatsForPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): FinancialStats {
        // Для простоты возвращаем базовую статистику
        // В будущем можно реализовать расчет через репозиторий
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
        transactionRepository?.clearAllTransactions()
        walletRepository?.clearAllWallets()
        subcategoryRepository?.clearAllCategories()
        achievementsRepository?.clearAllAchievements()
    }

    override suspend fun isConnected(): Boolean {
        return true
    }

    override suspend fun syncData(): SyncResult {
        return SyncResult.Success
    }
}
