package com.davidbugayov.financeanalyzer.utils

import android.os.Bundle
import com.davidbugayov.financeanalyzer.BuildConfig
import com.davidbugayov.financeanalyzer.FinanceApp
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

/**
 * Утилитарный класс для работы с Firebase Analytics.
 * Содержит методы для отслеживания событий в приложении.
 */
object AnalyticsUtils {

    /**
     * Логирует событие в Firebase Analytics
     * @param eventName Название события
     * @param params Параметры события
     */
    private fun logEvent(eventName: String, params: Bundle?) {
        try {
            if (BuildConfig.DEBUG) {
                // В debug-сборке только логируем события
                Timber.d("Analytics event: $eventName, params: $params")
            } else {
                // В release-сборке отправляем события в Firebase
                FinanceApp.analytics.logEvent(eventName, params)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to log analytics event: $eventName")
        }
    }

    // События для экранов
    object Screen {

        /**
         * Отслеживает открытие экрана
         * @param screenName Название экрана
         * @param screenClass Класс экрана
         */
        fun view(screenName: String, screenClass: String) {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
            }
            logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        }
    }

    // События для транзакций
    object Transaction {

        /**
         * Отслеживает добавление новой транзакции
         * @param transaction Объект транзакции
         */
        fun add(transaction: com.davidbugayov.financeanalyzer.domain.model.Transaction) {
            val bundle = Bundle().apply {
                putString("transaction_id", transaction.id.toString())
                putString("transaction_type", if (transaction.isExpense) "expense" else "income")
                putString("transaction_category", transaction.category)
                putDouble("transaction_amount", transaction.amount.amount.toDouble())
                putString("transaction_currency", transaction.amount.currency.name)
                putLong("transaction_date", transaction.date.time)
                transaction.title?.let { putString("transaction_title", it) }
                transaction.note?.let { putString("transaction_note", it) }
            }
            logEvent("transaction_added", bundle)
        }

        /**
         * Отслеживает удаление транзакции
         * @param transactionId ID транзакции
         * @param category Категория транзакции
         * @param amount Сумма транзакции
         * @param isExpense Тип транзакции (расход/доход)
         */
        fun delete(transactionId: Long, category: String, amount: Money, isExpense: Boolean) {
            val bundle = Bundle().apply {
                putString("transaction_id", transactionId.toString())
                putString("transaction_type", if (isExpense) "expense" else "income")
                putString("transaction_category", category)
                putDouble("transaction_amount", amount.amount.toDouble())
                putString("transaction_currency", amount.currency.name)
            }
            logEvent("transaction_deleted", bundle)
        }
    }

    // События для категорий
    object Category {

        /**
         * Отслеживает добавление новой категории
         * @param categoryName Название категории
         * @param isExpense Тип категории (расход/доход)
         */
        fun add(categoryName: String, isExpense: Boolean) {
            val bundle = Bundle().apply {
                putString("category_name", categoryName)
                putString("category_type", if (isExpense) "expense" else "income")
                putBoolean("is_custom", true)
            }
            logEvent("category_added", bundle)
        }

        /**
         * Отслеживает удаление категории
         * @param categoryName Название категории
         * @param isExpense Тип категории (расход/доход)
         * @param isCustom Является ли категория пользовательской
         */
        fun delete(categoryName: String, isExpense: Boolean, isCustom: Boolean) {
            val bundle = Bundle().apply {
                putString("category_name", categoryName)
                putString("category_type", if (isExpense) "expense" else "income")
                putBoolean("is_custom", isCustom)
            }
            logEvent("category_deleted", bundle)
        }
    }

    // События для фильтров и периодов
    object Filter {

        /**
         * Отслеживает изменение периода фильтрации
         * @param periodType Тип периода (день, неделя, месяц и т.д.)
         * @param startDate Начальная дата (в миллисекундах)
         * @param endDate Конечная дата (в миллисекундах)
         */
        fun changePeriod(periodType: String, startDate: Long, endDate: Long) {
            val bundle = Bundle().apply {
                putString("period_type", periodType)
                putLong("start_date", startDate)
                putLong("end_date", endDate)
                putLong("period_duration", endDate - startDate)
            }
            logEvent("filter_period_changed", bundle)
        }

        /**
         * Отслеживает применение фильтра по категории
         * @param categoryName Название категории
         * @param isExpense Тип категории (расход/доход)
         */
        fun applyCategory(categoryName: String, isExpense: Boolean) {
            val bundle = Bundle().apply {
                putString("category_name", categoryName)
                putString("category_type", if (isExpense) "expense" else "income")
            }
            logEvent("filter_category_applied", bundle)
        }
    }

    // События для диаграмм и аналитики
    object Chart {

        /**
         * Отслеживает просмотр диаграммы
         * @param chartType Тип диаграммы (pie, bar, line и т.д.)
         * @param dataType Тип данных (expenses, income, balance и т.д.)
         */
        fun view(chartType: String, dataType: String) {
            val bundle = Bundle().apply {
                putString("chart_type", chartType)
                putString("data_type", dataType)
            }
            logEvent("chart_viewed", bundle)
        }

        /**
         * Отслеживает экспорт данных
         * @param format Формат экспорта (csv, pdf и т.д.)
         * @param periodType Тип периода данных
         * @param recordCount Количество записей
         */
        fun export(format: String, periodType: String, recordCount: Int) {
            val bundle = Bundle().apply {
                putString("export_format", format)
                putString("period_type", periodType)
                putInt("record_count", recordCount)
            }
            logEvent("data_exported", bundle)
        }
    }

    // События для источников средств
    object Source {

        /**
         * Отслеживает добавление нового источника средств
         * @param sourceName Название источника
         * @param color Цвет источника
         */
        fun add(sourceName: String, color: Int) {
            val bundle = Bundle().apply {
                putString("source_name", sourceName)
                putInt("source_color", color)
            }
            logEvent("source_added", bundle)
        }

        /**
         * Отслеживает выбор источника средств при добавлении транзакции
         * @param sourceName Название источника
         */
        fun select(sourceName: String) {
            val bundle = Bundle().apply {
                putString("source_name", sourceName)
            }
            logEvent("source_selected", bundle)
        }
    }
} 