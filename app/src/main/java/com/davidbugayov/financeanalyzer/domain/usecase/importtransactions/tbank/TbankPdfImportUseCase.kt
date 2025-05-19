package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.tbank

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
import java.util.regex.Pattern

class TbankPdfImportUseCase(
    context: Context,
    transactionRepository: TransactionRepository
) : BankImportUseCase(transactionRepository, context) {

    override val bankName: String = "Тинькофф Банк (PDF)"
    private val transactionSource: String = "Тинькофф"

    private var currentPartialTransaction: PartialTransaction? = null

    private data class PartialTransaction(
        var date: Date? = null,
        var documentNumber: String? = null, // Может использоваться время операции
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

        fun isValidForFinalization(): Boolean = date != null && amount != null && isExpense != null

        fun buildDescription(): String = descriptionLines.joinToString(" ").trim().replace("\\s+".toRegex(), " ")
    }

    companion object {
        private val TBANK_INDICATORS = listOf(
            "TINKOFF", "ТИНЬКОФФ", "Тинькофф Банк", "Тинькофф", 
            "ТБАНК", "TBANK", "АО «ТБАНК»", "АО «Тинькофф Банк»"
        )
        private val TBANK_STATEMENT_TITLES = listOf(
            "Выписка по счетам", "Выписка по карте", "Операции по счету", "История операций",
            "Справка о движении средств", "Движение средств", "Платежное поручение",
            "С движением средств", "Справка о движении"
        )

        private const val MAX_VALIDATION_LINES = 40
        private const val MAX_HEADER_SKIP_LINES = 300

        // Паттерны для распознавания строк с датой и временем (формат dd.MM.yyyy, HH:mm)
        private val tbankDateRegex = Regex("""^(\d{2}\.\d{2}\.\d{4})$""")
        private val tbankTimeRegex = Regex("""^(\d{2}:\d{2})$""")
        
        // Паттерн для распознавания строк с суммой
        private val tbankAmountWithDescRegex = 
            Regex("""([+\-])?[\s]*(\d[\d\s.,]*[\d])[\s]*(?:₽|P|Р)[\s]+([+\-])?[\s]*(\d[\d\s.,]*[\d])[\s]*(?:₽|P|Р)[\s]+(.+)""")
        private val tbankSimpleAmountRegex = 
            Regex("""([+\-])?[\s]*(\d[\d\s.,]*[\d])[\s]*(?:₽|P|Р)""")

        private val IGNORE_PATTERNS = listOf(
            Regex("^Итого:", RegexOption.IGNORE_CASE),
            Regex("^Баланс на начало периода", RegexOption.IGNORE_CASE),
            Regex("^Баланс на конец периода", RegexOption.IGNORE_CASE),
            Regex("^Выписка сформирована:", RegexOption.IGNORE_CASE),
            Regex("^Пополнения:", RegexOption.IGNORE_CASE),
            Regex("^Расходы:", RegexOption.IGNORE_CASE),
            Regex("^С уважением,", RegexOption.IGNORE_CASE),
            Regex("^Руководитель", RegexOption.IGNORE_CASE),
            Regex("^АО «Тинькофф Банк»", RegexOption.IGNORE_CASE),
            Regex("^БИК", RegexOption.IGNORE_CASE),
            Regex("^Страница \\d+ из \\d+", RegexOption.IGNORE_CASE)
        )
        
        private const val MAX_LINES_PER_TRANSACTION_DESCRIPTION = 10
    }

    private suspend fun extractTextFromPdf(uri: Uri): String = withContext(Dispatchers.IO) {
        Timber.d("$bankName extractTextFromPdf: Начало извлечения текста из URI: $uri")
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                inputStream.use { stream ->
                    PDDocument.load(stream).use { document ->
                        val stripper = PDFTextStripper()
                        val text = stripper.getText(document)
                        Timber.i("$bankName extractTextFromPdf: Текст успешно извлечен. Длина: ${text.length}, Первые 100 символов: ${text.take(100)}")
                        val lineCount = text.lines().size
                        Timber.d("$bankName extractTextFromPdf: Общее количество строк в извлеченном тексте: $lineCount")
                        return@withContext text
                    }
                }
            } else {
                Timber.w("$bankName extractTextFromPdf: Не удалось открыть InputStream для PDF: $uri")
                return@withContext ""
            }
        } catch (e: Exception) {
            Timber.e(e, "$bankName extractTextFromPdf: Ошибка при извлечении текста из PDF.")
            throw IOException("Ошибка при извлечении текста из PDF для $bankName: ${e.localizedMessage}", e)
        }
    }

    override fun isValidFormat(reader: BufferedReader): Boolean {
        Timber.d("$bankName isValidFormat: Начало проверки формата PDF для $bankName...")
        val headerLines = mutableListOf<String>()
        var linesReadCount = 0
        try {
            reader.mark(16384)
            var currentLineText: String?
            while (linesReadCount < MAX_VALIDATION_LINES) {
                currentLineText = reader.readLine()
                if (currentLineText != null) {
                    val cleanLine = currentLineText.replace("\u0000", "").trim()
                    headerLines.add(cleanLine)
                    Timber.v("$bankName isValidFormat: Строка ${linesReadCount+1}: '$cleanLine'")
                    linesReadCount++
                } else {
                    Timber.d("$bankName isValidFormat: Достигнут конец файла после $linesReadCount строк")
                    break 
                }
            }
            reader.reset()
        } catch (e: Exception) {
            Timber.e(e, "$bankName isValidFormat: Ошибка при чтении строк для валидации. linesReadCount=$linesReadCount")
            return false
        }

        if (headerLines.isEmpty()) {
            Timber.w("$bankName isValidFormat: Не удалось прочитать ни одной строки для валидации.")
            return false
        }

        val content = headerLines.joinToString("\n")
        
        // Проверяем наличие индикаторов банка
        val hasBankIndicator = TBANK_INDICATORS.any { content.contains(it, ignoreCase = true) }
        Timber.d("$bankName isValidFormat: Индикатор банка: $hasBankIndicator")
        
        // Проверяем наличие заголовков выписки
        val hasStatementTitle = TBANK_STATEMENT_TITLES.any { content.contains(it, ignoreCase = true) }
        Timber.d("$bankName isValidFormat: Заголовок выписки: $hasStatementTitle")
        
        // Проверяем наличие типичных для Тинькофф шаблонов: дат и сумм
        var hasDateFormat = false
        var hasTimeFormat = false
        var hasAmountFormat = false
        
        for (line in headerLines) {
            if (tbankDateRegex.find(line) != null) {
                hasDateFormat = true
                Timber.d("$bankName isValidFormat: Обнаружен формат даты в строке: '$line'")
            }
            if (tbankTimeRegex.find(line) != null) {
                hasTimeFormat = true
                Timber.d("$bankName isValidFormat: Обнаружен формат времени в строке: '$line'")
            }
            if (tbankSimpleAmountRegex.find(line) != null) {
                hasAmountFormat = true
                Timber.d("$bankName isValidFormat: Обнаружен формат суммы в строке: '$line'")
            }
        }
        
        // Если найдены индикаторы и заголовок выписки, или характерные шаблоны данных - считаем формат валидным
        val isValid = hasBankIndicator && (hasStatementTitle || (hasDateFormat && hasAmountFormat))
        
        Timber.i("$bankName isValidFormat: Итог валидации: $isValid (Банк: $hasBankIndicator, Заголовок: $hasStatementTitle, Даты: $hasDateFormat, Время: $hasTimeFormat, Суммы: $hasAmountFormat)")
        
        return isValid
    }

    override fun skipHeaders(reader: BufferedReader) {
        Timber.d("$bankName skipHeaders: Начало пропуска заголовков...")
        var linesSkipped = 0
        var line: String?
        
        reader.mark(32768)
        
        while (linesSkipped < MAX_HEADER_SKIP_LINES) {
            line = reader.readLine()?.replace("\u0000", "")?.trim()
            if (line == null) {
                Timber.w("$bankName skipHeaders: Достигнут конец файла перед нахождением начала транзакций после $linesSkipped строк.")
                reader.reset()
                return 
            }
            linesSkipped++
            
            Timber.v("$bankName skipHeaders: Анализ строки $linesSkipped: '$line'")
            
            // Ищем первую строку с датой или суммой - вероятное начало транзакций
            if (tbankDateRegex.find(line) != null || tbankSimpleAmountRegex.find(line) != null) {
                Timber.i("$bankName skipHeaders: Найдена строка с датой/суммой на строке $linesSkipped, возможно это начало транзакций: '$line'")
                reader.reset() // Сбрасываем к началу, чтобы не пропустить транзакцию
                for (i in 0 until linesSkipped - 1) {
                    reader.readLine() // Пропускаем строки до найденной
                }
                return
            }
        }
        
        Timber.w("$bankName skipHeaders: Начало транзакций не найдено после $linesSkipped строк.")
        reader.reset() // Возвращаемся к началу
    }
    
    override fun shouldSkipLine(line: String): Boolean {
        val trimmedLine = line.trim()
        if (trimmedLine.isBlank()) {
            Timber.v("$bankName shouldSkipLine: ПРОПУСК (пустая): '$line'")
            return true
        }
        if (IGNORE_PATTERNS.any { it.containsMatchIn(trimmedLine) }) {
            Timber.v("$bankName shouldSkipLine: ПРОПУСК (по шаблону игнорирования): '$line'")
            return true
        }
        return false
    }
    
    override fun parseLine(line: String): Transaction? {
        val trimmedLine = line.replace("\u0000", "").trim()
        Timber.d("$bankName parseLine: Обработка строки: '$trimmedLine'")

        if (shouldSkipLine(trimmedLine)) return null
        
        val ptx = currentPartialTransaction ?: PartialTransaction().also { currentPartialTransaction = it }
        
        // Проверяем, содержит ли строка дату (формат: dd.MM.yyyy)
        val dateMatch = tbankDateRegex.find(trimmedLine)
        if (dateMatch != null) {
            val dateStr = dateMatch.groupValues[1]
            
            // Если у нас уже есть частичная транзакция с датой и она завершена, финализируем ее
            if (ptx.date != null && ptx.amount != null && ptx.isComplete) {
                val transaction = finalizeTransaction(ptx)
                ptx.clear()
                ptx.date = parseTbankDate(dateStr)
                Timber.i("$bankName parseLine: Новая дата транзакции: $dateStr")
                return transaction
            }
            
            // Если у нас уже есть частичная транзакция с датой, но она не завершена,
            // это начало новой транзакции, сбрасываем предыдущую
            if (ptx.date != null) {
                Timber.w("$bankName parseLine: Начало новой транзакции с датой, предыдущая не завершена: $ptx")
                ptx.clear()
            }
            
            ptx.date = parseTbankDate(dateStr)
            Timber.i("$bankName parseLine: Установлена дата: ${ptx.date}")
            return null
        }
        
        // Проверяем, содержит ли строка время (формат: HH:mm)
        val timeMatch = tbankTimeRegex.find(trimmedLine)
        if (timeMatch != null && ptx.date != null && ptx.documentNumber == null) {
            val timeStr = timeMatch.groupValues[1]
            ptx.documentNumber = timeStr
            Timber.i("$bankName parseLine: Установлено время: $timeStr")
            return null
        }
        
        // Проверяем, содержит ли строка сумму с описанием
        val amountWithDescMatch = tbankAmountWithDescRegex.find(trimmedLine)
        if (amountWithDescMatch != null && ptx.date != null) {
            val sign = amountWithDescMatch.groupValues[1].ifEmpty { amountWithDescMatch.groupValues[3] }
            val amountStr = amountWithDescMatch.groupValues[2].replace("\\s".toRegex(), "").replace(",", ".")
            val description = amountWithDescMatch.groupValues[5].trim()
            
            try {
                val amountValue = amountStr.toDouble()
                ptx.amount = amountValue
                ptx.descriptionLines.add(description)
                
                // Определяем расход/доход на основе знака и описания
                val isExpenseBySign = sign == "-"
                val isExpenseByDescription = !description.contains("Пополнение", ignoreCase = true) &&
                                           !description.contains("Перевод от", ignoreCase = true) &&
                                           !description.contains("Возврат", ignoreCase = true)
                
                ptx.isExpense = isExpenseBySign || (sign.isEmpty() && isExpenseByDescription)
                ptx.currency = Currency.RUB
                ptx.isComplete = true
                
                Timber.i("$bankName parseLine: Транзакция готова. Сумма: ${ptx.amount}, Описание: $description, isExpense: ${ptx.isExpense}")
                
                // Финализируем и возвращаем транзакцию
                val transaction = finalizeTransaction(ptx)
                ptx.clear()
                return transaction
            } catch (e: Exception) {
                Timber.e(e, "$bankName parseLine: Ошибка при обработке суммы: $amountStr")
            }
            return null
        }
        
        // Проверяем, содержит ли строка просто сумму
        val simpleAmountMatch = tbankSimpleAmountRegex.find(trimmedLine)
        if (simpleAmountMatch != null && ptx.date != null) {
            try {
                val sign = simpleAmountMatch.groupValues[1]
                val amountStr = simpleAmountMatch.groupValues[2].replace("\\s".toRegex(), "").replace(",", ".")
                val amountValue = amountStr.toDouble()
                
                ptx.amount = amountValue
                ptx.isExpense = sign == "-" || (sign.isEmpty() && !ptx.buildDescription().contains("Пополнение", ignoreCase = true))
                ptx.currency = Currency.RUB
                
                // Добавляем оставшуюся часть строки как описание
                val remainingText = trimmedLine.substring(simpleAmountMatch.range.last + 1).trim()
                if (remainingText.isNotEmpty()) {
                    ptx.descriptionLines.add(remainingText)
                }
                
                ptx.isComplete = true
                Timber.i("$bankName parseLine: Найдена сумма: $amountValue, isExpense: ${ptx.isExpense}")
                
                // Если транзакция валидна, финализируем и возвращаем
                if (ptx.isValidForFinalization()) {
                    val transaction = finalizeTransaction(ptx)
                    ptx.clear()
                    return transaction
                }
            } catch (e: Exception) {
                Timber.e(e, "$bankName parseLine: Ошибка при обработке суммы из строки: $trimmedLine")
            }
            return null
        }
        
        // Если у нас уже есть дата, добавляем текущую строку к описанию
        if (ptx.date != null) {
            ptx.descriptionLines.add(trimmedLine)
            Timber.d("$bankName parseLine: Добавлена строка к описанию: '$trimmedLine'")
            
            // Если у нас достаточно описания и есть сумма, финализируем транзакцию
            if (ptx.isComplete && ptx.isValidForFinalization() && 
                ptx.descriptionLines.size >= 2) { // Предполагаем, что при хотя бы 2-х строках описания транзакция готова
                Timber.i("$bankName parseLine: Транзакция завершена после добавления описания")
                val transaction = finalizeTransaction(ptx)
                ptx.clear()
                return transaction
            }
        }
        
        return null
    }

    private fun finalizeTransaction(ptx: PartialTransaction): Transaction {
        val description = ptx.buildDescription().ifEmpty { "Операция от ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(ptx.date!!)}" }
        val category = detectCategory(description)
        
        Timber.i("$bankName finalizeTransaction: Финализирована транзакция: Дата='${ptx.date}', Описание='$description', Сумма=${ptx.amount} ${ptx.currency}, Расход=${ptx.isExpense}, Категория='$category'")
        
        return Transaction(
            date = ptx.date!!,
            title = description,
            amount = Money(ptx.amount!!, ptx.currency),
            isExpense = ptx.isExpense!!,
            source = transactionSource, 
            sourceColor = 0,
            category = category,
            note = if (ptx.documentNumber != null) "Время: ${ptx.documentNumber}" else ""
        )
    }

    private fun parseTbankDate(dateStr: String): Date? {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        try {
            return format.parse(dateStr)
        } catch (e: ParseException) {
            Timber.e(e, "$bankName parseTbankDate: Не удалось распарсить дату: '$dateStr'")
            return null
        }
    }

    override fun importTransactions(uri: Uri, progressCallback: ImportProgressCallback): Flow<ImportResult> = flow {
        emit(ImportResult.Progress(0, 100, "Начало импорта $bankName"))
        Timber.i("$bankName importTransactions: Начало импорта из URI: $uri")
        currentPartialTransaction = null
        val collectedTransactions = mutableListOf<Transaction>()

        try {
            val extractedText = extractTextFromPdf(uri)
            if (extractedText.isBlank()) {
                Timber.e("$bankName importTransactions: Извлеченный текст из PDF пуст.")
                emit(ImportResult.Error(message = "Не удалось извлечь текст из PDF или файл пуст для $bankName."))
                return@flow
            }
            emit(ImportResult.Progress(5, 100, "Текст извлечен, проверка формата..."))
            Timber.d("$bankName importTransactions: Извлеченный текст, первые 500 символов:\n${extractedText.take(500)}...")

            BufferedReader(StringReader(extractedText)).use { validationReader ->
                if (!isValidFormat(validationReader)) {
                    Timber.e("$bankName importTransactions: Файл не прошел валидацию как выписка $bankName.")
                    emit(ImportResult.Error(message = "Файл не является выпиской $bankName или формат не поддерживается."))
                    return@flow
                }
            }
            Timber.i("$bankName importTransactions: Формат файла успешно валидирован.")
            emit(ImportResult.Progress(10, 100, "Формат проверен, пропуск заголовков..."))

            BufferedReader(StringReader(extractedText)).use { contentReader ->
                skipHeaders(contentReader) // Пропускаем заголовки
                Timber.i("$bankName importTransactions: Заголовки пропущены, начинаем обработку транзакций.")
                emit(ImportResult.Progress(15, 100, "Обработка транзакций..."))

                var linesProcessedAfterHeader = 0
                val totalLinesEstimate = extractedText.lines().size.coerceAtLeast(1)
                Timber.d("$bankName importTransactions: Общее количество строк в файле: $totalLinesEstimate")
                
                var lineContent: String? = contentReader.readLine()
                while (lineContent != null) {
                    linesProcessedAfterHeader++
                    
                    if (linesProcessedAfterHeader % 100 == 0) {
                        Timber.d("$bankName importTransactions: Обработано $linesProcessedAfterHeader строк, найдено ${collectedTransactions.size} транзакций")
                    }
                    
                    val currentProgress = 15 + (linesProcessedAfterHeader * 70 / totalLinesEstimate).coerceAtMost(70)
                    emit(ImportResult.Progress(currentProgress, 100, "Обработка строки $linesProcessedAfterHeader / ~$totalLinesEstimate"))

                    parseLine(lineContent)?.let { transaction ->
                        Timber.i("$bankName importTransactions: Собрана транзакция #${collectedTransactions.size + 1}: ${transaction.title}, Сумма: ${transaction.amount.amount} ${transaction.amount.currency}, Дата: ${transaction.date}")
                        collectedTransactions.add(transaction)
                    }
                    lineContent = contentReader.readLine()
                }
                
                // Финализация последней транзакции, если осталась
                currentPartialTransaction?.let { ptx ->
                    if (ptx.isComplete && ptx.isValidForFinalization()) {
                        Timber.i("$bankName importTransactions: Финализирована последняя транзакция")
                        val transaction = finalizeTransaction(ptx)
                        collectedTransactions.add(transaction)
                    }
                }
                currentPartialTransaction = null

                if (collectedTransactions.isEmpty()) {
                    Timber.w("$bankName importTransactions: Транзакции не найдены после обработки $linesProcessedAfterHeader строк.")
                    emit(ImportResult.Error(message = "Не найдено ни одной транзакции в файле для $bankName."))
                } else {
                    emit(ImportResult.Progress(85, 100, "Сохранение ${collectedTransactions.size} транзакций..."))
                    Timber.i("$bankName importTransactions: Найдено ${collectedTransactions.size} транзакций. Начинаем сохранение в базу данных")
                    
                    var savedCount = 0
                    collectedTransactions.forEach { transaction ->
                        try {
                            transactionRepository.addTransaction(transaction)
                            savedCount++
                        } catch (e: Exception) {
                            Timber.e(e, "$bankName importTransactions: Ошибка при сохранении транзакции: ${transaction.title}")
                        }
                    }
                    
                    Timber.i("$bankName importTransactions: Импорт завершен. Сохранено: $savedCount из ${collectedTransactions.size}")
                    emit(ImportResult.Success(savedCount, collectedTransactions.size - savedCount))
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "$bankName importTransactions: Ошибка ввода-вывода.")
            emit(ImportResult.Error(exception = e, message = "Ошибка чтения файла $bankName: ${e.localizedMessage}"))
        } catch (e: Exception) {
            Timber.e(e, "$bankName importTransactions: Непредвиденная ошибка.")
            emit(ImportResult.Error(exception = e, message = "Неизвестная ошибка при импорте $bankName: ${e.localizedMessage}"))
        }
    }

    fun detectCategory(description: String): String {
        // Приводим описание к нижнему регистру для упрощения проверки
        val lowerDesc = description.lowercase()
        
        return when {
            // Переводы и пополнения
            lowerDesc.contains("внешний перевод") -> "Переводы"
            lowerDesc.contains("внутренний перевод") -> "Переводы"
            lowerDesc.contains("пополнение. система быстрых платежей") -> "Пополнения"
            lowerDesc.contains("пополнение") || lowerDesc.contains("перевод от") -> "Пополнения"
            
            // Супермаркеты и продукты
            lowerDesc.contains("близкий_p_qr") || lowerDesc.contains("близкий") -> "Продукты"
            lowerDesc.contains("самбери") || lowerDesc.contains("самбери_p_qr") -> "Продукты"
            lowerDesc.contains("пятёрочка") || lowerDesc.contains("pyaterochka") -> "Продукты"
            lowerDesc.contains("магнит") || lowerDesc.contains("magnit") -> "Продукты"
            lowerDesc.contains("вкусвилл") || lowerDesc.contains("ашан") -> "Продукты"
            lowerDesc.contains("лента") && !lowerDesc.contains("интернет") -> "Продукты"
            lowerDesc.contains("метро") && !lowerDesc.contains("транспорт") -> "Продукты"
            lowerDesc.contains("магазин причал") -> "Продукты"
            
            // Кафе и рестораны
            lowerDesc.contains("кафе") || lowerDesc.contains("ресторан") -> "Кафе и рестораны"
            lowerDesc.contains("додо") || lowerDesc.contains("dodo") -> "Кафе и рестораны"
            lowerDesc.contains("бургер") || lowerDesc.contains("burger") -> "Кафе и рестораны"
            lowerDesc.contains("макдоналдс") || lowerDesc.contains("mcdonald") -> "Кафе и рестораны"
            lowerDesc.contains("кофе") || lowerDesc.contains("coffee") -> "Кафе и рестораны"
            
            // Транспорт и такси
            lowerDesc.contains("такси") || lowerDesc.contains("yandex.go") -> "Транспорт"
            lowerDesc.contains("uber") || lowerDesc.contains("яндекс.такси") -> "Транспорт"
            lowerDesc.contains("метрополитен") || lowerDesc.contains("metro") -> "Транспорт"
            lowerDesc.contains("автобус") || lowerDesc.contains("трамвай") -> "Транспорт"
            
            // Онлайн покупки
            lowerDesc.contains("ozon") || lowerDesc.contains("озон") -> "Покупки Ozon"
            lowerDesc.contains("яндекс.маркет") || lowerDesc.contains("yandex market") -> "Онлайн-покупки"
            lowerDesc.contains("wildberries") || lowerDesc.contains("вайлдберриз") -> "Онлайн-покупки"
            lowerDesc.contains("wb") && !lowerDesc.contains("web") -> "Онлайн-покупки"
            lowerDesc.contains("aliexpress") || lowerDesc.contains("али") -> "Онлайн-покупки"
            lowerDesc.contains("вайме") || lowerDesc.contains("vimemc") -> "Онлайн-покупки"
            
            // Автомобиль
            lowerDesc.contains("азс") || lowerDesc.contains("топливо") -> "Автомобиль"
            lowerDesc.contains("бензин") || lowerDesc.contains("автозаправ") -> "Автомобиль"
            lowerDesc.contains("парковк") || lowerDesc.contains("стоянк") -> "Автомобиль"
            
            // Аптека и здоровье
            lowerDesc.contains("аптека") || lowerDesc.contains("apteka") -> "Аптека"
            lowerDesc.contains("здоровье") || lowerDesc.contains("клиник") -> "Здоровье"
            lowerDesc.contains("врач") || lowerDesc.contains("доктор") -> "Здоровье"
            
            // Электроника и бытовая техника
            lowerDesc.contains("связной") || lowerDesc.contains("эльдорадо") -> "Электроника"
            lowerDesc.contains("мвидео") || lowerDesc.contains("mvideo") -> "Электроника"
            lowerDesc.contains("ситилинк") || lowerDesc.contains("citilink") -> "Электроника"
            lowerDesc.contains("dns") || lowerDesc.contains("днс") -> "Электроника"
            
            // Коммунальные платежи и связь
            lowerDesc.contains("жкх") || lowerDesc.contains("коммунал") -> "ЖКХ"
            lowerDesc.contains("связь") || lowerDesc.contains("мобильный") -> "Связь"
            lowerDesc.contains("мтс") || lowerDesc.contains("билайн") -> "Связь"
            lowerDesc.contains("мегафон") || lowerDesc.contains("tele2") -> "Связь"
            
            // Развлечения и подписки
            lowerDesc.contains("кино") || lowerDesc.contains("cinema") -> "Развлечения"
            lowerDesc.contains("подписк") || lowerDesc.contains("subscription") -> "Подписки"
            lowerDesc.contains("spotify") || lowerDesc.contains("netflix") -> "Подписки"
            lowerDesc.contains("okko") || lowerDesc.contains("кинопоиск") -> "Подписки"
            
            // Банковские услуги
            lowerDesc.contains("комиссия") || lowerDesc.contains("обслуживание") -> "Банковские услуги"
            lowerDesc.contains("процент") || lowerDesc.contains("interest") -> "Банковские услуги"
            
            // Если ничего не подошло
            else -> "Без категории"
        }
    }
} 