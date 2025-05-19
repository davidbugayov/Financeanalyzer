package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.sberbank

import android.content.Context
import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.domain.model.ImportProgressCallback
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.AbstractPdfImportUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.TransactionCategoryDetector
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Класс для импорта транзакций из PDF-выписок Сбербанка
 */
class SberbankPdfImportUseCase(
    context: Context,
    transactionRepository: TransactionRepository
) : AbstractPdfImportUseCase(context, transactionRepository) {

    override val bankName: String = "Сбербанк (PDF)"
    private val transactionSource: String = "Сбер"

    /**
     * Модель для обработки многострочных транзакций
     */
    private data class PartialTransaction(
        var date: String? = null,
        var time: String? = null,
        var authCode: String? = null,
        var category: String? = null,
        var description: MutableList<String> = mutableListOf(),
        var amount: String? = null,
        var balance: String? = null,
        var isComplete: Boolean = false
    ) {
        fun isValid(): Boolean = date != null && amount != null
    }

    // Шаблоны для анализа строк
    private val mainLineRegex =
        Regex("^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+(\\d{2}:\\d{2})\\s+(\\d+)\\s+(.+?)\\s+([+-]?[\\d\\s.,]+[\\d])\\s+([\\d\\s.,]+[\\d])$")
    private val dateTimeRegex = Regex("^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+(\\d{2}:\\d{2}).*$")
    private val descriptionLineRegex = Regex("^\\d{2}\\.\\d{2}\\.\\d{4}\\s+(.+)$")
    private val cardNumberRegex = Regex("^(?:карте|карты)\\s+\\*{4}(\\d{4})$", RegexOption.IGNORE_CASE)

    override fun isValidFormat(reader: BufferedReader): Boolean {
        val headerLines = mutableListOf<String>()
        reader.mark(8192)
        repeat(25) {
            val line = reader.readLine()?.replace("\u0000", "")
            if (line != null) headerLines.add(line) else return@repeat
        }
        reader.reset()
        val textSample = headerLines.joinToString(separator = "\n")
        val hasBankIndicator = textSample.contains("СБЕР", ignoreCase = true) || textSample.contains("Сбербанк", ignoreCase = true)
        val hasStatementTitle =
            textSample.contains("Выписка по счёту", ignoreCase = true) || textSample.contains("Выписка по счету", ignoreCase = true)
        val hasTableMarker = headerLines.any { it.contains("Расшифровка операций", ignoreCase = true) } ||
                headerLines.any { it.contains("ДАТА ОПЕРАЦИИ", ignoreCase = true) && it.contains("КАТЕГОРИЯ", ignoreCase = true) }
        return hasBankIndicator && hasStatementTitle && hasTableMarker
    }

    override fun skipHeaders(reader: BufferedReader) {
        var line: String?
        var transcriptFound = false
        while (true) {
            line = reader.readLine()?.replace("\u0000", "")
            if (line == null) return
            if (line.contains("Расшифровка операций", ignoreCase = true)) {
                transcriptFound = true
                break
            }
        }
        while (true) {
            reader.mark(1024)
            val nextLine = reader.readLine()?.replace("\u0000", "")
            reader.reset()
            if (nextLine == null) break
            if (nextLine.trim().matches(Regex("^\\d{2}\\.\\d{2}\\.\\d{4}.*"))) break
            else reader.readLine()
        }
    }

    override fun shouldSkipLine(line: String): Boolean {
        if (line.isBlank()) return true
        val tableHeaders = setOf(
            "ДАТА ОПЕРАЦИИ (МСК)", "Дата обработки¹ и код авторизации", "КАТЕГОРИЯ", "Описание операции",
            "СУММА В ВАЛЮТЕ СЧЁТА", "Сумма в валюте", "операции²", "ОСТАТОК СРЕДСТВ", "В ВАЛЮТЕ СЧЁТА"
        )
        if (tableHeaders.any { line.equals(it, ignoreCase = true) }) return true
        val footerOrPageLinesPatterns = listOf(
            Regex("^Выписка по счёту дебетовой карты Страница \\d+ из \\d+$", RegexOption.IGNORE_CASE),
            Regex("^Продолжение на следующей странице$", RegexOption.IGNORE_CASE),
            Regex("^Дата формирования \\d{2}\\.\\d{2}\\.\\d{4}$", RegexOption.IGNORE_CASE),
            Regex("^ПАО Сбербанк\\. Генеральная лицензия", RegexOption.IGNORE_CASE),
            Regex("^Денежные средства списываются", RegexOption.IGNORE_CASE),
            Regex("^отображаются только обработанные", RegexOption.IGNORE_CASE),
            Regex("^до 30 дней\\.$", RegexOption.IGNORE_CASE),
            Regex("^\\d$"),
            Regex("^Дата списания / зачисления денежных средств на счёт карты$", RegexOption.IGNORE_CASE),
            Regex("^По курсу банка на дату обработки операции$", RegexOption.IGNORE_CASE),
            Regex("^Дергунова К\\. А\\.$", RegexOption.IGNORE_CASE),
            Regex("^Управляющий директор Дивизиона «Забота о клиентах»$", RegexOption.IGNORE_CASE)
        )
        return footerOrPageLinesPatterns.any { line.matches(it) }
    }

    override fun parseTransactions(
        reader: BufferedReader,
        progressCallback: ImportProgressCallback,
        rawText: String
    ): List<Transaction> {
        val importedTransactions = mutableListOf<Transaction>()
        var line: String?
        var currentTransaction = PartialTransaction()
        var lineNumber = 0
        line = reader.readLine()
        while (line != null) {
            lineNumber++
            if (!shouldSkipLine(line)) {
                val mainMatch = mainLineRegex.find(line.trim())
                if (mainMatch != null) {
                    if (currentTransaction.isValid()) {
                        val transaction = createTransactionFromPartial(currentTransaction)
                        if (transaction != null) importedTransactions.add(transaction)
                    }
                    val dateStr = mainMatch.groupValues[1]
                    val timeStr = mainMatch.groupValues[2]
                    val authCode = mainMatch.groupValues[3]
                    val category = mainMatch.groupValues[4].trim()
                    val amountStr = mainMatch.groupValues[5].trim()
                    val balanceStr = mainMatch.groupValues[6].trim()
                    currentTransaction = PartialTransaction(
                        date = dateStr,
                        time = timeStr,
                        authCode = authCode,
                        category = category,
                        amount = amountStr,
                        balance = balanceStr
                    )
                    val descMatch = descriptionLineRegex.find(line)
                    if (descMatch != null) currentTransaction.description.add(descMatch.groupValues[1])
                    else {
                        val cardMatch = cardNumberRegex.find(line)
                        if (cardMatch != null) currentTransaction.description.add(line)
                        else if (!dateTimeRegex.matches(line)) currentTransaction.description.add(line)
                    }
                }
            }
            line = reader.readLine()
        }
        if (currentTransaction.isValid()) {
            val transaction = createTransactionFromPartial(currentTransaction)
            if (transaction != null) importedTransactions.add(transaction)
        }
        return importedTransactions
    }

    private fun createTransactionFromPartial(partial: PartialTransaction): Transaction? {
        try {
            if (!partial.isValid()) return null
            val transactionDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(partial.date!!) ?: Date()
            val cleanedAmount = partial.amount!!.replace("\\s".toRegex(), "").replace(',', '.')
            val amount = cleanedAmount.toDoubleOrNull() ?: return null
            val isExpense: Boolean = when {
                cleanedAmount.startsWith("-") -> true
                cleanedAmount.startsWith("+") -> false
                else -> !(partial.category?.contains("внесение наличных", ignoreCase = true) == true ||
                        partial.category?.contains("перевод", ignoreCase = true) == true)
            }
            val absAmount = kotlin.math.abs(amount)
            val money = Money(absAmount, Currency.RUB)
            val noteParts = mutableListOf<String>()
            if (partial.time != null) noteParts.add("Время: ${partial.time}")
            if (partial.authCode != null) noteParts.add("Код: ${partial.authCode}")
            if (partial.balance != null) noteParts.add("Баланс: ${partial.balance}")
            if (partial.description.isNotEmpty()) noteParts.add("Детали: ${partial.description.joinToString(" ")}")
            val title = partial.category ?: "Неизвестная операция"

            // Используем банковскую категорию напрямую, если она соответствует нашим категориям
            val bankCategory = partial.category ?: ""
            val detectedCategoryFromBankCategory = TransactionCategoryDetector.detect(bankCategory)

            // Если категория не обнаружена по исходной категории банка, проверяем по описанию
            val detectedCategory = if (detectedCategoryFromBankCategory != "Без категории") {
                detectedCategoryFromBankCategory
            } else {
                // Используем все доступные данные для определения категории
                val fullDescription = listOf(
                    bankCategory,
                    partial.description.joinToString(" ")
                ).joinToString(" ")
                TransactionCategoryDetector.detect(fullDescription)
            }
            
            return Transaction(
                amount = money,
                category = detectedCategory,
                date = transactionDate,
                isExpense = isExpense,
                note = noteParts.joinToString("; ").ifBlank { null },
                source = transactionSource,
                sourceColor = 0,
                categoryId = "",
                title = title
            )
        } catch (_: Exception) {
            return null
        }
    }

    override fun parseLine(line: String): Transaction? = throw NotImplementedError("parseLine не используется в SberbankPdfImportUseCase")
} 