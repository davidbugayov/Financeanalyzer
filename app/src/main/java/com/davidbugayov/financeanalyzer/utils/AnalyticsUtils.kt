package com.davidbugayov.financeanalyzer.utils

import android.os.Bundle
import com.davidbugayov.financeanalyzer.FinanceApp
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

/**
 * Утилитарный класс для работы с Firebase Analytics.
 * Содержит методы для отслеживания событий и действий пользователя.
 */
object AnalyticsUtils {

    // Константы для имен событий
    object Events {

        const val TRANSACTION_ADDED = "transaction_added"
        const val TRANSACTION_DELETED = "transaction_deleted"
        const val CATEGORY_DELETED = "category_deleted"
        const val CHART_VIEWED = "chart_viewed"
        const val ERROR_OCCURRED = "error_occurred"
    }

    // Константы для параметров событий
    object Params {

        const val TRANSACTION_TYPE = "transaction_type"
        const val TRANSACTION_AMOUNT = "transaction_amount"
        const val TRANSACTION_CATEGORY = "transaction_category"
        const val CHART_TYPE = "chart_type"
        const val PERIOD_TYPE = "period_type"
        const val ERROR_TYPE = "error_type"
        const val ERROR_MESSAGE = "error_message"
        const val TRANSACTION_CURRENCY = "transaction_currency"
    }

