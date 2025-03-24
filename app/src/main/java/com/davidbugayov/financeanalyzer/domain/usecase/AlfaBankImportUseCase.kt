package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.absoluteValue

/**
 * Реализация импорта транзакций из выписки Альфа-Банка.
 * Поддерживает формат выписки из личного кабинета и мобильного приложения.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class AlfaBankImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context
) : BankImportUseCase(repository, context) {

    override val bankName: String = "Альфа-Банк"

    // Форматы даты, используемые в выписках Альфа-Банка
    private val dateFormats = listOf(
        SimpleDateFormat("dd.MM.yyyy", Locale("ru")),
        SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru")),
        SimpleDateFormat("yyyy-MM-dd", Locale("ru"))
    )

    // Паттерны для проверки формата
    private val headerPattern = Pattern.compile(".*Дата.*Сумма.*Категория.*", Pattern.DOTALL)

    /**
     * Проверяет, соответствует ли файл формату выписки Альфа-Банка.
     */
    override fun isValidFormat(reader: BufferedReader): Boolean {
        val headerLines = StringBuilder()
        reader.useLines { lines ->
            lines.take(10).forEach { line ->
                headerLines.append(line)
                headerLines.append("\n")
            }
        }

        val headerText = headerLines.toString().lowercase()
        return headerText.contains("альфа") ||
                headerText.contains("alfa") ||
                headerText.contains("alfabank") ||
                (headerText.contains("дата") &&
                        headerText.contains("описание") &&
                        headerText.contains("сумма")) ||
                headerPattern.matcher(headerText).matches()
    }

    /**
     * Пропускает заголовки в выписке Альфа-Банка.
     */
    override fun skipHeaders(reader: BufferedReader) {
        var headerFound = false
        var linesSkipped = 0

        while (!headerFound && linesSkipped < 15) {
            val line = reader.readLine() ?: break
            linesSkipped++

            headerFound = line.contains("Дата операции") ||
                    line.contains("Дата,") ||
                    (line.contains("Сумма") &&
                            (line.contains("Описание") || line.contains("Категория")))
        }
    }

    /**
     * Парсит строку из выписки Альфа-Банка в объект Transaction.
     */
    override fun parseLine(line: String): Transaction {
        // Разбиваем строку на поля, учитывая потенциальные разделители
        val parts = if (line.contains(";")) {
            line.split(";")
        } else if (line.contains(",") && line.count { it == ',' } > 1) {
            line.split(",")
        } else {
            line.split("\t")
        }.map { it.trim() }

        if (parts.size < 3) {
            throw IllegalArgumentException("Недостаточно полей для парсинга транзакции")
        }

        // Определяем индексы
        val dateIndex = findDateIndex(parts)
        val amountIndex = findAmountIndex(parts)
        val descriptionIndex = findDescriptionIndex(parts)

        if (dateIndex == -1 || amountIndex == -1) {
            throw IllegalArgumentException("Не удалось определить основные поля")
        }

        // Парсим дату
        val date = parseDate(parts[dateIndex])

        // Парсим сумму и определяем тип транзакции
        val amountString = parts[amountIndex].replace("\\s".toRegex(), "")
            .replace(",", ".").replace("[^\\d.-]".toRegex(), "")
        val amount = amountString.toDoubleOrNull() ?: 0.0
        val isExpense = amount < 0 ||
                parts.any {
                    it.contains("списание", ignoreCase = true) ||
                            it.contains("расход", ignoreCase = true)
                }

        // Определяем категорию из описания
        val description = if (descriptionIndex != -1 && descriptionIndex < parts.size) {
            parts[descriptionIndex]
        } else {
            ""
        }

        val category = inferCategoryFromDescription(description, isExpense)

        return Transaction(
            id = "alfabank_${date.time}_${System.nanoTime()}",
            amount = amount.absoluteValue,
            category = category,
            isExpense = isExpense,
            date = date,
            note = description.takeIf { it.isNotBlank() },
            source = "Альфа-Банк"
        )
    }

    /**
     * Парсит дату из строки в различных форматах.
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
     * Находит индекс поля с датой.
     */
    private fun findDateIndex(parts: List<String>): Int {
        return parts.indexOfFirst { part ->
            part.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}.*")) ||
                    part.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*"))
        }
    }

    /**
     * Находит индекс поля с суммой.
     */
    private fun findAmountIndex(parts: List<String>): Int {
        return parts.indexOfFirst { part ->
            val normalized = part.replace("\\s".toRegex(), "")
            normalized.contains(Regex("-?\\d+[,.][\\d]+")) &&
                    (normalized.contains("₽") || normalized.contains("RUB") ||
                            normalized.contains("руб") || !normalized.contains("[a-zA-Zа-яА-Я]".toRegex()))
        }
    }

    /**
     * Находит индекс поля с описанием операции.
     */
    private fun findDescriptionIndex(parts: List<String>): Int {
        return parts.indexOfFirst { part ->
            part.length > 5 &&
                    !part.contains(Regex("\\d{2}\\.\\d{2}\\.\\d{4}")) && // Не содержит дату
                    !part.contains(Regex("\\d+[,.]\\d+")) && // Не содержит сумму
                    part.isNotBlank()
        }
    }

    /**
     * Определяет категорию на основе описания транзакции.
     */
    private fun inferCategoryFromDescription(description: String, isExpense: Boolean): String {
        val lowercaseDesc = description.lowercase()

        return when {
            lowercaseDesc.contains("перевод") -> "Переводы"
            lowercaseDesc.contains("зарплата") || lowercaseDesc.contains("аванс") -> "Зарплата"
            lowercaseDesc.contains("кафе") || lowercaseDesc.contains("ресторан") -> "Рестораны"
            lowercaseDesc.contains("такси") || lowercaseDesc.contains("метро") ||
                    lowercaseDesc.contains("автобус") -> "Транспорт"

            lowercaseDesc.contains("аптека") || lowercaseDesc.contains("клиника") -> "Здоровье"
            lowercaseDesc.contains("супермаркет") || lowercaseDesc.contains("продукты") ||
                    lowercaseDesc.contains("магазин") -> "Продукты"

            lowercaseDesc.contains("жкх") || lowercaseDesc.contains("коммунал") -> "Коммунальные платежи"
            lowercaseDesc.contains("мтс") || lowercaseDesc.contains("билайн") ||
                    lowercaseDesc.contains("мегафон") -> "Связь"

            lowercaseDesc.contains("одежда") || lowercaseDesc.contains("обувь") -> "Одежда"
            else -> if (isExpense) "Другое" else "Доход"
        }
    }

    /**
     * Проверяет, пропускать ли строку.
     */
    override fun shouldSkipLine(line: String): Boolean {
        return line.isBlank() ||
                line.startsWith("Дата") || // Заголовок таблицы
                line.contains("Итого:") ||
                line.contains("Остаток:") ||
                line.contains("Оборот:")
    }
} 