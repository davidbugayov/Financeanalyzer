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
import kotlinx.coroutines.delay
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
import timber.log.Timber

/**
 * Реализация импорта транзакций из PDF-выписки Т-Банка.
 * Поддерживает формат PDF-выписки из мобильного приложения и веб-версии.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class TBankPdfImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context
) : BankImportUseCase(repository, context) {

    companion object {
        private const val PDF_PARSING_TIMEOUT = 60000L // 60 секунд таймаут
        
        // Регулярные выражения для парсинга данных
        private val DATE_PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})(\\s+\\d{2}:\\d{2}(:\\d{2})?)?")
        
        // Более точный шаблон для сумм с обязательным знаком рубля или полным словом 'руб'
        private val AMOUNT_PATTERN = Pattern.compile("([-+]?\\s*\\d+[\\s\\d]*[.,]\\d{2})\\s*(?:₽|руб\\.?|RUB)")
        
        // Запасной паттерн для сумм без валюты, когда валюта указана в другом месте
        private val AMOUNT_FALLBACK_PATTERN = Pattern.compile("([-+]?\\s*\\d+[\\s\\d]*[.,]\\d{2})(?!\\.|\\d)")
        
        private val OPERATION_NUMBER_PATTERN = Pattern.compile("(\\d{6,})")
    }

    override val bankName: String = "Т-Банк"

    // Форматы даты, используемые в выписках Т-Банка
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru"))
    private val shortDateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

    /**
     * Т.к. этот UseCase обрабатывает только PDF, эти методы не используются
     */
    override fun isValidFormat(reader: BufferedReader): Boolean = false

    /**
     * Т.к. этот UseCase обрабатывает только PDF, эти методы не используются
     */
    override fun skipHeaders(reader: BufferedReader) {
        // Not used for PDF
    }

    /**
     * Т.к. этот UseCase обрабатывает только PDF, эти методы не используются
     */
    override fun parseLine(line: String): Transaction {
        throw UnsupportedOperationException("Метод не применим для PDF-файлов")
    }

    /**
     * Основной метод, который выполняет импорт транзакций из PDF-файла Т-Банка
     */
    override suspend fun invoke(uri: Uri): Flow<ImportResult> = flow {
        try {
            emit(ImportResult.Progress(1, 100, "Открытие PDF-файла выписки Т-Банка"))
            
            // Инициализируем PDFBox для Android, если это ещё не сделано
            withContext(Dispatchers.IO) {
                if (!PDFBoxResourceLoader.isReady()) {
                    PDFBoxResourceLoader.init(context)
                }
            }
            
            // Используем таймаут для чтения PDF содержимого и выполняем в IO-потоке
            val pdfLines = withTimeoutOrNull(PDF_PARSING_TIMEOUT) {
                withContext(Dispatchers.IO) {
                    readPdfContent(uri)
                }
            } ?: run {
                emit(ImportResult.Error("Превышено время ожидания при чтении PDF-файла. Файл может быть слишком большим или поврежденным."))
                return@flow
            }
            
            if (pdfLines.isEmpty()) {
                emit(ImportResult.Error("Не удалось прочитать PDF-файл или файл пуст"))
                return@flow
            }
            
            Timber.d("T-БАНК PDF: Прочитано ${pdfLines.size} строк из PDF")
            
            // Проверяем наличие заголовка таблицы, который подтверждает формат Т-Банка
            val hasTBankHeader = withContext(Dispatchers.Default) {
                pdfLines.any { 
                    it.contains("Т-Банк", ignoreCase = true) || 
                    it.contains("Выписка по счету", ignoreCase = true) || 
                    it.contains("История операций", ignoreCase = true)
                }
            }
            
            if (!hasTBankHeader) {
                Timber.w("T-БАНК PDF: Не найден заголовок таблицы Т-Банка")
                emit(ImportResult.Error("Данный файл не является выпиской Т-Банка или имеет неизвестный формат"))
                return@flow
            }
            
            // Всегда используем "Т-Банк" как источник
            val source = "Т-Банк"
            
            // Парсим данные из PDF и преобразуем их в транзакции
            emit(ImportResult.Progress(20, 100, "Анализ выписки"))
            
            // Используем таймаут для парсинга транзакций и выполняем в Default-потоке
            val transactions = withTimeoutOrNull(PDF_PARSING_TIMEOUT) {
                withContext(Dispatchers.Default) {
                    parsePdfTransactions(pdfLines, source)
                }
            } ?: run {
                emit(ImportResult.Error("Превышено время ожидания при обработке данных PDF-файла"))
                return@flow
            }
            
            if (transactions.isEmpty()) {
                emit(ImportResult.Error("Не найдено транзакций в файле"))
                return@flow
            }
            
            Timber.d("T-БАНК PDF: Найдено ${transactions.size} транзакций")
            
            // Сохраняем транзакции в базу данных
            emit(ImportResult.Progress(70, 100, "Сохранение транзакций"))
            
            var importedCount = 0
            var skippedCount = 0
            var totalAmount = 0.0
            val importedTransactions = mutableListOf<Transaction>()
            
            // Разбиваем сохранение на пакеты для предотвращения ANR
            val batchSize = 10
            val transactionBatches = transactions.chunked(batchSize)
            
            for ((batchIndex, batch) in transactionBatches.withIndex()) {
                // Выполняем сохранение пакета в IO-потоке
                withContext(Dispatchers.IO) {
                    for (transaction in batch) {
                        try {
                            repository.addTransaction(transaction)
                            importedCount++
                            importedTransactions.add(transaction)
                            totalAmount += if (transaction.isExpense) -transaction.amount else transaction.amount
                        } catch (e: Exception) {
                            skippedCount++
                            Timber.e(e, "Ошибка при сохранении транзакции: ${e.message}")
                        }
                    }
                }
                
                // Обновляем прогресс после каждого пакета
                val progress = 70 + (batchIndex.toFloat() / transactionBatches.size * 30).toInt()
                emit(ImportResult.Progress(progress, 100, "Сохранение транзакций: обработано ${(batchIndex + 1) * batchSize} из ${transactions.size}"))
                
                // Маленькая пауза между пакетами, чтобы дать UI-потоку возможность обновиться
                delay(50)
            }
            
            // Отправляем результат успешного импорта
            emit(ImportResult.Success(importedCount, skippedCount, totalAmount, importedTransactions))
            
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
            
            val document = PDDocument.load(inputStream)
            
            val stripper = PDFTextStripper()
            val pdfText = stripper.getText(document)
            
            val endTime = System.currentTimeMillis()
            Timber.d("Чтение PDF-файла Т-Банка завершено за ${endTime - startTime} мс")
            
            document.close()
            inputStream.close()
            
            // Предварительная обработка текста для удаления лишних пробелов и разбиение на строки
            return pdfText.split("\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при чтении PDF Т-Банка: ${e.message}")
            inputStream.close()
            throw e
        }
    }

    /**
     * Парсит транзакции из текста PDF-файла Т-Банка
     */
    private suspend fun parsePdfTransactions(lines: List<String>, source: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        // Выводим первые 30 строк для отладки
        lines.take(30).forEachIndexed { index, line ->
            Timber.d("T-БАНК PDF DEBUG: Строка $index: '$line'")
        }
        
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            
            // Проверяем, есть ли дата в строке (основной идентификатор начала транзакции)
            val dateMatcher = DATE_PATTERN.matcher(line)
            if (dateMatcher.find()) {
                try {
                    // Нашли строку с датой - начало транзакции
                    val dateString = dateMatcher.group(0) ?: continue
                    Timber.d("T-БАНК PDF: Найдена строка с датой: $dateString")
                    
                    // Получаем дату операции
                    val date = parseDate(dateString)
                    
                    // Ищем код документа для идентификации транзакции
                    val documentId = extractDocumentId(line)
                    
                    // Собираем полное описание транзакции
                    val fullDescriptionBuilder = StringBuilder()
                    fullDescriptionBuilder.append(extractDescription(line))
                    
                    // Ищем сумму операции и дополнительное описание
                    var amountFound = false
                    var amount = 0.0
                    var isExpense = true
                    
                    // Собираем все строки до следующей даты операции
                    val transactionLines = mutableListOf<String>()
                    transactionLines.add(line)
                    
                    var nextLineIndex = i + 1
                    while (nextLineIndex < lines.size && 
                           !DATE_PATTERN.matcher(lines[nextLineIndex]).find() && 
                           nextLineIndex - i < 10) { // не более 10 строк от начала транзакции
                           
                        val nextLine = lines[nextLineIndex]
                        transactionLines.add(nextLine)
                        
                        // Добавляем информацию в описание, если это не служебная строка
                        if (!shouldSkipLineInDescription(nextLine)) {
                            val desc = extractDescription(nextLine)
                            if (desc.isNotBlank() && !fullDescriptionBuilder.contains(desc)) {
                                if (fullDescriptionBuilder.isNotEmpty()) {
                                    fullDescriptionBuilder.append(" ")
                                }
                                fullDescriptionBuilder.append(desc)
                            }
                        }
                        
                        nextLineIndex++
                    }
                    
                    // Ищем сумму в объединенных строках
                    val fullText = transactionLines.joinToString(" ")
                    
                    // Пробуем найти сумму с символом валюты (основной паттерн)
                    val amountMatcher = AMOUNT_PATTERN.matcher(fullText)
                    
                    if (amountMatcher.find()) {
                        // Проверяем, что найденное значение не является датой (формат ДД.ММ.ГГГГ)
                        val matchedValue = amountMatcher.group(1) ?: ""
                        if (!matchedValue.matches("\\d{2}\\.\\d{2}\\.\\d{4}".toRegex())) {
                            val amountStr = matchedValue
                                .replace(" ", "")
                                .replace(",", ".")
                                .replace("[^0-9.+-]".toRegex(), "")
                            
                            // Определяем тип операции по описанию и знаку суммы
                            isExpense = if (amountStr.startsWith("-")) {
                                true // Расход, если перед суммой стоит минус
                            } else if (amountStr.startsWith("+")) {
                                false // Доход, если перед суммой стоит плюс
                            } else if (fullText.contains("пополнение", ignoreCase = true) || 
                                     fullText.contains("система быстрых платежей", ignoreCase = true) ||
                                     fullText.contains("зачисление", ignoreCase = true)) {
                                // Если в описании есть ключевые слова о пополнении
                                false // Это доход
                            } else {
                                // Если нет знака, определяем по другим ключевым словам
                                fullText.contains("списание", ignoreCase = true) ||
                                fullText.contains("оплата", ignoreCase = true)
                            }
                            
                            // Извлекаем числовое значение суммы
                            val cleanAmount = amountStr.replace("[^0-9.]".toRegex(), "")
                            amount = cleanAmount.toDoubleOrNull() ?: 0.0
                            
                            if (amount > 0) {
                                amountFound = true
                                Timber.d("T-БАНК PDF: Найдена сумма с символом валюты: ${if (isExpense) "-" else "+"} $amount руб.")
                            }
                        } else {
                            Timber.d("T-БАНК PDF: Найденное значение похоже на дату, пропускаем: $matchedValue")
                        }
                    }
                    
                    // Если не нашли сумму с символом валюты, пробуем запасной паттерн
                    if (!amountFound) {
                        val fallbackMatcher = AMOUNT_FALLBACK_PATTERN.matcher(fullText)
                        while (fallbackMatcher.find() && !amountFound) {
                            val matchedValue = fallbackMatcher.group(1) ?: ""
                            
                            // Пропускаем если это похоже на дату
                            if (matchedValue.matches("\\d{2}\\.\\d{2}\\.\\d{4}".toRegex()) || 
                                matchedValue.matches("\\d{2}\\.\\d{2}\\.\\d{2}".toRegex())) {
                                Timber.d("T-БАНК PDF: Пропускаем значение похожее на дату: $matchedValue")
                                continue
                            }
                            
                            val amountStr = matchedValue
                                .replace(" ", "")
                                .replace(",", ".")
                                .replace("[^0-9.+-]".toRegex(), "")
                            
                            // Определяем тип операции
                            isExpense = if (amountStr.startsWith("-")) {
                                true // Расход, если перед суммой стоит минус
                            } else if (amountStr.startsWith("+")) {
                                false // Доход, если перед суммой стоит плюс
                            } else if (fullText.contains("пополнение", ignoreCase = true) || 
                                     fullText.contains("система быстрых платежей", ignoreCase = true) ||
                                     fullText.contains("зачисление", ignoreCase = true)) {
                                // Если в описании есть ключевые слова о пополнении
                                false // Это доход
                            } else {
                                // Если нет знака, определяем по другим ключевым словам
                                fullText.contains("списание", ignoreCase = true) ||
                                fullText.contains("оплата", ignoreCase = true)
                            }
                            
                            // Извлекаем числовое значение суммы
                            val cleanAmount = amountStr.replace("[^0-9.]".toRegex(), "")
                            amount = cleanAmount.toDoubleOrNull() ?: 0.0
                            
                            // Проверяем, что нашли реальную сумму а не номер договора/счета
                            if (amount > 0 && amount < 1000000) { // Обычно суммы транзакций меньше миллиона
                                amountFound = true
                                Timber.d("T-БАНК PDF: Найдена сумма без символа валюты: ${if (isExpense) "-" else "+"} $amount руб.")
                                break
                            }
                        }
                    }
                    
                    // Учитываем сколько строк мы обработали
                    if (nextLineIndex > i + 1) {
                        i = nextLineIndex - 1  // -1 потому что в конце цикла будет i++
                    }
                    
                    // Финальное описание транзакции
                    val descriptionText = fullDescriptionBuilder.toString().trim()
                    
                    if (amountFound && amount > 0) {
                        // Определяем категорию транзакции
                        val category = inferCategoryFromDescription(descriptionText, isExpense)
                        
                        // Создаем транзакцию
                        val transaction = Transaction(
                            id = "tbank_${documentId}_${date.time}_${System.nanoTime()}",
                            amount = amount,
                            category = category,
                            isExpense = isExpense,
                            date = date,
                            note = if (descriptionText.isNotBlank()) descriptionText else null,
                            source = source
                        )
                        
                        transactions.add(transaction)
                        Timber.d("T-БАНК PDF: Добавлена транзакция: ${if (isExpense) "-" else "+"} $amount руб., $descriptionText")
                    } else {
                        Timber.w("T-БАНК PDF: Не удалось найти сумму для транзакции с датой $dateString, описание: $descriptionText")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "T-БАНК PDF: Ошибка при обработке строки с транзакцией: ${e.message}")
                }
            }
            i++
        }
        
        Timber.d("T-БАНК PDF: Всего найдено ${transactions.size} транзакций")
        return transactions
    }

    /**
     * Извлекает ID документа из строки
     */
    private fun extractDocumentId(line: String): String {
        val matcher = OPERATION_NUMBER_PATTERN.matcher(line)
        return if (matcher.find()) {
            matcher.group(1) ?: "unknown_${System.currentTimeMillis()}"
        } else {
            "unknown_${System.currentTimeMillis()}"
        }
    }
    
    /**
     * Парсит дату из строки
     */
    private fun parseDate(dateString: String): Date {
        return try {
            if (dateString.contains(":")) {
                dateFormatter.parse(dateString) ?: Date()
            } else {
                shortDateFormatter.parse(dateString) ?: Date()
            }
        } catch (e: Exception) {
            Timber.w(e, "T-БАНК PDF: Ошибка при парсинге даты: $dateString")
            Date()
        }
    }

    /**
     * Извлекает описание операции из строки, убирая служебную информацию
     */
    private fun extractDescription(line: String): String {
        // Удаляем дату и номер документа
        var desc = line.replace(DATE_PATTERN.pattern().toRegex(), "")
            .replace(OPERATION_NUMBER_PATTERN.pattern().toRegex(), "")
            
        // Удаляем лишние пробелы и специальные символы
        desc = desc.replace("\\s+".toRegex(), " ")
            .replace("\\t", " ")
            .replace("Назначение платежа", "", ignoreCase = true)
            .replace("Документ", "", ignoreCase = true)
            .replace("Сумма операции", "", ignoreCase = true)
            .replace("[+-]?\\s*\\d+[\\s\\d]*[.,]\\d{2}".toRegex(), "") // Удаляем суммы
            .trim()
            
        return desc
    }

    /**
     * Проверяет, нужно ли пропустить строку при формировании описания транзакции
     */
    private fun shouldSkipLineInDescription(line: String): Boolean {
        val lowercaseLine = line.lowercase()
        return lowercaseLine.contains("итого:") ||
               lowercaseLine.contains("баланс:") ||
               lowercaseLine.contains("остаток:") ||
               lowercaseLine.contains("всего:") ||
               lowercaseLine.contains("лимит:") ||
               lowercaseLine.contains("доступно:") ||
               lowercaseLine.contains("оборот") ||
               lowercaseLine.startsWith("дата") ||
               lowercaseLine == "сумма операции"
    }

    /**
     * Определяет категорию транзакции на основе её описания
     */
    private fun inferCategoryFromDescription(description: String, isExpense: Boolean): String {
        val lowercaseDesc = description.lowercase()

        // Если содержит "система быстрых платежей" или "пополнение", то это должен быть доход
        if (lowercaseDesc.contains("система быстрых платежей") || 
            (lowercaseDesc.contains("пополнение") && !isExpense)) {
            return "Переводы"
        }

        return when {
            // Проверяем на типичные места
            lowercaseDesc.contains("кафе") || 
            lowercaseDesc.contains("ресторан") ||
            lowercaseDesc.contains("кофейня") -> "Рестораны"
            
            lowercaseDesc.contains("продукт") || 
            lowercaseDesc.contains("супермаркет") || 
            lowercaseDesc.contains("магазин") ||
            lowercaseDesc.contains("магнит") ||
            lowercaseDesc.contains("пятерочка") ||
            lowercaseDesc.contains("ашан") ||
            lowercaseDesc.contains("лента") -> "Продукты"
            
            lowercaseDesc.contains("алкоголь") ||
            lowercaseDesc.contains("вино") ||
            lowercaseDesc.contains("винлаб") -> "Алкоголь"
            
            // Банковские и платежные операции
            lowercaseDesc.contains("комиссия") -> "Комиссии"
            lowercaseDesc.contains("перевод") && (lowercaseDesc.contains("отправитель") || !isExpense) -> "Переводы"
            lowercaseDesc.contains("перевод") && !lowercaseDesc.contains("отправитель") -> "Переводы"
            lowercaseDesc.contains("перевод") && lowercaseDesc.contains("сбп") -> "Переводы"
            
            // Покупки в магазинах
            lowercaseDesc.contains("оплата товаров") ||
            lowercaseDesc.contains("оплата услуг") ||
            lowercaseDesc.contains("покупка") -> "Покупки"
            
            // Другие типичные категории
            lowercaseDesc.contains("зарплата") || lowercaseDesc.contains("аванс") -> "Зарплата"
            lowercaseDesc.contains("такси") || lowercaseDesc.contains("метро") ||
            lowercaseDesc.contains("транспорт") || lowercaseDesc.contains("автобус") -> "Транспорт"
            lowercaseDesc.contains("аптека") || lowercaseDesc.contains("клиника") ||
            lowercaseDesc.contains("медицин") -> "Здоровье"
            lowercaseDesc.contains("жкх") || lowercaseDesc.contains("коммунал") -> "Коммунальные платежи"
            lowercaseDesc.contains("мтс") || lowercaseDesc.contains("связь") ||
            lowercaseDesc.contains("билайн") || lowercaseDesc.contains("мегафон") -> "Связь"
            lowercaseDesc.contains("одежда") || lowercaseDesc.contains("обувь") -> "Одежда"
            lowercaseDesc.contains("кэшбэк") || lowercaseDesc.contains("cashback") -> "Кэшбэк"
            lowercaseDesc.contains("пополнение") || lowercaseDesc.contains("зачисление") -> "Пополнение"
            
            // Если не смогли определить категорию
            else -> if (isExpense) "Другое" else "Доход"
        }
    }
} 