package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.model.ImportProgressCallback
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Абстрактный базовый UseCase для импорта транзакций из файла конкретного банка.
 * Предоставляет общую структуру для парсинга файлов.
 */
abstract class BankImportUseCase(
    protected val transactionRepository: TransactionRepository,
    protected val context: Context
) : ImportTransactionsUseCase {

    /**
     * Название банка, для которого предназначен этот UseCase.
     * Должно быть переопределено в конкретных реализациях.
     */
    abstract val bankName: String

    /**
     * Проверяет, соответствует ли файл (представленный через BufferedReader)
     * ожидаемому формату этого банка.
     * @param reader BufferedReader для чтения содержимого файла.
     * @return true, если формат действителен.
     */
    abstract fun isValidFormat(reader: BufferedReader): Boolean

    /**
     * Пропускает строки заголовков в файле.
     * @param reader BufferedReader для чтения содержимого файла.
     */
    abstract fun skipHeaders(reader: BufferedReader)

    /**
     * Парсит одну строку из файла и преобразует ее в объект Transaction.
     * @param line Строка из файла.
     * @return Распарсенная транзакция или null, если строка не является транзакцией или не может быть спарсена корректно.
     * @throws Exception если строка имеет неверный формат (можно не бросать, а вернуть null и залогировать).
     */
    abstract fun parseLine(line: String): Transaction?

    /**
     * Определяет, следует ли пропустить данную строку при парсинге.
     * Например, пустые строки, итоговые суммы и т.д.
     * @param line Строка из файла.
     * @return true, если строку следует пропустить.
     */
    open fun shouldSkipLine(line: String): Boolean {
        return line.isBlank()
    }

    /**
     * Processes transactions from a given BufferedReader.
     * This method contains the core logic for validating format, skipping headers,
     * parsing lines, and saving transactions.
     *
     * @param reader The BufferedReader to read transaction data from.
     * @param progressCallback Callback for reporting import progress.
     * @return Flow emitting import results.
     */
    protected open fun processTransactionsFromReader(
        reader: BufferedReader,
        progressCallback: ImportProgressCallback
    ): Flow<ImportResult> = flow {
        var importedCount = 0
        var skippedCount = 0
        val importedTransactions = mutableListOf<Transaction>()

        try {
            emit(ImportResult.progress(5, 100, "Проверка формата файла для банка $bankName..."))
            // Важно: isValidFormat может "потребить" часть потока.
            // Убедитесь, что reader поддерживает mark/reset, если isValidFormat читает вперед.
            if (!isValidFormat(reader)) {
                Timber.w("Файл не соответствует формату банка $bankName")
                emit(ImportResult.error("Файл не соответствует формату банка $bankName."))
                return@flow
            }

            emit(ImportResult.progress(10, 100, "Пропуск заголовков..."))
            skipHeaders(reader) // Пропускаем заголовки

            var line = reader.readLine()
            var lineNumber = 0

            // TODO: Определить общее количество строк для более точного прогресса, если возможно.
            // Это сложно сделать, не прочитав весь reader заранее, что не всегда оптимально.

            // Примерное количество строк для расчета прогресса
            val estimatedLines = 100 // Это приблизительное значение

            while (line != null) {
                lineNumber++
                // Обновляем прогресс каждые 10 строк
                if (lineNumber % 10 == 0) {
                    val progress = 10 + (lineNumber * 70 / estimatedLines).coerceAtMost(70)
                    emit(ImportResult.progress(progress, 100, "Обработка строки $lineNumber..."))
                    progressCallback.onProgress(progress, 100, "Обработка строки $lineNumber из примерно $estimatedLines...")
                }

                if (shouldSkipLine(line)) {
                    Timber.v("[ИМПОРТ] Пропуск строки $lineNumber для банка $bankName: '$line'")
                } else {
                    try {
                        Timber.d("[ИМПОРТ] Обработка строки $lineNumber для банка $bankName: '$line'")
                        val transaction = parseLine(line)
                        if (transaction != null) {
                            Timber.i("[ИМПОРТ] Создана транзакция из строки $lineNumber: ID=${transaction.id}, Сумма=${transaction.amount}, Дата=${transaction.date}, Категория='${transaction.category}'")
                            importedTransactions.add(transaction)
                            importedCount++
                        } else {
                            Timber.w("[ИМПОРТ] Не удалось создать транзакцию из строки $lineNumber")
                            skippedCount++
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "[ИМПОРТ] ❌ Ошибка парсинга строки $lineNumber для банка $bankName: '$line'")
                        skippedCount++
                    }
                }
                line = reader.readLine()
            }

            if (importedTransactions.isNotEmpty()) {
                Timber.i("[ИМПОРТ] Подготовлено ${importedTransactions.size} транзакций для сохранения в базу данных (банк $bankName)")
                emit(ImportResult.progress(90, 100, "Сохранение транзакций..."))
                progressCallback.onProgress(90, 100, "Сохранение ${importedTransactions.size} транзакций...")
                try {
                    Timber.d("[ИМПОРТ] Начало сохранения ${importedTransactions.size} транзакций в базу данных")

                    var savedCount = 0
                    importedTransactions.forEach { transaction ->
                        try {
                            Timber.d("[ИМПОРТ] Сохранение транзакции: ID=${transaction.id}, Сумма=${transaction.amount}, Дата=${transaction.date}")
                            Timber.i("[ИМПОРТ-ОТЛАДКА] ⚠️ ПЕРЕД вызовом transactionRepository.addTransaction для ID=${transaction.id}")
                            val result = transactionRepository.addTransaction(transaction)
                            Timber.i("[ИМПОРТ-ОТЛАДКА] ✅ ПОСЛЕ вызова transactionRepository.addTransaction для ID=${transaction.id}, результат=$result")
                            savedCount++
                            Timber.d("[ИМПОРТ] Транзакция успешно сохранена (${savedCount}/${importedTransactions.size})")
                        } catch (ex: Exception) {
                            Timber.e(ex, "[ИМПОРТ] ❌ Ошибка при сохранении транзакции ID=${transaction.id}: ${ex.message}")
                            Timber.e("[ИМПОРТ-ОТЛАДКА] 🔍 Детали транзакции с ошибкой: ID=${transaction.id}, amount=${transaction.amount}, date=${transaction.date}, category=${transaction.category}, title=${transaction.title}")
                            Timber.e("[ИМПОРТ-ОТЛАДКА] 🔍 Стек вызовов: ${ex.stackTraceToString()}")
                        }
                    }

                    // Проверяем сколько транзакций было сохранено
                    Timber.i("[ИМПОРТ] Успешно сохранено $savedCount из ${importedTransactions.size} транзакций для банка $bankName.")

                    // Дополнительная проверка количества транзакций в базе данных
                    try {
                        Timber.d("[ИМПОРТ-ПРОВЕРКА] Проверка общего количества транзакций в базе...")
                        val allTransactions = transactionRepository.getAllTransactions()
                        Timber.i("[ИМПОРТ-ПРОВЕРКА] Общее количество транзакций в базе после импорта: ${allTransactions.size}")
                    } catch (e: Exception) {
                        Timber.e(e, "[ИМПОРТ-ПРОВЕРКА] Ошибка при проверке количества транзакций: ${e.message}")
                    }

                    emit(ImportResult.success(savedCount, skippedCount))
                } catch (e: Exception) {
                    Timber.e(e, "[ИМПОРТ] ❌ Ошибка при сохранении транзакций: ${e.message}")
                    emit(ImportResult.error("Ошибка при сохранении транзакций: ${e.message}"))
                }
            } else {
                Timber.w("[ИМПОРТ] Нет транзакций для импорта")
                emit(ImportResult.success(0, skippedCount))
            }
        } catch (e: Exception) {
            Timber.e(e, "[ИМПОРТ] ❌ Общая ошибка при импорте: ${e.message}")
            emit(ImportResult.error("Ошибка при импорте: ${e.message}"))
        }
    }

    /**
     * Основной метод импорта, который использует BufferedReader.
     * Конкретные реализации для PDF/Excel могут переопределить importTransactions
     * или предоставить свою логику, которая в итоге вызовет этот метод, если применимо.
     */
    @Deprecated(
        "Используйте importTransactions, который теперь напрямую работает с URI и вызывает processTransactionsFromReader. Этот метод может быть удален в будущем.",
        ReplaceWith("importTransactions(uri, progressCallback)")
    )
    protected open suspend fun importFromReader(
        uri: Uri,
        progressCallback: ImportProgressCallback
    ): ImportResult = withContext(Dispatchers.IO) {
        // Эта реализация теперь в основном делегирует processTransactionsFromReader,
        // но для совместимости и демонстрации, как это могло бы быть.
        // В новой структуре importTransactions открывает reader и вызывает processTransactionsFromReader.
        Timber.w("Вызван устаревший метод importFromReader для $bankName. Рассмотрите переход на новую структуру.")
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    // processTransactionsFromReader возвращает Flow, а importFromReader - ImportResult.
                    // Это неудобно для прямого вызова.
                    // Для сохранения старого контракта, мы бы собирали Flow здесь, но это неэффективно.
                    // Вместо этого, указываем, что этот метод устарел.
                    // Логика ниже - это то, что было бы, если бы мы адаптировали.
                    // В текущем рефакторинге, `importTransactions` будет вызывать `processTransactionsFromReader` напрямую.
                    return@withContext ImportResult.error("importFromReader устарел и не должен вызываться напрямую в новой структуре.")

                    // Пример того, как можно было бы собрать Flow, если бы это было необходимо (НЕ ИСПОЛЬЗУЕТСЯ):
                    /*
                    var finalResult: ImportResult = ImportResult.error("Не удалось инициализировать импорт")
                    processTransactionsFromReader(reader, progressCallback)
                        .collect { result ->
                            if (result is ImportResult.Success || result is ImportResult.Error) {
                                finalResult = result
                            }
                            // Прогресс можно логировать или передавать дальше через progressCallback,
                            // но progressCallback уже используется внутри processTransactionsFromReader.
                        }
                    return@withContext finalResult
                    */
                }
            } ?: ImportResult.error("Не удалось открыть файл $uri для банка $bankName")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка в устаревшем importFromReader для банка $bankName из $uri: ${e.message}")
            ImportResult.error("Ошибка импорта для $bankName: ${e.message}")
        }
    }

    /**
     * Реализация основного метода интерфейса ImportTransactionsUseCase.
     * По умолчанию вызывает importFromReader.
     * Может быть переопределен для форматов, не работающих с BufferedReader (например, бинарные Excel или PDF, где текст извлекается иначе).
     */
    override fun importTransactions(
        uri: Uri,
        progressCallback: ImportProgressCallback
    ): Flow<ImportResult> = flow {
        emit(ImportResult.progress(0, 100, "Начало импорта для банка $bankName..."))
        progressCallback.onProgress(0, 100, "Начало импорта для банка $bankName...")

        try {
            withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        emitAll(processTransactionsFromReader(reader, progressCallback))
                    }
                } ?: run {
                    Timber.e("Не удалось открыть InputStream для URI: $uri в банке $bankName")
                    emit(ImportResult.error("Не удалось открыть файл $uri для банка $bankName"))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при открытии файла или обработке в importTransactions для банка $bankName из $uri: ${e.message}")
            emit(ImportResult.error("Ошибка импорта для $bankName: ${e.message}"))
        }
    }
} 