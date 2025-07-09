package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.RetirementForecast
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * UseCase для расчета прогноза достижения пенсионных целей.
 * 
 * Рассчитывает:
 * - Прогнозируемые накопления к пенсии
 * - Необходимые ежемесячные взносы
 * - Время достижения целевой суммы
 * - Рекомендации по пенсионному планированию
 */
class CalculateRetirementForecastUseCase {

    operator fun invoke(
        transactions: List<Transaction>,
        currentAge: Int = 30,
        retirementAge: Int = 65,
        currentSavings: Money = Money.zero(),
        desiredMonthlyPension: Money? = null // Желаемая месячная пенсия
    ): RetirementForecast {
        
        val yearsToRetirement = max(0, retirementAge - currentAge)
        if (yearsToRetirement == 0) {
            return createImmediateRetirementForecast(currentSavings)
        }
        
        // Анализируем историю накоплений
        val monthlySavingsRate = calculateMonthlySavingsRate(transactions)
        val averageMonthlySavings = calculateAverageMonthlySavings(transactions)
        
        // Рассчитываем целевую сумму для пенсии
        val recommendedRetirementAmount = calculateRecommendedRetirementAmount(
            desiredMonthlyPension, 
            transactions
        )
        
        // Прогнозируем накопления с учетом инфляции и доходности
        val projectedAmount = calculateProjectedSavings(
            currentSavings = currentSavings,
            monthlySavings = averageMonthlySavings,
            years = yearsToRetirement,
            annualReturn = 0.07, // 7% годовых (консервативная оценка)
            inflationRate = 0.03 // 3% инфляции
        )
        
        // Рассчитываем прогресс к цели
        val goalProgress = if (recommendedRetirementAmount.amount > BigDecimal.ZERO) {
            (projectedAmount.amount.divide(recommendedRetirementAmount.amount, 4, RoundingMode.HALF_UP).toDouble() * 100)
        } else {
            100.0
        }
        
        // Рассчитываем необходимые ежемесячные взносы
        val requiredMonthlySavings = calculateRequiredMonthlySavings(
            currentSavings = currentSavings,
            targetAmount = recommendedRetirementAmount,
            years = yearsToRetirement,
            annualReturn = 0.07
        )
        
        // Рассчитываем время достижения цели при текущем темпе
        val yearsToGoal = calculateYearsToGoal(
            currentSavings = currentSavings,
            monthlySavings = averageMonthlySavings,
            targetAmount = recommendedRetirementAmount,
            annualReturn = 0.07
        )
        
        return RetirementForecast(
            retirementAge = retirementAge,
            currentSavings = currentSavings,
            recommendedRetirementAmount = recommendedRetirementAmount,
            projectedRetirementAmount = projectedAmount,
            retirementGoalProgress = max(0.0, min(200.0, goalProgress)), // Ограничиваем 200%
            requiredMonthlySavings = requiredMonthlySavings,
            yearsToGoal = yearsToGoal
        )
    }

    /**
     * Создает прогноз для тех, кто уже на пенсии
     */
    private fun createImmediateRetirementForecast(currentSavings: Money): RetirementForecast {
        return RetirementForecast(
            retirementAge = 65,
            currentSavings = currentSavings,
            recommendedRetirementAmount = currentSavings,
            projectedRetirementAmount = currentSavings,
            retirementGoalProgress = 100.0,
            requiredMonthlySavings = Money.zero(),
            yearsToGoal = 0.0
        )
    }

    /**
     * Рассчитывает среднемесячную норму сбережений на основе истории транзакций
     */
    private fun calculateMonthlySavingsRate(transactions: List<Transaction>): Double {
        if (transactions.isEmpty()) return 0.0
        
        // Группируем по месяцам
        val monthlyData = transactions.groupBy { transaction ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = transaction.date
            "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)}"
        }
        
        val monthlySavingsRates = monthlyData.map { (_, monthTransactions) ->
            val income = monthTransactions.filter { !it.isExpense }.sumOf { it.amount.amount }
            val expense = monthTransactions.filter { it.isExpense }.sumOf { it.amount.amount }
            
            if (income > BigDecimal.ZERO) {
                (income - expense).divide(income, 4, RoundingMode.HALF_UP).toDouble()
            } else {
                0.0
            }
        }
        
        return if (monthlySavingsRates.isNotEmpty()) {
            max(0.0, monthlySavingsRates.average())
        } else {
            0.0
        }
    }

    /**
     * Рассчитывает среднемесячную сумму сбережений
     */
    private fun calculateAverageMonthlySavings(transactions: List<Transaction>): Money {
        if (transactions.isEmpty()) return Money.zero()
        
        val monthlyData = transactions.groupBy { transaction ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = transaction.date
            "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)}"
        }
        
        val monthlySavings = monthlyData.map { (_, monthTransactions) ->
            val income = monthTransactions.filter { !it.isExpense }.sumOf { it.amount.amount }
            val expense = monthTransactions.filter { it.isExpense }.sumOf { it.amount.amount }
            if (income >= expense) income - expense else BigDecimal.ZERO
        }
        
        return if (monthlySavings.isNotEmpty()) {
            val averageSavings = monthlySavings.fold(BigDecimal.ZERO) { acc, amount -> acc + amount }
                .divide(BigDecimal(monthlySavings.size), 2, RoundingMode.HALF_UP)
            Money(averageSavings)
        } else {
            Money.zero()
        }
    }

