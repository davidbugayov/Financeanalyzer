package com.davidbugayov.financeanalyzer.presentation.chart.detail.state
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction

object FinancialDetailStatisticsContract {
    // Состояние экрана финансовой статистики
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val transactions: List<Transaction> = emptyList(),
        val income: Money = Money.zero(),
        val expense: Money = Money.zero(),
        val period: String = "",
    )

    // Интенты (события)
    sealed class Intent {
        object LoadData : Intent()
        // Добавь другие интенты по необходимости
    }

    // Эффекты (одноразовые события)
    sealed class Effect {
        data class ShowError(val message: String) : Effect()
        // Добавь другие эффекты по необходимости
    }
}
