package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.LocalDate
import java.util.Date

/**
 * Унифицированный репозиторий для работы с транзакциями.
 * Объединяет функциональность ITransactionRepository и TransactionRepository.
 * Следует принципам Clean Architecture и Interface Segregation Principle (ISP).
 */
interface UnifiedTransactionRepository : TransactionRepository, ITransactionRepository {

    /**
     * Поток событий изменения данных в репозитории.
     * ViewModel могут подписаться на него, чтобы реагировать на добавление,
     * удаление или обновление транзакций.
     */
    override val dataChangeEvents: SharedFlow<DataChangeEvent>

    /**
     * Принудительно отправляет событие изменения данных.
     * Используется для принудительного обновления UI, например после изменения транзакции.
     * @param transactionId ID измененной транзакции или null для массовых изменений.
     */
    suspend fun notifyDataChanged(transactionId: String? = null)

    /**
     * Получает транзакции с пагинацией.
     * @param limit Количество транзакций для загрузки.
     * @param offset Смещение (количество пропускаемых транзакций).
     * @return Список транзакций с учетом пагинации.
     */
    override suspend fun getTransactionsPaginated(limit: Int, offset: Int): List<Transaction>

    /**
     * Получает транзакции за указанный период с пагинацией.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @param limit Количество транзакций для загрузки.
     * @param offset Смещение (количество пропускаемых транзакций).
     * @return Список транзакций с учетом пагинации и диапазона дат.
     */
    override suspend fun getTransactionsByDateRangePaginated(
        startDate: Date,
        endDate: Date,
        limit: Int,
        offset: Int,
    ): List<Transaction>

    /**
     * Получает транзакции за указанный период.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Flow со списком транзакций.
     */
    override suspend fun getTransactions(startDate: Date, endDate: Date): Flow<List<Transaction>>

    /**
     * Получает транзакции за указанный период.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Список транзакций за указанный период.
     */
    override suspend fun getTransactionsByDateRange(startDate: Date, endDate: Date): List<Transaction>

    /**
     * Получает транзакции за указанный период с использованием LocalDate.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Список транзакций за указанный период.
     */
    override suspend fun getTransactionsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Transaction>

    /**
     * Получает список транзакций за указанный период (не Flow).
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Список транзакций.
     */
    override suspend fun getTransactionsByDateRangeList(startDate: Date, endDate: Date): List<Transaction>

    /**
     * Получает транзакции за указанный месяц.
     * @param year Год.
     * @param month Месяц (1-12).
     * @return Список транзакций за указанный месяц.
     */
    override suspend fun getTransactionsByMonth(year: Int, month: Int): List<Transaction>

    /**
     * Получает транзакции за указанную неделю.
     * @param year Год.
     * @param week Неделя года (1-53).
     * @return Список транзакций за указанную неделю.
     */
    override suspend fun getTransactionsByWeek(year: Int, week: Int): List<Transaction>

    /**
     * Получает общее количество транзакций.
     * @return Общее количество транзакций в базе данных.
     */
    override suspend fun getTransactionsCount(): Int

    /**
     * Получает общее количество транзакций в указанном диапазоне дат.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Количество транзакций в указанном диапазоне дат.
     */
    override suspend fun getTransactionsCountByDateRange(startDate: Date, endDate: Date): Int

    /**
     * Загружает все транзакции
     * @return Список транзакций
     */
    override suspend fun loadTransactions(): List<Transaction>

    /**
     * Загружает транзакции с пагинацией
     * @param limit Количество транзакций для загрузки
     * @param offset Смещение (количество пропускаемых транзакций)
     * @return Список транзакций с учетом пагинации
     */
    override suspend fun loadTransactionsPaginated(limit: Int, offset: Int): List<Transaction>

    /**
     * Добавляет новую транзакцию.
     * @param transaction Транзакция для добавления.
     * @return ID добавленной транзакции.
     */
    override suspend fun addTransaction(transaction: Transaction): String

    /**
     * Обновляет существующую транзакцию.
     * @param transaction Транзакция для обновления.
     */
    override suspend fun updateTransaction(transaction: Transaction)

    /**
     * Удаляет транзакцию.
     * @param transaction Транзакция для удаления.
     */
    override suspend fun deleteTransaction(transaction: Transaction)

    /**
     * Удаляет транзакцию по идентификатору.
     * @param id Идентификатор транзакции для удаления.
     */
    override suspend fun deleteTransaction(id: String)

    /**
     * Получает транзакцию по идентификатору.
     * @param id Идентификатор транзакции.
     * @return Транзакция или null, если транзакция не найдена.
     */
    override suspend fun getTransactionById(id: String): Transaction?
    
    /**
     * Получает сущность по идентификатору.
     * @param id Идентификатор сущности.
     * @return Сущность или null, если сущность не найдена.
     */
    suspend fun getById(id: String): Transaction?

    /**
     * Получает поток всех сущностей.
     * @return Flow со списком всех сущностей.
     */
    fun getAll(): Flow<List<Transaction>>

    /**
     * Добавляет новую сущность.
     * @param item Сущность для добавления.
     * @return ID добавленной сущности.
     */
    suspend fun add(item: Transaction): String

    /**
     * Обновляет существующую сущность.
     * @param item Сущность для обновления.
     */
    suspend fun update(item: Transaction)

    /**
     * Удаляет сущность по идентификатору.
     * @param id Идентификатор сущности для удаления.
     * @return true, если сущность успешно удалена, иначе false.
     */
    suspend fun delete(id: String): Boolean
} 