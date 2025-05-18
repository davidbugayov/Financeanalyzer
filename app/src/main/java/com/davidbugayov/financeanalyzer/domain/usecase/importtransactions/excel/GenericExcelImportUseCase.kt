package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.excel

// Apache POI imports
import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.domain.model.ImportProgressCallback
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.BankImportUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import timber.log.Timber
import java.io.BufferedReader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import org.apache.poi.ss.usermodel.Cell as PoiCell
import org.apache.poi.ss.usermodel.Row as PoiRow

// --- Configuration Data Classes Start ---

sealed interface SheetSelector {
    data class ByIndex(val index: Int) : SheetSelector
    data class ByName(val name: String) : SheetSelector
    // data object FirstActive : SheetSelector // POI typically iterates sheets by index or gets by name.
    // Defaulting to ByIndex(0) is common.
}

enum class ExpenseDetermination {
    FROM_AMOUNT_SIGN,
    FROM_COLUMN_VALUE
}

data class ExcelColumnMapping(
    val dateColumnIndex: Int? = 0,
    val descriptionColumnIndex: Int? = 1,
    val amountColumnIndex: Int? = 2,
    val currencyColumnIndex: Int? = 3,
    val categoryColumnIndex: Int? = null,
    val noteColumnIndex: Int? = null,
    val isExpenseColumnIndex: Int? = null // Used if ExpenseDetermination.FROM_COLUMN_VALUE
)

