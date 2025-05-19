package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.sberbank

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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Класс для импорта транзакций из PDF-выписок Сбербанка
 */
class SberbankPdfImportUseCase(
    context: Context,
    transactionRepository: TransactionRepository
) : BankImportUseCase(transactionRepository, context) {

    override val bankName: String = "Сбербанк (PDF)"

    // Константа для источника транзакций
    private val transactionSource: String = "Сбер"

    /**
     * Модель для обработки многострочных транзакций
     */
    private data class PartialTransaction(
        var date: String? = null,
        var time: String? = null,
        var authCode: String? = null,
        var category: String? = null,
        var description: MutableList<String> = mutableListOf(),
        var amount: String? = null,
        var balance: String? = null,
        var isComplete: Boolean = false
    ) {

        fun clear() {
            date = null
            time = null
            authCode = null
            category = null
            description.clear()
            amount = null
            balance = null
            isComplete = false
        }

        fun isValid(): Boolean = date != null && amount != null
    }

    // Шаблоны для анализа строк
    private val mainLineRegex =
        Regex("^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+(\\d{2}:\\d{2})\\s+(\\d+)\\s+(.+?)\\s+([+-]?[\\d\\s.,]+[\\d])\\s+([\\d\\s.,]+[\\d])$")
    private val dateTimeRegex = Regex("^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+(\\d{2}:\\d{2}).*$")
    private val descriptionLineRegex = Regex("^\\d{2}\\.\\d{2}\\.\\d{4}\\s+(.+)$")
    private val cardNumberRegex = Regex("^(?:карте|карты)\\s+\\*{4}(\\d{4})$", RegexOption.IGNORE_CASE)

    private suspend fun extractTextFromPdf(uri: Uri): String = withContext(Dispatchers.IO) {
        var text = ""
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                inputStream.use { stream ->
                    PDDocument.load(stream).use { document ->
                        val stripper = PDFTextStripper()
                        text = stripper.getText(document)
                    }
                }
            } else {
                Timber.w("Failed to open InputStream for PDF: $uri")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting text from PDF for Sberbank")
        }
        text
    }

    override fun isValidFormat(reader: BufferedReader): Boolean {
        try {
            val headerLines = mutableListOf<String>()
            reader.mark(8192)
            for (i in 0 until 25) {
                val line = reader.readLine()?.replace("\u0000", "")
                if (line != null) headerLines.add(line) else break
            }
            reader.reset()

            val textSample = headerLines.joinToString(separator = "\n")

            val hasBankIndicator = textSample.contains("СБЕР", ignoreCase = true) ||
                    textSample.contains("Сбербанк", ignoreCase = true)
            val hasStatementTitle = textSample.contains("Выписка по счёту", ignoreCase = true) ||
                    textSample.contains("Выписка по счету", ignoreCase = true)
            val hasTableMarker = headerLines.any { it.contains("Расшифровка операций", ignoreCase = true) } ||
                    headerLines.any {
                        it.contains("ДАТА ОПЕРАЦИИ", ignoreCase = true) &&
                                it.contains("КАТЕГОРИЯ", ignoreCase = true)
                    }

            Timber.d(
                "Sberbank PDF format validation: hasBankIndicator=%s, hasStatementTitle=%s, hasTableMarker=%s",
                hasBankIndicator, hasStatementTitle, hasTableMarker
            )

            return hasBankIndicator && hasStatementTitle && hasTableMarker
        } catch (e: Exception) {
            Timber.e(e, "Error in Sberbank PDF isValidFormat")
            return false
        }
    }

    override fun skipHeaders(reader: BufferedReader) {
        var line: String?
        var расшифровкаНайдена = false

        Timber.d("Sberbank skipHeaders: Ищем маркер начала таблицы 'Расшифровка операций'")
        // Сначала ищем "Расшифровка операций"
        while (true) {
            line = reader.readLine()?.replace("\u0000", "")
            if (line == null) {
                Timber.w("Sberbank skipHeaders: Достигнут конец файла перед нахождением маркера 'Расшифровка операций'.")
                return
            }
            if (line.contains("Расшифровка операций", ignoreCase = true)) {
                Timber.d("Sberbank skipHeaders: Найден маркер 'Расшифровка операций': %s", line)
                расшифровкаНайдена = true
                break
            }
            Timber.v("Sberbank skipHeaders: (Поиск маркера) Пропущена строка: %s", line)
        }

        if (!расшифровкаНайдена) {
            Timber.w("Sberbank skipHeaders: Маркер 'Расшифровка операций' так и не был найден.")
            return
        }

        Timber.d("Sberbank skipHeaders: Ищем первую строку с датой операции (начало данных)")
        // Теперь пропускаем все строки заголовков таблицы ПОСЛЕ "Расшифровка операций"
        while (true) {
            reader.mark(1024)
            val nextLine = reader.readLine()?.replace("\u0000", "")
            reader.reset()

            if (nextLine == null) {
                Timber.w("Sberbank skipHeaders: Достигнут конец файла при пропуске заголовков таблицы.")
                break
            }

            if (nextLine.trim().matches(Regex("^\\d{2}\\.\\d{2}\\.\\d{4}.*"))) {
                Timber.d(
                    "Sberbank skipHeaders: Найдена строка, похожая на начало данных: '%s'. Предыдущая строка была последней в заголовке.",
                    nextLine.trim()
                )
                break // Заголовки пропущены
            } else {
                val skippedHeaderLine = reader.readLine()?.replace("\u0000", "")
                Timber.d("Sberbank skipHeaders: (Пропуск заголовков) Пропущена строка: %s", skippedHeaderLine)
            }
        }
    }

    override fun shouldSkipLine(line: String): Boolean {
        if (line.isBlank()) {
            Timber.v("Sberbank shouldSkipLine: ПРОПУСК (пустая): '%s'", line)
            return true
        }

        // Заголовки столбцов таблицы
        val tableHeaders = setOf(
            "ДАТА ОПЕРАЦИИ (МСК)",
            "Дата обработки¹ и код авторизации",
            "КАТЕГОРИЯ",
            "Описание операции",
            "СУММА В ВАЛЮТЕ СЧЁТА",
            "Сумма в валюте",
            "операции²",
            "ОСТАТОК СРЕДСТВ",
            "В ВАЛЮТЕ СЧЁТА"
        )
        if (tableHeaders.any { line.equals(it, ignoreCase = true) }) {
            Timber.v("Sberbank shouldSkipLine: ПРОПУСК (заголовок таблицы): '%s'", line)
            return true
        }

        // Строки, связанные с нумерацией страниц, переносами и подвалом документа
        val footerOrPageLinesPatterns = listOf(
            Regex("^Выписка по счёту дебетовой карты Страница \\d+ из \\d+$", RegexOption.IGNORE_CASE),
            Regex("^Продолжение на следующей странице$", RegexOption.IGNORE_CASE),
            Regex("^Дата формирования \\d{2}\\.\\d{2}\\.\\d{4}$", RegexOption.IGNORE_CASE),
            Regex("^ПАО Сбербанк\\. Генеральная лицензия", RegexOption.IGNORE_CASE),
            Regex("^Денежные средства списываются", RegexOption.IGNORE_CASE),
            Regex("^отображаются только обработанные", RegexOption.IGNORE_CASE),
            Regex("^до 30 дней\\.$", RegexOption.IGNORE_CASE),
            Regex("^\\d$"), // Single digits (footnotes)
            Regex("^Дата списания / зачисления денежных средств на счёт карты$", RegexOption.IGNORE_CASE),
            Regex("^По курсу банка на дату обработки операции$", RegexOption.IGNORE_CASE),
            Regex("^Дергунова К\\. А\\.$", RegexOption.IGNORE_CASE),
            Regex("^Управляющий директор Дивизиона «Забота о клиентах»$", RegexOption.IGNORE_CASE)
        )

        if (footerOrPageLinesPatterns.any { line.matches(it) }) {
            Timber.v("Sberbank shouldSkipLine: ПРОПУСК (подвал/страница/сноска): '%s'", line)
            return true
        }

        Timber.v("Sberbank shouldSkipLine: НЕ ПРОПУСК: '%s'", line)
        return false
    }

    override fun processTransactionsFromReader(
        reader: BufferedReader,
        progressCallback: ImportProgressCallback
    ): Flow<ImportResult> = flow {
        var line: String?
        var importedCount = 0
        var skippedCount = 0
        val importedTransactions = mutableListOf<Transaction>()

        // Очищаем PDF от заголовков и подвалов, находим основные строки с транзакциями

        try {
            // Проверяем формат
            emit(ImportResult.progress(5, 100, "Проверка формата файла..."))

            if (!isValidFormat(reader)) {
                Timber.w("Файл не соответствует формату Сбербанка")
                emit(ImportResult.error("Файл не соответствует формату выписки Сбербанка."))
                return@flow
            }

            emit(ImportResult.progress(10, 100, "Начинаем обработку строк..."))
            skipHeaders(reader)

            // Создаем объект для хранения частичных данных о транзакции
            // В Сбербанке одна транзакция может занимать несколько строк
            var currentTransaction = PartialTransaction()

            line = reader.readLine()
            var lineNumber = 0
            // Обработка строк
            while (line != null) {
                lineNumber++

                // Обновляем прогресс
                if (lineNumber % 10 == 0) {
                    val progress = 10 + (lineNumber * 70 / 100).coerceAtMost(70)
                    emit(ImportResult.progress(progress, 100, "Обработка строки $lineNumber из ~100..."))
                    progressCallback.onProgress(progress, 100, "Обработка строки $lineNumber из ~100...")
                }

                if (shouldSkipLine(line)) {
                    Timber.v("Пропускаем строку %d: '%s'", lineNumber, line)
                } else {
                    // Проверяем, является ли строка началом новой транзакции
                    val mainMatch = mainLineRegex.find(line.trim())
                    if (mainMatch != null) {
                        // Сохраняем предыдущую транзакцию, если она была начата
                        if (currentTransaction.isValid()) {
                            val transaction = createTransactionFromPartial(currentTransaction)
                            if (transaction != null) {
                                importedTransactions.add(transaction)
                                Timber.d(
                                    "Parsed Sberbank transaction: Date=%s, Title=%s, Amount=%s, IsExpense=%s",
                                    transaction.date, transaction.title, transaction.amount.amount, transaction.isExpense
                                )
                                importedCount++
                            } else {
                                skippedCount++
                            }
                        }

                        // Начинаем новую транзакцию
                        val dateStr = mainMatch.groupValues[1] // ДД.ММ.ГГГГ
                        val timeStr = mainMatch.groupValues[2] // ЧЧ:ММ
                        val authCode = mainMatch.groupValues[3] // Код авторизации
                        val category = mainMatch.groupValues[4].trim() // Категория/описание
                        val amountStr = mainMatch.groupValues[5].trim() // Сумма
                        val balanceStr = mainMatch.groupValues[6].trim() // Остаток

                        currentTransaction = PartialTransaction(
                            date = dateStr,
                            time = timeStr,
                            authCode = authCode,
                            category = category,
                            amount = amountStr,
                            balance = balanceStr
                        )
                    } else if (currentTransaction.date != null) {
                        // Если строка не содержит новую транзакцию, но у нас уже есть активная,
                        // добавляем текущую строку к описанию транзакции

                        // Проверяем, содержит ли строка дополнительную информацию
                        val descMatch = descriptionLineRegex.find(line)
                        if (descMatch != null) {
                            currentTransaction.description.add(descMatch.groupValues[1])
                        } else {
                            // Проверяем, содержит ли строка номер карты
                            val cardMatch = cardNumberRegex.find(line)
                            if (cardMatch != null) {
                                currentTransaction.description.add(line)
                            } else if (!dateTimeRegex.matches(line)) {
                                // Если строка не содержит дату, но содержит текст - 
                                // считаем её продолжением описания
                                currentTransaction.description.add(line)
                            }
                        }
                    }
                }

                line = reader.readLine()
            }

            // Обрабатываем последнюю транзакцию, если она есть
            if (currentTransaction.isValid()) {
                val transaction = createTransactionFromPartial(currentTransaction)
                if (transaction != null) {
                    importedTransactions.add(transaction)
                    importedCount++
                } else {
                    skippedCount++
                }
            }

            // Завершаем импорт
            Timber.i(
                "Импорт для банка %s завершен. Импортировано: %d, Пропущено: %d",
                bankName, importedCount, skippedCount
            )

            emit(ImportResult.progress(95, 100, "Сохранение транзакций..."))
            progressCallback.onProgress(95, 100, "Сохранение транзакций...")

            if (importedTransactions.isNotEmpty()) {
                try {
                    Timber.i("[ИМПОРТ-SB] Начало сохранения ${importedTransactions.size} транзакций из PDF Сбербанка")
                    var savedCount = 0

                    importedTransactions.forEach { transaction ->
                        try {
                            Timber.d("[ИМПОРТ-SB] Сохранение транзакции ID=${transaction.id}, сумма=${transaction.amount}, дата=${transaction.date}")
                            Timber.i("[ИМПОРТ-SB] ⚠️ ПЕРЕД вызовом transactionRepository.addTransaction для ID=${transaction.id}")
                            val result = transactionRepository.addTransaction(transaction)
                            Timber.i("[ИМПОРТ-SB] ✅ ПОСЛЕ вызова transactionRepository.addTransaction для ID=${transaction.id}, результат=$result")
                            savedCount++
                        } catch (e: Exception) {
                            Timber.e(e, "[ИМПОРТ-SB] ❌ Ошибка при сохранении транзакции ID=${transaction.id}: ${e.message}")
                        }
                    }

                    Timber.i("[ИМПОРТ-SB] Сохранено $savedCount из ${importedTransactions.size} транзакций")

                    // Проверяем количество транзакций в базе данных
                    try {
                        Timber.d("[ИМПОРТ-SB-ПРОВЕРКА] Проверка общего количества транзакций в базе...")
                        val allTransactions = transactionRepository.getAllTransactions()
                        Timber.i("[ИМПОРТ-SB-ПРОВЕРКА] Общее количество транзакций в базе после импорта: ${allTransactions.size}")
                    } catch (e: Exception) {
                        Timber.e(e, "[ИМПОРТ-SB-ПРОВЕРКА] Ошибка при проверке количества транзакций: ${e.message}")
                    }

                    emit(ImportResult.success(savedCount, skippedCount))
                } catch (e: Exception) {
                    Timber.e(e, "[ИМПОРТ-SB] ❌ Ошибка при сохранении транзакций: ${e.message}")
                    emit(ImportResult.error("Ошибка при сохранении транзакций: ${e.message}"))
                }
            } else {
                Timber.w("[ИМПОРТ-SB] Нет транзакций для сохранения")
                emit(ImportResult.success(0, skippedCount))
            }
        } catch (e: Exception) {
            Timber.e(e, "[ИМПОРТ-SB] ❌ Общая ошибка при импорте: ${e.message}")
            emit(ImportResult.error("Ошибка при импорте: ${e.message}"))
        }
    }

    /**
     * Создает объект Transaction из частичных данных
     */
    private fun createTransactionFromPartial(partial: PartialTransaction): Transaction? {
        try {
            if (!partial.isValid()) {
                return null
            }

            val transactionDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .parse(partial.date!!) ?: Date()

            // Обработка суммы
            val cleanedAmount = partial.amount!!.replace("\\s".toRegex(), "").replace(',', '.')
            val amount = cleanedAmount.toDoubleOrNull() ?: run {
                Timber.e("Failed to parse amount: %s", partial.amount)
                return null
            }

            // Определение дохода/расхода
            val isExpense: Boolean = when {
                cleanedAmount.startsWith("-") -> true
                cleanedAmount.startsWith("+") -> false
                // Если нет знака, пытаемся угадать по категории (упрощенно)
                else -> !(partial.category?.contains("внесение наличных", ignoreCase = true) == true ||
                        partial.category?.contains("перевод", ignoreCase = true) == true)
            }

            val absAmount = kotlin.math.abs(amount)
            val money = Money(absAmount, Currency.RUB)

            val noteParts = mutableListOf<String>()
            if (partial.time != null) noteParts.add("Время: ${partial.time}")
            if (partial.authCode != null) noteParts.add("Код: ${partial.authCode}")
            if (partial.balance != null) noteParts.add("Баланс: ${partial.balance}")
            if (partial.description.isNotEmpty()) {
                noteParts.add("Детали: ${partial.description.joinToString(" ")}")
            }

            val title = partial.category ?: "Неизвестная операция"

            Timber.d(
                "Parsed Sberbank transaction: Date=%s, Title=%s, Amount=%s, IsExpense=%s",
                transactionDate, title, money.amount, isExpense
            )

            return Transaction(
                amount = money,
                category = title,
                date = transactionDate,
                isExpense = isExpense,
                note = noteParts.joinToString("; ").ifBlank { null },
                source = transactionSource,
                sourceColor = 0,
                categoryId = "",
                title = title
            )
        } catch (e: Exception) {
            Timber.e(e, "Error creating transaction from partial data")
            return null
        }
    }

    override fun importTransactions(uri: Uri, progressCallback: ImportProgressCallback): Flow<ImportResult> = flow {
        emit(ImportResult.Progress(0, 100, "Начало импорта PDF Сбербанка..."))
        progressCallback.onProgress(0, 100, "Извлечение текста из PDF...")

        val textContent = extractTextFromPdf(uri)
        if (textContent.isBlank()) {
            Timber.w("Не удалось извлечь текст из PDF файла Сбербанка: $uri")
            emit(ImportResult.Error(message = "Не удалось извлечь текст из PDF файла Сбербанка."))
            progressCallback.onProgress(100, 100, "Ошибка: не удалось извлечь текст.")
            return@flow
        }
        emit(ImportResult.Progress(10, 100, "Текст из PDF извлечен. Начало обработки..."))
        progressCallback.onProgress(10, 100, "Текст из PDF извлечен. Начало обработки...")

        try {
            StringReader(textContent).use { stringReader ->
                BufferedReader(stringReader).use { reader ->
                    emitAll(processTransactionsFromReader(reader, progressCallback))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка во время импорта транзакций Сбербанка PDF после извлечения текста")
            emit(ImportResult.Error(exception = e, message = "Ошибка обработки данных PDF Сбербанка: ${e.message ?: "Неизвестная ошибка"}"))
            progressCallback.onProgress(100, 100, "Ошибка обработки данных PDF.")
        }
    }.catch { e ->
        Timber.e(e, "Ошибка в importTransactions для Sberbank PDF")
        val exceptionToSend = if (e is Exception) e else Exception("Неизвестная ошибка Throwable: ${e.message}", e)
        emit(ImportResult.Error(exceptionToSend, "Критическая ошибка при импорте PDF: ${exceptionToSend.message ?: "Неизвестная ошибка"}"))
        progressCallback.onProgress(100, 100, "Ошибка импорта: ${exceptionToSend.message ?: "Неизвестная ошибка"}")
    }

    /**
     * Реализация метода parseLine, требуемая базовым классом.
     * В нашем случае мы используем многострочный парсер через
     * processTransactionsFromReader, но базовый класс требует этот метод.
     */
    override fun parseLine(line: String): Transaction? {
        Timber.d("Parsing Sberbank line: %s", line)

        val mainMatch = mainLineRegex.find(line.trim())
        if (mainMatch == null) {
            Timber.w("Line does not match Sberbank transaction pattern: %s", line)
            return null
        }

        try {
            val dateStr = mainMatch.groupValues[1] // ДД.ММ.ГГГГ
            val timeStr = mainMatch.groupValues[2] // ЧЧ:ММ
            val authCode = mainMatch.groupValues[3] // Код авторизации
            val category = mainMatch.groupValues[4].trim() // Категория/описание
            val amountStr = mainMatch.groupValues[5].trim() // Сумма
            val balanceStr = mainMatch.groupValues[6].trim() // Остаток

            val partialTransaction = PartialTransaction(
                date = dateStr,
                time = timeStr,
                authCode = authCode,
                category = category,
                amount = amountStr,
                balance = balanceStr
            )

            return createTransactionFromPartial(partialTransaction)
        } catch (e: Exception) {
            Timber.e(e, "Error parsing Sberbank line: %s", line)
            return null
        }
    }
} 