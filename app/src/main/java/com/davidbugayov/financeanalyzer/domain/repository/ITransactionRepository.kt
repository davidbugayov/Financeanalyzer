package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Интерфейс репозитория для работы с транзакциями.
 * Следует принципу инверсии зависимостей (Dependency Inversion Principle).
 * Высокоуровневые модули не должны зависеть от низкоуровневых модулей.
 * Оба должны зависеть от абстракций.
 */
interface ITransactionRepository {
    /**
     * Загружает все транзакции
     * @return Список транзакций
     */
    suspend fun loadTransactions(): List<Transaction>

    /**
     * Загружает транзакции с пагинацией
     * @param limit Количество транзакций для загрузки
     * @param offset Смещение (количество пропускаемых транзакций)
     * @return Список транзакций с учетом пагинации
     */
    suspend fun loadTransactionsPaginated(limit: Int, offset: Int): List<Transaction>

    /**
     * Получает общее количество транзакций
     * @return Общее количество транзакций в базе данных
     */
    suspend fun getTransactionsCount(): Int
    
    /**
     * Добавляет новую транзакцию
     * @param transaction Транзакция для добавления
     */
    suspend fun addTransaction(transaction: Transaction)
    
    /**
     * Удаляет транзакцию
     * @param transaction Транзакция для удаления
     */
    suspend fun deleteTransaction(transaction: Transaction)
    
    /**
     * Обновляет существующую транзакцию
     * @param transaction Обновленная транзакция
     */
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * Получает транзакции за указанный период
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Flow со списком транзакций
     */
    suspend fun getTransactions(startDate: Date, endDate: Date): Flow<List<Transaction>>

    /**
     * Получает транзакции за указанный период с пагинацией
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @param limit Количество транзакций для загрузки
     * @param offset Смещение (количество пропускаемых транзакций)
     * @return Список транзакций с учетом пагинации и диапазона дат
     */
    suspend fun getTransactionsPaginated(startDate: Date, endDate: Date, limit: Int, offset: Int): List<Transaction>

    /**
     * Получает общее количество транзакций в указанном диапазоне дат
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Количество транзакций в указанном диапазоне дат
     */
    suspend fun getTransactionsCountByDateRange(startDate: Date, endDate: Date): Int

    /**
     * Получает транзакцию по идентификатору
     * @param id Идентификатор транзакции
     * @return Транзакция или null, если транзакция не найдена
     */
    suspend fun getTransactionById(id: String): Transaction?
} 