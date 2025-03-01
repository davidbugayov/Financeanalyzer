package com.davidbugayov.financeanalyzer.data.repository

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.data.model.Transaction
import com.davidbugayov.financeanalyzer.utils.FileHelper

class TransactionRepository(private val context: Context) {

    private val csvFileName = "transactions.csv"
    private var csvFileUri: Uri? = null

    init {
        // Проверяем, существует ли файл, и используем его URI
        csvFileUri = FileHelper.getCsvFileUri(context, csvFileName)
        if (csvFileUri == null) {
            // Если файл не существует, создаем новый
            csvFileUri = FileHelper.createCsvFile(context, csvFileName)
        }
    }


    // Загрузка транзакций из CSV-файла
    fun loadTransactions(): List<Transaction> {
        return if (csvFileUri == null) {
            // Если файл не создан, создаем его
            csvFileUri = FileHelper.createCsvFile(context, csvFileName)
            emptyList()  // Возвращаем пустой список, так как файл только что создан
        } else {
            FileHelper.readCsv(context, csvFileUri!!)
        }
    }

    // Добавление транзакции в CSV-файл
    fun addTransaction(transaction: Transaction) {
        csvFileUri?.let { uri ->
            val currentTransactions = loadTransactions().toMutableList()
            currentTransactions.add(transaction)
            FileHelper.writeCsv(context, uri, currentTransactions)
        }
    }
}