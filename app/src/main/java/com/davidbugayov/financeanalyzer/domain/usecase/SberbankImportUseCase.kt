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
 * Реализация импорта транзакций из выписки Сбербанка.
 * Поддерживает формат выписки из СберБанк Онлайн и мобильного приложения.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class SberbankImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context
) : BankImportUseCase(repository, context) {

    override val bankName: String = "Сбербанк"

    // Паттерны для проверки формата выписки
    private val headerPattern =
        Pattern.compile(".*Дата операции.*Сумма.*Категория.*", Pattern.DOTALL)
    private val datePattern = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

    /**
     * Проверяет, соответствует ли файл формату выписки Сбербанка.
     */
    override fun isValidFormat(reader: BufferedReader): Boolean {
        // Читаем первые 10 строк для поиска характерных признаков формата
        val headerLines = StringBuilder()
        var lineCount = 0
        reader.useLines { lines ->
            lines.take(10).forEach { line ->
                headerLines.append(line)
                headerLines.append("\n")
                lineCount++
            }
        }

        // Проверяем наличие характерных заголовков Сбербанка
        if (headerPattern.matcher(headerLines).matches()) {
            return true
        }

        // Альтернативная проверка: ищем ключевые термины Сбербанка
        return headerLines.contains("Сбербанк") ||
                headerLines.contains("СберБанк") ||
                headerLines.contains("СБЕРБАНК") ||
                headerLines.contains("Выписка по счету") ||
                headerLines.contains("Выписка по карте")
    }

    /**
     * Пропускает заголовки в выписке Сбербанка.
     * Обычно это первые несколько строк, содержащие информацию о клиенте и счете.
     */
    override fun skipHeaders(reader: BufferedReader) {
        var foundTransactionStart = false
        var linesSkipped = 0

        // Пропускаем строки до тех пор, пока не встретим начало транзакций
        // или не достигнем максимального количества строк заголовка
        while (!foundTransactionStart && linesSkipped < 20) {
            val line = reader.readLine() ?: break
            linesSkipped++

            // Проверяем, является ли строка началом списка транзакций
            foundTransactionStart = line.contains("Дата операции") &&
                    (line.contains("Сумма") || line.contains("Категория"))
        }
    }

    /**
     * Парсит строку из выписки Сбербанка в объект Transaction.
     */
    override fun parseLine(line: String): Transaction {
        // Разбиваем строку на поля
        val parts = line.split(";").map { it.trim() }

        // Проверяем, достаточно ли полей
        if (parts.size < 4) {
            throw IllegalArgumentException("Неверный формат строки транзакции")
        }

        // Индексы полей в выписке Сбербанка могут различаться в зависимости от версии
        // Обычно формат: Дата операции;Дата проводки;Номер карты;Операция;Сумма в валюте счёта;Категория;MCC;Описание

        // Определяем индексы основных полей
        val dateIndex = parts.indexOfFirst { isDate(it) }
        val amountIndex = parts.indexOfFirst { isAmount(it) }
        val categoryIndex = parts.indexOfFirst { isCategory(it) }
        val descriptionIndex = if (parts.size > 7) 7 else parts.size - 1

        if (dateIndex == -1 || amountIndex == -1) {
            throw IllegalArgumentException("Не удалось определить основные поля транзакции")
        }

        // Парсим дату
        val date = try {
            datePattern.parse(parts[dateIndex]) ?: Date()
        } catch (e: Exception) {
            Date() // Если не удалось распарсить, используем текущую дату
        }

        // Парсим сумму и определяем тип транзакции
        val amountString = parts[amountIndex].replace("\\s".toRegex(), "").replace(",", ".")
        val cleanAmount = amountString.replace("[^0-9.-]".toRegex(), "")
        val amount = cleanAmount.toDoubleOrNull() ?: 0.0
        val isExpense = amount < 0

        // Определяем категорию
        val category = if (categoryIndex != -1 && categoryIndex < parts.size) {
            mapSberbankCategory(parts[categoryIndex])
        } else {
            if (isExpense) "Другое" else "Доход"
        }

        // Определяем примечание
        val note = if (descriptionIndex < parts.size) {
            parts[descriptionIndex].takeIf { it.isNotBlank() }
        } else {
            null
        }

        return Transaction(
            id = "sber_${date.time}_${System.nanoTime()}",
            amount = amount.absoluteValue,
            category = category,
            isExpense = isExpense,
            date = date,
            note = note,
            source = "Сбер"
        )
    }

    /**
     * Проверяет, пропускать ли строку (игнорировать итоговые суммы, пустые строки и т.д.).
     */
    override fun shouldSkipLine(line: String): Boolean {
        return line.isBlank() ||
                line.contains("Итого по операциям") ||
                line.contains("Итоговая сумма") ||
                line.startsWith("Дата операции") || // Заголовок таблицы
                line.contains("Баланс")
    }

    /**
     * Проверяет, является ли строка датой в формате Сбербанка.
     */
    private fun isDate(str: String): Boolean {
        val dateRegex = "\\d{2}\\.\\d{2}\\.\\d{4}".toRegex()
        return str.matches(dateRegex)
    }

    /**
     * Проверяет, является ли строка суммой в формате Сбербанка.
     */
    private fun isAmount(str: String): Boolean {
        return str.contains(Regex("[0-9]")) &&
                (str.contains(",") || str.contains("\\.") || str.contains("-") || str.contains("+"))
    }

    /**
     * Проверяет, является ли строка категорией в формате Сбербанка.
     */
    private fun isCategory(str: String): Boolean {
        // Стандартные категории Сбербанка не содержат цифр и обычно состоят из 1-3 слов
        return !str.contains(Regex("[0-9]")) && str.split(" ").size <= 3 && str.length < 30
    }

    /**
     * Преобразует категорию Сбербанка в категорию приложения.
     */
    private fun mapSberbankCategory(sberCategory: String): String {
        return when (sberCategory.lowercase()) {
            "супермаркеты" -> "Продукты"
            "рестораны" -> "Рестораны"
            "транспорт" -> "Транспорт"
            "одежда и обувь" -> "Одежда"
            "здоровье и красота" -> "Здоровье"
            "жкх и домашние телефоны" -> "Коммунальные платежи"
            "мобильная связь" -> "Связь"
            "интернет и тв" -> "Интернет"
            "наличные" -> "Наличные"
            "перевод" -> "Переводы"
            "зарплата" -> "Зарплата"
            // Можно добавить другие соответствия
            else -> if (sberCategory.isBlank()) "Другое" else sberCategory
        }
    }
} 