package com.davidbugayov.financeanalyzer.shared

import com.davidbugayov.financeanalyzer.shared.model.BalanceMetrics
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.repository.AchievementsRepository
import com.davidbugayov.financeanalyzer.shared.repository.SubcategoryRepository
import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.shared.repository.WalletRepository
import com.davidbugayov.financeanalyzer.shared.usecase.AchievementEngine
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateEnhancedFinancialMetricsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateExpenseDisciplineIndexUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateFinancialHealthScoreUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculatePeerComparisonUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateRetirementForecastUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.FilterTransactionsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetCategoriesWithAmountUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetExpenseOptimizationRecommendationsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetProfileAnalyticsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetSmartExpenseTipsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetTransactionByIdUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GoalProgressUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GroupTransactionsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.PredictFutureExpensesUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.UpdateWalletBalancesUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.subcategory.AddSubcategoryUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.subcategory.DeleteSubcategoryUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.subcategory.GetSubcategoriesByCategoryIdUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.subcategory.GetSubcategoryByIdUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.transaction.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.transaction.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.transaction.GetPagedTransactionsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.transaction.GetRecentTransactionsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.transaction.GetTransactionsForPeriodFlowUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.transaction.GetTransactionsForPeriodUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.transaction.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.wallet.AllocateIncomeUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.widgets.UpdateWidgetsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

/**
 * Простой фасад KMP для вызова из iOS/Android.
 */
class SharedFacade {

    private val transactionRepository: TransactionRepository?
    private val walletRepository: WalletRepository?
    private val subcategoryRepository: SubcategoryRepository?
    private val achievementsRepository: AchievementsRepository?
    private val appScope: kotlinx.coroutines.CoroutineScope?

    constructor(
        transactionRepository: TransactionRepository? = null,
        walletRepository: WalletRepository? = null,
        subcategoryRepository: SubcategoryRepository? = null,
        achievementsRepository: AchievementsRepository? = null,
        appScope: kotlinx.coroutines.CoroutineScope? = null
    ) {
        this.transactionRepository = transactionRepository
        this.walletRepository = walletRepository
        this.subcategoryRepository = subcategoryRepository
        this.achievementsRepository = achievementsRepository
        this.appScope = appScope
        this.calculateBalanceMetrics = CalculateBalanceMetricsUseCase()
        this.calculateCategoryStatsUseCase = CalculateCategoryStatsUseCase()
        this.getCategoriesWithAmountUseCase = GetCategoriesWithAmountUseCase()
        this.calculateExpenseDisciplineIndex = CalculateExpenseDisciplineIndexUseCase()
        this.calculatePeerComparison = CalculatePeerComparisonUseCase()
        this.predictFutureExpenses = PredictFutureExpensesUseCase()
        this.calculateRetirementForecast = CalculateRetirementForecastUseCase()
        this.calculateFinancialHealthScore = CalculateFinancialHealthScoreUseCase()
        this.calculateEnhancedFinancialMetrics = CalculateEnhancedFinancialMetricsUseCase(
            calculateFinancialHealthScore,
            calculateExpenseDisciplineIndex,
            calculateRetirementForecast,
            calculatePeerComparison,
        )
        this.getSmartExpenseTips = GetSmartExpenseTipsUseCase()
        this.getProfileAnalytics = GetProfileAnalyticsUseCase()
        this.filterTransactionsUseCase = FilterTransactionsUseCase()
        this.groupTransactionsUseCase = GroupTransactionsUseCase()
        this.validateTransactionUseCase = ValidateTransactionUseCase()
        this.exportTransactionsToCSVUseCase = ExportTransactionsToCSVUseCase()
        this.getTransactionByIdUseCase = GetTransactionByIdUseCase()
        this.loadTransactions = transactionRepository?.let { LoadTransactionsUseCase(it) }
        this.goalProgressUseCase = GoalProgressUseCase()
        this.updateWalletBalancesUseCase = walletRepository?.let { UpdateWalletBalancesUseCase(it) }
        this.getSubcategoriesByCategoryIdUseCase =
            subcategoryRepository?.let { GetSubcategoriesByCategoryIdUseCase(it) }
        this.addTransactionUseCase = transactionRepository?.let { AddTransactionUseCase(it) }
        this.updateTransactionUseCase = transactionRepository?.let { UpdateTransactionUseCase(it) }
        this.deleteTransactionUseCase = transactionRepository?.let { DeleteTransactionUseCase(it) }
        this.getTransactionsForPeriodFlow = transactionRepository?.let { GetTransactionsForPeriodFlowUseCase(it) }
        this.achievementEngine =
            if (achievementsRepository != null && appScope != null) AchievementEngine(
                achievementsRepository,
                appScope
            ) else null
        this.getExpenseOptimizationRecommendations = GetExpenseOptimizationRecommendationsUseCase()
        this.getSubcategoryByIdUseCase = subcategoryRepository?.let { GetSubcategoryByIdUseCase(it) }
        this.deleteSubcategoryUseCase = subcategoryRepository?.let { DeleteSubcategoryUseCase(it) }
        this.addSubcategoryUseCase = subcategoryRepository?.let { AddSubcategoryUseCase(it) }
        this.allocateIncomeUseCase = walletRepository?.let { AllocateIncomeUseCase(it) }
        this.getPagedTransactionsUseCase = transactionRepository?.let { GetPagedTransactionsUseCase(it) }
        this.getTransactionsForPeriodUseCaseKmp = transactionRepository?.let { GetTransactionsForPeriodUseCase(it) }
        this.getRecentTransactionsUseCase = transactionRepository?.let { GetRecentTransactionsUseCase(it) }
        this.updateWidgets = UpdateWidgetsUseCase()
    }

