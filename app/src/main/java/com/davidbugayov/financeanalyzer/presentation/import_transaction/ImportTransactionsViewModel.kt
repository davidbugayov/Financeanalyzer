package com.davidbugayov.financeanalyzer.presentation.import_transaction

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.core.util.Result as CoreResult
import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportState
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportTransactionsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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
            skippedCount = 0,
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
                        progressMessage = message,
                    )
                    _uiState.value = ImportUiState.Loading(message, progress)
                }.collect { result ->
                    when (result) {
                        is CoreResult.Success<*> -> {
                            // Безопасное преобразование без проверки типа
                            @Suppress("USELESS_IS_CHECK")
                            val data = result.data

                            // Извлекаем значения из данных
                            val (importedCount, skippedCount) = try {
                                // Попытка безопасного преобразования
                                @Suppress("UNCHECKED_CAST", "USELESS_IS_CHECK")
                                val pair = data as? Pair<*, *>
                                val imported = (pair?.first as? Number)?.toInt() ?: 0
                                val skipped = (pair?.second as? Number)?.toInt() ?: 0
                                Pair(imported, skipped)
                            } catch (e: ClassCastException) {
                                Timber.w("Ошибка преобразования данных: ${e.message}")
                                Pair(0, 0)
                            }

                            val successMessage = "Импорт успешно завершен. " +
                                "Импортировано: $importedCount, " +
                                "Пропущено: $skippedCount"
                            Timber.d(
                                "Импорт успешно завершен: импортировано $importedCount, пропущено $skippedCount",
                            )
                            // Добавляем диагностическое логирование для проверки, были ли транзакции действительно сохранены
                            Timber.i(
                                "[VIEWMODEL] Импорт завершен успешно! Импортировано: $importedCount, Пропущено: $skippedCount",
                            )
                            // Запустим проверку наличия транзакций в базе через 1 секунду
                            viewModelScope.launch(Dispatchers.IO) {
                                Timber.d(
                                    "[VIEWMODEL] Планируем проверку наличия транзакций в базе через 1 секунду",
                                )
                                kotlinx.coroutines.delay(1000)
                                try {
                                    // Используем инъектированный transactionDao
                                    val count = transactionDao.getTransactionsCount()
                                    Timber.i(
                                        "[VIEWMODEL] ✅ Проверка после импорта: всего транзакций в базе данных: $count",
                                    )
                                    // Получим последние 5 транзакций для анализа
                                    Timber.i(
                                        "[VIEWMODEL-ОТЛАДКА] 🔍 Попытка получить последние транзакции из базы...",
                                    )
                                    try {
                                        val latestTransactions = transactionDao.getTransactionsPaginated(
                                            5,
                                            0,
                                        )
                                        if (latestTransactions.isNotEmpty()) {
                                            Timber.i(
                                                "[VIEWMODEL-ОТЛАДКА] ✅ Получено ${latestTransactions.size} последних транзакций:",
                                            )
                                            latestTransactions.forEachIndexed { index, tx ->
                                                Timber.i(
                                                    "[VIEWMODEL-ОТЛАДКА] 📝 Транзакция #${index + 1}: ID=${tx.id}, idString=${tx.idString}, " +
                                                        "Дата=${tx.date}, Сумма=${tx.amount}, Категория='${tx.category}'",
                                                )
                                            }
                                        } else {
                                            Timber.e(
                                                "[VIEWMODEL-ОТЛАДКА] ❌ В базе данных НЕТ транзакций!",
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Timber.e(
                                            e,
                                            "[VIEWMODEL-ОТЛАДКА] ❌ Ошибка при получении последних транзакций: ${e.message}",
                                        )
                                    }
                                    // Еще одна проверка с другим методом
                                    try {
                                        Timber.i(
                                            "[VIEWMODEL-ОТЛАДКА] 🔍 Альтернативная проверка через getAllTransactions...",
                                        )
                                        val allTransactions = transactionDao.getAllTransactions()
                                        Timber.i(
                                            "[VIEWMODEL-ОТЛАДКА] 📊 Всего транзакций через getAllTransactions: ${allTransactions.size}",
                                        )
                                    } catch (e: Exception) {
                                        Timber.e(
                                            "[VIEWMODEL-ОТЛАДКА] ❌ Ошибка при вызове getAllTransactions: ${e.message}",
                                        )
                                    }
                                } catch (e: Exception) {
                                    Timber.e(
                                        e,
                                        "[VIEWMODEL] ❌ Ошибка при проверке количества транзакций после импорта: ${e.message}",
                                    )
                                }
                            }
                            _state.value = _state.value.copy(
                                isLoading = false,
                                successCount = importedCount,
                                skippedCount = skippedCount,
                                successMessage = successMessage,
                                error = null,
                            )
                            _uiState.value = ImportUiState.Success(
                                message = successMessage,
                                importedCount = importedCount,
                                skippedCount = skippedCount,
                            )
                        }
                        is CoreResult.Error -> {
                            val errorMessage = result.exception.message ?: "Неизвестная ошибка"
                            Timber.e(result.exception, "❌ Ошибка импорта: $errorMessage")
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = errorMessage,
                            )
                            _uiState.value = ImportUiState.Error(errorMessage)
                        }
                        else -> {
                            Timber.w("Получен неизвестный тип результата: $result")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Необработанное исключение при импорте: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неизвестная ошибка",
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
