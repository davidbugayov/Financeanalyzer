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
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import org.koin.core.context.GlobalContext

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
                return@withContext Result.Error(
                    AppException.Business.InvalidOperation(
                        GlobalContext.get().get<ResourceProvider>().getStringByName("error_no_transactions_to_export")
                    )
                )
            }

            // Создаем файл во временной директории с понятным именем
            val fileName = GlobalContext.get().get<ResourceProvider>().getStringByName("export_filename") + "_" + getCurrentDateTimeFormatted() + ".csv"
            val file = File.createTempFile("transactions_", ".csv")
            // Переименовываем файл с красивым именем
            val finalFile = File(file.parent, fileName)
            file.renameTo(finalFile)

            // Записываем данные в файл с красивыми заголовками
            FileWriter(finalFile).use { writer ->
                // Заголовок CSV на русском языке
                writer.append("${GlobalContext.get().get<ResourceProvider>().getStringByName("export_csv_header")}\n")

                // Данные транзакций
                transactions.forEach { transaction ->
                    writer.append("\"${transaction.id}\",")
                    
                    // Форматируем дату в читаемом формате
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    writer.append("\"${dateFormat.format(transaction.date)}\",")
                    
                    writer.append("\"${transaction.category}\",")
                    writer.append("\"${transaction.amount.toPlainString()}\",")
                    writer.append("\"${if (transaction.isExpense) GlobalContext.get().get<ResourceProvider>().getStringByName("export_transaction_type_expense") else GlobalContext.get().get<ResourceProvider>().getStringByName("export_transaction_type_income")}\",")
                    writer.append("\"${transaction.note ?: ""}\",")
                    writer.append("\"${transaction.source}\"\n")
                }
            }

            Timber.d(GlobalContext.get().get<ResourceProvider>().getStringByName("log_csv_file_created"), finalFile.absolutePath, finalFile.length().toInt())
            return@withContext Result.Success(finalFile)
        } catch (e: IOException) {
            Timber.e(e, GlobalContext.get().get<ResourceProvider>().getStringByName("log_export_error"))
            return@withContext Result.Error(AppException.FileSystem.ReadError(cause = e))
        } catch (e: Exception) {
            Timber.e(e, GlobalContext.get().get<ResourceProvider>().getStringByName("log_export_unexpected_error"))
            return@withContext Result.Error(AppException.Unknown(cause = e))
        }
    }

    /**
     * Открывает CSV файл в подходящем приложении
     * Пока возвращает заглушку - логика должна быть реализована в UI слое
     */
    fun openCSVFile(file: File): Result<Unit> {
        Timber.d(GlobalContext.get().get<ResourceProvider>().getStringByName("log_file_created"), file.absolutePath)
        return Result.Success(Unit)
    }

    /**
     * Предоставляет CSV файл для отправки через различные приложения
     * Пока возвращает заглушку - логика должна быть реализована в UI слое
     */
    fun shareCSVFile(file: File): Result<Unit> {
        Timber.d(GlobalContext.get().get<ResourceProvider>().getStringByName("log_file_ready_for_send"), file.absolutePath)
        return Result.Success(Unit)
    }

    private fun getCurrentDateTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
} 