    private val calculateBalanceMetrics: CalculateBalanceMetricsUseCase
    private val calculateCategoryStatsUseCase: CalculateCategoryStatsUseCase
    private val getCategoriesWithAmountUseCase: GetCategoriesWithAmountUseCase
    private val calculateExpenseDisciplineIndex: CalculateExpenseDisciplineIndexUseCase
    private val calculatePeerComparison: CalculatePeerComparisonUseCase
    private val predictFutureExpenses: PredictFutureExpensesUseCase
    private val calculateRetirementForecast: CalculateRetirementForecastUseCase
    private val calculateFinancialHealthScore: CalculateFinancialHealthScoreUseCase
    private val calculateEnhancedFinancialMetrics: CalculateEnhancedFinancialMetricsUseCase
    private val getSmartExpenseTips: GetSmartExpenseTipsUseCase
    private val getProfileAnalytics: GetProfileAnalyticsUseCase
    private val filterTransactionsUseCase: FilterTransactionsUseCase
    private val groupTransactionsUseCase: GroupTransactionsUseCase
    private val validateTransactionUseCase: ValidateTransactionUseCase
    private val exportTransactionsToCSVUseCase: ExportTransactionsToCSVUseCase
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase
    private val loadTransactions: LoadTransactionsUseCase?
    private val goalProgressUseCase: GoalProgressUseCase
    private val updateWalletBalancesUseCase: UpdateWalletBalancesUseCase?
    private val getSubcategoriesByCategoryIdUseCase: GetSubcategoriesByCategoryIdUseCase?
    private val addTransactionUseCase: AddTransactionUseCase?
    private val updateTransactionUseCase: UpdateTransactionUseCase?
    private val deleteTransactionUseCase: DeleteTransactionUseCase?
    private val getTransactionsForPeriodFlow: GetTransactionsForPeriodFlowUseCase?
    private val achievementEngine: AchievementEngine?
    private val getExpenseOptimizationRecommendations: GetExpenseOptimizationRecommendationsUseCase
    private val getSubcategoryByIdUseCase: GetSubcategoryByIdUseCase?
    private val deleteSubcategoryUseCase: DeleteSubcategoryUseCase?
    private val addSubcategoryUseCase: AddSubcategoryUseCase?
    private val allocateIncomeUseCase: AllocateIncomeUseCase?
    private val getPagedTransactionsUseCase: GetPagedTransactionsUseCase?
    private val getTransactionsForPeriodUseCaseKmp: GetTransactionsForPeriodUseCase?
    private val getRecentTransactionsUseCase: GetRecentTransactionsUseCase?
    private val updateWidgets: UpdateWidgetsUseCase

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
     * Профильная аналитика по транзакциям.
     */
    fun profileAnalytics(
        transactions: List<Transaction>,
        currencyCode: String,
        totalWallets: Int = 0,
    ): com.davidbugayov.financeanalyzer.shared.model.ProfileAnalytics {
        val currency = Currency.fromCode(currencyCode)
        return getProfileAnalytics(transactions, currency, totalWallets)
    }

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
     * Валидация транзакции (коды ошибок возвращаются для UI-локализации).
     */
    fun validateTransaction(amount: String, category: String, source: String): ValidateTransactionUseCase.Result =
        validateTransactionUseCase.invoke(amount, category, source)

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
     * Загрузка транзакций (если передан репозиторий в конструктор фасада).
     */
    suspend fun loadTransactions(): List<Transaction> =
        loadTransactions?.invoke() ?: emptyList()

