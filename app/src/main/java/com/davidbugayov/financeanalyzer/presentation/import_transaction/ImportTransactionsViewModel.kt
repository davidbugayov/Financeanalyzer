package com.davidbugayov.financeanalyzer.presentation.import_transaction

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.usecase.ImportTransactionsManager
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportTransactionsIntent
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportTransactionsState
import com.davidbugayov.financeanalyzer.utils.logging.FileLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel для экрана импорта транзакций.
 * Реализует паттерн MVI (Model-View-Intent) и принципы Clean Architecture.
 */
class ImportTransactionsViewModel(
    private val importManager: ImportTransactionsManager
) : ViewModel() {

    // Состояние UI, соответствующее MVI
    private val _state = MutableStateFlow(ImportTransactionsState())
    val state: StateFlow<ImportTransactionsState> = _state.asStateFlow()

    /**
     * Обрабатывает интенты, пришедшие из UI.
     * Ключевой метод паттерна MVI.
     *
     * @param intent Интент для обработки
     */
    fun handleIntent(intent: ImportTransactionsIntent) {
        when (intent) {
            is ImportTransactionsIntent.StartImport -> startImport(intent.uri)
            is ImportTransactionsIntent.RefreshLogs -> refreshLogs()
            is ImportTransactionsIntent.ResetState -> resetState()
        }
    }

    /**
     * Начинает процесс импорта транзакций из выбранного файла.
     *
     * @param uri URI выбранного файла
     */
    private fun startImport(uri: Uri) {
        Timber.d("Начинаем импорт из URI: $uri")
        viewModelScope.launch {
            try {
                // Сбрасываем состояние перед началом нового импорта
                _state.update {
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

                // Делегируем процесс импорта в менеджер
                try {
                    importManager.importTransactions(uri).collect { result ->
                        processImportResult(result)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Ошибка при выполнении импорта")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Ошибка импорта: ${e.message ?: "Неизвестная ошибка"}",
                            isImportCompleted = true
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Неперехваченная ошибка в процессе импорта")
                _state.update {
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
     * Обрабатывает результаты импорта и обновляет состояние UI.
     * @param result Результат операции импорта
     */
    private fun processImportResult(result: ImportResult) {
        when (result) {
            is ImportResult.Progress -> handleProgress(result)
            is ImportResult.Success -> handleSuccess(result)
            is ImportResult.Error -> handleError(result)
        }
    }

    /**
     * Обрабатывает прогресс импорта.
     */
    private fun handleProgress(progress: ImportResult.Progress) {
        Timber.d("Обработка прогресса: ${progress.current}/${progress.total} - ${progress.message}")
        _state.update { state ->
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
        Timber.d("Импорт успешно завершен. Импортировано: ${success.importedCount}, пропущено: ${success.skippedCount}")
        _state.update { state ->
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
        Timber.e(error.exception, "Ошибка импорта: ${error.message}")
        _state.update { state ->
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
    private fun resetState() {
        Timber.d("Сброс состояния импорта")
        _state.update { ImportTransactionsState() }
    }
    
    /**
     * Обновляет логи в состоянии UI.
     */
    private fun refreshLogs() {
        val logs = FileLogger.getLastLogs(100).map { it.toString() }
        _state.update { state ->
            state.copy(
                logs = logs
            )
        }
    }
} 