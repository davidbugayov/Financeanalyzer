package com.davidbugayov.financeanalyzer.presentation.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.usecase.ImportFactory
import com.davidbugayov.financeanalyzer.utils.logging.FileLogger
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.io.File

/**
 * ViewModel для экрана импорта транзакций.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 *
 * @property repository Репозиторий транзакций
 * @property context Контекст приложения
 */
class ImportTransactionsViewModel(
    private val repository: TransactionRepositoryImpl,
    private val context: Context
) : ViewModel() {

    /**
     * Импортирует транзакции из URI файла.
     * Автоматически определяет формат файла и использует соответствующий обработчик.
     *
     * @param uri URI файла для импорта
     * @return Flow с результатами импорта
     */
    suspend fun importTransactions(uri: Uri): Flow<ImportResult> {
        val importFactory = ImportFactory(repository, context)
        val importer = importFactory.createImporter(uri)
        return importer(uri)
    }
    
    /**
     * Импортирует тестовый файл из ресурсов для отладки.
     * Создает временный файл из ресурса и импортирует его.
     * 
     * @param resId Идентификатор ресурса с тестовым файлом
     * @return Flow с результатами импорта
     */
    suspend fun importTestFile(resId: Int): Flow<ImportResult> {
        Timber.d("Импорт тестового файла из ресурса: $resId")
        
        // Создаем временный файл
        val tempFile = File(context.cacheDir, "test_import_${System.currentTimeMillis()}.csv")
        context.resources.openRawResource(resId).use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        Timber.d("Тестовый файл создан: ${tempFile.absolutePath}")
        
        // Конвертируем в URI и импортируем
        val uri = Uri.fromFile(tempFile)
        
        // Начинаем импорт с последующей очисткой
        try {
            val importFactory = ImportFactory(repository, context)
            val importer = importFactory.createImporter(uri)
            Timber.d("Для тестового файла выбран импортер: ${importer.javaClass.simpleName}")
            return importer(uri)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при импорте тестового файла")
            throw e
        } finally {
            // Удаляем временный файл после импорта
            if (tempFile.exists()) {
                val deleted = tempFile.delete()
                Timber.d("Временный файл удален: $deleted")
            }
        }
    }
    
    /**
     * Получает список логов из FileLogger.
     * 
     * @param limit Количество последних логов (по умолчанию 100)
     * @return Список строк с логами
     */
    suspend fun getLogs(limit: Int = 100): List<String> {
        return FileLogger.getLastLogs(limit).map { it.toString() }
    }
} 