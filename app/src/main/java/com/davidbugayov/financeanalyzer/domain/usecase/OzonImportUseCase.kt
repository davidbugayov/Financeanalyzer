package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue
import timber.log.Timber

/**
 * Реализация импорта транзакций из выписки Озон Банка.
 * Поддерживает формат выписки из личного кабинета и мобильного приложения Озон.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class OzonImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context
) : BankImportUseCase(repository, context) {

    override val bankName: String = "Озон Банк"

    // Форматы даты, используемые в выписках Озон Банка
    private val dateFormats = listOf(
        SimpleDateFormat("dd.MM.yyyy", Locale("ru")),
        SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru")),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale("ru"))
    )

    /**
     * Проверяет, соответствует ли файл формату выписки Озон Банка.
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
        return headerText.contains("озон") ||
                headerText.contains("ozon") ||
                headerText.contains("интернет-магазин") ||
                (headerText.contains("дата") &&
                        headerText.contains("операция") &&
                        headerText.contains("ozon")) ||
                headerText.contains("ozon.card")
    }

    /**
     * Пропускает заголовки в выписке Озон Банка.
     */
    override fun skipHeaders(reader: BufferedReader) {
        var headerFound = false
        var linesSkipped = 0

        while (!headerFound && linesSkipped < 15) {
            val line = reader.readLine() ?: break
            linesSkipped++

            headerFound = line.contains("Дата операции") ||
                    line.contains("Дата платежа") ||
                    (line.contains("Сумма") && line.contains("Описание"))
        }
    }

    /**
     * Парсит строку из выписки Озон Банка в объект Transaction.
     */
    override fun parseLine(line: String): Transaction {
        // Определяем разделитель
        val delimiter = when {
            line.contains(";") -> ";"
            line.contains("\t") -> "\t"
            else -> ","
        }

        val parts = line.split(delimiter).map { it.trim() }

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
        val amountString = parts[amountIndex].replace("\\s".toRegex(), "")
            .replace(",", ".").replace("[^\\d.-]".toRegex(), "")
        val amount = amountString.toDoubleOrNull() ?: 0.0

        // Определяем тип транзакции (расход/доход)
        val isExpense = amount < 0 ||
                parts.any {
                    it.contains("списание", ignoreCase = true) ||
                            it.contains("покупка", ignoreCase = true)
                }

        // Получаем описание транзакции
        val description = if (descriptionIndex != -1 && descriptionIndex < parts.size) {
            parts[descriptionIndex]
        } else {
            ""
        }

        // Определяем категорию на основе описания
        val category = inferCategoryFromDescription(description, isExpense)

        return Transaction(
            id = "ozon_${date.time}_${System.nanoTime()}",
            amount = amount.absoluteValue,
            category = category,
            isExpense = isExpense,
            date = date,
            note = description.takeIf { it.isNotBlank() },
            source = "Озон Банк"
        )
    }

    /**
     * Находит индекс поля с датой.
     */
    private fun findDateFieldIndex(parts: List<String>): Int {
        return parts.indexOfFirst { part ->
            part.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}.*")) ||
                    part.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*"))
        }
    }

    /**
     * Находит индекс поля с суммой.
     */
    private fun findAmountFieldIndex(parts: List<String>): Int {
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
    private fun findDescriptionFieldIndex(parts: List<String>): Int {
        return parts.indexOfFirst { part ->
            part.length > 5 &&
                    !part.contains(Regex("\\d{2}\\.\\d{2}\\.\\d{4}")) && // Не содержит дату
                    !part.contains(Regex("\\d+[,.]\\d+")) && // Не содержит сумму
                    part.isNotBlank()
        }
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
                line.contains("Всего:")
    }

    /**
     * Определяет категорию на основе описания транзакции.
     */
    private fun inferCategoryFromDescription(description: String, isExpense: Boolean): String {
        val lowercaseDesc = description.lowercase()

        return when {
            // Озон-специфичные категории
            lowercaseDesc.contains("ozon") && lowercaseDesc.contains("заказ") -> "Покупки"
            lowercaseDesc.contains("озон") && lowercaseDesc.contains("заказ") -> "Покупки"
            lowercaseDesc.contains("ozon") && !lowercaseDesc.contains("пополнение") -> "Покупки"
            lowercaseDesc.contains("ozon") && lowercaseDesc.contains("travel") -> "Путешествия"

            // Общие категории
            lowercaseDesc.contains("перевод") -> "Переводы"
            lowercaseDesc.contains("зарплата") || lowercaseDesc.contains("аванс") -> "Зарплата"
            lowercaseDesc.contains("кафе") || lowercaseDesc.contains("ресторан") -> "Рестораны"
            lowercaseDesc.contains("такси") || lowercaseDesc.contains("метро") -> "Транспорт"
            lowercaseDesc.contains("аптека") || lowercaseDesc.contains("клиника") -> "Здоровье"
            lowercaseDesc.contains("супермаркет") || lowercaseDesc.contains("продукты") -> "Продукты"
            lowercaseDesc.contains("жкх") || lowercaseDesc.contains("коммунал") -> "Коммунальные платежи"
            lowercaseDesc.contains("мтс") || lowercaseDesc.contains("связь") -> "Связь"
            lowercaseDesc.contains("одежда") || lowercaseDesc.contains("обувь") -> "Одежда"
            lowercaseDesc.contains("кэшбэк") || lowercaseDesc.contains("cashback") -> "Кэшбэк"
            lowercaseDesc.contains("пополнение") -> "Пополнение"
            else -> if (isExpense) "Другое" else "Доход"
        }
    }
} 