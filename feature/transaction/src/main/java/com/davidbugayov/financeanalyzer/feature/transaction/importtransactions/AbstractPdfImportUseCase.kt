package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.BankImportUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportProgressCallback
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportResult
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class AbstractPdfImportUseCase(
    context: Context,
    transactionRepository: TransactionRepository,
) : BankImportUseCase(transactionRepository, context) {
    open val headerMarkers: List<String> by lazy {
        listOf(
            context.getString(R.string.header_date_operation),
            context.getString(R.string.header_date),
            context.getString(R.string.header_operation),
            context.getString(R.string.header_document),
            context.getString(R.string.header_amount),
        )
    }
    open val dataStartRegex: Regex = Regex("^\\d{2}\\.\\d{2}\\.\\d{4}")

    /**
     * Безопасно форматирует сообщение об ошибке
     */
    private fun formatErrorMessage(
        resourceId: Int,
        bankName: String,
        errorMessage: String?,
    ): String {
        return try {
            // Список ресурсов строк, которые принимают два параметра
            val twoParamResources =
                listOf(
                    R.string.import_error_unknown,
                    R.string.import_error_io_exception,
                    R.string.import_error_pdf_extraction_exception,
                )

            if (resourceId in twoParamResources) {
                // Обработка строк, которые используют два параметра
                context.getString(resourceId, bankName, errorMessage ?: "Неизвестная ошибка")
            } else {
                // Для строк с одним параметром
                context.getString(resourceId, bankName)
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при форматировании сообщения об ошибке: $resourceId, $bankName, $errorMessage")
            "Ошибка при импорте файла $bankName: ${errorMessage ?: "Неизвестная ошибка"}"
        }
    }

    /**
     * Извлекает текст из PDF-файла по URI
     */
    protected open suspend fun extractTextFromPdf(uri: Uri): String =
        withContext(Dispatchers.IO) {
            Timber.d("$bankName extractTextFromPdf: Начало извлечения текста из URI: $uri")
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    inputStream.use { stream ->
                        PDDocument.load(stream).use { document ->
                            val stripper = PDFTextStripper()
                            val text = stripper.getText(document)
                            Timber.i(
                                "$bankName extractTextFromPdf: Текст успешно извлечен. Длина: ${text.length}, Первые 100 символов: ${text.take(
                                    100,
                                )}",
                            )
                            return@withContext text
                        }
                    }
                } else {
                    Timber.w(
                        "$bankName extractTextFromPdf: Не удалось открыть InputStream для PDF: $uri",
                    )
                    return@withContext ""
                }
            } catch (e: Exception) {
                Timber.e(e, "$bankName extractTextFromPdf: Ошибка при извлечении текста из PDF.")
                throw IOException(
                    formatErrorMessage(
                        R.string.import_error_pdf_extraction_exception,
                        bankName,
                        e.localizedMessage,
                    ),
                    e,
                )
            }
        }

    /**
     * Шаблонный метод импорта PDF-файлов (общий для всех PDF-банков)
     */
    override fun importTransactions(
        uri: Uri,
        progressCallback: ImportProgressCallback,
    ): Flow<ImportResult> =
        flow {
            val currentBankName = bankName
            emit(
                ImportResult.Progress(
                    0,
                    100,
                    context.getString(R.string.import_progress_starting, currentBankName),
                ),
            )
            Timber.i("$currentBankName importTransactions: Начало импорта из URI: $uri")
            try {
                val extractedText = extractTextFromPdf(uri)
                if (extractedText.isBlank()) {
                    Timber.e("$currentBankName importTransactions: Извлеченный текст из PDF пуст.")
                    emit(
                        ImportResult.Error(
                            message =
                                formatErrorMessage(
                                    R.string.import_error_pdf_extraction_failed,
                                    currentBankName,
                                    null,
                                ),
                        ),
                    )
                    return@flow
                }
                emit(
                    ImportResult.Progress(
                        5,
                        100,
                        context.getString(R.string.import_progress_text_extracted_validating, currentBankName),
                    ),
                )
                Timber.d(
                    "$currentBankName importTransactions: Извлеченный текст, первые 500 символов:\n${extractedText.take(
                        500,
                    )}...",
                )

                BufferedReader(StringReader(extractedText)).use { validationReader ->
                    if (!isValidFormat(validationReader)) {
                        Timber.e(
                            "$currentBankName importTransactions: Файл не прошел валидацию как выписка $currentBankName.",
                        )
                        emit(
                            ImportResult.Error(
                                message =
                                    formatErrorMessage(
                                        R.string.import_error_invalid_format,
                                        currentBankName,
                                        null,
                                    ),
                            ),
                        )
                        return@flow
                    }
                }
                Timber.i("$currentBankName importTransactions: Формат файла успешно валидирован.")
                emit(
                    ImportResult.Progress(
                        10,
                        100,
                        context.getString(R.string.import_progress_format_validated_skipping_headers, currentBankName),
                    ),
                )

                BufferedReader(StringReader(extractedText)).use { contentReader ->
                    skipHeaders(contentReader)
                    Timber.i(
                        "$currentBankName importTransactions: Заголовки пропущены, начинаем обработку транзакций.",
                    )
                    emit(
                        ImportResult.Progress(
                            15,
                            100,
                            context.getString(R.string.import_progress_processing_transactions, currentBankName),
                        ),
                    )

                    val transactions = parseTransactions(contentReader, progressCallback, extractedText)
                    if (transactions.isEmpty()) {
                        Timber.w(
                            "$currentBankName importTransactions: Транзакции не найдены после обработки файла.",
                        )
                        emit(
                            ImportResult.Error(
                                message =
                                    formatErrorMessage(
                                        R.string.import_error_no_transactions_found,
                                        currentBankName,
                                        null,
                                    ),
                            ),
                        )
                        return@flow
                    }
                    emit(
                        ImportResult.Progress(
                            85,
                            100,
                            context.getString(
                                R.string.import_progress_saving_transactions,
                                transactions.size,
                                currentBankName,
                            ),
                        ),
                    )
                    Timber.i(
                        "$currentBankName importTransactions: Найдено ${transactions.size} транзакций. Начинаем сохранение в базу данных",
                    )
                    var savedCount = 0
                    transactions.forEach { transaction ->
                        try {
                            transactionRepository.addTransaction(transaction)
                            savedCount++
                        } catch (e: Exception) {
                            Timber.e(
                                e,
                                "$currentBankName importTransactions: Ошибка при сохранении транзакции: ${transaction.title}",
                            )
                            CrashLoggerProvider.crashLogger.logDatabaseError(
                                "importTransactions",
                                "Ошибка при сохранении транзакции: ${transaction.title}",
                                e,
                            )
                        }
                    }
                    Timber.i(
                        "$currentBankName importTransactions: Импорт завершен. Сохранено: $savedCount из ${transactions.size}",
                    )
                    emit(ImportResult.Success(savedCount, transactions.size - savedCount, bankName = currentBankName))
                }
            } catch (e: IOException) {
                Timber.e(e, "$currentBankName importTransactions: Ошибка ввода-вывода.")
                emit(
                    ImportResult.Error(
                        exception = e,
                        message =
                            formatErrorMessage(
                                R.string.import_error_io_exception,
                                currentBankName,
                                e.localizedMessage,
                            ),
                    ),
                )
            } catch (e: Exception) {
                Timber.e(e, "$currentBankName importTransactions: Непредвиденная ошибка.")
                emit(
                    ImportResult.Error(
                        exception = e,
                        message =
                            formatErrorMessage(
                                R.string.import_error_unknown,
                                currentBankName,
                                e.localizedMessage,
                            ),
                    ),
                )
            }
        }

    /**
     * Абстрактный метод для парсинга транзакций из BufferedReader
     */
    protected abstract fun parseTransactions(
        reader: BufferedReader,
        progressCallback: ImportProgressCallback,
        rawText: String,
    ): List<com.davidbugayov.financeanalyzer.domain.model.Transaction>

    override fun isValidFormat(reader: BufferedReader): Boolean {
        val headerLines = mutableListOf<String>()
        reader.mark(8192)
        repeat(25) {
            val line = reader.readLine()?.replace("\u0000", "") ?: return@repeat
            headerLines.add(line)
        }
        reader.reset()
        val content = headerLines.joinToString("\n")
        return headerMarkers.any { marker -> content.contains(marker, ignoreCase = true) }
    }

    override fun skipHeaders(reader: BufferedReader) {
        var line: String?
        while (true) {
            reader.mark(1024)
            line = reader.readLine()?.replace("\u0000", "")
            if (line == null) break
            if (dataStartRegex.containsMatchIn(line)) {
                reader.reset()
                break
            }
        }
    }
}
