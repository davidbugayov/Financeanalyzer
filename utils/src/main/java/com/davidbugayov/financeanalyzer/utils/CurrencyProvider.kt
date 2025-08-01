package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.core.model.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Синглтон для предоставления глобального доступа к текущей валюте
 */
object CurrencyProvider {

    private var _currentCurrency: Currency = Currency.RUB
    private val _currencyFlow = MutableStateFlow(_currentCurrency)

    /**
     * Инициализация с валютой из PreferencesManager
     */
    fun init(preferencesManager: PreferencesManager) {
        _currentCurrency = preferencesManager.getCurrency()
        _currencyFlow.value = _currentCurrency
    }

    /**
     * Получить текущую валюту
     */
    fun getCurrency(): Currency = _currentCurrency

    /**
     * Установить новую валюту
     */
    fun setCurrency(currency: Currency) {
        _currentCurrency = currency
        _currencyFlow.value = currency
    }

    /**
     * Получить Flow для отслеживания изменений валюты
     */
    fun getCurrencyFlow(): StateFlow<Currency>? = _currencyFlow.asStateFlow()
}
