package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.util.Date
import kotlinx.coroutines.flow.SharedFlow

/**
 * Репозиторий для работы с транзакциями.
 * Следует принципам Clean Architecture.
 */
interface TransactionRepository {

    /**
     * Поток событий изменения данных в репозитории.
     * ViewModel могут подписаться на него, чтобы реагировать на добавление,
     * удаление или обновление транзакций.
     */
    val dataChangeEvents: SharedFlow<DataChangeEvent>

    /**
     * Получает все транзакции.
     * @return Список всех транзакций.
     */
    suspend fun getAllTransactions(): List<Transaction>

    /**
     * Получает транзакции с пагинацией.
     * @param limit Количество транзакций для загрузки.
     * @param offset Смещение (количество пропускаемых транзакций).
     * @return Список транзакций с учетом пагинации.
     */
    suspend fun getTransactionsPaginated(limit: Int, offset: Int): List<Transaction>

    /**
     * Получает транзакции за указанный период с пагинацией.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @param limit Количество транзакций для загрузки.
     * @param offset Смещение (количество пропускаемых транзакций).
     * @return Список транзакций с учетом пагинации и диапазона дат.
     */
    suspend fun getTransactionsByDateRangePaginated(
        startDate: Date,
        endDate: Date,
        limit: Int,
        offset: Int,
    ): List<Transaction>

    /**
     * Получает транзакции за указанный период.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Список транзакций за указанный период.
     */
    suspend fun getTransactionsByDateRange(startDate: Date, endDate: Date): List<Transaction>

    /**
     * Получает транзакции за указанный период с использованием LocalDate.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Список транзакций за указанный период.
     */
    suspend fun getTransactionsByDateRange(
        startDate: kotlinx.datetime.LocalDate,
        endDate: kotlinx.datetime.LocalDate,
    ): List<Transaction>

    /**
     * Получает список транзакций за указанный период (не Flow).
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Список транзакций.
     */
    suspend fun getTransactionsByDateRangeList(startDate: Date, endDate: Date): List<Transaction>

    /**
     * Получает транзакции за указанный месяц.
     * @param year Год.
     * @param month Месяц (1-12).
     * @return Список транзакций за указанный месяц.
     */
    suspend fun getTransactionsByMonth(year: Int, month: Int): List<Transaction>

    /**
     * Получает транзакции за указанную неделю.
     * @param year Год.
     * @param week Неделя года (1-53).
     * @return Список транзакций за указанную неделю.
     */
    suspend fun getTransactionsByWeek(year: Int, week: Int): List<Transaction>

    /**
     * Получает общее количество транзакций.
     * @return Общее количество транзакций в базе данных.
     */
    suspend fun getTransactionsCount(): Int

    /**
     * Получает транзакцию по идентификатору.
     * @param id Идентификатор транзакции.
     * @return Транзакция или null, если транзакция не найдена.
     */
    suspend fun getTransactionById(id: String): Transaction?

    /**
     * Добавляет новую транзакцию.
     * @param transaction Транзакция для добавления.
     * @return ID добавленной транзакции.
     */
    suspend fun addTransaction(transaction: Transaction): String

    /**
     * Обновляет существующую транзакцию.
     * @param transaction Транзакция для обновления.
     */
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * Удаляет транзакцию.
     * @param transaction Транзакция для удаления.
     */
    suspend fun deleteTransaction(transaction: Transaction)

    /**
     * Удаляет транзакцию по идентификатору.
     * @param id Идентификатор транзакции для удаления.
     */
    suspend fun deleteTransaction(id: String)
}
