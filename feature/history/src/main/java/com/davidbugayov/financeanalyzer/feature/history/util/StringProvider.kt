package com.davidbugayov.financeanalyzer.feature.history.util

import android.content.Context
import com.davidbugayov.financeanalyzer.feature.history.R

/**
 * Утилитный класс для получения строковых ресурсов в модуле history
 */
object StringProvider {
    
    private var context: Context? = null
    
    fun init(context: Context) {
        this.context = context.applicationContext
    }
    
    fun getString(@androidx.annotation.StringRes resId: Int): String {
        return context?.getString(resId) ?: "String not found"
    }
    
    fun getString(@androidx.annotation.StringRes resId: Int, vararg formatArgs: Any): String {
        return context?.getString(resId, *formatArgs) ?: "String not found"
    }
    
    // Статистика категорий
    val currentPeriod: String get() = getString(R.string.current_period)
    val previousPeriod: String get() = getString(R.string.previous_period)
    fun increaseByPercent(percent: Int): String = getString(R.string.increase_by_percent, percent)
    fun decreaseByPercent(percent: Int): String = getString(R.string.decrease_by_percent, percent)
    val noChanges: String get() = getString(R.string.no_changes)
    
    // Диалоги выбора
    val selectCategory: String get() = getString(R.string.select_category)
    val selectSources: String get() = getString(R.string.select_sources)
    val selectPeriod: String get() = getString(R.string.select_period)
    val allCategories: String get() = getString(R.string.all_categories)
    val allSources: String get() = getString(R.string.all_sources)
    val clearSelection: String get() = getString(R.string.clear_selection)
    val selectAll: String get() = getString(R.string.select_all)
    val apply: String get() = getString(R.string.apply)
    val close: String get() = getString(R.string.close)
    val cancel: String get() = getString(R.string.cancel)
    
    // Группы категорий
    val expenses: String get() = getString(R.string.expenses)
    val incomes: String get() = getString(R.string.incomes)
    
    // Удаление категории
    val deleteCategoryTitle: String get() = getString(R.string.delete_category_title)
    fun deleteCategoryConfirmIrreversible(category: String): String = getString(R.string.delete_category_confirm_irreversible, category)
    fun deleteCategoryConfirmMove(category: String): String = getString(R.string.delete_category_confirm_move, category)
    val delete: String get() = getString(R.string.delete)
    
    // Удаление источника
    fun deleteSourceConfirmationMessage(source: String): String = getString(R.string.delete_source_confirmation_message, source)
    
    // Защищенные категории
    val categoryOther: String get() = getString(R.string.category_other)
    
    // Группировка
    val groupByDays: String get() = getString(R.string.group_by_days)
    val groupByWeeks: String get() = getString(R.string.group_by_weeks)
    val groupByMonths: String get() = getString(R.string.group_by_months)
    val collapse: String get() = getString(R.string.collapse)
    val expand: String get() = getString(R.string.expand)
    
    // Заголовки
    val transactionHistory: String get() = getString(R.string.transaction_history)
    
    // Фильтры
    val categoryFilter: String get() = getString(R.string.category_filter)
    val sourceFilter: String get() = getString(R.string.source_filter)
    val filter: String get() = getString(R.string.filter)
    val addTransaction: String get() = getString(R.string.add_transaction)
    
    // Периоды
    val allTime: String get() = getString(R.string.all_time)
    val day: String get() = getString(R.string.day)
    val week: String get() = getString(R.string.week)
    val month: String get() = getString(R.string.month)
    val quarter: String get() = getString(R.string.quarter)
    val year: String get() = getString(R.string.year)
    val customPeriod: String get() = getString(R.string.custom_period)
    val startDate: String get() = getString(R.string.start_date)
    val endDate: String get() = getString(R.string.end_date)
    
    // Статистика
    fun categoriesSelected(categories: String): String = getString(R.string.categories_selected, categories)
    fun sourcesSelected(sources: String): String = getString(R.string.sources_selected, sources)
    
    // Загрузка
    val loadingData: String get() = getString(R.string.loading_data)
    
    // Ошибки
    val errorLoadingTransactions: String get() = getString(R.string.error_loading_transactions)
    
    // Форматирование
    fun weekFormat(week: Int, year: Int): String = getString(R.string.week_format, week, year)
} 