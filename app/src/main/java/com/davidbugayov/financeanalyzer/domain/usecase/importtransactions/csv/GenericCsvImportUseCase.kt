package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.csv

import android.content.Context
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.category.TransactionCategoryDetector
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.BankImportUseCase
import timber.log.Timber
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Конфигурация для парсинга CSV-файлов.
 * Позволяет гибко настраивать структуру и правила обработки CSV для разных банков и форматов.
 */
data class CsvParseConfig(
    val delimiter: Char = ',', // Разделитель значений (например, ',' или ';')
    val dateFormatString: String = "yyyy-MM-dd", // Формат даты (например, "dd.MM.yyyy")
    val locale: Locale = Locale.getDefault(), // Локаль для парсинга дат
    val hasHeader: Boolean = true, // Есть ли строка заголовка
    val dateColumnIndex: Int = 0, // Индекс колонки с датой
    val descriptionColumnIndex: Int = 1, // Индекс колонки с описанием
    val amountColumnIndex: Int = 2, // Индекс колонки с суммой
    val currencyColumnIndex: Int? = 3, // Индекс колонки с валютой (опционально)
    val defaultCurrencyCode: String = "USD", // Валюта по умолчанию
    val isExpenseColumnIndex: Int? = null, // Индекс колонки, явно указывающей расход (опционально)
    val isExpenseTrueValue: String = "true", // Значение, означающее расход (если колонка есть)
    val expectedMinColumnCount: Int = 3, // Минимальное количество колонок для валидной строки
    val amountDecimalSeparator: Char = '.', // Разделитель дробной части в сумме
    val amountCharsToRemoveRegexPattern: String? = null, // Регулярка для удаления лишних символов из суммы
    val statusColumnIndex: Int? = null, // Индекс колонки со статусом транзакции (опционально)
    val validStatusValues: List<String>? = null, // Список валидных статусов (если колонка есть)
    val skipTransactionIfStatusInvalid: Boolean = true, // Пропускать ли строки с невалидным статусом
) {

    // Регулярка для очистки суммы
    val amountCharsToRemoveRegex: Regex? by lazy {
        amountCharsToRemoveRegexPattern?.let { Regex(it) }
    }

    // Форматтер даты
    val dateFormat: SimpleDateFormat by lazy {
        SimpleDateFormat(dateFormatString, locale)
    }
}

/**
 * Универсальный класс для импорта транзакций из CSV-файлов.
 * Гибко настраивается через CsvParseConfig.
 */
