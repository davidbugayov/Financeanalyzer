package com.davidbugayov.financeanalyzer.shared

import com.davidbugayov.financeanalyzer.shared.model.BalanceMetrics
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.repository.AchievementsRepository
import com.davidbugayov.financeanalyzer.shared.repository.SubcategoryRepository
import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.shared.repository.WalletRepository
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
import com.davidbugayov.financeanalyzer.shared.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.UpdateWalletBalancesUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.subcategory.AddSubcategoryUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.subcategory.GetSubcategoriesByCategoryIdUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.subcategory.GetSubcategoryByIdUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.transaction.GetTransactionsForPeriodFlowUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.wallet.AllocateIncomeUseCase
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
        // Инициализация продвинутых метрик
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
        this.loadTransactions = transactionRepository?.let { LoadTransactionsUseCase(it) }
        this.goalProgressUseCase = GoalProgressUseCase()
        this.updateWalletBalancesUseCase = UpdateWalletBalancesUseCase()
        this.getSubcategoriesByCategoryIdUseCase =
            subcategoryRepository?.let { GetSubcategoriesByCategoryIdUseCase(it) }
        this.addSubcategoryUseCase = subcategoryRepository?.let { AddSubcategoryUseCase(it) }
        this.allocateIncomeUseCase = walletRepository?.let { AllocateIncomeUseCase(it) }
        this.getTransactionsForPeriodFlow = transactionRepository?.let { GetTransactionsForPeriodFlowUseCase(it) }
        this.getExpenseOptimizationRecommendations = GetExpenseOptimizationRecommendationsUseCase()
        this.getSubcategoryByIdUseCase = subcategoryRepository?.let { GetSubcategoryByIdUseCase(it) }
    }

    private val calculateBalanceMetrics: CalculateBalanceMetricsUseCase
    private val calculateEnhancedFinancialMetrics: CalculateEnhancedFinancialMetricsUseCase
    private val getSmartExpenseTips: GetSmartExpenseTipsUseCase

    private val filterTransactionsUseCase: FilterTransactionsUseCase
    private val groupTransactionsUseCase: GroupTransactionsUseCase
    private val validateTransactionUseCase: ValidateTransactionUseCase
    private val exportTransactionsToCSVUseCase: ExportTransactionsToCSVUseCase
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase
    private val loadTransactions: LoadTransactionsUseCase?
    private val goalProgressUseCase: GoalProgressUseCase
    private val updateWalletBalancesUseCase: UpdateWalletBalancesUseCase
    private val getSubcategoriesByCategoryIdUseCase: GetSubcategoriesByCategoryIdUseCase?
    private val addSubcategoryUseCase: AddSubcategoryUseCase?
    private val allocateIncomeUseCase: AllocateIncomeUseCase?
    private val getTransactionsForPeriodFlow: GetTransactionsForPeriodFlowUseCase?
    private val getExpenseOptimizationRecommendations: GetExpenseOptimizationRecommendationsUseCase
    private val getSubcategoryByIdUseCase: GetSubcategoryByIdUseCase?

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
     * Загрузка транзакций (если передан репозиторий в конструктор фасада).
     */
    suspend fun loadTransactions(): List<Transaction> =
        loadTransactions?.invoke() ?: emptyList()

    fun goalProgress(current: Money, target: Money): Double =
        goalProgressUseCase(current, target)


    fun transactionsForPeriodFlow(start: LocalDate, end: LocalDate): kotlinx.coroutines.flow.Flow<List<Transaction>>? =
        getTransactionsForPeriodFlow?.invoke(start, end)

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
            getSubcategoriesByCategoryIdUseCase?.invoke(categoryId)?.first() ?: emptyList()
        } catch (_: Exception) {
            emptyList()
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
}


