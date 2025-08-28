package com.davidbugayov.financeanalyzer.shared.repository

import com.davidbugayov.financeanalyzer.shared.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Основной интерфейс репозитория для работы с финансовыми данными.
 * Единый контракт для всех платформ (Android, iOS).
 */
interface FinanceRepository {

    // ===== TRANSACTIONS =====

    /**
     * Получить все транзакции
     */
    fun getAllTransactions(): Flow<List<Transaction>>

    /**
     * Получить транзакции за период
     */
    suspend fun getTransactionsForPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Transaction>

    /**
     * Получить транзакцию по ID
     */
    suspend fun getTransactionById(id: String): Transaction?

    /**
     * Создать новую транзакцию
     */
    suspend fun createTransaction(transaction: Transaction): String

    /**
     * Обновить существующую транзакцию
     */
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * Удалить транзакцию по ID
     */
    suspend fun deleteTransaction(id: String)

    /**
     * Получить транзакции по категории
     */
    suspend fun getTransactionsByCategory(categoryId: String): List<Transaction>

    /**
     * Получить транзакции по типу (доходы/расходы)
     */
    suspend fun getTransactionsByType(isExpense: Boolean): List<Transaction>

    // ===== CATEGORIES =====

    /**
     * Получить все категории
     */
    fun getAllCategories(): Flow<List<Category>>

    /**
     * Получить категорию по ID
     */
    suspend fun getCategoryById(id: Long): Category?

    /**
     * Создать новую категорию
     */
    suspend fun createCategory(category: Category): Long

    /**
     * Обновить существующую категорию
     */
    suspend fun updateCategory(category: Category)

    /**
     * Удалить категорию по ID
     */
    suspend fun deleteCategory(id: Long)

    /**
     * Получить категории по типу (доходы/расходы)
     */
    suspend fun getCategoriesByType(isExpense: Boolean): List<Category>

    // ===== WALLETS =====

    /**
     * Получить все кошельки
     */
    fun getAllWallets(): Flow<List<Wallet>>

    /**
     * Получить кошелек по ID
     */
    suspend fun getWalletById(id: String): Wallet?

    /**
     * Создать новый кошелек
     */
    suspend fun createWallet(wallet: Wallet): String

    /**
     * Обновить существующий кошелек
     */
    suspend fun updateWallet(wallet: Wallet)

    /**
     * Удалить кошелек по ID
     */
    suspend fun deleteWallet(id: String)

    /**
     * Получить кошельки по типу
     */
    suspend fun getWalletsByType(type: WalletType): List<Wallet>

    /**
     * Обновить баланс кошелька
     */
    suspend fun updateWalletBalance(walletId: String, newBalance: Money)

    // ===== ANALYTICS & REPORTS =====

    /**
     * Получить общую статистику за период
     */
    suspend fun getTotalStatsForPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): FinancialStats

    /**
     * Получить статистику по категориям за период
     */
    suspend fun getCategoryStatsForPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CategoryStats>

    /**
     * Получить тренды расходов/доходов
     */
    suspend fun getExpenseTrends(
        months: Int = 6
    ): List<ExpenseTrend>

    // ===== UTILITIES =====

    /**
     * Очистить все данные (для тестирования)
     */
    suspend fun clearAllData()

    /**
     * Проверить подключение к хранилищу данных
     */
    suspend fun isConnected(): Boolean

    /**
     * Синхронизировать данные (если поддерживается)
     */
    suspend fun syncData(): SyncResult
}

/**
 * Результат синхронизации данных
 */
sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
    data class PartialSuccess(val syncedItems: Int, val totalItems: Int) : SyncResult()
}

/**
 * Финансовая статистика за период
 */
data class FinancialStats(
    val totalIncome: Money,
    val totalExpenses: Money,
    val netIncome: Money,
    val transactionCount: Int,
    val averageTransaction: Money,
    val period: String
)

/**
 * Тренд расходов
 */
data class ExpenseTrend(
    val month: String,
    val amount: Money,
    val changePercent: Double,
    val categoryBreakdown: Map<String, Money>
)
