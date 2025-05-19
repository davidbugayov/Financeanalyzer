package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.csv

import android.content.Context
import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.BankImportUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.TransactionCategoryDetector
import timber.log.Timber
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Configuration for parsing CSV files.
 *
 * @param delimiter The character used to separate values in the CSV.
 * @param dateFormatString The date format pattern (e.g., "yyyy-MM-dd").
 * @param locale The locale to use for parsing dates.
 * @param hasHeader True if the CSV file has a header row to be skipped, false otherwise.
 * @param dateColumnIndex The 0-based index of the column containing the transaction date.
 * @param descriptionColumnIndex The 0-based index of the column containing the transaction description.
 * @param amountColumnIndex The 0-based index of the column containing the transaction amount.
 * @param currencyColumnIndex The 0-based index of the column containing the currency code (optional). If null, a default or inference might be used.
 * @param defaultCurrencyCode The currency code to use if `currencyColumnIndex` is null or the column is empty.
 * @param isExpenseColumnIndex The 0-based index of a column that explicitly states if a transaction is an expense (optional).
 *                             If null, expense status is inferred from the sign of the amount.
 * @param isExpenseTrueValue The string value in the `isExpenseColumnIndex` that indicates an expense (e.g., "true", "DR", "EXPENSE"). Ignored if `isExpenseColumnIndex` is null. Case-insensitive.
 * @param expectedMinColumnCount The minimum number of columns expected for a valid transaction line.
 * @param amountDecimalSeparator The character used as a decimal separator in amount strings (e.g., '.' or ',').
 * @param amountCharsToRemoveRegexPattern A regex pattern for characters to remove from amount strings before parsing (e.g., currency symbols, thousand separators).
 * @param statusColumnIndex Optional 0-based index of the column containing transaction status (e.g., "OK", "PENDING").
 * @param validStatusValues Optional list of string values in the `statusColumnIndex` that indicate a transaction should be processed. Case-insensitive. Used if `statusColumnIndex` is not null.
 * @param skipTransactionIfStatusInvalid If true and `statusColumnIndex` and `validStatusValues` are set, transactions with statuses not in `validStatusValues` will be skipped.
 */
data class CsvParseConfig(
    val delimiter: Char = ',',
    val dateFormatString: String = "yyyy-MM-dd",
    val locale: Locale = Locale.getDefault(),
    val hasHeader: Boolean = true,
    val dateColumnIndex: Int = 0,
    val descriptionColumnIndex: Int = 1,
    val amountColumnIndex: Int = 2,
    val currencyColumnIndex: Int? = 3,
    val defaultCurrencyCode: String = "USD",
    val isExpenseColumnIndex: Int? = null,
    val isExpenseTrueValue: String = "true",
    val expectedMinColumnCount: Int = 3, // Assumes Date, Description, Amount are minimal
    val amountDecimalSeparator: Char = '.',
    val amountCharsToRemoveRegexPattern: String? = null, // e.g., "[\\s€$]"
    val statusColumnIndex: Int? = null,
    val validStatusValues: List<String>? = null,
    val skipTransactionIfStatusInvalid: Boolean = true
) {

    val amountCharsToRemoveRegex: Regex? by lazy {
        amountCharsToRemoveRegexPattern?.let { Regex(it) }
    }
    val dateFormat: SimpleDateFormat by lazy {
        SimpleDateFormat(dateFormatString, locale)
    }
}

/**
 * Generic CSV import use case. This UseCase attempts to parse a CSV file based on
 * a provided [CsvParseConfig].
 */
