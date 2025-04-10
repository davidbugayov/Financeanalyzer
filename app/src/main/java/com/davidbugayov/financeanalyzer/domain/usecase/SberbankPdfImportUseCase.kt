package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.utils.ColorUtils
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
import android.graphics.Color
import com.davidbugayov.financeanalyzer.domain.model.Money

/**
 * Реализация импорта транзакций из PDF-выписки Сбербанка.
 * Поддерживает формат PDF-выписки из СберБанк Онлайн и мобильного приложения.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class SberbankPdfImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context
) : BankImportUseCase(repository, context) {

    override val bankName: String = "Сбербанк"
    
    // Таймаут для операции парсинга PDF (в миллисекундах)
    private val PDF_PARSING_TIMEOUT = 30000L // 30 секунд
    
    /**
     * Безопасно парсит строку с суммой в число, учитывая различные форматы записи
     */
    private fun safeParseAmount(amountStr: String): Money? {
        return try {
            // Логируем исходную строку для отладки
            Timber.d("Парсинг суммы: '$amountStr'")
            
            // Удаляем знаки валюты и другие специальные символы сначала
            val withoutCurrency = amountStr.replace("[₽руб.\\s]+$".toRegex(), "").trim()
            
            // Удаляем все пробелы, заменяем запятую на точку
            val cleanAmount = withoutCurrency.replace("\\s".toRegex(), "").replace(",", ".")
            
            // Удаляем знаки валюты, если они остались
            val numericOnly = cleanAmount.replace("[₽руб]".toRegex(), "").trim()
            
            // Удаляем знак "+" в начале строки, если он есть
            val finalAmount = if (numericOnly.startsWith("+")) {
                numericOnly.substring(1)
            } else {
                numericOnly
            }
            
            // Преобразуем в число
            Timber.d("Преобразование суммы: исходная '$amountStr', очищенная '$finalAmount'")
            Money.parse(finalAmount)
        } catch (e: Exception) {
            Timber.e(e, "Не удалось преобразовать строку в число: $amountStr")
            null
        }
    }
    
    // Паттерны для парсинга PDF-выписки
    private val datePattern = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    
    // Паттерны для обработки выписок Сбербанка
    private val dateRegex = "\\d{2}\\.\\d{2}\\.\\d{4}".toRegex()
    private val amountRegex = "([+-]?\\d+[\\s]?\\d+[,.]\\d{2})".toRegex()
    private val amountRegexImproved = "(([+-]?\\d+[\\s]?\\d+[,.]\\d{2})|(\\d+[,.]\\d{2}))".toRegex()
    private val cardNumberPattern = Pattern.compile("Карта\\s+([*•\\d]+)\\s+", Pattern.MULTILINE)
    
    // Усовершенствованный паттерн для сумм в выписке, включая пробелы между группами цифр
    // Поддерживает форматы из скриншота: "2 800,00", "+2 000,00", "+800,00", "4 800,00" и т.д.
    private val tableAmountRegex = "([+-]?\\d{1,3}(\\s\\d{3})*[,.]\\d{2})(?:\\s*$|\\s+(?:[А-Яа-я₽]+)?)".toRegex()
    
    // Паттерны для новой формы выписки с таблицей "Расшифровка операций"
    private val tableHeaderRegex = "Расшифровка\\s+операций".toRegex(RegexOption.IGNORE_CASE)
    private val tableColumnDateRegex = "ДАТА\\s+ОПЕРАЦИИ".toRegex(RegexOption.IGNORE_CASE)
    private val tableColumnDescriptionRegex = "Описание\\s+операции".toRegex(RegexOption.IGNORE_CASE)
    private val tableColumnAmountRegex = "СУММА\\s+В\\s+ВАЛЮТЕ\\s+СЧЁТА".toRegex(RegexOption.IGNORE_CASE)
    private val dateProcessingRegex = "Дата\\s+обработки".toRegex(RegexOption.IGNORE_CASE)
    private val authCodeRegex = "код\\s+авторизации".toRegex(RegexOption.IGNORE_CASE)
    private val tableEndRegex = "Итого:|Общая сумма:|остаток|Конец выписки".toRegex(RegexOption.IGNORE_CASE)
    
    // Перечень стандартных категорий Сбербанка
    private val standardCategories = listOf(
        "Здоровье и красота", "Прочие операции", "Переводы", "Перевод на карту",
        "Супермаркеты", "Рестораны", "Транспорт", "Одежда и обувь", 
        "Развлечения", "Связь", "Коммунальные платежи", "Отели",
        "Авто", "Топливо", "Фастфуд"
    )
    
    // Инициализация PDFBox при создании экземпляра
    init {
        PDFBoxResourceLoader.init(context)
    }

    /**
     * Переопределение метода импорта для работы с PDF
     */
    override suspend fun invoke(uri: Uri): Flow<ImportResult> = flow {
        try {
            emit(ImportResult.Progress(1, 100, "Открытие PDF-файла выписки Сбербанка"))
            
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
            
            // Всегда используем "Сбер" как источник
            val source = "Сбер"
            
            // Парсим данные из PDF и преобразуем их в транзакции
            emit(ImportResult.Progress(20, 100, "Анализ выписки"))
            
            // Используем таймаут для парсинга транзакций
            val transactions = withTimeoutOrNull(PDF_PARSING_TIMEOUT) {
                parsePdfTransactions(pdfLines, source)
            } ?: run {
                emit(ImportResult.Error("Превышено время ожидания при анализе PDF-файла. Файл может содержать слишком много транзакций или иметь сложную структуру."))
                return@flow
            }
            
            if (transactions.isEmpty()) {
                emit(ImportResult.Error("Не удалось найти транзакции в выписке"))
                return@flow
            }
            
            // Сохраняем импортированные транзакции
            emit(ImportResult.Progress(70, 100, "Сохранение ${transactions.size} транзакций"))
            
            var importedCount = 0
            var skippedCount = 0
            var totalAmount = Money.zero()
            
            for ((index, transaction) in transactions.withIndex()) {
                try {
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
            Timber.e(e, "Ошибка импорта из PDF Сбербанка: ${e.message}")
            emit(ImportResult.Error("Ошибка импорта из PDF Сбербанка: ${e.message}", e))
        }
    }
    
    /**
     * Читает содержимое PDF файла и возвращает список строк
     */
    private suspend fun readPdfContent(uri: Uri): List<String> {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Не удалось открыть файл")
        
        try {
            Timber.d("Начало чтения PDF-файла")
            val startTime = System.currentTimeMillis()
            
            val document = withContext(Dispatchers.IO) {
                PDDocument.load(inputStream)
            }
            
            val stripper = PDFTextStripper()
            val pdfText = withContext(Dispatchers.IO) {
                stripper.getText(document)
            }
            
            val endTime = System.currentTimeMillis()
            Timber.d("Чтение PDF-файла завершено за ${endTime - startTime} мс")
            
            document.close()
            inputStream.close()
            
            return pdfText.split("\n").filter { it.isNotBlank() }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при чтении PDF: ${e.message}")
            inputStream.close()
            throw e
        }
    }

    /**
     * Извлекает номер карты из текста выписки
     */
    private fun extractCardNumber(text: String): String? {
        val cardNumberMatcher = cardNumberPattern.matcher(text)
        return if (cardNumberMatcher.find()) {
            "Карта ${cardNumberMatcher.group(1)}"
        } else {
            val cardMatch = "••••\\s*\\d+".toRegex().find(text)
            if (cardMatch != null) {
                "Карта ${cardMatch.value}"
            } else {
                null
            }
        }
    }

    /**
     * Парсит транзакции из текста PDF выписки
     */
    private fun parsePdfTransactions(pdfLines: List<String>, source: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        // Проверяем, является ли выписка форматом с таблицей "Расшифровка операций"
        val isTableFormat = pdfLines.any { 
            it.contains("Расшифровка операций", ignoreCase = true) &&
            (it.contains("ДАТА ОПЕРАЦИИ", ignoreCase = true) || pdfLines.any { line -> line.contains("ДАТА ОПЕРАЦИИ", ignoreCase = true) })
        }
        
        if (isTableFormat) {
            // Обрабатываем формат таблицы "Расшифровка операций"
            Timber.d("Обнаружен формат таблицы 'Расшифровка операций', используем специальную обработку")
            return parseTableFormat(pdfLines, source)
        }
        
        // Продолжаем стандартную обработку для обычной выписки
        // Используем объект Date для совместимости с моделью Transaction
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Первый проход: ищем строки с датами и группируем данные
        val dateLines = mutableListOf<Pair<Int, Date>>()
        
        pdfLines.forEachIndexed { index, line ->
            // Мы используем наш новый метод для извлечения LocalDate
            val localDate = extractDateFromString(line)
            if (localDate != null) {
                // Конвертируем LocalDate в Date для совместимости
                val date = dateFormatter.parse(localDate.toString())
                if (date != null) {
                    dateLines.add(Pair(index, date))
                }
            }
        }
        
        // Сортируем строки с датами
        dateLines.sortBy { it.first }
        
        // Второй проход: формируем блоки транзакций на основе найденных дат
        for (i in dateLines.indices) {
            val (startIndex, date) = dateLines[i]
            val endIndex = if (i < dateLines.size - 1) dateLines[i + 1].first - 1 else pdfLines.size - 1
            
            // Собираем все строки для текущей даты
            val transactionBlock = pdfLines.subList(startIndex, endIndex + 1).joinToString(" ")
            
            // Ищем сумму в описании
            val amountInfo = findAmountInString(transactionBlock)
            
            if (amountInfo != null) {
                val (amount, isExpense) = amountInfo
                
                // Если сумма найдена, создаем транзакцию
                if (amount > Money.zero()) {
                    val category = determineCategory(transactionBlock)
                    
                    val note = extractNoteFromDescription(transactionBlock)
                    
                    // Определяем категорию
                    val categoryIndex = standardCategories.indexOf(category)
                    
                    // Проверяем, является ли транзакция переводом
                    val isTransfer = category == "Переводы" || (note?.contains("перев", ignoreCase = true) == true)
                    
                    // Создаем объект транзакции
                    val transaction = Transaction(
                        id = "sber_pdf_${date.time}_${amount}_${System.nanoTime()}",
                        amount = amount,
                        category = category,
                        date = date,
                        isExpense = isExpense,
                        note = note,
                        source = source,
                        sourceColor = if (isTransfer) ColorUtils.TRANSFER_COLOR else 
                            ColorUtils.getSourceColor(source) ?: (if (isExpense) ColorUtils.EXPENSE_COLOR else ColorUtils.INCOME_COLOR),
                        isTransfer = isTransfer
                    )
                    
                    transactions.add(transaction)
                }
            }
        }
        
        // Возвращаем обработанные транзакции, удаляя дубликаты
        return removeDuplicateTransactions(transactions)
    }

    /**
     * Парсит выписку в формате таблицы "Расшифровка операций"
     */
    private fun parseTableFormat(pdfLines: List<String>, source: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        Timber.d("Начинаю парсинг таблицы, строк: ${pdfLines.size}")
        
        // Ищем заголовок таблицы с тремя столбцами
        var tableStartIndex = -1
        var hasFoundHeader = false
        
        // Ищем строку с заголовками "Дата операции", "Категория/Описание", "Сумма"
        for (i in pdfLines.indices) {
            val line = pdfLines[i].trim()
            if (line.contains("Дата операции", ignoreCase = true) || 
                line.contains("Дата обработки", ignoreCase = true) ||
                line.contains("ДАТА ОПЕРАЦИИ", ignoreCase = true)) {
                tableStartIndex = i
                hasFoundHeader = true
                Timber.d("Найден заголовок таблицы в строке $i: $line")
                break
            }
        }
        
        // Дополнительный шаг: проверяем несколько строк ниже на наличие СУММА В ВАЛЮТЕ СЧЁТА
        if (hasFoundHeader) {
            val maxCheck = minOf(tableStartIndex + 5, pdfLines.size - 1)
            for (i in tableStartIndex..maxCheck) {
                val line = pdfLines[i].trim()
                if (line.contains("СУММА В ВАЛЮТЕ СЧЁТА", ignoreCase = true)) {
                    Timber.d("Подтвержден формат выписки с 'СУММА В ВАЛЮТЕ СЧЁТА' в строке $i")
                    break
                }
            }
        }
        
        if (!hasFoundHeader) {
            Timber.d("Не найден заголовок таблицы, пробуем анализировать данные напрямую")
            return parseIndividualStatement(pdfLines, source)
        }
        
        // Пропускаем строки заголовка и подзаголовка
        var dataIndex = tableStartIndex + 1
        while (dataIndex < pdfLines.size && 
               !pdfLines[dataIndex].contains("\\d{2}\\.\\d{2}\\.\\d{4}".toRegex())) {
            dataIndex++
        }
        
        Timber.d("Начало данных таблицы: строка $dataIndex")
        
        // Отладочный вывод первых строк после заголовка
        if (tableStartIndex >= 0) {
            for (i in tableStartIndex until minOf(tableStartIndex + 10, pdfLines.size)) {
                Timber.d("Строка ${i}: ${pdfLines[i]}")
            }
        }
        
        // Теперь анализируем строки данных, предполагая структуру из трех столбцов
        var currentDate: Date?
        var currentLineIndex = dataIndex
        var categories = standardCategories.map { it.lowercase(Locale.getDefault()) }
        
        while (currentLineIndex < pdfLines.size) {
            val currentLine = pdfLines[currentLineIndex].trim()
            
            if (currentLine.isBlank() || currentLine.contains("Продолжение на", ignoreCase = true)) {
                currentLineIndex++
                    continue
                }
            
            // Проверяем, если строка содержит "Итого" или "Конец выписки", это конец таблицы
            if (currentLine.contains("Итого", ignoreCase = true) || 
                currentLine.contains("Конец выписки", ignoreCase = true) ||
                currentLine.contains("ОСТАТОК НА", ignoreCase = true)) {
                break
            }
            
            // Проверяем, является ли эта строка началом новой транзакции (содержит дату)
            val dateMatch = "\\d{2}\\.\\d{2}\\.\\d{4}".toRegex().find(currentLine)
            
            if (dateMatch != null) {
                // Это новая транзакция
                try {
                    // Парсим дату
                    val dateStr = dateMatch.value
                    currentDate = parseDate(dateStr)
                    
                    // Ищем время операции (обычно следует за датой)
                    val timeMatch = "\\d{2}:\\d{2}".toRegex().find(currentLine, dateMatch.range.last + 1)
                    
                    // Определяем позицию, где заканчивается первый столбец с датой/временем
                    val firstColumnEndPos = if (timeMatch != null) {
                        timeMatch.range.last + 1
                    } else {
                        dateMatch.range.last + 1
                    }
                    
                    // Ищем сумму в конце строки (третий столбец)
                    // Используем обновленное регулярное выражение для более точного определения сумм
                    val amountMatch = tableAmountRegex.find(currentLine)
                    
                    // Дополнительная отладка для определения суммы
                    Timber.d("Строка для поиска суммы: '$currentLine'")
                    if (amountMatch != null) {
                        Timber.d("Найдена сумма: '${amountMatch.value}' в позиции ${amountMatch.range}")
                    } else {
                        // Если не нашли сумму с помощью tableAmountRegex, пробуем другие шаблоны
                        val altAmountMatch = "([+-]?\\d+[,.]\\d{2})".toRegex().find(currentLine)
                        if (altAmountMatch != null) {
                            Timber.d("Найдена альтернативная сумма: '${altAmountMatch.value}' в позиции ${altAmountMatch.range}")
                        }
                    }
                    
                    // Если нашли сумму, обрабатываем транзакцию
                    if (amountMatch != null) {
                        // Получаем средний столбец между датой/временем и суммой
                        val middleColumnStart = firstColumnEndPos
                        val middleColumnEnd = amountMatch.range.first
                        
                        if (middleColumnStart < middleColumnEnd) {
                            val middleColumn = currentLine.substring(middleColumnStart, middleColumnEnd).trim()
                            
                            // Парсим сумму
                            val amountStr = amountMatch.value
                            val parsedAmount = safeParseAmount(amountStr)
                            
                            if (parsedAmount != null && parsedAmount > Money.zero()) {
                                // Определяем тип операции (расход/доход)
                                var isExpense = !amountStr.startsWith("+")
                                
                                // Анализ операции на основе описания и суммы
                                var finalIsExpense = isExpense
                                
                                // Анализируем тип операции по описанию
                                if (middleColumn.contains("Перевод на карту", ignoreCase = true) || 
                                    middleColumn.contains("Перевод от", ignoreCase = true)) {
                                    // Переводы на карту обычно доходы
                                    finalIsExpense = false
                                } else if (middleColumn.contains("Перевод СБП", ignoreCase = true) || 
                                           middleColumn.contains("Перевод для", ignoreCase = true)) {
                                    // Переводы СБП или для кого-то обычно расходы, если нет явного +
                                    finalIsExpense = !amountStr.startsWith("+")
                                } else if (middleColumn.contains("Прочие расходы", ignoreCase = true) || 
                                           middleColumn.contains("Автоплатёж", ignoreCase = true)) {
                                    // Расходы и автоплатежи однозначно расходы
                                    finalIsExpense = true
                                }
                                
                                // Дополнительная проверка для определения типа операции на основе суммы
                                // Обычно в выписке Сбербанка положительные значения (+X XXX,XX) - доходы
                                if (amountStr.startsWith("+")) {
                                    finalIsExpense = false
                                }
                                
                                // Добавляем уточнение для конкретных случаев
                                if (amountMatch.value.matches("^[+]\\d+".toRegex()) || 
                                    amountMatch.value.matches("^[+]\\d+\\s\\d+".toRegex())) {
                                    Timber.d("Найдена положительная сумма с явным плюсом: ${amountMatch.value}")
                                    finalIsExpense = false
                                }
                                
                                // Определяем категорию и описание из среднего столбца
                                var category = ""
                                
                                // Ищем категорию среди стандартных
                                for (cat in standardCategories) {
                                    if (middleColumn.startsWith(cat, ignoreCase = true)) {
                                        category = cat
                                        break
                                    }
                                }
                                
                                // Если категория не была обнаружена, проверяем первое слово
                                if (category.isBlank()) {
                                    val lcMiddleColumn = middleColumn.lowercase(Locale.getDefault())
                                    
                                    // Проверяем, содержит ли начало описания какую-либо стандартную категорию
                                    val matchingCategory = categories.find { cat -> 
                                        lcMiddleColumn.startsWith(cat)
                                    }
                                    
                                    if (matchingCategory != null) {
                                        // Нашли категорию, которая является началом описания
                                        val actualCategory = standardCategories[categories.indexOf(matchingCategory)]
                                        category = actualCategory
                                    } else {
                                        // Определяем категорию по содержимому
                                        category = determineCategory(middleColumn)
                                    }
                                }
                                
                                // Собираем дополнительную информацию из следующих строк
                                var additionalInfo = ""
                                var nextLineIndex = currentLineIndex + 1
                                
                                // Проверяем следующую строку - если это код авторизации, 
                                // то пропускаем его и берем следующую строку как описание
                                if (nextLineIndex < pdfLines.size) {
                                    val nextLine = pdfLines[nextLineIndex].trim()
                                    Timber.d("Проверяем строку ${nextLineIndex}: '${nextLine}'")
                                    
                                    // Проверяем, является ли строка кодом авторизации (5-7 цифр)
                                    if (nextLine.matches("\\d{5,7}".toRegex())) {
                                        // Это код авторизации, пропускаем его
                                        Timber.d("Найден код авторизации: $nextLine")
                                        nextLineIndex++
                                        
                                        // Берем следующую строку целиком как описание, если она существует и не начинается с даты
                                        if (nextLineIndex < pdfLines.size) {
                                            val descriptionLine = pdfLines[nextLineIndex].trim()
                                            Timber.d("Строка после кода авторизации: '${descriptionLine}'")
                                            
                                            // Если это не начало новой транзакции (не содержит дату в формате XX.XX.XXXX)
                                            if (!descriptionLine.contains("\\d{2}\\.\\d{2}\\.\\d{4}".toRegex())) {
                                                // Используем всю строку целиком как примечание
                                                additionalInfo = descriptionLine
                                                Timber.d("Установлено примечание: '$additionalInfo'")
                                                
                                                nextLineIndex++
                                            }
                                        }
                                    } 
                                    // Эта строка не код авторизации, значит структура другая или код отсутствует
                                    else if (!nextLine.contains("\\d{2}\\.\\d{2}\\.\\d{4}".toRegex())) {
                                        // Если у нас нет кода авторизации, но следующая строка содержит описание операции,
                                        // все равно используем её как описание
                                        additionalInfo = nextLine
                                        Timber.d("Строка не код авторизации, используем как примечание: '$additionalInfo'")
                                        nextLineIndex++
                                    }
                                }
                                
                                // Создаем примечание из имеющихся данных 
                                val noteText = when {
                                    // Используем строку дополнительной информации если она есть
                                    additionalInfo.isNotBlank() -> {
                                        Timber.d("Используем additionalInfo для примечания: '$additionalInfo'")
                                        additionalInfo
                                    }
                                    
                                    // Используем содержимое строки с авторизацией + строки описания
                                    nextLineIndex < pdfLines.size && nextLineIndex > currentLineIndex + 1 -> {
                                        val authCode = pdfLines[currentLineIndex + 1].trim()
                                        val operationDesc = pdfLines[nextLineIndex - 1].trim()
                                        val fullDesc = "$operationDesc $authCode".trim()
                                        Timber.d("Используем составное примечание: '$fullDesc'")
                                        fullDesc
                                    }
                                    
                                    // Или описание из среднего столбца
                                    middleColumn.isNotBlank() -> {
                                        Timber.d("Используем описание для примечания: '$middleColumn'")
                                        middleColumn
                                    }
                                    
                                    // В крайнем случае формируем обобщенное описание
                                    else -> {
                                        val generatedNote = "${category}. Операция от ${datePattern.format(currentDate!!)}"
                                        Timber.d("Генерируем примечание: '$generatedNote'")
                                        generatedNote
                                    }
                                }
                                
                                Timber.d("Примечание для транзакции: $noteText")
                                
                                // Проверяем, является ли транзакция переводом
                                val isTransfer = category == "Переводы" || noteText.contains("перев", ignoreCase = true)

                                // Создаем транзакцию с примечанием
                                val transaction = Transaction(
                                    id = "sber_pdf_${currentDate!!.time}_${parsedAmount}_${System.nanoTime()}",
                                    amount = parsedAmount,
                                    category = category,
                                    date = currentDate,
                                    isExpense = finalIsExpense,
                                    note = noteText,
                                    source = source,
                                    sourceColor = if (isTransfer) ColorUtils.TRANSFER_COLOR else
                                        if (finalIsExpense) ColorUtils.EXPENSE_COLOR else ColorUtils.INCOME_COLOR,
                                    isTransfer = isTransfer
                                )
                                
                                transactions.add(transaction)
                                Timber.d("Добавлена транзакция: $transaction")
                                
                                // Переходим к следующей строке после обработанных
                                currentLineIndex = nextLineIndex
                                continue
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Ошибка при парсинге строки: $currentLine")
                }
            }
            
            currentLineIndex++
        }
        
        Timber.d("Всего извлечено ${transactions.size} транзакций из таблицы")
        
        // Возвращаем обработанные транзакции, удаляя дубликаты
        return removeDuplicateTransactions(transactions)
    }
    
    /**
     * Проверяет, что строка содержит дату, но не является частью других данных
     */
    private fun isValidDateLine(line: String, dateStr: String): Boolean {
        // Проверяем, что датой начинается строка (с небольшим отступом)
        if (!line.trim().startsWith(dateStr)) {
            return false
        }
        
        // Проверяем, что после даты следует время в формате ЧЧ:ММ
        val timePattern = "\\d{2}:\\d{2}".toRegex()
        val timeMatch = timePattern.find(line, line.indexOf(dateStr) + dateStr.length)
        
        if (timeMatch == null) {
            Timber.d("Строка содержит дату, но не содержит времени: $line")
            return false
        }
        
        // Проверяем, что строка не содержит "Дата формирования" или подобных служебных фраз
        if (line.contains("Дата формирования", ignoreCase = true) || 
            line.contains("Дата запроса", ignoreCase = true) ||
            line.contains("Продолжение", ignoreCase = true)) {
            Timber.d("Строка содержит служебную информацию: $line")
            return false
        }
        
        return true
    }

    /**
     * Специализированный метод для парсинга индивидуальной выписки с тремя столбцами
     */
    private fun parseIndividualStatement(pdfLines: List<String>, source: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        Timber.d("Анализирую выписку напрямую, строк: ${pdfLines.size}")
        
        // Список стандартных категорий в нижнем регистре для поиска
        val categoriesLc = standardCategories.map { it.lowercase(Locale.getDefault()) }
        
        // Представим все строки в виде трех столбцов даже без четкого разделения
        var i = 0
        while (i < pdfLines.size - 1) {
            val line = pdfLines[i].trim()
            if (line.isBlank()) {
                i++
                continue
            }
            
            // Ищем строки, которые начинаются с даты
            val dateMatch = "^\\d{2}\\.\\d{2}\\.\\d{4}".toRegex().find(line)
            if (dateMatch != null) {
                try {
                // Парсим дату
                    val dateStr = dateMatch.value
                    
                    // Дополнительная проверка, что это действительно строка транзакции, а не служебная информация
                    if (!isValidDateLine(line, dateStr)) {
                        Timber.d("Пропускаем строку, не прошла проверку валидности даты: $line")
                        i++
                        continue
                    }
                    
                    val date = parseDate(dateStr)
                    if (date == null) {
                        i++
                        continue
                    }
                    
                    // Ищем время (обычно следует сразу за датой)
                    val timeMatch = "\\d{2}:\\d{2}".toRegex().find(line, dateMatch.range.last + 1)
                    if (timeMatch == null) {
                        i++
                        continue
                    }
                    
                    // Обрабатываем формат с тремя столбцами: дата и время, категория/описание, сумма
                    val firstColumnEndPos = timeMatch.range.last + 1
                    
                    // Ищем сумму в конце строки (третий столбец)
                    val amountMatch = tableAmountRegex.find(line)
                    
                    if (amountMatch != null) {
                        // Получаем средний столбец между датой/временем и суммой
                        val middleColumnStart = firstColumnEndPos
                        val middleColumnEnd = amountMatch.range.first
                        
                        if (middleColumnStart < middleColumnEnd) {
                            val middleColumn = line.substring(middleColumnStart, middleColumnEnd).trim()
                            
                            // Парсим сумму
                            val amountStr = amountMatch.value
                            val parsedAmount = safeParseAmount(amountStr)
                            
                            if (parsedAmount != null && parsedAmount > Money.zero()) {
                                // Определяем тип операции (расход/доход)
                                var isExpense = !amountStr.startsWith("+")
                                
                                // Дополнительная логика для определения типа операции
                                if (middleColumn.contains("Перевод на карту", ignoreCase = true) || 
                                    middleColumn.contains("Перевод от", ignoreCase = true)) {
                                    // Переводы на карту обычно доходы
                                    isExpense = false
                                } else if (middleColumn.contains("Перевод СБП", ignoreCase = true) || 
                                          middleColumn.contains("Перевод для", ignoreCase = true)) {
                                    // Переводы СБП или для кого-то обычно расходы, если нет явного +
                                    isExpense = !amountStr.startsWith("+")
                                } else if (middleColumn.contains("Прочие расходы", ignoreCase = true) || 
                                          middleColumn.contains("Автоплатёж", ignoreCase = true)) {
                                    // Расходы и автоплатежи однозначно расходы
                                    isExpense = true
                                }
                                
                                // Определяем категорию и описание из среднего столбца
                                var category = ""
                                var description = middleColumn
                                
                                // Ищем категорию среди стандартных категорий
                                for (cat in standardCategories) {
                                    if (middleColumn.startsWith(cat, ignoreCase = true)) {
                                        category = cat
                                        break
                                    }
                                }
                                
                                // Если категория не найдена, проверяем начало строки более тщательно
                                if (category.isBlank()) {
                                    val lcMiddleColumn = middleColumn.lowercase(Locale.getDefault())
                                    
                                    // Проверяем каждую стандартную категорию
                                    val matchingCategory = categoriesLc.find { cat -> 
                                        lcMiddleColumn.startsWith(cat)
                                    }
                                    
                                    if (matchingCategory != null) {
                                        // Находим исходную категорию с правильным регистром
                                        val catIndex = categoriesLc.indexOf(matchingCategory)
                                        category = standardCategories[catIndex]
                                    } else {
                                        // Определяем категорию по содержимому
                                        category = determineCategory(middleColumn)
                                    }
                                }
                                
                                // Собираем дополнительную информацию из следующих строк
                                var additionalInfo = ""
                                var nextLineIndex = i + 1
                                
                                // Проверяем следующую строку - если это код авторизации, 
                                // то пропускаем его и берем следующую строку как описание
                                if (nextLineIndex < pdfLines.size) {
                                    val nextLine = pdfLines[nextLineIndex].trim()
                                    
                                    // Проверяем, является ли строка кодом авторизации (5-7 цифр)
                                    if (nextLine.matches("\\d{5,7}".toRegex())) {
                                        // Это код авторизации, пропускаем его
                                        nextLineIndex++
                                        
                                        // Берем следующую строку целиком как описание, если она существует и не начинается с даты
                                        if (nextLineIndex < pdfLines.size) {
                                            val descriptionLine = pdfLines[nextLineIndex].trim()
                                            
                                            // Если это не начало новой транзакции (не содержит дату в формате XX.XX.XXXX)
                                            if (!descriptionLine.contains("\\d{2}\\.\\d{2}\\.\\d{4}".toRegex())) {
                                                // Используем всю строку целиком как примечание
                                                additionalInfo = descriptionLine
                                                nextLineIndex++
                                            }
                                        }
                                    } 
                                    // Эта строка не код авторизации, значит структура другая или код отсутствует
                                    else if (!nextLine.contains("\\d{2}\\.\\d{2}\\.\\d{4}".toRegex())) {
                                        // Используем эту строку как описание
                                        additionalInfo = nextLine
                                        nextLineIndex++
                                    }
                                }
                                
                                // Fix for linter error: 'equals' with 'ignoreCase' parameter doesn't exist for String
                                // Replace category.equals("Перевод на карту", ignoreCase = true) with equals check
                                val isTransferOnCardCategory = category.equals("Перевод на карту", ignoreCase = true)

                                // Специальная обработка для операций "Перевод на карту"
                                if (description.contains("Перевод на карту", ignoreCase = true) ||
                                    description.contains("перевод с карты", ignoreCase = true) ||
                                    isTransferOnCardCategory) {
                                    // Если категория не определена, используем "Перевод на карту"
                                    if (category.isBlank()) {
                                        category = "Перевод на карту"
                                    }
                                    // Переводы часто доходы
                                    isExpense = false
                                }
                                
                                // Создаем примечание из имеющихся данных
                                val noteText = when {
                                    // Используем строку дополнительной информации если она есть
                                    additionalInfo.isNotBlank() -> {
                                        Timber.d("Используем additionalInfo для примечания: '$additionalInfo'")
                                        additionalInfo
                                    }
                                    
                                    // Используем содержимое строки с авторизацией + строки описания
                                    nextLineIndex < pdfLines.size && nextLineIndex > i + 1 -> {
                                        val authCode = pdfLines[i + 1].trim()
                                        val operationDesc = pdfLines[nextLineIndex - 1].trim()
                                        val fullDesc = "$operationDesc $authCode".trim()
                                        Timber.d("Используем составное примечание: '$fullDesc'")
                                        fullDesc
                                    }
                                    
                                    // Или описание из среднего столбца
                                    description.isNotBlank() -> {
                                        Timber.d("Используем описание для примечания: '$description'")
                                        description
                                    }
                                    
                                    // В крайнем случае формируем обобщенное описание
                                    else -> {
                                        val generatedNote = "${category}. Операция от ${datePattern.format(date)}"
                                        Timber.d("Генерируем примечание: '$generatedNote'")
                                        generatedNote
                                    }
                                }
                                
                                Timber.d("Примечание для транзакции: $noteText")
                                
                                // Проверяем, является ли транзакция переводом
                                val isTransfer = category == "Переводы" || noteText.contains("перев", ignoreCase = true)

                                // Создаем транзакцию с примечанием
                                val transaction = Transaction(
                                    id = "sber_individual_${date.time}_${parsedAmount}_${System.nanoTime()}",
                                    amount = parsedAmount,
                                    category = category,
                                    date = date,
                                    isExpense = isExpense,
                                    note = noteText,
                                    source = source,
                                    sourceColor = if (isTransfer) ColorUtils.TRANSFER_COLOR else
                                        ColorUtils.getSourceColor(source) ?: (if (isExpense) ColorUtils.EXPENSE_COLOR else ColorUtils.INCOME_COLOR),
                                    isTransfer = isTransfer
                                )
                                
                                transactions.add(transaction)
                                Timber.d("Добавлена транзакция: $transaction")
                                
                                // Переходим к следующей строке после всех обработанных
                                i = nextLineIndex - 1
                                i++
                                continue
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Ошибка при обработке строки: $line - ${e.message}")
                }
            }
            i++
        }
        
        Timber.d("Всего извлечено ${transactions.size} транзакций из индивидуальной выписки")
        
        // Возвращаем обработанные транзакции, удаляя дубликаты
        return removeDuplicateTransactions(transactions)
    }
    
    /**
     * Преобразует категорию Сбербанка в категорию приложения
     */
    private fun mapSberbankCategory(category: String): String {
        return when (category.trim().lowercase(Locale.getDefault())) {
            "супермаркеты" -> "Продукты"
            "рестораны" -> "Рестораны"
            "транспорт" -> "Транспорт"
            "одежда и обувь" -> "Одежда"
            "здоровье и красота" -> "Здоровье"
            "связь, интернет" -> "Связь"
            "коммунальные услуги" -> "Коммунальные платежи"
            "дом, ремонт" -> "Дом"
            "развлечения" -> "Развлечения"
            "прочие операции" -> "Другое"
            "переводы" -> "Переводы"
            else -> category // Возвращаем оригинальную категорию, если не найдено соответствие
        }
    }
    
    /**
     * Проверяет наличие положительных индикаторов в строке
     */
    private fun containsPositiveSign(line: String): Boolean {
        val lowerLine = line.lowercase(Locale.getDefault())
        return lowerLine.contains("возврат") || 
               lowerLine.contains("пополнение") || 
               lowerLine.contains("зачисление") ||
               lowerLine.contains("поступление")
    }
    
    /**
     * Парсит дату в формате ДД.ММ.ГГГГ
     */
    private fun parseDate(dateStr: String): Date? {
        // Логируем исходную строку
        Timber.d("Пытаемся распознать дату: $dateStr")
        
        // Список поддерживаемых форматов дат
        val dateFormats = listOf(
            "dd.MM.yyyy",     // 01.01.2023
            "dd/MM/yyyy",     // 01/01/2023
            "dd-MM-yyyy",     // 01-01-2023
            "yyyy-MM-dd"      // 2023-01-01
        )
        
        // Пробуем найти полный паттерн даты в строке
        for (format in dateFormats) {
            try {
                val dateFormat = SimpleDateFormat(format, Locale("ru"))
                val dateMatch = 
                    if (format == "dd.MM.yyyy") "\\d{2}\\.\\d{2}\\.\\d{4}".toRegex().find(dateStr)
                    else if (format == "dd/MM/yyyy") "\\d{2}/\\d{2}/\\d{4}".toRegex().find(dateStr)
                    else if (format == "dd-MM-yyyy") "\\d{2}-\\d{2}-\\d{4}".toRegex().find(dateStr)
                    else if (format == "yyyy-MM-dd") "\\d{4}-\\d{2}-\\d{2}".toRegex().find(dateStr)
                    else null
                
                if (dateMatch != null) {
                    val matchedDateStr = dateMatch.value
                    Timber.d("Найдено совпадение для формата $format: $matchedDateStr")
                    return dateFormat.parse(matchedDateStr)
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при парсинге даты в формате $format: ${e.message}")
            }
        }
        
        // Если не нашли дату в известном формате, попробуем разобрать части строки
        try {
            // Ищем разделенные цифры, которые могут быть частями даты
            val dayMatch = "\\b(0?[1-9]|[12][0-9]|3[01])\\b".toRegex().find(dateStr)
            val monthMatch = "\\b(0?[1-9]|1[0-2])\\b".toRegex().find(dateStr, startIndex = dayMatch?.range?.last ?: 0)
            val yearMatch = "\\b(20\\d{2})\\b".toRegex().find(dateStr)
            
            if (dayMatch != null && monthMatch != null && yearMatch != null) {
                val day = dayMatch.value.toInt()
                val month = monthMatch.value.toInt() - 1  // В Date месяцы начинаются с 0
                val year = yearMatch.value.toInt() - 1900 // В Date годы отсчитываются от 1900
                
                Timber.d("Найдены компоненты даты: день=$day, месяц=$month+1, год=${year+1900}")
                
                // Создаем объект Date из найденных компонентов
                val calendar = java.util.Calendar.getInstance()
                calendar.set(year + 1900, month, day, 0, 0, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                return calendar.time
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при разборе компонентов даты: ${e.message}")
        }
        
        // Если не смогли разобрать дату, возвращаем null
        Timber.w("Не удалось распознать дату: $dateStr")
        return null
    }

    /**
     * Проверяет, что файл является выпиской Сбербанка
     */
    private fun isValidSberbankStatement(text: String): Boolean {
        return (text.contains("Сбербанк", ignoreCase = true) ||
                text.contains("СБЕР", ignoreCase = true)) &&
               (text.contains("выписка", ignoreCase = true) ||
                text.contains("операции", ignoreCase = true) ||
                text.contains("Расшифровка операций", ignoreCase = true) ||
                text.contains("ДАТА ОПЕРАЦИИ", ignoreCase = true) ||
                text.contains("Дата обработки", ignoreCase = true) ||
                text.contains("СУММА В ВАЛЮТЕ СЧЁТА", ignoreCase = true)) &&
               (text.contains("дата", ignoreCase = true) ||
                text.contains("ДАТА", ignoreCase = true)) &&
               (text.contains("сумма", ignoreCase = true) ||
                text.contains("СУММА", ignoreCase = true))
    }
    
    // Требуемые методы абстрактного класса, но они не используются напрямую
    // из-за переопределения основного метода invoke
    
    override fun isValidFormat(reader: BufferedReader): Boolean {
        val text = reader.readText()
        return isValidSberbankStatement(text)
    }

    override fun skipHeaders(reader: BufferedReader) {
        // Не используется для PDF
    }

    override fun parseLine(line: String): Transaction {
        // Этот метод не будет использован для PDF
        throw NotImplementedError("Метод не реализован для PDF, используется специализированная логика")
    }

    /**
     * Извлекает дату из строки текста
     */
    private fun extractDateFromString(text: String): LocalDate? {
        // Различные форматы дат, которые могут встречаться в выписке
        val datePatterns = listOf(
            // Стандартный формат ДД.ММ.ГГГГ
            "(\\d{2}\\.\\d{2}\\.\\d{4})",
            // ДД месяц ГГГГ (например, 12 января 2023)
            "(\\d{1,2})\\s+([а-яА-Я]+)\\s+(\\d{4})"
        )

        for (pattern in datePatterns) {
            val matches = pattern.toRegex().findAll(text).toList()
            if (matches.isNotEmpty()) {
                val dateString = matches.first().value
                
                try {
                    // Для формата ДД.ММ.ГГГГ
                    if (dateString.matches("\\d{2}\\.\\d{2}\\.\\d{4}".toRegex())) {
                        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        return LocalDate.parse(dateString, formatter)
                    } 
                    // Для формата ДД месяц ГГГГ
                    else {
                        val parts = dateString.split(" ")
                        val day = parts[0].toInt()
                        val month = when (parts[1].lowercase()) {
                            "января" -> 1
                            "февраля" -> 2
                            "марта" -> 3
                            "апреля" -> 4
                            "мая" -> 5
                            "июня" -> 6
                            "июля" -> 7
                            "августа" -> 8
                            "сентября" -> 9
                            "октября" -> 10
                            "ноября" -> 11
                            "декабря" -> 12
                            else -> return null
                        }
                        val year = parts[2].toInt()
                        return LocalDate.of(year, month, day)
                    }
                } catch (e: Exception) {
                    // Если не удалось преобразовать дату, продолжаем поиск
                    continue
                }
            }
        }

        // Если не нашли дату обычными способами, пробуем найти любые упоминания чисел и месяцев
        val dayNumber = "\\b(\\d{1,2})\\b".toRegex().find(text)
        val monthMention = "\\b(январ|феврал|март|апрел|ма[йя]|июн|июл|август|сентябр|октябр|ноябр|декабр)\\w*\\b"
            .toRegex(RegexOption.IGNORE_CASE).find(text)
        val yearMention = "\\b(20\\d{2})\\b".toRegex().find(text)
        
        if (dayNumber != null && monthMention != null && yearMention != null) {
            val day = dayNumber.value.toInt()
            val month = when (monthMention.value.lowercase().take(3)) {
                "янв" -> 1
                "фев" -> 2
                "мар" -> 3
                "апр" -> 4
                "май", "мая" -> 5
                "июн" -> 6
                "июл" -> 7
                "авг" -> 8
                "сен" -> 9
                "окт" -> 10
                "ноя" -> 11
                "дек" -> 12
                else -> return null
            }
            val year = yearMention.value.toInt()
            
            // Проверяем, что день в допустимых пределах
            if (day in 1..31) {
                try {
                    return LocalDate.of(year, month, day)
                } catch (e: Exception) {
                    // Игнорируем невалидные даты (например, 31 февраля)
                }
            }
        }
        
        return null
    }

    /**
     * Извлекает примечание из описания операции
     */
    private fun extractNoteFromDescription(description: String): String? {
        // Проверяем на системные сообщения Android Studio
        if (description.contains("looks like you just edited", ignoreCase = true) ||
            description.contains("Toggle info", ignoreCase = true) ||
            description.contains("Android SDK", ignoreCase = true)) {
            Timber.d("Обнаружено системное сообщение в примечании, игнорируем: ${description.take(50)}...")
            return null
        }
        
        // Удаляем дату и время из начала строки
        var note = description.replace("\\d{2}\\.\\d{2}\\.\\d{4}\\s+\\d{2}:\\d{2}".toRegex(), "").trim()
        
        // Удаляем категорию из начала строки
        val categories = listOf(
            "Здоровье и красота",
            "Прочие операции",
            "Переводы",
            "Супермаркеты",
            "Рестораны",
            "Транспорт",
            "Связь",
            "Одежда и обувь",
            "Коммунальные услуги",
            "Дом и ремонт",
            "Развлечения",
            "Отели",
            "Авто",
            "Топливо",
            "Фастфуд"
        )
        
        for (category in categories) {
            if (note.startsWith(category, ignoreCase = true)) {
                note = note.substring(category.length).trim()
                break
            }
        }
        
        // Удаляем сумму из конца строки
        note = note.replace("\\d+[\\s]?\\d+[,.]\\d{2}".toRegex(), "").trim()
        
        // Удаляем стандартные фразы Сбербанка
        note = note.replace("Операция по карте.*".toRegex(), "")
            .replace("Карта.*".toRegex(), "")
            .replace("Сбербанк.*".toRegex(), "")
            .replace("Сбер.*".toRegex(), "")
            .replace("\\s+".toRegex(), " ")
            .trim()
        
        // Дополнительная проверка на системные сообщения IDE после очистки
        if (note.contains("targetSdkVersion", ignoreCase = true) ||
            note.contains("⌘F1", ignoreCase = true)) {
            Timber.d("После очистки обнаружено системное сообщение IDE, игнорируем: $note")
            return null
        }
        
        // Если после очистки остался только номер карты, возвращаем null
        if (note.matches("\\*+\\s*\\d+".toRegex())) {
            return null
        }
        
        // Если после очистки осталась пустая строка, возвращаем null
        if (note.isBlank()) {
            return null
        }
        
        return note
    }

    /**
     * Пытается найти сумму в строке, учитывая различные форматы
     */
    private fun findAmountInString(text: String): Pair<Money, Boolean>? {
        // Улучшенный паттерн для поиска сумм
        val amountRegexPatterns = listOf(
            // Сумма с пробелами и запятой: 1 000,00
            "([+-]?\\d{1,3}(\\s\\d{3})*[,.]\\d{2})".toRegex(),
            // Сумма без пробелов с запятой: 1000,00
            "([+-]?\\d+[,.]\\d{2})".toRegex(),
            // Сумма с руб./₽: 1000,00 руб.
            "(\\d+[,.]\\d{2})\\s*(₽|руб)".toRegex(),
            // Сумма со знаком +/- перед ней: +1000,00
            "([+-]\\s*\\d+[,.]\\d{2})".toRegex(),
            // Формат для выписки с "расшифровкой операций"
            "([+-]?\\d{1,3}(\\s\\d{3})*[,.]\\d{2})\\s*$".toRegex()
        )

        // Проверяем каждый паттерн
        for (pattern in amountRegexPatterns) {
            val matches = pattern.findAll(text).toList()
            if (matches.isNotEmpty()) {
                // Берем последнее совпадение в строке (обычно это и есть сумма операции)
                val match = matches.last()
                val matchedText = match.groupValues[0].trim()
                
                Timber.d("Найдена сумма: '$matchedText' в тексте: '${text.take(50)}...'")
                
                // Извлекаем числовое значение, убирая все нецифровые символы, кроме точки и запятой
                var amountStr = matchedText.replace("[^0-9,.+-]".toRegex(), "")
                    .replace(",", ".")
                
                // По умолчанию, если нет явного + в начале суммы, считаем операцию расходом
                var isExpense = !amountStr.startsWith("+")
                
                // Убираем знаки +/- для конвертации в число
                amountStr = amountStr.removePrefix("+").removePrefix("-")
                
                // Проверяем контекст на ключевые слова для определения типа операции
                if (text.contains("зачисление", ignoreCase = true) ||
                    text.contains("поступление", ignoreCase = true) ||
                    text.contains("возврат", ignoreCase = true) ||
                    (text.contains("перевод", ignoreCase = true) && 
                     text.contains("на карту", ignoreCase = true) &&
                     !text.contains("с карты", ignoreCase = true))) {
                    isExpense = false
                }
                
                // Дополнительно проверяем явный знак "+" в начале строки суммы
                if (matchedText.startsWith("+")) {
                    isExpense = false
                }
                
                // Проверяем отдельно для выписки с "Расшифровкой операций"
                if (text.contains("Перевод на карту", ignoreCase = true) || 
                    text.contains("Перевод от", ignoreCase = true)) {
                    isExpense = false 
                }
                
                // Явные признаки расхода
                if (text.contains("Перевод СБП", ignoreCase = true) || 
                    text.contains("Перевод для", ignoreCase = true) || 
                    text.contains("Прочие расходы", ignoreCase = true) || 
                    text.contains("Автоплатёж", ignoreCase = true)) {
                    isExpense = true
                }
                
                try {
                    // Используем наш безопасный метод для парсинга числа
                    val amount = safeParseAmount(amountStr)
                    if (amount == null) {
                        Timber.e("Не удалось преобразовать строку суммы в число: $amountStr")
                        continue
                    }
                    return Pair(amount, isExpense)
                } catch (e: Exception) {
                    // Если не удалось преобразовать в число, продолжаем поиск
                    continue
                }
            }
        }
        
        // Если ни один паттерн не сработал, пробуем найти просто числа с запятой/точкой
        val simpleAmount = "(\\d+[,.]\\d{2})".toRegex().find(text)
        if (simpleAmount != null) {
            val amountStr = simpleAmount.value.replace(",", ".")
            try {
                // Используем наш безопасный метод для парсинга числа
                val amount = safeParseAmount(amountStr)
                if (amount == null) {
                    Timber.e("Не удалось преобразовать строку суммы в число: $amountStr")
                    return null
                }
                // По умолчанию считаем операцию расходом, если нет явных признаков поступления
                var isExpense = true
                
                if (text.contains("зачисление", ignoreCase = true) ||
                    text.contains("поступление", ignoreCase = true) ||
                    text.contains("возврат", ignoreCase = true) ||
                    (text.contains("перевод", ignoreCase = true) && 
                     text.contains("на карту", ignoreCase = true) &&
                     !text.contains("с карты", ignoreCase = true))) {
                    isExpense = false
                }
                
                return Pair(amount, isExpense)
            } catch (e: Exception) {
                // Игнорируем ошибки преобразования
            }
        }
        
        return null
    }

    /**
     * Определяет категорию транзакции на основе описания
     */
    private fun determineCategory(description: String): String {
        // Сначала проверяем стандартные категории Сбербанка и сохраняем их как есть
        val sberCategories = listOf(
            "Здоровье и красота",
            "Прочие операции",
            "Переводы",
            "Супермаркеты",
            "Рестораны",
            "Транспорт",
            "Связь",
            "Одежда и обувь",
            "Коммунальные услуги",
            "Дом и ремонт",
            "Развлечения",
            "Отели",
            "Авто",
            "Топливо",
            "Фастфуд"
        )
        
        // Проверяем наличие стандартной категории Сбербанка
        for (sberCategory in sberCategories) {
            if (description.contains(sberCategory, ignoreCase = true)) {
                return sberCategory
            }
        }
        
        // Если стандартная категория не найдена, определяем по ключевым словам
        val lowerDesc = description.lowercase(Locale.getDefault())
        
        return when {
            lowerDesc.contains("перевод") && (lowerDesc.contains("на карту") || 
                                            lowerDesc.contains("поступление")) -> "Переводы"
            lowerDesc.contains("перевод") || lowerDesc.contains("пополнение") -> "Переводы"
            lowerDesc.contains("снятие") || lowerDesc.contains("банкомат") -> "Наличные"
            lowerDesc.contains("пятерочка") || lowerDesc.contains("магнит") ||
            lowerDesc.contains("ашан") || lowerDesc.contains("лента") || 
            lowerDesc.contains("супермаркет") -> "Супермаркеты"
            lowerDesc.contains("аптека") || lowerDesc.contains("больница") ||
            lowerDesc.contains("клиника") || lowerDesc.contains("здоровье") -> "Здоровье и красота"
            lowerDesc.contains("такси") || lowerDesc.contains("метро") ||
            lowerDesc.contains("автобус") -> "Транспорт"
            lowerDesc.contains("ресторан") || lowerDesc.contains("кафе") ||
            lowerDesc.contains("кофейня") -> "Рестораны"
            lowerDesc.contains("жкх") || lowerDesc.contains("коммунальн") ||
            lowerDesc.contains("свет") || lowerDesc.contains("газ") ||
            lowerDesc.contains("вода") -> "Коммунальные услуги"
            lowerDesc.contains("зарплата") || lowerDesc.contains("аванс") -> "Зарплата"
            lowerDesc.contains("мтс") || lowerDesc.contains("мегафон") ||
            lowerDesc.contains("билайн") || lowerDesc.contains("теле2") -> "Связь"
            lowerDesc.contains("одежда") || lowerDesc.contains("обувь") -> "Одежда и обувь"
            else -> "Прочие операции"
        }
    }

    /**
     * Удаляет дублирующиеся транзакции из списка
     */
    private fun removeDuplicateTransactions(transactions: List<Transaction>): List<Transaction> {
        // Группируем транзакции по дате и сумме
        val groupedTransactions = transactions.groupBy { "${it.date.time}_${it.amount}_${it.isExpense}" }
        
        // Результирующий список без дубликатов
        val uniqueTransactions = mutableListOf<Transaction>()
        
        // Проходим по каждой группе и выбираем одну транзакцию из группы
        for ((_, group) in groupedTransactions) {
            if (group.size > 1) {
                // Если есть дубликаты, логируем это и выбираем транзакцию с наиболее информативным описанием
                Timber.d("Найдены ${group.size} дублирующиеся транзакции с датой ${group.first().date} и суммой ${group.first().amount}")
                
                // Выбираем транзакцию с наиболее длинным описанием, предполагая, что она содержит больше информации
                val bestTransaction = group.maxByOrNull { it.note?.length ?: 0 } ?: group.first()
                uniqueTransactions.add(bestTransaction)
            } else {
                // Если нет дубликатов, просто добавляем транзакцию в результат
                uniqueTransactions.add(group.first())
            }
        }
        
        Timber.d("Удалено ${transactions.size - uniqueTransactions.size} дубликатов, осталось ${uniqueTransactions.size} уникальных транзакций")
        return uniqueTransactions
    }
} 
