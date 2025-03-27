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
    
    // Паттерны для парсинга PDF-выписки
    private val datePattern = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    
    // Паттерны для обработки выписок Сбербанка
    private val dateRegex = "\\d{2}\\.\\d{2}\\.\\d{4}".toRegex()
    private val amountRegex = "([+-]?\\d+[\\s]?\\d+[,.]\\d{2})".toRegex()
    private val amountRegexImproved = "(([+-]?\\d+[\\s]?\\d+[,.]\\d{2})|(\\d+[,.]\\d{2}))".toRegex()
    private val cardNumberPattern = Pattern.compile("Карта\\s+([*•\\d]+)\\s+", Pattern.MULTILINE)
    
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
            
            // Открываем PDF файл
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Не удалось открыть файл")
            
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
                if (amount > 0) {
                    val category = determineCategory(transactionBlock)
                    
                    val note = extractNoteFromDescription(transactionBlock)
                    
                    val transaction = Transaction(
                        id = "sber_pdf_${date.time}_${System.nanoTime()}",
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
     * Пытается найти сумму в строке, учитывая различные форматы
     */
    private fun findAmountInString(text: String): Pair<Double, Boolean>? {
        // Улучшенный паттерн для поиска сумм
        val amountRegexPatterns = listOf(
            // Сумма с пробелами и запятой: 1 000,00
            "([+-]?\\d{1,3}(\\s\\d{3})*[,.]\\d{2})".toRegex(),
            // Сумма без пробелов с запятой: 1000,00
            "([+-]?\\d+[,.]\\d{2})".toRegex(),
            // Сумма с руб./₽: 1000,00 руб.
            "(\\d+[,.]\\d{2})\\s*(₽|руб)".toRegex(),
            // Сумма со знаком +/- перед ней: +1000,00
            "([+-]\\s*\\d+[,.]\\d{2})".toRegex()
        )

        // Проверяем каждый паттерн
        for (pattern in amountRegexPatterns) {
            val matches = pattern.findAll(text).toList()
            if (matches.isNotEmpty()) {
                // Берем последнее совпадение в строке (обычно это и есть сумма операции)
                val match = matches.last()
                val matchedText = match.groupValues[0].trim()
                
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
                
                try {
                    val amount = amountStr.toDouble()
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
                val amount = amountStr.toDouble()
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
     * Парсит блок операции и возвращает транзакцию
     */
    private fun parseOperationBlock(block: String, source: String): Transaction? {
        try {
            // Разбиваем блок на строки для анализа
            val lines = block.split("\n").filter { it.isNotBlank() }
            if (lines.isEmpty()) return null
            
            // Попробуем определить формат из примера: дата время категория, вторая строка - операция, сумма справа
            if (lines.size >= 2) {
                // Первая строка обычно содержит дату, время и категорию
                val firstLine = lines[0]
                
                // Ищем дату с форматом ДД.ММ.ГГГГ
                val dateMatch = dateRegex.find(firstLine) ?: return null
                val dateStr = dateMatch.value
                
                // Ищем время в формате ЧЧ:ММ после даты
                val timePattern = "\\d{2}:\\d{2}".toRegex()
                val timeMatch = timePattern.find(firstLine, dateMatch.range.last)
                
                // Извлекаем категорию - обычно это текст между временем и концом строки или суммой
                val categoryStartIndex = if (timeMatch != null) timeMatch.range.last + 1 else dateMatch.range.last + 1
                var categoryEndIndex = firstLine.length
                
                // Если в первой строке есть сумма, обрезаем категорию до этой суммы
                val firstLineAmountMatch = amountRegex.find(firstLine)
                if (firstLineAmountMatch != null) {
                    categoryEndIndex = firstLineAmountMatch.range.first
                }
                
                var category = if (categoryStartIndex < categoryEndIndex) {
                    firstLine.substring(categoryStartIndex, categoryEndIndex).trim()
                } else {
                    "Другое" // По умолчанию, если не удалось определить категорию
                }
                
                // Обработаем случай "Прочие операции" и "Здоровье и красота"
                if (category == "Прочие операции") {
                    category = "Другое"
                } else if (category == "Здоровье и красота") {
                    category = "Здоровье"
                } else if (category == "Перевод на карту") {
                    category = "Переводы"
                }
                
                // Пытаемся найти сумму в каждой строке блока, начиная с последней,
                // так как сумма обычно указывается в конце или на отдельной строке
                var amount = 0.0
                var amountFound = false
                var isExpense = true
                
                // Пытаемся найти сумму в правой части строк
                for (i in lines.indices.reversed()) {
                    val line = lines[i]
                    
                    // Используем улучшенную функцию поиска суммы
                    val amountResult = findAmountInString(line)
                    if (amountResult != null) {
                        amount = amountResult.first
                        isExpense = amountResult.second
                        amountFound = true
                        break
                    }
                }
                
                if (!amountFound) return null
                
                // Создаем описание - объединяем все строки кроме первой
                val description = if (lines.size > 1) {
                    lines.subList(1, lines.size).joinToString(" ").trim()
                } else {
                    ""
                }
                
                // Парсим дату
                val date = try {
                    datePattern.parse(dateStr) ?: return null
                } catch (e: Exception) {
                    return null
                }
                
                return Transaction(
                    id = "sber_pdf_${date.time}_${System.nanoTime()}",
                    amount = amount.absoluteValue,
                    category = category,
                    isExpense = isExpense,
                    date = date,
                    note = description.takeIf { it.isNotBlank() },
                    source = source
                )
            }
            
            // Если не смогли обработать новый формат, используем старую логику
            val dateMatch = dateRegex.find(block) ?: return null
            val date = try {
                datePattern.parse(dateMatch.value)
            } catch (e: Exception) {
                return null
            }
            
            // Используем улучшенную функцию поиска суммы
            val amountResult = findAmountInString(block)
            if (amountResult == null) return null
            
            val amount = amountResult.first
            val isExpense = amountResult.second
            
            // Определяем категорию
            val category = determineCategoryFromBlock(block)
            
            // Создаем описание
            val description = block
                .replace(dateRegex, "")
                .replace(amountRegex, "")
                .replace("\\s+".toRegex(), " ")
                .trim()
            
            return Transaction(
                id = "sber_pdf_${date.time}_${System.nanoTime()}",
                amount = amount.absoluteValue,
                category = category,
                isExpense = isExpense,
                date = date,
                note = description.takeIf { it.isNotBlank() },
                source = source
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Определяет категорию на основе блока операции
     */
    private fun determineCategoryFromBlock(block: String): String {
        // Список стандартных категорий Сбербанка
        val sberCategories = mapOf(
            "Здоровье и красота" to "Здоровье",
            "Прочие операции" to "Другое",
            "Переводы" to "Переводы",
            "Супермаркеты" to "Продукты",
            "Рестораны" to "Рестораны",
            "Транспорт" to "Транспорт",
            "Связь" to "Связь",
            "Одежда и обувь" to "Одежда",
            "Коммунальные услуги" to "Коммунальные платежи",
            "Дом и ремонт" to "Дом"
        )
        
        // Проверяем наличие категории Сбербанка в блоке
        for ((sberCategory, appCategory) in sberCategories) {
            if (block.contains(sberCategory, ignoreCase = true)) {
                return appCategory
            }
        }
        
        // Если стандартная категория не найдена, пробуем определить по ключевым словам
        return determineCategory(block)
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
     * Проверяет, что файл является выпиской Сбербанка
     */
    private fun isValidSberbankStatement(text: String): Boolean {
        return text.contains("Сбербанк", ignoreCase = true) &&
               (text.contains("выписка", ignoreCase = true) ||
                text.contains("операции", ignoreCase = true)) &&
               text.contains("дата", ignoreCase = true) &&
               text.contains("сумма", ignoreCase = true)
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
} 