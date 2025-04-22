package com.davidbugayov.financeanalyzer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import java.util.Date

/**
 * Entity для хранения транзакций в базе данных Room.
 * Представляет таблицу transactions в базе данных.
 */
@Entity(
    tableName = "transactions",
    // Используем id как основной ключ, а id_string как индексированное поле для поиска по строке
    indices = [androidx.room.Index(value = ["id_string"], unique = true)]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "id_string") val idString: String, // Строковый ID для поиска
    val amount: Money,
    val category: String,
    val isExpense: Boolean,
    val date: Date,
    val note: String? = null,
    val source: String = "Наличные",
    val sourceColor: Int = 0,
    val isTransfer: Boolean = false,
    val categoryId: String = "",
    val title: String = "",
    @ColumnInfo(name = "wallet_ids") val walletIds: List<String>? = null
)