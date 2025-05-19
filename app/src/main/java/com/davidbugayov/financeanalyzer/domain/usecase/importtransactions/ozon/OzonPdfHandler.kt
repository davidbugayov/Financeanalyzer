package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ozon

import android.content.Context
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractPdfBankHandler
import timber.log.Timber

class OzonPdfHandler(
    transactionRepository: TransactionRepository,
    context: Context
) : AbstractPdfBankHandler(transactionRepository, context) {

    override val bankName: String = "Ozon PDF"

    override val pdfKeywords: List<String> = listOf(
        "ozon", "озон", "ozon statement", "выписка ozon"
    )

    override fun supportsFileType(fileType: FileType): Boolean {
        return fileType == FileType.PDF
    }

    override fun createImporter(fileType: FileType): ImportTransactionsUseCase {
        if (supportsFileType(fileType)) {
            Timber.d("[$bankName Handler] Creating OzonPdfImportUseCase")
            return OzonPdfImportUseCase(context, transactionRepository)
        }
        throw IllegalArgumentException("[$bankName Handler] does not support file type: $fileType")
    }

    // getFileNameKeywords() больше не нужен, так как используется pdfKeywords
}