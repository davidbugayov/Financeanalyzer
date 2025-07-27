package com.davidbugayov.financeanalyzer.domain.util

import android.content.Context
import com.davidbugayov.financeanalyzer.domain.R

/**
 * Утилитный класс для получения строковых ресурсов в модуле domain
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
    
    // Валидация
    val errorEnterTransactionAmount: String get() = getString(R.string.error_enter_transaction_amount)
    
    // Достижения
    val logAchievementTriggerInitialized: String get() = getString(R.string.log_achievement_trigger_initialized)
    val logAchievementTransactionAdded: String get() = getString(R.string.log_achievement_transaction_added)
    val logAchievementBudgetCreated: String get() = getString(R.string.log_achievement_budget_created)
    val logAchievementStatisticsViewed: String get() = getString(R.string.log_achievement_statistics_viewed)
    fun logAchievementSectionVisited(sectionName: String): String = getString(R.string.log_achievement_section_visited, sectionName)
    fun logAchievementSavingsChanged(amount: String): String = getString(R.string.log_achievement_savings_changed, amount)
    fun logAchievementBudgetProgress(percentage: Int): String = getString(R.string.log_achievement_budget_progress, percentage)
    fun logAchievementCategoryUsed(categoryId: String): String = getString(R.string.log_achievement_category_used, categoryId)
    fun logAchievementMilestoneReached(milestoneType: String): String = getString(R.string.log_achievement_milestone_reached, milestoneType)
    
    // Советы по расходам
    fun tipTopCategorySpending(category: String): String = getString(R.string.tip_top_category_spending, category)
    val tipSubscriptionsFound: String get() = getString(R.string.tip_subscriptions_found)
    val tipLargeExpenses: String get() = getString(R.string.tip_large_expenses)
    val tipSmallExpenses: String get() = getString(R.string.tip_small_expenses)
    val tipExpensesEqualIncome: String get() = getString(R.string.tip_expenses_equal_income)
    
    // Рекомендации по оптимизации
    val recommendationCheckSubscriptions: String get() = getString(R.string.recommendation_check_subscriptions)
    val recommendationLargeExpenses: String get() = getString(R.string.recommendation_large_expenses)
    val recommendationCafeSpending: String get() = getString(R.string.recommendation_cafe_spending)
    val recommendationSmallExpenses: String get() = getString(R.string.recommendation_small_expenses)
    val recommendationNoSavings: String get() = getString(R.string.recommendation_no_savings)
    
    // Экспорт
    val errorNoTransactionsToExport: String get() = getString(R.string.error_no_transactions_to_export)
    fun exportFilename(dateTime: String): String = getString(R.string.export_filename, dateTime)
    val exportCsvHeader: String get() = getString(R.string.export_csv_header)
    val exportTransactionTypeExpense: String get() = getString(R.string.export_transaction_type_expense)
    val exportTransactionTypeIncome: String get() = getString(R.string.export_transaction_type_income)
    fun logCsvFileCreated(path: String, size: Long): String = getString(R.string.log_csv_file_created, path, size)
    val logExportError: String get() = getString(R.string.log_export_error)
    val logExportUnexpectedError: String get() = getString(R.string.log_export_unexpected_error)
    fun logFileCreated(path: String): String = getString(R.string.log_file_created, path)
    fun logFileReadyForSend(path: String): String = getString(R.string.log_file_ready_for_send, path)
    
    // Сравнение с аналогами
    val incomeRangeInsufficientData: String get() = getString(R.string.income_range_insufficient_data)
    
    // Категории расходов
    val categoryProducts: String get() = getString(R.string.category_products)
    val categoryTransport: String get() = getString(R.string.category_transport)
    val categoryUtilities: String get() = getString(R.string.category_utilities)
    val categoryClothing: String get() = getString(R.string.category_clothing)
    val categoryEntertainment: String get() = getString(R.string.category_entertainment)
    val categoryOther: String get() = getString(R.string.category_other)
    val categorySubscription: String get() = getString(R.string.category_subscription)
    val categoryServices: String get() = getString(R.string.category_services)
    val categoryCafe: String get() = getString(R.string.category_cafe)
    val categoryRestaurant: String get() = getString(R.string.category_restaurant)
    
    // Рекомендации по финансовому здоровью
    val recommendationImproveFinancialHealth: String get() = getString(R.string.recommendation_improve_financial_health)
    fun recommendationImproveFinancialHealthDesc(score: Int): String = getString(R.string.recommendation_improve_financial_health_desc, score)
    val recommendationImproveExpenseControl: String get() = getString(R.string.recommendation_improve_expense_control)
    fun recommendationImproveExpenseControlDesc(index: Int): String = getString(R.string.recommendation_improve_expense_control_desc, index)
    val recommendationIncreaseRetirementSavings: String get() = getString(R.string.recommendation_increase_retirement_savings)
    fun recommendationIncreaseRetirementSavingsDesc(amount: String, progress: Int): String = getString(R.string.recommendation_increase_retirement_savings_desc, amount, progress)
    val recommendationIncreaseSavingsRate: String get() = getString(R.string.recommendation_increase_savings_rate)
    fun recommendationIncreaseSavingsRateDesc(difference: Int): String = getString(R.string.recommendation_increase_savings_rate_desc, difference)
    val recommendationDiversifyIncome: String get() = getString(R.string.recommendation_diversify_income)
    fun recommendationDiversifyIncomeDesc(sources: Int): String = getString(R.string.recommendation_diversify_income_desc, sources)
    val recommendationCreateEmergencyFund: String get() = getString(R.string.recommendation_create_emergency_fund)
    fun recommendationCreateEmergencyFundDesc(months: Int): String = getString(R.string.recommendation_create_emergency_fund_desc, months)
    fun recommendationBudgetExceeded(walletName: String): String = getString(R.string.recommendation_budget_exceeded, walletName)
    fun recommendationBudgetExceededDesc(walletName: String): String = getString(R.string.recommendation_budget_exceeded_desc, walletName)
    fun recommendationBudgetCloseToLimit(walletName: String): String = getString(R.string.recommendation_budget_close_to_limit, walletName)
    fun recommendationBudgetCloseToLimitDesc(walletName: String): String = getString(R.string.recommendation_budget_close_to_limit_desc, walletName)
    val recommendationCheckSubscriptionsTitle: String get() = getString(R.string.recommendation_check_subscriptions_title)
    val recommendationCheckSubscriptionsDesc: String get() = getString(R.string.recommendation_check_subscriptions_desc)
    val recommendationSaveOnCafe: String get() = getString(R.string.recommendation_save_on_cafe)
    val recommendationSaveOnCafeDesc: String get() = getString(R.string.recommendation_save_on_cafe_desc)
    
    // Названия категорий
    val categoryFood: String get() = getString(R.string.category_food)
    val categoryHealth: String get() = getString(R.string.category_health)
    val categoryHousing: String get() = getString(R.string.category_housing)
    val categoryCommunication: String get() = getString(R.string.category_communication)
    val categoryPet: String get() = getString(R.string.category_pet)
    val categoryCharity: String get() = getString(R.string.category_charity)
    val categoryCredit: String get() = getString(R.string.category_credit)
    val categoryTransfer: String get() = getString(R.string.category_transfer)
    val categoryOtherExpense: String get() = getString(R.string.category_other_expense)
    val categorySalary: String get() = getString(R.string.category_salary)
    val categoryFreelance: String get() = getString(R.string.category_freelance)
    val categoryGifts: String get() = getString(R.string.category_gifts)
    val categoryInterest: String get() = getString(R.string.category_interest)
    val categoryRental: String get() = getString(R.string.category_rental)
    val categoryOtherIncome: String get() = getString(R.string.category_other_income)
    
    // Периоды
    val periodAllTime: String get() = getString(R.string.period_all_time)
    fun periodDay(date: String): String = getString(R.string.period_day, date)
    fun periodWeek(startDate: String, endDate: String): String = getString(R.string.period_week, startDate, endDate)
    fun periodMonth(startDate: String, endDate: String): String = getString(R.string.period_month, startDate, endDate)
    fun periodQuarter(startDate: String, endDate: String): String = getString(R.string.period_quarter, startDate, endDate)
    fun periodYear(startDate: String, endDate: String): String = getString(R.string.period_year, startDate, endDate)
    fun periodCustom(startDate: String, endDate: String): String = getString(R.string.period_custom, startDate, endDate)
    
    // Типы кошельков
    val walletTypeCash: String get() = getString(R.string.wallet_type_cash)
    val walletTypeCard: String get() = getString(R.string.wallet_type_card)
    val walletTypeSavings: String get() = getString(R.string.wallet_type_savings)
    val walletTypeInvestment: String get() = getString(R.string.wallet_type_investment)
    val walletTypeGoal: String get() = getString(R.string.wallet_type_goal)
    val walletTypeOther: String get() = getString(R.string.wallet_type_other)
    
    // Описания типов кошельков
    val walletTypeCashDescription: String get() = getString(R.string.wallet_type_cash_description)
    val walletTypeCardDescription: String get() = getString(R.string.wallet_type_card_description)
    val walletTypeSavingsDescription: String get() = getString(R.string.wallet_type_savings_description)
    val walletTypeInvestmentDescription: String get() = getString(R.string.wallet_type_investment_description)
    val walletTypeGoalDescription: String get() = getString(R.string.wallet_type_goal_description)
    val walletTypeOtherDescription: String get() = getString(R.string.wallet_type_other_description)
    
    // Транзакции
    val transactionTransfers: String get() = getString(R.string.transaction_transfers)
    val transactionTransfer: String get() = getString(R.string.transaction_transfer)
} 