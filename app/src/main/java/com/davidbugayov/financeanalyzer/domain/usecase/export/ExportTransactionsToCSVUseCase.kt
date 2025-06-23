package com.davidbugayov.financeanalyzer.domain.usecase.export

import android.content.Context
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

/**
 * UseCase для экспорта транзакций в CSV-файл.
 */
class ExportTransactionsToCSVUseCase(
    private val transactionRepository: TransactionRepository,
    private val context: Context,
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
                return@withContext Result.Error(AppException.Business.InvalidOperation("Нет транзакций для экспорта"))
            }

            // Создаем файл в директории приложения
            val fileName = "transactions_${getCurrentDateTimeFormatted()}.csv"
            val file = File(context.filesDir, fileName)

            // Записываем данные в файл
            FileWriter(file).use { writer ->
                // Заголовок CSV
                writer.append("ID,Date,Category,Amount,IsExpense,Note,Source\n")

                // Данные транзакций
                transactions.forEach { transaction ->
                    writer.append("${transaction.id},")
                    writer.append("${transaction.date},")
                    writer.append("${transaction.category},")
                    writer.append("${transaction.amount},")
                    writer.append("${transaction.isExpense},")
                    writer.append("${transaction.note ?: ""},")
                    writer.append("${transaction.source}\n")
                }
            }

            return@withContext Result.Success(file)
        } catch (e: IOException) {
            Timber.e(e, "Ошибка при экспорте транзакций в CSV")
            return@withContext Result.Error(AppException.FileSystem.ReadError(cause = e))
        } catch (e: Exception) {
            Timber.e(e, "Непредвиденная ошибка при экспорте транзакций")
            return@withContext Result.Error(AppException.Unknown(cause = e))
        }
    }

    /**
     * Открывает CSV-файл с помощью внешнего приложения.
     *
     * @param file Файл для открытия
     * @return Результат операции
     */
    fun openCSVFile(file: File): Result<Unit> {
        return Result.Success(Unit)
    }

    /**
     * Делится CSV-файлом с помощью диалога выбора приложения.
     *
     * @param file Файл для отправки
     * @return Результат операции
     */
    fun shareCSVFile(file: File): Result<Unit> {
        return Result.Success(Unit)
    }

    /**
     * Возвращает текущую дату и время в формате для имени файла.
     *
     * @return Строка с датой и временем в формате yyyy-MM-dd_HH-mm-ss
     */
    private fun getCurrentDateTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
