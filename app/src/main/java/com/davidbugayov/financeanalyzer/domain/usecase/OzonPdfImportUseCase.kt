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
import java.util.regex.Pattern

/**
 * Реализация импорта транзакций из PDF-выписки Озон Банка.
 * Поддерживает формат PDF-выписки из личного кабинета Озон Банка.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 * @param categoryPreferences Предпочтения категорий для создания новых категорий
 * @param sourcePreferences Предпочтения источников для создания новых источников
 */
class OzonPdfImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context,
    private val categoryPreferences: CategoryPreferences,
    private val sourcePreferences: SourcePreferences
) : BankImportUseCase(repository, context) {

    companion object {
        private const val PDF_PARSING_TIMEOUT = 60000L // 60 секунд таймаут
        
        // Регулярные выражения для парсинга данных
        private val DATE_PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})(\\s+\\d{2}:\\d{2}:\\d{2})?")
        private val OPERATION_NUMBER_PATTERN = Pattern.compile("(\\d{7,})")
    }

    override val bankName: String = "Озон Банк"

    // Форматы даты, используемые в выписках Озон Банка
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
     * Основной метод, который выполняет импорт транзакций из PDF-файла Озон Банка
     */
    override suspend fun invoke(uri: Uri): Flow<ImportResult> = flow {
        try {
            emit(ImportResult.Progress(1, 100, "Открытие PDF-файла выписки Озон Банка"))
            
            // Инициализируем PDFBox для Android, если это ещё не сделано
            if (!PDFBoxResourceLoader.isReady()) {
                PDFBoxResourceLoader.init(context)
            }
            
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
            
            Timber.d("ОЗОН PDF: Прочитано ${pdfLines.size} строк из PDF")
            
            // Проверяем наличие заголовка таблицы, который подтверждает формат Озон Банка
            val hasOzonHeader = pdfLines.any { 
                it.contains("Дата операции", ignoreCase = true) && 
                (it.contains("Документ", ignoreCase = true) || it.contains("Назначение", ignoreCase = true))
            }
            
            if (!hasOzonHeader) {
                Timber.w("ОЗОН PDF: Не найден заголовок таблицы Озон Банка")
                // Продолжаем обработку, но выводим предупреждение
            }
            
            // Добавляем источник "Озон Банк", если его еще нет в предпочтениях
            addSourceIfNotExists()
            
            // Парсим данные из PDF и преобразуем их в транзакции
            emit(ImportResult.Progress(20, 100, "Анализ выписки"))
            
            // Используем таймаут для парсинга транзакций
            val transactions = withTimeoutOrNull(PDF_PARSING_TIMEOUT) {
                parsePdfTransactions(pdfLines)
            } ?: run {
                emit(ImportResult.Error("Превышено время ожидания при обработке данных PDF-файла"))
                return@flow
            }
            
            if (transactions.isEmpty()) {
                emit(ImportResult.Error("Не найдено транзакций в файле"))
                return@flow
            }
            
            Timber.d("ОЗОН PDF: Найдено ${transactions.size} транзакций")
            
            // Сохраняем транзакции в базу данных
            emit(ImportResult.Progress(70, 100, "Сохранение транзакций"))
            
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
            Timber.e(e, "Ошибка импорта из PDF Озон Банка: ${e.message}")
            emit(ImportResult.Error("Ошибка импорта из PDF Озон Банка: ${e.message}", e))
        }
    }
    
    /**
     * Читает содержимое PDF файла и возвращает список строк
     */
    private suspend fun readPdfContent(uri: Uri): List<String> {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Не удалось открыть файл")
        
        try {
            Timber.d("Начало чтения PDF-файла Озон Банка")
            val startTime = System.currentTimeMillis()
            
            val document = withContext(Dispatchers.IO) {
                PDDocument.load(inputStream)
            }
            
            val stripper = PDFTextStripper()
            val pdfText = withContext(Dispatchers.IO) {
                stripper.getText(document)
            }
            
            val endTime = System.currentTimeMillis()
            Timber.d("Чтение PDF-файла Озон Банка завершено за ${endTime - startTime} мс")
            
            document.close()
            inputStream.close()
            
            return pdfText.split("\n").filter { it.isNotBlank() }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при чтении PDF Озон Банка: ${e.message}")
            inputStream.close()
            throw e
        }
    }

    /**
     * Парсит транзакции из текста PDF-файла Озон Банка
     */
    private fun parsePdfTransactions(lines: List<String>): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        // Выводим первые 30 строк для отладки
        lines.take(30).forEachIndexed { index, line ->
            Timber.d("ОЗОН PDF DEBUG: Строка $index: '$line'")
        }
        
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            
            // Проверяем, есть ли дата в начале строки
            val dateMatcher = DATE_PATTERN.matcher(line)
            if (dateMatcher.find()) {
                try {
                    // Нашли строку с датой - начало транзакции
                    val dateString = dateMatcher.group(0) ?: continue
                    Timber.d("ОЗОН PDF: Найдена строка с датой: $dateString")
                    
                    // Получаем дату операции
                    val date = parseDate(dateString)

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
                           nextLineIndex - i < 15) { // не более 15 строк от начала транзакции
                           
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
                    
                    // Ищем строку "Сумма операции" и берем следующую строку как сумму
                    for (j in 0 until transactionLines.size - 1) {
                        if (transactionLines[j].contains("Сумма операции", ignoreCase = true)) {
                            val amountLine = transactionLines[j + 1].trim()
                            Timber.d("ОЗОН PDF: Найдена строка суммы: '$amountLine'")
                            
                            // Определяем тип операции по знаку или содержимому
                            isExpense = amountLine.startsWith("-") || 
                                      (!amountLine.contains("+") && fullDescriptionBuilder.contains("оплата", ignoreCase = true))
                            
                            // Извлекаем сумму, очищая от знаков и других символов
                            val cleanAmountStr = amountLine
                                .replace("[^0-9.,]".toRegex(), "") // Оставляем только цифры и разделители
                                .trim()
                            
                            if (cleanAmountStr.matches("\\d+[.,]\\d{2}".toRegex())) {
                                amount = cleanAmountStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                                
                                if (amount > 0) {
                                    amountFound = true
                                    Timber.d("ОЗОН PDF: Выделена сумма: ${if (isExpense) "-" else "+"} $amount руб.")
                                }
                            }
                            
                            break
                        }
                    }
                    
                    // Если мы не нашли строку "Сумма операции", ищем сумму в явном формате
                    if (!amountFound) {
                        for (transactionLine in transactionLines) {
                            // Ищем строку, которая содержит только сумму
                            if (transactionLine.trim().matches("^[+-]?\\s*\\d+[\\s\\d]*[.,]\\d{2}\\s*$".toRegex())) {
                                val amountLine = transactionLine.trim()
                                Timber.d("ОЗОН PDF: Найдена отдельная строка суммы: '$amountLine'")
                                
                                // Определяем тип операции
                                isExpense = amountLine.startsWith("-") || 
                                          (!amountLine.contains("+") && fullDescriptionBuilder.contains("оплата", ignoreCase = true))
                                
                                // Извлекаем сумму
                                val cleanAmountStr = amountLine
                                    .replace("[^0-9.,]".toRegex(), "") // Оставляем только цифры и разделители
                                    .trim()
                                
                                if (cleanAmountStr.matches("\\d+[.,]\\d{2}".toRegex())) {
                                    amount = cleanAmountStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    
                                    if (amount > 0) {
                                        amountFound = true
                                        Timber.d("ОЗОН PDF: Выделена сумма из отдельной строки: ${if (isExpense) "-" else "+"} $amount руб.")
                                    }
                                }
                                
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

                    if (amountFound) {
                        // Дополнительная проверка типа транзакции
                        isExpense = if (descriptionText.contains("перевод", ignoreCase = true) && 
                                       descriptionText.contains("отправитель", ignoreCase = true)) {
                            false // входящий перевод
                        } else if (descriptionText.contains("оплата", ignoreCase = true)) {
                            true // исходящий платеж
                        } else {
                            isExpense // оставляем как определили ранее
                        }
                        
                        // Определяем категорию транзакции
                        val category = inferCategoryFromDescription(descriptionText, isExpense)
                        
                        // Проверяем, является ли транзакция переводом
                        val isTransfer = category == "Переводы" || descriptionText.contains("перев", ignoreCase = true)
                        
                        // Создаем транзакцию
                        val transaction = Transaction(
                            id = "ozon_pdf_${date.time}_${System.nanoTime()}",
                            amount = Money(amount),
                            category = category,
                            date = date,
                            isExpense = isExpense,
                            note = descriptionText.takeIf { it.isNotBlank() },
                            source = bankName,
                            sourceColor = if (isTransfer) TransferColorInt
                            else ColorUtils.getSourceColorByName(bankName)?.toArgb()
                                ?: if (isExpense) ExpenseColorInt else IncomeColorInt,
                            isTransfer = isTransfer
                        )
                        
                        transactions.add(transaction)
                        Timber.d("ОЗОН PDF: Добавлена транзакция: ${if (isExpense) "-" else "+"} $amount руб., $descriptionText")
                    } else {
                        Timber.w("ОЗОН PDF: Не удалось найти сумму для транзакции с датой $dateString, описание: $descriptionText")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "ОЗОН PDF: Ошибка при обработке строки с транзакцией: ${e.message}")
                }
            }
            i++
        }
        
        Timber.d("ОЗОН PDF: Всего найдено ${transactions.size} транзакций")
        return transactions
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
            Timber.w(e, "ОЗОН PDF: Ошибка при парсинге даты: $dateString")
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
        // Словарь категорий для Озон Банка
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
                    // Найдена категория, добавляем её в настройки, если нужно
                    addCategoryIfNotExists(category, isExpense)
                    return category
                }
            }
        }
        
        // Если категория не определена, используем "Другое"
        val defaultCategory = if (isExpense) "Другое" else "Доход"
        addCategoryIfNotExists(defaultCategory, isExpense)
        return defaultCategory
    }
    
    /**
     * Добавляет категорию в настройки, если её там еще нет
     * 
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
    }
    
    /**
     * Добавляет источник в настройки, если его там еще нет
     * */
    private fun addSourceIfNotExists() {
        val currentSources = sourcePreferences.getCustomSources()
        if (currentSources.none { it.name.equals(bankName, ignoreCase = true) }) {
            val newSource = Source(
                name = bankName,
                // Используем DefaultSourceColorInt, т.к. у Озона обычно свой цвет
                color = ColorUtils.getSourceColorByName(bankName)?.toArgb() ?: DefaultSourceColorInt 
            )
            val updatedSources = (currentSources + newSource).distinctBy { it.name }
            sourcePreferences.saveCustomSources(updatedSources)
        }
    }
} 