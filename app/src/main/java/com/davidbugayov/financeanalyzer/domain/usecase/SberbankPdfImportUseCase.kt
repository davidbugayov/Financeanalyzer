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
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.absoluteValue

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

    // Паттерны для парсинга PDF-выписки
    private val datePattern = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    
    // Паттерны для обработки выписок Сбербанка
    private val dateRegex = "\\d{2}\\.\\d{2}\\.\\d{4}".toRegex()
    private val amountRegex = "([+-]?\\d+[\\s]?\\d+[,.]\\d{2})".toRegex()
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
            
            // Извлекаем текст из PDF
            val document = withContext(Dispatchers.IO) {
                PDDocument.load(inputStream)
            }
            
            val stripper = PDFTextStripper()
            val pdfText = withContext(Dispatchers.IO) {
                stripper.getText(document)
            }
            
            document.close()
            inputStream.close()
            
            // Проверяем, что это действительно выписка Сбербанка
            if (!isValidSberbankStatement(pdfText)) {
                emit(ImportResult.Error("Формат PDF-файла не соответствует выписке Сбербанка"))
                return@flow
            }
            
            emit(ImportResult.Progress(10, 100, "Анализ выписки Сбербанка"))
            
            // Извлекаем номер карты (если есть)
            val cardNumberMatcher = cardNumberPattern.matcher(pdfText)
            val cardNumber = if (cardNumberMatcher.find()) {
                "Карта ${cardNumberMatcher.group(1)}"
            } else {
                val cardMatch = "••••\\s*\\d+".toRegex().find(pdfText)
                if (cardMatch != null) {
                    "Карта ${cardMatch.value}"
                } else {
                    "Сбербанк"
                }
            }
            
            // Разбиваем текст на строки для анализа
            val lines = pdfText.split("\n")
            
            // Находим раздел с транзакциями
            var transactionSectionStarted = false
            val transactions = mutableListOf<Transaction>()
            
            // Создаем паттерн для операций
            val operationBlock = StringBuilder()
            var lineIndex = 0
            
            emit(ImportResult.Progress(15, 100, "Поиск транзакций в выписке"))
            
            // Первый проход - определяем все блоки транзакций
            while (lineIndex < lines.size) {
                if (lineIndex % 20 == 0) {
                    val progress = 15 + (lineIndex.toFloat() / lines.size * 30).toInt()
                    emit(ImportResult.Progress(progress, 100, "Анализ структуры выписки"))
                }
                
                val line = lines[lineIndex]
                
                // Ищем начало списка транзакций
                if (!transactionSectionStarted && 
                    (line.contains("Расшифровка операций", ignoreCase = true) || 
                     line.contains("ДАТА ОПЕРАЦИИ", ignoreCase = true))) {
                    transactionSectionStarted = true
                    lineIndex++
                    continue
                }
                
                // Обрабатываем транзакции
                if (transactionSectionStarted) {
                    // Ищем дату в формате ДД.ММ.ГГГГ, которая может быть началом блока транзакции
                    val dateMatch = dateRegex.find(line)
                    
                    if (dateMatch != null) {
                        // Если нашли новую дату и у нас уже есть операция в буфере,
                        // обрабатываем предыдущую операцию
                        if (operationBlock.isNotEmpty()) {
                            val transaction = parseOperationBlock(operationBlock.toString(), cardNumber)
                            if (transaction != null) {
                                transactions.add(transaction)
                            }
                            operationBlock.clear()
                        }
                        
                        // Начинаем новый блок операции
                        operationBlock.append(line).append("\n")
                    } else if (operationBlock.isNotEmpty()) {
                        // Продолжаем собирать текущий блок операции
                        operationBlock.append(line).append("\n")
                    }
                }
                
                lineIndex++
            }
            
            // Обрабатываем последний блок, если он есть
            if (operationBlock.isNotEmpty()) {
                val transaction = parseOperationBlock(operationBlock.toString(), cardNumber)
                if (transaction != null) {
                    transactions.add(transaction)
                }
            }
            
            emit(ImportResult.Progress(50, 100, "Найдено ${transactions.size} транзакций"))
            
            // Если не нашли транзакции стандартным способом, попробуем альтернативный подход
            if (transactions.isEmpty()) {
                emit(ImportResult.Progress(55, 100, "Применение альтернативного алгоритма поиска транзакций"))
                val altTransactions = parseAlternativeFormat(pdfText, cardNumber)
                transactions.addAll(altTransactions)
            }
            
            // Сохраняем найденные транзакции
            emit(ImportResult.Progress(70, 100, "Сохранение ${transactions.size} транзакций"))
            
            var importedCount = 0
            var skippedCount = 0
            var totalAmount = 0.0
            
            // Сохраняем только транзакции с ненулевой суммой
            for ((index, transaction) in transactions.withIndex()) {
                if (transaction.amount > 0) {
                    repository.addTransaction(transaction)
                    importedCount++
                    totalAmount += if (transaction.isExpense) -transaction.amount else transaction.amount
                    
                    if (index % 5 == 0) {
                        val progress = 70 + (index.toFloat() / transactions.size * 30).toInt()
                        emit(
                            ImportResult.Progress(
                                current = progress,
                                total = 100,
                                message = "Сохранение транзакций: ${index + 1} из ${transactions.size}"
                            )
                        )
                    }
                } else {
                    skippedCount++
                }
            }
            
            // Отправляем результат успешного импорта
            emit(
                ImportResult.Success(
                    importedCount = importedCount,
                    skippedCount = skippedCount,
                    totalAmount = totalAmount
                )
            )
            
        } catch (e: Exception) {
            emit(
                ImportResult.Error(
                    message = "Ошибка импорта из PDF Сбербанка: ${e.message}",
                    exception = e
                )
            )
        }
    }
    
    /**
     * Парсит блок операции и возвращает транзакцию
     */
    private fun parseOperationBlock(block: String, source: String): Transaction? {
        try {
            // Ищем дату
            val dateMatch = dateRegex.find(block) ?: return null
            val date = try {
                datePattern.parse(dateMatch.value)
            } catch (e: Exception) {
                return null
            }
            
            // Ищем сумму
            val amountMatch = amountRegex.find(block) ?: return null
            val amountStr = amountMatch.value
                .replace(" ", "")
                .replace(",", ".")
            
            val amount = try {
                amountStr.toDoubleOrNull() ?: return null
            } catch (e: Exception) {
                return null
            }
            
            // Определяем тип операции (доход/расход)
            val isExpense = !amountStr.contains("+")
            
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
     * Пытается найти транзакции альтернативным методом, если стандартный не сработал
     */
    private fun parseAlternativeFormat(pdfText: String, source: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        // Ищем все даты
        val dateMatches = dateRegex.findAll(pdfText)
        
        // Для каждой даты пытаемся найти транзакцию
        for (dateMatch in dateMatches) {
            try {
                val dateStr = dateMatch.value
                val date = datePattern.parse(dateStr) ?: continue
                
                // Находим строку, содержащую дату
                val dateLineIndex = pdfText.indexOf(dateStr)
                if (dateLineIndex == -1) continue
                
                // Вырезаем часть текста после даты для поиска суммы (до 200 символов)
                val contextAfterDate = pdfText.substring(
                    dateLineIndex, 
                    minOf(dateLineIndex + 200, pdfText.length)
                )
                
                // Ищем сумму в контексте
                val amountMatch = amountRegex.find(contextAfterDate) ?: continue
                val amountStr = amountMatch.value
                    .replace(" ", "")
                    .replace(",", ".")
                
                val amount = try {
                    amountStr.toDoubleOrNull() ?: continue
                } catch (e: Exception) {
                    continue
                }
                
                // Определяем тип операции
                val isExpense = !amountStr.contains("+")
                
                // Создаем описание
                val description = contextAfterDate
                    .replace(dateRegex, "")
                    .replace(amountRegex, "")
                    .replace("\\s+".toRegex(), " ")
                    .take(100) // Ограничиваем длину описания
                    .trim()
                
                // Определяем категорию
                val category = determineCategory(description)
                
                // Создаем транзакцию
                val transaction = Transaction(
                    id = "sber_pdf_alt_${date.time}_${System.nanoTime()}",
                    amount = amount.absoluteValue,
                    category = category,
                    isExpense = isExpense,
                    date = date,
                    note = description.takeIf { it.isNotBlank() },
                    source = source
                )
                
                transactions.add(transaction)
            } catch (e: Exception) {
                // Пропускаем проблемные транзакции
                continue
            }
        }
        
        return transactions
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
        val lowerDesc = description.lowercase(Locale.getDefault())
        
        return when {
            lowerDesc.contains("перевод") || lowerDesc.contains("пополнение") -> "Переводы"
            lowerDesc.contains("снятие") || lowerDesc.contains("банкомат") -> "Наличные"
            lowerDesc.contains("пятерочка") || lowerDesc.contains("магнит") ||
            lowerDesc.contains("ашан") || lowerDesc.contains("лента") || 
            lowerDesc.contains("супермаркет") -> "Продукты"
            lowerDesc.contains("аптека") || lowerDesc.contains("больница") ||
            lowerDesc.contains("клиника") || lowerDesc.contains("здоровье") -> "Здоровье"
            lowerDesc.contains("такси") || lowerDesc.contains("метро") ||
            lowerDesc.contains("автобус") || lowerDesc.contains("транспорт") -> "Транспорт"
            lowerDesc.contains("ресторан") || lowerDesc.contains("кафе") ||
            lowerDesc.contains("кофейня") -> "Рестораны"
            lowerDesc.contains("жкх") || lowerDesc.contains("коммунальн") ||
            lowerDesc.contains("свет") || lowerDesc.contains("газ") ||
            lowerDesc.contains("вода") -> "Коммунальные платежи"
            lowerDesc.contains("зарплата") || lowerDesc.contains("аванс") -> "Зарплата"
            lowerDesc.contains("мтс") || lowerDesc.contains("мегафон") ||
            lowerDesc.contains("билайн") || lowerDesc.contains("теле2") ||
            lowerDesc.contains("связь") -> "Связь"
            lowerDesc.contains("одежда") || lowerDesc.contains("обувь") -> "Одежда"
            else -> if (description.contains("+")) "Другие доходы" else "Другие расходы"
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
} 