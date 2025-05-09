package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences.CustomCategoryData
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.ui.theme.DefaultSourceColorInt
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseColorInt
import com.davidbugayov.financeanalyzer.ui.theme.IncomeColorInt
import com.davidbugayov.financeanalyzer.ui.theme.TransferColorInt
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Реализация импорта транзакций из PDF-выписки Т-Банка.
 * Поддерживает формат PDF-выписки из мобильного приложения Т-Банк (бывший Тинькофф).
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 * @param categoryPreferences Предпочтения категорий для создания новых категорий
 * @param sourcePreferences Предпочтения источников для создания новых источников
 */
class TBankPdfImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context,
    private val categoryPreferences: CategoryPreferences,
    private val sourcePreferences: SourcePreferences
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

    // Дополнительные паттерны для справки о движении средств
    private val statementHeaderRegex = "Справка о движении средств".toRegex(RegexOption.IGNORE_CASE)
    private val periodRegex = "Движение средств за период с (\\d{2}\\.\\d{2}\\.\\d{4}) по (\\d{2}\\.\\d{2}\\.\\d{4})".toRegex()
    private val tableEndRegex = "Остаток на|Поступления:|Списания:|Итого|Номер верх|Конец выписки".toRegex(RegexOption.IGNORE_CASE)
    // Паттерн для сумм, более точный
    private val bigAmountRegex = "-?\\d{1,3}(?:[\\s]?\\d{3})*[,.](\\d{2})\\s?₽?".toRegex()
    
    // Паттерны специально для выписок из скриншота
    private val tbankHeaderRegex = "АКЦИОНЕРНОЕ ОБЩЕСТВО «ТБАНК»".toRegex(RegexOption.IGNORE_CASE)
    private val tbankAddressRegex = "РОССИЯ, \\d+, МОСКВА".toRegex(RegexOption.IGNORE_CASE)
    private val tbankWebsiteRegex = "TBANK.RU".toRegex(RegexOption.IGNORE_CASE)
    private val transferTypeRegex = "Внутрибанковский перевод|Внешний перевод".toRegex(RegexOption.IGNORE_CASE)
    private val contractRegex = "с договора \\d+|по номеру телефона".toRegex(RegexOption.IGNORE_CASE)
    
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

            // Добавляем источник "Т-Банк", если его еще нет в предпочтениях
            addSourceIfNotExists()
            
            // Парсим данные из PDF и преобразуем их в транзакции
            emit(ImportResult.Progress(20, 100, "Анализ выписки Т-Банка"))
            
            // Используем таймаут для парсинга транзакций
            val transactions = withTimeoutOrNull(PDF_PARSING_TIMEOUT) {
                parsePdfTransactions(pdfLines)
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
            var totalAmount = Money.zero()
            
            for ((index, transaction) in transactions.withIndex()) {
                try {
                    // Добавляем категорию в предпочтения, если её там еще нет
                    addCategoryIfNotExists(transaction.category, transaction.isExpense)
                    
                    repository.addTransaction(transaction)
                    importedCount++
                    totalAmount = if (transaction.isExpense) 
                        totalAmount - transaction.amount 
                    else 
                        totalAmount + transaction.amount
                    
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
    private fun parsePdfTransactions(pdfLines: List<String>): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        val isStatementOfFunds = pdfLines.any { 
            it.contains("Справка о движении средств", ignoreCase = true) ||
            it.contains("Движение средств за период", ignoreCase = true)
        }
        
        if (isStatementOfFunds) {
            Timber.d("Обнаружен формат 'Справка о движении средств', используем специальную обработку")
            return parseStatementOfFunds(pdfLines)
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
                if (amount > Money.zero()) {
                    // Определяем категорию операции на основе описания
                    val category = determineCategory(transactionBlock)
                    
                    // Извлекаем примечание из описания операции
                    val note = extractNoteFromDescription(transactionBlock)
                    
                    // Проверяем, является ли транзакция переводом
                    val isTransfer = category == "Переводы" || transactionBlock.contains("перевод", ignoreCase = true)
                    
                    // Создаем транзакцию
                    val transaction = Transaction(
                        id = "tbank_pdf_${date.time}_${System.nanoTime()}",
                        amount = amount,
                        category = category,
                        date = date,
                        isExpense = isExpense,
                        note = note,
                        source = bankName,
                        sourceColor = if (isTransfer) TransferColorInt
                        else ColorUtils.getSourceColorByName(bankName)?.toArgb()
                            ?: if (isExpense) ExpenseColorInt else IncomeColorInt,
                        isTransfer = isTransfer
                    )
                    
                    transactions.add(transaction)
                }
            }
        }
        
        return transactions
    }
    
    /**
     * Парсит выписку в формате "Справка о движении средств"
     */
    private fun parseStatementOfFunds(pdfLines: List<String>): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        try {
            Timber.d("Начинаем парсинг справки о движении средств")
            
            // Находим диапазон дат в выписке
            val periodInfo = pdfLines.find { it.contains("Движение средств за период", ignoreCase = true) }
            var startPeriodDate: Date? = null
            var endPeriodDate: Date? = null
            
            if (periodInfo != null) {
                val periodMatch = periodRegex.find(periodInfo)
                if (periodMatch != null) {
                    Timber.d("Найден период выписки: ${periodMatch.value}")
                    startPeriodDate = periodMatch.groupValues[1].let { parseDate(it) }
                    endPeriodDate = periodMatch.groupValues[2].let { parseDate(it) }
                }
            }
            
            // Найдем заголовок таблицы с транзакциями
            val tableHeaderIndex = pdfLines.indexOfFirst { line ->
                line.contains("Дата и время операции", ignoreCase = true) ||
                line.contains("Дата операции", ignoreCase = true) ||
                line.contains("Сумма операции", ignoreCase = true) ||
                line.contains("Сумма в валюте", ignoreCase = true) ||
                line.contains("Описание операции", ignoreCase = true)
            }
            
            if (tableHeaderIndex == -1) {
                Timber.d("Заголовок таблицы не найден, используем альтернативный метод парсинга")
                return parseStatementWithoutHeaders(pdfLines, 0)
            }
            
            Timber.d("Найден заголовок таблицы на строке $tableHeaderIndex: ${pdfLines[tableHeaderIndex]}")
            
            // Ищем строки, содержащие даты и суммы после заголовка таблицы
            var i = tableHeaderIndex + 1
            while (i < pdfLines.size) {
                val line = pdfLines[i]
                
                // Проверяем, не конец ли таблицы
                if (tableEndRegex.containsMatchIn(line)) {
                    Timber.d("Достигнут конец таблицы на строке $i: $line")
                    break
                }
                
                // Новый формат из скриншота: проверяем наличие даты и времени операции в формате "30.03.2025 19:42"
                val dateTimeMatch = "\\d{2}\\.\\d{2}\\.\\d{4}\\s+\\d{2}:\\d{2}".toRegex().find(line)
                if (dateTimeMatch != null) {
                    // Извлекаем дату и время в формате "30.03.2025 19:42"
                    val dateTimeStr = dateTimeMatch.value
                    Timber.d("Найдена дата и время операции: $dateTimeStr")
                    
                    // Пытаемся получить все необходимые данные из текущей и следующих строк
                    val dateStr = dateTimeStr.split(" ")[0]
                    val date = parseDate(dateStr)
                    
                    if (date != null) {
                        // Ищем сумму в текущей строке
                        var amount: Pair<Money, Boolean>? = findAmountInString(line)
                        var description = line
                        
                        // Если сумма не найдена, смотрим в следующей строке
                        if (amount == null && i + 1 < pdfLines.size) {
                            amount = findAmountInString(pdfLines[i + 1])
                            if (amount != null) {
                                description += " " + pdfLines[i + 1]
                                i++ // Увеличиваем индекс, так как используем следующую строку
                            }
                        }
                        
                        // Смотрим описание операции в следующих строках
                        var j = i + 1
                        while (j < pdfLines.size && j < i + 3) {
                            val nextLine = pdfLines[j]
                            
                            // Проверяем, что строка не содержит другую дату операции
                            if ("\\d{2}\\.\\d{2}\\.\\d{4}\\s+\\d{2}:\\d{2}".toRegex().find(nextLine) == null) {
                                // Проверяем, содержит ли строка полезную информацию для описания
                                if (nextLine.contains("перевод", ignoreCase = true) || 
                                    nextLine.contains("договора", ignoreCase = true) || 
                                    nextLine.contains("номеру", ignoreCase = true) ||
                                    nextLine.contains("внутрибанк", ignoreCase = true) ||
                                    nextLine.contains("внешний", ignoreCase = true) ||
                                    nextLine.contains("телефона", ignoreCase = true)) {
                                    description += " " + nextLine
                                    i = j // Обновляем текущий индекс
                                }
                            } else {
                                // Если нашли следующую дату операции, прекращаем поиск
                                break
                            }
                            j++
                        }
                        
                        // Если нашли сумму, создаем транзакцию
                        if (amount != null) {
                            saveTransaction(transactions, date, amount, description)
                        }
                    }
                } else {
                    // Традиционный поиск даты в формате dd.MM.yyyy
                    val dateMatch = dateRegex.find(line)
                    if (dateMatch != null) {
                        val dateStr = dateMatch.value
                        val date = parseDate(dateStr)
                        
                        if (date != null) {
                            // Ищем сумму в текущей строке
                            val amount = findAmountInString(line)
                            if (amount != null) {
                                val description = line
                                saveTransaction(transactions, date, amount, description)
                            }
                        }
                    }
                }
                
                i++
            }
            
            Timber.d("Парсинг справки завершен, найдено транзакций: ${transactions.size}")
            
            // Если не нашли ни одной транзакции, попробуем другой метод
            if (transactions.isEmpty()) {
                Timber.d("Транзакции не найдены, используем альтернативный метод парсинга")
                return parseStatementWithoutHeaders(pdfLines, tableHeaderIndex + 1)
            }
            
            // Если нашли транзакции, проверяем их даты с учетом периода выписки
            if (startPeriodDate != null && endPeriodDate != null) {
                transactions.removeIf { transaction ->
                    val transactionDate = transaction.date.time
                    val isOutsidePeriod = transactionDate < startPeriodDate.time || transactionDate > endPeriodDate.time
                    if (isOutsidePeriod) {
                        Timber.w("Удалена транзакция вне периода выписки: ${transaction.date}, сумма: ${transaction.amount}")
                    }
                    isOutsidePeriod
                }
            }
            
            return transactions
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при парсинге справки о движении средств: ${e.message}")
            // В случае ошибки возвращаем пустой список
            return transactions
        }
    }
    
    /**
     * Парсит выписку без чётких заголовков таблицы
     */
    private fun parseStatementWithoutHeaders(pdfLines: List<String>, startFromIndex: Int): List<Transaction> {
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
                    var amount: Pair<Money, Boolean>? = findAmountInString(line)
                    var description = line
                    
                    if (amount != null) {
                        saveTransaction(transactions, date, amount, description)
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
            val allAmounts = mutableListOf<Pair<Int, Pair<Money, Boolean>>>()
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

                    saveTransaction(transactions, date, amount, description)
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
     * Вспомогательный метод для создания и сохранения транзакции
     */
    private fun saveTransaction(
        transactions: MutableList<Transaction>,
        date: Date,
        amountInfo: Pair<Money, Boolean>,
        description: String
    ) {
        val (amount, isExpense) = amountInfo
        
        if (amount > Money.zero()) {
            val category = determineCategory(description)
            val note = extractNoteFromDescription(description)
            
            // Проверяем, является ли это переводом
            val isTransfer = category == "Переводы" || description.contains("перевод", ignoreCase = true)
            
            val transaction = Transaction(
                id = "tbank_pdf_${date.time}_${System.nanoTime()}",
                amount = amount,
                category = category,
                date = date,
                isExpense = isExpense,
                note = note,
                source = bankName,
                sourceColor = if (isTransfer) TransferColorInt
                else ColorUtils.getSourceColorByName(bankName)?.toArgb()
                    ?: if (isExpense) ExpenseColorInt else IncomeColorInt,
                isTransfer = isTransfer
            )
            
            transactions.add(transaction)
            Timber.d("Добавлена транзакция: дата=${date}, сумма=${amount}, расход=${isExpense}, перевод=${isTransfer}, описание=${note ?: "нет"}")
        }
    }
    
    /**
     * Проверяет, что PDF-файл является выпиской Т-Банка
     */
    private fun isValidTBankStatement(text: String): Boolean {
        // Проверяем присутствие типичных маркеров выписки Т-Банка
        val hasTBankMarkers = text.contains("Т-Банк", ignoreCase = true) ||
                             text.contains("Тинькофф", ignoreCase = true) ||
                             text.contains("Tinkoff", ignoreCase = true) ||
                             text.contains("ТБАНК", ignoreCase = true) ||
                             tbankHeaderRegex.containsMatchIn(text) ||
                             tbankAddressRegex.containsMatchIn(text) ||
                             tbankWebsiteRegex.containsMatchIn(text)
        
        // Проверяем признаки справки о движении средств
        val isStatementOfFunds = statementHeaderRegex.containsMatchIn(text) &&
                                periodRegex.containsMatchIn(text)
        
        // Проверяем дополнительные признаки из выписки на скриншоте
        val hasTransferDetails = transferTypeRegex.containsMatchIn(text) &&
                               contractRegex.containsMatchIn(text)
        
        val hasOperationData = text.contains("Дата и время операции", ignoreCase = true) &&
                             text.contains("Сумма операции", ignoreCase = true) &&
                             text.contains("Номер карты", ignoreCase = true)
        
        Timber.d("Проверка выписки Т-Банка: маркеры=$hasTBankMarkers, справка=$isStatementOfFunds, " +
                "детали переводов=$hasTransferDetails, данные операций=$hasOperationData")
        
        return hasTBankMarkers || isStatementOfFunds || hasTransferDetails || hasOperationData
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
     * Находит сумму в строке и определяет, является ли она расходом.
     * @return Пара (сумма как Money, признак расхода) или null, если сумма не найдена.
     */
    private fun findAmountInString(text: String): Pair<Money, Boolean>? {
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
     * Парсит строку суммы в объект Money и определяет тип транзакции
     */
    private fun parseAmountValue(amountStr: String): Pair<Money, Boolean>? {
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
            val amount = Money.fromString(normalizedAmount)
            
            // Дополнительная проверка: сумма не должна быть слишком маленькой
            // (это может быть дата, которую неправильно распознали как сумму)
            if (amount.amount.toDouble() < 0.5) {
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
    
    /**
     * Добавляет категорию в предпочтения, если её еще нет
     * @param category Название категории
     * @param isExpense Флаг, является ли категория расходной
     */
    private fun addCategoryIfNotExists(category: String, isExpense: Boolean) {
        val customCategory = CustomCategoryData(category, "Add")
        if (isExpense) {
            categoryPreferences.addExpenseCategory(customCategory)
        } else {
            categoryPreferences.addIncomeCategory(customCategory)
        }
        Timber.d("Проверка и добавление категории: $category, расход: $isExpense")
    }
    
    /**
     * Добавляет источник "Т-Банк" в настройки, если его там еще нет
     */
    private fun addSourceIfNotExists() {
        val currentSources: List<Source> = sourcePreferences.getCustomSources().toList()
        if (currentSources.none { it.name.equals(bankName, ignoreCase = true) }) {
            Timber.d("Добавляем новый источник: $bankName")
            val sourceColorInt = ColorUtils.getSourceColorByName(bankName)?.toArgb()
                ?: DefaultSourceColorInt

            val newSource = Source(
                name = bankName,
                color = sourceColorInt
            )
            val updatedSources = (currentSources + newSource).distinctBy { it.name }
            sourcePreferences.saveCustomSources(updatedSources)
        }
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

    override suspend fun countTransactionLines(uri: Uri): Int {
        // Для PDF мы не можем заранее точно определить количество транзакций
        return 100 // Возвращаем примерное значение
    }
} 