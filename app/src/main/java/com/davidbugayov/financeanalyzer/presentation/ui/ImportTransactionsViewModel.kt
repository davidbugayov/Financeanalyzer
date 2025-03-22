package com.davidbugayov.financeanalyzer.presentation.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.usecase.ImportFactory
import kotlinx.coroutines.flow.Flow

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
} 