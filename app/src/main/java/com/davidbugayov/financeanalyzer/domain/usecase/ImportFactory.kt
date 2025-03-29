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
        
        // Список всех доступных импортеров для CSV
        val csvImporters = listOf(
            AlfaBankImportUseCase(repository, context),
        )

        // Проверяем расширение файла
        val fileName = getFileName(uri)
        val fileExtension = fileName.substringAfterLast('.', "").lowercase()
        Timber.d("ИМПОРТ ФАБРИКА: Имя файла: $fileName, расширение: $fileExtension")

        // Обработка PDF файлов
        if (fileExtension == "pdf") {
            // Проверяем содержимое PDF для выбора подходящего импортера
            if (fileName.lowercase().contains("ozon") || fileName.lowercase().contains("озон")) {
                Timber.d("ИМПОРТ ФАБРИКА: Обнаружен PDF файл Озон Банка, используем OzonPdfImportUseCase")
                return OzonPdfImportUseCase(repository, context)
            } else {
                Timber.d("ИМПОРТ ФАБРИКА: Обнаружен PDF файл, используем SberbankPdfImportUseCase")
                // Для PDF файлов по умолчанию используем реализацию для Сбербанка
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
            Timber.d("ИМПОРТ ФАБРИКА: Обнаружен CSV файл, пытаемся определить формат")
            
            // Читаем содержимое файла для определения формата
            try {
                // Открываем поток один раз и закрываем его только в конце проверки всех импортеров
                val content = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.readText()
                    
                if (content != null) {
                    Timber.d("ИМПОРТ ФАБРИКА: Прочитано ${content.length} символов для анализа")
                    
                    // Проверяем каждый импортер на совместимость с контентом
                    for (importer in csvImporters) {
                        try {
                            Timber.d("ИМПОРТ ФАБРИКА: Проверяем совместимость с импортером: ${importer.javaClass.simpleName}")
                            
                            // Создаем новый BufferedReader из строки контента для каждого импортера
                            val reader = content.reader().buffered()
                            val isValid = importer.isValidFormat(reader)
                            reader.close()
                            
                            Timber.d("ИМПОРТ ФАБРИКА: Проверка формата для ${importer.javaClass.simpleName} - ${if (isValid) "СОВМЕСТИМ" else "НЕ СОВМЕСТИМ"}")
                            
                            if (isValid) {
                                Timber.d("ИМПОРТ ФАБРИКА: Найден совместимый импортер: ${importer.javaClass.simpleName}")
                                return importer
                            }
                        } catch (e: Exception) {
                            Timber.w(e, "ИМПОРТ ФАБРИКА: Ошибка при проверке формата для импортера ${importer.javaClass.simpleName}")
                        }
                    }
                } else {
                    Timber.w("ИМПОРТ ФАБРИКА: Не удалось прочитать содержимое файла")
                }
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ ФАБРИКА: Критическая ошибка при чтении файла для анализа формата")
            }

            // Если не удалось определить формат, используем общий CSV импортер
            Timber.d("ИМПОРТ ФАБРИКА: Не удалось определить специфический формат CSV, используем общий ImportFromCSVUseCase")
            return ImportFromCSVUseCase(repository, context)
        }

        // Для xls/xlsx файлов или если формат не определен, используем общий CSV импортер
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
} 