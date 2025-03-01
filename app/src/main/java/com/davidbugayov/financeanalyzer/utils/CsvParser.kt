package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.data.model.Transaction
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object CsvParser {

    // Чтение CSV-файла
    fun readCsv(file: File): List<Transaction> {
        val reader = FileReader(file)
        val csvParser = CSVParser(reader, CSVFormat.DEFAULT.withHeader("Date", "Description", "Amount", "Type"))
        val transactions = mutableListOf<Transaction>()

        for (record in csvParser) {
            val date = record["Date"]
            val description = record["Description"]
            val amount = record["Amount"].toDouble()
            val type = record["Type"]

            transactions.add(Transaction(date, description, amount, type))
        }

        return transactions
    }

    // Запись в CSV-файл
    fun writeCsv(file: File, transactions: List<Transaction>) {
        val writer = FileWriter(file, true)  // true для добавления в конец файла
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Date", "Description", "Amount", "Type"))

        for (transaction in transactions) {
            csvPrinter.printRecord(transaction.date, transaction.description, transaction.amount, transaction.type)
        }

        csvPrinter.flush()
        csvPrinter.close()
    }
}