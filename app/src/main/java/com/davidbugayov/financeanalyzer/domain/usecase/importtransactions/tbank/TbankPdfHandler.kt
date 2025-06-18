package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.tbank

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractPdfBankHandler
import java.io.BufferedInputStream
import timber.log.Timber

/**
 * Хендлер для PDF-выписок Тинькофф Банка
 */
class TbankPdfHandler(
    transactionRepository: TransactionRepository,
    context: Context,
) : AbstractPdfBankHandler(transactionRepository, context) {

    override val bankName: String = "Тинькофф PDF"

    // Ключевые слова для PDF-файлов Тинькофф
    override val pdfKeywords: List<String> = listOf(
        "tinkoff",
        "тинькофф",
        "тбанк",
        "tbank",
        "движение средств",
        "справка о движении",
        "номер договора",
        "номер лицевого счета",
    )

    // Негативные ключевые слова для исключения ложных срабатываний
    private fun getNegativeKeywords(): List<String> = listOf(
        "sberbank",
        "сбербанк",
        "сбер",
        "sber",
        "альфа",
        "альфабанк",
        "alfa",
        "ozon",
    )

    /**
     * Проверяет, может ли данный хендлер обработать файл по имени и содержимому
     */
    override fun canHandle(fileName: String, uri: Uri, fileType: FileType): Boolean {
        if (!supportsFileType(fileType)) return false
        val hasPositiveKeyword = pdfKeywords.any { fileName.lowercase().contains(it.lowercase()) }
        val containsNegativeKeyword = getNegativeKeywords().any {
            fileName.lowercase().contains(
                it.lowercase(),
            )
        }
        if (containsNegativeKeyword) {
            Timber.d("[$bankName Handler] Файл содержит ключевые слова других банков: $fileName")
            return false
        }
        // Дополнительная проверка по содержимому файла
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArray(4096) // Увеличиваем размер буфера для лучшего обнаружения
                val bis = BufferedInputStream(inputStream)
                val bytesRead = bis.read(buffer, 0, buffer.size)
                if (bytesRead > 0) {
                    val content = String(buffer, 0, bytesRead)
                    val tinkoffIndicators = listOf(
                        "TINKOFF",
                        "ТИНЬКОФФ",
                        "Тинькофф Банк",
                        "Тинькофф",
                        "ТБАНК",
                        "TBANK",
                        "Номер договора",
                        "Номер лицевого счета",
                        "Движение средств за период",
                    )
                    val hasTinkoffIndicator = tinkoffIndicators.any {
                        content.contains(
                            it,
                            ignoreCase = true,
                        )
                    }

                    // Проверка на наличие табличного формата
                    val hasTableFormat = content.contains("Дата и время") &&
                        content.contains("Сумма в валюте") &&
                        content.contains("Описание операции")

                    val otherBankIndicators = listOf(
                        "СБЕРБАНК",
                        "SBERBANK",
                        "СберБанк",
                        "Альфа-Банк",
                        "АЛЬФА-БАНК",
                        "OZON",
                    )
                    val hasOtherBankIndicator = otherBankIndicators.any {
                        content.contains(
                            it,
                            ignoreCase = true,
                        )
                    }
                    if (hasOtherBankIndicator) {
                        Timber.d("[$bankName Handler] Файл содержит указания на другой банк")
                        return false
                    }
                    if (hasTinkoffIndicator || hasTableFormat) {
                        Timber.d("[$bankName Handler] Найден индикатор Тинькофф в содержимом файла. Табличный формат: $hasTableFormat")
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w("[$bankName Handler] Ошибка при чтении файла для определения: ${e.message}")
        }
        return hasPositiveKeyword
    }

    /**
     * Создаёт UseCase для импорта PDF-выписки Тинькофф
     */
    override fun createImporter(fileType: FileType): ImportTransactionsUseCase {
        if (supportsFileType(fileType)) {
            Timber.d("[$bankName Handler] Создание TbankPdfImportUseCase")
            return TbankPdfImportUseCase(context, transactionRepository)
        }
        throw IllegalArgumentException("[$bankName Handler] не поддерживает тип файла: $fileType")
    }
}
