package com.davidbugayov.financeanalyzer.feature.statistics.contract

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.feature.statistics.model.FinancialMetrics
import java.time.LocalDate

sealed interface FinancialDetailsStatisticsContract {
    data class State(
        val period: LocalDate = LocalDate.now(),
        val metrics: FinancialMetrics = FinancialMetrics(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed interface Intent {
        data class LoadMetrics(val period: LocalDate) : Intent
    }

    sealed interface Effect {
        data class ShowMessage(val message: String) : Effect
    }
} 