package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import timber.log.Timber

/**
 * Базовый класс для обработчиков Excel-файлов различных банков.
 * Предоставляет общую функциональность для всех Excel-обработчиков.
 */
abstract class AbstractExcelBankHandler(
    transactionRepository: TransactionRepository,
    context: Context
) : AbstractBankHandler(transactionRepository, context) {

    /**
     * Список ключевых слов для определения Excel-файлов конкретного банка.
     * Должен быть переопределен в наследниках для каждого банка.
     */
    abstract val excelKeywords: List<String>

    /**
     * Проверяет, поддерживает ли обработчик тип файла Excel.
     */
    override fun supportsFileType(fileType: FileType): Boolean = fileType == FileType.EXCEL

    /**
     * Возвращает список ключевых слов для поиска по имени файла.
     */
    override fun getFileNameKeywords(): List<String> = excelKeywords

    /**
     * Базовая реализация проверки, может ли обработчик обработать файл.
     * Проверяет тип файла и наличие ключевых слов в имени файла.
     * Наследники могут переопределить этот метод для более сложной логики.
     */
    override fun canHandle(fileName: String, uri: Uri, fileType: FileType): Boolean {
        if (!supportsFileType(fileType)) {
            Timber.d("[$bankName Handler] Не поддерживает тип файла: $fileType")
            return false
        }

        val hasKeyword = excelKeywords.any { fileName.lowercase().contains(it.lowercase()) }
        if (hasKeyword) {
            Timber.d("[$bankName Handler] Найдено ключевое слово в имени файла: $fileName")
            return true
        }

        // Если ключевые слова не найдены в имени файла, наследники могут
        // реализовать дополнительную проверку содержимого файла
        return false
    }
} 
