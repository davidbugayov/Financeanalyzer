package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.excel

// Apache POI imports
import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.category.TransactionCategoryDetector
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.BankImportUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportProgressCallback
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportResult
import java.io.BufferedReader
import java.io.StringReader
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Cell as PoiCell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row as PoiRow
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import timber.log.Timber

// --- Configuration Data Classes Start ---

sealed interface SheetSelector {
    data class ByIndex(
        val index: Int,
    ) : SheetSelector

    data class ByName(
        val name: String,
    ) : SheetSelector
}

enum class ExpenseDetermination {
    FROM_AMOUNT_SIGN,
    FROM_COLUMN_VALUE,
}

data class ExcelColumnMapping(
    val dateColumnIndex: Int? = 0,
    val descriptionColumnIndex: Int? = 1,
    val amountColumnIndex: Int? = 2,
    val currencyColumnIndex: Int? = 3,
    val categoryColumnIndex: Int? = null,
    val noteColumnIndex: Int? = null,
    // Used if ExpenseDetermination.FROM_COLUMN_VALUE
    val isExpenseColumnIndex: Int? = null,
)

data class DateFormatConfig(
    // Common format DataFormatter might output for ru locale
    val primaryDateFormatString: String = "dd.MM.yyyy",
    val fallbackDateFormatStrings: List<String> =
        listOf(
            "MM/dd/yyyy",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "MM.dd.yyyy",
        ),
    val locale: Locale = Locale.getDefault(),
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
    // e.g., ',' if numbers are "1,234.56"
    val groupingSeparator: Char? = null,
    // e.g., listOf("$", "€", "руб")
    val currencySymbolsToRemove: List<String> = emptyList(),
    // Keep digits, decimal sep, and minus. Adjusted to use decimalSeparator
    val otherCharsToRemoveRegexPattern: String? = "[^0-9${'$'}{decimalSeparator}-]",
    val howIsExpenseDetermined: ExpenseDetermination = ExpenseDetermination.FROM_AMOUNT_SIGN,
    // Value in isExpenseColumnIndex that means it's an expense. Case-insensitive.
    val isExpenseTrueValue: String = "EXPENSE",
)

data class ExcelParseConfig(
    val sheetSelector: SheetSelector = SheetSelector.ByIndex(0),
    val headerRowCount: Int = 1,
    val columnMapping: ExcelColumnMapping = ExcelColumnMapping(),
    val defaultCurrencyCode: String = "RUB",
    val dateFormatConfig: DateFormatConfig = DateFormatConfig(),
    val amountParseConfig: AmountParseConfig = AmountParseConfig(),
    val skipEmptyRows: Boolean = true, // Rows where all mapped cells are empty
    val expectedMinValuesPerRow: Int = 2, // e.g., at least date and amount must be present
)

/**
 * Универсальный UseCase для импорта транзакций из Excel-файлов (XLS, XLSX).
 * Гибко настраивается через ExcelParseConfig для поддержки разных банков и форматов.
 * Использует Apache POI для парсинга.
 */
