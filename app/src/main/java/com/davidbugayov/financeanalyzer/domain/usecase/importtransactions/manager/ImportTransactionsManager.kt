package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.manager

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.BankImportUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportProgressCallback
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportResult
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.factory.ImportFactory
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import timber.log.Timber
import java.io.InputStream

/**
 * Менеджер для импорта транзакций из различных источников.
 * Централизует логику выбора подходящего обработчика на основе типа файла.
 */
class ImportTransactionsManager(
    private val context: Context
) : KoinComponent {

    private val importFactory: ImportFactory = get()

    init {
        try {
            // Инициализируем PDFBox при создании менеджера
            PDFBoxResourceLoader.init(context)
            Timber.d("PDFBox успешно инициализирован")
        } catch (e: Exception) {
            Timber.e(e, "❌ Ошибка инициализации PDFBox: ${e.message}")
        }
    }

    /**
     * Импортирует транзакции из файла, указанного по URI.
     *
     * @param uri URI файла для импорта
     * @param progressCallback Колбэк для отслеживания прогресса
     * @return Результат импорта
     */
    fun importFromUri(uri: Uri, progressCallback: ImportProgressCallback): Flow<ImportResult> =
        flow {
            Timber.d("ImportTransactionsManager - начало importFromUri с URI: $uri")

            var fileFormat = FileType.UNKNOWN
            var fileName = ""
            var fileNameForDiagnostics = ""

            try {
                val mimeType = context.contentResolver.getType(uri)
                Timber.d("MIME-тип файла: $mimeType")

                fileName = getFileNameFromUri(uri)
                Timber.d("Имя файла из getFileNameFromUri: $fileName")
                fileNameForDiagnostics = fileName

                // Определение типа файла
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (displayNameIndex != -1) {
                            val fileName = cursor.getString(displayNameIndex)
                            Timber.d("Имя файла из OpenableColumns: $fileName")
                            fileNameForDiagnostics = fileName

                            // Определяем формат по расширению
                            fileFormat = when {
                                fileName.endsWith(".pdf", ignoreCase = true) -> {
                                    Timber.d("Обнаружен PDF-файл")
                                    FileType.PDF
                                }
                                fileName.endsWith(".xlsx", ignoreCase = true) || fileName.endsWith(
                                    ".xls",
                                    ignoreCase = true
                                ) -> {
                                    Timber.d("Обнаружен Excel-файл")
                                    FileType.EXCEL
                                }
                                fileName.endsWith(".csv", ignoreCase = true) -> {
                                    Timber.d("Обнаружен CSV-файл")
                                    FileType.CSV
                                }
                                else -> {
                                    Timber.d("Неизвестный формат файла")
                                    FileType.UNKNOWN
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при определении типа файла: ${e.message}")
            }

            if (fileFormat == FileType.UNKNOWN) {
                Timber.d(
                    "Формат не определен по расширению, пытаемся определить по содержимому URI: $uri"
                )
                val successfullyDetected = try {
                    // Используем flow с flowOn вместо withContext для согласования контекстов
                    val detectedFormat = flow<FileType> {
                        context.contentResolver.openInputStream(uri)?.use { streamForDetection ->
                            emit(detectFormatByContent(streamForDetection))
                        } ?: emit(FileType.UNKNOWN)
                    }.flowOn(Dispatchers.IO)

                    // Собираем одно значение из потока
                    val formatResult = mutableListOf<FileType>()
                    detectedFormat.collect { format ->
                        formatResult.add(format)
                    }

                    if (formatResult.isNotEmpty()) {
                        fileFormat = formatResult.first()
                        Timber.d("Формат после detectFormatByContent: $fileFormat")
                        true
                    } else {
                        Timber.e(
                            "Не удалось открыть файл для определения формата по содержимому URI: $uri"
                        )
                        emit(ImportResult.error("Не удалось открыть файл для определения формата"))
                        false
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Ошибка при определении формата по содержимому: ${e.message}")
                    emit(
                        ImportResult.error(
                            "Ошибка при определении формата файла по содержимому: ${e.message}"
                        )
                    )
                    false
                }
                if (!successfullyDetected) return@flow
            }

            if (fileFormat == FileType.UNKNOWN) {
                Timber.w(
                    "Не удалось определить формат файла для URI: $uri (имя: $fileNameForDiagnostics)"
                )
                emit(ImportResult.error("Не удалось определить формат файла."))
                return@flow
            }

            // Используем flow с flowOn вместо withContext для получения импортера
            val importerFlow = flow {
                Timber.d(
                    "Попытка создать импортер через ImportFactory. Файл: $fileNameForDiagnostics, Тип: $fileFormat, URI: $uri"
                )
                emit(importFactory.getImporter(fileNameForDiagnostics, uri, fileFormat))
            }.flowOn(Dispatchers.Default)

            // Собираем одно значение из потока
            val importerResults = mutableListOf<ImportTransactionsUseCase?>()
            importerFlow.collect { importer ->
                importerResults.add(importer)
            }

            val importerUseCase = if (importerResults.isNotEmpty()) importerResults.first() else null

            if (importerUseCase == null) {
                Timber.e(
                    "Не удалось создать импортер для файла: $fileNameForDiagnostics (тип: $fileFormat). Подходящий BankHandler не найден или не поддерживает этот тип файла."
                )
                emit(
                    ImportResult.error(
                        "Не найден подходящий обработчик для импорта файла $fileNameForDiagnostics."
                    )
                )
                return@flow
            }

            Timber.i(
                "Используется импортер: ${(importerUseCase as? BankImportUseCase)?.bankName ?: importerUseCase::class.simpleName} для файла $fileNameForDiagnostics"
            )

            // Использование flowOn для согласования контекста эмиссии
            val importFlow = importerUseCase.importTransactions(uri, progressCallback)
                .flowOn(Dispatchers.IO)

            emitAll(importFlow)
        }.flowOn(Dispatchers.IO) // Обеспечиваем правильный контекст для всех операций в потоке

    /**
     * Пытается определить формат файла по его содержимому
     */
    private fun detectFormatByContent(inputStream: InputStream): FileType {
        try {
            // Сохраняем позицию в потоке
            val markSupported = inputStream.markSupported()
            if (markSupported) {
                inputStream.mark(100)
            }

            val bytes = ByteArray(50)
            val bytesRead = inputStream.read(bytes)

            // Восстанавливаем позицию в потоке
            if (markSupported) {
                inputStream.reset()
            }

            if (bytesRead > 0) {
                val content = String(bytes, 0, bytesRead)
                return when {
                    content.startsWith("%PDF-") -> {
                        Timber.d("По содержимому определен PDF")
                        FileType.PDF
                    }
                    content.contains(",") || content.contains(";") -> {
                        Timber.d("По содержимому предположительно CSV")
                        FileType.CSV
                    }
                    else -> {
                        Timber.d("Не удалось определить формат по содержимому")
                        FileType.UNKNOWN
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при определении формата по содержимому: ${e.message}")
        }

        return FileType.UNKNOWN
    }

    /**
     * Извлекает имя файла из URI.
     *
     * @param uri URI файла
     * @return Имя файла или пустая строка
     */
    private fun getFileNameFromUri(uri: Uri): String {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex("_display_name")
                if (displayNameIndex >= 0) {
                    return it.getString(displayNameIndex)
                }
            }
        }

        // Если не удалось определить имя через cursor, пытаемся извлечь из path
        val path = uri.path
        if (path != null) {
            val slash = path.lastIndexOf('/')
            if (slash >= 0 && slash < path.length - 1) {
                return path.substring(slash + 1)
            }
        }

        return ""
    }
} 
