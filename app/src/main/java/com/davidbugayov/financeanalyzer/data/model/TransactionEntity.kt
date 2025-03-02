package com.davidbugayov.financeanalyzer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.util.Date

/**
 * Модель данных для хранения транзакций в базе данных.
 * Содержит аннотации Room для работы с базой данных.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val isExpense: Boolean,
    val date: Date,
    val note: String? = null
)

/**
 * Функции расширения для конвертации между доменной моделью и моделью данных
 */
fun TransactionEntity.toDomain(): Transaction {
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

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        category = category,
        isExpense = isExpense,
        date = date,
        note = note
    )
}

/**
 * Функции расширения для конвертации списков
 */
fun List<TransactionEntity>.toDomainList(): List<Transaction> {
    return map { it.toDomain() }
}

fun List<Transaction>.toEntityList(): List<TransactionEntity> {
    return map { it.toEntity() }
} 