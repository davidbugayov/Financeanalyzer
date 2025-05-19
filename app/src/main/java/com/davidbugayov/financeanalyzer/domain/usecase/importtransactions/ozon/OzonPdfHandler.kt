package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ozon

import android.content.Context
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractBankHandler
import timber.log.Timber

class OzonPdfHandler(
    transactionRepository: TransactionRepository,
    context: Context
) : AbstractBankHandler(transactionRepository, context) {

    override val bankName: String = "Ozon PDF"

    override fun supportsFileType(fileType: FileType): Boolean {
        return fileType == FileType.PDF
    }

    override fun createImporter(fileType: FileType): ImportTransactionsUseCase {
        if (supportsFileType(fileType)) {
            Timber.Forest.d("[$bankName Handler] Creating OzonPdfImportUseCase")
            return OzonPdfImportUseCase(context, transactionRepository)
        }
        throw IllegalArgumentException("[$bankName Handler] does not support file type: $fileType")
    }

    override fun getFileNameKeywords(): List<String> {
        return listOf("ozon", "озон", "выписка", "statement")
    }

    // canHandle будет использовать реализацию из AbstractBankHandler,
    // которая проверяет supportsFileType и getFileNameKeywords
}