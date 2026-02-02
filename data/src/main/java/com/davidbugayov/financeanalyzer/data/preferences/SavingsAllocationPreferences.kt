package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import timber.log.Timber

/**
 * Предпочтения для управления автоматическим распределением сбережений.
 * Позволяет пользователю установить процент от доходов, который автоматически
 * откладывается как сбережения.
 */
class SavingsAllocationPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("savings_allocation_preferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_SAVINGS_ENABLED = "savings_allocation_enabled"
        private const val KEY_SAVINGS_PERCENTAGE = "savings_allocation_percentage"
        private const val KEY_SAVINGS_CATEGORY = "savings_allocation_category"

        @Volatile
        private var instance: SavingsAllocationPreferences? = null

        fun getInstance(context: Context): SavingsAllocationPreferences =
            instance ?: synchronized(this) {
                instance ?: SavingsAllocationPreferences(context).also { instance = it }
            }
    }

    /**
     * Включает/отключает автоматическое распределение сбережений
     */
    fun setSavingsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SAVINGS_ENABLED, enabled) }
        Timber.d("Автоматические сбережения: $enabled")
    }

    /**
     * Проверяет, включено ли автоматическое распределение сбережений
     */
    fun isSavingsEnabled(): Boolean = prefs.getBoolean(KEY_SAVINGS_ENABLED, false)

    /**
     * Устанавливает процент от доходов для автоматических сбережений (0-100)
     */
    fun setSavingsPercentage(percentage: Double) {
        require(percentage in 0.0..100.0) { "Процент должен быть от 0 до 100" }
        prefs.edit { putString(KEY_SAVINGS_PERCENTAGE, percentage.toString()) }
        Timber.d("Процент сбережений установлен: $percentage%")
    }

    /**
     * Получает процент от доходов для автоматических сбережений
     */
    fun getSavingsPercentage(): Double =
        prefs.getString(KEY_SAVINGS_PERCENTAGE, "20.0")?.toDoubleOrNull() ?: 20.0

    /**
     * Устанавливает категорию, в которую откладываются сбережения
     * (обычно это "Сбережения" или "Другие доходы")
     */
    fun setSavingsCategory(category: String) {
        prefs.edit { putString(KEY_SAVINGS_CATEGORY, category) }
        Timber.d("Категория сбережений установлена: $category")
    }

    /**
     * Получает категорию для сбережений
     */
    fun getSavingsCategory(): String? = prefs.getString(KEY_SAVINGS_CATEGORY, null)

    /**
     * Очищает все предпочтения для сбережений
     */
    fun clearSavingsPreferences() {
        prefs.edit {
            remove(KEY_SAVINGS_ENABLED)
            remove(KEY_SAVINGS_PERCENTAGE)
            remove(KEY_SAVINGS_CATEGORY)
        }
        Timber.d("Предпочтения сбережений очищены")
    }

    /**
     * Получает конфигурацию сбережений в виде объекта
     */
    data class SavingsConfig(
        val enabled: Boolean = false,
        val percentage: Double = 20.0,
        val category: String? = null,
    )

    fun getSavingsConfig(): SavingsConfig =
        SavingsConfig(
            enabled = isSavingsEnabled(),
            percentage = getSavingsPercentage(),
            category = getSavingsCategory(),
        )
}
