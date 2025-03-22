package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
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
        // Список всех доступных импортеров
        val importers = listOf(
            SberbankImportUseCase(repository, context),
            TinkoffImportUseCase(repository, context),
            AlfaBankImportUseCase(repository, context),
            OzonImportUseCase(repository, context),
            VTBImportUseCase(repository, context),
            GazprombankImportUseCase(repository, context)
        )

        // Проверяем расширение файла
        val fileName = getFileName(uri)

        // Если это CSV файл, пробуем определить его формат
        if (fileName.endsWith(".csv", ignoreCase = true)) {
            // Пытаемся определить банк по содержимому файла
            context.contentResolver.openInputStream(uri)?.use { _ ->
                // Проверяем каждый импортер на совместимость с файлом
                for (importer in importers) {
                    try {
                        if (isMatchingFormat(importer, uri)) {
                            return importer
                        }
                    } catch (e: Exception) {
                        // Если не удалось проверить формат, пробуем следующий импортер
                    }
                }
            }

            // Если не удалось определить формат, используем общий CSV импортер
            return ImportFromCSVUseCase(repository, context)
        }

        // Для xls/xlsx файлов или если формат не определен, используем общий CSV импортер
        return ImportFromCSVUseCase(repository, context)
    }

    /**
     * Проверяет, соответствует ли файл формату данного импортера.
     *
     * @param importer Импортер для проверки
     * @param uri URI файла для проверки
     * @return true, если файл соответствует формату импортера
     */
    private fun isMatchingFormat(importer: BankImportUseCase, uri: Uri): Boolean {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            return importer.isValidFormat(reader)
        }
        return false
    }

    /**
     * Получает имя файла из URI.
     *
     * @param uri URI файла
     * @return Имя файла
     */
    private fun getFileName(uri: Uri): String {
        // Пробуем получить имя файла из метаданных
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex("_display_name")
                if (displayNameIndex != -1) {
                    return cursor.getString(displayNameIndex)
                }
            }
        }

        // Если не удалось получить имя из метаданных, извлекаем из URI
        val path = uri.path
        return path?.substringAfterLast('/') ?: "unknown.csv"
    }
} 