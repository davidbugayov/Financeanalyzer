package com.davidbugayov.financeanalyzer.shared

import com.davidbugayov.financeanalyzer.shared.model.BalanceMetrics
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsProviderBridge
import com.davidbugayov.financeanalyzer.shared.repository.FinanceRepository
import com.davidbugayov.financeanalyzer.shared.repository.FinanceRepositoryFactory
import com.davidbugayov.financeanalyzer.shared.repository.FinanceRepositoryAdapter
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateEnhancedFinancialMetricsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateExpenseDisciplineIndexUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateFinancialHealthScoreUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculatePeerComparisonUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateRetirementForecastUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.FilterTransactionsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetExpenseOptimizationRecommendationsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetSmartExpenseTipsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetTransactionByIdUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GoalProgressUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GroupTransactionsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.UpdateWalletBalancesUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.ValidateTransactionUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate

/**
 * Простой фасад KMP для вызова из iOS/Android.
 * Теперь использует единый FinanceRepository для всех операций.
 */
class SharedFacade {

    private val financeRepository: FinanceRepository
    private val appScope: kotlinx.coroutines.CoroutineScope?

    constructor(
        financeRepository: FinanceRepository = FinanceRepositoryFactory.create(),
        appScope: kotlinx.coroutines.CoroutineScope? = null
    ) {
        this.financeRepository = financeRepository
        this.appScope = appScope

        // Инициализация use cases
        this.calculateBalanceMetrics = CalculateBalanceMetricsUseCase()
        this.calculateEnhancedFinancialMetrics = CalculateEnhancedFinancialMetricsUseCase(
            CalculateFinancialHealthScoreUseCase(),
            CalculateExpenseDisciplineIndexUseCase(),
            CalculateRetirementForecastUseCase(),
            CalculatePeerComparisonUseCase(),
        )
        this.getSmartExpenseTips = GetSmartExpenseTipsUseCase()
        this.filterTransactionsUseCase = FilterTransactionsUseCase()
        this.groupTransactionsUseCase = GroupTransactionsUseCase()
        this.validateTransactionUseCase = ValidateTransactionUseCase()
        this.exportTransactionsToCSVUseCase = ExportTransactionsToCSVUseCase()
        this.getTransactionByIdUseCase = GetTransactionByIdUseCase()
        this.goalProgressUseCase = GoalProgressUseCase()
        this.updateWalletBalancesUseCase = UpdateWalletBalancesUseCase()
        this.getExpenseOptimizationRecommendations = GetExpenseOptimizationRecommendationsUseCase()
    }

    // Constructor that creates FinanceRepository from provided repositories
    constructor(
        transactionRepository: com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository? = null,
        walletRepository: com.davidbugayov.financeanalyzer.shared.repository.WalletRepository? = null,
        subcategoryRepository: com.davidbugayov.financeanalyzer.shared.repository.SubcategoryRepository? = null,
        achievementsRepository: com.davidbugayov.financeanalyzer.shared.repository.AchievementsRepository? = null,
        appScope: kotlinx.coroutines.CoroutineScope? = null
    ) : this(
        financeRepository = FinanceRepositoryAdapter(
            transactionRepository,
            walletRepository,
            subcategoryRepository,
            achievementsRepository
        ),
        appScope = appScope
    )

    private val calculateBalanceMetrics: CalculateBalanceMetricsUseCase
    private val calculateEnhancedFinancialMetrics: CalculateEnhancedFinancialMetricsUseCase
    private val getSmartExpenseTips: GetSmartExpenseTipsUseCase

    private val filterTransactionsUseCase: FilterTransactionsUseCase
    private val groupTransactionsUseCase: GroupTransactionsUseCase
    private val validateTransactionUseCase: ValidateTransactionUseCase
    private val exportTransactionsToCSVUseCase: ExportTransactionsToCSVUseCase
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase
    private val goalProgressUseCase: GoalProgressUseCase
    private val updateWalletBalancesUseCase: UpdateWalletBalancesUseCase
    private val getExpenseOptimizationRecommendations: GetExpenseOptimizationRecommendationsUseCase

    /**
     * Считает метрики по списку транзакций.
     */
    fun calculateMetrics(
        transactions: List<Transaction>,
        currencyCode: String,
        start: LocalDate?,
        end: LocalDate?,
    ): BalanceMetrics {
        val currency = Currency.fromCode(currencyCode)
        return calculateBalanceMetrics(transactions, currency, start, end)
    }

