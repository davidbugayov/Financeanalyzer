package com.davidbugayov.financeanalyzer.domain.usecase.export

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.util.Result
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportTransactionsToCSVUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val context: Context
) {

    suspend operator fun invoke(): Result<File> {
        return try {
            val transactions = transactionRepository.getAllTransactions().first()
            val csvFile = createCSVFile(transactions)
            Result.Success(csvFile)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun createCSVFile(transactions: List<Transaction>): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "transactions_$timestamp.csv"
        val file = File(context.cacheDir, fileName)
        
        FileWriter(file).use { writer ->
            // Write CSV header
            writer.append("ID,Date,Amount,Type,Category,Description,Wallet\n")
            
            // Write transaction data
            transactions.forEach { transaction ->
                writer.append(transaction.id).append(",")
                writer.append(transaction.date.toString()).append(",")
                writer.append(transaction.amount.value.toString()).append(",")
                writer.append(transaction.type.name).append(",")
                writer.append(transaction.category?.name ?: "").append(",")
                writer.append(transaction.description ?: "").append(",")
                writer.append(transaction.walletId)
                writer.append("\n")
            }
        }
        
        return file
    }
    
    enum class ExportAction {
        SHARE, OPEN, SAVE
    }
    
    suspend fun shareCSVFile(file: File): Result<Uri> {
        return try {
            // Implementation depends on Android-specific code for sharing files
            // This would typically use FileProvider to get a content URI
            Result.Success(Uri.fromFile(file))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun openCSVFile(file: File): Result<Uri> {
        return try {
            // Implementation depends on Android-specific code for opening files
            Result.Success(Uri.fromFile(file))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 