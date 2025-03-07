package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.davidbugayov.financeanalyzer.domain.model.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Класс для работы с настройками валюты.
 * Хранит выбранную пользователем валюту по умолчанию.
 */
class CurrencyPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _defaultCurrency = MutableStateFlow(getDefaultCurrency())

    /**
     * Поток с текущей валютой по умолчанию
     */
    val defaultCurrency: StateFlow<Currency> = _defaultCurrency

    /**
     * Получает валюту по умолчанию из настроек
     * @return Валюта по умолчанию
     */
    fun getDefaultCurrency(): Currency {
        val currencyCode = prefs.getString(KEY_DEFAULT_CURRENCY, Currency.RUB.code) ?: Currency.RUB.code
        return Currency.fromCode(currencyCode)
    }

    /**
     * Устанавливает валюту по умолчанию
     * @param currency Валюта по умолчанию
     */
    fun setDefaultCurrency(currency: Currency) {
        prefs.edit().putString(KEY_DEFAULT_CURRENCY, currency.code).apply()
        _defaultCurrency.value = currency
    }

    companion object {

        private const val PREFS_NAME = "currency_preferences"
        private const val KEY_DEFAULT_CURRENCY = "default_currency"

        @Volatile
        private var INSTANCE: CurrencyPreferences? = null

        /**
         * Получает экземпляр CurrencyPreferences
         * @param context Контекст приложения
         * @return Экземпляр CurrencyPreferences
         */
        fun getInstance(context: Context): CurrencyPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = CurrencyPreferences(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
} 