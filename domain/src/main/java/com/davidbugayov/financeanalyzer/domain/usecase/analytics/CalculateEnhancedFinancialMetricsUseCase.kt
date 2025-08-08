package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.FinancialHealthMetrics
import com.davidbugayov.financeanalyzer.domain.model.FinancialRecommendation
import com.davidbugayov.financeanalyzer.domain.model.RecommendationCategory
import com.davidbugayov.financeanalyzer.domain.model.RecommendationPriority
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import org.koin.core.context.GlobalContext
import com.davidbugayov.financeanalyzer.domain.R
import kotlin.math.max
import java.math.BigDecimal

/**
 * Главный UseCase для расчета продвинутых метрик финансового здоровья.
 * Объединяет все компоненты: коэффициент здоровья, индекс дисциплины, 
 * прогноз пенсии и сравнение с пирами.
 */
class CalculateEnhancedFinancialMetricsUseCase(
    private val calculateFinancialHealthScoreUseCase: CalculateFinancialHealthScoreUseCase,
    private val calculateExpenseDisciplineIndexUseCase: CalculateExpenseDisciplineIndexUseCase,
    private val calculateRetirementForecastUseCase: CalculateRetirementForecastUseCase,
    private val calculatePeerComparisonUseCase: CalculatePeerComparisonUseCase,
    private val walletRepository: com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
) {

    suspend operator fun invoke(
        transactions: List<Transaction>,
        currentAge: Int = 30,
        retirementAge: Int = 65,
        currentSavings: Money = Money.zero(),
        desiredMonthlyPension: Money? = null
    ): FinancialHealthMetrics {
        
        // Рассчитываем коэффициент финансового здоровья
        val (healthScore, healthBreakdown) = calculateFinancialHealthScoreUseCase(transactions)
        
        // Рассчитываем индекс расходной дисциплины
        val expenseDisciplineIndex = calculateExpenseDisciplineIndexUseCase(transactions)
        
        // Рассчитываем прогноз пенсионных накоплений
        val retirementForecast = calculateRetirementForecastUseCase(
            transactions = transactions,
            currentAge = currentAge,
            retirementAge = retirementAge,
            currentSavings = currentSavings,
            desiredMonthlyPension = desiredMonthlyPension
        )
        
        // Рассчитываем сравнение с пирами
        val peerComparison = calculatePeerComparisonUseCase(transactions, healthScore)
        
        // Генерируем рекомендации
        val recommendations = generateRecommendations(
            healthScore = healthScore,
            expenseDisciplineIndex = expenseDisciplineIndex,
            retirementForecast = retirementForecast,
            peerComparison = peerComparison,
            transactions = transactions
        )
        
        return FinancialHealthMetrics(
            financialHealthScore = healthScore,
            expenseDisciplineIndex = expenseDisciplineIndex,
            retirementForecast = retirementForecast,
            peerComparison = peerComparison,
            healthScoreBreakdown = healthBreakdown,
            recommendations = recommendations
        )
    }

    /**
     * Генерирует персонализированные рекомендации по улучшению финансового здоровья
     */
    private suspend fun generateRecommendations(
        healthScore: Double,
        expenseDisciplineIndex: Double,
        retirementForecast: com.davidbugayov.financeanalyzer.domain.model.RetirementForecast,
        peerComparison: com.davidbugayov.financeanalyzer.domain.model.PeerComparison,
        transactions: List<Transaction>
    ): List<FinancialRecommendation> {
        
        val recommendations = mutableListOf<FinancialRecommendation>()
        
        // Рекомендации по коэффициенту финансового здоровья
        if (healthScore < 50) {
            val rp: ResourceProvider = GlobalContext.get().get()
            recommendations.add(
                FinancialRecommendation(
                    title = rp.getString(R.string.recommendation_improve_financial_health),
                    description = rp.getString(R.string.recommendation_improve_financial_health_desc, healthScore.toInt()),
                    priority = RecommendationPriority.HIGH,
                    category = RecommendationCategory.SAVINGS,
                    potentialImpact = 15.0
                )
            )
        }
        
        // Рекомендации по дисциплине расходов
        if (expenseDisciplineIndex < 60) {
            val rp: ResourceProvider = GlobalContext.get().get()
            recommendations.add(
                FinancialRecommendation(
                    title = rp.getString(R.string.recommendation_improve_expense_control),
                    description = rp.getString(R.string.recommendation_improve_expense_control_desc, expenseDisciplineIndex.toInt()),
                    priority = RecommendationPriority.HIGH,
                    category = RecommendationCategory.EXPENSES,
                    potentialImpact = 12.0
                )
            )
        }
        
        // Рекомендации по пенсионным накоплениям
        if (retirementForecast.retirementGoalProgress < 50) {
            val monthlyDeficit = retirementForecast.requiredMonthlySavings.amount.subtract(
                calculateCurrentMonthlySavings(transactions).amount
            )
            
            if (monthlyDeficit.toDouble() > 0) {
                val rp: ResourceProvider = GlobalContext.get().get()
                recommendations.add(
                    FinancialRecommendation(
                        title = rp.getString(R.string.recommendation_increase_retirement_savings),
                        description = rp.getString(R.string.recommendation_increase_retirement_savings_desc, Money(monthlyDeficit).formatted, retirementForecast.retirementGoalProgress.toInt()),
                        priority = RecommendationPriority.MEDIUM,
                        category = RecommendationCategory.RETIREMENT,
                        potentialImpact = 8.0
                    )
                )
            }
        }
        
        // Рекомендации на основе сравнения с пирами
        if (peerComparison.savingsRateVsPeers < -5) { // На 5% меньше среднего
            val rp: ResourceProvider = GlobalContext.get().get()
            recommendations.add(
                FinancialRecommendation(
                    title = rp.getString(R.string.recommendation_increase_savings_rate),
                    description = rp.getString(R.string.recommendation_increase_savings_rate_desc, (-peerComparison.savingsRateVsPeers).toInt()),
                    priority = RecommendationPriority.MEDIUM,
                    category = RecommendationCategory.SAVINGS,
                    potentialImpact = 10.0
                )
            )
        }
        
        // Рекомендации по разнообразию источников дохода
        val incomeSources = transactions.filter { !it.isExpense }.map { it.source }.distinct().size
        if (incomeSources < 2) {
            val rp: ResourceProvider = GlobalContext.get().get()
            recommendations.add(
                FinancialRecommendation(
                    title = rp.getString(R.string.recommendation_diversify_income),
                    description = rp.getString(R.string.recommendation_diversify_income_desc, incomeSources),
                    priority = RecommendationPriority.LOW,
                    category = RecommendationCategory.INCOME,
                    potentialImpact = 5.0
                )
            )
        }
        
        // Рекомендации по экстренному фонду
        val monthlySavings = calculateCurrentMonthlySavings(transactions)
        val monthlyExpenses = calculateAverageMonthlyExpenses(transactions)
        val emergencyFundMonths = if (monthlyExpenses.amount.toDouble() > 0) {
            monthlySavings.amount.toDouble() / monthlyExpenses.amount.toDouble()
        } else {
            0.0
        }
        
        if (emergencyFundMonths < 3) {
            val rp: ResourceProvider = GlobalContext.get().get()
            recommendations.add(
                FinancialRecommendation(
                    title = rp.getString(R.string.recommendation_create_emergency_fund),
                    description = rp.getString(R.string.recommendation_create_emergency_fund_desc, max(0.0, emergencyFundMonths).toInt()),
                    priority = RecommendationPriority.HIGH,
                    category = RecommendationCategory.EMERGENCY_FUND,
                    potentialImpact = 12.0
                )
            )
        }
        
        // --- Новая логика: советы по экономии и предупреждения о бюджете ---
        val wallets = walletRepository.getAllWallets()
        wallets.forEach { wallet ->
            if (wallet.limit.amount > java.math.BigDecimal.ZERO && wallet.spent.amount > wallet.limit.amount) {
                val rp: ResourceProvider = GlobalContext.get().get()
                recommendations.add(
                    FinancialRecommendation(
                        title = rp.getString(R.string.recommendation_budget_exceeded, wallet.name),
                        description = rp.getString(R.string.recommendation_budget_exceeded_desc, wallet.name),
                        priority = RecommendationPriority.HIGH,
                        category = RecommendationCategory.EXPENSES,
                        potentialImpact = 15.0
                    )
                )
            } else if (wallet.limit.amount > java.math.BigDecimal.ZERO && wallet.spent.amount > wallet.limit.amount.multiply(java.math.BigDecimal("0.8"))) {
                val rp: ResourceProvider = GlobalContext.get().get()
                recommendations.add(
                    FinancialRecommendation(
                        title = rp.getString(R.string.recommendation_budget_close_to_limit, wallet.name),
                        description = rp.getString(R.string.recommendation_budget_close_to_limit_desc, wallet.name),
                        priority = RecommendationPriority.MEDIUM,
                        category = RecommendationCategory.EXPENSES,
                        potentialImpact = 8.0
                    )
                )
            }
        }
        // Анализ повторяющихся подписок и крупных трат (пример)
        val rp: ResourceProvider = GlobalContext.get().get()
        val subscriptionCategories = listOf(rp.getString(R.string.category_subscription), "Subscription", rp.getString(R.string.category_services))
        val unusedSubscriptions = transactions.filter { tx ->
            subscriptionCategories.any { cat -> tx.category.contains(cat, ignoreCase = true) }
            // Здесь можно добавить анализ неиспользуемых подписок
        }
        if (unusedSubscriptions.isNotEmpty()) {
                        recommendations.add(
                FinancialRecommendation(
                    title = rp.getString(R.string.recommendation_check_subscriptions_title),
                    description = rp.getString(R.string.recommendation_check_subscriptions_desc),
                    priority = RecommendationPriority.MEDIUM,
                    category = RecommendationCategory.EXPENSES,
                    potentialImpact = 6.0
                )
            )
        }
        // Совет по экономии на кафе
        val cafeCategories = listOf(rp.getString(R.string.category_cafe), rp.getString(R.string.category_restaurant), "Coffee", "Restaurant")
        val cafeExpenses = transactions.filter { tx ->
            tx.isExpense && cafeCategories.any { cat -> tx.category.contains(cat, ignoreCase = true) }
        }
        if (cafeExpenses.size > 5) { // Порог можно скорректировать
                        recommendations.add(
                FinancialRecommendation(
                    title = rp.getString(R.string.recommendation_save_on_cafe),
                    description = rp.getString(R.string.recommendation_save_on_cafe_desc),
                    priority = RecommendationPriority.LOW,
                    category = RecommendationCategory.EXPENSES,
                    potentialImpact = 4.0
                )
            )
        }
        // Сортируем рекомендации по приоритету и потенциальному влиянию
        return recommendations.sortedWith(
            compareByDescending<FinancialRecommendation> { 
                when (it.priority) {
                    RecommendationPriority.HIGH -> 3
                    RecommendationPriority.MEDIUM -> 2
                    RecommendationPriority.LOW -> 1
                }
            }.thenByDescending { it.potentialImpact }
        )
    }

    /**
     * Рассчитывает текущие месячные сбережения
     */
    private fun calculateCurrentMonthlySavings(transactions: List<Transaction>): Money {
        val monthlyData = transactions.groupBy { transaction ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = transaction.date
            "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)}"
        }
        
        if (monthlyData.isEmpty()) return Money.zero()
        
        val monthlySavings = monthlyData.map { (_, monthTransactions) ->
            val income = monthTransactions.filter { !it.isExpense }.sumOf { it.amount.amount }
            val expense = monthTransactions.filter { it.isExpense }.sumOf { it.amount.amount }
            if (income >= expense) income - expense else java.math.BigDecimal.ZERO
        }
        
        val averageSavings = monthlySavings.fold(java.math.BigDecimal.ZERO) { acc, amount -> acc + amount }
            .divide(java.math.BigDecimal(monthlySavings.size), 2, java.math.RoundingMode.HALF_UP)
            
        return Money(averageSavings)
    }

    /**
     * Рассчитывает средние месячные расходы
     */
    private fun calculateAverageMonthlyExpenses(transactions: List<Transaction>): Money {
        val expenseTransactions = transactions.filter { it.isExpense }
        if (expenseTransactions.isEmpty()) return Money.zero()
        
        val monthlyData = expenseTransactions.groupBy { transaction ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = transaction.date
            "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)}"
        }
        
        if (monthlyData.isEmpty()) return Money.zero()
        
        val monthlyExpenses = monthlyData.map { (_, monthTransactions) ->
            monthTransactions.sumOf { it.amount.amount }
        }
        
        val averageExpense = monthlyExpenses.fold(java.math.BigDecimal.ZERO) { acc, amount -> acc + amount }
            .divide(java.math.BigDecimal(monthlyExpenses.size), 2, java.math.RoundingMode.HALF_UP)
            
        return Money(averageExpense)
    }
} 