    /**
     * Безопасно получает экземпляр FirebaseAnalytics
     * @return Экземпляр FirebaseAnalytics или null, если не удалось получить
     */
    private fun getAnalyticsInstance(): FirebaseAnalytics? {
        return try {
            if (FinanceApp.isFirebaseInitialized) {
                FinanceApp.analytics
            } else {
                Timber.w("Firebase не инициализирован, Analytics недоступен")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении экземпляра Analytics")
            null
        }
    }

    /**
     * Общий метод для логирования произвольного события
     * @param eventName Название события
     * @param params Параметры события в виде Map
     */
    fun logEvent(eventName: String, params: Map<String, Any>) {
        try {
            // Всегда логируем в Timber для отладки
            Timber.d("Analytics: Event - $eventName, params: $params")

            // Отправляем событие в Firebase Analytics
            val bundle = Bundle().apply {
                params.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Double -> putDouble(key, value)
                        is Boolean -> putBoolean(key, value)
                        else -> putString(key, value.toString())
                    }
                }
            }

            getAnalyticsInstance()?.logEvent(eventName, bundle)
                ?: Timber.d("Событие не отправлено: $eventName, params: $params")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при логировании события $eventName")
        }
    }

    /**
     * Логирует событие просмотра экрана
     * @param screenName Имя экрана
     * @param screenClass Класс экрана
     */
    fun logScreenView(screenName: String, screenClass: String) {
        try {
            // TODO: Вернуть проверку на DEBUG перед релизом
            // if (BuildConfig.DEBUG) {
            //     Timber.d("Analytics: Screen view - $screenName ($screenClass)")
            //     return
            // }

            // Всегда логируем в Timber для отладки
            Timber.d("Analytics: Screen view - $screenName ($screenClass)")

            // Отправляем событие в Firebase Analytics
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
            }

            getAnalyticsInstance()?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
                ?: Timber.d("Событие просмотра экрана не отправлено: $screenName ($screenClass)")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при логировании просмотра экрана")
        }
    }

    /**
     * Логирует добавление транзакции
     * @param type Тип транзакции (доход/расход)
     * @param amount Сумма транзакции в виде объекта Money
     * @param category Категория транзакции
     */
    fun logTransactionAdded(type: String, amount: Money, category: String) {
        try {
            // TODO: Вернуть проверку на DEBUG перед релизом
            // if (BuildConfig.DEBUG) {
            //     Timber.d("Analytics: Transaction added - $type, $amount, $category")
            //     return
            // }

            // Всегда логируем в Timber для отладки
            Timber.d("Analytics: Transaction added - $type, $amount, $category")

            // Отправляем событие в Firebase Analytics
            val bundle = Bundle().apply {
                putString(Params.TRANSACTION_TYPE, type)
                putDouble(Params.TRANSACTION_AMOUNT, amount.amount.toDouble())
                putString(Params.TRANSACTION_CATEGORY, category)
                putString(Params.TRANSACTION_CURRENCY, amount.currency.code)
            }

            getAnalyticsInstance()?.logEvent(Events.TRANSACTION_ADDED, bundle)
                ?: Timber.d("Событие добавления транзакции не отправлено: $type, $amount, $category")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при логировании добавления транзакции")
        }
    }

    /**
     * Устаревший метод для обратной совместимости
     * @deprecated Используйте версию с Money
     */
    @Deprecated("Используйте версию с Money", ReplaceWith("logTransactionAdded(type, Money(amount), category)"))
    fun logTransactionAdded(type: String, amount: Double, category: String) {
        logTransactionAdded(type, Money(amount), category)
    }

    /**
     * Логирует удаление транзакции
     * @param type Тип транзакции (доход/расход)
     * @param category Категория транзакции
     */
    fun logTransactionDeleted(type: String, category: String) {
        try {
            // TODO: Вернуть проверку на DEBUG перед релизом
            // if (BuildConfig.DEBUG) {
            //     Timber.d("Analytics: Transaction deleted - $type, $category")
            //     return
            // }

            // Всегда логируем в Timber для отладки
            Timber.d("Analytics: Transaction deleted - $type, $category")

            // Отправляем событие в Firebase Analytics
            val bundle = Bundle().apply {
                putString(Params.TRANSACTION_TYPE, type)
                putString(Params.TRANSACTION_CATEGORY, category)
            }

            getAnalyticsInstance()?.logEvent(Events.TRANSACTION_DELETED, bundle)
                ?: Timber.d("Событие удаления транзакции не отправлено: $type, $category")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при логировании удаления транзакции")
        }
    }

    /**
     * Логирует удаление категории
     * @param category Название категории
     * @param isExpense Является ли категория расходной
     */
    fun logCategoryDeleted(category: String, isExpense: Boolean) {
        try {
            // TODO: Вернуть проверку на DEBUG перед релизом
            // if (BuildConfig.DEBUG) {
            //     Timber.d("Analytics: Category deleted - $category, isExpense: $isExpense")
            //     return
            // }

            // Всегда логируем в Timber для отладки
            Timber.d("Analytics: Category deleted - $category, isExpense: $isExpense")

            // Отправляем событие в Firebase Analytics
            val bundle = Bundle().apply {
                putString(Params.TRANSACTION_CATEGORY, category)
                putString(Params.TRANSACTION_TYPE, if (isExpense) "expense" else "income")
            }

            getAnalyticsInstance()?.logEvent(Events.CATEGORY_DELETED, bundle)
                ?: Timber.d("Событие удаления категории не отправлено: $category, isExpense: $isExpense")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при логировании удаления категории")
        }
    }

    /**
     * Логирует просмотр графика
     * @param chartType Тип графика
     * @param periodType Тип периода
     */
    fun logChartViewed(chartType: String, periodType: String) {
        try {
            // TODO: Вернуть проверку на DEBUG перед релизом
            // if (BuildConfig.DEBUG) {
            //     Timber.d("Analytics: Chart viewed - $chartType, period: $periodType")
            //     return
            // }

            // Всегда логируем в Timber для отладки
            Timber.d("Analytics: Chart viewed - $chartType, period: $periodType")

            // Отправляем событие в Firebase Analytics
            val bundle = Bundle().apply {
                putString(Params.CHART_TYPE, chartType)
                putString(Params.PERIOD_TYPE, periodType)
            }

            getAnalyticsInstance()?.logEvent(Events.CHART_VIEWED, bundle)
                ?: Timber.d("Событие просмотра графика не отправлено: $chartType, period: $periodType")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при логировании просмотра графика")
        }
    }

    /**
     * Логирует ошибку
     * @param errorType Тип ошибки
     * @param errorMessage Сообщение об ошибке
     */
    fun logError(errorType: String, errorMessage: String) {
        try {
            // TODO: Вернуть проверку на DEBUG перед релизом
            // if (BuildConfig.DEBUG) {
            //     Timber.d("Analytics: Error - $errorType: $errorMessage")
            //     return
            // }

            // Всегда логируем в Timber для отладки
            Timber.d("Analytics: Error - $errorType: $errorMessage")

            // Отправляем событие в Firebase Analytics
            val bundle = Bundle().apply {
                putString(Params.ERROR_TYPE, errorType)
                putString(Params.ERROR_MESSAGE, errorMessage)
            }

            getAnalyticsInstance()?.logEvent(Events.ERROR_OCCURRED, bundle) ?: Timber.d("Событие ошибки не отправлено: $errorType: $errorMessage")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при логировании ошибки")
        }
    }

    /**
     * Логирует удаление источника
     * @param source Название источника
     */
    fun logSourceDeleted(source: String) {
        try {
            // TODO: Вернуть проверку на DEBUG перед релизом
            // if (BuildConfig.DEBUG) {
            //     Timber.d("Analytics: Source deleted - $source")
            //     return
            // }

            // Всегда логируем в Timber для отладки
            Timber.d("Analytics: Source deleted - $source")

            // Отправляем событие в Firebase Analytics
            val bundle = Bundle().apply {
                putString("source", source)
            }

            getAnalyticsInstance()?.logEvent("source_deleted", bundle)
                ?: Timber.d("Событие удаления источника не отправлено: $source")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при логировании удаления источника")
        }
    }
} 