package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.davidbugayov.financeanalyzer.domain.model.ImportProgressCallback
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

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
    fun importFromUri(uri: Uri, progressCallback: ImportProgressCallback): Flow<ImportResult> = flow {
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
                            fileName.endsWith(".xlsx", ignoreCase = true) || fileName.endsWith(".xls", ignoreCase = true) -> {
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
            Timber.d("Формат не определен по расширению, пытаемся определить по содержимому URI: $uri")
            val successfullyDetected = try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { streamForDetection ->
                        fileFormat = detectFormatByContent(streamForDetection)
                        Timber.d("Формат после detectFormatByContent: $fileFormat")
                        true
                    } ?: run {
                        Timber.e("Не удалось открыть файл для определения формата по содержимому URI: $uri")
                        emit(ImportResult.error("Не удалось открыть файл для определения формата"))
                        false
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при определении формата по содержимому: ${e.message}")
                emit(ImportResult.error("Ошибка при определении формата файла по содержимому: ${e.message}"))
                false
            }
            if (!successfullyDetected) return@flow
        }

        if (fileFormat == FileType.UNKNOWN) {
            Timber.w("Не удалось определить формат файла для URI: $uri (имя: $fileNameForDiagnostics)")
            emit(ImportResult.error("Не удалось определить формат файла."))
            return@flow
        }

        val importerUseCase = withContext(Dispatchers.Default) {
            Timber.d("Попытка создать импортер через ImportFactory. Файл: $fileNameForDiagnostics, Тип: $fileFormat, URI: $uri")
            importFactory.getImporter(fileNameForDiagnostics, uri, fileFormat)
        }

        if (importerUseCase == null) {
            Timber.e("Не удалось создать импортер для файла: $fileNameForDiagnostics (тип: $fileFormat). Подходящий BankHandler не найден или не поддерживает этот тип файла.")
            emit(ImportResult.error("Не найден подходящий обработчик для импорта файла $fileNameForDiagnostics."))
            return@flow
        }

        Timber.i("Используется импортер: ${(importerUseCase as? BankImportUseCase)?.bankName ?: importerUseCase::class.simpleName} для файла $fileNameForDiagnostics")
        emitAll(importerUseCase.importTransactions(uri, progressCallback))
    }

    /**
     * Читает содержимое CSV-файла
     */
    private fun readCsvContent(uri: Uri): List<String> {
        Timber.d("Чтение содержимого CSV-файла (readCsvContent - возможно, больше не используется активно)")
        val lines = mutableListOf<String>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    var lineCount = 0
                    while (reader.readLine().also { line = it } != null && lineCount < 10) {
                        line?.let {
                            lines.add(it)
                            lineCount++
                        }
                    }
                    Timber.d("Прочитано $lineCount строк CSV")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Ошибка при чтении CSV: ${e.message}")
        }

        return lines
    }

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
     * Пытается прочитать содержимое PDF-файла.
     * Метод только для диагностики проблем.
     *
     * @param uri URI PDF-файла
     * @return Текст из PDF или пустую строку в случае ошибки
     */
    private fun tryReadPdfContent(uri: Uri): String {
        Timber.d("Пытаемся прочитать содержимое PDF-файла (tryReadPdfContent - возможно, больше не используется активно)")
        try {
            // Повторно инициализируем PDFBox для надежности
            try {
                PDFBoxResourceLoader.init(context)
                Timber.d("PDFBox инициализирован повторно")
            } catch (e: Exception) {
                Timber.e(e, "❌ Ошибка при повторной инициализации PDFBox: ${e.message}")
            }

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                Timber.d("Открыт inputStream для PDF, доступно байт: ${inputStream.available()}")

                try {
                    // Пытаемся проверить первые байты файла
                    val header = ByteArray(5)
                    val bytesRead = inputStream.read(header)
                    val headerStr = String(header)
                    Timber.d("Прочитано $bytesRead байт, заголовок: $headerStr")

                    // Проверяем, действительно ли это PDF (должен начинаться с "%PDF-")
                    if (!headerStr.startsWith("%PDF-")) {
                        Timber.e("❌ Файл не является PDF (неверный заголовок): $headerStr")
                        return ""
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Ошибка при чтении заголовка PDF: ${e.message}")
                }
            }

            // Открываем новый поток для чтения всего документа
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                try {
                    Timber.d("Начинаем загрузку PDF документа")
                    val document = PDDocument.load(inputStream)
                    val pageCount = document.numberOfPages
                    Timber.d("PDF документ загружен успешно, количество страниц: $pageCount")

                    // Ограничиваем чтение только первой страницей
                    val textStripper = PDFTextStripper()
                    textStripper.startPage = 1
                    textStripper.endPage = 1

                    Timber.d("Начинаем извлечение текста только из первой страницы PDF")
                    val text = try {
                        textStripper.getText(document)
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Ошибка при вызове textStripper.getText: ${e.message}")
                        ""
                    }

                    if (text.isNotEmpty()) {
                        Timber.d("Текст успешно извлечен из первой страницы, длина: ${text.length}")
                    } else {
                        Timber.e("❌ Извлеченный текст пустой")
                    }

                    try {
                        document.close()
                        Timber.d("PDF документ успешно закрыт")
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Ошибка при закрытии документа: ${e.message}")
                    }

                    return text
                } catch (e: Exception) {
                    Timber.e(e, "❌ Ошибка при извлечении текста из PDF: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Общая ошибка при чтении PDF: ${e.message}")
        }

        return ""
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