package com.davidbugayov.financeanalyzer.feature.budget.util

import android.content.Context
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Утилитный класс для получения строковых ресурсов в модуле budget
 */
object StringProvider {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun getString(
        @androidx.annotation.StringRes resId: Int,
    ): String {
        return context?.getString(resId) ?: "String not found"
    }

    fun getString(
        @androidx.annotation.StringRes resId: Int,
        vararg formatArgs: Any,
    ): String {
        return context?.getString(resId, *formatArgs) ?: "String not found"
    }

    // Импорт категорий
    val importCategoriesTitle: String get() = getString(R.string.import_categories_title)
    val importCategoriesSubtitle: String get() = getString(R.string.import_categories_subtitle)
    val importCategoriesConfirm: String get() = getString(R.string.import_categories_confirm)
    val cancel: String get() = getString(R.string.cancel)

    // Ошибки кошельков
    val errorWalletNotFound: String get() = getString(R.string.error_wallet_not_found)
    val errorLoadingWallet: String get() = getString(R.string.error_loading_wallet)
    val errorLoadingTransactions: String get() = getString(R.string.error_loading_transactions)

    // Валидация кошельков
    val errorEnterWalletName: String get() = getString(R.string.error_enter_wallet_name)
    val errorEnterTargetAmount: String get() = getString(R.string.error_enter_target_amount)
    val errorTargetAmountPositive: String get() = getString(R.string.error_target_amount_positive)
    val errorEnterValidAmount: String get() = getString(R.string.error_enter_valid_amount)

    fun errorCreatingWallet(message: String): String = getString(R.string.error_creating_wallet, message)

    // Бюджет
    val budget: String get() = getString(R.string.budget)
    val add: String get() = getString(R.string.add)

    // Ошибки
    val error: String get() = getString(R.string.error)

    val errorLinkingCategories: String get() = getString(R.string.error_linking_categories)

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
