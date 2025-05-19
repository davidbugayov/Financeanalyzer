package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ImportTransactionsUseCase
import timber.log.Timber

/**
 * Абстрактный базовый класс для обработчиков импорта от различных банков.
 * Каждый обработчик отвечает за определение, может ли он обработать данный файл,
 * и за создание соответствующего UseCase для импорта.
 */
abstract class AbstractBankHandler(
    protected val transactionRepository: TransactionRepository,
    protected val context: Context
) {

    /**
     * Уникальное имя банка, которое будет отображаться пользователю и использоваться в логах.
     */
    abstract val bankName: String

    /**
     * Проверяет, поддерживает ли этот обработчик данный тип файла.
     * @param fileType Тип файла.
     * @return true, если тип файла поддерживается.
     */
    abstract fun supportsFileType(fileType: FileType): Boolean

    /**
     * Создает экземпляр ImportTransactionsUseCase, специфичный для этого банка и типа файла.
     * @param fileType Тип файла, для которого создается импортер.
     * @return Экземпляр ImportTransactionsUseCase.
     * @throws IllegalArgumentException если тип файла не поддерживается.
     */
    abstract fun createImporter(fileType: FileType): ImportTransactionsUseCase

    /**
     * Возвращает список ключевых слов, характерных для имен файлов этого банка.
     * Используется в базовой реализации canHandle для проверки по имени файла.
     */
    abstract fun getFileNameKeywords(): List<String>

    /**
     * Определяет, может ли этот обработчик обработать данный файл.
     * Базовая реализация проверяет поддержку типа файла и соответствие имени файла ключевым словам.
     * Наследники должны переопределить этот метод, вызвав super.canHandle() и/или добавив
     * проверки по содержимому файла.
     *
     * @param fileName Имя файла.
     * @param uri URI файла, для возможности чтения содержимого при необходимости.
     * @param fileType Тип файла.
     * @return true, если обработчик может обработать файл.
     */
    open fun canHandle(fileName: String, uri: Uri, fileType: FileType): Boolean {
        if (!supportsFileType(fileType)) {
            return false
        }

        // Проверка по имени файла
        val keywords = getFileNameKeywords()
        val nameMatch = keywords.any { keyword ->
            // Универсальная проверка: для расширений (начинающихся с точки) - endsWith,
            // для остальных ключевых слов - contains.
            if (keyword.startsWith(".")) {
                fileName.endsWith(keyword, ignoreCase = true)
            } else {
                fileName.contains(keyword, ignoreCase = true)
            }
        }

        if (nameMatch) {
            Timber.d("[$bankName Handler] Matched by filename keyword for: $fileName")
            return true // Если имя файла совпало, считаем, что можем обработать (базовая логика)
        }
        // Наследники могут добавить здесь проверку fileContent, если nameMatch == false
        return false
    }
}

abstract class AbstractPdfBankHandler(
    transactionRepository: TransactionRepository,
    context: Context
) : AbstractBankHandler(transactionRepository, context) {

    abstract val pdfKeywords: List<String>
    override fun supportsFileType(fileType: FileType): Boolean = fileType == FileType.PDF
    override fun getFileNameKeywords(): List<String> = pdfKeywords
    override fun canHandle(fileName: String, uri: Uri, fileType: FileType): Boolean {
        return supportsFileType(fileType) && pdfKeywords.any { fileName.contains(it, ignoreCase = true) }
    }
} 