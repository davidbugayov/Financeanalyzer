package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.shared.model.Money
import java.math.BigDecimal

/**
 * Продвинутые метрики финансового здоровья пользователя.
 * Включает в себя комплексные показатели для оценки финансового состояния.
 */
data class FinancialHealthMetrics(
    /**
     * Коэффициент финансового здоровья (0-100)
     * Комплексный показатель, учитывающий норму сбережений, стабильность доходов,
     * разнообразие источников дохода и контроль расходов
     */
    val financialHealthScore: Double = 0.0,
    
    /**
     * Индекс расходной дисциплины (0-100)
     * Показывает, насколько последовательно пользователь контролирует свои расходы
     */
    val expenseDisciplineIndex: Double = 0.0,
    
    /**
     * Прогноз пенсионных накоплений
     */
    val retirementForecast: RetirementForecast,
    
    /**
     * Сравнение с пользователями схожего дохода
     */
    val peerComparison: PeerComparison,
    
    /**
     * Детализация компонентов коэффициента финансового здоровья
     */
    val healthScoreBreakdown: HealthScoreBreakdown,
    
    /**
     * Рекомендации по улучшению финансового здоровья
     */
    val recommendations: List<FinancialRecommendation> = emptyList(),
    
    /**
     * Дата последнего расчета метрик
     */
    val calculatedAt: Long = System.currentTimeMillis()
)

/**
 * Прогноз достижения пенсионных целей
 */
data class RetirementForecast(
    /**
     * Предполагаемый возраст выхода на пенсию
     */
    val retirementAge: Int = 65,
    
    /**
     * Текущая сумма накоплений
     */
    val currentSavings: Money = Money.zero(),
    
    /**
     * Рекомендуемая сумма для комфортной пенсии
     */
    val recommendedRetirementAmount: Money = Money.zero(),
    
    /**
     * Прогнозируемая сумма к моменту выхода на пенсию
     */
    val projectedRetirementAmount: Money = Money.zero(),
    
    /**
     * Процент достижения пенсионной цели (0-100+)
     */
    val retirementGoalProgress: Double = 0.0,
    
    /**
     * Необходимая ежемесячная сумма для достижения цели
     */
    val requiredMonthlySavings: Money = Money.zero(),
    
    /**
     * Количество лет до достижения цели при текущем темпе накоплений
     */
    val yearsToGoal: Double = Double.POSITIVE_INFINITY
)

/**
 * Сравнение с пользователями схожего дохода
 */
data class PeerComparison(
    /**
     * Диапазон дохода для сравнения (например, "50-75k")
     */
    val incomeRange: String = "",
    
    /**
     * Норма сбережений пользователя vs средняя по группе
     */
    val savingsRateVsPeers: Double = 0.0,
    
    /**
     * Основные категории расходов vs средние по группе
     */
    val expenseCategoriesVsPeers: Map<String, Double> = emptyMap(),
    
    /**
     * Позиция пользователя в группе по финансовому здоровью (percentile 0-100)
     */
    val healthScorePercentile: Double = 50.0,
    
    /**
     * Количество пользователей в группе для статистической значимости
     */
    val peerGroupSize: Int = 0
)

/**
 * Детализация компонентов коэффициента финансового здоровья
 */
data class HealthScoreBreakdown(
    /**
     * Баллы за норму сбережений (0-25)
     */
    val savingsRateScore: Double = 0.0,
    
    /**
     * Баллы за стабильность доходов (0-25)
     */
    val incomeStabilityScore: Double = 0.0,
    
    /**
     * Баллы за контроль расходов (0-25)
     */
    val expenseControlScore: Double = 0.0,
    
    /**
     * Баллы за разнообразие финансовых инструментов (0-25)
     */
    val diversificationScore: Double = 0.0
)

/**
 * Рекомендация по улучшению финансового здоровья
 */
data class FinancialRecommendation(
    /**
     * Заголовок рекомендации
     */
    val title: String,
    
    /**
     * Описание рекомендации
     */
    val description: String,
    
    /**
     * Приоритет рекомендации (HIGH, MEDIUM, LOW)
     */
    val priority: RecommendationPriority,
    
    /**
     * Категория рекомендации (SAVINGS, EXPENSES, INCOME, etc.)
     */
    val category: RecommendationCategory,
    
    /**
     * Потенциальное улучшение коэффициента здоровья при выполнении
     */
    val potentialImpact: Double
)

/**
 * Приоритет рекомендации
 */
enum class RecommendationPriority {
    HIGH, MEDIUM, LOW
}

/**
 * Категория рекомендации
 */
enum class RecommendationCategory {
    SAVINGS,        // Сбережения
    EXPENSES,       // Расходы
    INCOME,         // Доходы
    INVESTMENT,     // Инвестиции
    DEBT,          // Долги
    EMERGENCY_FUND, // Резервный фонд
    RETIREMENT     // Пенсионные накопления
} 