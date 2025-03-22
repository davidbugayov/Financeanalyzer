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
 * Реализация импорта транзакций из ВТБ банка
 * Поддерживает формат выписки из интернет-банка и мобильного приложения ВТБ
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class VTBImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context
) : BankImportUseCase(repository, context) {

    override val bankName: String = "ВТБ"

    // Форматы даты, используемые в выписках ВТБ
    private val dateFormats = listOf(
        SimpleDateFormat("dd.MM.yyyy", Locale("ru")),
        SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru")),
        SimpleDateFormat("yyyy-MM-dd", Locale("ru"))
    )

    // Паттерн заголовка выписки ВТБ
    private val headerPattern =
        ".*\\b(втб|vtb|выписка втб|vtb statement)\\b.*".toRegex(RegexOption.IGNORE_CASE)

    /**
     * Проверяет, соответствует ли файл формату выписки ВТБ
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
        return headerText.contains("втб") ||
                headerText.contains("vtb") ||
                headerText.contains("дата операции") && headerText.contains("сумма") &&
                (headerText.contains("счет") || headerText.contains("карта"))
    }

    /**
     * Пропускает заголовки в выписке ВТБ
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
                            (line.contains("Описание") || line.contains("Назначение")))
        }
    }

    /**
     * Парсит строку из выписки ВТБ в объект Transaction
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
                .takeIf { it.isNotBlank() } ?: "Транзакция ВТБ"
        }

        // Определяем категорию на основе описания
        val category = inferCategoryFromDescription(description, isExpense)

        return Transaction(
            id = "vtb_${date.time}_${System.nanoTime()}",
            amount = amount.absoluteValue,
            category = category,
            isExpense = isExpense,
            date = date,
            note = description.takeIf { it.isNotBlank() },
            source = "ВТБ"
        )
    }

    /**
     * Находит индекс поля с датой
     */
    private fun findDateFieldIndex(parts: List<String>): Int {
        return parts.indexOfFirst { part ->
            part.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}.*")) ||
                    part.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*"))
        }
    }

    /**
     * Находит индекс поля с суммой
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
     * Находит индекс поля с описанием транзакции
     */
    private fun findDescriptionFieldIndex(parts: List<String>): Int {
        val keywords = listOf(
            "описание",
            "назначение",
            "детали",
            "примечание",
            "информация",
            "детали операции"
        )

        // Ищем по заголовку колонки
        for ((index, part) in parts.withIndex()) {
            val normalized = part.lowercase()
            if (keywords.any { normalized.contains(it) }) {
                return index
            }
        }

        // Если ничего не нашли, выбираем самую длинную строку (вероятно, это описание)
        return parts.withIndex()
            .filter { (index, _) ->
                index != findDateFieldIndex(parts) && index != findAmountFieldIndex(
                    parts
                )
            }
            .maxByOrNull { (_, part) -> part.length }
            ?.index ?: -1
    }

    /**
     * Парсит дату из строки в нескольких форматах
     */
    private fun parseDate(dateString: String): Date {
        for (format in dateFormats) {
            try {
                return format.parse(dateString) ?: continue
            } catch (e: Exception) {
                continue
            }
        }

        // Если не удалось распарсить дату, возвращаем текущую
        return Date()
    }

    /**
     * Определяет, нужно ли пропустить данную строку
     */
    override fun shouldSkipLine(line: String): Boolean {
        val lowerCase = line.lowercase()
        return lowerCase.isBlank() ||
                lowerCase.startsWith("итого") ||
                lowerCase.contains("остаток") ||
                lowerCase.contains("входящий") ||
                lowerCase.contains("исходящий") ||
                lowerCase.contains("баланс") ||
                lowerCase == "дата" ||
                lowerCase.startsWith("выписка сформирована")
    }

    /**
     * Определяет категорию на основе описания транзакции
     */
    private fun inferCategoryFromDescription(description: String, isExpense: Boolean): String {
        val lowerDesc = description.lowercase()

        return when {
            // Расходы
            isExpense -> when {
                lowerDesc.contains("аренда") ||
                        lowerDesc.contains("квартира") ||
                        lowerDesc.contains("жкх") ||
                        lowerDesc.contains("коммунальн") -> "Жилье"

                lowerDesc.contains("перевод") ||
                        lowerDesc.contains("пополнение") -> "Переводы"

                lowerDesc.contains("ресторан") ||
                        lowerDesc.contains("кафе") ||
                        lowerDesc.contains("бар") ||
                        lowerDesc.contains("пицц") ||
                        lowerDesc.contains("суши") ||
                        lowerDesc.contains("доставка еды") -> "Рестораны"

                lowerDesc.contains("супермаркет") ||
                        lowerDesc.contains("продукт") ||
                        lowerDesc.contains("магазин") ||
                        lowerDesc.contains("пятерочка") ||
                        lowerDesc.contains("магнит") ||
                        lowerDesc.contains("перекресток") ||
                        lowerDesc.contains("лента") ||
                        lowerDesc.contains("ашан") -> "Продукты"

                lowerDesc.contains("аптек") ||
                        lowerDesc.contains("лекарств") ||
                        lowerDesc.contains("доктор") ||
                        lowerDesc.contains("медицин") ||
                        lowerDesc.contains("клиник") -> "Здоровье"

                lowerDesc.contains("такси") ||
                        lowerDesc.contains("метро") ||
                        lowerDesc.contains("автобус") ||
                        lowerDesc.contains("транспорт") -> "Транспорт"

                lowerDesc.contains("билет") ||
                        lowerDesc.contains("кино") ||
                        lowerDesc.contains("театр") ||
                        lowerDesc.contains("концерт") ||
                        lowerDesc.contains("музей") -> "Развлечения"

                else -> "Другое"
            }

            // Доходы
            else -> when {
                lowerDesc.contains("зарплата") ||
                        lowerDesc.contains("аванс") ||
                        lowerDesc.contains("зп ") -> "Зарплата"

                lowerDesc.contains("кэшбэк") ||
                        lowerDesc.contains("кэшбек") ||
                        lowerDesc.contains("вознаграждение") ||
                        lowerDesc.contains("бонус") -> "Кэшбэк"

                lowerDesc.contains("проценты") ||
                        lowerDesc.contains("капитализация") -> "Проценты"

                lowerDesc.contains("перевод") ||
                        lowerDesc.contains("пополнение") -> "Переводы"

                else -> "Другой доход"
            }
        }
    }
} 