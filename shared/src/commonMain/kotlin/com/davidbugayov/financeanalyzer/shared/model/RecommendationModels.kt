package com.davidbugayov.financeanalyzer.shared.model

/**
 * Категория рекомендации.
 */
enum class RecommendationCategory { SAVINGS, EXPENSES, INCOME, EMERGENCY_FUND, RETIREMENT }

/**
 * Приоритет рекомендации.
 */
enum class RecommendationPriority { LOW, MEDIUM, HIGH }

/**
 * Рекомендация без UI-строк. UI отвечает за локализацию по коду и параметрам.
 * @param code ключ для маппинга на строковые ресурсы в UI (напр. "recommendation_improve_financial_health").
 * @param priority приоритет отображения.
 * @param category тематическая категория.
 * @param params произвольные параметры для шаблонов строк (значения в текст UI подставляет самостоятельно).
 */
data class FinancialRecommendation(
    val code: String,
    val priority: RecommendationPriority,
    val category: RecommendationCategory,
    val params: Map<String, String> = emptyMap(),
)

/**
 * Сводный результат метрик финансового здоровья.
 */
data class FinancialHealthMetrics(
    val financialHealthScore: Double,
    val expenseDisciplineIndex: Double,
    val retirementForecast: RetirementForecast,
    val peerComparison: PeerComparison,
    val healthScoreBreakdown: HealthScoreBreakdown,
    val recommendations: List<FinancialRecommendation>,
)


