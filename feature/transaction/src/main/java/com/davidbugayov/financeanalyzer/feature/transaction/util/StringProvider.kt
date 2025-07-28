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
    
    fun getString(@androidx.annotation.StringRes resId: Int): String {
        return context?.getString(resId) ?: "String not found"
    }
    
    fun getString(@androidx.annotation.StringRes resId: Int, vararg formatArgs: Any): String {
        return context?.getString(resId, *formatArgs) ?: "String not found"
    }
    
    // Логирование транзакций
    fun logTransactionWalletsLoaded(count: Int): String = getString(R.string.log_transaction_wallets_loaded, count)
    val logErrorLoadingWallets: String get() = getString(R.string.log_error_loading_wallets)
    fun logTransactionValidateInput(amount: String): String = getString(R.string.log_transaction_validate_input, amount)
    val logTransactionEmptyAmountError: String get() = getString(R.string.log_transaction_empty_amount_error)
    fun logTransactionZeroAmountError(amount: Float): String = getString(R.string.log_transaction_zero_amount_error, amount)
    fun logTransactionParseAmountError(message: String): String = getString(R.string.log_transaction_parse_amount_error, message)
    val logTransactionEmptyCategoryError: String get() = getString(R.string.log_transaction_empty_category_error)
    fun logTransactionValidationResult(isValid: Boolean, hasAmountError: Boolean): String = getString(R.string.log_transaction_validation_result, isValid, hasAmountError)
    fun logTransactionInitializeScreen(forceExpense: Boolean, isExpense: Boolean): String = getString(R.string.log_transaction_initialize_screen, forceExpense, isExpense)
    
    // Ошибки валидации
    val errorEmptyAmount: String get() = getString(R.string.error_empty_amount)
    fun errorZeroAmount(amount: String): String = getString(R.string.error_zero_amount, amount)
    val errorEmptyCategory: String get() = getString(R.string.error_empty_category)
    val errorUnknown: String get() = getString(R.string.error_unknown)
    
    // Импорт CSV
    val csvFileEmpty: String get() = getString(R.string.csv_file_empty)
    fun logCsvFormatCheck(firstLine: String, delimiter: Char, isValid: Boolean): String = getString(R.string.csv_format_check, firstLine, delimiter, isValid)
    fun logCsvHeaderSkipped(headerLine: String?): String = getString(R.string.csv_header_skipped, headerLine ?: "")
    val logCsvNoHeader: String get() = getString(R.string.csv_no_header)
    fun logCsvParsingLine(line: String): String = getString(R.string.csv_parsing_line, line)
    val csvDateFormat: String get() = getString(R.string.csv_date_format)
    fun logCsvDelimiterUsed(bankName: String, delimiter: String, columns: Int): String = getString(R.string.log_csv_delimiter_used, bankName, delimiter, columns)
    fun logCsvDateNotFound(bankName: String, columnIndex: Int, dateString: String): String = getString(R.string.log_csv_date_not_found, bankName, columnIndex, dateString)
    fun logCsvDateFound(bankName: String, columnIndex: Int, dateString: String): String = getString(R.string.log_csv_date_found, bankName, columnIndex, dateString)
    fun logCsvAmountProcessed(bankName: String, amountString: String, currencyString: String): String = getString(R.string.log_csv_amount_processed, bankName, amountString, currencyString)
    fun logCsvDateParsed(bankName: String, dateString: String, format: String): String = getString(R.string.log_csv_date_parsed, bankName, dateString, format)
    val errorDateParseFailed: String get() = getString(R.string.error_date_parse_failed)
    
    // Базовый ViewModel
    val logErrorUpdatingWalletBalance: String get() = getString(R.string.log_error_updating_wallet_balance)
    fun logSavingExpenseWallets(count: Int): String = getString(R.string.log_saving_expense_wallets, count)
    fun logSavingIncomeWallets(count: Int): String = getString(R.string.log_saving_income_wallets, count)
    fun logNotSavingWallets(isExpense: Boolean, addToWallet: Boolean, selectedWallets: Int): String = getString(R.string.log_not_saving_wallets, isExpense, addToWallet, selectedWallets)
    fun logWalletsLoaded(count: Int): String = getString(R.string.log_wallets_loaded, count)
    val logErrorLoadingWalletsBase: String get() = getString(R.string.log_error_loading_wallets_base)
    fun logEnablingExpenseWallet(walletId: String): String = getString(R.string.log_enabling_expense_wallet, walletId)
    val logNoWalletsForExpense: String get() = getString(R.string.log_no_wallets_for_expense)
    fun logEnablingIncomeWallets(count: Int): String = getString(R.string.log_enabling_income_wallets, count)
    val logDisablingWallets: String get() = getString(R.string.log_disabling_wallets)
    fun logSelectWalletEvent(walletId: String, selected: Boolean): String = getString(R.string.log_select_wallet_event, walletId, selected)
    fun logUpdatingSelectedWallets(was: Int, became: Int): String = getString(R.string.log_updating_selected_wallets, was, became)
    fun logTransactionTypeSwitch(from: Boolean, to: Boolean): String = getString(R.string.log_transaction_type_switch, from, to)
    fun logTransactionAfterSwitch(isExpense: Boolean, category: String): String = getString(R.string.log_transaction_after_switch, isExpense, category)
    val logTransactionResetFields: String get() = getString(R.string.log_transaction_reset_fields)
    fun logExpenseCategoriesSorted(categories: String): String = getString(R.string.log_expense_categories_sorted, categories)
    fun logIncomeCategoriesSorted(categories: String): String = getString(R.string.log_income_categories_sorted, categories)
    val logCategoryPositionsUpdated: String get() = getString(R.string.log_category_positions_updated)
    fun logErrorLoadingSources(message: String): String = getString(R.string.log_error_loading_sources, message)
    val logParseMoneyEmpty: String get() = getString(R.string.log_parse_money_empty)
    fun logParseMoneyExpression(original: String, processed: String): String = getString(R.string.log_parse_money_expression, original, processed)
    fun logParseMoneyRemoveOperator(expression: String): String = getString(R.string.log_parse_money_remove_operator, expression)
    val logParseMoneyEmptyAfterClean: String get() = getString(R.string.log_parse_money_empty_after_clean)
    fun logParseMoneySimpleNumber(result: String): String = getString(R.string.log_parse_money_simple_number, result)
    fun logParseMoneyNumberError(expression: String): String = getString(R.string.log_parse_money_number_error, expression)
    fun logParseMoneyInvalidChars(cleaned: String): String = getString(R.string.log_parse_money_invalid_chars, cleaned)
    fun logParseMoneyExpressionSuccess(result: String): String = getString(R.string.log_parse_money_expression_success, result)
    fun logParseMoneyExpressionError(expression: String): String = getString(R.string.log_parse_money_expression_error, expression)
    fun logParseMoneyUnrecognized(expression: String): String = getString(R.string.log_parse_money_unrecognized, expression)
    val logSourcePositionsUpdated: String get() = getString(R.string.log_source_positions_updated)
    fun logCategoryExpenseUsageIncreased(category: String): String = getString(R.string.log_category_expense_usage_increased, category)
    fun logCategoryIncomeUsageIncreased(category: String): String = getString(R.string.log_category_income_usage_increased, category)
    fun logCategoryUsageCount(category: String, count: Int): String = getString(R.string.log_category_usage_count, category, count)
    fun logSourceUsageIncreased(source: String): String = getString(R.string.log_source_usage_increased, source)
    fun logSourceUsageCount(source: String, count: Int): String = getString(R.string.log_source_usage_count, source, count)
    
    // Защищенные категории и источники
    val categoryOther: String get() = getString(R.string.category_other)
    val categoryTransfers: String get() = getString(R.string.category_transfers)
    val sourceCash: String get() = getString(R.string.source_cash)
    val sourceCard: String get() = getString(R.string.source_card)
    
    // Банки
    val bankSberbankPdf: String get() = getString(R.string.bank_sberbank_pdf)
    val bankOzon: String get() = getString(R.string.bank_ozon)
    fun logSberbankIndicatorFound(bankName: String): String = getString(R.string.log_sberbank_indicator_found, bankName)
    fun logSberbankReadError(bankName: String, message: String): String = getString(R.string.log_sberbank_read_error, bankName, message)
    fun logSberbankImportCreated(bankName: String): String = getString(R.string.log_sberbank_import_created, bankName)
    fun logOzonImportCreated(bankName: String): String = getString(R.string.log_ozon_import_created, bankName)
    fun errorUnsupportedFileType(bankName: String, fileType: String): String = getString(R.string.error_unsupported_file_type, bankName, fileType)
    
    // Сбербанк PDF
    val sberbankCardPattern: String get() = getString(R.string.sberbank_card_pattern)
    val sberbankName: String get() = getString(R.string.sberbank_name)
    val sberbankStatement: String get() = getString(R.string.sberbank_statement)
    val sberbankStatementAlt: String get() = getString(R.string.sberbank_statement_alt)
    val sberbankOperationsDecoding: String get() = getString(R.string.sberbank_operations_decoding)
    val sberbankDateOperation: String get() = getString(R.string.sberbank_date_operation)
    val sberbankCategory: String get() = getString(R.string.sberbank_category)
    val sberbankHeaders: String get() = getString(R.string.sberbank_headers)
    
    // UI строки
    val editTransactionTitle: String get() = getString(R.string.edit_transaction_title)
    val addTransaction: String get() = getString(R.string.add_transaction)
    val saveButtonText: String get() = getString(R.string.save_button_text)
    val addButtonText: String get() = getString(R.string.add_button_text)
    val addButton: String get() = getString(R.string.add_button)
    val selectCategory: String get() = getString(R.string.select_category)
    val addCustomCategory: String get() = getString(R.string.add_custom_category)
    val addCustomSource: String get() = getString(R.string.add_custom_source)
    val selectColor: String get() = getString(R.string.select_color)
    val selectDateButton: String get() = getString(R.string.select_date_button)
    val noteOptional: String get() = getString(R.string.note_optional)
    val comment: String get() = getString(R.string.comment)
    val selectSource: String get() = getString(R.string.select_source)
    val deleteSource: String get() = getString(R.string.delete_source)
    val source: String get() = getString(R.string.source)
    val incomeType: String get() = getString(R.string.income_type)
    val expenseType: String get() = getString(R.string.expense_type)
    val category: String get() = getString(R.string.category)
    val sourceName: String get() = getString(R.string.source_name)
    val date: String get() = getString(R.string.date)
    val categoryName: String get() = getString(R.string.category_name)
    val addCategory: String get() = getString(R.string.add_category)
    val selectIcon: String get() = getString(R.string.select_icon)
    val categoryTransfer: String get() = getString(R.string.category_transfer)
    val cancel: String get() = getString(UiR.string.cancel)
    val close: String get() = getString(UiR.string.close)
    val showMoreCategories: String get() = getString(R.string.show_more_categories)
    val hideCategories: String get() = getString(R.string.hide_categories)
    
    // Кошельки
    val deductFromWallets: String get() = getString(R.string.deduct_from_wallets)
    val addToWallets: String get() = getString(R.string.add_to_wallets)
    val walletSelectedForExpense: String get() = getString(R.string.wallet_selected_for_expense)
    val walletSelectedForIncome: String get() = getString(R.string.wallet_selected_for_income)
    val walletExpenseExplanation: String get() = getString(R.string.wallet_expense_explanation)
    val walletIncomeExplanation: String get() = getString(R.string.wallet_income_explanation)
    val selectWalletForExpense: String get() = getString(R.string.select_wallet_for_expense)
    val selectWalletsForIncome: String get() = getString(R.string.select_wallets_for_income)
    val addToWallet: String get() = getString(R.string.add_to_wallets)
    val selectWallets: String get() = getString(R.string.select_wallets)
    val selectWalletsContentDescription: String get() = getString(R.string.select_wallets_content_description)
    
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
    val chooseFileButton: String get() = getString(R.string.choose_file_button)
    val howToImport: String get() = getString(R.string.how_to_import)
    val importInstructions: String get() = getString(R.string.import_instructions)
    val importResults: String get() = getString(R.string.import_results)
    val importingFile: String get() = getString(R.string.importing_file)
    val supportedBanksTitle: String get() = getString(R.string.supported_banks_title)
    val importTransactionsContentDescription: String get() = getString(R.string.import_transactions_content_description)
    
    // Методы с параметрами
    fun walletsSelectedForExpense(count: Int): String = getString(R.string.wallets_selected_for_expense, count)
    fun walletsSelectedForIncome(count: Int): String = getString(R.string.wallets_selected_for_income, count)
    fun selectedWalletCount(count: Int): String = getString(R.string.selected_wallet_count, count)
    fun selectedWalletsCount(count: Int): String = getString(R.string.selected_wallets_count, count)
    fun deleteCategoryConfirmation(category: String): String = getString(R.string.delete_category_confirmation, category)
    fun deleteSourceConfirmation(source: String): String = getString(R.string.delete_source_confirmation, source)
} 