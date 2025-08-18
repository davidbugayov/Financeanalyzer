package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.shared.model.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Синглтон для предоставления глобального доступа к текущей валюте
 */
object CurrencyProvider {
    private var currentCurrencyInternal: Currency = Currency.RUB
    private val currencyFlowInternal = MutableStateFlow(currentCurrencyInternal)

    /**
     * Инициализация с валютой из PreferencesManager
     */
    fun init(preferencesManager: PreferencesManager) {
        val currency = preferencesManager.getCurrency()
        currentCurrencyInternal = currency
        currencyFlowInternal.value = currentCurrencyInternal
    }

    /**
     * Получить текущую валюту
     */
    fun getCurrency(): Currency = currentCurrencyInternal

    /**
     * Установить новую валюту
     */
    fun setCurrency(currency: Currency) {
        currentCurrencyInternal = currency
        currencyFlowInternal.value = currency
    }

    /**
     * Получить Flow для отслеживания изменений валюты
     */
    fun getCurrencyFlow(): StateFlow<Currency> = currencyFlowInternal.asStateFlow()
}
