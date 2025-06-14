package com.davidbugayov.financeanalyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity
import java.util.Date

/**
 * DAO (Data Access Object) для работы с транзакциями в базе данных Room.
 * Предоставляет методы для выполнения CRUD операций с транзакциями.
 */
@Dao
interface TransactionDao {

    /**
     * Получает все транзакции из базы данных
     * @return Список всех транзакций
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    /**
     * Получает транзакции с пагинацией
     * @param limit Количество транзакций для загрузки
     * @param offset Смещение (количество пропускаемых транзакций)
     * @return Список транзакций с учетом пагинации
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPaginated(limit: Int, offset: Int): List<TransactionEntity>

    /**
     * Получает транзакции в указанном диапазоне дат с пагинацией
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @param limit Количество транзакций для загрузки
     * @param offset Смещение (количество пропускаемых транзакций)
     * @return Список транзакций, отфильтрованный по датам с учетом пагинации
     */
    @Query(
        "SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC LIMIT :limit OFFSET :offset",
    )
    suspend fun getTransactionsByDateRangePaginated(
        startDate: Date,
        endDate: Date,
        limit: Int,
        offset: Int,
    ): List<TransactionEntity>

    /**
     * Получает общее количество транзакций
     * @return Общее количество транзакций в базе данных
     */
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionsCount(): Int

    /**
     * Получает общее количество транзакций в указанном диапазоне дат
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Количество транзакций в указанном диапазоне дат
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTransactionsCountByDateRange(startDate: Date, endDate: Date): Int

    /**
     * Получает транзакции в указанном диапазоне дат без пагинации
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Список всех транзакций в указанном диапазоне дат
     */
    @Query(
        "SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC",
    )
    suspend fun getTransactionsByDateRange(startDate: Date, endDate: Date): List<TransactionEntity>

    /**
     * Получает транзакцию по ID
     * @param id ID транзакции
     * @return Транзакция с указанным ID или null, если не найдена
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    /**
     * Получает транзакцию по строковому ID
     * @param idString Строковый ID транзакции
     * @return Транзакция с указанным строковым ID или null, если не найдена
     */
    @Query("SELECT * FROM transactions WHERE id_string = :idString")
    suspend fun getTransactionByIdString(idString: String): TransactionEntity?

    /**
     * Добавляет новую транзакцию в базу данных
     * @param transaction Транзакция для добавления
     * @return ID добавленной транзакции
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    /**
     * Обновляет существующую транзакцию в базе данных
     * @param transaction Обновленная транзакция
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    /**
     * Удаляет транзакцию из базы данных
     * @param transaction Транзакция для удаления
     */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    /**
     * Удаляет все транзакции из базы данных
     */
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    /**
     * Удаляет транзакцию по строковому идентификатору.
     * Применяется, когда ID может быть не только числовым.
     * @param id Строковый ID транзакции
     */
    @Query("DELETE FROM transactions WHERE id_string = :id")
    suspend fun deleteTransactionById(id: String)
} 