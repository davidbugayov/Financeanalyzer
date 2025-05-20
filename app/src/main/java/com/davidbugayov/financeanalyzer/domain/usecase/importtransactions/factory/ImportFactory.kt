package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.factory

// Removed SberbankHandler, TinkoffCsvHandler, AlfaBankCsvHandler
import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.alfabank.AlfaBankExcelHandler
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.csv.GenericCsvHandler
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractBankHandler
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.GenericExcelHandler
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ozon.OzonPdfHandler
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.sberbank.SberbankPdfHandler
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.tbank.TbankPdfHandler
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
            TbankPdfHandler(transactionRepository, context),
            SberbankPdfHandler(transactionRepository, context),
            AlfaBankExcelHandler(transactionRepository, context),
            OzonPdfHandler(transactionRepository, context),
            // Generic handlers last as fallbacks
            GenericCsvHandler(transactionRepository, context),
            GenericExcelHandler(transactionRepository, context)
        ).also {
            Timber.d("ImportFactory: Зарегистрированы обработчики: ${it.joinToString { h -> h.bankName }}")
        }
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