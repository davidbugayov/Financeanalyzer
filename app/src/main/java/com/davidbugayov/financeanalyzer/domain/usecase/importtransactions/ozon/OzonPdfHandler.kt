package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ozon

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractPdfBankHandler
import timber.log.Timber

/**
 * Хендлер для PDF-выписок Ozon Банка
 */
class OzonPdfHandler(
    transactionRepository: TransactionRepository,
    context: Context
) : AbstractPdfBankHandler(transactionRepository, context) {

    override val bankName: String = "Ozon PDF"

    // Ключевые слова для PDF-файлов Ozon
    override val pdfKeywords: List<String> = listOf(
        "ozon",
        "озон",
        "ozon statement",
        "выписка ozon"
    )

    /**
     * Проверяет, может ли данный хендлер обработать файл по имени
     */
    override fun canHandle(fileName: String, uri: Uri, fileType: FileType): Boolean {
        if (!supportsFileType(fileType)) return false
        val hasKeyword = pdfKeywords.any { fileName.lowercase().contains(it.lowercase()) }
        return hasKeyword
    }

    /**
     * Создаёт UseCase для импорта PDF-выписки Ozon
     */
    override fun createImporter(fileType: FileType): ImportTransactionsUseCase {
        if (supportsFileType(fileType)) {
            Timber.d("[$bankName Handler] Создание OzonPdfImportUseCase")
            return OzonPdfImportUseCase(context, transactionRepository)
        }
        throw IllegalArgumentException("[$bankName Handler] не поддерживает тип файла: $fileType")
    }
}
