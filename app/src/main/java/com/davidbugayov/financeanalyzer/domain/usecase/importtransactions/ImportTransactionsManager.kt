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
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
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
    suspend fun importFromUri(uri: Uri, progressCallback: ImportProgressCallback): ImportResult {
        Timber.d("ImportTransactionsManager - начало importFromUri с URI: $uri")

        // Информация о файле для обработки
        var fileFormat = FileFormat.UNKNOWN
        var fileNameForDiagnostics = ""

        try {
            val mimeType = context.contentResolver.getType(uri)
            Timber.d("MIME-тип файла: $mimeType")

            val fileName = getFileNameFromUri(uri)
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
                                FileFormat.PDF
                            }
                            fileName.endsWith(".xlsx", ignoreCase = true) || fileName.endsWith(".xls", ignoreCase = true) -> {
                                Timber.d("Обнаружен Excel-файл")
                                FileFormat.EXCEL
                            }
                            fileName.endsWith(".csv", ignoreCase = true) -> {
                                Timber.d("Обнаружен CSV-файл")
                                FileFormat.CSV
                            }
                            else -> {
                                Timber.d("Неизвестный формат файла")
                                FileFormat.UNKNOWN
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при определении типа файла: ${e.message}")
        }

        return withContext(Dispatchers.IO) {
            try {
                // Открываем входной поток для чтения файла
                Timber.d("Пытаемся открыть inputStream")
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext ImportResult.error("Не удалось открыть файл")

                Timber.d("inputStream успешно открыт, доступно байт: ${inputStream.available()}")

                // В зависимости от формата выбираем соответствующий обработчик
                val result = when (fileFormat) {
                    FileFormat.PDF -> {
                        progressCallback.onProgress(10, 100, "Обработка PDF файла...")
                        handlePdfFile(uri, progressCallback)
                    }
                    FileFormat.CSV -> {
                        progressCallback.onProgress(10, 100, "Обработка CSV файла...")
                        handleCsvFile(uri, progressCallback)
                    }
                    FileFormat.EXCEL -> {
                        progressCallback.onProgress(10, 100, "Обработка Excel файла...")
                        handleExcelFile(uri, progressCallback)
                    }
                    else -> {
                        // Пробуем определить формат по содержимому
                        val format = detectFormatByContent(inputStream)
                        progressCallback.onProgress(10, 100, "Обработка файла (формат: $format)...")
                        ImportResult.error("Неподдерживаемый или неизвестный формат файла")
                    }
                }

                Timber.d("Закрываем inputStream")
                inputStream.close()

                Timber.d("Возвращаем результат: $result")

                // Заполняем результат с диагностической информацией, если он пустой
                if (result is ImportResult.Success && result.importedCount == 0 && result.skippedCount == 0) {
                    ImportResult.success(0, 0, "Файл обработан: $fileNameForDiagnostics (формат: $fileFormat)")
                } else {
                    result
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Ошибка при импорте файла: ${e.message}")
                ImportResult.error("Ошибка при импорте: ${e.message}")
            }
        }
    }

    /**
     * Обрабатывает PDF-файл
     */
    private fun handlePdfFile(uri: Uri, progressCallback: ImportProgressCallback): ImportResult {
        Timber.d("Обработка PDF-файла")
        try {
            val pdfText = tryReadPdfContent(uri)
            progressCallback.onProgress(50, 100, "Анализ данных из PDF...")
            if (pdfText.isNotEmpty()) {
                Timber.d("Текст из PDF успешно прочитан, длина: ${pdfText.length} символов")
                return ImportResult.success(0, 0, "PDF файл прочитан успешно, но данные не импортированы")
            } else {
                return ImportResult.error("Не удалось извлечь текст из PDF файла")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Ошибка при обработке PDF: ${e.message}")
            return ImportResult.error("Ошибка при обработке PDF: ${e.message}")
        }
    }

    /**
     * Обрабатывает CSV-файл
     */
    private fun handleCsvFile(uri: Uri, progressCallback: ImportProgressCallback): ImportResult {
        Timber.d("Обработка CSV-файла")
        try {
            val csvContent = readCsvContent(uri)
            progressCallback.onProgress(50, 100, "Анализ данных из CSV...")
            if (csvContent.isNotEmpty()) {
                Timber.d("CSV файл успешно прочитан, строк: ${csvContent.size}")
                return ImportResult.success(0, 0, "CSV файл прочитан успешно, но данные не импортированы")
            } else {
                return ImportResult.error("CSV файл пустой или неверного формата")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Ошибка при обработке CSV: ${e.message}")
            return ImportResult.error("Ошибка при обработке CSV: ${e.message}")
        }
    }

    /**
     * Обрабатывает Excel-файл
     */
    private fun handleExcelFile(uri: Uri, progressCallback: ImportProgressCallback): ImportResult {
        Timber.d("Обработка Excel-файла")
        progressCallback.onProgress(50, 100, "Анализ данных из Excel...")
        // Excel пока не поддерживается полностью
        return ImportResult.error("Импорт Excel-файлов временно не поддерживается")
    }

    /**
     * Читает содержимое CSV-файла
     */
    private fun readCsvContent(uri: Uri): List<String> {
        Timber.d("Чтение содержимого CSV-файла")
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
    private fun detectFormatByContent(inputStream: InputStream): FileFormat {
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
                        FileFormat.PDF
                    }
                    content.contains(",") || content.contains(";") -> {
                        Timber.d("По содержимому предположительно CSV")
                        FileFormat.CSV
                    }
                    else -> {
                        Timber.d("Не удалось определить формат по содержимому")
                        FileFormat.UNKNOWN
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при определении формата по содержимому: ${e.message}")
        }

        return FileFormat.UNKNOWN
    }

    /**
     * Пытается прочитать содержимое PDF-файла.
     * Метод только для диагностики проблем.
     *
     * @param uri URI PDF-файла
     * @return Текст из PDF или пустую строку в случае ошибки
     */
    private fun tryReadPdfContent(uri: Uri): String {
        Timber.d("Пытаемся прочитать содержимое PDF-файла")
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

    /**
     * Перечисление форматов файлов
     */
    enum class FileFormat {

        PDF,
        CSV,
        EXCEL,
        UNKNOWN
    }
} 