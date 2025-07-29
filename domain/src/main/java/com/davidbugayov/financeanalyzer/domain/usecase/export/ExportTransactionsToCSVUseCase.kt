package com.davidbugayov.financeanalyzer.domain.usecase.export

import com.davidbugayov.financeanalyzer.core.model.AppException
import com.davidbugayov.financeanalyzer.core.util.Result
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.davidbugayov.financeanalyzer.domain.util.StringProvider

/**
 * UseCase для экспорта транзакций в CSV-файл.
 */
class ExportTransactionsToCSVUseCase(
    private val transactionRepository: TransactionRepository,
) {

    /**
     * Тип действия для экспорта.
     */
    enum class ExportAction {
        SHARE,    // Поделиться файлом
        OPEN,     // Открыть файл
        SAVE_ONLY, // Только сохранить файл
        SAVE      // Сохранить файл (альтернативное название)
    }

    /**
     * Экспортирует все транзакции в CSV-файл.
     *
     * @return Результат операции с файлом или сообщением об ошибке
     */
    suspend operator fun invoke(): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Получаем все транзакции из репозитория
            val transactions = transactionRepository.getAllTransactions()

            if (transactions.isEmpty()) {
                return@withContext Result.Error(AppException.Business.InvalidOperation(StringProvider.errorNoTransactionsToExport))
            }

            // Создаем файл во временной директории с понятным именем
            val fileName = StringProvider.exportFilename(getCurrentDateTimeFormatted())
            val file = File.createTempFile("transactions_", ".csv")
            // Переименовываем файл с красивым именем
            val finalFile = File(file.parent, fileName)
            file.renameTo(finalFile)

            // Записываем данные в файл с красивыми заголовками
            FileWriter(finalFile).use { writer ->
                // Заголовок CSV на русском языке
                writer.append("${StringProvider.exportCsvHeader}\n")

                // Данные транзакций
                transactions.forEach { transaction ->
                    writer.append("\"${transaction.id}\",")
                    
                    // Форматируем дату в читаемом формате
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    writer.append("\"${dateFormat.format(transaction.date)}\",")
                    
                    writer.append("\"${transaction.category}\",")
                    writer.append("\"${transaction.amount.toPlainString()}\",")
                    writer.append("\"${if (transaction.isExpense) StringProvider.exportTransactionTypeExpense else StringProvider.exportTransactionTypeIncome}\",")
                    writer.append("\"${transaction.note ?: ""}\",")
                    writer.append("\"${transaction.source}\"\n")
                }
            }

            Timber.d(StringProvider.logCsvFileCreated(finalFile.absolutePath, finalFile.length().toInt()))
            return@withContext Result.Success(finalFile)
        } catch (e: IOException) {
            Timber.e(e, StringProvider.logExportError)
            return@withContext Result.Error(AppException.FileSystem.ReadError(cause = e))
        } catch (e: Exception) {
            Timber.e(e, StringProvider.logExportUnexpectedError)
            return@withContext Result.Error(AppException.Unknown(cause = e))
        }
    }

    /**
     * Открывает CSV файл в подходящем приложении
     * Пока возвращает заглушку - логика должна быть реализована в UI слое
     */
    fun openCSVFile(file: File): Result<Unit> {
        Timber.d(StringProvider.logFileCreated(file.absolutePath))
        return Result.Success(Unit)
    }

    /**
     * Предоставляет CSV файл для отправки через различные приложения
     * Пока возвращает заглушку - логика должна быть реализована в UI слое
     */
    fun shareCSVFile(file: File): Result<Unit> {
        Timber.d(StringProvider.logFileReadyForSend(file.absolutePath))
        return Result.Success(Unit)
    }

    private fun getCurrentDateTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
} 