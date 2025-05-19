package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.sberbank

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractBankHandler
import timber.log.Timber

class SberbankPdfHandler(
    transactionRepository: TransactionRepository,
    context: Context
) : AbstractBankHandler(transactionRepository, context) {

    override val bankName: String = "Sberbank PDF"

    override fun supportsFileType(fileType: FileType): Boolean {
        return fileType == FileType.PDF
    }

    override fun createImporter(fileType: FileType): ImportTransactionsUseCase {
        if (supportsFileType(fileType)) {
            Timber.Forest.d("[$bankName Handler] Creating SberbankPdfImportUseCase")
            return SberbankPdfImportUseCase(context, transactionRepository)
        }
        throw IllegalArgumentException("[$bankName Handler] does not support file type: $fileType")
    }

    override fun getFileNameKeywords(): List<String> {
        // User will provide these keywords. Using common placeholders for now.
        return listOf("sberbank", "сбербанк", "выписка", "statement", "сбер")
    }

    override fun canHandle(fileName: String, uri: Uri, fileType: FileType): Boolean {
        // Basic check (file type and keywords in the name)
        if (super.canHandle(fileName, uri, fileType)) {
            return true
        }
        // For PDF, content check in canHandle is usually not performed beyond basic magic number checks (not implemented here).
        // If super.canHandle() is false (name/type mismatch), then this handler cannot process it.
        Timber.Forest.d("[$bankName Handler] Did not match file by name/type: $fileName. Content check for PDF is complex for a simple preview.")
        return false
    }
}