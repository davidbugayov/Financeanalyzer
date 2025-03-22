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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * Реализация UseCase для импорта транзакций из CSV файла.
 * Поддерживает стандартный формат CSV с заголовками.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class ImportFromCSVUseCase(
    private val repository: TransactionRepositoryImpl,
    private val context: Context
) : ImportTransactionsUseCase {

    /**
     * Импортирует транзакции из CSV файла.
     *
     * @param uri URI CSV файла для импорта
     * @return Flow с результатами импорта для обновления UI
     */
    override suspend fun invoke(uri: Uri): Flow<ImportResult> = flow {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Не удалось открыть файл")

            val reader = BufferedReader(InputStreamReader(inputStream))

            // Читаем заголовки для определения индексов колонок
            val header = reader.readLine()?.split(",")
                ?: throw IllegalArgumentException("Файл пуст или некорректен")

            val dateIndex = header.indexOfFirst { it.contains("date", ignoreCase = true) }
            val amountIndex = header.indexOfFirst { it.contains("amount", ignoreCase = true) }
            val categoryIndex = header.indexOfFirst { it.contains("category", ignoreCase = true) }
            val isExpenseIndex = header.indexOfFirst {
                it.contains(
                    "expense",
                    ignoreCase = true
                ) || it.contains("type", ignoreCase = true)
            }
            val noteIndex = header.indexOfFirst {
                it.contains(
                    "note",
                    ignoreCase = true
                ) || it.contains("description", ignoreCase = true)
            }
            val sourceIndex = header.indexOfFirst { it.contains("source", ignoreCase = true) }

            // Проверяем обязательные поля
            if (dateIndex == -1 || amountIndex == -1) {
                throw IllegalArgumentException("В файле отсутствуют обязательные колонки (дата, сумма)")
            }

            // Считаем количество строк для прогресса
            var lineCount = 0
            reader.use { r ->
                while (r.readLine() != null) {
                    lineCount++
                }
            }

            // Заново открываем поток для чтения данных
            inputStream.close()
            val dataStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Не удалось открыть файл")

            val dataReader = BufferedReader(InputStreamReader(dataStream))
            // Пропускаем заголовок
            dataReader.readLine()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            var currentLine = 0
            var importedCount = 0
            var skippedCount = 0
            var totalAmount = 0.0

            // Читаем и обрабатываем данные
            dataReader.useLines { lines ->
                lines.forEach { line ->
                    currentLine++

                    // Эмитим прогресс каждые 10 записей
                    if (currentLine % 10 == 0) {
                        emit(
                            ImportResult.Progress(
                                current = currentLine,
                                total = lineCount,
                                message = "Импортируется $currentLine из $lineCount транзакций"
                            )
                        )
                    }

                    try {
                        val values = parseCsvLine(line)

                        if (values.size <= maxOf(dateIndex, amountIndex)) {
                            skippedCount++
                            return@forEach
                        }

                        val dateString = values[dateIndex].trim()
                        val amountString = values[amountIndex].trim().replace(",", ".")

                        // Парсим дату
                        val date = try {
                            dateFormat.parse(dateString) ?: Date()
                        } catch (e: Exception) {
                            Date() // Используем текущую дату при ошибке парсинга
                        }

                        // Парсим сумму
                        val amount = try {
                            amountString.toDouble()
                        } catch (e: Exception) {
                            0.0 // Пропускаем при ошибке парсинга суммы
                        }

                        // Определяем тип транзакции (расход/доход)
                        val isExpense = if (isExpenseIndex != -1 && values.size > isExpenseIndex) {
                            val typeValue = values[isExpenseIndex].lowercase()
                            typeValue.contains("expense") || typeValue.contains("расход") ||
                                    typeValue == "true" || typeValue == "1"
                        } else {
                            amount < 0 // Отрицательная сумма = расход
                        }

                        // Определяем категорию
                        val category = if (categoryIndex != -1 && values.size > categoryIndex) {
                            values[categoryIndex]
                        } else {
                            if (isExpense) "Другое" else "Другое"
                        }

                        // Определяем примечание
                        val note = if (noteIndex != -1 && values.size > noteIndex) {
                            values[noteIndex]
                        } else {
                            null
                        }

                        // Определяем источник
                        val source = if (sourceIndex != -1 && values.size > sourceIndex) {
                            values[sourceIndex]
                        } else {
                            "Импорт"
                        }

                        // Создаем объект транзакции
                        val transaction = Transaction(
                            id = "import_${date.time}_${System.nanoTime()}",
                            amount = amount.absoluteValue, // Храним положительное значение
                            category = category,
                            isExpense = isExpense,
                            date = date,
                            note = note,
                            source = source
                        )

                        // Сохраняем транзакцию
                        repository.addTransaction(transaction)

                        importedCount++
                        totalAmount += amount.absoluteValue
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
                    message = "Ошибка импорта: ${e.message}",
                    exception = e
                )
            )
        }
    }

    /**
     * Парсит строку CSV с учетом кавычек.
     *
     * @param line Строка CSV
     * @return Список значений полей
     */
    private fun parseCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        var value = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '\"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    values.add(value.toString())
                    value = StringBuilder()
                }

                else -> value.append(char)
            }
        }

        values.add(value.toString())
        return values
    }
} 