    fun goalProgress(current: Money, target: Money): Double =
        goalProgressUseCase(current, target)

    suspend fun recomputeWalletBalances(transactions: List<Transaction>) {
        updateWalletBalancesUseCase?.invoke(transactions)
    }

    suspend fun updateWalletBalances(
        walletIdsToUpdate: List<String>,
        amountForWallets: Money,
        originalTransaction: Transaction?,
    ) {
        // Простая реализация - пересчитываем все балансы
        updateWalletBalancesUseCase?.invoke(emptyList())
    }

    fun observeSubcategories(categoryId: Long): kotlinx.coroutines.flow.Flow<List<com.davidbugayov.financeanalyzer.shared.model.Subcategory>>? =
        getSubcategoriesByCategoryIdUseCase?.invoke(categoryId)

    suspend fun addTransaction(transaction: Transaction): Boolean {
        return try {
            addTransactionUseCase?.invoke(transaction)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateTransaction(transaction: Transaction): Boolean {
        return try {
            updateTransactionUseCase?.invoke(transaction)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteTransaction(transaction: Transaction): Boolean {
        return try {
            deleteTransactionUseCase?.invoke(transaction.id)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteTransaction(id: String): Boolean {
        return try {
            deleteTransactionUseCase?.invoke(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun achievementsFlow(): kotlinx.coroutines.flow.SharedFlow<com.davidbugayov.financeanalyzer.shared.model.Achievement>? =
        achievementEngine?.newAchievements

    fun notifyTransactionAddedForAchievements() {
        achievementEngine?.onTransactionAdded()
    }

    fun transactionsForPeriodFlow(start: LocalDate, end: LocalDate): kotlinx.coroutines.flow.Flow<List<Transaction>>? =
        getTransactionsForPeriodFlow?.invoke(start, end)

    /**
     * Утилита создания суммы из double (для удобства Swift-клиента).
     */
    fun moneyFromDouble(value: Double, currencyCode: String): Money {
        val currency = Currency.fromCode(currencyCode)
        return Money.fromMajor(value, currency)
    }

    // === Новые методы для остальных use case ===

    /**
     * Рекомендации по оптимизации расходов.
     */
    fun expenseOptimizationRecommendations(transactions: List<Transaction>): List<String> =
        getExpenseOptimizationRecommendations.invoke(transactions)

    /**
     * Получение подкатегории по ID.
     */
    suspend fun getSubcategoryById(id: Long): com.davidbugayov.financeanalyzer.shared.model.Subcategory? =
        getSubcategoryByIdUseCase?.invoke(id)

    suspend fun getSubcategoriesByCategoryId(categoryId: Long): List<com.davidbugayov.financeanalyzer.shared.model.Subcategory> =
        try {
            getSubcategoriesByCategoryIdUseCase?.invoke(categoryId)?.let { flow ->
                flow.first()
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

    /**
     * Удаление подкатегории.
     */
    suspend fun deleteSubcategory(subcategoryId: Long) {
        deleteSubcategoryUseCase?.invoke(subcategoryId)
    }

    /**
     * Добавление подкатегории.
     */
    suspend fun addSubcategory(name: String, categoryId: Long): Long =
        addSubcategoryUseCase?.invoke(name, categoryId) ?: -1L

    /**
     * Распределение дохода между кошельками.
     */
    suspend fun allocateIncome(income: Money): Boolean =
        allocateIncomeUseCase?.invoke(income) ?: false

    /**
     * Получение пагинированных транзакций.
     */
    suspend fun getPagedTransactions(pageSize: Int): List<Transaction> =
        getPagedTransactionsUseCase?.invoke(pageSize) ?: emptyList()

    /**
     * Получение транзакций за период (без Flow).
     */
    suspend fun getTransactionsForPeriod(startDate: LocalDate, endDate: LocalDate): List<Transaction> =
        getTransactionsForPeriodUseCaseKmp?.invoke(startDate, endDate) ?: emptyList()

    /**
     * Получение недавних транзакций.
     */
    suspend fun getRecentTransactions(days: Int): List<Transaction> =
        getRecentTransactionsUseCase?.invoke(days) ?: emptyList()

    /**
     * Обновление виджетов (заглушка для KMP).
     */
    fun updateWidgets() {
        updateWidgets.invoke()
    }
}


