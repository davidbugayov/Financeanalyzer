package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.data.model.Transaction
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvParser {
    // Используем lazy для инициализации dateFormat, чтобы избежать проблем при смене локали
    private val dateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    private val HEADERS = arrayOf("Date", "Title", "Amount", "Category", "IsExpense", "Note")

    // Чтение CSV-файла
    fun readCsv(file: File): List<Transaction> {
        val reader = FileReader(file)
        val csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader(*HEADERS)
            .setSkipHeaderRecord(true)
            .build()
        val csvParser = CSVParser(reader, csvFormat)
        val transactions = mutableListOf<Transaction>()

        for (record in csvParser) {
            val date = dateFormat.parse(record["Date"]) ?: Date()
            val title = record["Title"] ?: ""
            val amount = record["Amount"]?.toDoubleOrNull() ?: 0.0
            val category = record["Category"] ?: ""
            val isExpense = record["IsExpense"]?.toBoolean() ?: false
            val note = record["Note"]

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
        }

        csvParser.close()
        reader.close()
        return transactions
    }

    // Запись в CSV-файл
    fun writeCsv(file: File, transactions: List<Transaction>) {
        val writer = FileWriter(file, true)  // true для добавления в конец файла
        val csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader(*HEADERS)
            .build()
        val csvPrinter = CSVPrinter(writer, csvFormat)

        for (transaction in transactions) {
            csvPrinter.printRecord(
                dateFormat.format(transaction.date),
                transaction.title,
                transaction.amount,
                transaction.category,
                transaction.isExpense,
                transaction.note
            )
        }

        csvPrinter.flush()
        csvPrinter.close()
        writer.close()
    }
}