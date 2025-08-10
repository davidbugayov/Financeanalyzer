package com.davidbugayov.financeanalyzer.shared

import com.davidbugayov.financeanalyzer.shared.model.BalanceMetrics
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateBalanceMetricsUseCase
import kotlinx.datetime.LocalDate

/**
 * Простой фасад KMP для вызова из iOS/Android.
 */
class SharedFacade {
    private val calculateBalanceMetrics = CalculateBalanceMetricsUseCase()

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
     * Утилита создания суммы из double (для удобства Swift-клиента).
     */
    fun moneyFromDouble(value: Double, currencyCode: String): Money {
        val currency = Currency.fromCode(currencyCode)
        return Money.fromMajor(value, currency)
    }
}


