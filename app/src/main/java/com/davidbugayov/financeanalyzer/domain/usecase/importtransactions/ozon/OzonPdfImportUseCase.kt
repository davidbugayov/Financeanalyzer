package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ozon

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.category.TransactionCategoryDetector
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.BankImportUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportProgressCallback
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportResult
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Класс для импорта транзакций из PDF-выписок Ozon Банка
 */
class OzonPdfImportUseCase(
    context: Context,
    transactionRepository: TransactionRepository,
) : BankImportUseCase(transactionRepository, context) {

    override val bankName: String = "Ozon Банк (PDF)"

    private val transactionSource: String = context.getString(R.string.transaction_source_ozon)

    // Состояние для парсинга многострочной транзакции
    private data class TransactionState(
        var date: Date? = null,
        var documentNumber: String? = null,
        var description: StringBuilder = StringBuilder(),
        var amount: Double? = null,
        var isExpense: Boolean = false,
        var currency: String = "RUB",
    )

    private var currentTransactionState: TransactionState? = null

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
            Timber.e(e, "Error extracting text from PDF for Ozon Bank")
        }
        text
    }

    override fun importTransactions(
        uri: Uri,
        progressCallback: ImportProgressCallback,
    ): Flow<ImportResult> = flow {
        emit(
            ImportResult.Progress(
                0,
                100,
                "Начало импорта из PDF для Ozon Банка",
            ),
        )
        Timber.d("Начало импорта из URI для Ozon Банка: $uri")

        var text = ""
        try {
            text = extractTextFromPdf(uri)
            if (text.isBlank()) {
                Timber.w("Извлеченный текст из PDF пуст для Ozon Банка.")
                emit(
                    ImportResult.Error(
                        message = "Не удалось извлечь текст из PDF файла.",
                    ),
                )
                return@flow
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при извлечении текста из PDF для Ozon Банка")
            emit(
                ImportResult.Error(
                    exception = e,
                    message = e.localizedMessage ?: "Неизвестная ошибка",
                ),
            )
            return@flow
        }

        // 1. Проверка формата с новым reader
        var validationReader: BufferedReader? = null
        try {
            validationReader = BufferedReader(StringReader(text))
            if (!isValidFormat(validationReader)) {
                Timber.w("Файл не соответствует формату выписки Ozon Банка.")
                emit(
                    ImportResult.Error(
                        message = "Файл не является выпиской Ozon Банка или его формат не поддерживается.",
                    ),
                )
                return@flow
            }
        } finally {
            validationReader?.close()
        }

        // 2. Пропуск заголовков с новым reader
        emit(
            ImportResult.Progress(
                10,
                100,
                "Пропуск заголовков...",
            ),
        )

        var processingReader: BufferedReader? = null
        var totalTransactionsFound = 0
        var totalTransactionsSaved = 0

        try {
            processingReader = BufferedReader(StringReader(text))
            skipHeaders(processingReader)

            // 3. Обработка транзакций - reader уже находится на первой строке данных
            // Создаем собственную реализацию обработки транзакций, чтобы пропустить повторную валидацию
            val startProgress = 20
            val endProgress = 100
            var linesProcessed = 0

            // Очищаем ненужные переменные для освобождения памяти
            text = ""
            System.gc()

            emit(
                ImportResult.Progress(
                    startProgress,
                    endProgress,
                    "Обработка транзакций...",
                ),
            )

            // Обрабатываем транзакции пакетами для экономии памяти
            val batchSize = 20
            val currentBatch = mutableListOf<Transaction>()

            var line: String?
            while (processingReader.readLine().also { line = it } != null) {
                linesProcessed++

                if (linesProcessed % 10 == 0) {
                    val currentProgress = startProgress + (linesProcessed.coerceAtMost(1000) * (endProgress - startProgress) / 1000)
                    emit(
                        ImportResult.Progress(
                            currentProgress,
                            endProgress,
                            "Обработано строк: $linesProcessed, найдено транзакций: $totalTransactionsFound",
                        ),
                    )
                }

                if (line == null || shouldSkipLine(line)) continue

                val transaction = parseLine(line)
                if (transaction != null) {
                    totalTransactionsFound++
                    currentBatch.add(transaction)

                    // Если пакет заполнен, сохраняем его и очищаем для следующего
                    if (currentBatch.size >= batchSize) {
                        val savedCount = saveBatchOfTransactions(currentBatch)
                        totalTransactionsSaved += savedCount

                        emit(
                            ImportResult.Progress(
                                (startProgress + endProgress) / 2,
                                endProgress,
                                "Сохранено $totalTransactionsSaved из $totalTransactionsFound транзакций",
                            ),
                        )

                        // Очищаем пакет
                        currentBatch.clear()
                        System.gc()
                    }
                }
            }

            // Сохраняем оставшиеся транзакции
            if (currentBatch.isNotEmpty()) {
                val savedCount = saveBatchOfTransactions(currentBatch)
                totalTransactionsSaved += savedCount
            }

            emit(
                ImportResult.Progress(
                    endProgress,
                    endProgress,
                    "Импорт завершен. Сохранено $totalTransactionsSaved из $totalTransactionsFound транзакций",
                ),
            )

            emit(
                ImportResult.Success(
                    totalTransactionsSaved,
                    totalTransactionsFound - totalTransactionsSaved,
                ),
            )
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при обработке транзакций: ${e.message}")
            emit(
                ImportResult.Error(
                    exception = e,
                    message = e.localizedMessage ?: "Ошибка при обработке транзакций",
                ),
            )
        } finally {
            processingReader?.close()
            System.gc()
        }
    }

    /**
     * Сохраняет пакет транзакций в базу данных.
     * @param transactions Список транзакций для сохранения
     * @return Количество успешно сохраненных транзакций
     */
    private suspend fun saveBatchOfTransactions(transactions: List<Transaction>): Int {
        var savedCount = 0

        try {
            transactions.forEach { transaction ->
                try {
                    transactionRepository.addTransaction(transaction)
                    savedCount++
                } catch (e: Exception) {
                    Timber.e(e, "Ошибка при сохранении транзакции: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при сохранении пакета транзакций: ${e.message}")
        }

        return savedCount
    }

    override fun isValidFormat(reader: BufferedReader): Boolean {
        Timber.d("Ozon isValidFormat: Начало проверки формата PDF для Ozon Банка...")
        try {
            val headerLines = mutableListOf<String>()
            reader.mark(8192) // Отметить текущую позицию для последующего сброса
            repeat(30) {
                val line = reader.readLine()?.replace("\\u0000", "") // Удаляем нулевые символы
                if (line != null) {
                    headerLines.add(line)
                } else {
                    return@repeat
                }
            }
            reader.reset() // Вернуться к отмеченной позиции

            val textSample = headerLines.joinToString(separator = "\n")
            // Timber.v("Ozon isValidFormat: Образец текста для валидации (первые %d строк):\n%s", headerLines.size, textSample) // Раскомментируйте, если нужно видеть весь блок

            val hasBankIndicator = textSample.contains("OZON", ignoreCase = true) ||
                textSample.contains("ОЗОН", ignoreCase = true) ||
                textSample.contains("Ozon Банк", ignoreCase = true) ||
                textSample.contains("Озон Банк", ignoreCase = true)
            Timber.d(
                "Ozon isValidFormat: Проверка индикатора банка (OZON, ОЗОН, Ozon Банк, Озон Банк) -> %s",
                hasBankIndicator,
            )

            val hasStatementTitle = textSample.contains("Выписка по счёту", ignoreCase = true) ||
                textSample.contains("Выписка по счету", ignoreCase = true) ||
                textSample.contains("Информация по счёту", ignoreCase = true) ||
                textSample.contains("ИСТОРИЯ ОПЕРАЦИЙ", ignoreCase = true) ||
                textSample.contains("Справка о движении средств", ignoreCase = true)
            Timber.d(
                "Ozon isValidFormat: Проверка заголовка выписки (Выписка по счёту, Информация по счёту, ИСТОРИЯ ОПЕРАЦИЙ, Справка о движении средств) -> %s",
                hasStatementTitle,
            )

            val hasTableMarker = headerLines.any {
                it.contains("Дата", ignoreCase = true) &&
                    it.contains("Описание", ignoreCase = true) &&
                    it.contains("Сумма", ignoreCase = true)
            } || headerLines.any {
                it.contains("ДАТА И ВРЕМЯ", ignoreCase = true) &&
                    it.contains("ОПИСАНИЕ ОПЕРАЦИИ", ignoreCase = true) &&
                    it.contains("СУММА", ignoreCase = true)
            } || headerLines.any {
                it.contains("История операций", ignoreCase = true)
            } || headerLines.any {
                it.contains("Дата операции", ignoreCase = true) &&
                    it.contains("Документ", ignoreCase = true) &&
                    it.contains("Назначение платежа", ignoreCase = true) &&
                    it.contains("Сумма операции", ignoreCase = true)
            }
            Timber.d(
                "Ozon isValidFormat: Проверка маркеров таблицы (Дата, Описание, Сумма / ДАТА И ВРЕМЯ, ОПИСАНИЕ ОПЕРАЦИИ, СУММА / История операций / Дата операции, Документ, Назначение платежа, Сумма операции) -> %s",
                hasTableMarker,
            )

            val isValid = hasBankIndicator && hasStatementTitle && hasTableMarker
            Timber.i(
                "Ozon isValidFormat: Результат валидации: %s. Банк: %s, Заголовок: %s, Маркер таблицы: %s",
                isValid,
                hasBankIndicator,
                hasStatementTitle,
                hasTableMarker,
            )
            return isValid
        } catch (e: Exception) {
            Timber.e(e, "Ozon isValidFormat: Ошибка в процессе валидации формата")
            return false
        }
    }

    override fun skipHeaders(reader: BufferedReader) {
        Timber.d("Ozon skipHeaders: Начало пропуска заголовков...")
        var line: String?
        var linesSkipped = 0
        var tableHeaderFound = false
        val tableHeaderKeywords = listOf("ДАТА И ВРЕМЯ", "ОПИСАНИЕ ОПЕРАЦИИ", "СУММА", "БАЛАНС")
        val alternativeHeaderKeyword = "История операций"
        val statementHeaderKeyword = "Дата операции Документ Назначение платежа Сумма операции"

        // Сначала ищем заголовки таблицы
        while (true) {
            line = reader.readLine()?.replace("\\u0000", "")
            linesSkipped++
            if (line == null) {
                Timber.w(
                    "Ozon skipHeaders: Достигнут конец файла (пропущено %d строк) перед нахождением заголовка таблицы.",
                    linesSkipped,
                )
                return
            }
            Timber.v("Ozon skipHeaders: (Поиск заголовка) Строка %d: '%s'", linesSkipped, line)

            // Проверяем на заголовок "Справка о движении средств"
            if (line.trim().equals(statementHeaderKeyword, ignoreCase = true)) {
                Timber.d(
                    "Ozon skipHeaders: Найден заголовок справки о движении средств на строке %d: '%s'",
                    linesSkipped,
                    line,
                )
                tableHeaderFound = true
                break
            } else if (line.contains(alternativeHeaderKeyword, ignoreCase = true)) {
                Timber.d(
                    "Ozon skipHeaders: Найден альтернативный маркер заголовка '%s' на строке %d",
                    alternativeHeaderKeyword,
                    linesSkipped,
                )
                // Пропускаем саму строку с "История операций"
                // Следующая строка может быть периодом или заголовками столбцов

                // Проверяем следующую строку на период
                reader.mark(1024)
                val potentialPeriodLine = reader.readLine()?.replace("\\u0000", "")
                reader.reset()
                if (potentialPeriodLine != null && potentialPeriodLine.matches(
                        Regex(
                            "^за период с \\d{2}\\.\\d{2}\\.\\d{4} по \\d{2}\\.\\d{2}\\.\\d{4}$",
                            RegexOption.IGNORE_CASE,
                        ),
                    )
                ) {
                    val skippedPeriodLine = reader.readLine()?.replace("\\u0000", "") // Съедаем строку периода
                    linesSkipped++
                    Timber.d(
                        "Ozon skipHeaders: Пропущена строка с периодом после '%s' (строка %d): %s",
                        alternativeHeaderKeyword,
                        linesSkipped,
                        skippedPeriodLine,
                    )
                }

                // Ищем фактическое начало данных или заголовки столбцов
                while (true) {
                    reader.mark(1024)
                    val nextLineAfterMarker = reader.readLine()?.replace("\\u0000", "")
                    reader.reset()

                    if (nextLineAfterMarker == null) {
                        Timber.w(
                            "Ozon skipHeaders: Конец файла (пропущено %d строк) после поиска '%s'.",
                            linesSkipped,
                            alternativeHeaderKeyword,
                        )
                        return
                    }
                    Timber.v(
                        "Ozon skipHeaders: (После '%s') Строка %d: '%s'",
                        alternativeHeaderKeyword,
                        linesSkipped + 1,
                        nextLineAfterMarker,
                    )

                    // Проверяем, является ли это заголовками таблицы
                    if (tableHeaderKeywords.all { keyword ->
                            nextLineAfterMarker.contains(
                                keyword,
                                ignoreCase = true,
                            )
                        }
                    ) {
                        val skippedTableHeaderLine = reader.readLine()?.replace(
                            "\\u0000",
                            "",
                        ) // Съедаем строку заголовков
                        linesSkipped++
                        Timber.d(
                            "Ozon skipHeaders: Найдены и пропущены заголовки таблицы (строка %d) после '%s': %s",
                            linesSkipped,
                            alternativeHeaderKeyword,
                            skippedTableHeaderLine,
                        )
                        tableHeaderFound = true
                        break
                    } else if (nextLineAfterMarker.trim().matches(
                            Regex("^\\d{2}\\.\\d{2}\\.\\d{4}.*"),
                        )
                    ) { // Или это уже начало данных
                        Timber.d(
                            "Ozon skipHeaders: Найдено начало данных (строка %d) после '%s': %s",
                            linesSkipped + 1,
                            alternativeHeaderKeyword,
                            nextLineAfterMarker,
                        )
                        tableHeaderFound = true // Данные начинаются сразу, заголовков не было или они не подошли
                        break
                    } else {
                        val skippedIrrelevantLine = reader.readLine()?.replace(
                            "\\u0000",
                            "",
                        ) // Пропускаем незначимую строку
                        linesSkipped++
                        Timber.v(
                            "Ozon skipHeaders: Пропущена незначимая строка %d после '%s': %s",
                            linesSkipped,
                            alternativeHeaderKeyword,
                            skippedIrrelevantLine,
                        )
                    }
                }
                break
            } else if (tableHeaderKeywords.all { keyword ->
                    line.contains(
                        keyword,
                        ignoreCase = true,
                    )
                }
            ) {
                Timber.d(
                    "Ozon skipHeaders: Найдены заголовки таблицы (строка %d): '%s'",
                    linesSkipped,
                    line,
                )
                // Строка 'line' уже является заголовком, ее мы прочитали. Следующая строка должна быть либо данными, либо пустой.
                tableHeaderFound = true
                break
            }
        }
        Timber.d(
            "Ozon skipHeaders: Основной заголовок или маркер найден. Пропущено %d строк. Текущая строка (предположительно заголовки или первая строка данных): '%s'",
            linesSkipped,
            line,
        )

        // Дополнительный пропуск строк, если после основного заголовка есть еще строки, не являющиеся данными
        // Например, пустые строки или строки с разделителями, или вторая строка заголовков
        // Этот цикл нужен, если предыдущий остановился на строке заголовков, а не на данных.
        var finalLinesSkipped = linesSkipped
        while (true) {
            reader.mark(2048) // Отмечаем позицию перед чтением строки
            val currentLineForDataCheck = reader.readLine()?.replace("\\u0000", "")
            reader.reset() // Возвращаемся к отмеченной позиции, чтобы не "съесть" строку данных

            if (currentLineForDataCheck == null) {
                Timber.w(
                    "Ozon skipHeaders: Достигнут конец файла (всего пропущено %d строк) при пропуске дополнительных строк после заголовка.",
                    finalLinesSkipped,
                )
                break
            }

            Timber.v(
                "Ozon skipHeaders: (Проверка на данные) Строка %d: '%s'",
                finalLinesSkipped + 1,
                currentLineForDataCheck,
            )

            // Проверяем, является ли текущая строка началом данных
            val isDataLine = currentLineForDataCheck.trim().matches(
                Regex("^\\d{2}\\.\\d{2}\\.\\d{4}.*"),
            ) || // Начинается с даты
                currentLineForDataCheck.contains(Regex("[+\\-]?[\\d\\s.,]+[\\d]")) && // Содержит суммы
                !tableHeaderKeywords.any {
                    currentLineForDataCheck.contains(
                        it,
                        ignoreCase = true,
                    )
                } && // И не является строкой заголовка
                !currentLineForDataCheck.trim().equals(
                    statementHeaderKeyword,
                    ignoreCase = true,
                ) // И не является заголовком справки

            if (isDataLine) {
                Timber.i(
                    "Ozon skipHeaders: Найдена строка, похожая на начало данных: '%s' (строка %d). Заголовки полностью пропущены.",
                    currentLineForDataCheck.trim(),
                    finalLinesSkipped + 1,
                )
                break // Нашли данные, прекращаем пропуск
            } else {
                val skippedLine = reader.readLine()?.replace("\\u0000", "") // Теперь реально "съедаем" строку
                finalLinesSkipped++
                Timber.d(
                    "Ozon skipHeaders: (Пропуск доп. строки %d) Пропущена строка: %s",
                    finalLinesSkipped,
                    skippedLine,
                )
            }
        }
        Timber.i(
            "Ozon skipHeaders: Пропуск заголовков завершен. Всего пропущено %d строк.",
            finalLinesSkipped,
        )
    }

    override fun shouldSkipLine(line: String): Boolean {
        val trimmedLine = line.trim()
        Timber.v("Ozon shouldSkipLine: Проверка строки: '%s'", line)
        if (trimmedLine.isBlank()) {
            Timber.d("Ozon shouldSkipLine: ПРОПУСК (пустая): '%s'", line)
            return true
        }

        val patternsToSkip = listOf(
            Regex("^Итого:.*", RegexOption.IGNORE_CASE),
            Regex("^Перенесено со страницы.*", RegexOption.IGNORE_CASE),
            Regex("^Продолжение на странице.*", RegexOption.IGNORE_CASE),
            Regex("^Обороты по сч[её]ту за период.*", RegexOption.IGNORE_CASE),
            Regex("^Входящий остаток на начало периода.*", RegexOption.IGNORE_CASE),
            Regex("^Исходящий остаток на конец периода.*", RegexOption.IGNORE_CASE),
            Regex("^Страница \\d+ из \\d+.*", RegexOption.IGNORE_CASE),
            Regex("^Сформировано .* \\d{2}:\\d{2}:\\d{2}.*", RegexOption.IGNORE_CASE),
            Regex("^Подпись Банка.*", RegexOption.IGNORE_CASE),
            Regex("^Выписка по сч[её]ту №.*", RegexOption.IGNORE_CASE),
            Regex("^Период: с .* по .*", RegexOption.IGNORE_CASE),
            Regex("^ДАТА И ВРЕМЯ\\s+ОПИСАНИЕ ОПЕРАЦИИ\\s+СУММА.*", RegexOption.IGNORE_CASE),
        )

        for (pattern in patternsToSkip) {
            if (pattern.matches(trimmedLine)) {
                Timber.d(
                    "Ozon shouldSkipLine: ПРОПУСК (по паттерну '%s'): '%s'",
                    pattern.pattern,
                    line,
                )
                return true
            }
        }

        if (trimmedLine.equals("ДАТА И ВРЕМЯ ОПИСАНИЕ ОПЕРАЦИИ СУММА БАЛАНС", ignoreCase = true) ||
            trimmedLine.equals(
                "ДАТА И ВРЕМЯ МСК ОПИСАНИЕ ОПЕРАЦИИ СУММА В ВАЛЮТЕ ОПЕРАЦИИ СУММА В ВАЛЮТЕ СЧЕТА ОСТАТОК НА СЧЕТЕ",
                ignoreCase = true,
            ) ||
            (
                trimmedLine.contains("ОПИСАНИЕ ОПЕРАЦИИ", ignoreCase = true) &&
                    trimmedLine.contains("СУММА", ignoreCase = true) &&
                    trimmedLine.contains("БАЛАНС", ignoreCase = true) &&
                    trimmedLine.split(Regex("\\s{2,}")).size >= 3
                ) // Более строгая проверка на заголовок таблицы
        ) {
            Timber.d("Ozon shouldSkipLine: ПРОПУСК (повторяющийся заголовок таблицы): '%s'", line)
            return true
        }

        Timber.v("Ozon shouldSkipLine: НЕ ПРОПУСК: '%s'", line)
        return false
    }

    override fun parseLine(line: String): Transaction? {
        Timber.d("Ozon parseLine: Обработка строки для парсинга: '%s'", line)

        val trimmedLine = line.trim()
        if (trimmedLine.isBlank()) {
            Timber.v("Ozon parseLine: Пропуск пустой строки")
            return null
        }

        // Оригинальное регулярное выражение
        val originalTransactionRegex = Regex(
            "^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+" + // 1: Дата (DD.MM.YYYY)
                "((\\d{2}:\\d{2}:\\d{2})|(\\d{2}:\\d{2})|\\s*)\\s*" + // 2: Время (HH:MM:SS или HH:MM) или пробелы если нет времени (Группа 3, 4)
                "(.+?)\\s+" + // 5: Описание операции (нежадный захват до следующего паттерна суммы)
                "([+\\-])?\\s*([\\d\\s.,]+[\\d])\\s+" + // 6: Знак (+/-), 7: Сумма операции
                "([A-ZА-Я]{3})" + // 8: Валюта операции (RUB, USD, EUR, РУБ - теперь и кириллица)
                "(?:\\s+([+\\-])?\\s*([\\d\\s.,]+[\\d])\\s+([A-ZА-Я]{3}))?.*$", // 9: Знак баланса (опц), 10: Сумма баланса, 11: Валюта баланса (опц)
        )

        // Новое регулярное выражение для формата "Справка о движении средств"
        val statementTransactionRegex = Regex(
            "^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+" + // 1: Дата (DD.MM.YYYY)
                "(\\d{2}:\\d{2}:\\d{2})\\s+" + // 2: Время (HH:MM:SS)
                "(\\d+)\\s*$", // 3: Номер документа
        )

        // Регулярное выражение для строки суммы
        val amountLineRegex = Regex(
            "^([+\\-])?\\s*(\\d[\\d\\s.,]*)\\s*$", // 1: Знак (опционально), 2: Сумма
        )

        // Проверка на оригинальный формат транзакции
        val match = originalTransactionRegex.find(trimmedLine)
        if (match != null) {
            Timber.v(
                "Ozon parseLine: Строка '%s' соответствует оригинальному регулярному выражению.",
                line,
            )
            Timber.v("Ozon parseLine: Группы: %s", match.groupValues.joinToString(" | "))
            try {
                // Деструктуризация с учетом возможного отсутствия некоторых групп времени и баланса
                val dateStr = match.groupValues[1]
                val timeWithSecondsStr = match.groupValues[3]
                val timeShortStr = match.groupValues[4]
                val descriptionDirty = match.groupValues[5]
                val sign = match.groupValues[6] // Может быть пустым, если сумма положительная и без знака
                val amountStr = match.groupValues[7]
                val currencyStr = match.groupValues[8]
                // val balanceSign = match.groupValues[9] // Не используется пока
                // val balanceAmountStr = match.groupValues[10] // Не используется пока
                // val balanceCurrencyStr = match.groupValues[11] // Не используется пока

                val timeStr = if (timeWithSecondsStr.isNotBlank()) {
                    timeWithSecondsStr
                } else if (timeShortStr.isNotBlank()) {
                    timeShortStr
                } else {
                    "00:00:00"
                }

                val dateTimeStr = "$dateStr $timeStr"
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                val date = dateFormat.parse(dateTimeStr) ?: run {
                    Timber.w(
                        "Ozon parseLine: Не удалось распарсить дату '%s'. Исходная строка: '%s'",
                        dateTimeStr,
                        line,
                    )
                    Date() // Возвращаем текущую дату как fallback, или можно бросить исключение/вернуть null
                }

                val cleanedAmountStr = amountStr.replace("\\s".toRegex(), "").replace(",", ".")
                val amount = cleanedAmountStr.toDoubleOrNull() ?: run {
                    Timber.w(
                        "Ozon parseLine: Не удалось распарсить сумму '%s' (исходная '%s'). Исходная строка: '%s'",
                        cleanedAmountStr,
                        amountStr,
                        line,
                    )
                    return null // Критическая ошибка, если сумма не парсится
                }
                val finalAmount = if (sign == "-") -amount else amount
                val isExpense = sign == "-" || finalAmount < 0 // Убыток либо по знаку, либо по отрицательной сумме

                var description = descriptionDirty.trim()

                val category = TransactionCategoryDetector.detect(description)

                Timber.i(
                    "Ozon parseLine: Успешный парсинг: Дата='$date', Сумма='$finalAmount', Валюта='$currencyStr', Описание='$description', Категория='$category'",
                )

                // Сбрасываем состояние, т.к. нашли транзакцию стандартного формата
                currentTransactionState = null

                // Создаем объект Transaction с правильными параметрами согласно определению модели
                return Transaction(
                    amount = com.davidbugayov.financeanalyzer.domain.model.Money(
                        finalAmount,
                        Currency.valueOf(currencyStr.uppercase(Locale.ROOT)),
                    ),
                    category = category,
                    date = date,
                    isExpense = isExpense,
                    source = transactionSource,
                    sourceColor = 0,
                    title = description,
                    note = "Импортировано автоматически: $line",
                )
            } catch (e: Exception) {
                Timber.e(
                    e,
                    "Ozon parseLine: Ошибка при разборе полей из совпадения Regex для строки: '%s'",
                    line,
                )
                currentTransactionState = null // Сбрасываем состояние в случае ошибки
                return null
            }
        }

        // Новый формат "Справка о движении средств" - обработка многострочных транзакций

        // Попытка обработать строку как заголовок транзакции нового формата
        val statementMatch = statementTransactionRegex.find(trimmedLine)
        if (statementMatch != null) {
            Timber.v(
                "Ozon parseLine: Строка '%s' соответствует формату заголовка транзакции 'Справка о движении средств'",
                line,
            )

            // Завершаем предыдущую транзакцию, если она есть и содержит достаточно данных
            val transaction = finalizeCurrentTransaction()

            // Начинаем новую транзакцию
            val dateStr = statementMatch.groupValues[1]
            val timeStr = statementMatch.groupValues[2]
            val documentNumber = statementMatch.groupValues[3]

            val dateTimeStr = "$dateStr $timeStr"
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

            try {
                val date = dateFormat.parse(dateTimeStr) ?: Date()
                currentTransactionState = TransactionState(
                    date = date,
                    documentNumber = documentNumber,
                )
                Timber.d(
                    "Ozon parseLine: Начата новая транзакция с датой '$dateTimeStr' и номером документа '$documentNumber'",
                )
            } catch (e: Exception) {
                Timber.e(e, "Ozon parseLine: Ошибка при парсинге даты '$dateTimeStr'")
                currentTransactionState = null
            }

            return transaction // Возвращаем предыдущую завершенную транзакцию или null
        }

        // Попытка обработать строку с суммой
        val amountMatch = amountLineRegex.find(trimmedLine)
        if (amountMatch != null && currentTransactionState != null) {
            Timber.v("Ozon parseLine: Строка '%s' соответствует формату строки суммы", line)

            val sign = amountMatch.groupValues[1]
            val amountStr = amountMatch.groupValues[2].replace("\\s".toRegex(), "").replace(
                ",",
                ".",
            )

            try {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                val isExpense = sign == "-"
                // Для Ozon Bank в выписке:
                // "-" перед суммой означает списание/расход
                // "+" перед суммой означает пополнение/доход
                // Но в нашем приложении:
                // Положительное число = доход, Отрицательное число = расход
                val finalAmount = if (isExpense) -amount else amount

                currentTransactionState?.amount = Math.abs(finalAmount)
                currentTransactionState?.isExpense = isExpense

                Timber.d(
                    "Ozon parseLine: Для текущей транзакции добавлена сумма: $finalAmount, расход: $isExpense",
                )

                // Если нам удалось обработать сумму, это означает конец данных о транзакции
                val transaction = finalizeCurrentTransaction()
                currentTransactionState = null
                return transaction
            } catch (e: Exception) {
                Timber.e(e, "Ozon parseLine: Ошибка при парсинге суммы '$amountStr'")
                // Не сбрасываем состояние, возможно следующие строки помогут заполнить транзакцию
            }

            return null
        }

        // Если у нас есть незавершенная транзакция, и текущая строка не пустая, добавляем её к описанию
        if (currentTransactionState != null && trimmedLine.isNotBlank()) {
            currentTransactionState?.description?.append(
                if (currentTransactionState?.description?.isEmpty() == true) "" else " ",
            )
                ?.append(trimmedLine)
            Timber.d(
                "Ozon parseLine: Добавлена строка к описанию текущей транзакции: '$trimmedLine'",
            )
            return null
        }

        Timber.w("Ozon parseLine: Строка НЕ соответствует формату транзакции: '%s'", line)
        return null
    }

    // Вспомогательный метод для завершения текущей транзакции
    private fun finalizeCurrentTransaction(): Transaction? {
        val state = currentTransactionState ?: return null

        // Проверяем, есть ли все необходимые данные
        if (state.date == null || state.amount == null || state.description.isEmpty()) {
            Timber.w(
                "Ozon finalizeCurrentTransaction: Недостаточно данных для создания транзакции: date=${state.date}, amount=${state.amount}, description=${state.description}",
            )
            return null
        }

        val description = state.description.toString().trim()
        val category = TransactionCategoryDetector.detect(description)

        Timber.i(
            "Ozon finalizeCurrentTransaction: Формирование транзакции из состояния: date=${state.date}, amount=${state.amount}, isExpense=${state.isExpense}, description=$description, category=$category",
        )

        return Transaction(
            amount = com.davidbugayov.financeanalyzer.domain.model.Money(
                state.amount!!,
                Currency.valueOf(state.currency),
            ),
            category = category,
            date = state.date!!,
            isExpense = state.isExpense,
            source = transactionSource,
            sourceColor = 0,
            title = description,
            note = "Импортировано автоматически из Справки о движении средств (документ ${state.documentNumber})",
        )
    }
}
