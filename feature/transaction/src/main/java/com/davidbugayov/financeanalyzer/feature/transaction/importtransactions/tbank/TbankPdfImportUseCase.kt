package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.tbank

import android.content.Context
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.AbstractPdfImportUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.category.TransactionCategoryDetector
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportProgressCallback
import timber.log.Timber
import java.io.BufferedReader
import java.math.BigDecimal
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TbankPdfImportUseCase(
    context: Context,
    transactionRepository: TransactionRepository,
) : AbstractPdfImportUseCase(context, transactionRepository) {

    override val bankName: String = "Тинькофф Банк (PDF)"
    private val transactionSource: String = context.getString(R.string.transaction_source_tinkoff)

    private data class PartialTransaction(
        var date: Date? = null,
        var documentNumber: String? = null,
        val descriptionLines: MutableList<String> = mutableListOf(),
        var amount: Double? = null,
        var currency: Currency = Currency.RUB,
        var isExpense: Boolean? = null,
        var linesProcessedForCurrentTx: Int = 0,
        var isComplete: Boolean = false,
    ) {
        fun clear() {
            date = null
            documentNumber = null
            descriptionLines.clear()
            amount = null
            currency = Currency.RUB
            isExpense = null
            linesProcessedForCurrentTx = 0
            isComplete = false
        }
        fun isValidForFinalization(): Boolean = date != null && amount != null && isExpense != null
        fun buildDescription(): String = descriptionLines.joinToString(" ").trim().replace(
            "\\s+".toRegex(),
            " ",
        )
    }

    companion object {
        private val TBANK_INDICATORS = listOf(
            "TINKOFF",
            "ТИНЬКОФФ",
            "Тинькофф Банк",
            "Тинькофф",
            "ТБАНК",
            "TBANK",
            "АО «ТБАНК»",
            "АО «Тинькофф Банк»",
        )
        private val TBANK_STATEMENT_TITLES = listOf(
            "Выписка по счетам", "Выписка по карте", "Операции по счету", "История операций",
            "Справка о движении средств", "Движение средств", "Платежное поручение",
            "С движением средств", "Справка о движении",
        )
        private const val MAX_VALIDATION_LINES = 40
        private const val MAX_HEADER_SKIP_LINES = 300
        private val tbankDateRegex = Regex("""^(\d{2}\.\d{2}\.\d{4})$""")
        private val tbankTimeRegex = Regex("""^(\d{2}:\d{2})$""")
        private val tbankAmountWithDescRegex =
            Regex(
                """([+\-])?[\s]*(\d[\d\s.,]*[\d])[\s]*[₽PР][\s]+([+\-])?[\s]*(\d[\d\s.,]*[\d])[\s]*[₽PР][\s]+(.+)""",
            )
        private val tbankSimpleAmountRegex = Regex("""([+\-])?[\s]*(\d[\d\s.,]*[\d])[\s]*[₽PР]""")
        private val IGNORE_PATTERNS = listOf(
            Regex("^Итого:", RegexOption.IGNORE_CASE),
            Regex("^Баланс на начало периода", RegexOption.IGNORE_CASE),
            Regex("^Баланс на конец периода", RegexOption.IGNORE_CASE),
            Regex("^Выписка сформирована:", RegexOption.IGNORE_CASE),
            Regex("^Пополнения:", RegexOption.IGNORE_CASE),
            Regex("^Расходы:", RegexOption.IGNORE_CASE),
            Regex("^С уважением,", RegexOption.IGNORE_CASE),
            Regex("^Руководитель", RegexOption.IGNORE_CASE),
            Regex("^АО «Тинькофф Банк»", RegexOption.IGNORE_CASE),
            Regex("^БИК", RegexOption.IGNORE_CASE),
            Regex("^Страница \\d+ из \\d+", RegexOption.IGNORE_CASE),
        )
    }

    override fun isValidFormat(reader: BufferedReader): Boolean {
        val headerLines = mutableListOf<String>()
        var linesReadCount = 0
        try {
            reader.mark(16384)
            var currentLineText: String?
            while (linesReadCount < MAX_VALIDATION_LINES) {
                currentLineText = reader.readLine()
                if (currentLineText != null) {
                    val cleanLine = currentLineText.replace("\u0000", "").trim()
                    headerLines.add(cleanLine)
                    linesReadCount++
                } else {
                    break
                }
            }
            reader.reset()
        } catch (_: Exception) {
            return false
        }
        val content = headerLines.joinToString("\n")
        val hasBankIndicator = TBANK_INDICATORS.any { content.contains(it, ignoreCase = true) }
        val hasStatementTitle = TBANK_STATEMENT_TITLES.any { content.contains(it, ignoreCase = true) }
        var hasDateFormat = false
        var hasAmountFormat = false
        for (line in headerLines) {
            if (tbankDateRegex.find(line) != null) hasDateFormat = true
            if (tbankSimpleAmountRegex.find(line) != null) hasAmountFormat = true
        }
        return hasBankIndicator && (hasStatementTitle || (hasDateFormat && hasAmountFormat))
    }

    override fun skipHeaders(reader: BufferedReader) {
        var linesSkipped = 0
        var line: String?
        reader.mark(32768)
        while (linesSkipped < MAX_HEADER_SKIP_LINES) {
            line = reader.readLine()?.replace("\u0000", "")?.trim()
            if (line == null) {
                reader.reset()
                return
            }
            linesSkipped++
            if (tbankDateRegex.find(line) != null || tbankSimpleAmountRegex.find(line) != null) {
                reader.reset()
                repeat(linesSkipped - 1) { reader.readLine() }
                return
            }
        }
        reader.reset()
    }

    override fun shouldSkipLine(line: String): Boolean {
        val trimmedLine = line.trim()
        if (trimmedLine.isBlank()) return true
        if (IGNORE_PATTERNS.any { it.containsMatchIn(trimmedLine) }) return true
        return false
    }

    override fun parseLine(line: String): Transaction? = throw NotImplementedError(
        "parseLine(line: String) не используется в TbankPdfImportUseCase",
    )

    private fun parseLineInternal(line: String, ptx: PartialTransaction?): Pair<Transaction?, PartialTransaction?> {
        val trimmedLine = line.replace("\u0000", "").trim()
        if (shouldSkipLine(trimmedLine)) return null to ptx
        val partial = ptx ?: PartialTransaction()
        val dateMatch = tbankDateRegex.find(trimmedLine)
        if (dateMatch != null) {
            val dateStr = dateMatch.groupValues[1]
            if (partial.date != null && partial.amount != null && partial.isComplete) {
                val tx = finalizeTransaction(partial)
                partial.clear()
                partial.date = parseTbankDate(dateStr)
                return tx to partial
            }
            if (partial.date != null) partial.clear()
            partial.date = parseTbankDate(dateStr)
            return null to partial
        }
        val timeMatch = tbankTimeRegex.find(trimmedLine)
        if (timeMatch != null && partial.date != null && partial.documentNumber == null) {
            partial.documentNumber = timeMatch.groupValues[1]
            return null to partial
        }
        val amountWithDescMatch = tbankAmountWithDescRegex.find(trimmedLine)
        if (amountWithDescMatch != null && partial.date != null) {
            val sign = amountWithDescMatch.groupValues[1].ifEmpty { amountWithDescMatch.groupValues[3] }
            val amountStr = amountWithDescMatch.groupValues[2].replace("\\s".toRegex(), "").replace(
                ",",
                ".",
            )
            val description = amountWithDescMatch.groupValues[5].trim()
            try {
                val amountValue = amountStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
                partial.amount = amountValue.toDouble()
                partial.descriptionLines.add(description)
                val isExpenseBySign = sign == "-"
                val isExpenseByDescription = !description.contains("Пополнение", ignoreCase = true) &&
                    !description.contains("Перевод от", ignoreCase = true) &&
                    !description.contains("Возврат", ignoreCase = true)
                partial.isExpense = isExpenseBySign || (sign.isEmpty() && isExpenseByDescription)
                partial.currency = Currency.RUB
                partial.isComplete = true
                val tx = finalizeTransaction(partial)
                partial.clear()
                return tx to partial
            } catch (_: Exception) {
            }
            return null to partial
        }
        val simpleAmountMatch = tbankSimpleAmountRegex.find(trimmedLine)
        if (simpleAmountMatch != null && partial.date != null) {
            try {
                val sign = simpleAmountMatch.groupValues[1]
                val amountStr = simpleAmountMatch.groupValues[2].replace("\\s".toRegex(), "").replace(
                    ",",
                    ".",
                )
                val amountValue = amountStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
                partial.amount = amountValue.toDouble()
                partial.isExpense = sign == "-" || (
                    sign.isEmpty() && !partial.buildDescription().contains(
                        "Пополнение",
                        ignoreCase = true,
                    )
                    )
                partial.currency = Currency.RUB
                val remainingText = trimmedLine.substring(simpleAmountMatch.range.last + 1).trim()
                if (remainingText.isNotEmpty()) partial.descriptionLines.add(remainingText)
                partial.isComplete = true
                if (partial.isValidForFinalization()) {
                    val tx = finalizeTransaction(partial)
                    partial.clear()
                    return tx to partial
                }
            } catch (_: Exception) {
            }
            return null to partial
        }
        if (partial.date != null) {
            partial.descriptionLines.add(trimmedLine)
            if (partial.isComplete && partial.isValidForFinalization() && partial.descriptionLines.size >= 2) {
                val tx = finalizeTransaction(partial)
                partial.clear()
                return tx to partial
            }
        }
        return null to partial
    }

    override fun parseTransactions(
        reader: BufferedReader,
        progressCallback: ImportProgressCallback,
        rawText: String,
    ): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        var linesProcessed = 0
        val totalLines = rawText.lines().size.coerceAtLeast(1)
        var line: String? = reader.readLine()
        var currentPartialTransaction: PartialTransaction? = null
        while (line != null) {
            linesProcessed++
            if (linesProcessed % 100 == 0) {
                Timber.d(
                    "$bankName: Обработано $linesProcessed строк, найдено ${transactions.size} транзакций",
                )
            }
            val progress = 15 + (linesProcessed * 70 / totalLines).coerceAtMost(70)
            progressCallback.onProgress(
                progress,
                100,
                "Обработка строки $linesProcessed / ~$totalLines",
            )
            parseLineInternal(line, currentPartialTransaction).let { (tx, updatedPartial) ->
                if (tx != null) transactions.add(tx)
                currentPartialTransaction = updatedPartial
            }
            line = reader.readLine()
        }
        // Финализируем последнюю транзакцию
        currentPartialTransaction?.let { ptx ->
            if (ptx.isComplete && ptx.isValidForFinalization()) {
                transactions.add(finalizeTransaction(ptx))
            }
        }
        return transactions
    }

    private fun finalizeTransaction(ptx: PartialTransaction): Transaction {
        val description = ptx.buildDescription().ifEmpty {
            "Операция от " + SimpleDateFormat(
                "dd.MM.yyyy",
                Locale.getDefault(),
            ).format(ptx.date!!)
        }
        val category = TransactionCategoryDetector.detect(description)
        return Transaction(
            date = ptx.date!!,
            title = description,
            amount = Money(ptx.amount!!, ptx.currency),
            isExpense = ptx.isExpense!!,
            source = transactionSource,
            sourceColor = 0,
            category = category,
            note = if (ptx.documentNumber != null) "Время: ${ptx.documentNumber}" else "",
        )
    }

    private fun parseTbankDate(dateStr: String): Date? {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return try {
            format.parse(dateStr)
        } catch (_: ParseException) {
            null
        }
    }
}
