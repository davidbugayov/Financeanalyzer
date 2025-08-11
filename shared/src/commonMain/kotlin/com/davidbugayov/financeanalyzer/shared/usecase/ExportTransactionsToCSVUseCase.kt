package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Transaction

/**
 * Генерирует CSV-представление списка транзакций. Возвращает CSV как строку.
 * UI/клиент сохраняет/шэрит строку самостоятельно.
 */
class ExportTransactionsToCSVUseCase {
    operator fun invoke(
        transactions: List<Transaction>,
        includeHeader: Boolean = true,
        separator: Char = ',',
    ): String {
        val sb = StringBuilder()
        if (includeHeader) {
            sb.append(listOf("id", "date", "category", "source", "isExpense", "amount", "note").joinToString(separator.toString()))
            sb.append('\n')
        }
        transactions.forEach { tx ->
            val cells = listOf(
                tx.id,
                tx.date.toString(),
                escape(tx.category, separator),
                escape(tx.source, separator),
                tx.isExpense.toString(),
                tx.amount.toPlainString(),
                escape(tx.note ?: "", separator),
            )
            sb.append(cells.joinToString(separator.toString()))
            sb.append('\n')
        }
        return sb.toString()
    }

    private fun escape(value: String, sep: Char): String {
        if (value.isEmpty()) return ""
        val needsQuotes = value.contains(sep) || value.contains('\n') || value.contains('"')
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }
}