    /**
     * Коды советов по экономии.
     */
    fun smartExpenseTips(transactions: List<Transaction>): List<String> =
        getSmartExpenseTips.invoke(transactions)

    /**
     * Фильтрация и группировка транзакций.
     */
    fun filterTransactions(
        transactions: List<Transaction>,
        periodType: com.davidbugayov.financeanalyzer.shared.model.filter.PeriodType,
        now: LocalDate,
        customStart: LocalDate? = null,
        customEnd: LocalDate? = null,
        isExpense: Boolean? = null,
    ): List<Transaction> = filterTransactionsUseCase(transactions, periodType, now, customStart, customEnd, isExpense)

    fun groupTransactions(
        transactions: List<Transaction>,
        keyType: GroupTransactionsUseCase.KeyType,
    ): Map<String, List<Transaction>> = groupTransactionsUseCase(transactions, keyType)

    /**
     * Экспорт транзакций в CSV (строка).
     */
    fun exportTransactionsCsv(
        transactions: List<Transaction>,
        includeHeader: Boolean = true,
        separator: Char = ',',
    ): String = exportTransactionsToCSVUseCase(transactions, includeHeader, separator)

    /**
     * Поиск транзакции по id в списке.
     */
    fun getTransactionById(transactions: List<Transaction>, id: String): Transaction? =
        getTransactionByIdUseCase(transactions, id)

    /**
     * Загрузка всех транзакций.
     */
    suspend fun loadTransactions(): List<Transaction> {
        return financeRepository.getAllTransactions().first()
    }

    fun goalProgress(current: Money, target: Money): Double =
        goalProgressUseCase(current, target)


    fun transactionsForPeriodFlow(start: LocalDate, end: LocalDate): kotlinx.coroutines.flow.Flow<List<Transaction>> =
        kotlinx.coroutines.flow.flow {
            // Try FinanceRepository first, fallback to loadTransactions + filter
            var transactions = financeRepository.getTransactionsForPeriod(start, end)
            if (transactions.isEmpty()) {
                try {
                    val allTransactions = loadTransactions()
                    transactions = allTransactions.filter { transaction ->
                        val transactionDate = transaction.date
                        transactionDate >= start && transactionDate <= end
                    }
                } catch (e: Exception) {
                    // Handle exceptions and emit empty list
                }
            }
            emit(transactions)
        }.catch { e ->
            // Handle exceptions and emit empty list
            emit(emptyList())
        }

    /**
     * Рекомендации по оптимизации расходов.
     */
    fun expenseOptimizationRecommendations(transactions: List<Transaction>): List<String> =
        getExpenseOptimizationRecommendations.invoke(transactions)

    /**
     * Получение категории по ID.
     */
    suspend fun getCategoryById(id: Long): com.davidbugayov.financeanalyzer.shared.model.Category? =
        financeRepository.getCategoryById(id)

    /**
     * Получение всех категорий.
     */
    fun getAllCategories(): kotlinx.coroutines.flow.Flow<List<com.davidbugayov.financeanalyzer.shared.model.Category>> =
        financeRepository.getAllCategories()

    /**
     * Создание новой категории.
     */
    suspend fun createCategory(category: com.davidbugayov.financeanalyzer.shared.model.Category): Long =
        financeRepository.createCategory(category)

    /**
     * Получение кошелька по ID.
     */
    suspend fun getWalletById(id: String): com.davidbugayov.financeanalyzer.shared.model.Wallet? =
        financeRepository.getWalletById(id)

    /**
     * Получение всех кошельков.
     */
    fun getAllWallets(): kotlinx.coroutines.flow.Flow<List<com.davidbugayov.financeanalyzer.shared.model.Wallet>> =
        financeRepository.getAllWallets()

    /**
     * Создание нового кошелька.
     */
    suspend fun createWallet(wallet: com.davidbugayov.financeanalyzer.shared.model.Wallet): String =
        financeRepository.createWallet(wallet)

    // ===== LEGACY METHODS FOR BACKWARD COMPATIBILITY =====

    /**
     * Получение подкатегории по ID (legacy method).
     */
    suspend fun getSubcategoryById(id: Long): com.davidbugayov.financeanalyzer.shared.model.Subcategory? =
        getCategoryById(id)?.let { category ->
            // Конвертация Category в Subcategory для обратной совместимости
            com.davidbugayov.financeanalyzer.shared.model.Subcategory(
                id = category.id,
                categoryId = 0L, // Default parent category ID
                name = category.name,
                isCustom = category.isCustom,
            )
        }

