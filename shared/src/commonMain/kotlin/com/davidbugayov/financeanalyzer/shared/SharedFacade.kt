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
class SharedFacade(
    private val transactionRepository: TransactionRepository? = null,
    private val walletRepository: WalletRepository? = null,
    private val subcategoryRepository: SubcategoryRepository? = null,
    private val achievementsRepository: AchievementsRepository? = null,
    private val appScope: kotlinx.coroutines.CoroutineScope? = null,
) {
    private val calculateBalanceMetrics = CalculateBalanceMetricsUseCase()
    private val calculateCategoryStatsUseCase = CalculateCategoryStatsUseCase()
    private val getCategoriesWithAmountUseCase = GetCategoriesWithAmountUseCase()
    private val calculateExpenseDisciplineIndex = CalculateExpenseDisciplineIndexUseCase()
    private val calculatePeerComparison = CalculatePeerComparisonUseCase()
    private val predictFutureExpenses = PredictFutureExpensesUseCase()
    private val calculateRetirementForecast = CalculateRetirementForecastUseCase()
    private val calculateFinancialHealthScore = CalculateFinancialHealthScoreUseCase()
    private val calculateEnhancedFinancialMetrics = CalculateEnhancedFinancialMetricsUseCase(
        calculateFinancialHealthScore,
        calculateExpenseDisciplineIndex,
        calculateRetirementForecast,
        calculatePeerComparison,
    )
    private val getSmartExpenseTips = GetSmartExpenseTipsUseCase()
    private val getProfileAnalytics = GetProfileAnalyticsUseCase()
    private val filterTransactionsUseCase = FilterTransactionsUseCase()
    private val groupTransactionsUseCase = GroupTransactionsUseCase()
    private val validateTransactionUseCase = ValidateTransactionUseCase()
    private val exportTransactionsToCSVUseCase = ExportTransactionsToCSVUseCase()
    private val getTransactionByIdUseCase = GetTransactionByIdUseCase()
    private val loadTransactions: LoadTransactionsUseCase? = transactionRepository?.let { LoadTransactionsUseCase(it) }
    private val goalProgressUseCase = GoalProgressUseCase()
    private val updateWalletBalancesUseCase: UpdateWalletBalancesUseCase? =
        walletRepository?.let { UpdateWalletBalancesUseCase(it) }
    private val getSubcategoriesByCategoryIdUseCase: GetSubcategoriesByCategoryIdUseCase? =
        subcategoryRepository?.let { GetSubcategoriesByCategoryIdUseCase(it) }
    private val addTransactionUseCase: AddTransactionUseCase? = transactionRepository?.let { AddTransactionUseCase(it) }
    private val updateTransactionUseCase: UpdateTransactionUseCase? = transactionRepository?.let { UpdateTransactionUseCase(it) }
    private val deleteTransactionUseCase: DeleteTransactionUseCase? = transactionRepository?.let { DeleteTransactionUseCase(it) }
    private val getTransactionsForPeriodFlow: GetTransactionsForPeriodFlowUseCase? = transactionRepository?.let { GetTransactionsForPeriodFlowUseCase(it) }
    private val achievementEngine: AchievementEngine? = if (achievementsRepository != null && appScope != null) AchievementEngine(achievementsRepository, appScope) else null
    private val getExpenseOptimizationRecommendations = GetExpenseOptimizationRecommendationsUseCase()
    private val getSubcategoryByIdUseCase: GetSubcategoryByIdUseCase? =
        subcategoryRepository?.let { GetSubcategoryByIdUseCase(it) }
    private val deleteSubcategoryUseCase: DeleteSubcategoryUseCase? =
        subcategoryRepository?.let { DeleteSubcategoryUseCase(it) }
    private val addSubcategoryUseCase: AddSubcategoryUseCase? = subcategoryRepository?.let { AddSubcategoryUseCase(it) }
    private val allocateIncomeUseCase: AllocateIncomeUseCase? = walletRepository?.let { AllocateIncomeUseCase(it) }
    private val getPagedTransactionsUseCase: GetPagedTransactionsUseCase? =
        transactionRepository?.let { GetPagedTransactionsUseCase(it) }
    private val getTransactionsForPeriodUseCaseKmp: GetTransactionsForPeriodUseCase? =
        transactionRepository?.let { GetTransactionsForPeriodUseCase(it) }
    private val getRecentTransactionsUseCase: GetRecentTransactionsUseCase? =
        transactionRepository?.let { GetRecentTransactionsUseCase(it) }
    private val updateWidgets = UpdateWidgetsUseCase()

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

    fun calculateCategoryStats(transactions: List<Transaction>): Triple<List<com.davidbugayov.financeanalyzer.shared.model.CategoryStats>, Money, Money> =
        calculateCategoryStatsUseCase(transactions)

    fun getCategoriesWithAmount(transactions: List<Transaction>, isExpense: Boolean): List<com.davidbugayov.financeanalyzer.shared.model.CategoryWithAmount> =
        getCategoriesWithAmountUseCase(transactions, isExpense)

    fun expenseDisciplineIndex(transactions: List<Transaction>, periodMonths: Int = 6): Double =
        calculateExpenseDisciplineIndex(transactions, periodMonths)

    fun peerComparison(transactions: List<Transaction>, healthScore: Double): com.davidbugayov.financeanalyzer.shared.model.PeerComparison =
        calculatePeerComparison(transactions, healthScore)

    fun predictExpenses(transactions: List<Transaction>, months: Int = 1): Money =
        predictFutureExpenses(transactions, months)

    fun retirementForecast(
        transactions: List<Transaction>,
        currentAge: Int = 30,
        retirementAge: Int = 65,
        currentSavings: Money = Money.zero(),
        desiredMonthlyPension: Money? = null,
    ): com.davidbugayov.financeanalyzer.shared.model.RetirementForecast =
        calculateRetirementForecast(transactions, currentAge, retirementAge, currentSavings, desiredMonthlyPension)

    /**
     * Возвращает коэффициент финансового здоровья и декомпозицию компонентов.
     */
    fun financialHealthScore(transactions: List<Transaction>, periodMonths: Int = 6): Pair<Double, com.davidbugayov.financeanalyzer.shared.model.HealthScoreBreakdown> =
        calculateFinancialHealthScore(transactions, periodMonths)

    /**
     * Композитный расчет метрик и рекомендаций.
     */
    fun enhancedFinancialMetrics(
        transactions: List<Transaction>,
        currentAge: Int = 30,
        retirementAge: Int = 65,
        currentSavings: Money = Money.zero(),
        desiredMonthlyPension: Money? = null,
    ): com.davidbugayov.financeanalyzer.shared.model.FinancialHealthMetrics =
        calculateEnhancedFinancialMetrics(transactions, currentAge, retirementAge, currentSavings, desiredMonthlyPension)

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


