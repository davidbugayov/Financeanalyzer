package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.csv

import android.content.Context
import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.category.TransactionCategoryDetector
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.BankImportUseCase
import com.davidbugayov.financeanalyzer.feature.transaction.R
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.core.context.GlobalContext
import timber.log.Timber

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
            val rp: ResourceProvider = GlobalContext.get().get()
            Timber.w(
                "[$bankName] ${rp.getString(
                    com.davidbugayov.financeanalyzer.feature.transaction.R.string.csv_file_empty,
                )}",
            )
            return false
        }
        val columns = firstLine.split(config.delimiter)
        val isValid = columns.isNotEmpty() && columns.size >= config.expectedMinColumnCount
        val rp: ResourceProvider = GlobalContext.get().get()
        Timber.d("[$bankName] ${rp.getString(R.string.csv_format_check, firstLine, config.delimiter, isValid)}")
        return isValid
    }

    /**
     * Пропускает строку заголовка, если она есть (согласно конфигу).
     */
    override fun skipHeaders(reader: BufferedReader) {
        if (config.hasHeader) {
            val headerLine = reader.readLine()
            val rp: ResourceProvider = GlobalContext.get().get()
            Timber.d("[$bankName] ${rp.getString(R.string.csv_header_skipped, headerLine)}")
        } else {
            val rp: ResourceProvider = GlobalContext.get().get()
            Timber.d("[$bankName] ${rp.getString(R.string.csv_no_header)}")
        }
    }

    /**
     * Парсит строку CSV в объект Transaction.
     * Возвращает null, если строка невалидна или не содержит транзакцию.
     */
    override fun parseLine(line: String): Transaction? {
        val rp: ResourceProvider = GlobalContext.get().get()
        Timber.d("[$bankName] ${rp.getString(R.string.csv_parsing_line, line)}")

        // Определяем разделитель, если строка не соответствует ожидаемому формату
        val actualDelimiter =
            if (line.contains(config.delimiter)) {
                config.delimiter
            } else if (line.contains(',')) {
                ','
            } else if (line.contains(';')) {
                ';'
            } else if (line.contains('\t')) {
                '\t'
            } else {
                config.delimiter // Используем настроенный разделитель по умолчанию
            }

        val columns = line.split(actualDelimiter).map { it.trim().removeSurrounding("\"") }

        // Логируем информацию о разделителе и количестве колонок
        Timber.d("[$bankName] Использован разделитель: '$actualDelimiter', найдено колонок: ${columns.size}")

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
            if (status == null ||
                config.validStatusValues.none {
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
            // Пытаемся найти колонку с датой
            var dateColumnIndex = config.dateColumnIndex
            var dateString = columns.getOrNull(dateColumnIndex)

            // Если дата не найдена или не похожа на дату, пробуем найти колонку с датой
            if (dateString == null || !isLikelyDate(dateString)) {
                Timber.w("[$bankName] Дата не найдена в колонке $dateColumnIndex или не похожа на дату: '$dateString'")

                // Пробуем найти колонку, которая выглядит как дата
                for (i in columns.indices) {
                    val value = columns[i]
                    if (isLikelyDate(value)) {
                        dateColumnIndex = i
                        dateString = value
                        Timber.d("[$bankName] Найдена колонка с датой: $dateColumnIndex, значение: '$dateString'")
                        break
                    }
                }
            }

            // Если дата всё равно не найдена, возвращаем null
            if (dateString == null || !isLikelyDate(dateString)) {
                Timber.e(
                    "[$bankName] ${context.getString(
                        R.string.csv_date_not_found,
                        dateColumnIndex,
                        line,
                    )}",
                )
                return null
            }
            val description =
                columns.getOrNull(config.descriptionColumnIndex) ?: context.getString(
                    R.string.csv_no_description,
                )
            var amountString =
                columns.getOrNull(config.amountColumnIndex) ?: run {
                    Timber.e(
                        "[$bankName] ${context.getString(
                            R.string.csv_amount_not_found,
                            config.amountColumnIndex,
                            line,
                        )}",
                    )
                    return null
                }

            // Очистка суммы от нецифровых символов, кроме разделителей
            config.amountCharsToRemoveRegex?.let { regex ->
                amountString = regex.replace(amountString, "")
            } ?: run {
                // Сначала сохраняем знак, если он есть
                val isNegative = amountString.contains("-")

                // Очищаем от всех символов, кроме цифр и разделителей
                amountString = amountString.replace("[^0-9.,\\-\\s]".toRegex(), "")

                // Удаляем пробелы
                amountString = amountString.replace("\\s".toRegex(), "")

                // Если была отрицательная сумма, но знак потерялся, восстанавливаем
                if (isNegative && !amountString.contains("-")) {
                    amountString = "-$amountString"
                }
            }

            // Обработка разделителей
            if (config.amountDecimalSeparator != '.') {
                amountString = amountString.replace(config.amountDecimalSeparator, '.')
            }

            // Определение валюты
            val currencyString =
                config.currencyColumnIndex?.let { index ->
                    columns.getOrNull(index)?.takeIf { it.isNotBlank() }
                } ?: config.defaultCurrencyCode

            Timber.d("[$bankName] Обработанная строка суммы: '$amountString', валюта: '$currencyString'")
            val transactionDate =
                try {
                    // Пробуем парсить дату с использованием нескольких распространенных форматов
                    val dateFormats =
                        listOf(
                            config.dateFormat, // Сначала пробуем формат из конфига
                            SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", config.locale),
                            SimpleDateFormat("yyyy-MM-dd", config.locale),
                            SimpleDateFormat("dd.MM.yyyy", config.locale),
                            SimpleDateFormat("dd.MM.yyyy HH:mm:ss", config.locale),
                            SimpleDateFormat("yyyy/MM/dd", config.locale),
                            SimpleDateFormat("MM/dd/yyyy", config.locale),
                        )

                    var parsedDate: Date? = null
                    for (format in dateFormats) {
                        try {
                            parsedDate = format.parse(dateString)
                            if (parsedDate != null) {
                                Timber.d(
                                    "[$bankName] Успешно распарсили дату '$dateString' с форматом '${format.toPattern()}'",
                                )
                                break
                            }
                        } catch (e: Exception) {
                            // Просто пробуем следующий формат
                        }
                    }

                    parsedDate ?: throw Exception("Не удалось распарсить дату ни одним из доступных форматов")
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
            val amountValue =
                try {
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
            val isExpense =
                if (config.isExpenseColumnIndex != null) {
                    val expenseValue = columns.getOrNull(config.isExpenseColumnIndex)
                    expenseValue?.equals(config.isExpenseTrueValue, ignoreCase = true) == true
                } else {
                    columns.getOrNull(4)?.equals(
                        context.getString(com.davidbugayov.financeanalyzer.ui.R.string.csv_expense_value),
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

    /**
     * Проверяет, похожа ли строка на дату
     */
    private fun isLikelyDate(value: String): Boolean {
        // Проверяем наличие типичных разделителей дат
        val hasDateSeparators = value.contains("-") || value.contains("/") || value.contains(".")

        // Проверяем, содержит ли строка цифры (должно быть не менее 4 цифр для года)
        val digitCount = value.count { it.isDigit() }

        // Проверяем, не является ли строка просто числом (ID, сумма и т.д.)
        val isJustNumber = value.trim().all { it.isDigit() || it == '.' || it == ',' || it == '-' }

        // Строка похожа на дату, если в ней есть разделители дат, достаточно цифр и она не просто число
        return hasDateSeparators && digitCount >= 4 && !isJustNumber
    }

    // importTransactions(uri, progressCallback) не переопределяем — базовая реализация BankImportUseCase подходит для CSV
}
