package com.davidbugayov.financeanalyzer.shared

import com.davidbugayov.financeanalyzer.shared.model.BalanceMetrics
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetCategoriesWithAmountUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateExpenseDisciplineIndexUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculatePeerComparisonUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.PredictFutureExpensesUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateRetirementForecastUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateFinancialHealthScoreUseCase
import kotlinx.datetime.LocalDate
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateEnhancedFinancialMetricsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetSmartExpenseTipsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetProfileAnalyticsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.FilterTransactionsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GroupTransactionsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.GetTransactionByIdUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.shared.repository.WalletRepository
import com.davidbugayov.financeanalyzer.shared.usecase.GoalProgressUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.UpdateWalletBalancesUseCase
import com.davidbugayov.financeanalyzer.shared.repository.SubcategoryRepository
import com.davidbugayov.financeanalyzer.shared.usecase.subcategory.GetSubcategoriesByCategoryIdUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.transaction.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.transaction.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.transaction.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.shared.repository.AchievementsRepository
import com.davidbugayov.financeanalyzer.shared.usecase.AchievementEngine

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
    private val calculateCategoryStats = CalculateCategoryStatsUseCase()
    private val getCategoriesWithAmount = GetCategoriesWithAmountUseCase()
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
    private val filterTransactions = FilterTransactionsUseCase()
    private val groupTransactions = GroupTransactionsUseCase()
    private val validateTransaction = ValidateTransactionUseCase()
    private val exportTransactionsToCSV = ExportTransactionsToCSVUseCase()
    private val getTransactionById = GetTransactionByIdUseCase()
    private val loadTransactions: LoadTransactionsUseCase? = transactionRepository?.let { LoadTransactionsUseCase(it) }
    private val goalProgress = GoalProgressUseCase()
    private val updateWalletBalances: UpdateWalletBalancesUseCase? = walletRepository?.let { UpdateWalletBalancesUseCase(it) }
    private val getSubcategoriesByCategoryId: GetSubcategoriesByCategoryIdUseCase? = subcategoryRepository?.let { GetSubcategoriesByCategoryIdUseCase(it) }
    private val addTransaction: AddTransactionUseCase? = transactionRepository?.let { AddTransactionUseCase(it) }
    private val updateTransaction: UpdateTransactionUseCase? = transactionRepository?.let { UpdateTransactionUseCase(it) }
    private val deleteTransaction: DeleteTransactionUseCase? = transactionRepository?.let { DeleteTransactionUseCase(it) }
    private val achievementEngine: AchievementEngine? = if (achievementsRepository != null && appScope != null) AchievementEngine(achievementsRepository, appScope) else null

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
        calculateCategoryStats(transactions)

    fun getCategoriesWithAmount(transactions: List<Transaction>, isExpense: Boolean): List<com.davidbugayov.financeanalyzer.shared.model.CategoryWithAmount> =
        getCategoriesWithAmount(transactions, isExpense)

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
        now: kotlinx.datetime.LocalDate,
        customStart: kotlinx.datetime.LocalDate? = null,
        customEnd: kotlinx.datetime.LocalDate? = null,
        isExpense: Boolean? = null,
    ): List<Transaction> = filterTransactions(transactions, periodType, now, customStart, customEnd, isExpense)

    fun groupTransactions(
        transactions: List<Transaction>,
        keyType: GroupTransactionsUseCase.KeyType,
    ): Map<String, List<Transaction>> = groupTransactions(transactions, keyType)

    /**
     * Валидация транзакции (коды ошибок возвращаются для UI-локализации).
     */
    fun validateTransaction(amount: String, category: String, source: String): ValidateTransactionUseCase.Result =
        validateTransaction.invoke(amount, category, source)

    /**
     * Экспорт транзакций в CSV (строка).
     */
    fun exportTransactionsCsv(
        transactions: List<Transaction>,
        includeHeader: Boolean = true,
        separator: Char = ',',
    ): String = exportTransactionsToCSV(transactions, includeHeader, separator)

    /**
     * Поиск транзакции по id в списке.
     */
    fun getTransactionById(transactions: List<Transaction>, id: String): Transaction? =
        getTransactionById(transactions, id)

    /**
     * Загрузка транзакций (если передан репозиторий в конструктор фасада).
     */
    suspend fun loadTransactions(): List<Transaction> =
        loadTransactions?.invoke() ?: emptyList()

    fun goalProgress(current: com.davidbugayov.financeanalyzer.shared.model.Money, target: com.davidbugayov.financeanalyzer.shared.model.Money): Double =
        goalProgress(current, target)

    suspend fun recomputeWalletBalances(transactions: List<Transaction>) {
        updateWalletBalances?.invoke(transactions)
    }

    fun observeSubcategories(categoryId: Long): kotlinx.coroutines.flow.Flow<List<com.davidbugayov.financeanalyzer.shared.model.Subcategory>>? =
        getSubcategoriesByCategoryId?.invoke(categoryId)

    suspend fun addTransaction(transaction: Transaction) {
        addTransaction?.invoke(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        updateTransaction?.invoke(transaction)
    }

    suspend fun deleteTransaction(id: String) {
        deleteTransaction?.invoke(id)
    }

    fun achievementsFlow(): kotlinx.coroutines.flow.SharedFlow<com.davidbugayov.financeanalyzer.shared.model.Achievement>? =
        achievementEngine?.newAchievements

    fun notifyTransactionAddedForAchievements() {
        achievementEngine?.onTransactionAdded()
    }

    /**
     * Утилита создания суммы из double (для удобства Swift-клиента).
     */
    fun moneyFromDouble(value: Double, currencyCode: String): Money {
        val currency = Currency.fromCode(currencyCode)
        return Money.fromMajor(value, currency)
    }
}


