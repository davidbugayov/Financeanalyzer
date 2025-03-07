package com.davidbugayov.financeanalyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity

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
     * Получает транзакцию по ID
     * @param id ID транзакции
     * @return Транзакция с указанным ID или null, если не найдена
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

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
} 