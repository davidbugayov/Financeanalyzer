package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

/**
 * Абстрактный базовый UseCase для импорта транзакций из файла конкретного банка.
 * Предоставляет общую структуру для парсинга файлов.
 */
abstract class BankImportUseCase(
    protected val transactionRepository: TransactionRepository,
    protected val context: Context,
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
        progressCallback: ImportProgressCallback,
    ): Flow<ImportResult> = flow {
        var importedCount = 0
        var skippedCount = 0
        val importedTransactions = mutableListOf<Transaction>()

        try {
            // Начинаем с 0% прогресса
            emit(ImportResult.progress(0, 100, "Начало обработки файла..."))
            progressCallback.onProgress(0, 100, "Начало обработки файла...")

            // Проверка формата - 5% прогресса
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

            // Прогресс рассчитывается по количеству строк, но если неизвестно — используем шаги по 10 строк
            val estimatedLines = 100 // Можно доработать для динамического определения

            // Выделяем 60% прогресса на чтение и парсинг строк (от 10% до 70%)
            while (line != null) {
                lineNumber++
                // Обновляем прогресс каждые 10 строк
                if (lineNumber % 10 == 0) {
                    val progress = 10 + (lineNumber * 60 / estimatedLines).coerceAtMost(60)
                    emit(ImportResult.progress(progress, 100, "Обработка строки $lineNumber..."))
                    progressCallback.onProgress(
                        progress,
                        100,
                        "Обработка строки $lineNumber из примерно $estimatedLines...",
                    )
                }

                if (shouldSkipLine(line)) {
                    Timber.v("[ИМПОРТ] Пропуск строки $lineNumber для банка $bankName: '$line'")
                } else {
                    try {
                        Timber.d(
                            "[ИМПОРТ] Обработка строки $lineNumber для банка $bankName: '$line'",
                        )
                        val transaction = parseLine(line)
                        if (transaction != null) {
                            Timber.i(
                                "[ИМПОРТ] Создана транзакция из строки $lineNumber: ID=${transaction.id}, Сумма=${transaction.amount}, Дата=${transaction.date}, Категория='${transaction.category}'",
                            )
                            importedTransactions.add(transaction)
                            importedCount++
                        } else {
                            Timber.w("[ИМПОРТ] Не удалось создать транзакцию из строки $lineNumber")
                            skippedCount++
                        }
                    } catch (e: Exception) {
                        Timber.e(
                            e,
                            "[ИМПОРТ] ❌ Ошибка парсинга строки $lineNumber для банка $bankName: '$line'",
                        )
                        skippedCount++
                    }
                }
                line = reader.readLine()
            }

            if (importedTransactions.isNotEmpty()) {
                Timber.i(
                    "[ИМПОРТ] Подготовлено ${importedTransactions.size} транзакций для сохранения в базу данных (банк $bankName)",
                )
                emit(ImportResult.progress(70, 100, "Сохранение транзакций..."))
                progressCallback.onProgress(
                    70,
                    100,
                    "Сохранение ${importedTransactions.size} транзакций...",
                )
                try {
                    Timber.d(
                        "[ИМПОРТ] Начало сохранения ${importedTransactions.size} транзакций в базу данных",
                    )

                    var savedCount = 0
                    val totalTransactions = importedTransactions.size

                    // Выделяем 25% прогресса на сохранение транзакций (от 70% до 95%)
                    importedTransactions.forEachIndexed { index, transaction ->
                        try {
                            // Обновляем прогресс сохранения транзакций
                            val saveProgress = 70 + (index * 25 / totalTransactions.coerceAtLeast(1))
                            if (index % 5 == 0 || index == totalTransactions - 1) {
                                emit(
                                    ImportResult.progress(
                                        saveProgress,
                                        100,
                                        "Сохранение транзакций (${index + 1}/$totalTransactions)...",
                                    ),
                                )
                                progressCallback.onProgress(
                                    saveProgress,
                                    100,
                                    "Сохранение транзакций (${index + 1}/$totalTransactions)...",
                                )
                            }

                            Timber.d(
                                "[ИМПОРТ] Сохранение транзакции: ID=${transaction.id}, Сумма=${transaction.amount}, Дата=${transaction.date}",
                            )
                            Timber.i(
                                "[ИМПОРТ-ОТЛАДКА] ⚠️ ПЕРЕД вызовом transactionRepository.addTransaction для ID=${transaction.id}",
                            )
                            val result = transactionRepository.addTransaction(transaction)
                            Timber.i(
                                "[ИМПОРТ-ОТЛАДКА] ✅ ПОСЛЕ вызова transactionRepository.addTransaction для ID=${transaction.id}, результат=$result",
                            )
                            savedCount++
                            Timber.d(
                                "[ИМПОРТ] Транзакция успешно сохранена ($savedCount/${importedTransactions.size})",
                            )
                        } catch (ex: Exception) {
                            Timber.e(
                                ex,
                                "[ИМПОРТ] ❌ Ошибка при сохранении транзакции ID=${transaction.id}: ${ex.message}",
                            )
                            Timber.e(
                                "[ИМПОРТ-ОТЛАДКА] 🔍 Детали транзакции с ошибкой: ID=${transaction.id}, amount=${transaction.amount}, date=${transaction.date}, category=${transaction.category}, title=${transaction.title}",
                            )
                            Timber.e("[ИМПОРТ-ОТЛАДКА] 🔍 Стек вызовов: ${ex.stackTraceToString()}")
                        }
                    }

                    // Проверяем сколько транзакций было сохранено
                    Timber.i(
                        "[ИМПОРТ] Успешно сохранено $savedCount из ${importedTransactions.size} транзакций для банка $bankName.",
                    )

                    // Финальный прогресс перед успешным завершением - 95%
                    emit(ImportResult.progress(95, 100, "Завершение импорта..."))
                    progressCallback.onProgress(
                        95,
                        100,
                        "Завершение импорта...",
                    )

                    // Дополнительная проверка количества транзакций в базе данных
                    try {
                        Timber.d(
                            "[ИМПОРТ-ПРОВЕРКА] Проверка общего количества транзакций в базе...",
                        )
                        val allTransactions = transactionRepository.getAllTransactions()
                        Timber.i(
                            "[ИМПОРТ-ПРОВЕРКА] Общее количество транзакций в базе после импорта: ${allTransactions.size}",
                        )
                    } catch (e: Exception) {
                        Timber.e(
                            e,
                            "[ИМПОРТ-ПРОВЕРКА] Ошибка при проверке количества транзакций: ${e.message}",
                        )
                    }

                    // Гарантированно эмитируем результат успеха с количеством сохраненных транзакций
                    Timber.i("[ИМПОРТ-ФИНАЛ] ✅ Эмитируем финальный результат: успешно импортировано $savedCount, пропущено $skippedCount, банк: $bankName")

                    // Эмитируем результат дважды для гарантии обработки
                    emit(ImportResult.success(savedCount, skippedCount, bankName = bankName))
                    kotlinx.coroutines.delay(300) // Небольшая задержка
                    Timber.i("[ИМПОРТ-ФИНАЛ] ✅✅ Эмитируем повторный финальный результат для гарантии")
                    emit(ImportResult.success(savedCount, skippedCount, bankName = bankName))
                } catch (e: Exception) {
                    Timber.e(e, "[ИМПОРТ] ❌ Ошибка при сохранении транзакций: ${e.message}")
                    emit(ImportResult.error("Ошибка при сохранении транзакций: ${e.message}"))
                }
            } else {
                Timber.w("[ИМПОРТ] Нет транзакций для импорта")
                // Если нет транзакций для импорта, возвращаем ошибку вместо успеха с 0 транзакциями
                emit(ImportResult.error("Неподдерживаемый формат файла. Пожалуйста, выберите CSV-файл или выписку из поддерживаемого банка."))
            }
        } catch (e: Exception) {
            Timber.e(e, "[ИМПОРТ] ❌ Общая ошибка при импорте: ${e.message}")
            emit(ImportResult.error("Ошибка при импорте: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO) // Ensure Flow emissions happen in the correct context

    /**
     * Реализация основного метода интерфейса ImportTransactionsUseCase.
     * По умолчанию вызывает processTransactionsFromReader.
     * Может быть переопределен для форматов, не работающих с BufferedReader (например, бинарные Excel или PDF, где текст извлекается иначе).
     */
    override fun importTransactions(uri: Uri, progressCallback: ImportProgressCallback): Flow<ImportResult> = flow {
        emit(ImportResult.progress(0, 100, "Начало импорта для банка $bankName..."))
        progressCallback.onProgress(0, 100, "Начало импорта для банка $bankName...")

        try {
            // Важно: мы используем withContext внутри flow, что может привести к проблемам с контекстом
            // Вместо emitAll внутри withContext, мы сначала создадим новый Flow и применим к нему flowOn
            val readerFlow = flow {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        emitAll(processTransactionsFromReader(reader, progressCallback))
                    }
                } ?: run {
                    Timber.e("Не удалось открыть InputStream для URI: $uri в банке $bankName")
                    emit(ImportResult.error("Не удалось открыть файл $uri для банка $bankName"))
                }
            }.flowOn(Dispatchers.IO)

            // Теперь безопасно эмитируем все результаты из readerFlow
            emitAll(readerFlow)
        } catch (e: Exception) {
            Timber.e(
                e,
                "Ошибка при открытии файла или обработке в importTransactions для банка $bankName из $uri: ${e.message}",
            )
            emit(ImportResult.error("Ошибка импорта для $bankName: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO) // Ensure all Flow operations happen in the IO context
}
