package com.davidbugayov.financeanalyzer.presentation.import_transaction

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportState
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportTransactionsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel для экрана импорта транзакций.
 * Управляет процессом импорта и состоянием UI.
 * Использует паттерн MVI (Model-View-Intent).
 */
class ImportTransactionsViewModel(
    private val importTransactionsUseCase: ImportTransactionsUseCase,
    application: Application
) : AndroidViewModel(application) {

    // UI состояние для отображения на экране
    private val _state = MutableStateFlow(ImportState())
    val state: StateFlow<ImportState> = _state.asStateFlow()

    // Для обратной совместимости с предыдущим подходом
    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Initial)
    val uiState: StateFlow<ImportUiState> = _uiState
    
    /**
     * Обрабатывает намерения пользователя в соответствии с паттерном MVI.
     *
     * @param intent Намерение пользователя
     */
    fun handleIntent(intent: ImportTransactionsIntent) {
        when (intent) {
            is ImportTransactionsIntent.StartImport -> startImport(intent.uri)
            is ImportTransactionsIntent.RefreshLogs -> refreshLogs()
            is ImportTransactionsIntent.ResetState -> resetState()
        }
    }

    /**
     * Запускает импорт транзакций из указанного файла.
     *
     * @param uri URI файла для импорта
     */
    private fun startImport(uri: Uri) {
        if (_state.value.isLoading) {
            Timber.d("Импорт уже выполняется, запрос игнорируется")
            return
        }

        Timber.d("Начинаем импорт файла с URI: $uri, схема: ${uri.scheme}, путь: ${uri.path}")

        // Проверяем доступность файла
        try {
            getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                Timber.d("Файл успешно открыт, размер: ${stream.available()} байт")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ ОШИБКА при открытии файла: ${e.message}")
        }

        // Получаем MIME-тип и имя файла для диагностики
        try {
            val mimeType = getApplication<Application>().contentResolver.getType(uri)
            Timber.d("MIME-тип файла: $mimeType")

            getApplication<Application>().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        val fileName = cursor.getString(displayNameIndex)
                        Timber.d("Имя файла: $fileName")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении информации о файле: ${e.message}")
        }

        // Обновляем состояние, показывая процесс загрузки
        _state.value = _state.value.copy(
            isLoading = true,
            progress = 0,
            progressMessage = "Начало импорта...",
            error = null,
            successCount = 0,
            skippedCount = 0
        )

        // Для обратной совместимости
        _uiState.value = ImportUiState.Loading("Начало импорта...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                importTransactionsUseCase.importTransactions(uri) { current, total, message ->
                    val progress = if (total > 0) (current * 100 / total) else 0
                    Timber.d("Прогресс импорта: $current/$total ($progress%) - $message")
                    _state.value = _state.value.copy(
                        progress = progress,
                        progressMessage = message
                    )
                    _uiState.value = ImportUiState.Loading(message, progress)
                }.collect { result ->
                    when (result) {
                        is ImportResult.Progress -> {
                            val progress = if (result.total > 0) (result.current * 100 / result.total) else 0
                            Timber.d("Получен прогресс: $progress% - ${result.message}")
                            _state.value = _state.value.copy(
                                progress = progress,
                                progressMessage = result.message
                            )
                            _uiState.value = ImportUiState.Loading(result.message, progress)
                        }
                        is ImportResult.Success -> {
                            val successMessage = "Импорт успешно завершен. " +
                                    "Импортировано: ${result.importedCount}, " +
                                    "Пропущено: ${result.skippedCount}"

                            Timber.d("Импорт успешно завершен: импортировано ${result.importedCount}, пропущено ${result.skippedCount}")

                            _state.value = _state.value.copy(
                                isLoading = false,
                                successCount = result.importedCount,
                                skippedCount = result.skippedCount,
                                successMessage = successMessage,
                                error = null
                            )

                            _uiState.value = ImportUiState.Success(
                                message = successMessage,
                                importedCount = result.importedCount,
                                skippedCount = result.skippedCount
                            )
                        }
                        is ImportResult.Error -> {
                            val errorMessage = result.exception?.message ?: result.message

                            Timber.e(result.exception, "❌ Ошибка импорта: $errorMessage")

                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = errorMessage
                            )

                            _uiState.value = ImportUiState.Error(errorMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Необработанное исключение при импорте: ${e.message}")

                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неизвестная ошибка"
                )

                _uiState.value = ImportUiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    /**
     * Обновляет логи импорта.
     * Эта функция в настоящее время ничего не делает, но оставлена для
     * совместимости с интерфейсом ImportTransactionsIntent.
     */
    private fun refreshLogs() {
        // В текущей реализации не используется
        Timber.d("refreshLogs called, but not implemented")
    }

    /**
     * Сбрасывает состояние импорта.
     */
    private fun resetState() {
        _state.value = ImportState()
        _uiState.value = ImportUiState.Initial
    }
}

/**
 * Состояния UI для экрана импорта (для обратной совместимости).
 */
sealed class ImportUiState {

    /**
     * Начальное состояние, до начала импорта.
     */
    object Initial : ImportUiState()

    /**
     * Состояние загрузки/импорта.
     *
     * @param message Информационное сообщение
     * @param progress Прогресс импорта (0-100)
     */
    data class Loading(val message: String, val progress: Int = 0) : ImportUiState()

    /**
     * Состояние успешного завершения импорта.
     *
     * @param message Сообщение об успешном завершении
     * @param importedCount Количество импортированных транзакций
     * @param skippedCount Количество пропущенных транзакций
     */
    data class Success(
        val message: String,
        val importedCount: Int,
        val skippedCount: Int
    ) : ImportUiState()

    /**
     * Состояние ошибки.
     *
     * @param message Сообщение об ошибке
     */
    data class Error(val message: String) : ImportUiState()
} 