package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.data.model.Transaction
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object CsvParser {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val csvFormat = CSVFormat.DEFAULT.withHeader(
        "Date", "Title", "Amount", "Category", "IsExpense", "Note"
    )

    // Чтение CSV из InputStream
    fun readCsv(inputStream: InputStream): List<Transaction> {
        val reader = inputStream.bufferedReader()
        val csvParser = CSVParser(reader, csvFormat)
        val transactions = mutableListOf<Transaction>()

        try {
            for (record in csvParser) {
                try {
                    val date = dateFormat.parse(record["Date"]) ?: Date()
                    val title = record["Title"] ?: ""
                    val amount = record["Amount"]?.toDoubleOrNull() ?: 0.0
                    val category = record["Category"] ?: ""
                    val isExpense = record["IsExpense"]?.toBoolean() ?: false
                    val note = record["Note"]?.takeIf { it.isNotEmpty() }

                    transactions.add(
                        Transaction(
                            title = title,
                            amount = amount,
                            category = category,
                            isExpense = isExpense,
                            date = date,
                            note = note
                        )
                    )
                } catch (e: Exception) {
                    // Пропускаем некорректные записи
                    e.printStackTrace()
                }
            }
        } finally {
            csvParser.close()
            reader.close()
        }
        
        return transactions
    }

    // Запись в CSV через OutputStream
    fun writeCsv(outputStream: OutputStream, transactions: List<Transaction>) {
        val writer = outputStream.bufferedWriter()
        val csvPrinter = CSVPrinter(writer, csvFormat)

        try {
            // Записываем заголовки
            csvPrinter.printRecord("Date", "Title", "Amount", "Category", "IsExpense", "Note")
            
            // Записываем транзакции
            for (transaction in transactions) {
                csvPrinter.printRecord(
                    dateFormat.format(transaction.date),
                    transaction.title,
                    transaction.amount,
                    transaction.category,
                    transaction.isExpense,
                    transaction.note ?: ""
                )
            }
        } finally {
            csvPrinter.flush()
            csvPrinter.close()
            writer.close()
        }
    }
}