class GenericCsvImportUseCase(
    context: Context,
    transactionRepository: TransactionRepository,
    private val config: CsvParseConfig = CsvParseConfig() // Provide a default config
) : BankImportUseCase(transactionRepository, context) {

    override val bankName: String = "Generic CSV (Configurable)"

    override fun isValidFormat(reader: BufferedReader): Boolean {
        reader.mark(4096)
        val firstLine = reader.readLine()
        reader.reset()

        if (firstLine == null) {
            Timber.w("[$bankName] CSV file is empty.")
            return false
        }
        val columns = firstLine.split(config.delimiter)
        val isValid =
            columns.isNotEmpty() && columns.size >= config.expectedMinColumnCount - (if (config.currencyColumnIndex != null) 0 else 1) - (if (config.statusColumnIndex != null) 0 else 1)
        Timber.d("[$bankName] Generic CSV format validation. First line: ${firstLine}. Delimiter: '${config.delimiter}'. IsValid (basic check): $isValid")
        return isValid
    }

    override fun skipHeaders(reader: BufferedReader) {
        if (config.hasHeader) {
            val headerLine = reader.readLine()
            Timber.d("[$bankName] Skipped header line (as per config): $headerLine")
        } else {
            Timber.d("[$bankName] No header to skip (as per config).")
        }
    }

    override fun parseLine(line: String): Transaction? {
        Timber.d("[$bankName] Parsing line: $line")
        val columns = line.split(config.delimiter).map { it.trim().removeSurrounding("\"") }

        if (columns.size < config.expectedMinColumnCount) {
            Timber.w("[$bankName] Line does not have enough columns. Expected at least ${config.expectedMinColumnCount}, got ${columns.size}. Line: $line")
            return null
        }

        // Status Check
        if (config.skipTransactionIfStatusInvalid && config.statusColumnIndex != null && config.validStatusValues?.isNotEmpty() == true) {
            val status = columns.getOrNull(config.statusColumnIndex)
            if (status == null || config.validStatusValues.none { it.equals(status, ignoreCase = true) }) {
                Timber.d("[$bankName] Skipping transaction due to invalid status: '$status'. Valid statuses: ${config.validStatusValues}. Line: $line")
                return null
            }
        }

        try {
            val dateString = columns.getOrNull(config.dateColumnIndex) ?: run {
                Timber.e("[$bankName] Date string missing at configured index ${config.dateColumnIndex}. Line: $line")
                return null
            }
            val description = columns.getOrNull(config.descriptionColumnIndex) ?: "N/A"

            var amountString = columns.getOrNull(config.amountColumnIndex) ?: run {
                Timber.e("[$bankName] Amount string missing at configured index ${config.amountColumnIndex}. Line: $line")
                return null
            }

            config.amountCharsToRemoveRegex?.let { regex ->
                amountString = regex.replace(amountString, "")
            }
            if (config.amountDecimalSeparator != '.') { // Normalize to '.' for Double.parseDouble
                amountString = amountString.replace(config.amountDecimalSeparator, '.')
            }

            val currencyString = config.currencyColumnIndex?.let { index ->
                columns.getOrNull(index)?.takeIf { it.isNotBlank() }
            } ?: config.defaultCurrencyCode

            val transactionDate = config.dateFormat.parse(dateString) ?: run {
                Timber.e("[$bankName] Failed to parse date: '${dateString}' with format '${config.dateFormat.toPattern()}'. Line: ${line}")
                return null
            }
            val amountValue = amountString.toDoubleOrNull() ?: run {
                Timber.e("[$bankName] Failed to parse amount: '${amountString}' (after cleanup/normalization). Line: ${line}")
                return null
            }

            val isExpense: Boolean = config.isExpenseColumnIndex?.let { index ->
                columns.getOrNull(index)?.equals(config.isExpenseTrueValue, ignoreCase = true)
            } ?: (amountValue < 0) // Infer from amount sign if no specific column or value mismatch

            val absAmount = kotlin.math.abs(amountValue)

            val currency = Currency.fromCode(currencyString.uppercase(Locale.ROOT))
            val money = Money(absAmount, currency)

            val category = TransactionCategoryDetector.detect(description)

            // TODO: Source color and categoryId mapping could also be part of config or a separate mapping mechanism
            return Transaction(
                amount = money,
                category = category,
                date = transactionDate,
                isExpense = isExpense,
                note = "Imported from Generic CSV", // TODO: Maybe bankName from config or handler?
                source = bankName, // This will be "Generic CSV (Configurable)". Consider if this should come from Handler or a config field.
                sourceColor = 0,
                categoryId = "",
                title = description
            )
        } catch (e: Exception) {
            Timber.e(e, "[$bankName] Failed to parse transaction line: $line. Config: $config")
            return null
        }
    }

    override fun shouldSkipLine(line: String): Boolean {
        if (super.shouldSkipLine(line)) return true // Skips blank lines
        // Basic check: if line doesn't have enough parts after splitting by delimiter, skip.
        // This might be too aggressive if some lines are intentionally shorter (e.g. notes)
        // but for transaction data, it's a reasonable heuristic.
        if (line.split(config.delimiter).size < config.expectedMinColumnCount) {
            Timber.d("[$bankName] Skipping line due to insufficient columns based on delimiter '${config.delimiter}': $line")
            return true
        }
        // TODO: Add more (configurable?) conditions to skip specific non-transaction lines
        return false
    }

    // No need to override importTransactions(uri, progressCallback) as the
    // default implementation in BankImportUseCase (which uses processTransactionsFromReader)
    // is suitable for CSV files that can be read directly as text streams.
} 