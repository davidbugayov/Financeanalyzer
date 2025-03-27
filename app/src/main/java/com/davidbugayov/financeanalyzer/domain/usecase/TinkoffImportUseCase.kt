package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * Реализация импорта транзакций из выписки Тинькофф банка.
 * Поддерживает формат CSV выписки из личного кабинета и мобильного приложения.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class TinkoffImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context
) : BankImportUseCase(repository, context) {

    override val bankName: String = "Тинькофф"

    // Форматы даты, используемые в выписках Тинькофф
    private val dateFormats = listOf(
        SimpleDateFormat("dd.MM.yyyy", Locale("ru")),
        SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru")),
        SimpleDateFormat("yyyy-MM-dd", Locale("ru"))
    )

    /**
     * Проверяет, соответствует ли файл формату выписки Тинькофф.
     */
    override fun isValidFormat(reader: BufferedReader): Boolean {
        // Читаем первые 5 строк для поиска характерных признаков формата
        val headerLines = StringBuilder()
        reader.useLines { lines ->
            lines.take(5).forEach { line ->
                headerLines.append(line)
                headerLines.append("\n")
            }
        }

        // Проверяем наличие характерных заголовков Тинькофф
        val headerText = headerLines.toString().lowercase()
        
        // Более строгая проверка формата Тинькофф
        return headerText.contains("tinkoff") ||
                headerText.contains("тинькофф") ||
                // Специфичные для Тинькофф строки в заголовке
                headerText.contains("номер карты") ||
                headerText.contains("дата операции") && headerText.contains("номер карты") ||
                // Более строгая комбинация полей, характерная именно для Тинькофф
                (headerText.contains("дата операции") &&
                        headerText.contains("категория") &&
                        headerText.contains("mcc") &&
                        headerText.contains("сумма в валюте"))
    }

    /**
     * Пропускает заголовки в выписке Тинькофф.
     */
    override fun skipHeaders(reader: BufferedReader) {
        var headerFound = false
        var linesSkipped = 0

        // Пропускаем строки до тех пор, пока не встретим заголовок таблицы
        // или не достигнем максимального количества строк заголовка
        while (!headerFound && linesSkipped < 10) {
            val line = reader.readLine() ?: break
            linesSkipped++

            // Проверяем, является ли строка заголовком таблицы
            headerFound = line.contains("Дата операции") ||
                    line.contains("Дата,") ||
                    (line.contains("Категория") && line.contains("Сумма"))
        }
    }

    /**
     * Парсит строку из выписки Тинькофф в объект Transaction.
     */
    override fun parseLine(line: String): Transaction {
        // Разбиваем строку на поля, учитывая кавычки
        val parts = parseCsvLine(line)

        // Проверяем, достаточно ли полей
        if (parts.size < 3) {
            throw IllegalArgumentException("Неверный формат строки транзакции")
        }

        // Определяем индексы полей на основе содержимого
        val dateIndex = findDateFieldIndex(parts)
        val amountIndex = findAmountFieldIndex(parts)
        val categoryIndex = findCategoryFieldIndex(parts)
        val descriptionIndex = findDescriptionFieldIndex(parts)

        if (dateIndex == -1 || amountIndex == -1) {
            throw IllegalArgumentException("Не удалось определить основные поля транзакции")
        }

        // Парсим дату
        val date = parseDate(parts[dateIndex])

        // Парсим сумму
        val amountString = parts[amountIndex].replace("\\s".toRegex(), "").replace(",", ".")
        val cleanAmount = amountString.replace("[^0-9.-]".toRegex(), "")
        val amount = cleanAmount.toDoubleOrNull() ?: 0.0
        val isExpense = amount < 0 || parts.any { it.contains("списание", ignoreCase = true) }

        // Определяем категорию
        val category = if (categoryIndex != -1 && parts.size > categoryIndex) {
            mapTinkoffCategory(parts[categoryIndex])
        } else {
            if (isExpense) "Другое" else "Доход"
        }

        // Определяем примечание/описание
        val note = if (descriptionIndex != -1 && parts.size > descriptionIndex) {
            parts[descriptionIndex].takeIf { it.isNotBlank() }
        } else {
            null
        }

        return Transaction(
            id = "tinkoff_${date.time}_${System.nanoTime()}",
            amount = amount.absoluteValue,
            category = category,
            isExpense = isExpense,
            date = date,
            note = note,
            source = "Тинькофф"
        )
    }

    /**
     * Находит индекс поля с датой.
     */
    private fun findDateFieldIndex(parts: List<String>): Int {
        return parts.indexOfFirst { part ->
            part.contains(Regex("\\d{2}\\.\\d{2}\\.\\d{4}")) ||
                    part.contains(Regex("\\d{4}-\\d{2}-\\d{2}"))
        }
    }

    /**
     * Находит индекс поля с суммой.
     */
    private fun findAmountFieldIndex(parts: List<String>): Int {
        return parts.indexOfFirst { part ->
            part.contains(Regex("-?\\d+[,.]\\d+")) &&
                    (part.contains("₽") || part.contains("руб") || part.endsWith("RUB"))
        }
    }

    /**
     * Находит индекс поля с категорией.
     */
    private fun findCategoryFieldIndex(parts: List<String>): Int {
        // В Тинькофф категория обычно отдельное поле с простым текстом без цифр
        return parts.indexOfFirst { part ->
            part.length < 30 && // Категории обычно короткие
                    !part.contains(Regex("\\d{2}\\.\\d{2}")) && // Не содержит даты
                    !part.contains(Regex("\\d+[,.]\\d+")) && // Не содержит суммы
                    part.isNotBlank() &&
                    !part.contains("₽") && !part.contains("руб")
        }
    }

    /**
     * Находит индекс поля с описанием.
     */
    private fun findDescriptionFieldIndex(parts: List<String>): Int {
        // Обычно это самое длинное текстовое поле
        return parts.indexOfFirst { part ->
            part.length > 20 &&
                    !part.contains(Regex("\\d{2}\\.\\d{2}\\.\\d{4}")) && // Не содержит полной даты
                    !part.contains(Regex("\\d+[,.]\\d+")) && // Не содержит суммы с разделителем
                    part.isNotBlank()
        }
    }

    /**
     * Парсит дату из строки в различных форматах Тинькофф.
     */
    private fun parseDate(dateString: String): Date {
        for (format in dateFormats) {
            try {
                return format.parse(dateString) ?: continue
            } catch (e: Exception) {
                // Пробуем следующий формат
            }
        }
        return Date() // Если не удалось распарсить, возвращаем текущую дату
    }

    /**
     * Разбивает строку CSV с учетом кавычек.
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

    /**
     * Проверяет, пропускать ли строку.
     */
    override fun shouldSkipLine(line: String): Boolean {
        return line.isBlank() ||
                line.startsWith("Дата") || // Заголовок таблицы
                line.contains("Итого:") ||
                line.contains("Входящий остаток:") ||
                line.contains("Исходящий остаток:")
    }

    /**
     * Преобразует категорию Тинькофф в категорию приложения.
     */
    private fun mapTinkoffCategory(tinkoffCategory: String): String {
        return when (tinkoffCategory.lowercase()) {
            "супермаркеты" -> "Продукты"
            "рестораны" -> "Рестораны"
            "кафе" -> "Рестораны"
            "транспорт" -> "Транспорт"
            "такси" -> "Транспорт"
            "одежда и обувь" -> "Одежда"
            "здоровье" -> "Здоровье"
            "красота" -> "Здоровье"
            "жкх" -> "Коммунальные платежи"
            "мобильная связь" -> "Связь"
            "интернет" -> "Интернет"
            "денежные переводы" -> "Переводы"
            "пополнение" -> "Доход"
            "зарплата" -> "Зарплата"
            // Можно добавить другие соответствия
            else -> if (tinkoffCategory.isBlank()) "Другое" else tinkoffCategory
        }
    }
} 