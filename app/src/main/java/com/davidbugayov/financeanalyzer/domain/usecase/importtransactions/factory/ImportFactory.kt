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
import com.davidbugayov.financeanalyzer.domain.util.Result
import com.davidbugayov.financeanalyzer.domain.util.safeCallSync
import timber.log.Timber

/**
 * Фабрика для создания и выбора подходящего обработчика импорта транзакций.
 * Использует зарегистрированные BankHandler'ы для определения банка и типа файла.
 */
class ImportFactory(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
) {

    private val handlers: List<AbstractBankHandler> by lazy {
        listOf(
            // Specific handlers first
            SberbankPdfHandler(transactionRepository, context),
            AlfaBankExcelHandler(transactionRepository, context),
            OzonPdfHandler(transactionRepository, context),
            // Generic handlers next
            GenericCsvHandler(transactionRepository, context),
            GenericExcelHandler(transactionRepository, context),
            // TbankPdfHandler последним, чтобы он мог обрабатывать все PDF,
            // которые не были обработаны другими хендлерами
            TbankPdfHandler(transactionRepository, context),
        ).also {
            Timber.d(
                "ImportFactory: Зарегистрированы обработчики: ${it.joinToString { h -> h.bankName }}",
            )
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

        // Явно проверяем, является ли файл выпиской с движением средств
        if (fileType == FileType.PDF && (
                fileName.contains("движени", ignoreCase = true) ||
                    fileName.contains("справка", ignoreCase = true)
                )
        ) {
            // Сначала проверяем на Ozon в имени файла
            if (fileName.contains("ozon", ignoreCase = true) || fileName.contains(
                    "озон",
                    ignoreCase = true,
                )
            ) {
                val ozonHandler = handlers.find { it is OzonPdfHandler }
                if (ozonHandler != null) {
                    Timber.i("Явно выбран обработчик OZON для файла движения средств: $fileName")
                    return when (val result = safeCallSync { ozonHandler.createImporter(fileType) }) {
                        is Result.Success -> result.data
                        is Result.Error -> {
                            Timber.e(
                                result.exception,
                                "Error creating importer with OzonPdfHandler for $fileName",
                            )
                            null
                        }
                    }
                }
            }

            // Затем проверяем на Тинькофф, если нет Ozon
            val tbankHandler = handlers.filterIsInstance<TbankPdfHandler>().firstOrNull()
            if (tbankHandler != null) {
                Timber.i("Явно выбран обработчик Тинькофф для файла движения средств: $fileName")
                return when (val result = safeCallSync { tbankHandler.createImporter(fileType) }) {
                    is Result.Success -> result.data
                    is Result.Error -> {
                        Timber.e(
                            result.exception,
                            "Error creating importer with TbankPdfHandler for $fileName",
                        )
                        null
                    }
                }
            }
        }

        for (handler in handlers) {
            Timber.d("Проверка обработчика ${handler.bankName} для файла $fileName")
            if (handler.canHandle(fileName, uri, fileType)) {
                Timber.i("Selected handler: ${handler.bankName} for file $fileName")
                return when (val result = safeCallSync { handler.createImporter(fileType) }) {
                    is Result.Success -> result.data
                    is Result.Error -> {
                        Timber.e(
                            result.exception,
                            "Error creating importer with handler ${handler.bankName} for $fileName",
                        )
                        null
                    }
                }
            }
        }
        Timber.w("No suitable handler found for: $fileName, type: $fileType")
        return null
    }
}
