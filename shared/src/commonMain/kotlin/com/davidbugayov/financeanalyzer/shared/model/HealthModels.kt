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
    val requiredSavings: Money = Money.zero(),
    val projectedSavings: Money = Money.zero(),
    val savingsGap: Money = Money.zero(),
    val monthlySavingsNeeded: Money = Money.zero(),
    val riskLevel: String = "MEDIUM",
    val recommendations: List<String> = emptyList(),
    val yearsToRetirement: Int = 35,
    val retirementYears: Int = 20,
)


