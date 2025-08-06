package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.ui.theme.AppTheme
import com.davidbugayov.financeanalyzer.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Менеджер для работы с SharedPreferences.
 * Отвечает за сохранение и загрузку пользовательских настроек и данных.
 */
class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE,
        )

    // Flow для темы
    private val _themeModeFlow =
        MutableStateFlow(
            getThemeModeInternal(),
        ) // Используем внутренний метод для инициализации
    val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

    // Flow для валюты
    private val _currencyFlow = MutableStateFlow(getCurrencyInternal())
    val currencyFlow: StateFlow<Currency> = _currencyFlow.asStateFlow()

    /**
     * Сохраняет тему приложения в SharedPreferences и обновляет Flow
     * @param themeMode Режим темы для сохранения
     */
    fun saveThemeMode(themeMode: ThemeMode) {
        // Проверяем, изменилась ли тема
        val currentTheme = _themeModeFlow.value
        if (currentTheme != themeMode) {
            sharedPreferences.edit { putString(KEY_THEME_MODE, themeMode.name) }
            _themeModeFlow.value = themeMode // Обновляем Flow

            // Обновляем глобальную тему приложения
            AppTheme.setTheme(themeMode)
        }
    }

    /**
     * Загружает тему приложения из SharedPreferences
     * @return Режим темы или SYSTEM, если ничего не сохранено
     */
    fun getThemeMode(): ThemeMode {
        return _themeModeFlow.value // Возвращаем текущее значение из Flow для консистентности
    }

    /**
     * Внутренний метод для первоначальной загрузки темы, чтобы избежать рекурсии при инициализации _themeModeFlow.
     */
    private fun getThemeModeInternal(): ThemeMode {
        val themeName = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(themeName ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "finance_analyzer_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_TRANSACTION_REMINDER_ENABLED = "transaction_reminder_enabled"
        private const val KEY_IMPORT_REMINDER_SHOWN = "import_reminder_shown"

        // Добавим ключи для времени напоминания, если они еще не в companion object
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"

        // Ключи для настроек безопасности
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_PIN_CODE = "pin_code"

        // Ключ для валюты
        private const val KEY_CURRENCY = "currency"
    }

    /**
     * Возвращает, включены ли уведомления о транзакциях.
     * @return true, если уведомления о транзакциях включены
     */
    fun isTransactionReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_TRANSACTION_REMINDER_ENABLED, true)
    }

    fun setTransactionReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_TRANSACTION_REMINDER_ENABLED, enabled) }
    }

    fun setReminderTime(
        hour: Int,
        minute: Int,
    ) {
        sharedPreferences.edit {
            putInt(KEY_REMINDER_HOUR, hour)
            putInt(KEY_REMINDER_MINUTE, minute)
        }
    }

    fun getReminderTime(): Pair<Int, Int> {
        val hour = sharedPreferences.getInt(KEY_REMINDER_HOUR, 20) // Используем константу ключа
        val minute = sharedPreferences.getInt(KEY_REMINDER_MINUTE, 0) // Используем константу ключа
        return Pair(hour, minute)
    }

    /**
     * Проверяет, было ли показано напоминание об импорте транзакций.
     * @return true, если напоминание уже было показано
     */
    fun isImportReminderShown(): Boolean {
        return sharedPreferences.getBoolean(KEY_IMPORT_REMINDER_SHOWN, false)
    }

    /**
     * Отмечает напоминание об импорте транзакций как показанное.
     */
    fun setImportReminderShown() {
        sharedPreferences.edit { putBoolean(KEY_IMPORT_REMINDER_SHOWN, true) }
    }

    // Методы для настроек безопасности

    /**
     * Проверяет, включена ли блокировка приложения
     * @return true, если блокировка приложения включена
     */
    fun isAppLockEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_APP_LOCK_ENABLED, false)
    }

    /**
     * Устанавливает состояние блокировки приложения
     * @param enabled Включена ли блокировка приложения
     */
    fun setAppLockEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_APP_LOCK_ENABLED, enabled) }
    }

    /**
     * Проверяет, включена ли биометрическая аутентификация
     * @return true, если биометрическая аутентификация включена
     */
    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    /**
     * Устанавливает состояние биометрической аутентификации
     * @param enabled Включена ли биометрическая аутентификация
     */
    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_BIOMETRIC_ENABLED, enabled) }
    }

    /**
     * Сохраняет PIN-код
     * @param pinCode PIN-код для сохранения
     */
    fun setPinCode(pinCode: String) {
        sharedPreferences.edit { putString(KEY_PIN_CODE, pinCode) }
    }

    /**
     * Получает сохраненный PIN-код
     * @return Сохраненный PIN-код или null, если не установлен
     */
    fun getPinCode(): String? {
        return sharedPreferences.getString(KEY_PIN_CODE, null)
    }

    /**
     * Удаляет сохраненный PIN-код
     */
    fun removePinCode() {
        sharedPreferences.edit { remove(KEY_PIN_CODE) }
    }

    // Методы для работы с валютой

    /**
     * Сохраняет выбранную валюту
     */
    fun saveCurrency(currency: Currency) {
        sharedPreferences.edit { putString(KEY_CURRENCY, currency.name) }
        _currencyFlow.value = currency
        // Уведомляем CurrencyProvider об изменении
        CurrencyProvider.setCurrency(currency)
    }

    /**
     * Получает сохраненную валюту
     */
    fun getCurrency(): Currency {
        val currency = _currencyFlow.value
        return currency
    }

    /**
     * Внутренний метод для первоначальной загрузки валюты
     */
    private fun getCurrencyInternal(): Currency {
        val currencyName = sharedPreferences.getString(KEY_CURRENCY, Currency.RUB.name)
        val currency =
            try {
                Currency.valueOf(currencyName ?: Currency.RUB.name)
            } catch (_: Exception) {
                Currency.RUB
            }
        return currency
    }
}
