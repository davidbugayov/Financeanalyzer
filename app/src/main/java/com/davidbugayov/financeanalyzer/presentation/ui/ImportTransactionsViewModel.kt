package com.davidbugayov.financeanalyzer.presentation.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.usecase.ImportFactory
import com.davidbugayov.financeanalyzer.utils.logging.FileLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

/**
 * ViewModel для экрана импорта транзакций.
 * Управляет процессом импорта и отображает прогресс и результаты.
 *
 * @property repository Репозиторий транзакций
 * @property context Контекст приложения
 */
class ImportTransactionsViewModel(
    private val repository: TransactionRepositoryImpl,
    private val context: Context
) : ViewModel() {

    /**
     * Состояние UI для экрана импорта
     */
    data class ImportUiState(
        val isLoading: Boolean = false,
        val progress: Int = 0,
        val totalCount: Int = 0,
        val currentStep: String = "",
        val successCount: Int = 0,
        val skippedCount: Int = 0,
        val error: String? = null,
        val isImportCompleted: Boolean = false,
        val totalAmount: Double = 0.0,
        val logs: List<String> = emptyList()
    )

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    private val importFactory = ImportFactory(repository, context)

    /**
     * Начинает процесс импорта транзакций из выбранного файла.
     *
     * @param uri URI выбранного файла
     */
    fun startImport(uri: Uri) {
        Timber.d("ImportTransactionsViewModel: Начинаем импорт из URI: $uri")
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        error = null,
                        isImportCompleted = false,
                        progress = 0,
                        totalCount = 0,
                        successCount = 0,
                        skippedCount = 0,
                        totalAmount = 0.0,
                        currentStep = "Анализ файла..."
                    )
                }
                Timber.d("ImportTransactionsViewModel: Состояние UI сброшено, начинаем анализ файла")

                // Создаем подходящий импортер и начинаем импорт
                try {
                    val importer = importFactory.createImporter(uri)
                    Timber.d("ImportTransactionsViewModel: Импортер создан: ${importer.javaClass.simpleName}")
                    
                    importer(uri).collect { result ->
                        Timber.d("ImportTransactionsViewModel: Получен результат импорта: $result")
                        when (result) {
                            is ImportResult.Progress -> handleProgress(result)
                            is ImportResult.Success -> handleSuccess(result)
                            is ImportResult.Error -> handleError(result)
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "ImportTransactionsViewModel: Ошибка при создании импортера или выполнении импорта")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Ошибка импорта: ${e.message ?: "Неизвестная ошибка"}",
                            isImportCompleted = true
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "ImportTransactionsViewModel: Неперехваченная ошибка в процессе импорта")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка импорта: ${e.message ?: "Неизвестная ошибка"}",
                        isImportCompleted = true
                    )
                }
            }
        }
    }

    /**
     * Обрабатывает прогресс импорта.
     */
    private fun handleProgress(progress: ImportResult.Progress) {
        Timber.d("ImportTransactionsViewModel: Обработка прогресса: ${progress.current}/${progress.total} - ${progress.message}")
        _uiState.update { state ->
            state.copy(
                progress = progress.current,
                totalCount = progress.total,
                currentStep = progress.message
            )
        }
    }

    /**
     * Обрабатывает успешное завершение импорта.
     */
    private fun handleSuccess(success: ImportResult.Success) {
        Timber.d("ImportTransactionsViewModel: Импорт успешно завершен. Импортировано: ${success.importedCount}, пропущено: ${success.skippedCount}, общая сумма: ${success.totalAmount}")
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                isImportCompleted = true,
                successCount = success.importedCount,
                skippedCount = success.skippedCount,
                totalAmount = success.totalAmount,
                currentStep = "Импорт завершен"
            )
        }
    }

    /**
     * Обрабатывает ошибку импорта.
     */
    private fun handleError(error: ImportResult.Error) {
        Timber.e(error.exception, "ImportTransactionsViewModel: Ошибка импорта: ${error.message}")
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                isImportCompleted = true,
                error = error.message
            )
        }
    }

    /**
     * Сбрасывает состояние для нового импорта.
     */
    fun resetState() {
        Timber.d("ImportTransactionsViewModel: Сброс состояния импорта")
        _uiState.update { ImportUiState() }
    }
    
    /**
     * Обновляет логи в состоянии UI.
     * Вызывайте эту функцию, чтобы получить обновленные логи.
     */
    fun refreshLogs() {
        Timber.d("ImportTransactionsViewModel: Обновление логов в UI")
        val logs = FileLogger.getLastLogs(100).map { it.toString() }
        _uiState.update { state ->
            state.copy(
                logs = logs
            )
        }
    }
    
    /**
     * Возвращает путь к файлу логов.
     */
    fun getLogFilePath(): String? {
        return FileLogger.getLogFilePath()
    }
    
    /**
     * Возвращает информацию о проблемах и ошибках для отладки.
     */
    fun getDebugInfo(): String {
        return """
            Версия приложения: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}
            Файл логов: ${getLogFilePath()}
            Последние ошибки: 
            ${FileLogger.getLastLogs(20).filter { it.level == "E" || it.level == "W" }.joinToString("\n")}
        """.trimIndent()
    }
    
    /**
     * Импортирует тестовый файл из ресурсов приложения.
     * Это упрощает тестирование и отладку функций импорта.
     * 
     * @param resId идентификатор ресурса (R.raw.test_file)
     */
    fun importTestFile(resId: Int) {
        Timber.d("ImportTransactionsViewModel: Импорт тестового файла из ресурсов, resId: $resId")
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        error = null,
                        isImportCompleted = false,
                        progress = 0,
                        totalCount = 0,
                        successCount = 0,
                        skippedCount = 0,
                        totalAmount = 0.0,
                        currentStep = "Подготовка тестового файла..."
                    )
                }
                
                // Создаем временный файл из ресурса
                val tempFile = File(context.cacheDir, "test_import_${System.currentTimeMillis()}.csv")
                context.resources.openRawResource(resId).use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                Timber.d("ImportTransactionsViewModel: Тестовый файл создан: ${tempFile.absolutePath}")
                
                // Конвертируем в URI и запускаем импорт
                val uri = Uri.fromFile(tempFile)
                
                // Создаем подходящий импортер и начинаем импорт
                try {
                    val importer = importFactory.createImporter(uri)
                    Timber.d("ImportTransactionsViewModel: Для тестового файла создан импортер: ${importer.javaClass.simpleName}")
                    
                    importer(uri).collect { result ->
                        Timber.d("ImportTransactionsViewModel: Получен результат импорта тестового файла: $result")
                        when (result) {
                            is ImportResult.Progress -> handleProgress(result)
                            is ImportResult.Success -> {
                                handleSuccess(result)
                                // Удаляем временный файл после успешного импорта
                                if (tempFile.exists()) {
                                    tempFile.delete()
                                    Timber.d("ImportTransactionsViewModel: Временный тестовый файл удален")
                                }
                            }
                            is ImportResult.Error -> {
                                handleError(result)
                                // Удаляем временный файл после ошибки
                                if (tempFile.exists()) {
                                    tempFile.delete()
                                    Timber.d("ImportTransactionsViewModel: Временный тестовый файл удален")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "ImportTransactionsViewModel: Ошибка при импорте тестового файла")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Ошибка импорта тестового файла: ${e.message ?: "Неизвестная ошибка"}",
                            isImportCompleted = true
                        )
                    }
                    
                    // Удаляем временный файл после ошибки
                    if (tempFile.exists()) {
                        tempFile.delete()
                        Timber.d("ImportTransactionsViewModel: Временный тестовый файл удален")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "ImportTransactionsViewModel: Неперехваченная ошибка при импорте тестового файла")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка импорта тестового файла: ${e.message ?: "Неизвестная ошибка"}",
                        isImportCompleted = true
                    )
                }
            }
        }
    }

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
     * Получает список логов из FileLogger.
     * 
     * @param limit Количество последних логов (по умолчанию 100)
     * @return Список строк с логами
     */
    suspend fun getLogs(limit: Int = 100): List<String> {
        return FileLogger.getLastLogs(limit).map { it.toString() }
    }
} 