package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Фабрика для создания соответствующих обработчиков импорта в зависимости от типа файла.
 * Определяет тип файла и возвращает подходящий UseCase для импорта транзакций.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class ImportFactory(
    private val repository: TransactionRepositoryImpl,
    private val context: Context
) {
    /**
     * Создает подходящий обработчик импорта на основе содержимого файла.
     *
     * @param uri URI файла для импорта
     * @return Подходящий UseCase для импорта транзакций
     */
    fun createImporter(uri: Uri): ImportTransactionsUseCase {
        Timber.d("ИМПОРТ ФАБРИКА: Начинаем выбор импортера для URI: $uri")

        // Проверяем расширение файла
        val fileName = getFileName(uri)
        val fileExtension = fileName.substringAfterLast('.', "").lowercase()
        Timber.d("ИМПОРТ ФАБРИКА: Имя файла: $fileName, расширение: $fileExtension")

        // Обработка PDF файлов
        if (fileExtension == "pdf") {
            try {
                // Попытаемся прочитать начальный фрагмент PDF для определения формата
                val pdfContent = readPDFContentPreview(uri)
                
                // Проверяем содержимое для выбора подходящего импортера
                if (fileName.lowercase().contains("ozon") || fileName.lowercase().contains("озон") ||
                    pdfContent.contains("OZON", ignoreCase = true) || pdfContent.contains("ОЗОН", ignoreCase = true)) {
                    Timber.d("ИМПОРТ ФАБРИКА: Обнаружен PDF файл Озон Банка, используем OzonPdfImportUseCase")
                    return OzonPdfImportUseCase(repository, context)
                } else if (pdfContent.contains("Сбербанк", ignoreCase = true) || 
                           pdfContent.contains("Расшифровка операций", ignoreCase = true) ||
                           pdfContent.contains("СБЕР", ignoreCase = true) ||
                           pdfContent.contains("ДАТА ОПЕРАЦИИ", ignoreCase = true) ||
                           pdfContent.contains("Дата обработки", ignoreCase = true) ||
                           pdfContent.contains("СУММА В ВАЛЮТЕ СЧЁТА", ignoreCase = true)) {
                    Timber.d("ИМПОРТ ФАБРИКА: Обнаружен PDF файл Сбербанка, используем SberbankPdfImportUseCase")
                    return SberbankPdfImportUseCase(repository, context)
                } else if (fileName.lowercase().contains("tinkoff") || 
                           fileName.lowercase().contains("тинькофф") || 
                           fileName.lowercase().contains("т-банк") ||
                           pdfContent.contains("Tinkoff", ignoreCase = true) ||
                           pdfContent.contains("Тинькофф", ignoreCase = true) ||
                           pdfContent.contains("Т-Банк", ignoreCase = true) ||
                           pdfContent.contains("ТБАНК", ignoreCase = true) ||
                           pdfContent.contains("TBANK.RU", ignoreCase = true) ||
                           pdfContent.contains("АКЦИОНЕРНОЕ ОБЩЕСТВО «ТБАНК»", ignoreCase = true) ||
                           pdfContent.contains("Справка о движении средств", ignoreCase = true) ||
                           (pdfContent.contains("Движение средств за период", ignoreCase = true) && 
                            pdfContent.contains("Дата и время операции", ignoreCase = true)) ||
                           (pdfContent.contains("Внутрибанковский перевод", ignoreCase = true) && 
                            pdfContent.contains("с договора", ignoreCase = true))) {
                    Timber.d("ИМПОРТ ФАБРИКА: Обнаружен PDF файл Т-Банка, используем TBankPdfImportUseCase")
                    return TBankPdfImportUseCase(repository, context)
                } else {
                    Timber.d("ИМПОРТ ФАБРИКА: PDF файл неизвестного формата, используем SberbankPdfImportUseCase")
                    // Для PDF файлов по умолчанию используем реализацию для Сбербанка
                    return SberbankPdfImportUseCase(repository, context)
                }
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ ФАБРИКА: Ошибка при чтении PDF-файла, используем SberbankPdfImportUseCase по умолчанию")
                return SberbankPdfImportUseCase(repository, context)
            }
        }
        
        // Проверяем, если это Excel файл - используем AlfaBankImportUseCase для всех XLSX
        if (fileExtension == "xlsx") {
            Timber.d("ИМПОРТ ФАБРИКА: Обнаружен XLSX файл, используем AlfaBankImportUseCase")
            return AlfaBankImportUseCase(repository, context)
        }

        // Если это CSV файл, пробуем определить его формат
        if (fileExtension == "csv") {
            Timber.d("ИМПОРТ ФАБРИКА: Обнаружен CSV файл, используем ImportFromCSVUseCase")
            return ImportFromCSVUseCase(repository, context)
        }

        // Для других форматов файлов или если формат не определен, используем общий CSV импортер
        Timber.d("ИМПОРТ ФАБРИКА: Неизвестный формат файла или формат не определен, используем общий ImportFromCSVUseCase")
        return ImportFromCSVUseCase(repository, context)
    }

    /**
     * Получает имя файла из URI.
     *
     * @param uri URI файла
     * @return Имя файла
     */
    private fun getFileName(uri: Uri): String {
        // Пробуем получить имя файла из метаданных
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex("_display_name")
                    if (displayNameIndex != -1) {
                        val name = cursor.getString(displayNameIndex)
                        Timber.d("ИМПОРТ ФАБРИКА: Получено имя файла из метаданных: $name")
                        return name
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "ИМПОРТ ФАБРИКА: Ошибка при получении имени файла из метаданных")
        }

        // Если не удалось получить имя из метаданных, извлекаем из URI
        val path = uri.path
        val fileName = path?.substringAfterLast('/') ?: "unknown.csv"
        Timber.d("ИМПОРТ ФАБРИКА: Получено имя файла из URI: $fileName")
        return fileName
    }

    /**
     * Читает первые несколько строк PDF-файла для определения формата
     */
    private fun readPDFContentPreview(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return ""
        
        try {
            // Инициализация PDFBox при необходимости
            com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(context)
            
            val document = com.tom_roush.pdfbox.pdmodel.PDDocument.load(inputStream)
            val stripper = com.tom_roush.pdfbox.text.PDFTextStripper()
            stripper.startPage = 1
            stripper.endPage = 1 // Читаем только первую страницу
            
            val pdfText = stripper.getText(document)
            document.close()
            inputStream.close()
            
            // Возвращаем первые 1000 символов или меньше
            return if (pdfText.length > 1000) pdfText.substring(0, 1000) else pdfText
        } catch (e: Exception) {
            Timber.e(e, "ИМПОРТ ФАБРИКА: Ошибка при чтении превью PDF: ${e.message}")
            inputStream.close()
            return ""
        }
    }
} 