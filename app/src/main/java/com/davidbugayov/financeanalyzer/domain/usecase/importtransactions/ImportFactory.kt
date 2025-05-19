package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions

// Removed SberbankHandler, TinkoffCsvHandler, AlfaBankCsvHandler
import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractBankHandler
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AlfaBankExcelHandler
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.GenericCsvHandler
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.GenericExcelHandler
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.SberbankPdfHandler
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.OzonPdfHandler
import timber.log.Timber

/**
 * Фабрика для создания и выбора подходящего обработчика импорта транзакций.
 * Использует зарегистрированные BankHandler'ы для определения банка и типа файла.
 */
class ImportFactory(
    private val context: Context,
    private val transactionRepository: TransactionRepository
) {

    private val handlers: List<AbstractBankHandler> by lazy {
        listOf(
            // Specific handlers first
            SberbankPdfHandler(transactionRepository, context),
            AlfaBankExcelHandler(transactionRepository, context),
            OzonPdfHandler(transactionRepository, context),
            // Generic handlers last as fallbacks
            GenericCsvHandler(transactionRepository, context),
            GenericExcelHandler(transactionRepository, context)
        )
    }

    /**
     * Находит подходящий BankHandler для данного файла.
     *
     * @param fileName Имя файла.
     * @param uri URI файла.
     * @param fileType Тип файла.
     * @return Подходящий AbstractBankHandler или null, если ни один не подходит.
     */
    fun findBankHandler(fileName: String, uri: Uri, fileType: FileType): AbstractBankHandler? {
        Timber.d("Поиск обработчика для файла: $fileName, тип: $fileType, uri: $uri")
        for (handler in handlers) {
            if (handler.canHandle(fileName, uri, fileType)) {
                Timber.i("Найден обработчик: ${handler.bankName} для файла $fileName")
                return handler
            }
        }
        Timber.w("Подходящий обработчик не найден для файла: $fileName, тип: $fileType")
        return null
    }

    /**
     * Создает импортер (UseCase) для данного файла, если найден подходящий обработчик.
     *
     * @param fileName Имя файла.
     * @param uri URI файла.
     * @param fileType Тип файла.
     * @return Экземпляр ImportTransactionsUseCase или null, если обработчик не найден или не поддерживает тип файла.
     */
    fun getImporter(fileName: String, uri: Uri, fileType: FileType): ImportTransactionsUseCase? {
        Timber.d("Attempting to find importer for: $fileName, type: $fileType")
        for (handler in handlers) {
            if (handler.canHandle(fileName, uri, fileType)) {
                Timber.i("Selected handler: ${handler.bankName} for file $fileName")
                try {
                    return handler.createImporter(fileType)
                } catch (e: Exception) {
                    Timber.e(e, "Error creating importer with handler ${handler.bankName} for $fileName")
                    // Optionally, could try next handler or just fail
                    return null // Or throw a specific exception
                }
            }
        }
        Timber.w("No suitable handler found for: $fileName, type: $fileType")
        return null
    }
} 