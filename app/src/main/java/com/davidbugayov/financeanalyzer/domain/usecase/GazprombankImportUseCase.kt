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
 * Реализация импорта транзакций из выписки Газпромбанка.
 * Поддерживает формат выписки из интернет-банка и мобильного приложения Газпромбанка.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class GazprombankImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context
) : BankImportUseCase(repository, context) {

    override val bankName: String = "Газпромбанк"

    // Форматы даты, используемые в выписках Газпромбанка
    private val dateFormats = listOf(
        SimpleDateFormat("dd.MM.yyyy", Locale("ru")),
        SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru")),
        SimpleDateFormat("yyyy-MM-dd", Locale("ru")),
        SimpleDateFormat("dd.MM.yy", Locale("ru"))
    )

    // Паттерн заголовка выписки Газпромбанка
    private val headerPattern =
        ".*\\b(газпромбанк|gazprombank|ГПБ|GPB)\\b.*".toRegex(RegexOption.IGNORE_CASE)

    /**
     * Проверяет, соответствует ли файл формату выписки Газпромбанка.
     */
    override fun isValidFormat(reader: BufferedReader): Boolean {
        val headerLines = StringBuilder()
        reader.useLines { lines ->
            lines.take(15).forEach { line ->
                headerLines.append(line)
                headerLines.append("\n")
            }
        }

        val headerText = headerLines.toString().lowercase()
        return headerText.contains("газпромбанк") ||
                headerText.contains("gazprombank") ||
                headerText.contains("гпб") ||
                headerText.contains("gpb") ||
                (headerText.contains("дата") && headerText.contains("сумма") &&
                        headerText.contains("операция"))
    }

    /**
     * Пропускает заголовки в выписке Газпромбанка.
     */
    override fun skipHeaders(reader: BufferedReader) {
        var headerFound = false
        var line: String?
        var linesSkipped = 0

        while (!headerFound && linesSkipped < 20) {
            line = reader.readLine()
            if (line == null) break

            linesSkipped++

            // Ищем строку таблицы с заголовками колонок
            headerFound = line.contains("Дата операции") ||
                    (line.contains("Дата") && line.contains("Сумма") &&
                            (line.contains("Описание") || line.contains("Операция")))
        }
    }

    /**
     * Парсит строку из выписки Газпромбанка в объект Transaction.
     */
    override fun parseLine(line: String): Transaction {
        // Определяем разделитель
        val delimiter = when {
            line.contains(";") -> ";"
            line.contains("\t") -> "\t"
            else -> ","
        }

        val parts = line.split(delimiter)
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (parts.size < 3) {
            throw IllegalArgumentException("Недостаточно полей в строке")
        }

        // Определяем индексы полей
        val dateIndex = findDateFieldIndex(parts)
        val amountIndex = findAmountFieldIndex(parts)
        val descriptionIndex = findDescriptionFieldIndex(parts)

        if (dateIndex == -1 || amountIndex == -1) {
            throw IllegalArgumentException("Не удалось определить основные поля")
        }

        // Парсим дату
        val date = parseDate(parts[dateIndex])

        // Парсим сумму
        val amountString = parts[amountIndex]
            .replace("\\s".toRegex(), "")
            .replace(",", ".")
            .replace("[^\\d.-]".toRegex(), "")

        val amount = amountString.toDoubleOrNull() ?: 0.0

        // Определяем тип транзакции (расход/доход)
        val isExpense = amount < 0 ||
                parts.any {
                    it.contains("списание", ignoreCase = true) ||
                            it.contains("расход", ignoreCase = true)
                }

        // Получаем описание транзакции
        val description = if (descriptionIndex != -1 && descriptionIndex < parts.size) {
            parts[descriptionIndex]
        } else {
            parts.filter { it != parts[dateIndex] && it != parts[amountIndex] }
                .joinToString(" ")
                .takeIf { it.isNotBlank() } ?: "Транзакция Газпромбанк"
        }

        // Определяем категорию на основе описания
        val category = inferCategoryFromDescription(description, isExpense)

        return Transaction(
            id = "gpb_${date.time}_${System.nanoTime()}",
            amount = amount.absoluteValue,
            category = category,
            isExpense = isExpense,
            date = date,
            note = description.takeIf { it.isNotBlank() },
            source = "Газпромбанк"
        )
    }

    /**
     * Находит индекс поля с датой.
     */
    private fun findDateFieldIndex(parts: List<String>): Int {
        for (i in parts.indices) {
            val part = parts[i]
            if (part.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}.*")) ||
                part.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{2}.*")) ||
                part.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*"))
            ) {
                return i
            }
        }
        return -1
    }

    /**
     * Находит индекс поля с суммой.
     */
    private fun findAmountFieldIndex(parts: List<String>): Int {
        for (i in parts.indices) {
            val part = parts[i]
            val normalized = part.replace("\\s".toRegex(), "")
            if (normalized.contains(Regex("-?\\d+[,.][\\d]+")) &&
                (normalized.contains("₽") || normalized.contains("RUB") ||
                        normalized.contains("руб") || !normalized.contains("[a-zA-Zа-яА-Я]".toRegex()))
            ) {
                return i
            }
        }
        return -1
    }

    /**
     * Находит индекс поля с описанием операции.
     */
    private fun findDescriptionFieldIndex(parts: List<String>): Int {
        // Сначала ищем по ключевым словам
        val keywords = listOf("описание", "операция", "назначение", "детали")
        for (i in parts.indices) {
            if (parts[i].lowercase().let { part -> keywords.any { part.contains(it) } }) {
                return if (i + 1 < parts.size) i + 1 else i
            }
        }

        // Если по ключевым словам не нашли, ищем самое длинное поле
        return parts.withIndex()
            .filter { (i, _) -> i != findDateFieldIndex(parts) && i != findAmountFieldIndex(parts) }
            .maxByOrNull { (_, value) -> value.length }
            ?.index ?: -1
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
     * Проверяет, пропускать ли строку.
     */
    override fun shouldSkipLine(line: String): Boolean {
        return line.isBlank() ||
                line.startsWith("Дата") || // Заголовок таблицы
                line.contains("Итого:") ||
                line.contains("Баланс:") ||
                line.contains("Остаток:") ||
                line.contains("Всего:") ||
                line.contains("Комиссия:") ||
                line.contains("По счету")
    }

    /**
     * Определяет категорию на основе описания транзакции.
     */
    private fun inferCategoryFromDescription(description: String, isExpense: Boolean): String {
        val lowercaseDesc = description.lowercase()

        return when {
            // Газпромбанк-специфичные категории
            lowercaseDesc.contains("зачисление заработной платы") ||
                    lowercaseDesc.contains("зарплата") -> "Зарплата"

            lowercaseDesc.contains("снятие в банкомате") ||
                    lowercaseDesc.contains("снятие наличных") -> "Наличные"

            // Общие категории
            lowercaseDesc.contains("перевод") || lowercaseDesc.contains("card2card") -> "Переводы"
            lowercaseDesc.contains("аванс") -> "Зарплата"
            lowercaseDesc.contains("кафе") || lowercaseDesc.contains("ресторан") ||
                    lowercaseDesc.contains("кофейня") -> "Рестораны"

            lowercaseDesc.contains("такси") || lowercaseDesc.contains("метро") ||
                    lowercaseDesc.contains("автобус") -> "Транспорт"

            lowercaseDesc.contains("аптека") || lowercaseDesc.contains("клиника") ||
                    lowercaseDesc.contains("больниц") -> "Здоровье"

            lowercaseDesc.contains("супермаркет") || lowercaseDesc.contains("продукты") ||
                    lowercaseDesc.contains("магазин") || lowercaseDesc.contains("market") -> "Продукты"

            lowercaseDesc.contains("жкх") || lowercaseDesc.contains("коммунал") -> "Коммунальные платежи"
            lowercaseDesc.contains("связь") || lowercaseDesc.contains("телеком") ||
                    lowercaseDesc.contains("мобильная") -> "Связь"

            lowercaseDesc.contains("одежда") || lowercaseDesc.contains("обувь") -> "Одежда"
            lowercaseDesc.contains("кэшбэк") || lowercaseDesc.contains("cashback") -> "Кэшбэк"
            else -> if (isExpense) "Другое" else "Доход"
        }
    }
} 