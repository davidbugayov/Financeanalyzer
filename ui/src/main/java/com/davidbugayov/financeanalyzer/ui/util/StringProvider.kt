package com.davidbugayov.financeanalyzer.ui.util

import android.content.Context
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Утилитный класс для получения строковых ресурсов в модуле UI
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
    
    // UI компоненты
    val pathCopied: String get() = getString(R.string.path_copied)
    val monthsShort: String get() = getString(R.string.months_short)
    val loadingDataDefault: String get() = getString(R.string.loading_data_default)
    val closeButton: String get() = getString(R.string.close_button)
    
    // Рекомендации - уровни
    val criticalLevel: String get() = getString(R.string.critical_level)
    val highLevel: String get() = getString(R.string.high_level)
    val mediumLevel: String get() = getString(R.string.medium_level)
    val normalLevel: String get() = getString(R.string.normal_level)
    val lowLevel: String get() = getString(R.string.low_level)
    
    val criticalLevelAlt: String get() = getString(R.string.critical_level_alt)
    val importantLevel: String get() = getString(R.string.important_level)
    val worthConsidering: String get() = getString(R.string.worth_considering)
    val recommended: String get() = getString(R.string.recommended)
    val forNote: String get() = getString(R.string.for_note)
    
    // Категории рекомендаций
    val savingsCategory: String get() = getString(R.string.savings_category)
    val expensesCategory: String get() = getString(R.string.expenses_category)
    val budgetingCategory: String get() = getString(R.string.budgeting_category)
    val investmentsCategory: String get() = getString(R.string.investments_category)
    val emergencyFundCategory: String get() = getString(R.string.emergency_fund_category)
    val habitsCategory: String get() = getString(R.string.habits_category)
    val generalCategory: String get() = getString(R.string.general_category)
    
    // Рекомендации по сбережениям
    val criticalLowSavingsTitle: String get() = getString(R.string.critical_low_savings_title)
    val criticalLowSavingsDescription: String get() = getString(R.string.critical_low_savings_description)
    val criticalLowSavingsImpact: String get() = getString(R.string.critical_low_savings_impact)
    
    val improveSavingsRateTitle: String get() = getString(R.string.improve_savings_rate_title)
    val improveSavingsRateDescription: String get() = getString(R.string.improve_savings_rate_description)
    val improveSavingsRateImpact: String get() = getString(R.string.improve_savings_rate_impact)
    
    fun optimizeCategoryTitle(category: String): String = getString(R.string.optimize_category_title, category)
    fun optimizeCategoryDescription(percentage: Int): String = getString(R.string.optimize_category_description, percentage)
    val optimizeCategoryImpact: String get() = getString(R.string.optimize_category_impact)
    
    val createEmergencyFundTitleUi: String get() = getString(R.string.create_emergency_fund_title_ui)
    val createEmergencyFundDescriptionUi: String get() = getString(R.string.create_emergency_fund_description_ui)
    val createEmergencyFundImpact: String get() = getString(R.string.create_emergency_fund_impact)
    
    val considerInvestmentsTitle: String get() = getString(R.string.consider_investments_title)
    val considerInvestmentsDescription: String get() = getString(R.string.consider_investments_description)
    val considerInvestmentsImpact: String get() = getString(R.string.consider_investments_impact)
    
    val manySmallExpensesTitle: String get() = getString(R.string.many_small_expenses_title)
    fun manySmallExpensesDescription(count: Int): String = getString(R.string.many_small_expenses_description, count)
    val manySmallExpensesImpact: String get() = getString(R.string.many_small_expenses_impact)
    
    // Рекомендации для карточек
    val createEmergencyFundUrgentTitle: String get() = getString(R.string.create_emergency_fund_urgent_title)
    val createEmergencyFundUrgentDescription: String get() = getString(R.string.create_emergency_fund_urgent_description)
    val createEmergencyFundUrgentImpact: String get() = getString(R.string.create_emergency_fund_urgent_impact)
    
    val savingsRateCriticalTitle: String get() = getString(R.string.savings_rate_critical_title)
    val savingsRateCriticalDescription: String get() = getString(R.string.savings_rate_critical_description)
    val savingsRateCriticalImpact: String get() = getString(R.string.savings_rate_critical_impact)
    
    val increaseSavingsRateTitleUi: String get() = getString(R.string.increase_savings_rate_title_ui)
    val increaseSavingsRateDescriptionUi: String get() = getString(R.string.increase_savings_rate_description_ui)
    val increaseSavingsRateImpactUi: String get() = getString(R.string.increase_savings_rate_impact_ui)
    
    val thinkAboutInvestmentsTitle: String get() = getString(R.string.think_about_investments_title)
    val thinkAboutInvestmentsDescription: String get() = getString(R.string.think_about_investments_description)
    val thinkAboutInvestmentsImpact: String get() = getString(R.string.think_about_investments_impact)
    
    val studyAchievementsTitle: String get() = getString(R.string.study_achievements_title)
    val studyAchievementsDescription: String get() = getString(R.string.study_achievements_description)
    
    // Рекомендации с эмодзи
    val criticalEmoji: String get() = getString(R.string.critical_emoji)
    val importantEmoji: String get() = getString(R.string.important_emoji)
    val ideaEmoji: String get() = getString(R.string.idea_emoji)
    val checkEmoji: String get() = getString(R.string.check_emoji)
    val thoughtEmoji: String get() = getString(R.string.thought_emoji)
    
    // Текст для рекомендаций
    fun criticalCountRecommendations(count: Int): String = getString(R.string.critical_count_recommendations, count)
    fun importantCountRecommendations(count: Int): String = getString(R.string.important_count_recommendations, count)
    val recommendationsRequireAttention: String get() = getString(R.string.recommendations_require_attention)
    
    // Дополнительные строки для Placeholders
    val noDataToDisplay: String get() = getString(R.string.no_data_to_display)
    val addTransactionsToSeeAnalytics: String get() = getString(R.string.add_transactions_to_see_analytics)
    val refresh: String get() = getString(R.string.refresh)
    val loadingError: String get() = getString(R.string.loading_error)
    val retry: String get() = getString(R.string.retry)
    
    // Строки для SmartRecommendationSystem
    val smartCardDefaultTitle: String get() = getString(R.string.smart_card_default_title)
    val smartCardDefaultSubtitle: String get() = getString(R.string.smart_card_default_subtitle)
    
    // Строки для FinancialDataMapper
    val statTotalTransactions: String get() = getString(R.string.stat_total_transactions)
    val statTotalTransactionsDesc: String get() = getString(R.string.stat_total_transactions_desc)
    val statIncomeTransactions: String get() = getString(R.string.stat_income_transactions)
    val statIncomeTransactionsDesc: String get() = getString(R.string.stat_income_transactions_desc)
    val statExpenseTransactions: String get() = getString(R.string.stat_expense_transactions)
    val statExpenseTransactionsDesc: String get() = getString(R.string.stat_expense_transactions_desc)
    val statAvgIncome: String get() = getString(R.string.stat_avg_income)
    val statAvgIncomeDesc: String get() = getString(R.string.stat_avg_income_desc)
    val statAvgExpense: String get() = getString(R.string.stat_avg_expense)
    val statAvgExpenseDesc: String get() = getString(R.string.stat_avg_expense_desc)
    val statMaxIncome: String get() = getString(R.string.stat_max_income)
    val statMaxIncomeDesc: String get() = getString(R.string.stat_max_income_desc)
    val statMaxExpense: String get() = getString(R.string.stat_max_expense)
    val statMaxExpenseDesc: String get() = getString(R.string.stat_max_expense_desc)
    val statSavingsRate: String get() = getString(R.string.stat_savings_rate)
    val statSavingsRateDesc: String get() = getString(R.string.stat_savings_rate_desc)
    val statFinancialCushion: String get() = getString(R.string.stat_financial_cushion)
    val statFinancialCushionDesc: String get() = getString(R.string.stat_financial_cushion_desc)
    val statDailyExpense: String get() = getString(R.string.stat_daily_expense)
    val statDailyExpenseDesc: String get() = getString(R.string.stat_daily_expense_desc)
    val statMonthlyExpense: String get() = getString(R.string.stat_monthly_expense)
    val statMonthlyExpenseDesc: String get() = getString(R.string.stat_monthly_expense_desc)
    val statMainIncomeCategory: String get() = getString(R.string.stat_main_income_category)
    val statMainIncomeCategoryDesc: String get() = getString(R.string.stat_main_income_category_desc)
    val statMainExpenseCategory: String get() = getString(R.string.stat_main_expense_category)
    val statMainExpenseCategoryDesc: String get() = getString(R.string.stat_main_expense_category_desc)
    val statMostFrequentExpenseDay: String get() = getString(R.string.stat_most_frequent_expense_day)
    val statMostFrequentExpenseDayDesc: String get() = getString(R.string.stat_most_frequent_expense_day_desc)
    
    // Методы с параметрами для FinancialDataMapper
    fun statIncomeTransactionsDesc(percentage: Int): String = getString(R.string.stat_income_transactions_desc, percentage)
    fun statExpenseTransactionsDesc(percentage: Int): String = getString(R.string.stat_expense_transactions_desc, percentage)
    
    // Строки для insight
    val insightExpenseConcentration: String get() = getString(R.string.insight_expense_concentration)
    val insightExpenseConcentrationDesc: String get() = getString(R.string.insight_expense_concentration_desc)
    val insightExpenseConcentrationCritical: String get() = getString(R.string.insight_expense_concentration_critical)
    val insightExpenseConcentrationHigh: String get() = getString(R.string.insight_expense_concentration_high)
    val insightExpenseConcentrationGood: String get() = getString(R.string.insight_expense_concentration_good)
    val insightExpenseConcentrationMetric: String get() = getString(R.string.insight_expense_concentration_metric)
    
    // Строки для RecommendationGenerator
    val recOnboardingAchievementsTitle: String get() = getString(R.string.rec_onboarding_achievements_title)
    val recOnboardingAchievementsDesc: String get() = getString(R.string.rec_onboarding_achievements_desc)
    val recOnboardingImportTitle: String get() = getString(R.string.rec_onboarding_import_title)
    val recOnboardingImportDesc: String get() = getString(R.string.rec_onboarding_import_desc)
    val recCriticalEmergencyTitle: String get() = getString(R.string.rec_critical_emergency_title)
    val recCriticalEmergencyDesc: String get() = getString(R.string.rec_critical_emergency_desc)
    val recCriticalEmergencyImpact: String get() = getString(R.string.rec_critical_emergency_impact)
    val recNormalInvestTitle: String get() = getString(R.string.rec_normal_invest_title)
    val recNormalInvestDesc: String get() = getString(R.string.rec_normal_invest_desc)
    val recNormalInvestImpact: String get() = getString(R.string.rec_normal_invest_impact)
    
    // Дополнительные строки для FinancialDataMapper
    val insightFinancialHealth: String get() = getString(R.string.insight_financial_health)
    val insightFinancialHealthExcellent: String get() = getString(R.string.insight_financial_health_excellent)
    val insightFinancialHealthGood: String get() = getString(R.string.insight_financial_health_good)
    val insightFinancialHealthOk: String get() = getString(R.string.insight_financial_health_ok)
    val insightFinancialHealthLow: String get() = getString(R.string.insight_financial_health_low)
    val insightFinancialHealthCritical: String get() = getString(R.string.insight_financial_health_critical)
    val insightFinancialHealthMetric: String get() = getString(R.string.insight_financial_health_metric)
    
    val insightFinancialProtection: String get() = getString(R.string.insight_financial_protection)
    val insightFinancialProtectionExcellent: String get() = getString(R.string.insight_financial_protection_excellent)
    val insightFinancialProtectionGood: String get() = getString(R.string.insight_financial_protection_good)
    val insightFinancialProtectionMinimal: String get() = getString(R.string.insight_financial_protection_minimal)
    val insightFinancialProtectionNone: String get() = getString(R.string.insight_financial_protection_none)
    val insightFinancialProtectionMetric: String get() = getString(R.string.insight_financial_protection_metric)
    
    val insightExpenseFrequency: String get() = getString(R.string.insight_expense_frequency)
    val insightExpenseFrequencyDesc: String get() = getString(R.string.insight_expense_frequency_desc)
    val insightExpenseFrequencyMetric: String get() = getString(R.string.insight_expense_frequency_metric)
    
    val insightExpensePattern: String get() = getString(R.string.insight_expense_pattern)
    val insightExpensePatternDesc: String get() = getString(R.string.insight_expense_pattern_desc)
    val insightExpensePatternMetric: String get() = getString(R.string.insight_expense_pattern_metric)
    
    val insightActiveWeekday: String get() = getString(R.string.insight_active_weekday)
    val insightActiveWeekdayDesc: String get() = getString(R.string.insight_active_weekday_desc)
    val insightActiveWeekdayMetric: String get() = getString(R.string.insight_active_weekday_metric)
    
    // Методы с параметрами для insight
    fun insightFinancialHealthMetric(savingsRate: Int): String = getString(R.string.insight_financial_health_metric, savingsRate)
    fun insightFinancialProtectionMetric(months: Int): String = getString(R.string.insight_financial_protection_metric, months)
    fun insightExpenseFrequencyDesc(totalTransactions: Int): String = getString(R.string.insight_expense_frequency_desc, totalTransactions)
    fun insightExpenseFrequencyMetric(totalTransactions: Int): String = getString(R.string.insight_expense_frequency_metric, totalTransactions)
    fun insightExpensePatternDesc(day: String): String = getString(R.string.insight_expense_pattern_desc, day)
    fun insightExpensePatternMetric(day: String): String = getString(R.string.insight_expense_pattern_metric, day)
    fun insightActiveWeekdayDesc(day: String): String = getString(R.string.insight_active_weekday_desc, day)
    fun insightActiveWeekdayMetric(day: String): String = getString(R.string.insight_active_weekday_metric, day)
    
    // Дополнительные строки для insight
    val insightSmallExpenses: String get() = getString(R.string.insight_small_expenses)
    val insightSmallExpensesDesc: String get() = getString(R.string.insight_small_expenses_desc)
    val insightSmallExpensesMetric: String get() = getString(R.string.insight_small_expenses_metric)
    
    val insightLargeExpenses: String get() = getString(R.string.insight_large_expenses)
    val insightLargeExpensesDesc: String get() = getString(R.string.insight_large_expenses_desc)
    val insightLargeExpensesMetric: String get() = getString(R.string.insight_large_expenses_metric)
    
    val insightHighActivity: String get() = getString(R.string.insight_high_activity)
    val insightHighActivityDesc: String get() = getString(R.string.insight_high_activity_desc)
    val insightHighActivityMetric: String get() = getString(R.string.insight_high_activity_metric)
    
    // Методы с параметрами для insight
    fun insightSmallExpensesMetric(amount: Int): String = getString(R.string.insight_small_expenses_metric, amount)
    fun insightLargeExpensesMetric(amount: Int): String = getString(R.string.insight_large_expenses_metric, amount)
    fun insightHighActivityDesc(count: Int): String = getString(R.string.insight_high_activity_desc, count)
    fun insightHighActivityMetric(count: Int): String = getString(R.string.insight_high_activity_metric, count)
} 