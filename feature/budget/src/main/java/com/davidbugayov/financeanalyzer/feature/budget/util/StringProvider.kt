package com.davidbugayov.financeanalyzer.feature.budget.util

import android.content.Context
import com.davidbugayov.financeanalyzer.feature.budget.R
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Утилитный класс для получения строковых ресурсов в модуле budget
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
    
    // Импорт категорий
    val importCategoriesTitle: String get() = getString(R.string.import_categories_title)
    val importCategoriesSubtitle: String get() = getString(R.string.import_categories_subtitle)
    val importCategoriesConfirm: String get() = getString(R.string.import_categories_confirm)
    val cancel: String get() = getString(UiR.string.cancel)
    
    // Ошибки кошельков
    val errorWalletNotFound: String get() = getString(R.string.error_wallet_not_found)
    val errorLoadingWallet: String get() = getString(R.string.error_loading_wallet)
    val errorLoadingTransactions: String get() = getString(R.string.error_loading_transactions)
    val errorBindingCategories: String get() = getString(R.string.error_binding_categories)
    
    // Валидация кошельков
    val errorEnterWalletName: String get() = getString(R.string.error_enter_wallet_name)
    val errorEnterTargetAmount: String get() = getString(R.string.error_enter_target_amount)
    val errorTargetAmountPositive: String get() = getString(R.string.error_target_amount_positive)
    val errorEnterValidAmount: String get() = getString(R.string.error_enter_valid_amount)
    fun errorCreatingWallet(message: String): String = getString(R.string.error_creating_wallet, message)
    
    // Подкошельки
    val subwallets: String get() = getString(R.string.subwallets)
    val back: String get() = getString(UiR.string.back)
    val addSubwallet: String get() = getString(R.string.add_subwallet)
    
    // Бюджет
    val budget: String get() = getString(R.string.budget)
    val addIncome: String get() = getString(R.string.add_income)
    val periodSettings: String get() = getString(R.string.period_settings)
    val myWallets: String get() = getString(R.string.my_wallets)
    val addNewWallet: String get() = getString(R.string.add_new_wallet)
    val walletName: String get() = getString(R.string.wallet_name)
    val expenseLimit: String get() = getString(R.string.expense_limit)
    val add: String get() = getString(R.string.add)
    
    // Распределение дохода
    val distributeIncome: String get() = getString(R.string.distribute_income)
    val distributeIncomeDescription: String get() = getString(R.string.distribute_income_description)
    val incomeAmount: String get() = getString(R.string.income_amount)
    val distribute: String get() = getString(R.string.distribute)
    val noWalletsAvailable: String get() = getString(R.string.no_wallets_available)
    val addWithoutDistribution: String get() = getString(R.string.add_without_distribution)
    
    // Траты
    val spendFromWallet: String get() = getString(R.string.spend_from_wallet)
    fun categoryLabel(category: String): String = getString(R.string.category_label, category)
    val amount: String get() = getString(R.string.amount)
    val spend: String get() = getString(R.string.spend)
    
    // Переводы
    val transferBetweenWallets: String get() = getString(R.string.transfer_between_wallets)
    fun fromCategory(category: String): String = getString(R.string.from_category, category)
    val toCategory: String get() = getString(R.string.to_category)
    fun selectedWallet(wallet: String): String = getString(R.string.selected_wallet, wallet)
    val transferAmount: String get() = getString(R.string.transfer_amount)
    val transfer: String get() = getString(R.string.transfer)
    
    // Настройки периода
    val periodSettingsTitle: String get() = getString(R.string.period_settings_title)
    val periodDurationDescription: String get() = getString(R.string.period_duration_description)
    val daysCount: String get() = getString(R.string.days_count)
    val save: String get() = getString(R.string.save)
    
    // Ошибки
    val error: String get() = getString(UiR.string.error)
    val ok: String get() = getString(UiR.string.ok)
    val errorDistributingIncome: String get() = getString(R.string.error_distributing_income)
    val errorAddingFunds: String get() = getString(R.string.error_adding_funds)
    val errorInsufficientFunds: String get() = getString(R.string.error_insufficient_funds)
    val errorSpendingFunds: String get() = getString(R.string.error_spending_funds)
    val errorInsufficientSourceFunds: String get() = getString(R.string.error_insufficient_source_funds)
    val errorTransferringFunds: String get() = getString(R.string.error_transferring_funds)
    val errorSettingPeriodDuration: String get() = getString(R.string.error_setting_period_duration)
    val errorResettingPeriod: String get() = getString(R.string.error_resetting_period)
    val errorResettingAllPeriods: String get() = getString(R.string.error_resetting_all_periods)
    
    // Превышение бюджета
    val budgetOverLimitTitle: String get() = getString(R.string.budget_over_limit_title)
    fun budgetOverLimitMessage(wallets: String): String = getString(R.string.budget_over_limit_message, wallets)
    
    // Кошельки
    val walletNotFound: String get() = getString(R.string.wallet_not_found)
    val linkCategories: String get() = getString(R.string.link_categories)
    val loading: String get() = getString(R.string.loading)
    val linkedCategories: String get() = getString(R.string.linked_categories)
    val transactionsSection: String get() = getString(R.string.transactions_section)
    val noTransactionsFound: String get() = getString(R.string.no_transactions_found)
    val linkCategoriesTitle: String get() = getString(R.string.link_categories_title)
    val linkCategoriesSubtitle: String get() = getString(R.string.link_categories_subtitle)
    val linkButton: String get() = getString(R.string.link_button)
    val errorTitle: String get() = getString(R.string.error_title)
    val okButton: String get() = getString(R.string.ok_button)
} 