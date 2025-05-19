package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.tbank

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractBankHandler
import timber.log.Timber

class TbankPdfHandler(
    transactionRepository: TransactionRepository,
    context: Context
) : AbstractBankHandler(transactionRepository, context) {

    override val bankName: String = "Tinkoff PDF"

    override fun supportsFileType(fileType: FileType): Boolean {
        return fileType == FileType.PDF
    }

    override fun createImporter(fileType: FileType): ImportTransactionsUseCase {
        if (supportsFileType(fileType)) {
            Timber.d("[$bankName Handler] Creating TbankPdfImportUseCase")
            return TbankPdfImportUseCase(context, transactionRepository)
        }
        throw IllegalArgumentException("[$bankName Handler] does not support file type: $fileType")
    }

    override fun getFileNameKeywords(): List<String> {
        return listOf(
            "tinkoff", "тинькофф", "выписка", "statement", "справка", 
            "движение средств", "справка о движении", "tbank", "тбанк",
            "с движением средств"
        )
    }

    // canHandle будет использовать реализацию из AbstractBankHandler,
    // которая проверяет supportsFileType и getFileNameKeywords
}