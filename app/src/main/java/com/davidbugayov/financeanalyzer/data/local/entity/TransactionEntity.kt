package com.davidbugayov.financeanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.davidbugayov.financeanalyzer.data.local.converter.DateConverter
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.util.Date

/**
 * Entity для хранения транзакций в базе данных Room.
 * Представляет таблицу transactions в базе данных.
 */
@Entity(tableName = "transactions")
@TypeConverters(DateConverter::class)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val isExpense: Boolean,
    val date: Date,
    val note: String?
) {

    /**
     * Преобразует Entity в доменную модель
     */
    fun toDomain(): Transaction {
        return Transaction(
            id = id,
            title = title,
            amount = amount,
            category = category,
            isExpense = isExpense,
            date = date,
            note = note
        )
    }

    companion object {

        /**
         * Преобразует доменную модель в Entity
         */
        fun fromDomain(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                title = transaction.title,
                amount = transaction.amount,
                category = transaction.category,
                isExpense = transaction.isExpense,
                date = transaction.date,
                note = transaction.note
            )
        }
    }
} 