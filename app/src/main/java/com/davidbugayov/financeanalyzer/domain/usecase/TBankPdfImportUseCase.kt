package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.absoluteValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import timber.log.Timber

/**
 * Реализация импорта транзакций из PDF-выписки Т-Банка.
 * Поддерживает формат PDF-выписки из мобильного приложения Т-Банк (бывший Тинькофф).
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class TBankPdfImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context
) : BankImportUseCase(repository, context) {

    override val bankName: String = "Т-Банк"
    
    // Таймаут для операции парсинга PDF (в миллисекундах)
    private val PDF_PARSING_TIMEOUT = 30000L // 30 секунд
    
    // Паттерны для парсинга PDF-выписки
    private val datePattern = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    
    // Паттерны для обработки выписок Т-Банка
    private val dateRegex = "\\d{2}\\.\\d{2}\\.\\d{4}".toRegex()
    private val amountRegex = "([+-]?\\d+[\\s]?\\d+[,.]\\d{2})".toRegex()
    private val amountRegexWithSymbol = "[+-]?\\d+[\\s]?\\d+[,.]\\d{2}\\s?₽".toRegex()
    private val cardNumberPattern = Pattern.compile("Карта\\s+([*•\\d]+)\\s+", Pattern.MULTILINE)
    
    // Дополнительные паттерны для справки о движении средств
    private val statementHeaderRegex = "Справка о движении средств".toRegex(RegexOption.IGNORE_CASE)
    private val statementTableStartRegex = "(?:Дата и время\\s+операции)|(?:Дата\\s+операции)|(?:Дата\\s+и\\s+время)|(?:Дата списания)|(?:Описание\\s+операции)".toRegex(RegexOption.IGNORE_CASE)
    private val periodRegex = "Движение средств за период с (\\d{2}\\.\\d{2}\\.\\d{4}) по (\\d{2}\\.\\d{2}\\.\\d{4})".toRegex()
    private val tableEndRegex = "Остаток на|Поступления:|Списания:|Итого|Номер верх|Конец выписки".toRegex(RegexOption.IGNORE_CASE)
    private val cardNumberExtractor = "[*•\\d]{16,19}|\\d{4}\\s+\\d{4}\\s+\\d{4}\\s+\\d{4}|\\d{4}\\s+XXXX\\s+XXXX\\s+\\d{4}".toRegex()
    // Паттерн для сумм, более точный
    private val bigAmountRegex = "-?\\d{1,3}(?:[\\s]?\\d{3})*[,.](\\d{2})\\s?₽?".toRegex()
    
    // Инициализация PDFBox при создании экземпляра
    init {
        PDFBoxResourceLoader.init(context)
    }

    /**
     * Переопределение метода импорта для работы с PDF
     */
    override suspend fun invoke(uri: Uri): Flow<ImportResult> = flow {
        try {
            emit(ImportResult.Progress(1, 100, "Открытие PDF-файла выписки Т-Банка"))
            
            // Используем таймаут для чтения PDF содержимого
            val pdfLines = withTimeoutOrNull(PDF_PARSING_TIMEOUT) {
                readPdfContent(uri)
            } ?: run {
                emit(ImportResult.Error("Превышено время ожидания при чтении PDF-файла. Файл может быть слишком большим или поврежденным."))
                return@flow
            }
            
            if (pdfLines.isEmpty()) {
                emit(ImportResult.Error("Не удалось прочитать PDF-файл или файл пуст"))
                return@flow
            }
            
            // Проверяем, что это выписка Т-Банка
            val fullText = pdfLines.joinToString(" ")
            if (!isValidTBankStatement(fullText)) {
                emit(ImportResult.Error("Файл не является выпиской Т-Банка или имеет неподдерживаемый формат"))
                return@flow
            }
            
            // Всегда используем "Т-Банк" как источник
            val source = "Т-Банк"
            
            // Парсим данные из PDF и преобразуем их в транзакции
            emit(ImportResult.Progress(20, 100, "Анализ выписки Т-Банка"))
            
            // Используем таймаут для парсинга транзакций
            val transactions = withTimeoutOrNull(PDF_PARSING_TIMEOUT) {
                parsePdfTransactions(pdfLines, source)
            } ?: run {
                emit(ImportResult.Error("Превышено время ожидания при анализе PDF-файла. Файл может содержать слишком много транзакций или иметь сложную структуру."))
                return@flow
            }
            
            if (transactions.isEmpty()) {
                emit(ImportResult.Error("Не удалось найти транзакции в выписке Т-Банка"))
                return@flow
            }
            
            // Сохраняем импортированные транзакции
            emit(ImportResult.Progress(70, 100, "Сохранение ${transactions.size} транзакций"))
            
            var importedCount = 0
            var skippedCount = 0
            var totalAmount = 0.0
            
            for ((index, transaction) in transactions.withIndex()) {
                try {
                    repository.addTransaction(transaction)
                    importedCount++
                    totalAmount += if (transaction.isExpense) -transaction.amount else transaction.amount
                    
                    if (index % 5 == 0) {
                        val progress = 70 + (index.toFloat() / transactions.size * 30).toInt()
                        emit(ImportResult.Progress(progress, 100, "Сохранение транзакций: ${index + 1} из ${transactions.size}"))
                    }
                } catch (e: Exception) {
                    skippedCount++
                    Timber.e(e, "Ошибка при сохранении транзакции: ${e.message}")
                }
            }
            
            // Отправляем результат успешного импорта
            emit(ImportResult.Success(importedCount, skippedCount, totalAmount))
        } catch (e: Exception) {
            Timber.e(e, "Ошибка импорта из PDF Т-Банка: ${e.message}")
            emit(ImportResult.Error("Ошибка импорта из PDF Т-Банка: ${e.message}", e))
        }
    }
    
    /**
     * Читает содержимое PDF файла и возвращает список строк
     */
    private suspend fun readPdfContent(uri: Uri): List<String> {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Не удалось открыть файл")
        
        try {
            Timber.d("Начало чтения PDF-файла Т-Банка")
            val startTime = System.currentTimeMillis()
            
            val document = withContext(Dispatchers.IO) {
                PDDocument.load(inputStream)
            }
            
            val stripper = PDFTextStripper()
            val pdfText = withContext(Dispatchers.IO) {
                stripper.getText(document)
            }
            
            val endTime = System.currentTimeMillis()
            Timber.d("Чтение PDF-файла Т-Банка завершено за ${endTime - startTime} мс")
            
            document.close()
            inputStream.close()
            
            return pdfText.split("\n").filter { it.isNotBlank() }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при чтении PDF Т-Банка: ${e.message}")
            inputStream.close()
            throw e
        }
    }

    /**
     * Парсит транзакции из текста PDF выписки Т-Банка
     */
    private fun parsePdfTransactions(pdfLines: List<String>, source: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        // Проверяем, является ли выписка "Справкой о движении средств"
        val isStatementOfFunds = pdfLines.any { 
            it.contains("Справка о движении средств", ignoreCase = true) ||
            it.contains("Движение средств за период", ignoreCase = true)
        }
        
        if (isStatementOfFunds) {
            // Обрабатываем формат "Справка о движении средств"
            Timber.d("Обнаружен формат 'Справка о движении средств', используем специальную обработку")
            return parseStatementOfFunds(pdfLines, source)
        }
        
        // Стандартная обработка для обычной выписки
        
        // Сначала найдем строки, содержащие даты
        val dateLineIndices = pdfLines.mapIndexedNotNull { index, line ->
            val dateMatch = dateRegex.find(line)
            if (dateMatch != null) {
                Pair(index, parseDate(dateMatch.value))
            } else {
                null
            }
        }
        
        // Для каждой найденной строки с датой пытаемся найти сумму и описание операции
        for ((index, date) in dateLineIndices) {
            if (date == null) continue
            
            // Берем несколько строк после даты, чтобы найти описание и сумму
            val blockEndIndex = minOf(index + 5, pdfLines.size)
            val transactionBlock = pdfLines.subList(index, blockEndIndex).joinToString(" ")
            
            // Ищем сумму в блоке (положительная для пополнений, отрицательная для списаний)
            val amountInfo = findAmountInString(transactionBlock)
            
            if (amountInfo != null) {
                val (amount, isExpense) = amountInfo
                
                // Если нашли сумму, извлекаем описание операции
                if (amount > 0) {
                    // Определяем категорию операции на основе описания
                    val category = determineCategory(transactionBlock)
                    
                    // Извлекаем примечание из описания операции
                    val note = extractNoteFromDescription(transactionBlock)
                    
                    // Создаем транзакцию
                    val transaction = Transaction(
                        id = "tbank_pdf_${date.time}_${System.nanoTime()}",
                        amount = amount,
                        category = category,
                        date = date,
                        isExpense = isExpense,
                        note = note,
                        source = source
                    )
                    
                    transactions.add(transaction)
                }
            }
        }
        
        return transactions
    }
    
    /**
     * Обрабатывает формат "Справка о движении средств"
     */
    private fun parseStatementOfFunds(pdfLines: List<String>, source: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        // Вывод всех строк документа для отладки
        pdfLines.forEachIndexed { index, line ->
            Timber.d("Строка $index: $line")
        }
        
        // Находим индекс начала таблицы с операциями
        val tableStartIndex = pdfLines.indexOfFirst { line -> 
            statementTableStartRegex.find(line) != null || 
            line.contains("Дата", ignoreCase = true) && (
                line.contains("операции", ignoreCase = true) ||
                line.contains("время", ignoreCase = true) ||
                line.contains("списания", ignoreCase = true)
            )
        }
        
        if (tableStartIndex == -1) {
            Timber.w("Не удалось найти начало таблицы с операциями в справке о движении средств")
            
            // Если не нашли стандартным способом, попробуем найти по датам
            val firstDateIndex = pdfLines.indexOfFirst { line ->
                dateRegex.find(line) != null && 
                findAmountInString(line) != null
            }
            
            if (firstDateIndex != -1) {
                Timber.d("Найдена первая операция с датой на строке $firstDateIndex: ${pdfLines[firstDateIndex]}")
                return parseStatementWithoutHeaders(pdfLines, source, firstDateIndex)
            }
            
            return transactions
        }
        
        // Ищем индекс конца таблицы (если есть такие маркеры)
        val tableEndIndex = pdfLines.subList(tableStartIndex + 1, pdfLines.size).indexOfFirst { line ->
            tableEndRegex.find(line) != null
        }.let { if (it == -1) pdfLines.size else tableStartIndex + 1 + it }
        
        Timber.d("Таблица операций: начало=${tableStartIndex}, конец=${tableEndIndex}")
        
        // Находим период выписки для дополнительной проверки дат
        val periodMatch = pdfLines.joinToString(" ").let { periodRegex.find(it) }
        val startPeriodDate = periodMatch?.groupValues?.getOrNull(1)?.let { parseDate(it) }
        val endPeriodDate = periodMatch?.groupValues?.getOrNull(2)?.let { parseDate(it) }
        
        Timber.d("Период выписки: с ${startPeriodDate?.toString() ?: "неизвестно"} по ${endPeriodDate?.toString() ?: "неизвестно"}")
        
        // Начинаем обработку строк после заголовка таблицы, пропуская сам заголовок
        var currentIndex = tableStartIndex + 1
        var inTransactionEntry = false
        var transactionDate: Date? = null
        var transactionAmount: Pair<Double, Boolean>? = null
        var transactionDescription = ""
        var transactionCardNumber: String? = null
        
        // Пропускаем заголовки столбцов и подзаголовки, если они есть
        while (currentIndex < tableEndIndex) {
            val line = pdfLines[currentIndex]
            
            // Пропускаем пустые строки и заголовки
            if (line.isBlank() || 
                line.contains("Номер карты", ignoreCase = true) || 
                line.contains("Дата списания", ignoreCase = true) ||
                line.contains("№ п/п", ignoreCase = true)) {
                currentIndex++
                continue
            }
            
            // Прерываем обработку, если дошли до конца таблицы
            if (tableEndRegex.find(line) != null) {
                break
            }
            
            // Проверяем, содержит ли строка дату операции (начало новой записи)
            val dateMatch = dateRegex.find(line)
            
            if (dateMatch != null) {
                // Если уже обрабатывали транзакцию, сохраняем предыдущую
                if (inTransactionEntry && transactionDate != null && transactionAmount != null) {
                    saveTransaction(transactions, transactionDate, transactionAmount, transactionDescription, source, transactionCardNumber)
                }
                
                // Начинаем новую запись транзакции
                inTransactionEntry = true
                transactionDate = parseDate(dateMatch.value)
                transactionDescription = line
                transactionAmount = findAmountInString(line)
                transactionCardNumber = extractCardNumber(line)
                
                currentIndex++
                continue
            }
            
            // Если обрабатываем транзакцию, добавляем текущую строку к описанию
            if (inTransactionEntry) {
                transactionDescription += " " + line
                
                // Если еще не нашли сумму, ищем в текущей строке
                if (transactionAmount == null) {
                    transactionAmount = findAmountInString(line)
                }
                
                // Если еще не нашли номер карты, ищем в текущей строке
                if (transactionCardNumber == null) {
                    transactionCardNumber = extractCardNumber(line)
                }
            } else {
                // Если не в режиме транзакции, проверяем, может это начало новой транзакции без даты
                val amountInLine = findAmountInString(line)
                if (amountInLine != null) {
                    // Возможно это строка транзакции без даты, используем дату периода
                    inTransactionEntry = true
                    transactionDate = endPeriodDate // Используем дату конца периода как приблизительную
                    transactionDescription = line
                    transactionAmount = amountInLine
                    transactionCardNumber = extractCardNumber(line)
                }
            }
            
            currentIndex++
        }
        
        // Сохраняем последнюю транзакцию, если есть и еще не сохранена
        if (inTransactionEntry && transactionDate != null && transactionAmount != null) {
            saveTransaction(transactions, transactionDate, transactionAmount, transactionDescription, source, transactionCardNumber)
        }
        
        // Дополнительная проверка: все даты должны быть в пределах периода выписки
        if (startPeriodDate != null && endPeriodDate != null) {
            transactions.removeIf { transaction ->
                val transactionDateVal = transaction.date.time
                val isOutsidePeriod = transactionDateVal < startPeriodDate.time || transactionDateVal > endPeriodDate.time
                if (isOutsidePeriod) {
                    Timber.w("Удалена транзакция вне периода выписки: ${transaction.date}, сумма: ${transaction.amount}")
                }
                isOutsidePeriod
            }
        }
        
        Timber.d("Всего найдено транзакций в справке о движении средств: ${transactions.size}")
        return transactions
    }
    
    /**
     * Парсит выписку без чётких заголовков таблицы
     */
    private fun parseStatementWithoutHeaders(pdfLines: List<String>, source: String, startFromIndex: Int): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val fullText = pdfLines.joinToString(" ")
        
        // Находим период выписки для дополнительной проверки дат
        val periodMatch = periodRegex.find(fullText)
        val startPeriodDate = periodMatch?.groupValues?.getOrNull(1)?.let { parseDate(it) }
        val endPeriodDate = periodMatch?.groupValues?.getOrNull(2)?.let { parseDate(it) }
        
        Timber.d("Парсинг без заголовков: период с ${startPeriodDate?.toString() ?: "неизвестно"} по ${endPeriodDate?.toString() ?: "неизвестно"}")
        
        // Прямой парсинг строк с датами
        val dateLines = pdfLines.subList(startFromIndex, pdfLines.size)
            .filterIndexed { index, line -> 
                dateRegex.find(line) != null &&
                (findAmountInString(line) != null || 
                 (index + 1 < pdfLines.size - startFromIndex && 
                  findAmountInString(pdfLines[startFromIndex + index + 1]) != null))
            }
        
        Timber.d("Найдено потенциальных строк с транзакциями: ${dateLines.size}")
        
        // Обрабатываем каждую строку с датой как потенциальную транзакцию
        for (line in dateLines) {
            val dateMatch = dateRegex.find(line)
            if (dateMatch != null) {
                val date = parseDate(dateMatch.value)
                
                if (date != null) {
                    // Ищем сумму в текущей строке или в следующих 2-3 строках
                    var amount: Pair<Double, Boolean>? = findAmountInString(line)
                    var description = line
                    var cardNumber = extractCardNumber(line)
                    
                    if (amount != null) {
                        saveTransaction(transactions, date, amount, description, source, cardNumber)
                    }
                }
            }
        }
        
        // Если нет транзакций, попробуем более агрессивный метод - просто искать даты и суммы
        if (transactions.isEmpty()) {
            Timber.d("Транзакции не найдены, применяем агрессивный метод парсинга")
            
            // Находим все даты
            val allDates = mutableListOf<Pair<Int, Date>>()
            pdfLines.forEachIndexed { index, line ->
                val date = dateRegex.find(line)?.value?.let { parseDate(it) }
                if (date != null) {
                    allDates.add(Pair(index, date))
                }
            }
            
            // Находим все суммы
            val allAmounts = mutableListOf<Pair<Int, Pair<Double, Boolean>>>()
            pdfLines.forEachIndexed { index, line ->
                val amount = findAmountInString(line)
                if (amount != null) {
                    allAmounts.add(Pair(index, amount))
                }
            }
            
            Timber.d("Найдено дат: ${allDates.size}, сумм: ${allAmounts.size}")
            
            // Создаём транзакции, сопоставляя даты и суммы по близости индексов
            for ((dateIndex, date) in allDates) {
                // Найдём ближайшую сумму (в пределах 3 строк)
                val closestAmount = allAmounts
                    .filter { (amountIndex, _) -> Math.abs(amountIndex - dateIndex) <= 3 }
                    .minByOrNull { (amountIndex, _) -> Math.abs(amountIndex - dateIndex) }
                
                if (closestAmount != null) {
                    val (amountIndex, amount) = closestAmount
                    
                    // Собираем описание
                    val descStart = minOf(dateIndex, amountIndex)
                    val descEnd = maxOf(dateIndex, amountIndex) + 1
                    val description = pdfLines.subList(descStart, minOf(descEnd, pdfLines.size)).joinToString(" ")
                    
                    // Ищем номер карты
                    val cardNumber = extractCardNumber(description)
                    
                    saveTransaction(transactions, date, amount, description, source, cardNumber)
                }
            }
        }
        
        // Дополнительная проверка: все даты должны быть в пределах периода выписки
        if (startPeriodDate != null && endPeriodDate != null) {
            transactions.removeIf { transaction ->
                val transactionDateVal = transaction.date.time
                val isOutsidePeriod = transactionDateVal < startPeriodDate.time || transactionDateVal > endPeriodDate.time
                if (isOutsidePeriod) {
                    Timber.w("Удалена транзакция вне периода выписки: ${transaction.date}, сумма: ${transaction.amount}")
                }
                isOutsidePeriod
            }
        }
        
        Timber.d("Всего найдено транзакций в справке о движении средств: ${transactions.size}")
        return transactions
    }
    
    /**
     * Извлекает номер карты из строки
     */
    private fun extractCardNumber(text: String): String? {
        val cardNumberMatch = cardNumberExtractor.find(text)
        return cardNumberMatch?.value?.replace("\\s+".toRegex(), "")
    }
    
    /**
     * Вспомогательный метод для создания и сохранения транзакции
     */
    private fun saveTransaction(
        transactions: MutableList<Transaction>,
        date: Date,
        amountInfo: Pair<Double, Boolean>,
        description: String,
        source: String,
        cardNumber: String? = null
    ) {
        val (amount, isExpense) = amountInfo
        
        if (amount > 0) {
            val category = determineCategory(description)
            var note = extractNoteFromDescription(description)
            
            // Если есть номер карты, добавляем его к примечанию
            if (cardNumber != null && !description.contains(cardNumber)) {
                note = if (note.isNullOrEmpty()) "Карта: $cardNumber" else "$note (Карта: $cardNumber)"
            }
            
            val transaction = Transaction(
                id = "tbank_statement_${date.time}_${System.nanoTime()}",
                amount = amount,
                category = category,
                date = date,
                isExpense = isExpense,
                note = note,
                source = source
            )
            
            transactions.add(transaction)
            Timber.d("Добавлена транзакция: дата=${date}, сумма=${amount}, расход=${isExpense}, описание=${note ?: "нет"}")
        }
    }
    
    /**
     * Проверяет, что PDF-файл является выпиской Т-Банка
     */
    private fun isValidTBankStatement(text: String): Boolean {
        // Проверяем наличие ключевых слов, характерных для выписки Т-Банка
        val isRegularStatement = (text.contains("Т-Банк", ignoreCase = true) || 
                text.contains("Tinkoff", ignoreCase = true) || 
                text.contains("Тинькофф", ignoreCase = true)) &&
               (text.contains("выписка", ignoreCase = true) ||
                text.contains("операции", ignoreCase = true) ||
                text.contains("движение средств", ignoreCase = true)) &&
               text.contains("дата", ignoreCase = true) &&
               text.contains("сумма", ignoreCase = true)
        
        // Проверяем, является ли документ справкой о движении средств
        val isStatementOfFunds = (statementHeaderRegex.find(text) != null || text.contains("Движение средств", ignoreCase = true)) &&
                                  (periodRegex.find(text) != null || (text.contains("период", ignoreCase = true) && dateRegex.findAll(text).count() >= 2))
        
        return isRegularStatement || isStatementOfFunds
    }
    
    /**
     * Парсит дату из строки формата dd.MM.yyyy
     */
    private fun parseDate(dateString: String): Date? {
        return try {
            datePattern.parse(dateString)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка парсинга даты: $dateString")
            null
        }
    }
    
    /**
     * Извлекает дату из строки текста
     */
    private fun extractDateFromString(text: String): LocalDate? {
        val matches = dateRegex.findAll(text)
        for (match in matches) {
            try {
                val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                return LocalDate.parse(match.value, dateFormatter)
            } catch (e: Exception) {
                Timber.w(e, "Не удалось распарсить дату из: ${match.value}")
            }
        }
        return null
    }

    /**
     * Пытается найти сумму в строке, учитывая различные форматы
     */
    private fun findAmountInString(text: String): Pair<Double, Boolean>? {
        // Проверяем, не является ли текст датой
        if (dateRegex.matches(text)) {
            Timber.d("Текст является датой, пропускаем обработку как суммы: $text")
            return null
        }
        
        // Убираем даты из текста перед поиском сумм
        val textWithoutDates = text.replace(dateRegex, " ")
        
        // Сначала ищем большие суммы с пробелами между разрядами (173 000.00)
        val bigAmountMatch = bigAmountRegex.find(textWithoutDates)
        if (bigAmountMatch != null) {
            Timber.d("Найдена сумма с большим форматом: ${bigAmountMatch.value}")
            return parseAmountValue(bigAmountMatch.value)
        }
        
        // Проверим наличие суммы с символом рубля
        val amountWithSymbolMatch = amountRegexWithSymbol.find(textWithoutDates)
        if (amountWithSymbolMatch != null) {
            val amountStr = amountWithSymbolMatch.value.replace("₽", "").trim()
            Timber.d("Найдена сумма с символом рубля: $amountStr")
            return parseAmountValue(amountStr)
        }
        
        // Если не нашли с символом рубля, ищем просто числа
        val amountMatch = amountRegex.find(textWithoutDates)
        if (amountMatch != null) {
            // Дополнительная проверка, чтобы исключить даты
            val value = amountMatch.value
            if (!value.matches("\\d{2}\\.\\d{2}\\.\\d{2}".toRegex()) && !value.matches("\\d{2}\\.\\d{2}\\.\\d{4}".toRegex())) {
                Timber.d("Найдена сумма без символа: $value")
                return parseAmountValue(value)
            }
        }
        
        return null
    }
    
    /**
     * Парсит строку суммы в число и определяет тип транзакции
     */
    private fun parseAmountValue(amountStr: String): Pair<Double, Boolean>? {
        try {
            // Проверяем, не является ли это датой
            if (amountStr.matches("\\d{2}\\.\\d{2}\\.\\d{4}".toRegex())) {
                Timber.d("Строка похожа на дату, пропускаем: $amountStr")
                return null
            }
            
            // Нормализуем строку с суммой: заменяем запятую на точку и удаляем пробелы
            val normalizedAmount = amountStr
                .replace(",", ".")
                .replace(" ", "")
                .replace("+", "")
                .replace("₽", "")
                .trim()
            
            // Парсим сумму и определяем тип транзакции (расход или доход)
            val amount = normalizedAmount.toDouble().absoluteValue
            
            // Дополнительная проверка: сумма не должна быть слишком маленькой
            // (это может быть дата, которую неправильно распознали как сумму)
            if (amount < 0.5) {
                Timber.d("Сумма слишком маленькая, возможно это дата: $amountStr")
                return null
            }
            
            // Если в исходной строке был минус - это расход, иначе - доход
            val isExpense = amountStr.contains("-")
            
            Timber.d("Успешно распарсена сумма: $amount, расход: $isExpense")
            return Pair(amount, isExpense)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка парсинга суммы: $amountStr")
            return null
        }
    }
    
    /**
     * Извлекает примечание из описания операции
     */
    private fun extractNoteFromDescription(description: String): String? {
        // Удаляем дату и сумму из описания, оставляя только суть операции
        var note = description
            .replace(dateRegex, "")
            .replace(amountRegexWithSymbol, "")
            .replace(amountRegex, "")
            .replace("\\s+".toRegex(), " ")
            .trim()
        
        // Если получилось слишком длинное примечание, обрезаем его
        if (note.length > 100) {
            note = note.substring(0, 97) + "..."
        }
        
        return if (note.isNotEmpty()) note else null
    }
    
    /**
     * Определяет категорию транзакции на основе описания
     */
    private fun determineCategory(description: String): String {
        // Словарь категорий для Т-Банка
        val categoryKeywords = mapOf(
            "Продукты" to listOf("супермаркет", "продукты", "магазин", "пятерочка", "магнит", "ашан", "лента", "перекресток", "продуктовый", "бакалея"),
            "Рестораны" to listOf("ресторан", "кафе", "столовая", "бар", "паб", "тратория", "пиццерия", "суши", "фастфуд", "бистро", "макдоналдс", "кофейня"),
            "Транспорт" to listOf("такси", "метро", "автобус", "троллейбус", "трамвай", "проезд", "транспорт", "каршеринг", "яндекс", "убер", "билет"),
            "Здоровье" to listOf("аптека", "лекарства", "врач", "медицина", "клиника", "больница", "поликлиника", "анализы", "стоматолог"),
            "Одежда" to listOf("одежда", "обувь", "зара", "h&m", "магазин", "бутик", "галерея", "shopping", "молл"),
            "Связь" to listOf("связь", "интернет", "мобильный", "телефон", "мтс", "билайн", "мегафон", "теле2", "оператор"),
            "Коммунальные платежи" to listOf("жкх", "коммунальные", "электричество", "газ", "вода", "отопление", "квартплата", "услуги"),
            "Развлечения" to listOf("кино", "театр", "концерт", "развлечения", "шоу", "парк", "музей", "выставка", "клуб"),
            "Подписки" to listOf("подписка", "netflix", "spotify", "apple", "google", "яндекс", "музыка", "кино", "плюс"),
            "Переводы" to listOf("перевод", "p2p", "с2с", "card2card", "по номеру телефона", "по номеру карты"),
            "Зарплата" to listOf("зарплата", "аванс", "премия", "бонус", "выплата", "оклад", "вознаграждение", "доход"),
            "Другое" to listOf()
        )
        
        val lowerDesc = description.lowercase(Locale.getDefault())
        
        // Проверяем наличие ключевых слов каждой категории в описании
        for ((category, keywords) in categoryKeywords) {
            for (keyword in keywords) {
                if (lowerDesc.contains(keyword)) {
                    return category
                }
            }
        }
        
        // Если не определили по ключевым словам, возвращаем "Другое"
        return "Другое"
    }
    
    // Методы базового класса, переопределены для соответствия формату Т-Банка
    
    override fun isValidFormat(reader: BufferedReader): Boolean {
        val text = reader.readText()
        return isValidTBankStatement(text)
    }

    override fun skipHeaders(reader: BufferedReader) {
        // Не используется для PDF
    }

    override fun parseLine(line: String): Transaction {
        // Этот метод не будет использован для PDF
        throw NotImplementedError("Метод не реализован для PDF, используется специализированная логика")
    }
    
    override fun countTransactionLines(uri: Uri): Int {
        // Для PDF мы не можем заранее точно определить количество транзакций
        return 100 // Возвращаем примерное значение
    }
} 