package com.davidbugayov.financeanalyzer.shared.model

data class HealthScoreBreakdown(
    val savingsRateScore: Double = 0.0,
    val incomeStabilityScore: Double = 0.0,
    val expenseControlScore: Double = 0.0,
    val diversificationScore: Double = 0.0,
)

data class PeerComparison(
    val incomeRange: String = "",
    val savingsRateVsPeers: Double = 0.0,
    val expenseCategoriesVsPeers: Map<String, Double> = emptyMap(),
    val healthScorePercentile: Double = 50.0,
    val peerGroupSize: Int = 0,
)

data class RetirementForecast(
    val retirementAge: Int = 65,
    val currentSavings: Money = Money.zero(),
    val recommendedRetirementAmount: Money = Money.zero(),
    val projectedRetirementAmount: Money = Money.zero(),
    val retirementGoalProgress: Double = 0.0,
    val requiredMonthlySavings: Money = Money.zero(),
    val yearsToGoal: Double = Double.POSITIVE_INFINITY,
)


