package com.davidbugayov.financeanalyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.davidbugayov.financeanalyzer.data.local.entity.DebtEntity

@Dao
interface DebtDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DebtEntity): Long

    @Update
    suspend fun update(entity: DebtEntity)

    @Query("DELETE FROM debts WHERE id_string = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM debts WHERE id_string = :id LIMIT 1")
    suspend fun getById(id: String): DebtEntity?

    @Query("SELECT * FROM debts ORDER BY created_at DESC")
    suspend fun getAll(): List<DebtEntity>

    @Query("SELECT * FROM debts WHERE type = :type ORDER BY created_at DESC")
    suspend fun getByType(type: String): List<DebtEntity>
}