class GenericExcelImportUseCase(
    context: Context,
    transactionRepository: TransactionRepository,
    private var config: ExcelParseConfig = ExcelParseConfig(), // теперь var
    private val transactionSource: String? = null, // Опциональный источник транзакций
    private val debugEnabled: Boolean = false, // Включение режима отладки с подробным логированием
) : BankImportUseCase(transactionRepository, context) {
    // Переменная для хранения обнаруженного типа банка
    private var detectedBankName: String = "Generic Excel (POI)"

    override val bankName: String
        get() = detectedBankName

    // Разделитель для преобразования строки Excel в текстовую строку
    private val excelRowToStringDelimiter = '\t'

    /**
     * Эвристика: определяет, похож ли лист на выписку Альфа-Банка и возвращает подходящий ExcelParseConfig.
     */
    private fun detectAlfaBankConfig(sheet: Sheet): ExcelParseConfig? {
        val headerRowLimit = 30
        val headerCandidates =
            (0 until minOf(headerRowLimit, sheet.lastRowNum + 1)).mapNotNull { idx ->
                sheet.getRow(idx)
            }
        val headerRow =
            headerCandidates.firstOrNull { row ->
                val text =
                    (0 until row.lastCellNum).joinToString(
                        " ",
                    ) { row.getCell(it)?.toString()?.lowercase() ?: "" }
                (text.contains("альфа") || text.contains("alfa")) &&
                    text.contains("дата") &&
                    text.contains(
                        "сумма",
                    ) ||
                    (text.contains("дата") && text.contains("описание") && text.contains("сумма")) ||
                    (text.contains("дата операции") && text.contains("сумма"))
            } ?: headerCandidates.firstOrNull { row ->
                val text =
                    (0 until row.lastCellNum).joinToString(
                        " ",
                    ) { row.getCell(it)?.toString()?.lowercase() ?: "" }
                text.contains("дата") && text.contains("сумма")
            }
        if (headerRow != null) {
            // Определяем индексы колонок по ключевым словам
            var dateIdx: Int? = null
            var amountIdx: Int? = null
            var descIdx: Int? = null
            var catIdx: Int? = null
            for (i in 0 until headerRow.lastCellNum) {
                val cellVal =
                    headerRow
                        .getCell(i)
                        ?.toString()
                        ?.lowercase()
                        ?.trim() ?: continue
                if (dateIdx == null && cellVal.contains("дата")) dateIdx = i
                if (amountIdx == null && cellVal.contains("сумма")) amountIdx = i
                if (descIdx == null &&
                    (
                        cellVal.contains("описание") ||
                            cellVal.contains(
                                "примечание",
                            ) ||
                            cellVal.contains("назначение")
                    )
                ) {
                    descIdx = i
                }
                if (catIdx == null && cellVal.contains("категория")) catIdx = i
            }
            // Если нашли дату и сумму — считаем, что это Альфа-Банк
            if (dateIdx != null && amountIdx != null) {
                // Устанавливаем название банка
                detectedBankName = "Альфа-Банк"

                return ExcelParseConfig(
                    sheetSelector = SheetSelector.ByName(sheet.sheetName),
                    headerRowCount = headerRow.rowNum + 1,
                    columnMapping =
                        ExcelColumnMapping(
                            dateColumnIndex = dateIdx,
                            descriptionColumnIndex = descIdx,
                            amountColumnIndex = amountIdx,
                            categoryColumnIndex = catIdx,
                        ),
                    defaultCurrencyCode = "RUB",
                    dateFormatConfig =
                        DateFormatConfig(
                            primaryDateFormatString = "dd.MM.yyyy",
                            fallbackDateFormatStrings =
                                listOf(
                                    "dd.MM.yyyy HH:mm:ss",
                                    "yyyy-MM-dd",
                                    "MM/dd/yyyy",
                                ),
                            locale = Locale.forLanguageTag("ru-RU"),
                        ),
                    amountParseConfig =
                        AmountParseConfig(
                            decimalSeparator = ',',
                            groupingSeparator = ' ',
                            currencySymbolsToRemove = listOf("₽", "руб", "RUB"),
                            otherCharsToRemoveRegexPattern = "[^0-9.,-]",
                            howIsExpenseDetermined = ExpenseDetermination.FROM_AMOUNT_SIGN,
                        ),
                    skipEmptyRows = true,
                    expectedMinValuesPerRow = 2,
                )
            }
        }
        return null
    }

    /**
     * Извлекает данные из Excel-файла и преобразует их в текстовый вид для дальнейшего парсинга.
     * Все параметры (какой лист, какие колонки, как парсить суммы и даты) задаются через config.
     */
    private suspend fun extractDataFromExcel(
        uri: Uri,
        progressCallback: ImportProgressCallback,
    ): String =
        withContext(
            Dispatchers.IO,
        ) {
            Timber.d("[$bankName] Начало извлечения данных из Excel по URI: $uri с конфигом: $config")
            progressCallback.onProgress(5, 100, "Чтение Excel файла...")
            val stringBuilder = StringBuilder()
            val dataFormatter = DataFormatter(config.dateFormatConfig.locale)
            try {
                context.contentResolver.openInputStream(uri)?.use { rawStream ->
                    val inputStream = java.io.BufferedInputStream(rawStream)
                    try {
                        // Проверяем первые байты файла для валидации формата Excel
                        val buffer = ByteArray(8)
                        inputStream.mark(8)
                        val bytesRead = inputStream.read(buffer)
                        inputStream.reset()
                        val isValidExcel =
                            if (bytesRead >= 8) {
                                (buffer[0] == 0x50.toByte() && buffer[1] == 0x4B.toByte()) ||
                                    (
                                        buffer[0] == 0xD0.toByte() &&
                                            buffer[1] == 0xCF.toByte() &&
                                            buffer[2] == 0x11.toByte() &&
                                            buffer[3] == 0xE0.toByte()
                                    )
                            } else {
                                false
                            }
                        if (!isValidExcel) {
                            Timber.e("[$bankName] Файл не является корректным Excel-файлом: $uri")
                            progressCallback.onProgress(
                                100,
                                100,
                                "Ошибка: Файл не является корректным Excel-файлом или поврежден.",
                            )
                            return@withContext ""
                        }
                        WorkbookFactory.create(inputStream).use { workbook ->
                            if (workbook.numberOfSheets == 0) {
                                Timber.w("[$bankName] В Excel-файле нет листов: $uri")
                                progressCallback.onProgress(
                                    100,
                                    100,
                                    "Ошибка: В Excel файле нет листов.",
                                )
                                return@withContext ""
                            }
                            // Выбор нужного листа по индексу или имени
                            var sheet: Sheet? =
                                when (val selector = config.sheetSelector) {
                                    is SheetSelector.ByIndex ->
                                        workbook.getSheetAt(
                                            selector.index.coerceIn(0, workbook.numberOfSheets - 1),
                                        )
                                    is SheetSelector.ByName -> workbook.getSheet(selector.name)
                                }
                            // --- ДОБАВЛЕНО: автоопределение Альфа-Банка ---
                            if (sheet == null) sheet = workbook.getSheetAt(0)
                            val alfaConfig = detectAlfaBankConfig(sheet!!)
                            if (alfaConfig != null) {
                                Timber.d(
                                    "[$bankName] Автоматически определена структура Альфа-Банка, применяем спец. конфиг: $alfaConfig",
                                )
                                config = alfaConfig
                            }
                            // --- КОНЕЦ ДОБАВЛЕНИЯ ---
                            sheet =
                                when (val selector = config.sheetSelector) {
                                    is SheetSelector.ByIndex ->
                                        workbook.getSheetAt(
                                            selector.index.coerceIn(0, workbook.numberOfSheets - 1),
                                        )
                                    is SheetSelector.ByName -> workbook.getSheet(selector.name)
                                }
                            if (sheet == null) {
                                Timber.w(
                                    "[$bankName] Не найден лист по конфигу ${config.sheetSelector} в файле $uri. Доступные листы: ${
                                        List(workbook.numberOfSheets) {
                                            workbook.getSheetName(
                                                it,
                                            )
                                        }.joinToString()
                                    }",
                                )
                                progressCallback.onProgress(
                                    100,
                                    100,
                                    "Ошибка: Лист не найден согласно конфигурации.",
                                )
                                return@withContext ""
                            }
                            Timber.d("[$bankName] Обработка листа: ${sheet.sheetName}")
                            progressCallback.onProgress(10, 100, "Обработка листа: ${sheet.sheetName}")
                            val physicalRows = sheet.physicalNumberOfRows
                            var rowsProcessedForProgress = 0
                            var actualDataRowsProcessed = 0
                            val maxColumnIndexNeeded =
                                with(config.columnMapping) {
                                    listOfNotNull(
                                        dateColumnIndex,
                                        descriptionColumnIndex,
                                        amountColumnIndex,
                                        currencyColumnIndex,
                                        categoryColumnIndex,
                                        noteColumnIndex,
                                        isExpenseColumnIndex,
                                    ).maxOrNull() ?: 0
                                }
                            sheet.rowIterator().withIndex().forEach { (rowIndex, row) ->
                                if (rowIndex < config.headerRowCount) {
                                    Timber.d("[$bankName] Пропуск строки заголовка ${rowIndex + 1}")
                                    return@forEach
                                }
                                // --- ДОБАВЛЕНО: пропуск служебных строк Альфа-Банка ---
                                val firstCell =
                                    row
                                        .getCell(0)
                                        ?.toString()
                                        ?.lowercase()
                                        ?.trim() ?: ""
                                if (firstCell.contains("итого") ||
                                    firstCell.contains("остаток") ||
                                    firstCell.contains(
                                        "оборот",
                                    ) ||
                                    firstCell.contains("сумма")
                                ) {
                                    Timber.d("[$bankName] Пропуск служебной строки: '$firstCell'")
                                    rowsProcessedForProgress++
                                    return@forEach
                                }
                                // --- КОНЕЦ ДОБАВЛЕНИЯ ---
                                val rowData = mutableListOf<String>()
                                var hasAnyValueInMappedColumns = false
                                for (i in 0..maxColumnIndexNeeded) {
                                    val cell: PoiCell? =
                                        row.getCell(
                                            i,
                                            PoiRow.MissingCellPolicy.RETURN_BLANK_AS_NULL,
                                        )
                                    val cellValue =
                                        if (cell != null) {
                                            dataFormatter
                                                .formatCellValue(
                                                    cell,
                                                ).trim()
                                        } else {
                                            ""
                                        }
                                    rowData.add(cellValue)
                                    if (cellValue.isNotBlank()) {
                                        if (config.columnMapping.dateColumnIndex == i ||
                                            config.columnMapping.descriptionColumnIndex == i ||
                                            config.columnMapping.amountColumnIndex == i
                                        ) {
                                            hasAnyValueInMappedColumns = true
                                        }
                                    }
                                }
                                if (config.skipEmptyRows &&
                                    !hasAnyValueInMappedColumns &&
                                    rowData.all { it.isBlank() }
                                ) {
                                    Timber.d("[$bankName] Пропуск пустой строки ${rowIndex + 1}")
                                    rowsProcessedForProgress++
                                } else {
                                    stringBuilder.append(
                                        rowData.joinToString(
                                            separator = excelRowToStringDelimiter.toString(),
                                        ),
                                    )
                                    stringBuilder.append("\n")
                                    actualDataRowsProcessed++
                                    rowsProcessedForProgress++
                                }
                                if (physicalRows > 0 && rowsProcessedForProgress % 20 == 0) {
                                    val progress =
                                        BigDecimal(rowsProcessedForProgress)
                                            .divide(
                                                BigDecimal(physicalRows),
                                                4,
                                                java.math.RoundingMode.HALF_EVEN,
                                            ).multiply(
                                                BigDecimal(80),
                                            ).setScale(0, java.math.RoundingMode.FLOOR)
                                            .toInt()
                                            .coerceIn(
                                                0,
                                                80,
                                            ) + 10
                                    progressCallback.onProgress(
                                        progress,
                                        100,
                                        "Чтение строк: $rowsProcessedForProgress/$physicalRows",
                                    )
                                }
                            }
                            Timber.d(
                                "[$bankName] Excel data extraction successful. Data rows processed: $actualDataRowsProcessed. Config: $config",
                            )
                            progressCallback.onProgress(
                                90,
                                100,
                                "Excel файл прочитан, подготовка данных...",
                            )
                        }
                    } catch (e: org.apache.poi.EmptyFileException) {
                        Timber.e(e, "[$bankName] Ошибка: Excel файл пуст: $uri")
                        progressCallback.onProgress(100, 100, "Ошибка: Excel файл пуст или поврежден.")
                        return@withContext ""
                    } catch (e: org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException) {
                        Timber.e(e, "[$bankName] Ошибка: Файл не является XLSX-файлом: $uri")
                        progressCallback.onProgress(
                            100,
                            100,
                            "Ошибка: Файл не является корректным Excel-файлом (XLSX).",
                        )
                        return@withContext ""
                    } catch (e: org.apache.poi.poifs.filesystem.NotOLE2FileException) {
                        Timber.e(e, "[$bankName] Ошибка: Файл не является XLS-файлом: $uri")
                        progressCallback.onProgress(
                            100,
                            100,
                            "Ошибка: Файл не является корректным Excel-файлом (XLS).",
                        )
                        return@withContext ""
                    } catch (e: org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException) {
                        Timber.e(e, "[$bankName] Ошибка: Файл не является XLSX-файлом (это XLS?): $uri")
                        progressCallback.onProgress(
                            100,
                            100,
                            "Ошибка: Формат Excel-файла не соответствует расширению.",
                        )
                        return@withContext ""
                    } catch (e: org.apache.poi.openxml4j.exceptions.InvalidFormatException) {
                        Timber.e(e, "[$bankName] Ошибка: Неверный формат Excel-файла: $uri")
                        progressCallback.onProgress(100, 100, "Ошибка: Неверный формат Excel-файла.")
                        return@withContext ""
                    } catch (e: org.apache.poi.UnsupportedFileFormatException) {
                        Timber.e(e, "[$bankName] Ошибка: Неподдерживаемый формат файла: $uri")
                        progressCallback.onProgress(100, 100, "Ошибка: Неподдерживаемый формат файла.")
                        return@withContext ""
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "[$bankName] Ошибка при чтении Excel: $uri. Config: $config")
                progressCallback.onProgress(100, 100, "Ошибка чтения Excel: ${e.message}")
                return@withContext ""
            }
            return@withContext stringBuilder.toString()
        }

    /**
     * Основной метод импорта: извлекает данные из Excel, затем парсит их в транзакции.
     * Весь парсинг и логика обработки строк делегируется в parseLine и конфиг.
     */
    override fun importTransactions(
        uri: Uri,
        progressCallback: ImportProgressCallback,
    ): Flow<ImportResult> =
        flow {
            emit(ImportResult.progress(0, 100, "Начало импорта Excel файла..."))
            val extractedData = extractDataFromExcel(uri, progressCallback)
            if (extractedData.isBlank()) {
                Timber.w("[$bankName] Не удалось извлечь данные из Excel файла: $uri")
                emit(
                    ImportResult.error(
                        "Не удалось извлечь данные из Excel файла. Проверьте формат файла или логи для деталей.",
                    ),
                )
                return@flow
            }
            try {
                StringReader(extractedData).use { stringReader ->
                    BufferedReader(stringReader).use { reader ->
                        emitAll(super.processTransactionsFromReader(reader, progressCallback))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "[$bankName] Ошибка при обработке извлеченных Excel данных.")
                emit(ImportResult.error("Ошибка обработки данных Excel: ${e.localizedMessage}"))
            }
        }

    /**
     * Проверяет, что первая строка данных (после заголовков) содержит нужное количество колонок.
     * Это базовая валидация структуры файла.
     */
    override fun isValidFormat(reader: BufferedReader): Boolean {
        reader.mark(8192)
        val firstLine = reader.readLine()
        reader.reset()
        if (firstLine == null) {
            Timber.w(
                "[$bankName] Извлеченные из Excel данные (текст) пусты (не найдено строк данных).",
            )
            return false
        }
        val columns = firstLine.split(excelRowToStringDelimiter)
        val minExpectedColumns =
            listOfNotNull(
                config.columnMapping.dateColumnIndex,
                config.columnMapping.amountColumnIndex,
            ).size
        val isValid = columns.size >= minExpectedColumns && columns.size >= config.expectedMinValuesPerRow
        Timber.d(
            "[$bankName] Валидация текстового представления Excel (первая строка данных): $firstLine. IsValid: $isValid (cols: ${columns.size} >= $minExpectedColumns)",
        )
        return isValid
    }

    /**
     * Заголовки пропускаются на этапе извлечения данных из Excel (extractDataFromExcel),
     * поэтому здесь просто логируем вызов.
     */
    override fun skipHeaders(reader: BufferedReader) {
        Timber.w(
            "[$bankName] skipHeaders() вызван, но заголовки уже были пропущены при извлечении Excel. Debug: ${reader.readLine()}",
        )
    }

    /**
     * Парсит одну строку (одну транзакцию) из текстового представления Excel.
     * Вся логика разбора колонок и преобразования значений управляется через config.
     */
    override fun parseLine(line: String): Transaction? {
        val columns = line.split(excelRowToStringDelimiter)

        // Режим отладки: выводим все значения столбцов
        if (debugEnabled) {
            Timber.d("[$bankName DEBUG] Содержимое строки: $line")
            columns.forEachIndexed { index, value ->
                Timber.d("[$bankName DEBUG] Столбец $index: '$value'")
            }
        }

        // Проверка на пустую строку или строку только с разделителями
        if (line.isBlank() || line.replace(excelRowToStringDelimiter.toString(), "").isBlank()) {
            Timber.d("[$bankName] Пропуск пустой строки")
            return null
        }

        // Проверяем, что заполнено минимум нужных колонок
        val populatedValues =
            config.columnMapping.let {
                listOfNotNull(it.dateColumnIndex, it.descriptionColumnIndex, it.amountColumnIndex)
                    .mapNotNull { index -> columns.getOrNull(index)?.takeIf { it.isNotBlank() } }
                    .count()
            }

        // Проверка на минимальное количество заполненных значений
        if (populatedValues < config.expectedMinValuesPerRow) {
            Timber.w(
                "[$bankName] Недостаточно заполненных значений в строке (нужно ${config.expectedMinValuesPerRow}, найдено $populatedValues из ключевых). Строка: $line",
            )
            return null
        }

        try {
            // --- Дата ---
            val dateString =
                config.columnMapping.dateColumnIndex?.let {
                    columns.getOrNull(it)?.takeIf { it.isNotBlank() && it != "null" && it != "N/A" }
                }

            if (dateString == null && config.columnMapping.dateColumnIndex != null) {
                Timber.w(
                    "[$bankName] Отсутствует дата в настроенном столбце ${config.columnMapping.dateColumnIndex}. Строка: $line",
                )
                return null
            }

            // --- Примечание (note) ---
            val note =
                config.columnMapping.noteColumnIndex?.let {
                    columns.getOrNull(it)?.takeIf { it.isNotBlank() }
                } ?: config.columnMapping.descriptionColumnIndex?.let {
                    columns.getOrNull(it)?.takeIf { it.isNotBlank() }
                } ?: "Импортировано из $bankName"

            // --- Сумма ---
            var amountString =
                config.columnMapping.amountColumnIndex?.let {
                    columns.getOrNull(it)?.takeIf { it.isNotBlank() && it != "null" && it != "N/A" }
                }

            if (amountString == null && config.columnMapping.amountColumnIndex != null) {
                Timber.w(
                    "[$bankName] Отсутствует сумма в настроенном столбце ${config.columnMapping.amountColumnIndex}. Строка: $line",
                )
                return null
            }

            // Очистка суммы от лишних символов, валюты, разделителей
            amountString = amountString?.let { initialAmount ->
                var currentAmount = initialAmount
                if (config.amountParseConfig.decimalSeparator == ',') {
                    config.amountParseConfig.groupingSeparator?.let { sep ->
                        if (sep != ',') {
                            currentAmount = currentAmount.replace(sep.toString(), "")
                        }
                    }
                    currentAmount = currentAmount.replace(',', '.')
                } else {
                    config.amountParseConfig.groupingSeparator?.let { sep ->
                        currentAmount = currentAmount.replace(sep.toString(), "")
                    }
                }
                config.amountParseConfig.currencySymbolsToRemove.forEach { sym ->
                    currentAmount = currentAmount.replace(sym, "")
                }
                config.amountParseConfig.otherCharsToRemoveRegexPattern?.let {
                    val pattern =
                        Regex(
                            it.replace(
                                "${'$'}{decimalSeparator}",
                                Regex.escape(config.amountParseConfig.decimalSeparator.toString()),
                            ),
                        )
                    currentAmount = pattern.replace(currentAmount, "")
                }
                currentAmount.trim()
            } ?: run {
                if (config.columnMapping.amountColumnIndex == null) "0" else return null
            }

            // --- Валюта ---
            val currencyString =
                config.columnMapping.currencyColumnIndex?.let {
                    columns.getOrNull(it)?.takeIf { it.isNotBlank() }
                } ?: config.defaultCurrencyCode

            // --- Парсинг даты ---
            val transactionDate =
                dateString?.let { ds ->
                    try {
                        config.dateFormatConfig.primaryFormat.parse(ds)
                    } catch (_: java.text.ParseException) {
                        try {
                            config.dateFormatConfig.fallbackFormats.firstNotNullOfOrNull {
                                try {
                                    it.parse(ds)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "[$bankName] Ошибка при парсинге даты '$ds': ${e.message}")
                            null
                        }
                    }
                } ?: run {
                    Timber.w(
                        "[$bankName] Не удалось распознать формат даты: '$dateString' из строки: $line",
                    )
                    return null
                }

            // --- Парсинг суммы ---
            val amountValue =
                try {
                    amountString.toDoubleOrNull() ?: run {
                        Timber.w(
                            "[$bankName] Ошибка парсинга суммы: '$amountString' (после очистки) из строки: $line",
                        )
                        if (config.columnMapping.amountColumnIndex == null) 0.0 else return null
                    }
                } catch (e: NumberFormatException) {
                    Timber.e(
                        e,
                        "[$bankName] Ошибка преобразования суммы '$amountString' в число: ${e.message}",
                    )
                    if (config.columnMapping.amountColumnIndex == null) 0.0 else return null
                }

            // --- Определение расход/доход ---
            val isExpense: Boolean =
                when (config.amountParseConfig.howIsExpenseDetermined) {
                    ExpenseDetermination.FROM_AMOUNT_SIGN -> amountValue < 0
                    ExpenseDetermination.FROM_COLUMN_VALUE -> {
                        val expVal =
                            config.columnMapping.isExpenseColumnIndex?.let {
                                columns.getOrNull(
                                    it,
                                )
                            }
                        expVal?.equals(config.amountParseConfig.isExpenseTrueValue, ignoreCase = true)
                            ?: (amountValue < 0)
                    }
                }

            val absAmount = kotlin.math.abs(amountValue)

            // Создаем объект Money с проверкой валидности валюты
            val currency =
                try {
                    Currency.fromCode(currencyString.uppercase(Locale.ROOT))
                } catch (_: Exception) {
                    Timber.w(
                        "[$bankName] Неизвестная валюта '$currencyString', используем дефолтную: ${config.defaultCurrencyCode}",
                    )
                    Currency.fromCode(config.defaultCurrencyCode)
                }

            val money = Money(absAmount, currency)

            // --- Категория ---
            val rawCategory =
                config.columnMapping.categoryColumnIndex?.let {
                    columns.getOrNull(it)?.takeIf { it.isNotBlank() }
                }
            val category =
                rawCategory
                    ?.removePrefix("Категория:")
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: TransactionCategoryDetector.detect(note)

            // --- Формируем объект транзакции ---
            val transaction =
                Transaction(
                    amount = money,
                    category = category,
                    date = transactionDate,
                    isExpense = isExpense,
                    note = note,
                    source = transactionSource ?: detectedBankName,
                    sourceColor = 0, // Можно сделать настраиваемым через config
                    categoryId = "",
                    title = "", // Описание не дублируется, если нужно — можно подставить note
                )

            if (debugEnabled) {
                Timber.d("[$bankName DEBUG] Создана транзакция: $transaction")
            }

            return transaction
        } catch (e: Exception) {
            Timber.e(
                e,
                "[$bankName] Ошибка парсинга строки Excel: $line. Config: $config. Ошибка: ${e.message}",
            )
            return null
        }
    }

    /**
     * Проверяет, нужно ли пропустить строку (например, если она пустая или не содержит данных).
     */
    override fun shouldSkipLine(line: String): Boolean {
        if (super.shouldSkipLine(line)) return true
        if (line.replace(excelRowToStringDelimiter.toString(), "").isBlank()) {
            Timber.d(
                "[$bankName] Пропуск пустой строки Excel (только разделители или пробелы): $line",
            )
            return true
        }
        return false
    }
}