class GenericCsvImportUseCase(
    context: Context,
    transactionRepository: TransactionRepository,
    private val config: CsvParseConfig = CsvParseConfig(),
) : BankImportUseCase(transactionRepository, context) {

    override val bankName: String = context.getString(R.string.bank_generic_csv)

    /**
     * Проверяет, подходит ли файл под ожидаемый CSV-формат (по первой строке).
     */
    override fun isValidFormat(reader: BufferedReader): Boolean {
        reader.mark(4096)
        val firstLine = reader.readLine()
        reader.reset()
        if (firstLine == null) {
            Timber.w("[$bankName] ${context.getString(R.string.csv_file_empty)}")
            return false
        }
        val columns = firstLine.split(config.delimiter)
        val isValid = columns.isNotEmpty() && columns.size >= config.expectedMinColumnCount
        Timber.d(
            "[$bankName] ${context.getString(
                R.string.csv_format_check,
                firstLine,
                config.delimiter,
                isValid,
            )}",
        )
        return isValid
    }

    /**
     * Пропускает строку заголовка, если она есть (согласно конфигу).
     */
    override fun skipHeaders(reader: BufferedReader) {
        if (config.hasHeader) {
            val headerLine = reader.readLine()
            Timber.d("[$bankName] ${context.getString(R.string.csv_header_skipped, headerLine)}")
        } else {
            Timber.d("[$bankName] ${context.getString(R.string.csv_no_header)}")
        }
    }

    /**
     * Парсит строку CSV в объект Transaction.
     * Возвращает null, если строка невалидна или не содержит транзакцию.
     */
    override fun parseLine(line: String): Transaction? {
        Timber.d("[$bankName] ${context.getString(R.string.csv_parsing_line, line)}")
        val columns = line.split(config.delimiter).map { it.trim().removeSurrounding("\"") }
        if (columns.size < config.expectedMinColumnCount) {
            Timber.w(
                "[$bankName] ${context.getString(
                    R.string.csv_not_enough_columns,
                    config.expectedMinColumnCount,
                    columns.size,
                    line,
                )}",
            )
            return null
        }
        if (config.skipTransactionIfStatusInvalid && config.statusColumnIndex != null && config.validStatusValues?.isNotEmpty() == true) {
            val status = columns.getOrNull(config.statusColumnIndex)
            if (status == null || config.validStatusValues.none {
                    it.equals(
                        status,
                        ignoreCase = true,
                    )
                }
            ) {
                Timber.d(
                    "[$bankName] ${context.getString(
                        R.string.csv_skip_invalid_status,
                        status,
                        config.validStatusValues,
                        line,
                    )}",
                )
                return null
            }
        }
        try {
            val dateString = columns.getOrNull(config.dateColumnIndex) ?: run {
                Timber.e(
                    "[$bankName] ${context.getString(
                        R.string.csv_date_not_found,
                        config.dateColumnIndex,
                        line,
                    )}",
                )
                return null
            }
            val description = columns.getOrNull(config.descriptionColumnIndex) ?: context.getString(
                R.string.csv_no_description,
            )
            var amountString = columns.getOrNull(config.amountColumnIndex) ?: run {
                Timber.e(
                    "[$bankName] ${context.getString(
                        R.string.csv_amount_not_found,
                        config.amountColumnIndex,
                        line,
                    )}",
                )
                return null
            }
            config.amountCharsToRemoveRegex?.let { regex ->
                amountString = regex.replace(amountString, "")
            } ?: run {
                amountString = amountString.replace("[^0-9.,\\-\\s]".toRegex(), "")
            }
            amountString = amountString.replace("\\s".toRegex(), "")
            if (config.amountDecimalSeparator != '.') {
                amountString = amountString.replace(config.amountDecimalSeparator, '.')
            }
            val currencyString = config.currencyColumnIndex?.let { index ->
                columns.getOrNull(index)?.takeIf { it.isNotBlank() }
            } ?: config.defaultCurrencyCode
            val transactionDate = try {
                config.dateFormat.parse(dateString)
            } catch (e: Exception) {
                Timber.e(
                    e,
                    "[$bankName] ${context.getString(
                        R.string.csv_date_parse_error,
                        dateString,
                        config.dateFormat.toPattern(),
                        line,
                    )}",
                )
                return null
            }
            val amountValue = try {
                amountString.toDoubleOrNull() ?: run {
                    Timber.e(
                        "[$bankName] ${context.getString(
                            R.string.csv_amount_parse_error,
                            amountString,
                            line,
                        )}",
                    )
                    return null
                }
            } catch (e: NumberFormatException) {
                Timber.e(
                    e,
                    "[$bankName] ${context.getString(
                        R.string.csv_amount_parse_exception,
                        amountString,
                        e.message,
                    )}",
                )
                return null
            }
            val isExpense = if (config.isExpenseColumnIndex != null) {
                val expenseValue = columns.getOrNull(config.isExpenseColumnIndex)
                expenseValue?.equals(config.isExpenseTrueValue, ignoreCase = true) == true
            } else {
                columns.getOrNull(4)?.equals(
                    context.getString(R.string.csv_expense_value),
                    ignoreCase = true,
                ) ?: (amountValue < 0)
            }
            val absAmount = kotlin.math.abs(amountValue)
            val currency = Currency.fromCode(currencyString.uppercase(Locale.ROOT))
            val money = Money(absAmount, currency)
            val category = TransactionCategoryDetector.detect(description)
            return Transaction(
                amount = money,
                category = category,
                date = transactionDate,
                isExpense = isExpense,
                note = context.getString(R.string.csv_imported_note),
                source = bankName,
                sourceColor = 0,
                categoryId = "",
                title = description,
            )
        } catch (e: Exception) {
            Timber.e(
                e,
                "[$bankName] ${context.getString(R.string.csv_parse_line_error, line, config)}",
            )
            return null
        }
    }

    /**
     * Определяет, нужно ли пропустить строку (например, если она пустая или не содержит достаточно данных).
     */
    override fun shouldSkipLine(line: String): Boolean {
        if (super.shouldSkipLine(line)) return true
        if (line.split(config.delimiter).size < config.expectedMinColumnCount) {
            Timber.d(
                "[$bankName] ${context.getString(R.string.csv_skip_line_not_enough_columns, line)}",
            )
            return true
        }
        return false
    }

    // importTransactions(uri, progressCallback) не переопределяем — базовая реализация BankImportUseCase подходит для CSV
}
