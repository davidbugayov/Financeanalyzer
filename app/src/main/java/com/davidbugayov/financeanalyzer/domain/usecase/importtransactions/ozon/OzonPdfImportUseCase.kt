package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ozon

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.domain.model.ImportProgressCallback
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.BankImportUseCase
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OzonPdfImportUseCase(
    context: Context,
    transactionRepository: TransactionRepository
) : BankImportUseCase(transactionRepository, context) {

    override val bankName: String = "Ozon Банк (PDF)"

    // Константа для источника транзакций
    private val transactionSource: String = "Озон"

    private var currentPartialTransaction: PartialTransaction? = null

    private data class PartialTransaction(
        var date: Date? = null,
        var documentNumber: String? = null,
        val descriptionLines: MutableList<String> = mutableListOf(),
        var amount: Double? = null,
        var currency: Currency = Currency.RUB,
        var isExpense: Boolean? = null,
        var linesProcessedForCurrentTx: Int = 0,
        var isComplete: Boolean = false
    ) {
        fun clear() {
            date = null
            documentNumber = null
            descriptionLines.clear()
            amount = null
            currency = Currency.RUB
            isExpense = null
            linesProcessedForCurrentTx = 0
            isComplete = false
        }

        fun isValidForFinalization(): Boolean = date != null && documentNumber != null && amount != null && isExpense != null

        fun buildDescription(): String = descriptionLines.joinToString(" ").trim().replace("\\s+".toRegex(), " ")
    }

    companion object {
        private val OZON_BANK_INDICATORS = listOf("OZON", "ОЗОН", "Ozon Банк", "Озон Банк")
        private val OZON_STATEMENT_TITLES = listOf(
            "Выписка по счёту", "Информация по счёту", "ИСТОРИЯ ОПЕРАЦИЙ", "Справка о движении средств"
        )
        private val OZON_TABLE_HEADER_MARKER = "Дата операции Документ Назначение платежа Сумма операции"
        private const val MAX_VALIDATION_LINES = 30
        private const val MAX_HEADER_SKIP_LINES = 300

        // Используем Raw Strings с явным экранированием для вложенных regex
        private val ozonDateDocRegex = Regex("""^(\d{2}\.\d{2}\.\d{4})\s+(\d{2}:\d{2}:\d{2})\s+(\d+).*$""")
        private val ozonAmountRegex = Regex("""^([+\-])\s*(\d[\d\s.,]*[\d])(?:\s+([A-Z]{3}))?$""")

        private val IGNORE_PATTERNS = listOf(
            Regex("^ИТОГО ПО СЧЕТУ:", RegexOption.IGNORE_CASE),
            Regex("^Исходящий остаток:", RegexOption.IGNORE_CASE),
            Regex("^Период:", RegexOption.IGNORE_CASE),
            Regex("^Выписка сформирована", RegexOption.IGNORE_CASE),
            Regex("^Страница \\d+ из \\d+", RegexOption.IGNORE_CASE)
        )
        private const val MAX_LINES_PER_TRANSACTION_DESCRIPTION = 10

        // Добавляем новый паттерн для комбинированных строк, содержащих дату, описание и сумму
        private val combinedTransactionRegex = Regex("""^(\d{2}\.\d{2}\.\d{4})\s+(\d{2}:\d{2}:\d{2})\s+(\d+)\s+(.+?)\s+([+\-])\s*(\d[\d\s.,]*[\d])(?:\s+([A-Z]{3}))?$""")
    }

    private suspend fun extractTextFromPdf(uri: Uri): String = withContext(Dispatchers.IO) {
        Timber.d("Ozon extractTextFromPdf: Начало извлечения текста из URI: $uri")
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                inputStream.use { stream ->
                    PDDocument.load(stream).use { document ->
                        val stripper = PDFTextStripper()
                        val text = stripper.getText(document)
                        Timber.i("Ozon extractTextFromPdf: Текст успешно извлечен. Длина: ${text.length}")
                        return@withContext text
                    }
                }
            } else {
                Timber.w("Ozon extractTextFromPdf: Не удалось открыть InputStream для PDF: $uri")
                return@withContext ""
            }
        } catch (e: Exception) {
            Timber.e(e, "Ozon extractTextFromPdf: Ошибка при извлечении текста из PDF.")
            throw IOException("Ошибка при извлечении текста из PDF: ${e.localizedMessage}", e)
        }
    }

    override fun isValidFormat(reader: BufferedReader): Boolean {
        Timber.d("Ozon isValidFormat: Начало проверки формата PDF для Ozon Банка...")
        val headerLines = mutableListOf<String>()
        var linesReadCount = 0
        try {
            reader.mark(8192)
            var currentLineText: String?
            while (linesReadCount < MAX_VALIDATION_LINES) {
                currentLineText = reader.readLine()
                if (currentLineText != null) {
                    headerLines.add(currentLineText.replace("\u0000", "").trim())
                    linesReadCount++
                } else {
                    break // Конец файла
                }
            }
            reader.reset()
        } catch (e: Exception) {
            Timber.e(e, "Ozon isValidFormat: Ошибка при чтении строк для валидации.")
            return false
        }

        if (headerLines.isEmpty()) {
            Timber.w("Ozon isValidFormat: Не удалось прочитать ни одной строки для валидации.")
            return false
        }

        val content = headerLines.joinToString("\n")
        Timber.v("Ozon isValidFormat: Анализируемый контент (первые $linesReadCount строк):\n$content")

        val hasBankIndicator = OZON_BANK_INDICATORS.any { content.contains(it, ignoreCase = true) }
        val hasStatementTitle = OZON_STATEMENT_TITLES.any { content.contains(it, ignoreCase = true) }
        val hasSpecificTableHeader = headerLines.any { it.contains(OZON_TABLE_HEADER_MARKER, ignoreCase = true) }

        Timber.d("Ozon isValidFormat: Индикатор банка: $hasBankIndicator, Заголовок: $hasStatementTitle, Маркер таблицы: $hasSpecificTableHeader")
        return hasBankIndicator && hasStatementTitle && hasSpecificTableHeader
    }

    override fun skipHeaders(reader: BufferedReader) {
        Timber.d("Ozon skipHeaders: Начало пропуска заголовков...")
        var linesSkipped = 0
        var line: String?
        var tableHeaderMarkerFound = false

        while (linesSkipped < MAX_HEADER_SKIP_LINES) {
            line = reader.readLine()?.replace("\u0000", "")?.trim()
            if (line == null) {
                Timber.w("Ozon skipHeaders: Достигнут конец файла перед нахождением маркера таблицы.")
                throw IOException("Не удалось найти заголовок таблицы '${OZON_TABLE_HEADER_MARKER}'.")
            }
            linesSkipped++
            Timber.v("Ozon skipHeaders: (Поиск маркера) Строка $linesSkipped: '$line'")
            if (line.contains(OZON_TABLE_HEADER_MARKER, ignoreCase = true)) {
                Timber.i("Ozon skipHeaders: Найден маркер таблицы на строке $linesSkipped.")
                tableHeaderMarkerFound = true
                return
            }
        }
        if (!tableHeaderMarkerFound) {
            Timber.e("Ozon skipHeaders: Маркер таблицы не найден после $linesSkipped строк.")
            throw IOException("Заголовок таблицы не найден в пределах $MAX_HEADER_SKIP_LINES строк.")
        }
    }
    
    override fun shouldSkipLine(line: String): Boolean {
        val trimmedLine = line.trim()
        if (trimmedLine.isBlank()) {
            Timber.v("Ozon shouldSkipLine: ПРОПУСК (пустая): '$line'")
            return true
        }
        if (IGNORE_PATTERNS.any { it.containsMatchIn(trimmedLine) }) {
            Timber.v("Ozon shouldSkipLine: ПРОПУСК (по шаблону игнорирования): '$line'")
            return true
        }
        if (trimmedLine.equals(OZON_TABLE_HEADER_MARKER, ignoreCase = true)) {
            Timber.v("Ozon shouldSkipLine: ПРОПУСК (повтор заголовка таблицы): '$line'")
            return true
        }
        return false
    }

    override fun parseLine(line: String): Transaction? {
        val trimmedLine = line.replace("\u0000", "").trim()
        Timber.d("Ozon parseLine: Обработка строки: '$trimmedLine'")

        if (shouldSkipLine(trimmedLine)) return null
        
        // Сначала проверяем комбинированный формат (дата + описание + сумма в одной строке)
        combinedTransactionRegex.find(trimmedLine)?.let { match ->
            Timber.d("Ozon parseLine: Обнаружена комбинированная строка с датой, описанием и суммой")
            
            val dateStrRaw = match.groupValues[1]
            val timeStr = match.groupValues[2]
            val docNumber = match.groupValues[3]
            val description = match.groupValues[4].trim()
            val sign = match.groupValues[5]
            val amountStr = match.groupValues[6].replace("\\s".toRegex(), "").replace(",", ".")
            val currencyStr = match.groupValues.getOrNull(7)
            
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            
            try {
                val date = dateFormat.parse("$dateStrRaw $timeStr") ?: throw ParseException("Не удалось распарсить дату", 0)
                var amount = amountStr.toDouble()
                val isExpense = (sign == "-")
                var currency = Currency.RUB
                
                if (currencyStr != null && currencyStr.isNotBlank()) {
                    try {
                        currency = Currency.valueOf(currencyStr.uppercase(Locale.ROOT))
                    } catch (e: IllegalArgumentException) {
                        Timber.w("Ozon parseLine: Неизвестная валюта '$currencyStr', используем RUB")
                    }
                }
                
                Timber.i("Ozon parseLine: Создана транзакция из комбинированной строки: Дата='$date', Документ='$docNumber', Сумма=$amount $currency, Расход=$isExpense")
                
                return Transaction(
                    date = date,
                    title = description,
                    amount = Money(amount, currency),
                    isExpense = isExpense,
                    source = transactionSource,
                    sourceColor = 0,
                    category = detectCategory(description),
                    note = "Документ №$docNumber"
                )
            } catch (e: Exception) {
                Timber.e(e, "Ozon parseLine: Ошибка при обработке комбинированной строки: '$trimmedLine'")
                // Продолжаем обработку стандартным методом
            }
        }

        // Обычный формат - попытка распознать начало новой транзакции (Дата, Время, Документ)
        ozonDateDocRegex.find(trimmedLine)?.let { match ->
            if (currentPartialTransaction != null && !currentPartialTransaction!!.isComplete) {
                Timber.w("Ozon parseLine: Начало новой транзакции, но предыдущая ($currentPartialTransaction) не была завершена. Сбрасываем")
                currentPartialTransaction?.clear()
            }
            
            currentPartialTransaction = PartialTransaction() // Создаем или очищаем для новой транзакции

            val dateStrRaw = match.groupValues[1]
            val timeStr = match.groupValues[2]
            val docNumber = match.groupValues[3]
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            try {
                currentPartialTransaction!!.date = dateFormat.parse("$dateStrRaw $timeStr")
                currentPartialTransaction!!.documentNumber = docNumber
                Timber.i("Ozon parseLine: Новая PartialTx: Дата='${currentPartialTransaction!!.date}', Документ='${currentPartialTransaction!!.documentNumber}'")
                return null // Еще не готовая транзакция
            } catch (e: ParseException) {
                Timber.e(e, "Ozon parseLine: Ошибка парсинга даты/времени: '$trimmedLine'")
                currentPartialTransaction?.clear()
                return null
            }
        }

        // Если не начало новой транзакции, и у нас есть активная частичная транзакция
        val ptx = currentPartialTransaction
        if (ptx != null && !ptx.isComplete) {
            // Попытка распознать строку с суммой
            ozonAmountRegex.find(trimmedLine)?.let { match ->
                Timber.d("Ozon parseLine: Обнаружена строка с суммой: '$trimmedLine' для PartialTx Doc: ${ptx.documentNumber}")
                val sign = match.groupValues[1]
                val amountStr = match.groupValues[2].replace("\\s".toRegex(), "").replace(",", ".")
                val currencyStr = match.groupValues.getOrNull(3)

                try {
                    ptx.amount = amountStr.toDouble()
                    ptx.isExpense = (sign == "-")
                    if (currencyStr != null && currencyStr.isNotBlank()) {
                        try {
                            ptx.currency = Currency.valueOf(currencyStr.uppercase(Locale.ROOT))
                        } catch (e: IllegalArgumentException) {
                            Timber.w("Ozon parseLine: Неизвестная валюта '$currencyStr', используем RUB")
                            // Оставляем RUB по умолчанию
                        }
                    }
                    ptx.isComplete = true // Основные данные собраны
                    Timber.i("Ozon parseLine: PartialTx сумма: ${ptx.amount} ${ptx.currency}, Расход=${ptx.isExpense}. PartialTx завершена.")
                    
                    // Теперь, когда транзакция полная, создаем и возвращаем ее
                    if (ptx.isValidForFinalization()) {
                        val transaction = Transaction(
                            date = ptx.date!!,
                            title = ptx.buildDescription().ifEmpty { "Операция №${ptx.documentNumber}" },
                            amount = Money(ptx.amount!!, ptx.currency),
                            isExpense = ptx.isExpense!!,
                            source = transactionSource,
                            sourceColor = 0,
                            category = detectCategory(ptx.buildDescription()),
                            note = "Документ №${ptx.documentNumber}"
                        )
                        currentPartialTransaction = null // Сбрасываем для следующей
                        return transaction
                    } else {
                        Timber.w("Ozon parseLine: PartialTx помечена isComplete, но невалидна: $ptx. Сбрасывается.")
                        currentPartialTransaction?.clear()
                        return null
                    }
                } catch (e: NumberFormatException) {
                    Timber.e(e, "Ozon parseLine: Ошибка парсинга суммы: '$trimmedLine'")
                    // Сумма не распознана, возможно это описание
                }
            }

            // Если это не сумма и транзакция не завершена, считаем это строкой описания
            if (!ptx.isComplete && ptx.date != null && ptx.documentNumber != null) {
                if (ptx.linesProcessedForCurrentTx < MAX_LINES_PER_TRANSACTION_DESCRIPTION) {
                    // Проверка, что строка не похожа на дату/сумму
                    val isAmountLine = ozonAmountRegex.find(trimmedLine) != null
                    val isDateLine = trimmedLine.length >= 10 && ozonDateDocRegex.find(trimmedLine) != null

                    if (!isAmountLine && !isDateLine) {
                        ptx.descriptionLines.add(trimmedLine)
                        ptx.linesProcessedForCurrentTx++
                        Timber.d("Ozon parseLine: Добавлено к описанию: '$trimmedLine'. Строк: ${ptx.linesProcessedForCurrentTx}")
                    } else {
                        Timber.d("Ozon parseLine: Строка '$trimmedLine' похожа на сумму/дату, не добавлена в описание.")
                    }
                } else {
                    Timber.w("Ozon parseLine: Превышено макс. строк для описания Doc ${ptx.documentNumber}.")
                }
            }
        } else if (trimmedLine.isNotEmpty() && ptx == null) {
            Timber.d("Ozon parseLine: Строка не распознана или нет активной PartialTx: '$trimmedLine'")
        }
        return null // Транзакция еще не готова или это была часть описания
    }

    override fun importTransactions(uri: Uri, progressCallback: ImportProgressCallback): Flow<ImportResult> = flow {
        emit(ImportResult.Progress(0, 100, "Начало импорта Ozon Банка (PDF)"))
        Timber.i("Ozon importTransactions: Начало импорта из URI: $uri")
        currentPartialTransaction = null
        val collectedTransactions = mutableListOf<Transaction>()

        try {
            val extractedText = extractTextFromPdf(uri)
            if (extractedText.isBlank()) {
                Timber.e("Ozon importTransactions: Извлеченный текст из PDF пуст.")
                emit(ImportResult.Error(message = "Не удалось извлечь текст из PDF или файл пуст."))
                return@flow
            }
            emit(ImportResult.Progress(5, 100, "Текст извлечен, проверка формата..."))

            BufferedReader(StringReader(extractedText)).use { validationReader ->
                if (!isValidFormat(validationReader)) {
                    Timber.e("Ozon importTransactions: Файл не прошел валидацию как выписка Ozon Банка.")
                    emit(ImportResult.Error(message = "Файл не является выпиской Ozon Банка или формат не поддерживается."))
                    return@flow
                }
            }
            Timber.i("Ozon importTransactions: Формат файла успешно валидирован.")
            emit(ImportResult.Progress(10, 100, "Формат проверен, пропуск заголовков..."))

            BufferedReader(StringReader(extractedText)).use { contentReader ->
                skipHeaders(contentReader)
                Timber.i("Ozon importTransactions: Заголовки успешно пропущены.")
                emit(ImportResult.Progress(15, 100, "Заголовки пропущены, обработка транзакций..."))

                var linesProcessedAfterHeader = 0
                val totalLinesEstimate = extractedText.lines().size.coerceAtLeast(1)

                var line: String? = contentReader.readLine()
                while (line != null) {
                    linesProcessedAfterHeader++
                    val currentProgress = 15 + (linesProcessedAfterHeader * 70 / totalLinesEstimate).coerceAtMost(70)
                    emit(ImportResult.Progress(currentProgress, 100, "Обработка строки $linesProcessedAfterHeader / ~$totalLinesEstimate"))

                    parseLine(line)?.let { transaction ->
                        Timber.i("Ozon importTransactions: Собрана транзакция: ${transaction.title}")
                        collectedTransactions.add(transaction)
                    }
                    line = contentReader.readLine()
                }
                
                currentPartialTransaction?.let { ptx ->
                    if (ptx.isComplete && ptx.isValidForFinalization()) {
                        Timber.w("Ozon importTransactions: Обнаружена завершенная, но не добавленная PartialTx. Финализация.")
                        val transaction = Transaction(
                            date = ptx.date!!,
                            title = ptx.buildDescription().ifEmpty { "Операция №${ptx.documentNumber}" },
                            amount = Money(ptx.amount!!, ptx.currency),
                            isExpense = ptx.isExpense!!,
                            source = transactionSource,
                            sourceColor = 0,
                            category = detectCategory(ptx.buildDescription()),
                            note = "Документ №${ptx.documentNumber}"
                        )
                        collectedTransactions.add(transaction)
                    } else if (ptx.date != null) {
                        Timber.w("Ozon importTransactions: Обнаружена незавершенная PartialTx в конце файла.")
                    }
                }
                currentPartialTransaction = null

                if (collectedTransactions.isEmpty()) {
                    Timber.w("Ozon importTransactions: Транзакции не найдены.")
                    emit(ImportResult.Error(message = "Транзакции не найдены в файле."))
                } else {
                    // Добавляем транзакции в базу данных
                    emit(ImportResult.Progress(85, 100, "Сохранение ${collectedTransactions.size} транзакций..."))
                    Timber.i("Ozon importTransactions: Начинаем сохранение ${collectedTransactions.size} транзакций в базу данных")
                    
                    var savedCount = 0
                    var errorCount = 0
                    
                    collectedTransactions.forEach { transaction ->
                        try {
                            Timber.d("Ozon importTransactions: Сохранение транзакции: ${transaction.title}, сумма: ${transaction.amount}")
                            transactionRepository.addTransaction(transaction)
                            savedCount++
                            Timber.d("Ozon importTransactions: Транзакция успешно сохранена (${savedCount}/${collectedTransactions.size})")
                        } catch (e: Exception) {
                            errorCount++
                            Timber.e(e, "Ozon importTransactions: Ошибка при сохранении транзакции: ${transaction.title}")
                        }
                    }
                    
                    Timber.i("Ozon importTransactions: Импорт завершен. Сохранено: $savedCount, Ошибок: $errorCount")
                    emit(ImportResult.Success(savedCount, errorCount))
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "Ozon importTransactions: Ошибка ввода-вывода.")
            emit(ImportResult.Error(exception = e, message = "Ошибка чтения файла: ${e.localizedMessage}"))
        } catch (e: Exception) {
            Timber.e(e, "Ozon importTransactions: Непредвиденная ошибка.")
            emit(ImportResult.Error(exception = e, message = "Неизвестная ошибка: ${e.localizedMessage}"))
        }
    }

    fun detectCategory(description: String): String {
        Timber.d("OzonPdfImportUseCase detectCategory: Описание: '$description'")
        return when {
            description.contains("Ozon", ignoreCase = true) || description.contains("Озон", ignoreCase = true) -> "Покупки Ozon"
            description.contains("СБП", ignoreCase = true) -> "Переводы СБП"
            description.contains("кафе", ignoreCase = true) || description.contains("ресторан", ignoreCase = true) -> "Кафе и рестораны"
            description.contains("Пятёрочка", ignoreCase = true) || description.contains("PYATEROCHKA", ignoreCase = true) -> "Супермаркеты"
            description.contains("HH CAREER SERVICE", ignoreCase = true) -> "Сервисы HH"
            else -> "Без категории"
        }
    }
} 