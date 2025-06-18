package com.davidbugayov.financeanalyzer.presentation.import_transaction

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.core.util.Result as CoreResult
import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportResult
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportState
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportTransactionsIntent
import com.davidbugayov.financeanalyzer.presentation.import_transaction.utils.ImportErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * ViewModel для экрана импорта транзакций.
 * Управляет процессом импорта и состоянием UI.
 * Использует паттерн MVI (Model-View-Intent).
 */
class ImportTransactionsViewModel(
    private val importTransactionsUseCase: ImportTransactionsUseCase,
    application: Application,
) : AndroidViewModel(application), KoinComponent {
    // Инъекция TransactionDao через Koin
    private val transactionDao: TransactionDao by inject()

    // UI состояние для отображения на экране
    private val _state = MutableStateFlow(ImportState())
    val state: StateFlow<ImportState> = _state.asStateFlow()

    // Для обратной совместимости с предыдущим подходом
    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Initial)

    // Наблюдатель за прямыми результатами импорта
    private val directResultObserver = Observer<ImportResult.Success?> { result ->
        result?.let {
            Timber.i("Получен прямой результат импорта: importedCount=${it.importedCount}, skippedCount=${it.skippedCount}")
            setSuccessState(it.importedCount, it.skippedCount)
        }
    }

    init {
        // Инициализируем наблюдение за прямыми результатами импорта
        ImportResult.directResultLiveData.observeForever(directResultObserver)
    }

    override fun onCleared() {
        super.onCleared()
        // Удаляем наблюдателя при уничтожении ViewModel
        ImportResult.directResultLiveData.removeObserver(directResultObserver)
    }

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
     * Устанавливает состояние успешного импорта с указанным количеством транзакций.
     */
    private fun setSuccessState(importedCount: Int, skippedCount: Int) {
        val context = getApplication<Application>().applicationContext
        val successMessage = context.getString(R.string.import_success_message, importedCount, skippedCount, _state.value.fileName)

        Timber.d("Устанавливаем состояние успеха: importedCount=$importedCount, skippedCount=$skippedCount")
        Timber.d("Текущее состояние перед обновлением: isLoading=${_state.value.isLoading}, error=${_state.value.error}, successCount=${_state.value.successCount}")

        // Создаем новый объект состояния
        val newState = ImportState(
            isLoading = false,
            progress = 100,
            progressMessage = context.getString(R.string.import_progress_completed, _state.value.fileName),
            successCount = importedCount,
            skippedCount = skippedCount,
            successMessage = successMessage,
            error = null,  // Гарантируем, что ошибка сброшена
            fileName = _state.value.fileName // Сохраняем имя файла
        )

        // Устанавливаем новое состояние
        _state.value = newState
        _uiState.value = ImportUiState.Success(
            message = successMessage,
            importedCount = importedCount,
            skippedCount = skippedCount,
        )

        // Проверяем, что состояние действительно обновилось
        Timber.d("Состояние после обновления: isLoading=${_state.value.isLoading}, error=${_state.value.error}, successCount=${_state.value.successCount}")
    }

    /**
     * Запускает импорт транзакций из указанного файла.
     * @param uri URI файла для импорта
     */
    private fun startImport(uri: Uri) {
        if (_state.value.isLoading) {
            Timber.d("Импорт уже выполняется, запрос игнорируется")
            return
        }

        // Сначала полностью сбрасываем состояние перед новым импортом
        resetState()

        Timber.d("Начинаем импорт файла с URI: $uri")

        // Получаем MIME-тип и имя файла для диагностики
        var fileName = "Файл"
        try {
            val mimeType = getApplication<Application>().contentResolver.getType(uri)
            getApplication<Application>().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex)
                        Timber.d("Импорт файла: $fileName, тип: $mimeType")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении информации о файле")
        }

        // Обновляем состояние, показывая процесс загрузки
        val context = getApplication<Application>().applicationContext
        val startMessage = context.getString(R.string.import_progress_starting, fileName)
        _state.value = _state.value.copy(
            isLoading = true,
            progress = 0,
            progressMessage = startMessage,
            error = null,
            successCount = 0,
            skippedCount = 0,
            fileName = fileName
        )
        // Для обратной совместимости
        _uiState.value = ImportUiState.Loading(startMessage)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                importTransactionsUseCase.importTransactions(uri) { current, total, message ->
                    val progress = if (total > 0) (current * 100 / total) else 0
                    // Во время прогресса убираем ошибку, если она была
                    _state.value = _state.value.copy(
                        progress = progress,
                        progressMessage = message,
                        error = null, // Важно: убираем ошибку во время прогресса
                    )
                    _uiState.value = ImportUiState.Loading(message, progress)
                }.collect { result ->
                    when (result) {
                        is CoreResult.Success<*> -> {
                            // Безопасное извлечение данных
                            val data = result.data
                            var importedCount = 0
                            var skippedCount = 0

                            // Проверяем тип данных для извлечения значений
                            when (data) {
                                is Pair<*, *> -> {
                                    val first = data.first
                                    val second = data.second
                                    if (first is Number) {
                                        importedCount = first.toInt()
                                    }
                                    if (second is Number) {
                                        skippedCount = second.toInt()
                                    }
                                }
                                else -> {
                                    Timber.d("Неизвестный тип данных в CoreResult.Success: ${data?.javaClass?.name}")
                                }
                            }

                            Timber.i("Импорт завершен успешно! Импортировано: $importedCount, Пропущено: $skippedCount")

                            // Устанавливаем состояние успешного импорта
                            setSuccessState(importedCount, skippedCount)

                            // Проверка наличия транзакций в базе
                            viewModelScope.launch(Dispatchers.IO) {
                                try {
                                    val count = transactionDao.getTransactionsCount()
                                    Timber.i("Проверка после импорта: всего транзакций в базе данных: $count")
                                } catch (e: Exception) {
                                    Timber.e(e, "Ошибка при проверке количества транзакций после импорта")
                                }
                            }
                        }
                        is CoreResult.Error -> {
                            // Получаем исходное сообщение об ошибке
                            val originalMessage = result.exception?.message ?: "Неизвестная ошибка"

                            // Используем отдельный класс для обработки ошибок
                            val context = getApplication<Application>().applicationContext
                            val errorHandler = ImportErrorHandler(context)
                            val userFriendlyMessage = errorHandler.getUserFriendlyErrorMessage(originalMessage)

                            Timber.e(result.exception, "Ошибка импорта: $originalMessage")
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = userFriendlyMessage,
                                progress = 0,
                                progressMessage = "",
                                fileName = _state.value.fileName // Сохраняем имя файла
                            )
                            _uiState.value = ImportUiState.Error(userFriendlyMessage)
                        }
                        is ImportResult.Progress -> {
                            // Обрабатываем прогресс, но не устанавливаем ошибку
                            val progress = if (result.total > 0) (result.current * 100 / result.total) else 0
                            _state.value = _state.value.copy(
                                isLoading = true,
                                progress = progress,
                                progressMessage = result.message,
                                error = null, // Важно: убираем ошибку во время прогресса
                            )
                            _uiState.value = ImportUiState.Loading(result.message, progress)
                        }
                        is ImportResult.Error -> {
                            // Обработка ошибки из ImportResult.Error
                            val originalMessage = result.message ?: result.exception?.message ?: "Неизвестная ошибка"
                            val context = getApplication<Application>().applicationContext
                            val errorHandler = ImportErrorHandler(context)
                            val userFriendlyMessage = errorHandler.getUserFriendlyErrorMessage(originalMessage)
                            
                            Timber.e(result.exception, "Ошибка импорта (ImportResult.Error): $originalMessage")
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = userFriendlyMessage,
                                progress = 0,
                                progressMessage = "",
                                fileName = _state.value.fileName // Сохраняем имя файла
                            )
                            _uiState.value = ImportUiState.Error(userFriendlyMessage)
                        }
                        is ImportResult.Success -> {
                            // Обработка успешного результата из ImportResult.Success
                            val importedCount = result.importedCount
                            val skippedCount = result.skippedCount
                            
                            Timber.i("Импорт завершен успешно через ImportResult.Success! Импортировано: $importedCount, Пропущено: $skippedCount")
                            
                            // Устанавливаем состояние успешного импорта
                            setSuccessState(importedCount, skippedCount)
                            
                            // Проверка наличия транзакций в базе
                            viewModelScope.launch(Dispatchers.IO) {
                                try {
                                    val count = transactionDao.getTransactionsCount()
                                    Timber.i("Проверка после импорта: всего транзакций в базе данных: $count")
                                } catch (e: Exception) {
                                    Timber.e(e, "Ошибка при проверке количества транзакций после импорта")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Необработанное исключение при импорте: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неизвестная ошибка",
                    fileName = _state.value.fileName // Сохраняем имя файла
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
    }

    /**
     * Сбрасывает состояние импорта.
     * Вызывается при выходе с экрана или при выборе нового файла.
     */
    private fun resetState() {
        Timber.d("Сброс состояния импорта")

        // Создаем новый объект состояния со значениями по умолчанию
        _state.value = ImportState(
            isLoading = false,
            progress = 0,
            progressMessage = "",
            successCount = 0,
            skippedCount = 0,
            successMessage = "",
            error = null,
            fileName = "" // Сбрасываем имя файла
        )

        // Сбрасываем состояние для обратной совместимости
        _uiState.value = ImportUiState.Initial

        // Убедимся, что прямой результат тоже сброшен
        ImportResult.directResultLiveData.postValue(null)
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
     * @param message Информационное сообщение
     * @param progress Прогресс импорта (0-100)
     */
    data class Loading(val message: String, val progress: Int = 0) : ImportUiState()

    /**
     * Состояние успешного завершения импорта.
     * @param message Сообщение об успешном завершении
     * @param importedCount Количество импортированных транзакций
     * @param skippedCount Количество пропущенных транзакций
     */
    data class Success(
        val message: String,
        val importedCount: Int,
        val skippedCount: Int,
    ) : ImportUiState()

    /**
     * Состояние ошибки.
     * @param message Сообщение об ошибке
     */
    data class Error(val message: String) : ImportUiState()
}
