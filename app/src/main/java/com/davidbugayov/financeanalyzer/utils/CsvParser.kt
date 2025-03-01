package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.data.model.Transaction
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object CsvParser {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Чтение CSV-файла
    fun readCsv(file: File): List<Transaction> {
        val reader = FileReader(file)
        val csvParser = CSVParser(reader, CSVFormat.DEFAULT.withHeader(
            "Date", "Title", "Amount", "Category", "IsExpense", "Note"
        ))
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
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
            "Date", "Title", "Amount", "Category", "IsExpense", "Note"
        ))

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