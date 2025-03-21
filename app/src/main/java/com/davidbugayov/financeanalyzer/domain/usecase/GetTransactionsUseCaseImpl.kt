package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Реализация Use case для получения потока транзакций за указанный период времени.
 * Использует репозиторий транзакций для получения данных из источника.
 * Преобразует операцию получения транзакций в реактивный Flow для наблюдения за изменениями.
 * Следует принципу единственной ответственности (Single Responsibility Principle) из SOLID.
 *
 * @property repository Репозиторий для работы с транзакциями
 */
class GetTransactionsUseCaseImpl(
    private val repository: ITransactionRepository
) : GetTransactionsUseCase {

    /**
     * Получает поток транзакций за указанный период времени через репозиторий.
     * Делегирует фактическую работу репозиторию, следуя принципу разделения обязанностей.
     *
     * @param startDate Начальная дата периода для получения транзакций
     * @param endDate Конечная дата периода для получения транзакций
     * @return Flow с списком транзакций за указанный период
     */
    override suspend fun invoke(startDate: Date, endDate: Date): Flow<List<Transaction>> {
        return repository.getTransactions(startDate, endDate)
    }
} 