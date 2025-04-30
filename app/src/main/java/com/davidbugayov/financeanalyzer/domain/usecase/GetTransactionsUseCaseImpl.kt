package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date
import java.util.Calendar

/**
 * Реализация Use case для получения потока транзакций за указанный период времени.
 * Использует репозиторий транзакций для получения данных из источника.
 * Преобразует операцию получения транзакций в реактивный Flow для наблюдения за изменениями.
 * Следует принципу единственной ответственности (Single Responsibility Principle) из SOLID.
 *
 * @property repository Репозиторий для работы с транзакциями
 */
class GetTransactionsUseCaseImpl(
    private val repository: TransactionRepository
) : GetTransactionsUseCase {

    /**
     * Возвращает Flow с транзакциями за указанный период
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Flow с списком транзакций за указанный период
     */
    override suspend fun invoke(startDate: Date, endDate: Date): Flow<List<Transaction>> {
        return flow {
            val transactions = repository.getTransactionsByDateRange(startDate, endDate)
            emit(transactions)
        }
    }

    /**
     * Получает список всех транзакций через репозиторий.
     *
     * @return Список всех транзакций
     */
    override suspend fun getAllTransactions(): List<Transaction> {
        return repository.getAllTransactions()
    }

    /**
     * Получает список транзакций за последние N дней.
     *
     * @param days Количество дней для получения транзакций
     * @return Список транзакций за указанный период
     */
    override suspend fun getRecentTransactions(days: Int): List<Transaction> {
        val endDate = Date() // Текущая дата
        val calendar = Calendar.getInstance()
        calendar.time = endDate
        calendar.add(Calendar.DAY_OF_YEAR, -days) // Вычитаем указанное количество дней
        val startDate = calendar.time
        
        return repository.getTransactionsByDateRangeList(startDate, endDate)
    }
} 