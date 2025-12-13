package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import timber.log.Timber
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider

/**
 * Хранит последний выбор пользователя: категории (для расхода/дохода) и источника.
 */
class LastSelectionPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "last_selection_preferences"
        private const val KEY_LAST_EXPENSE_CATEGORY = "last_expense_category"
        private const val KEY_LAST_INCOME_CATEGORY = "last_income_category"
        private const val KEY_LAST_SOURCE_NAME = "last_source_name"
        private const val KEY_LAST_SOURCE_COLOR = "last_source_color"

        @Volatile
        private var instance: LastSelectionPreferences? = null

        fun getInstance(context: Context): LastSelectionPreferences =
            instance ?: synchronized(this) {
                instance ?: LastSelectionPreferences(context.applicationContext).also { instance = it }
            }
    }

    /**
     * Сохраняет последнюю выбранную категорию для расхода.
     * @param name Имя категории
     */
    fun setLastExpenseCategory(name: String) {
        try {
            prefs.edit { putString(KEY_LAST_EXPENSE_CATEGORY, name) }
        } catch (e: Exception) {
            Timber.e(e, "Error saving last expense category")
            CrashLoggerProvider.crashLogger.logException(e)
        }
    }

    /**
     * Возвращает последнюю выбранную категорию для расхода.
     */
    fun getLastExpenseCategory(): String = prefs.getString(KEY_LAST_EXPENSE_CATEGORY, "") ?: ""

    /**
     * Сохраняет последнюю выбранную категорию для дохода.
     * @param name Имя категории
     */
    fun setLastIncomeCategory(name: String) {
        try {
            prefs.edit { putString(KEY_LAST_INCOME_CATEGORY, name) }
        } catch (e: Exception) {
            Timber.e(e, "Error saving last income category")
            CrashLoggerProvider.crashLogger.logException(e)
        }
    }

    /**
     * Возвращает последнюю выбранную категорию для дохода.
     */
    fun getLastIncomeCategory(): String = prefs.getString(KEY_LAST_INCOME_CATEGORY, "") ?: ""

    /**
     * Сохраняет последний выбранный источник.
     * @param name Имя источника
     * @param color Цвет источника
     */
    fun setLastSource(name: String, color: Int) {
        try {
            prefs.edit {
                putString(KEY_LAST_SOURCE_NAME, name)
                putInt(KEY_LAST_SOURCE_COLOR, color)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving last source")
            CrashLoggerProvider.crashLogger.logException(e)
        }
    }

    /**
     * Возвращает имя последнего выбранного источника.
     */
    fun getLastSourceName(): String = prefs.getString(KEY_LAST_SOURCE_NAME, "") ?: ""

    /**
     * Возвращает цвет последнего выбранного источника.
     */
    fun getLastSourceColor(): Int = prefs.getInt(KEY_LAST_SOURCE_COLOR, 0)
}