data class DateFormatConfig(
    val primaryDateFormatString: String = "dd.MM.yyyy", // Common format DataFormatter might output for ru locale
    val fallbackDateFormatStrings: List<String> = listOf("MM/dd/yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "MM.dd.yyyy"),
    val locale: Locale = Locale.getDefault()
) {

    val primaryFormat: SimpleDateFormat by lazy {
        SimpleDateFormat(primaryDateFormatString, locale)
    }
    val fallbackFormats: List<SimpleDateFormat> by lazy {
        fallbackDateFormatStrings.map { SimpleDateFormat(it, locale) }
    }
}

data class AmountParseConfig(
    val decimalSeparator: Char = '.',
    val groupingSeparator: Char? = null, // e.g., ',' if numbers are "1,234.56"
    val currencySymbolsToRemove: List<String> = emptyList(), // e.g., listOf("$", "€", "руб")
    val otherCharsToRemoveRegexPattern: String? = "[^0-9${'$'}{decimalSeparator}-]", // Keep digits, decimal sep, and minus. Adjusted to use decimalSeparator
    val howIsExpenseDetermined: ExpenseDetermination = ExpenseDetermination.FROM_AMOUNT_SIGN,
    val isExpenseTrueValue: String = "EXPENSE" // Value in isExpenseColumnIndex that means it's an expense. Case-insensitive.
)

data class ExcelParseConfig(
    val sheetSelector: SheetSelector = SheetSelector.ByIndex(0),
    val headerRowCount: Int = 1,
    // val hasHeader: Boolean = true, // Implied by headerRowCount > 0
    val columnMapping: ExcelColumnMapping = ExcelColumnMapping(),
    val defaultCurrencyCode: String = "USD",
    val dateFormatConfig: DateFormatConfig = DateFormatConfig(),
    val amountParseConfig: AmountParseConfig = AmountParseConfig(),
    val skipEmptyRows: Boolean = true, // Rows where all mapped cells are empty
    val expectedMinValuesPerRow: Int = 2 // e.g., at least date and amount must be present
)

// --- Configuration Data Classes End ---

/**
 * A generic UseCase for importing transactions from Excel files (XLS, XLSX).
 * Uses Apache POI for parsing Excel file content.
 */
class GenericExcelImportUseCase(
    context: Context,
    transactionRepository: TransactionRepository,
    private val config: ExcelParseConfig = ExcelParseConfig() // Added config parameter
) : BankImportUseCase(transactionRepository, context) {

    override val bankName: String = "Generic Excel (POI)"

    private val excelRowToStringDelimiter = '\t'
    // Old properties to be replaced by config:
    // private val expectedColumnCount = 3 
    // private val dateColumnIndex = 0
    // private val descriptionColumnIndex = 1
    // private val amountColumnIndex = 2
    // private val currencyColumnIndex = 3 

    private suspend fun extractDataFromExcel(uri: Uri, progressCallback: ImportProgressCallback): String = withContext(Dispatchers.IO) {
        Timber.d("[$bankName] Starting Excel data extraction from URI: $uri using config: $config")
        progressCallback.onProgress(5, 100, "Чтение Excel файла...")
        val stringBuilder = StringBuilder()
        val dataFormatter = DataFormatter(config.dateFormatConfig.locale)

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                WorkbookFactory.create(inputStream).use { workbook ->
                    if (workbook.numberOfSheets == 0) {
                        Timber.w("[$bankName] Excel file has no sheets: $uri")
                        progressCallback.onProgress(100, 100, "Ошибка: В Excel файле нет листов.")
                        return@withContext ""
                    }

                    val sheet: Sheet? = when (val selector = config.sheetSelector) {
                        is SheetSelector.ByIndex -> workbook.getSheetAt(selector.index.coerceIn(0, workbook.numberOfSheets - 1))
                        is SheetSelector.ByName -> workbook.getSheet(selector.name)
                        // is SheetSelector.FirstActive -> // POI doesn't have a direct concept of "first active non-hidden" easily.
                        // workbook.activeSheetIndex might work, or iterate and find first visible.
                        // For now, defaulting to first sheet if specific selector fails or not implemented.
                        // workbook.getSheetAt(workbook.activeSheetIndex.coerceIn(0, workbook.numberOfSheets-1))
                    }

                    if (sheet == null) {
                        Timber.w(
                            "[$bankName] Could not find sheet with selector ${config.sheetSelector} in file $uri. Available sheets: ${
                                List(
                                    workbook.numberOfSheets
                                ) { workbook.getSheetName(it) }.joinToString()
                            }"
                        )
                        progressCallback.onProgress(100, 100, "Ошибка: Лист не найден согласно конфигурации.")
                        return@withContext ""
                    }

                    Timber.d("[$bankName] Processing sheet: ${sheet.sheetName}")
                    progressCallback.onProgress(10, 100, "Обработка листа: ${sheet.sheetName}")

                    val physicalRows = sheet.physicalNumberOfRows
                    var rowsProcessedForProgress = 0
                    var actualDataRowsProcessed = 0

                    // Determine max column index needed based on config
                    val maxColumnIndexNeeded = with(config.columnMapping) {
                        listOfNotNull(
                            dateColumnIndex,
                            descriptionColumnIndex,
                            amountColumnIndex,
                            currencyColumnIndex,
                            categoryColumnIndex,
                            noteColumnIndex,
                            isExpenseColumnIndex
                        ).maxOrNull() ?: 0
                    }

                    sheet.rowIterator().withIndex().forEach { (rowIndex, row) ->
                        if (rowIndex < config.headerRowCount) {
                            Timber.d("[$bankName] Skipping header row ${rowIndex + 1}")
                            return@forEach // Skip header rows
                        }

                        val rowData = mutableListOf<String>()
                        var hasAnyValueInMappedColumns = false

                        // Iterate up to the maximum configured column index + buffer or a reasonable limit
                        // DataFormatter.formatCellValue handles different cell types (strings, numbers, dates, booleans, formulas - cached value)
                        for (i in 0..maxColumnIndexNeeded) { // Iterate only up to max configured index
                            val cell: PoiCell? = row.getCell(i, PoiRow.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                            val cellValue = if (cell != null) dataFormatter.formatCellValue(cell).trim() else ""
                            rowData.add(cellValue)
                            if (cellValue.isNotBlank()) { // Check if any of the relevant cells for this row has data
                                if (config.columnMapping.dateColumnIndex == i ||
                                    config.columnMapping.descriptionColumnIndex == i ||
                                    config.columnMapping.amountColumnIndex == i) { // Add other important columns if needed
                                    hasAnyValueInMappedColumns = true
                                }
                            }
                        }

                        if (config.skipEmptyRows && !hasAnyValueInMappedColumns && rowData.all { it.isBlank() }) {
                            Timber.d("[$bankName] Skipping empty row ${rowIndex + 1} (all cells blank or only unmapped cells have data)")
                            rowsProcessedForProgress++ // Count for progress calculation
                        } else {
                            stringBuilder.append(rowData.joinToString(separator = excelRowToStringDelimiter.toString()))
                            stringBuilder.append("\n")
                            actualDataRowsProcessed++
                            rowsProcessedForProgress++
                        }

                        if (physicalRows > 0 && rowsProcessedForProgress % 20 == 0) { // Update progress less frequently for large files
                            val currentProgress = 10 + ((rowsProcessedForProgress.toDouble() / physicalRows.toDouble()) * 80).toInt().coerceIn(0, 80)
                            progressCallback.onProgress(currentProgress, 100, "Чтение строк: $rowsProcessedForProgress/$physicalRows")
                        }
                    }
                    Timber.d("[$bankName] Excel data extraction successful. Data rows processed: $actualDataRowsProcessed. Config: $config")
                    progressCallback.onProgress(90, 100, "Excel файл прочитан, подготовка данных...")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "[$bankName] Error extracting data from Excel file: $uri. Config: $config")
            progressCallback.onProgress(100, 100, "Ошибка чтения Excel: ${e.message}")
            return@withContext ""
        }
        return@withContext stringBuilder.toString()
    }

    override fun importTransactions(uri: Uri, progressCallback: ImportProgressCallback): Flow<ImportResult> = flow {
        emit(ImportResult.progress(0, 100, "Начало импорта Excel файла..."))

        val extractedData = extractDataFromExcel(uri, progressCallback)

        if (extractedData.isBlank()) {
            Timber.w("[$bankName] Не удалось извлечь данные из Excel файла: $uri")
            emit(ImportResult.error("Не удалось извлечь данные из Excel файла. Проверьте формат файла или логи для деталей."))
            return@flow
        }

        try {
            StringReader(extractedData).use { stringReader ->
                BufferedReader(stringReader).use { reader ->
                    // Предполагаем, что processTransactionsFromReader в BankImportUseCase не имеет параметра skipHeaderManually
                    // Заголовки уже пропущены в extractDataFromExcel
                    emitAll(super.processTransactionsFromReader(reader, progressCallback))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "[$bankName] Ошибка при обработке извлеченных Excel данных.")
            emit(ImportResult.error("Ошибка обработки данных Excel: ${e.localizedMessage}"))
        }
    }

    override fun isValidFormat(reader: BufferedReader): Boolean {
        // This method now validates the *text representation* from POI (which should be pre-processed).
        // If extractDataFromExcel produced data, we assume the Excel format itself was valid enough to be read by POI.
        reader.mark(8192)
        val firstLine = reader.readLine()
        reader.reset()
        if (firstLine == null) {
            Timber.w("[$bankName] Извлеченные из Excel данные (текст) пусты (не найдено строк данных).")
            return false // No data rows were extracted
        }
        // A basic check on the first *data* line (headers should be skipped by extractDataFromExcel)
        val columns = firstLine.split(excelRowToStringDelimiter)
        val minExpectedColumns = listOfNotNull(config.columnMapping.dateColumnIndex, config.columnMapping.amountColumnIndex).size
        val isValid = columns.size >= minExpectedColumns && columns.size >= config.expectedMinValuesPerRow
        Timber.d("[$bankName] Валидация текстового представления Excel (первая строка данных): $firstLine. IsValid: $isValid (cols: ${columns.size} >= $minExpectedColumns)")
        return isValid
    }

    // This method is effectively bypassed if skipHeaderManually = false in processTransactionsFromReader
    // Headers are skipped during extractDataFromExcel based on config.headerRowCount.
    override fun skipHeaders(reader: BufferedReader) {
        Timber.w("[$bankName] skipHeaders() called, but headers should have been skipped during Excel extraction by config.headerRowCount. Line read for debug: ${reader.readLine()}")
        // reader.readLine() // If called, it would consume the first data line.
    }

    override fun parseLine(line: String): Transaction? {
        Timber.d("[$bankName] Парсинг текстового представления строки Excel: $line")
        val columns = line.split(excelRowToStringDelimiter) // No need to trim or unquote, POI DataFormatter should handle it.

        // Check based on expectedMinValuesPerRow or a more robust check based on mapped columns
        // This check could be more sophisticated, e.g., checking if all *required* mapped columns have data.
        val populatedValues = config.columnMapping.let {
            listOfNotNull(it.dateColumnIndex, it.descriptionColumnIndex, it.amountColumnIndex)
                .mapNotNull { index -> columns.getOrNull(index)?.takeIf { it.isNotBlank() } }
                .count()
        }
        if (populatedValues < config.expectedMinValuesPerRow) {
            Timber.w("[$bankName] Недостаточно заполненных значений в строке (нужно ${config.expectedMinValuesPerRow}, найдено $populatedValues из ключевых). Строка: $line. Columns: $columns")
            return null
        }

        try {
            val dateString = config.columnMapping.dateColumnIndex?.let { columns.getOrNull(it) }?.takeIf { it.isNotBlank() }
            if (dateString == null && config.columnMapping.dateColumnIndex != null) { // Only fail if date column was expected
                Timber.w("[$bankName] Отсутствует дата в настроенном столбце ${config.columnMapping.dateColumnIndex}. Строка: $line")
                return null
            }

            val description = config.columnMapping.descriptionColumnIndex?.let { columns.getOrNull(it) } ?: "N/A"

            var amountString = config.columnMapping.amountColumnIndex?.let { columns.getOrNull(it) }?.takeIf { it.isNotBlank() }
            if (amountString == null && config.columnMapping.amountColumnIndex != null) { // Only fail if amount column was expected
                Timber.w("[$bankName] Отсутствует сумма в настроенном столбце ${config.columnMapping.amountColumnIndex}. Строка: $line")
                return null
            }

            amountString = amountString?.let { initialAmount ->
                var currentAmount = initialAmount
                config.amountParseConfig.currencySymbolsToRemove.forEach { sym -> currentAmount = currentAmount.replace(sym, "") }
                config.amountParseConfig.groupingSeparator?.let { sep -> currentAmount = currentAmount.replace(sep.toString(), "") }

                config.amountParseConfig.otherCharsToRemoveRegexPattern?.let {
                    val pattern = Regex(it.replace("${'$'}{decimalSeparator}", Regex.escape(config.amountParseConfig.decimalSeparator.toString())))
                    currentAmount = pattern.replace(currentAmount, "")
                }

                if (config.amountParseConfig.decimalSeparator != '.') {
                    currentAmount = currentAmount.replace(config.amountParseConfig.decimalSeparator, '.')
                }
                currentAmount.trim()
            } ?: return null

            val currencyString =
                config.columnMapping.currencyColumnIndex?.let { columns.getOrNull(it)?.takeIf { it.isNotBlank() } } ?: config.defaultCurrencyCode

            val transactionDate = dateString?.let { ds ->
                try {
                    config.dateFormatConfig.primaryFormat.parse(ds)
                } catch (e: java.text.ParseException) {
                    config.dateFormatConfig.fallbackFormats.firstNotNullOfOrNull { it.parse(ds) }
                }
            } ?: run {
                Timber.w("[$bankName] Не удалось распознать формат даты: '$dateString' из строки: $line. Config: ${config.dateFormatConfig}")
                return null
            }

            val amountValue = amountString.toDoubleOrNull() ?: run {
                Timber.w("[$bankName] Ошибка парсинга суммы: '$amountString' (после очистки) из строки: $line. Config: ${config.amountParseConfig}")
                return null
            }

            val isExpense: Boolean = when (config.amountParseConfig.howIsExpenseDetermined) {
                ExpenseDetermination.FROM_AMOUNT_SIGN -> amountValue < 0
                ExpenseDetermination.FROM_COLUMN_VALUE -> {
                    val expVal = config.columnMapping.isExpenseColumnIndex?.let { columns.getOrNull(it) }
                    expVal?.equals(config.amountParseConfig.isExpenseTrueValue, ignoreCase = true) ?: (amountValue < 0) // Fallback to sign
                }
            }

            val absAmount = kotlin.math.abs(amountValue)
            val money = Money(absAmount, Currency.fromCode(currencyString.uppercase(Locale.ROOT)))

            val category = config.columnMapping.categoryColumnIndex?.let { columns.getOrNull(it)?.takeIf { it.isNotBlank() } }
                ?: (if (isExpense) "Generic Excel Expense" else "Generic Excel Income")

            val note = config.columnMapping.noteColumnIndex?.let { columns.getOrNull(it)?.takeIf { it.isNotBlank() } } ?: "Imported from $bankName"

            return Transaction(
                amount = money,
                category = category,
                date = transactionDate,
                isExpense = isExpense,
                note = note,
                source = bankName, // TODO: Allow bankName from config?
                sourceColor = 0, // TODO: Placeholder color, make configurable
                categoryId = "", // TODO: Placeholder category ID, map from category string if possible
                title = description
            )
        } catch (e: Exception) {
            Timber.e(e, "[$bankName] Ошибка парсинга текстового представления строки Excel: $line. Config: $config")
            return null
        }
    }

    override fun shouldSkipLine(line: String): Boolean {
        // This check is now against the text representation of a DATA row (headers already skipped)
        if (super.shouldSkipLine(line)) return true // Handles blank lines from stringBuilder output

        // If skipEmptyRows was true in config, extractDataFromExcel should have already filtered them.
        // However, a line might still be constructed with only delimiters if not perfectly filtered.
        if (line.replace(excelRowToStringDelimiter.toString(), "").isBlank()) {
            Timber.d("[$bankName] Skipping effectively blank line from Excel text (consists only of delimiters or whitespace): $line")
            return true
        }

        // Further checks based on config.expectedMinValuesPerRow can be done in parseLine
        return false
    }
} 