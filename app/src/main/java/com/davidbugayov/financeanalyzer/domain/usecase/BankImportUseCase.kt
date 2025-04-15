package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import com.davidbugayov.financeanalyzer.domain.model.Money

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

    // Переменная для хранения последнего прогресса импорта
    private var currentProgress: ImportResult.Progress? = null

    /**
     * Общая реализация импорта, одинаковая для всех банков.
     * Использует шаблонный метод (Template Method) для переопределения специфичной логики.
     */
    override suspend fun invoke(uri: Uri): Flow<ImportResult> = flow {
        try {
            val inputStream = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("Не удалось открыть файл")
            }

            val reader = withContext(Dispatchers.IO) {
                BufferedReader(InputStreamReader(inputStream))
            }

            // Проверяем, что формат файла соответствует ожидаемому для данного банка
            val validFormat = withContext(Dispatchers.IO) {
                isValidFormat(reader)
            }

            if (!validFormat) {
                emit(ImportResult.Error("Формат файла не соответствует формату $bankName"))
                return@flow
            }

            // Заново открываем поток для чтения данных
            withContext(Dispatchers.IO) {
                inputStream.close()
            }

            val dataStream = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("Не удалось открыть файл")
            }

            val dataReader = withContext(Dispatchers.IO) {
                BufferedReader(InputStreamReader(dataStream))
            }

            // Пропускаем заголовок (количество строк зависит от конкретного банка)
            withContext(Dispatchers.IO) {
                skipHeaders(dataReader)
            }

            // Считаем количество строк для отображения прогресса
            val totalLines = withContext(Dispatchers.IO) {
                countTransactionLines(uri)
            }

            var currentLine = 0
            var importedCount = 0
            var skippedCount = 0
            var totalAmount = Money.zero()

            // Читаем и обрабатываем строки с транзакциями
            withContext(Dispatchers.IO) {
                dataReader.useLines { lines ->
                    lines.forEach { line ->
                        if (shouldSkipLine(line)) {
                            return@forEach
                        }

                        currentLine++

                        try {
                            // Парсим строку и получаем транзакцию
                            val transaction = parseLine(line)

                            // Добавляем информацию о банке как источнике
                            val transactionWithSource = transaction.copy(
                                source = bankName
                            )

                            // Сохраняем транзакцию
                            repository.addTransaction(transactionWithSource)

                            importedCount++
                            // Учитываем сумму транзакции в общей сумме
                            totalAmount = if (transactionWithSource.isExpense) 
                                totalAmount - transactionWithSource.amount 
                            else 
                                totalAmount + transactionWithSource.amount
                        } catch (e: Exception) {
                            skippedCount++
                        }

                        // Эмитим прогресс каждые 10 записей
                        if (currentLine % 10 == 0) {
                            // Важно: нельзя эмитить из внутреннего контекста - будет ошибка
                            // Сохраняем данные прогресса для последующего эмита
                            currentProgress = ImportResult.Progress(
                                current = currentLine,
                                total = totalLines,
                                message = "Импортируется $currentLine из $totalLines транзакций из $bankName"
                            )
                        }
                    }
                }
            }

            // Эмитим сохраненный прогресс после withContext
            currentProgress?.let { emit(it) }
            
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
    protected open suspend fun countTransactionLines(uri: Uri): Int = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext 0
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
        count
    }
} 