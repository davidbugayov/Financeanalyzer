package com.davidbugayov.financeanalyzer.feature.home.util

import android.content.Context
import com.davidbugayov.financeanalyzer.feature.home.R
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Утилитный класс для получения строковых ресурсов в модуле home
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
    
    // Заголовки и навигация
    val currentBalance: String get() = getString(R.string.current_balance)
    val hideSummary: String get() = getString(R.string.hide_summary)
    val showSummary: String get() = getString(R.string.show_summary)
    val transactionsToday: String get() = getString(R.string.transactions_today)
    val transactionsWeek: String get() = getString(R.string.transactions_week)
    val transactionsMonth: String get() = getString(R.string.transactions_month)
    val transactionsAll: String get() = getString(R.string.transactions_all)
    val financialAnalyzer: String get() = getString(R.string.financial_analyzer)
    
    // Приветствие
    val welcomeTitle: String get() = getString(R.string.welcome_title)
    val welcomeSubtitle: String get() = getString(R.string.welcome_subtitle)
    
    // Кнопки и действия
    val generateTestData: String get() = getString(R.string.generate_test_data)
    val profile: String get() = getString(R.string.profile)
    val addTransaction: String get() = getString(R.string.add_transaction)
    val addFirstTransaction: String get() = getString(R.string.add_first_transaction)
    
    // Сообщения
    val testDataGenerated: String get() = getString(R.string.test_data_generated)
    val transactionDeleted: String get() = getString(R.string.transaction_deleted)
    val emptyTransactionIdError: String get() = getString(R.string.empty_transaction_id_error)
    val loadingData: String get() = getString(R.string.loading_data)
    val errorSavingTestTransactions: String get() = getString(R.string.error_saving_test_transactions)
    val errorGeneratingTestData: String get() = getString(R.string.error_generating_test_data)
    
    // Фильтры
    val filterToday: String get() = getString(R.string.filter_today)
    val filterWeek: String get() = getString(R.string.filter_week)
    val filterMonth: String get() = getString(R.string.filter_month)
    val filterAll: String get() = getString(R.string.filter_all)
    val filterAllTime: String get() = getString(R.string.filter_all_time)
    
    // Сводка
    val totalIncome: String get() = getString(R.string.total_income)
    val totalExpense: String get() = getString(R.string.total_expense)
    val balance: String get() = getString(R.string.balance)
    val expenseCategories: String get() = getString(R.string.expense_categories)
    val incomeCategories: String get() = getString(R.string.income_categories)
    val showExpenses: String get() = getString(R.string.show_expenses)
    val showIncomes: String get() = getString(R.string.show_incomes)
    val noExpensesPeriod: String get() = getString(R.string.no_expenses_period)
    val noIncomesPeriod: String get() = getString(R.string.no_incomes_period)
    fun andMoreCategories(count: Int): String = getString(R.string.and_more_categories, count)
    val hide: String get() = getString(R.string.hide)
    
    // Пустое состояние
    val noTransactions: String get() = getString(R.string.no_transactions)
    val addFirstTransactionDescription: String get() = getString(R.string.add_first_transaction_description)
    val emptyStateIcon: String get() = getString(R.string.empty_state_icon)
    
    // Типы обратной связи
    val feedbackSuccess: String get() = getString(R.string.feedback_success)
    val feedbackError: String get() = getString(R.string.feedback_error)
    val feedbackWarning: String get() = getString(R.string.feedback_warning)
    val feedbackInfo: String get() = getString(R.string.feedback_info)
} 