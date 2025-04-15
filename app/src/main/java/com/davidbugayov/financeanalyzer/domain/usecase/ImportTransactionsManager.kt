package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

/**
 * Менеджер для управления процессом импорта транзакций.
 * Соответствует принципам Clean Architecture и SOLID.
 * Следует принципу единственной ответственности (SRP) и открытости/закрытости (OCP).
 *
 * @property repository Репозиторий для работы с транзакциями
 * @property context Контекст приложения для доступа к файловой системе
 * @property categoryPreferences Предпочтения категорий для создания новых категорий
 * @property sourcePreferences Предпочтения источников для создания новых источников
 * @property importFactory Фабрика для создания подходящих импортеров
 */
class ImportTransactionsManager(
    private val repository: TransactionRepositoryImpl,
    private val context: Context,
    private val categoryPreferences: CategoryPreferences,
    private val sourcePreferences: SourcePreferences
) {
    private val importFactory = ImportFactory(repository, context, categoryPreferences, sourcePreferences)

    /**
     * Импортирует транзакции из указанного URI.
     * Автоматически выбирает подходящий импортер через фабрику.
     *
     * @param uri URI файла для импорта
     * @return Flow с результатами импорта (прогресс, успех, ошибка)
     */
    suspend fun importTransactions(uri: Uri): Flow<ImportResult> {
        Timber.d("ImportTransactionsManager: Начинаем импорт из URI: $uri")
        val importer = importFactory.createImporter(uri)
        Timber.d("ImportTransactionsManager: Импортер создан: ${importer.javaClass.simpleName}")
        return importer(uri)
    }
} 