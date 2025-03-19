package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * Класс для работы с настройками валюты.
 * Хранит выбранную пользователем валюту по умолчанию.
 */
class CurrencyPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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