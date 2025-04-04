package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Базовый абстрактный класс для импорта транзакций из выписок различных банков.
 * Определяет общую структуру и функциональность для всех банковских парсеров.
 * Следует принципам OCP (Open-Closed Principle) из SOLID.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
abstract class BankImportUseCase(
    protected val repository: TransactionRepositoryImpl,
    protected val context: Context
) : ImportTransactionsUseCase {

    /**
     * Название банка, используется для идентификации источника в транзакциях
     */
    abstract val bankName: String

    /**
     * Общая реализация импорта, одинаковая для всех банков.
     * Использует шаблонный метод (Template Method) для переопределения специфичной логики.
     */
    override suspend fun invoke(uri: Uri): Flow<ImportResult> = flow {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Не удалось открыть файл")

            val reader = BufferedReader(InputStreamReader(inputStream))

            // Проверяем, что формат файла соответствует ожидаемому для данного банка
            if (!isValidFormat(reader)) {
                emit(ImportResult.Error("Формат файла не соответствует формату $bankName"))
                return@flow
            }

            // Заново открываем поток для чтения данных
            inputStream.close()
            val dataStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Не удалось открыть файл")

            val dataReader = BufferedReader(InputStreamReader(dataStream))

            // Пропускаем заголовок (количество строк зависит от конкретного банка)
            skipHeaders(dataReader)

            // Считаем количество строк для отображения прогресса
            val totalLines = countTransactionLines(uri)

            var currentLine = 0
            var importedCount = 0
            var skippedCount = 0
            var totalAmount = 0.0

            // Читаем и обрабатываем строки с транзакциями
            dataReader.useLines { lines ->
                lines.forEach { line ->
                    if (shouldSkipLine(line)) {
                        return@forEach
                    }

                    currentLine++

                    // Эмитим прогресс каждые 10 записей
                    if (currentLine % 10 == 0) {
                        emit(
                            ImportResult.Progress(
                                current = currentLine,
                                total = totalLines,
                                message = "Импортируется $currentLine из $totalLines транзакций из $bankName"
                            )
                        )
                    }

                    try {
                        // Парсим строку и получаем транзакцию
                        val transaction = parseLine(line)

                        // Добавляем информацию о банке как источнике
                        val transactionWithSource = transaction.copy(
                            source = transaction.source ?: bankName
                        )

                        // Сохраняем транзакцию
                        repository.addTransaction(transactionWithSource)

                        importedCount++
                        totalAmount += transactionWithSource.amount
                    } catch (e: Exception) {
                        skippedCount++
                    }
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
                    message = "Ошибка импорта из $bankName: ${e.message}",
                    exception = e
                )
            )
        }
    }

    /**
     * Проверяет, что формат файла соответствует ожидаемому для данного банка.
     * Переопределяется в конкретных реализациях.
     *
     * @param reader Буферизованный читатель для чтения содержимого файла
     * @return true, если формат соответствует ожидаемому
     */
    abstract fun isValidFormat(reader: BufferedReader): Boolean

    /**
     * Пропускает заголовки в файле выписки.
     * Переопределяется в конкретных реализациях.
     *
     * @param reader Буферизованный читатель для чтения содержимого файла
     */
    protected abstract fun skipHeaders(reader: BufferedReader)

    /**
     * Парсит строку выписки и преобразует ее в объект Transaction.
     * Переопределяется в конкретных реализациях.
     *
     * @param line Строка с данными транзакции
     * @return Объект Transaction
     */
    protected abstract fun parseLine(line: String): Transaction

    /**
     * Проверяет, нужно ли пропустить строку (например, итоговые суммы, разделители).
     * Переопределяется в конкретных реализациях при необходимости.
     *
     * @param line Строка для проверки
     * @return true, если строку нужно пропустить
     */
    protected open fun shouldSkipLine(line: String): Boolean {
        return line.isBlank()
    }

    /**
     * Подсчитывает количество строк с транзакциями для отображения прогресса.
     * Может быть переопределен в конкретных реализациях при необходимости.
     *
     * @param uri URI файла выписки
     * @return Количество строк с транзакциями
     */
    protected open fun countTransactionLines(uri: Uri): Int {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
        val reader = BufferedReader(InputStreamReader(inputStream))

        // Пропускаем заголовки
        val tempReader = reader.also { skipHeaders(it) }

        // Считаем строки с транзакциями
        var count = 0
        tempReader.useLines { lines ->
            lines.forEach { line ->
                if (!shouldSkipLine(line)) {
                    count++
                }
            }
        }

        inputStream.close()
        return count
    }
} 