    /**
     * Получение подкатегорий по ID категории (legacy method).
     */
    suspend fun getSubcategoriesByCategoryId(categoryId: Long): List<com.davidbugayov.financeanalyzer.shared.model.Subcategory> =
        financeRepository.getCategoriesByType(true).map { category ->
            com.davidbugayov.financeanalyzer.shared.model.Subcategory(
                id = category.id,
                categoryId = categoryId,
                name = category.name,
                isCustom = category.isCustom,
            )
        }

    /**
     * Добавление подкатегории (legacy method).
     */
    suspend fun addSubcategory(name: String, categoryId: Long): Long =
        createCategory(
            com.davidbugayov.financeanalyzer.shared.model.Category(
                id = 0L, // Will be assigned by repository
                name = name,
                isExpense = true, // Default to expense
                count = 0,
                isCustom = true
            )
        )

    /**
     * Распределение дохода между кошельками (legacy method).
     */
    suspend fun allocateIncome(income: com.davidbugayov.financeanalyzer.shared.model.Money): Boolean =
        financeRepository.createWallet(
            com.davidbugayov.financeanalyzer.shared.model.Wallet(
                id = "income_allocation_${System.currentTimeMillis()}",
                name = "Income Allocation",
                type = com.davidbugayov.financeanalyzer.shared.model.WalletType.CASH,
                balance = income,
            )
        ).isNotEmpty()

    /**
     * Добавляет транзакцию через репозиторий. Возвращает успех операции.
     *
     * @param transaction транзакция из общего KMP-модуля
     * @return true при успехе, иначе false
     */
    suspend fun addTransaction(transaction: Transaction): Boolean =
        try {
            financeRepository.createTransaction(transaction)
            true
        } catch (t: Throwable) {
            AnalyticsProviderBridge.getProvider()?.logEvent(
                eventName = "kmp_repo_error",
                parameters = mapOf(
                    "op" to "addTransaction",
                    "type" to (t::class.simpleName ?: "Throwable"),
                    "message" to (t.message ?: ""),
                ),
            )
            false
        }

    /**
     * Обновляет транзакцию через репозиторий. Возвращает успех операции.
     *
     * @param transaction транзакция из общего KMP-модуля
     * @return true при успехе, иначе false
     */
    suspend fun updateTransaction(transaction: Transaction): Boolean =
        try {
            financeRepository.updateTransaction(transaction)
            true
        } catch (t: Throwable) {
            AnalyticsProviderBridge.getProvider()?.logEvent(
                eventName = "kmp_repo_error",
                parameters = mapOf(
                    "op" to "updateTransaction",
                    "type" to (t::class.simpleName ?: "Throwable"),
                    "message" to (t.message ?: ""),
                ),
            )
            false
        }

    /**
     * Обновляет балансы кошельков по списку ID.
     *
     * @param walletIdsToUpdate список ID кошельков для обновления
     * @param amountForWallets сумма, на которую изменить баланс (положительная для дохода)
     * @param originalTransaction исходная транзакция (необязательно)
     */
    suspend fun updateWalletBalances(
        walletIdsToUpdate: List<String>,
        amountForWallets: Money,
        originalTransaction: Transaction? = null,
    ) {
        try {
            walletIdsToUpdate.forEach { id ->
                financeRepository.updateWalletBalance(id, amountForWallets)
            }

            // Обновляем виджеты после изменения балансов кошельков
            // В KMP это заглушка - реальное обновление происходит в platform-specific коде
        } catch (t: Throwable) {
            AnalyticsProviderBridge.getProvider()?.logEvent(
                eventName = "kmp_wallet_update_error",
                parameters = mapOf(
                    "op" to "updateWalletBalances",
                    "type" to (t::class.simpleName ?: "Throwable"),
                    "message" to (t.message ?: ""),
                ),
            )
        }
    }

    /**
     * Удаляет транзакцию по её ID из переданного объекта.
     *
     * @param transaction транзакция, ID которой нужно удалить
     * @return true при успехе, иначе false
     */
    suspend fun deleteTransaction(transaction: Transaction): Boolean =
        try {
            financeRepository.deleteTransaction(transaction.id)
            true
        } catch (t: Throwable) {
            AnalyticsProviderBridge.getProvider()?.logEvent(
                eventName = "kmp_repo_error",
                parameters = mapOf(
                    "op" to "deleteTransaction",
                    "type" to (t::class.simpleName ?: "Throwable"),
                    "message" to (t.message ?: ""),
                ),
            )
            false
        }
}


