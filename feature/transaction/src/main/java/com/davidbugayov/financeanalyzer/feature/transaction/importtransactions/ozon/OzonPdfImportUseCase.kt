package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ozon

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider
import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.category.TransactionCategoryDetector
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.BankImportUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportProgressCallback
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportResult
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.BufferedReader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber

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

    private suspend fun extractTextFromPdf(uri: Uri): String =
        withContext(Dispatchers.IO) {
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
    ): Flow<ImportResult> =
        flow {
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
            } catch (e: StatisticsFileException) {
                // Специальная обработка для файлов статистики
                Timber.w("Обнаружен файл статистики: ${e.message}")
                emit(
                    ImportResult.Error(
                        message = context.getString(R.string.import_error_statistics_file),
                    ),
                )
                return@flow
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при проверке формата файла: ${e.message}")
                emit(
                    ImportResult.Error(
                        exception = e,
                        message = "Ошибка при проверке формата файла: ${e.message ?: "Неизвестная ошибка"}",
                    ),
                )
                return@flow
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
                        bankName = bankName,
                    ),
                )
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обработке транзакций: ${e.message}")
                CrashLoggerProvider.crashLogger.logException(e)
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
                    CrashLoggerProvider.crashLogger.logDatabaseError(
                        "saveBatchOfTransactions",
                        "Ошибка при сохранении транзакции",
                        e,
                    )
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

            val hasBankIndicator =
                textSample.contains("OZON", ignoreCase = true) ||
                    textSample.contains("ОЗОН", ignoreCase = true) ||
                    textSample.contains("Ozon Банк", ignoreCase = true) ||
                    textSample.contains("Озон Банк", ignoreCase = true)
            Timber.d(
                "Ozon isValidFormat: Проверка индикатора банка (OZON, ОЗОН, Ozon Банк, Озон Банк) -> %s",
                hasBankIndicator,
            )

            val hasStatementTitle =
                textSample.contains("Выписка по счёту", ignoreCase = true) ||
                    textSample.contains("Выписка по счету", ignoreCase = true) ||
                    textSample.contains("Информация по счёту", ignoreCase = true) ||
                    textSample.contains("ИСТОРИЯ ОПЕРАЦИЙ", ignoreCase = true) ||
                    textSample.contains("Справка о движении средств", ignoreCase = true)
            Timber.d(
                "Ozon isValidFormat: Проверка заголовка выписки (Выписка по счёту, Информация по счёту, ИСТОРИЯ ОПЕРАЦИЙ, Справка о движении средств) -> %s",
                hasStatementTitle,
            )

            val hasTableMarker =
                headerLines.any {
                    it.contains("Дата", ignoreCase = true) &&
                        it.contains("Описание", ignoreCase = true) &&
                        it.contains("Сумма", ignoreCase = true)
                } ||
                    headerLines.any {
                        it.contains("ДАТА И ВРЕМЯ", ignoreCase = true) &&
                            it.contains("ОПИСАНИЕ ОПЕРАЦИИ", ignoreCase = true) &&
                            it.contains("СУММА", ignoreCase = true)
                    } ||
                    headerLines.any {
                        it.contains("История операций", ignoreCase = true)
                    } ||
                    headerLines.any {
                        it.contains("Дата операции", ignoreCase = true) &&
                            it.contains("Документ", ignoreCase = true) &&
                            it.contains("Назначение платежа", ignoreCase = true) &&
                            it.contains("Сумма операции", ignoreCase = true)
                    } ||
                    headerLines.any {
                        // Проверка для формата из скриншота
                        (it.contains("Дата операции", ignoreCase = true) && it.contains("Документ", ignoreCase = true)) ||
                            (it.contains("Назначение платежа", ignoreCase = true) && it.contains("Сумма операции", ignoreCase = true)) ||
                            (it.contains("Российские рубли", ignoreCase = true) && it.contains("Валюта", ignoreCase = true))
                    } || textSample.contains("Входящий остаток", ignoreCase = true) // Дополнительный индикатор для выписки с транзакциями

            // Подробные логи для отладки определения маркеров таблицы
            Timber.i("ОТЛАДКА-ОЗОН: Содержимое первых строк файла для определения формата:")
            headerLines.forEachIndexed { index, line ->
                Timber.i("ОТЛАДКА-ОЗОН: Строка %d: '%s'", index + 1, line)
            }

            Timber.i("ОТЛАДКА-ОЗОН: Проверка наличия ключевых слов в тексте:")
            Timber.i("ОТЛАДКА-ОЗОН: 'Дата операции': %s", textSample.contains("Дата операции", ignoreCase = true))
            Timber.i("ОТЛАДКА-ОЗОН: 'Документ': %s", textSample.contains("Документ", ignoreCase = true))
            Timber.i(
                "ОТЛАДКА-ОЗОН: 'Назначение платежа': %s",
                textSample.contains("Назначение платежа", ignoreCase = true),
            )
            Timber.i("ОТЛАДКА-ОЗОН: 'Сумма операции': %s", textSample.contains("Сумма операции", ignoreCase = true))
            Timber.i("ОТЛАДКА-ОЗОН: 'Российские рубли': %s", textSample.contains("Российские рубли", ignoreCase = true))
            Timber.i("ОТЛАДКА-ОЗОН: 'Валюта': %s", textSample.contains("Валюта", ignoreCase = true))
            Timber.i("ОТЛАДКА-ОЗОН: 'Входящий остаток': %s", textSample.contains("Входящий остаток", ignoreCase = true))

            Timber.d(
                "Ozon isValidFormat: Проверка маркеров таблицы (Дата, Описание, Сумма / ДАТА И ВРЕМЯ, ОПИСАНИЕ ОПЕРАЦИИ, СУММА / История операций / Дата операции, Документ, Назначение платежа, Сумма операции) -> %s",
                hasTableMarker,
            )

            // Проверка на статистический файл (справка о движении средств без таблицы транзакций)
            val isStatisticsFile =
                hasBankIndicator && hasStatementTitle && !hasTableMarker &&
                    textSample.contains("движени", ignoreCase = true) &&
                    textSample.contains("средств", ignoreCase = true) &&
                    !textSample.contains("Входящий остаток", ignoreCase = true) // Если есть "Входящий остаток", то это не статистика, а выписка

            Timber.i("ОТЛАДКА-ОЗОН: Проверка на статистический файл: %s", isStatisticsFile)
            Timber.i("ОТЛАДКА-ОЗОН: Условия для статистического файла:")
            Timber.i("ОТЛАДКА-ОЗОН: - hasBankIndicator: %s", hasBankIndicator)
            Timber.i("ОТЛАДКА-ОЗОН: - hasStatementTitle: %s", hasStatementTitle)
            Timber.i("ОТЛАДКА-ОЗОН: - !hasTableMarker: %s", !hasTableMarker)
            Timber.i("ОТЛАДКА-ОЗОН: - содержит 'движени': %s", textSample.contains("движени", ignoreCase = true))
            Timber.i("ОТЛАДКА-ОЗОН: - содержит 'средств': %s", textSample.contains("средств", ignoreCase = true))
            Timber.i(
                "ОТЛАДКА-ОЗОН: - НЕ содержит 'Входящий остаток': %s",
                !textSample.contains("Входящий остаток", ignoreCase = true),
            )

            if (isStatisticsFile) {
                Timber.w("Ozon isValidFormat: Обнаружен файл со статистикой движения средств, а не с транзакциями")
                throw StatisticsFileException(
                    "Файл содержит статистические данные о движении средств, а не транзакции.",
                )
            }

            val isValid = hasBankIndicator && hasStatementTitle && hasTableMarker
            Timber.i(
                "Ozon isValidFormat: Результат валидации: %s. Банк: %s, Заголовок: %s, Маркер таблицы: %s",
                isValid,
                hasBankIndicator,
                hasStatementTitle,
                hasTableMarker,
            )
            return isValid
        } catch (e: StatisticsFileException) {
            // Пробрасываем специальную ошибку дальше
            Timber.w("Ozon isValidFormat: ${e.message}")
            throw e
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
        val newFormatHeaderKeywords =
            listOf("Дата операции", "Документ", "Назначение платежа", "Сумма операции", "Российские рубли", "Валюта")

        // Для отладки сохраним первые 30 строк
        val headerLines = mutableListOf<String>()
        reader.mark(8192)
        repeat(30) {
            val headerLine = reader.readLine()?.replace("\\u0000", "")
            if (headerLine != null) {
                headerLines.add(headerLine)
            }
        }
        reader.reset()

        Timber.i("ОТЛАДКА-ОЗОН-SKIP: Первые 30 строк файла:")
        headerLines.forEachIndexed { index, headerLine ->
            Timber.i("ОТЛАДКА-ОЗОН-SKIP: Строка %d: '%s'", index + 1, headerLine)
        }

        // Сначала ищем заголовки таблицы
        while (true) {
            reader.mark(1024)
            line = reader.readLine()?.replace("\\u0000", "")
            linesSkipped++
            if (line == null) {
                Timber.w(
                    "Ozon skipHeaders: Достигнут конец файла (пропущено %d строк) перед нахождением заголовка таблицы.",
                    linesSkipped,
                )
                reader.reset() // Возвращаемся к началу файла
                return
            }
            Timber.i("ОТЛАДКА-ОЗОН-SKIP: (Поиск заголовка) Строка %d: '%s'", linesSkipped, line)

            // Проверка на новый формат из скриншота
            if (line.contains("Дата операции", ignoreCase = true) &&
                (
                    line.contains("Документ", ignoreCase = true) ||
                        line.contains("Назначение платежа", ignoreCase = true)
                )
            ) {
                Timber.i("ОТЛАДКА-ОЗОН-SKIP: Найден заголовок нового формата на строке %d: '%s'", linesSkipped, line)

                // Проверяем следующую строку, там может быть продолжение заголовка
                reader.mark(1024)
                val nextLine = reader.readLine()?.replace("\\u0000", "")

                if (nextLine != null && (
                        nextLine.contains("Сумма операции", ignoreCase = true) ||
                            nextLine.contains("Российские рубли", ignoreCase = true) ||
                            nextLine.contains("Валюта", ignoreCase = true)
                    )
                ) {
                    Timber.i(
                        "ОТЛАДКА-ОЗОН-SKIP: Найдено продолжение заголовка на строке %d: '%s'",
                        linesSkipped + 1,
                        nextLine,
                    )
                    linesSkipped++

                    // Проверяем еще одну строку, там может быть третья часть заголовка
                    reader.mark(1024)
                    val thirdLine = reader.readLine()?.replace("\\u0000", "")

                    if (thirdLine != null && (
                            thirdLine.contains("Российские рубли", ignoreCase = true) ||
                                thirdLine.contains("Валюта", ignoreCase = true)
                        )
                    ) {
                        Timber.i(
                            "ОТЛАДКА-ОЗОН-SKIP: Найдена третья часть заголовка на строке %d: '%s'",
                            linesSkipped + 1,
                            thirdLine,
                        )
                        linesSkipped++
                    } else {
                        reader.reset() // Возвращаемся к предыдущей позиции
                    }
                } else {
                    reader.reset() // Возвращаемся к предыдущей позиции
                }

                tableHeaderFound = true
                break
            }

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
                if (potentialPeriodLine != null &&
                    potentialPeriodLine.matches(
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
                        val skippedTableHeaderLine =
                            reader.readLine()?.replace(
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
                        val skippedIrrelevantLine =
                            reader.readLine()?.replace(
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
                    "Ozon skipHeaders: Найдены заголовки таблицы (строка %d): %s",
                    linesSkipped,
                    line,
                )
                tableHeaderFound = true
                break
            } else if (line.trim().matches(Regex("^\\d{2}\\.\\d{2}\\.\\d{4}.*"))) {
                // Это может быть начало данных транзакции
                Timber.i(
                    "ОТЛАДКА-ОЗОН-SKIP: Найдено возможное начало данных транзакции (строка %d): %s",
                    linesSkipped,
                    line,
                )
                reader.reset() // Возвращаемся к началу строки, чтобы не пропустить транзакцию
                tableHeaderFound = true
                break
            }
        }

        if (tableHeaderFound) {
            Timber.i(
                "ОТЛАДКА-ОЗОН-SKIP: Заголовки таблицы успешно найдены и пропущены (всего пропущено %d строк)",
                linesSkipped,
            )
        } else {
            Timber.w("Ozon skipHeaders: Заголовки таблицы не найдены после пропуска %d строк", linesSkipped)
        }
    }

    override fun shouldSkipLine(line: String): Boolean {
        val trimmedLine = line.trim()
        Timber.v("Ozon shouldSkipLine: Проверка строки: '%s'", line)
        if (trimmedLine.isBlank()) {
            Timber.d("Ozon shouldSkipLine: ПРОПУСК (пустая): '%s'", line)
            return true
        }

        val patternsToSkip =
            listOf(
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

        // ОТЛАДКА: Вывод строки в формате для анализа регулярных выражений
        Timber.i("ОТЛАДКА-ОЗОН: Анализ строки: '%s'", trimmedLine)

        // Оригинальное регулярное выражение
        val originalTransactionRegex =
            Regex(
                "^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+" + // 1: Дата (DD.MM.YYYY)
                    "((\\d{2}:\\d{2}:\\d{2})|(\\d{2}:\\d{2})|\\s*)\\s*" + // 2: Время (HH:MM:SS или HH:MM) или пробелы если нет времени (Группа 3, 4)
                    "(.+?)\\s+" + // 5: Описание операции (нежадный захват до следующего паттерна суммы)
                    "([+\\-])?\\s*([\\d\\s.,]+[\\d])\\s+" + // 6: Знак (+/-), 7: Сумма операции
                    "([A-ZА-Я]{3})" + // 8: Валюта операции (RUB, USD, EUR, РУБ - теперь и кириллица)
                    "(?:\\s+([+\\-])?\\s*([\\d\\s.,]+[\\d])\\s+([A-ZА-Я]{3}))?.*$", // 9: Знак баланса (опц), 10: Сумма баланса, 11: Валюта баланса (опц)
            )

        // Новое регулярное выражение для формата "Справка о движении средств"
        val statementTransactionRegex =
            Regex(
                "^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+" + // 1: Дата (DD.MM.YYYY)
                    "(\\d{2}:\\d{2}:\\d{2})\\s+" + // 2: Время (HH:MM:SS)
                    "(\\d+)\\s*$", // 3: Номер документа
            )

        // ОТЛАДКА: Новое регулярное выражение для формата из скриншота
        val newFormatRegex =
            Regex(
                "^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+" + // 1: Дата (DD.MM.YYYY)
                    "(\\d{2}:\\d{2}:\\d{2})\\s+" + // 2: Время (HH:MM:SS)
                    "(\\d+)\\s+" + // 3: Номер документа
                    "(.+?)\\s+" + // 4: Описание операции
                    "([+\\-])\\s*(\\d[\\d\\s.,]*)\\s*₽.*$", // 5: Знак, 6: Сумма с символом рубля
            )

        // Дополнительное регулярное выражение для случая, когда дата и время на разных строках
        val multilineTransactionRegex =
            Regex(
                "^(\\d{2}\\.\\d{2}\\.\\d{4})$", // 1: Дата (DD.MM.YYYY)
            )

        // Регулярное выражение для строки времени
        val timeLineRegex =
            Regex(
                "^(\\d{2}:\\d{2}:\\d{2})$", // 1: Время (HH:MM:SS)
            )

        // Регулярное выражение для строки номера документа
        val documentLineRegex =
            Regex(
                "^(\\d+)$", // 1: Номер документа
            )

        // Регулярное выражение для строки суммы
        val amountLineRegex =
            Regex(
                "^([+\\-])?\\s*(\\d[\\d\\s.,]*)\\s*₽.*$", // 1: Знак (опционально), 2: Сумма с символом рубля
            )

        // Проверяем новый формат из скриншота
        val newFormatMatch = newFormatRegex.find(trimmedLine)
        if (newFormatMatch != null) {
            Timber.i("ОТЛАДКА-ОЗОН: Строка соответствует новому формату из скриншота!")
            Timber.i("ОТЛАДКА-ОЗОН: Группы нового формата: %s", newFormatMatch.groupValues.joinToString(" | "))

            try {
                val dateStr = newFormatMatch.groupValues[1]
                val timeStr = newFormatMatch.groupValues[2]
                val documentNumber = newFormatMatch.groupValues[3]
                val description = newFormatMatch.groupValues[4]
                val sign = newFormatMatch.groupValues[5]
                val amountStr = newFormatMatch.groupValues[6].replace("\\s".toRegex(), "").replace(",", ".")

                val dateTimeStr = "$dateStr $timeStr"
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                val date = dateFormat.parse(dateTimeStr) ?: Date()

                val amount = amountStr.toDoubleOrNull() ?: 0.0
                val isExpense = sign == "-"
                val finalAmount = if (isExpense) -amount else amount

                val category = TransactionCategoryDetector.detect(description)

                Timber.i(
                    "ОТЛАДКА-ОЗОН: Успешный парсинг нового формата: Дата='%s', Сумма='%s', Описание='%s', Категория='%s'",
                    date,
                    finalAmount,
                    description,
                    category,
                )

                // Создаем объект Transaction
                return Transaction(
                    amount =
                        Money(
                            finalAmount,
                            Currency.valueOf("RUB"),
                        ),
                    category = category,
                    date = date,
                    isExpense = isExpense,
                    source = transactionSource,
                    sourceColor = 0,
                    title = description,
                    note = "Импортировано автоматически из Озон Банка (документ $documentNumber)",
                )
            } catch (e: Exception) {
                Timber.e(e, "ОТЛАДКА-ОЗОН: Ошибка при парсинге нового формата: %s", e.message)
                return null
            }
        }

        // Проверка на строку с датой (начало многострочной транзакции)
        val multilineMatch = multilineTransactionRegex.find(trimmedLine)
        if (multilineMatch != null) {
            Timber.i("ОТЛАДКА-ОЗОН: Строка соответствует формату даты для многострочной транзакции: %s", trimmedLine)

            // Начинаем новую транзакцию
            val dateStr = multilineMatch.groupValues[1]

            // Сохраняем дату в состоянии
            try {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val date = dateFormat.parse(dateStr) ?: Date()
                currentTransactionState =
                    TransactionState(
                        date = date,
                    )
                Timber.i("ОТЛАДКА-ОЗОН: Начата новая многострочная транзакция с датой '%s'", dateStr)
            } catch (e: Exception) {
                Timber.e(e, "ОТЛАДКА-ОЗОН: Ошибка при парсинге даты '%s'", dateStr)
                currentTransactionState = null
            }

            return null
        }

        // Проверка на строку с временем
        if (currentTransactionState != null && currentTransactionState?.date != null) {
            val timeMatch = timeLineRegex.find(trimmedLine)
            if (timeMatch != null) {
                Timber.i("ОТЛАДКА-ОЗОН: Строка соответствует формату времени: %s", trimmedLine)

                val timeStr = timeMatch.groupValues[1]
                try {
                    // Обновляем дату, добавляя время
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                    val dateStr = dateFormat.format(currentTransactionState?.date!!)
                    val time = timeFormat.parse(timeStr)

                    val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                    val dateTimeStr = "$dateStr $timeStr"
                    val newDate = dateTimeFormat.parse(dateTimeStr) ?: currentTransactionState?.date

                    currentTransactionState?.date = newDate
                    Timber.i("ОТЛАДКА-ОЗОН: Добавлено время '%s' к дате транзакции", timeStr)
                } catch (e: Exception) {
                    Timber.e(e, "ОТЛАДКА-ОЗОН: Ошибка при добавлении времени '%s' к дате", timeStr)
                }

                return null
            }
        }

        // Проверка на строку с номером документа
        if (currentTransactionState != null && currentTransactionState?.date != null) {
            val documentMatch = documentLineRegex.find(trimmedLine)
            if (documentMatch != null) {
                Timber.i("ОТЛАДКА-ОЗОН: Строка соответствует формату номера документа: %s", trimmedLine)

                val documentNumber = documentMatch.groupValues[1]
                currentTransactionState?.documentNumber = documentNumber
                Timber.i("ОТЛАДКА-ОЗОН: Добавлен номер документа '%s' к транзакции", documentNumber)

                return null
            }
        }

        // Проверка на строку с суммой в новом формате
        if (currentTransactionState != null && currentTransactionState?.date != null) {
            val newAmountMatch = amountLineRegex.find(trimmedLine)
            if (newAmountMatch != null) {
                Timber.i("ОТЛАДКА-ОЗОН: Строка соответствует формату суммы с символом рубля: %s", trimmedLine)

                val sign = newAmountMatch.groupValues[1]
                val amountStr = newAmountMatch.groupValues[2].replace("\\s".toRegex(), "").replace(",", ".")

                try {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    val isExpense = sign == "-"
                    val finalAmount = if (isExpense) -amount else amount

                    currentTransactionState?.amount = Math.abs(finalAmount)
                    currentTransactionState?.isExpense = isExpense
                    currentTransactionState?.currency = "RUB"

                    Timber.i("ОТЛАДКА-ОЗОН: Добавлена сумма %s к транзакции, расход: %s", finalAmount, isExpense)

                    // Если у нас есть все необходимые данные, завершаем транзакцию
                    if (currentTransactionState?.date != null &&
                        currentTransactionState?.documentNumber != null &&
                        !currentTransactionState?.description.isNullOrEmpty()
                    ) {
                        val transaction = finalizeCurrentTransaction()
                        currentTransactionState = null
                        return transaction
                    }
                } catch (e: Exception) {
                    Timber.e(e, "ОТЛАДКА-ОЗОН: Ошибка при парсинге суммы '%s'", amountStr)
                }

                return null
            }
        }

        // Если у нас есть незавершенная транзакция, и текущая строка не пустая и не соответствует
        // ни одному из специальных форматов, считаем её частью описания
        if (currentTransactionState != null && trimmedLine.isNotBlank() &&
            !trimmedLine.matches(Regex("^\\d{2}\\.\\d{2}\\.\\d{4}$")) &&
            !trimmedLine.matches(Regex("^\\d{2}:\\d{2}:\\d{2}$")) &&
            !trimmedLine.matches(Regex("^\\d+$")) &&
            !amountLineRegex.matches(trimmedLine)
        ) {
            currentTransactionState?.description?.append(
                if (currentTransactionState?.description?.isEmpty() == true) "" else " ",
            )
                ?.append(trimmedLine)
            Timber.i(
                "ОТЛАДКА-ОЗОН: Добавлена строка к описанию текущей транзакции: '%s'",
                trimmedLine,
            )
            return null
        }

        Timber.i("ОТЛАДКА-ОЗОН: Строка НЕ соответствует ни одному формату транзакции: '%s'", trimmedLine)
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
            amount =
                Money(
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

    /**
     * Специальное исключение для случаев, когда файл содержит статистические данные, а не транзакции
     */
    class StatisticsFileException(message: String) : Exception(message)
}
