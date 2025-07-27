package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.FinancialHealthMetrics
import com.davidbugayov.financeanalyzer.domain.model.FinancialRecommendation
import com.davidbugayov.financeanalyzer.domain.model.RecommendationCategory
import com.davidbugayov.financeanalyzer.domain.model.RecommendationPriority
import com.davidbugayov.financeanalyzer.domain.model.Transaction
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
            recommendations.add(
                FinancialRecommendation(
                            title = "Улучшите общее финансовое здоровье",
        description = "Ваш коэффициент финансового здоровья (${healthScore.toInt()}/100) ниже среднего. Сосредоточьтесь на увеличении нормы сбережений и стабилизации доходов.",
                    priority = RecommendationPriority.HIGH,
                    category = RecommendationCategory.SAVINGS,
                    potentialImpact = 15.0
                )
            )
        }
        
        // Рекомендации по дисциплине расходов
        if (expenseDisciplineIndex < 60) {
            recommendations.add(
                FinancialRecommendation(
                            title = "Улучшите контроль расходов",
        description = "Ваш индекс расходной дисциплины (${expenseDisciplineIndex.toInt()}/100) указывает на необходимость лучшего планирования трат. Попробуйте вести бюджет и избегать импульсивных покупок.",
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
                recommendations.add(
                    FinancialRecommendation(
                                title = "Увеличьте пенсионные накопления",
        description = "Для достижения пенсионной цели увеличьте ежемесячные накопления на ${Money(monthlyDeficit).formatted}. Текущий прогресс: ${retirementForecast.retirementGoalProgress.toInt()}%",
                        priority = RecommendationPriority.MEDIUM,
                        category = RecommendationCategory.RETIREMENT,
                        potentialImpact = 8.0
                    )
                )
            }
        }
        
        // Рекомендации на основе сравнения с пирами
        if (peerComparison.savingsRateVsPeers < -5) { // На 5% меньше среднего
            recommendations.add(
                FinancialRecommendation(
                            title = "Увеличьте норму сбережений",
        description = "Ваша норма сбережений на ${(-peerComparison.savingsRateVsPeers).toInt()}% ниже среднего в вашей группе дохода. Попробуйте автоматически откладывать 10% от дохода.",
                    priority = RecommendationPriority.MEDIUM,
                    category = RecommendationCategory.SAVINGS,
                    potentialImpact = 10.0
                )
            )
        }
        
        // Рекомендации по разнообразию источников дохода
        val incomeSources = transactions.filter { !it.isExpense }.map { it.source }.distinct().size
        if (incomeSources < 2) {
            recommendations.add(
                FinancialRecommendation(
                            title = "Диверсифицируйте источники дохода",
        description = "У вас только $incomeSources источник дохода. Рассмотрите возможность дополнительного заработка или пассивного дохода для повышения финансовой стабильности.",
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
            recommendations.add(
                FinancialRecommendation(
                            title = "Создайте резервный фонд",
        description = "Рекомендуется иметь резерв на 3-6 месяцев расходов. Сейчас у вас есть средства примерно на ${max(0.0, emergencyFundMonths).toInt()} месяца.",
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
                recommendations.add(
                    FinancialRecommendation(
                                title = "Превышен бюджет по категории ${wallet.name}",
        description = "Вы превысили бюджет по категории ${wallet.name}. Пересмотрите траты или увеличьте лимит.",
                        priority = RecommendationPriority.HIGH,
                        category = RecommendationCategory.EXPENSES,
                        potentialImpact = 15.0
                    )
                )
            } else if (wallet.limit.amount > java.math.BigDecimal.ZERO && wallet.spent.amount > wallet.limit.amount.multiply(java.math.BigDecimal("0.8"))) {
                recommendations.add(
                    FinancialRecommendation(
                                title = "Близко к лимиту по категории ${wallet.name}",
        description = "Вы близки к превышению лимита по категории ${wallet.name}. Контролируйте расходы!",
                        priority = RecommendationPriority.MEDIUM,
                        category = RecommendationCategory.EXPENSES,
                        potentialImpact = 8.0
                    )
                )
            }
        }
        // Анализ повторяющихся подписок и крупных трат (пример)
        val subscriptionCategories = listOf("Подписка", "Subscription", "Сервисы")
        val unusedSubscriptions = transactions.filter { tx ->
            subscriptionCategories.any { cat -> tx.category.contains(cat, ignoreCase = true) }
            // Здесь можно добавить анализ неиспользуемых подписок
        }
        if (unusedSubscriptions.isNotEmpty()) {
            recommendations.add(
                FinancialRecommendation(
                            title = "Проверьте подписки",
        description = "У вас есть регулярные подписки. Проверьте, все ли они актуальны и нужны ли вам.",
                    priority = RecommendationPriority.MEDIUM,
                    category = RecommendationCategory.EXPENSES,
                    potentialImpact = 6.0
                )
            )
        }
        // Совет по экономии на кафе
        val cafeCategories = listOf("Кафе", "Ресторан", "Coffee", "Restaurant")
        val cafeExpenses = transactions.filter { tx ->
            tx.isExpense && cafeCategories.any { cat -> tx.category.contains(cat, ignoreCase = true) }
        }
        if (cafeExpenses.size > 5) { // Порог можно скорректировать
            recommendations.add(
                FinancialRecommendation(
                            title = "Экономьте на кафе и ресторанах",
        description = "Вы часто тратите на кафе и рестораны. Попробуйте готовить дома, чтобы сэкономить.",
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