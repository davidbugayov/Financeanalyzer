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
    val skipTransactionIfStatusInvalid: Boolean = true // Пропускать ли строки с невалидным статусом
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
    private val config: CsvParseConfig = CsvParseConfig() // Конфиг по умолчанию
) : BankImportUseCase(transactionRepository, context) {

    override val bankName: String = "Generic CSV (Configurable)"

    /**
     * Проверяет, подходит ли файл под ожидаемый CSV-формат (по первой строке).
     */
    override fun isValidFormat(reader: BufferedReader): Boolean {
        reader.mark(4096)
        val firstLine = reader.readLine()
        reader.reset()

        if (firstLine == null) {
            Timber.w("[$bankName] CSV-файл пуст.")
            return false
        }
        val columns = firstLine.split(config.delimiter)
        val isValid = columns.isNotEmpty() && columns.size >= config.expectedMinColumnCount
        Timber.d("[$bankName] Проверка формата CSV. Первая строка: $firstLine. Разделитель: '${config.delimiter}'. Валиден: $isValid")
        return isValid
    }

    /**
     * Пропускает строку заголовка, если она есть (согласно конфигу).
     */
    override fun skipHeaders(reader: BufferedReader) {
        if (config.hasHeader) {
            val headerLine = reader.readLine()
            Timber.d("[$bankName] Пропущена строка заголовка: $headerLine")
        } else {
            Timber.d("[$bankName] Заголовка нет (по конфигу)")
        }
    }

    /**
     * Парсит строку CSV в объект Transaction.
     * Возвращает null, если строка невалидна или не содержит транзакцию.
     */
    override fun parseLine(line: String): Transaction? {
        Timber.d("[$bankName] Парсинг строки: $line")
        val columns = line.split(config.delimiter).map { it.trim().removeSurrounding("\"") }

        if (columns.size < config.expectedMinColumnCount) {
            Timber.w("[$bankName] Недостаточно колонок. Ожидалось минимум ${config.expectedMinColumnCount}, получено ${columns.size}. Строка: $line")
            return null
        }

        // Проверка статуса транзакции (если настроено)
        if (config.skipTransactionIfStatusInvalid && config.statusColumnIndex != null && config.validStatusValues?.isNotEmpty() == true) {
            val status = columns.getOrNull(config.statusColumnIndex)
            if (status == null || config.validStatusValues.none { it.equals(status, ignoreCase = true) }) {
                Timber.d("[$bankName] Пропуск транзакции из-за невалидного статуса: '$status'. Валидные: ${config.validStatusValues}. Строка: $line")
                return null
            }
        }

        try {
            // Получаем дату
            val dateString = columns.getOrNull(config.dateColumnIndex) ?: run {
                Timber.e("[$bankName] Не найдена дата по индексу ${config.dateColumnIndex}. Строка: $line")
                return null
            }
            // Получаем описание
            val description = columns.getOrNull(config.descriptionColumnIndex) ?: "N/A"

            // Получаем сумму
            var amountString = columns.getOrNull(config.amountColumnIndex) ?: run {
                Timber.e("[$bankName] Не найдена сумма по индексу ${config.amountColumnIndex}. Строка: $line")
                return null
            }
            // Очищаем сумму от лишних символов
            config.amountCharsToRemoveRegex?.let { regex ->
                amountString = regex.replace(amountString, "")
            }
            // Приводим разделитель к '.' для Double.parseDouble
            if (config.amountDecimalSeparator != '.') {
                amountString = amountString.replace(config.amountDecimalSeparator, '.')
            }

            // Получаем валюту
            val currencyString = config.currencyColumnIndex?.let { index ->
                columns.getOrNull(index)?.takeIf { it.isNotBlank() }
            } ?: config.defaultCurrencyCode

            // Парсим дату
            val transactionDate = config.dateFormat.parse(dateString) ?: run {
                Timber.e("[$bankName] Не удалось распарсить дату: '$dateString' с форматом '${config.dateFormat.toPattern()}'. Строка: $line")
                return null
            }
            // Парсим сумму
            val amountValue = amountString.toDoubleOrNull() ?: run {
                Timber.e("[$bankName] Не удалось распарсить сумму: '$amountString' (после очистки). Строка: $line")
                return null
            }

            // Определяем расход/доход
            val isExpense: Boolean = config.isExpenseColumnIndex?.let { index ->
                columns.getOrNull(index)?.equals(config.isExpenseTrueValue, ignoreCase = true)
            } ?: (amountValue < 0) // Если нет отдельной колонки, определяем по знаку суммы

            val absAmount = kotlin.math.abs(amountValue)
            val currency = Currency.fromCode(currencyString.uppercase(Locale.ROOT))
            val money = Money(absAmount, currency)
            val category = TransactionCategoryDetector.detect(description)

            // Формируем объект транзакции
            return Transaction(
                amount = money,
                category = category,
                date = transactionDate,
                isExpense = isExpense,
                note = "Импортировано из Generic CSV", // Можно доработать для передачи имени банка
                source = bankName,
                sourceColor = 0, // Можно сделать настраиваемым
                categoryId = "",
                title = description
            )
        } catch (e: Exception) {
            Timber.e(e, "[$bankName] Ошибка при парсинге строки: $line. Config: $config")
            return null
        }
    }

    /**
     * Определяет, нужно ли пропустить строку (например, если она пустая или не содержит достаточно данных).
     */
    override fun shouldSkipLine(line: String): Boolean {
        if (super.shouldSkipLine(line)) return true // Пропуск пустых строк
        if (line.split(config.delimiter).size < config.expectedMinColumnCount) {
            Timber.d("[$bankName] Пропуск строки из-за недостаточного количества колонок: $line")
            return true
        }
        // Здесь можно добавить дополнительные условия для пропуска строк
        return false
    }

    // importTransactions(uri, progressCallback) не переопределяем — базовая реализация BankImportUseCase подходит для CSV
} 