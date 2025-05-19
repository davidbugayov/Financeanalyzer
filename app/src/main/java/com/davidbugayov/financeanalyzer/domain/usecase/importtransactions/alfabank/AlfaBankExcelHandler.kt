package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.alfabank

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.excel.ExcelParseConfig
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.excel.GenericExcelImportUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractBankHandler
import timber.log.Timber

class AlfaBankExcelHandler(
    transactionRepository: TransactionRepository,
    context: Context
) : AbstractBankHandler(transactionRepository, context) {

    override val bankName: String = "Alfa-Bank Excel"

    override fun supportsFileType(fileType: FileType): Boolean {
        return fileType == FileType.EXCEL
    }

    override fun createImporter(fileType: FileType): ImportTransactionsUseCase {
        if (supportsFileType(fileType)) {
            // TODO: Define a specific ExcelParseConfig for Alfa-Bank if its Excel structure is known and consistent.
            // For now, using default config. User might need to configure it manually if defaults don't match.
            val alfaBankConfig = ExcelParseConfig(
                // Example: if Alfa-Bank always has 2 header rows and date in column 1 (0-indexed)
                // headerRowCount = 2,
                // dateColumnIndex = 0, // Assuming date is the first column after potential headers
                // descriptionColumnIndex = 1,
                // amountColumnIndex = 2,
                // dateFormatString = "dd.MM.yyyy" // Example date format
            )
            Timber.Forest.d("[$bankName Handler] Creating GenericExcelImportUseCase with specific config for Alfa-Bank: $alfaBankConfig")
            return GenericExcelImportUseCase(context, transactionRepository, alfaBankConfig)
        }
        throw IllegalArgumentException("[$bankName Handler] does not support file type: $fileType")
    }

    override fun getFileNameKeywords(): List<String> {
        // User will provide these keywords. Using common placeholders for now.
        return listOf("alfabank", "альфабанк", "альфа-банк", "statement", "выписка", "excel_export")
    }

    override fun canHandle(fileName: String, uri: Uri, fileType: FileType): Boolean {
        // Basic check (file type and keywords in the name)
        if (super.canHandle(fileName, uri, fileType)) {
            return true
        }
        // For Excel, content check without parsing is not very effective.
        // If super.canHandle() is false, we assume this handler cannot process it.
        Timber.Forest.d("[$bankName Handler] Did not match file by name/type: $fileName. Content check for Excel is not performed at this stage.")
        return false
    }
}