    /**
     * Рассчитывает рекомендуемую сумму для комфортной пенсии
     */
    private fun calculateRecommendedRetirementAmount(
        desiredMonthlyPension: Money?,
        transactions: List<Transaction>
    ): Money {
        
        // Если указана желаемая месячная пенсия, используем её
        desiredMonthlyPension?.let { monthlyPension ->
            // 25x правило: умножаем годовые расходы на 25
            val annualPension = monthlyPension.amount.multiply(BigDecimal(12))
            return Money(annualPension.multiply(BigDecimal(25)))
        }
        
        // Иначе рассчитываем на основе текущих расходов
        val averageMonthlyExpense = calculateAverageMonthlyExpense(transactions)
        
        // Рекомендуем 80% от текущих расходов для комфортной пенсии
        val recommendedMonthlyPension = averageMonthlyExpense.amount.multiply(BigDecimal("0.8"))
        val annualPension = recommendedMonthlyPension.multiply(BigDecimal(12))
        
        // Применяем 25x правило
        return Money(annualPension.multiply(BigDecimal(25)))
    }

    /**
     * Рассчитывает средние месячные расходы
     */
    private fun calculateAverageMonthlyExpense(transactions: List<Transaction>): Money {
        if (transactions.isEmpty()) return Money(BigDecimal(50000)) // Дефолт 50k рублей
        
        val expenseTransactions = transactions.filter { it.isExpense }
        if (expenseTransactions.isEmpty()) return Money(BigDecimal(50000))
        
        val monthlyData = expenseTransactions.groupBy { transaction ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = transaction.date
            "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)}"
        }
        
        val monthlyExpenses = monthlyData.map { (_, monthTransactions) ->
            monthTransactions.sumOf { it.amount.amount }
        }
        
        return if (monthlyExpenses.isNotEmpty()) {
            val averageExpense = monthlyExpenses.fold(BigDecimal.ZERO) { acc, amount -> acc + amount }
                .divide(BigDecimal(monthlyExpenses.size), 2, RoundingMode.HALF_UP)
            Money(averageExpense)
        } else {
            Money(BigDecimal(50000))
        }
    }

    /**
     * Прогнозирует накопления с учетом сложного процента
     */
    private fun calculateProjectedSavings(
        currentSavings: Money,
        monthlySavings: Money,
        years: Int,
        annualReturn: Double,
        inflationRate: Double
    ): Money {
        val realReturn = (1 + annualReturn) / (1 + inflationRate) - 1 // Реальная доходность
        val monthlyReturn = realReturn / 12
        val totalMonths = years * 12
        
        // Будущая стоимость текущих накоплений
        val futureCurrentSavings = currentSavings.amount.toDouble() * (1 + realReturn).pow(years)
        
        // Будущая стоимость регулярных взносов (аннуитет)
        val futureMonthlySavings = if (monthlyReturn > 0) {
            monthlySavings.amount.toDouble() * 
            (((1 + monthlyReturn).pow(totalMonths) - 1) / monthlyReturn)
        } else {
            monthlySavings.amount.toDouble() * totalMonths
        }
        
        val totalFutureSavings = futureCurrentSavings + futureMonthlySavings
        
        return Money(BigDecimal(totalFutureSavings).setScale(2, RoundingMode.HALF_UP))
    }

    /**
     * Рассчитывает необходимые ежемесячные взносы для достижения цели
     */
    private fun calculateRequiredMonthlySavings(
        currentSavings: Money,
        targetAmount: Money,
        years: Int,
        annualReturn: Double
    ): Money {
        if (years <= 0) return Money.zero()
        
        val monthlyReturn = annualReturn / 12
        val totalMonths = years * 12
        
        // Будущая стоимость текущих накоплений
        val futureCurrentSavings = currentSavings.amount.toDouble() * (1 + annualReturn).pow(years)
        
        // Сколько нужно накопить дополнительно
        val additionalNeeded = targetAmount.amount.toDouble() - futureCurrentSavings
        
        if (additionalNeeded <= 0) return Money.zero()
        
        // Рассчитываем ежемесячный взнос для аннуитета
        val requiredMonthlyPayment = if (monthlyReturn > 0) {
            additionalNeeded * monthlyReturn / ((1 + monthlyReturn).pow(totalMonths) - 1)
        } else {
            additionalNeeded / totalMonths
        }
        
        return Money(BigDecimal(max(0.0, requiredMonthlyPayment)).setScale(2, RoundingMode.HALF_UP))
    }

    /**
     * Рассчитывает время достижения цели при текущем темпе накоплений
     */
    private fun calculateYearsToGoal(
        currentSavings: Money,
        monthlySavings: Money,
        targetAmount: Money,
        annualReturn: Double
    ): Double {
        if (targetAmount.amount <= currentSavings.amount) return 0.0
        if (monthlySavings.amount <= BigDecimal.ZERO) return Double.POSITIVE_INFINITY
        
        val monthlyReturn = annualReturn / 12
        val targetDouble = targetAmount.amount.toDouble()
        val currentDouble = currentSavings.amount.toDouble()
        val monthlyDouble = monthlySavings.amount.toDouble()
        
        if (monthlyReturn > 0) {
            // Решаем уравнение: FV = PV * (1+r)^n + PMT * [((1+r)^n - 1) / r]
            // Используем итерационный метод для поиска n
            var months = 1.0
            var currentValue = currentDouble
            
            while (currentValue < targetDouble && months < 12 * 100) { // Максимум 100 лет
                currentValue = currentDouble * (1 + monthlyReturn).pow(months) +
                    monthlyDouble * (((1 + monthlyReturn).pow(months) - 1) / monthlyReturn)
                if (currentValue >= targetDouble) break
                months += 1
            }
            
            return if (months >= 12 * 100) Double.POSITIVE_INFINITY else months / 12
        } else {
            // Без доходности - простое деление
            val monthsNeeded = (targetDouble - currentDouble) / monthlyDouble
            return if (monthsNeeded > 0) monthsNeeded / 12 else Double.POSITIVE_INFINITY
        }
    }
} 