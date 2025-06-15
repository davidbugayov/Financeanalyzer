package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Use case для получения потока транзакций за указанный период времени.
 * Предоставляет интерфейс для получения транзакций в виде Flow для реактивной обработки данных.
 * Следует принципу единственной ответственности (Single Responsibility Principle) из SOLID.
 * Соответствует концепции Clean Architecture, отделяя бизнес-логику от деталей реализации.
 */
interface GetTransactionsUseCase {

    /**
     * Получает поток транзакций за указанный период времени.
     * Метод реализует паттерн "функциональный объект" с помощью оператора invoke.
     *
     * @param startDate Начальная дата периода для получения транзакций
     * @param endDate Конечная дата периода для получения транзакций
     * @return Flow с списком транзакций за указанный период
     */
    suspend operator fun invoke(startDate: Date, endDate: Date): Flow<List<Transaction>>

    /**
     * Получает список всех транзакций.
     * * @return Список всех транзакций
     */
    suspend fun getAllTransactions(): List<Transaction>

    /**
     * Получает список транзакций за последние N дней.
     *
     * @param days Количество дней для получения транзакций
     * @return Список транзакций за указанный период
     */
    suspend fun getRecentTransactions(days: Int): List<Transaction>
} 