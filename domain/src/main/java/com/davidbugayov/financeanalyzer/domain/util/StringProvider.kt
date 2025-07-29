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
    fun logAchievementSectionVisited(section: String): String = getString(R.string.log_achievement_section_visited, section)
    fun logAchievementSavingsChanged(savings: String): String = getString(R.string.log_achievement_savings_changed, savings)
    fun logAchievementBudgetProgress(progress: Int): String = getString(R.string.log_achievement_budget_progress, progress)
    fun logAchievementCategoryUsed(category: String): String = getString(R.string.log_achievement_category_used, category)
    fun logAchievementMilestoneReached(milestone: String): String = getString(R.string.log_achievement_milestone_reached, milestone)
    
    // Названия достижений
    val achievementFirstSteps: String get() = getString(R.string.achievement_first_steps)
    val achievementFirstStepsDesc: String get() = getString(R.string.achievement_first_steps_desc)
    val achievementTransactionMaster: String get() = getString(R.string.achievement_transaction_master)
    val achievementTransactionMasterDesc: String get() = getString(R.string.achievement_transaction_master_desc)
    val achievementDataAnalyst: String get() = getString(R.string.achievement_data_analyst)
    val achievementDataAnalystDesc: String get() = getString(R.string.achievement_data_analyst_desc)
    val achievementFirstBudget: String get() = getString(R.string.achievement_first_budget)
    val achievementFirstBudgetDesc: String get() = getString(R.string.achievement_first_budget_desc)
    val achievementExplorer: String get() = getString(R.string.achievement_explorer)
    val achievementExplorerDesc: String get() = getString(R.string.achievement_explorer_desc)
    val achievementCategoryOrganizer: String get() = getString(R.string.achievement_category_organizer)
    val achievementCategoryOrganizerDesc: String get() = getString(R.string.achievement_category_organizer_desc)
    val achievementEarlyBird: String get() = getString(R.string.achievement_early_bird)
    val achievementEarlyBirdDesc: String get() = getString(R.string.achievement_early_bird_desc)
    val achievementNightOwl: String get() = getString(R.string.achievement_night_owl)
    val achievementNightOwlDesc: String get() = getString(R.string.achievement_night_owl_desc)
    val achievementFirstSavings: String get() = getString(R.string.achievement_first_savings)
    val achievementFirstSavingsDesc: String get() = getString(R.string.achievement_first_savings_desc)
    val achievementEmergencyFund: String get() = getString(R.string.achievement_emergency_fund)
    val achievementEmergencyFundDesc: String get() = getString(R.string.achievement_emergency_fund_desc)
    val achievementEconomical: String get() = getString(R.string.achievement_economical)
    val achievementEconomicalDesc: String get() = getString(R.string.achievement_economical_desc)
    val achievementRegularUser: String get() = getString(R.string.achievement_regular_user)
    val achievementRegularUserDesc: String get() = getString(R.string.achievement_regular_user_desc)
    val achievementLoyalUser: String get() = getString(R.string.achievement_loyal_user)
    val achievementLoyalUserDesc: String get() = getString(R.string.achievement_loyal_user_desc)
    val achievementCategoryExpert: String get() = getString(R.string.achievement_category_expert)
    val achievementCategoryExpertDesc: String get() = getString(R.string.achievement_category_expert_desc)
    val achievementTinkoffIntegrator: String get() = getString(R.string.achievement_tinkoff_integrator)
    val achievementTinkoffIntegratorDesc: String get() = getString(R.string.achievement_tinkoff_integrator_desc)
    val achievementSberCollector: String get() = getString(R.string.achievement_sber_collector)
    val achievementSberCollectorDesc: String get() = getString(R.string.achievement_sber_collector_desc)
    val achievementAlphaAnalyst: String get() = getString(R.string.achievement_alpha_analyst)
    val achievementAlphaAnalystDesc: String get() = getString(R.string.achievement_alpha_analyst_desc)
    val achievementOzonCollector: String get() = getString(R.string.achievement_ozon_collector)
    val achievementOzonCollectorDesc: String get() = getString(R.string.achievement_ozon_collector_desc)
    val achievementMultiBankCollector: String get() = getString(R.string.achievement_multi_bank_collector)
    val achievementMultiBankCollectorDesc: String get() = getString(R.string.achievement_multi_bank_collector_desc)
    val achievementExportMaster: String get() = getString(R.string.achievement_export_master)
    val achievementExportMasterDesc: String get() = getString(R.string.achievement_export_master_desc)
    val achievementBackupEnthusiast: String get() = getString(R.string.achievement_backup_enthusiast)
    val achievementBackupEnthusiastDesc: String get() = getString(R.string.achievement_backup_enthusiast_desc)
    val achievementCsvImporter: String get() = getString(R.string.achievement_csv_importer)
    val achievementCsvImporterDesc: String get() = getString(R.string.achievement_csv_importer_desc)
    
    // Ошибки логирования
    val logErrorLoadingAchievements: String get() = getString(R.string.log_error_loading_achievements)
    val logErrorSavingAchievements: String get() = getString(R.string.log_error_saving_achievements)
    val logErrorLoadingAchievementProgress: String get() = getString(R.string.log_error_loading_achievement_progress)
    fun logErrorUpdatingAchievementProgress(achievementId: String): String = getString(R.string.log_error_updating_achievement_progress, achievementId)
    fun logErrorCheckingAchievement(achievementId: String): String = getString(R.string.log_error_checking_achievement, achievementId)
    val logErrorProfileAnalytics: String get() = getString(R.string.log_error_profile_analytics)
    
    // Логирование транзакций
    fun logTransactionUpdate(transaction: String): String = getString(R.string.log_transaction_update, transaction)
    fun logTransactionAdd(amount: String, category: String): String = getString(R.string.log_transaction_add, amount, category)
    fun logTransactionNotFound(id: String): String = getString(R.string.log_transaction_not_found, id)
    
    // Месяцы для группировки
    val monthJanuary: String get() = getString(R.string.month_january)
    val monthFebruary: String get() = getString(R.string.month_february)
    val monthMarch: String get() = getString(R.string.month_march)
    val monthApril: String get() = getString(R.string.month_april)
    val monthMay: String get() = getString(R.string.month_may)
    val monthJune: String get() = getString(R.string.month_june)
    val monthJuly: String get() = getString(R.string.month_july)
    val monthAugust: String get() = getString(R.string.month_august)
    val monthSeptember: String get() = getString(R.string.month_september)
    val monthOctober: String get() = getString(R.string.month_october)
    val monthNovember: String get() = getString(R.string.month_november)
    val monthDecember: String get() = getString(R.string.month_december)
    
    // Категории
    val categoryProducts: String get() = getString(R.string.category_products)
    val categorySubscription: String get() = getString(R.string.category_subscription)
    val categoryCafe: String get() = getString(R.string.category_cafe)
    val categoryRestaurant: String get() = getString(R.string.category_restaurant)
    val categoryServices: String get() = getString(R.string.category_services)
    val categoryOtherExpense: String get() = getString(R.string.category_other_expense)
    val categoryGifts: String get() = getString(R.string.category_gifts)
    val categoryRental: String get() = getString(R.string.category_rental)
    val categoryOtherIncome: String get() = getString(R.string.category_other_income)
    val categoryTransport: String get() = getString(R.string.category_transport)
    val categoryUtilities: String get() = getString(R.string.category_utilities)
    val categoryClothing: String get() = getString(R.string.category_clothing)
    val categoryEntertainment: String get() = getString(R.string.category_entertainment)
    val categoryOther: String get() = getString(R.string.category_other)
    
    // Рекомендации
    val recommendationCheckSubscriptions: String get() = getString(R.string.recommendation_check_subscriptions)
    val recommendationLargeExpenses: String get() = getString(R.string.recommendation_large_expenses)
    val recommendationCafeSpending: String get() = getString(R.string.recommendation_cafe_spending)
    val recommendationSmallExpenses: String get() = getString(R.string.recommendation_small_expenses)
    val recommendationNoSavings: String get() = getString(R.string.recommendation_no_savings)
    
    // Дополнительные рекомендации
    val recommendationImproveFinancialHealth: String get() = getString(R.string.recommendation_improve_financial_health)
    fun recommendationImproveFinancialHealthDesc(healthScore: Int): String = getString(R.string.recommendation_improve_financial_health_desc, healthScore)
    val recommendationImproveExpenseControl: String get() = getString(R.string.recommendation_improve_expense_control)
    fun recommendationImproveExpenseControlDesc(disciplineIndex: Int): String = getString(R.string.recommendation_improve_expense_control_desc, disciplineIndex)
    val recommendationIncreaseRetirementSavings: String get() = getString(R.string.recommendation_increase_retirement_savings)
    fun recommendationIncreaseRetirementSavingsDesc(monthlyDeficit: String, progress: Int): String = getString(R.string.recommendation_increase_retirement_savings_desc, monthlyDeficit, progress)
    val recommendationIncreaseSavingsRate: String get() = getString(R.string.recommendation_increase_savings_rate)
    fun recommendationIncreaseSavingsRateDesc(percentage: Int): String = getString(R.string.recommendation_increase_savings_rate_desc, percentage)
    val recommendationDiversifyIncome: String get() = getString(R.string.recommendation_diversify_income)
    fun recommendationDiversifyIncomeDesc(sources: Int): String = getString(R.string.recommendation_diversify_income_desc, sources)
    val recommendationCreateEmergencyFund: String get() = getString(R.string.recommendation_create_emergency_fund)
    fun recommendationCreateEmergencyFundDesc(months: Int): String = getString(R.string.recommendation_create_emergency_fund_desc, months)
    fun recommendationBudgetExceeded(category: String): String = getString(R.string.recommendation_budget_exceeded, category)
    fun recommendationBudgetExceededDesc(category: String): String = getString(R.string.recommendation_budget_exceeded_desc, category)
    fun recommendationBudgetCloseToLimit(category: String): String = getString(R.string.recommendation_budget_close_to_limit, category)
    fun recommendationBudgetCloseToLimitDesc(category: String): String = getString(R.string.recommendation_budget_close_to_limit_desc, category)
    val recommendationCheckSubscriptionsTitle: String get() = getString(R.string.recommendation_check_subscriptions_title)
    val recommendationCheckSubscriptionsDesc: String get() = getString(R.string.recommendation_check_subscriptions_desc)
    val recommendationSaveOnCafe: String get() = getString(R.string.recommendation_save_on_cafe)
    val recommendationSaveOnCafeDesc: String get() = getString(R.string.recommendation_save_on_cafe_desc)
    
    // Экспорт
    fun exportFilename(date: String): String = getString(R.string.export_filename, date)
    val exportCsvHeader: String get() = getString(R.string.export_csv_header)
    val exportTransactionTypeExpense: String get() = getString(R.string.export_transaction_type_expense)
    val exportTransactionTypeIncome: String get() = getString(R.string.export_transaction_type_income)
    fun logCsvFileCreated(filename: String, size: Int): String = getString(R.string.log_csv_file_created, filename, size)
    val logExportError: String get() = getString(R.string.log_export_error)
    val logExportUnexpectedError: String get() = getString(R.string.log_export_unexpected_error)
    fun logFileCreated(filename: String): String = getString(R.string.log_file_created, filename)
    fun logFileReadyForSend(filename: String): String = getString(R.string.log_file_ready_for_send, filename)
    val errorNoTransactionsToExport: String get() = getString(R.string.error_no_transactions_to_export)
    
    // Советы
    fun tipTopCategorySpending(category: String): String = getString(R.string.tip_top_category_spending, category)
    fun tipSubscriptionsFound(count: Int): String = getString(R.string.tip_subscriptions_found, count)
    fun tipLargeExpenses(category: String): String = getString(R.string.tip_large_expenses, category)
    fun tipSmallExpenses(category: String): String = getString(R.string.tip_small_expenses, category)
    val tipExpensesEqualIncome: String get() = getString(R.string.tip_expenses_equal_income)
    
    // Сравнение с аналогами
    val incomeRangeInsufficientData: String get() = getString(R.string.income_range_insufficient_data)
    
    // Типы кошельков
    val walletTypeCash: String get() = getString(R.string.wallet_type_cash)
    val walletTypeCard: String get() = getString(R.string.wallet_type_card)
    val walletTypeSavings: String get() = getString(R.string.wallet_type_savings)
    val walletTypeInvestment: String get() = getString(R.string.wallet_type_investment)
    val walletTypeGoal: String get() = getString(R.string.wallet_type_goal)
    val walletTypeOther: String get() = getString(R.string.wallet_type_other)
    val walletTypeCashDescription: String get() = getString(R.string.wallet_type_cash_description)
    val walletTypeCardDescription: String get() = getString(R.string.wallet_type_card_description)
    val walletTypeSavingsDescription: String get() = getString(R.string.wallet_type_savings_description)
    val walletTypeInvestmentDescription: String get() = getString(R.string.wallet_type_investment_description)
    val walletTypeGoalDescription: String get() = getString(R.string.wallet_type_goal_description)
    val walletTypeOtherDescription: String get() = getString(R.string.wallet_type_other_description)
} 