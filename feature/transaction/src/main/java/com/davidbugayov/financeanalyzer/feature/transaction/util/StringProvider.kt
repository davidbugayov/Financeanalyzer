package com.davidbugayov.financeanalyzer.feature.transaction.util

import android.content.Context
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Утилитный класс для получения строковых ресурсов в модуле transaction
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

    // Логирование транзакций
    fun logTransactionWalletsLoaded(count: Int): String = getString(R.string.log_transaction_wallets_loaded, count)

    val logErrorLoadingWallets: String get() = getString(R.string.log_error_loading_wallets)

    fun logTransactionValidateInput(amount: String): String = getString(R.string.log_transaction_validate_input, amount)

    val logTransactionEmptyAmountError: String get() = getString(R.string.log_transaction_empty_amount_error)

    fun logTransactionZeroAmountError(amount: Float): String =
        getString(
            R.string.log_transaction_zero_amount_error,
            amount,
        )

    fun logTransactionParseAmountError(message: String): String =
        getString(
            R.string.log_transaction_parse_amount_error,
            message,
        )

    val logTransactionEmptyCategoryError: String get() = getString(R.string.log_transaction_empty_category_error)

    fun logTransactionValidationResult(
        isValid: Boolean,
        hasAmountError: Boolean,
    ): String =
        getString(
            R.string.log_transaction_validation_result,
            isValid,
            hasAmountError,
        )

    fun logTransactionInitializeScreen(
        forceExpense: Boolean,
        isExpense: Boolean,
    ): String =
        getString(
            R.string.log_transaction_initialize_screen,
            forceExpense,
            isExpense,
        )

    // Ошибки валидации
    val errorEmptyAmount: String get() = getString(R.string.error_empty_amount)

    fun errorZeroAmount(amount: String): String = getString(R.string.error_zero_amount, amount)

    val errorEmptyCategory: String get() = getString(R.string.error_empty_category)

    // Импорт CSV
    val csvFileEmpty: String get() = getString(R.string.csv_file_empty)

    fun logCsvFormatCheck(
        firstLine: String,
        delimiter: Char,
        isValid: Boolean,
    ): String =
        getString(
            R.string.csv_format_check,
            firstLine,
            delimiter,
            isValid,
        )

    fun logCsvHeaderSkipped(headerLine: String?): String = getString(R.string.csv_header_skipped, headerLine ?: "")

    val logCsvNoHeader: String get() = getString(R.string.csv_no_header)

    fun logCsvParsingLine(line: String): String = getString(R.string.csv_parsing_line, line)

    // Защищенные категории и источники
    val categoryOther: String get() = getString(R.string.category_other)
    // UI строки
    val editTransactionTitle: String get() = getString(R.string.edit_transaction_title)
    val addTransaction: String get() = getString(R.string.add_transaction)
    val saveButtonText: String get() = getString(R.string.save_button_text)
    val addButtonText: String get() = getString(R.string.add_button_text)

    val source: String get() = getString(R.string.source)
    val category: String get() = getString(R.string.category)
    val date: String get() = getString(R.string.date)
    val categoryTransfer: String get() = getString(R.string.category_transfer)
    val cancel: String get() = getString(UiR.string.cancel)
    val close: String get() = getString(UiR.string.close)

    val addToWallet: String get() = getString(R.string.add_to_wallets)
    // Диалоги
    val errorTitle: String get() = getString(R.string.error_title)
    val unknownErrorMessage: String get() = getString(UiR.string.unknown_error_message)
    val dialogOk: String get() = getString(UiR.string.dialog_ok)
    val attentionTitle: String get() = getString(R.string.attention_title)
    val unsavedDataWarning: String get() = getString(R.string.unsaved_data_warning)
    val proceedToImport: String get() = getString(R.string.proceed_to_import)
    val dialogCancel: String get() = getString(R.string.dialog_cancel)
    val transactionSavedSuccess: String get() = getString(R.string.transaction_saved_success)
    val deleteCategoryTitle: String get() = getString(R.string.delete_category_title)
    val dialogDelete: String get() = getString(R.string.dialog_delete)
    val deleteSourceTitle: String get() = getString(R.string.delete_source_title)
    val delete: String get() = getString(UiR.string.delete)

    // Импорт
    val importTransactionsTitle: String get() = getString(R.string.import_transactions_title)
    val importTransactionsHint: String get() = getString(R.string.import_transactions_hint)
    val importButton: String get() = getString(R.string.import_button)

    val importTransactionsContentDescription: String get() = getString(R.string.import_transactions_content_description)

    fun deleteCategoryConfirmation(category: String): String =
        getString(
            R.string.delete_category_confirmation,
            category,
        )

    fun deleteSourceConfirmation(source: String): String = getString(R.string.delete_source_confirmation, source)
}
