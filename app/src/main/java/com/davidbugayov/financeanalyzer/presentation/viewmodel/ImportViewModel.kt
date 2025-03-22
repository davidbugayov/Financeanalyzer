package com.davidbugayov.financeanalyzer.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.usecase.ImportFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана импорта транзакций.
 * Управляет процессом импорта и отображает прогресс и результаты.
 */
class ImportViewModel(
    private val transactionRepository: TransactionRepositoryImpl,
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
        val totalAmount: Double = 0.0
    )

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    private val importFactory = ImportFactory(transactionRepository, context)

    /**
     * Начинает процесс импорта транзакций из выбранного файла.
     *
     * @param uri URI выбранного файла
     */
    fun startImport(uri: Uri) {
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

                // Создаем подходящий импортер и начинаем импорт
                val importer = importFactory.createImporter(uri)

                importer(uri).collect { result ->
                    when (result) {
                        is ImportResult.Progress -> handleProgress(result)
                        is ImportResult.Success -> handleSuccess(result)
                        is ImportResult.Error -> handleError(result)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка импорта: ${e.message}",
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
        _uiState.update { ImportUiState() }
    }
} 