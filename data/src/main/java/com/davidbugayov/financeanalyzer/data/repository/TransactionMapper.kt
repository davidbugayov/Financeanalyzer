package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity
import com.davidbugayov.financeanalyzer.domain.model.Transaction

/**
 * Маппер для преобразования между доменной моделью Transaction и Entity-моделью TransactionEntity.
 * Отвечает за преобразование данных между слоями.
 */
class TransactionMapper {

    /**
     * Преобразует Entity в доменную модель
     * @param entity Entity-модель из базы данных
     * @return Доменная модель Transaction
     */
    fun mapFromEntity(entity: TransactionEntity): Transaction {
        return Transaction(
            id = entity.idString,
            amount = entity.amount,
            category = entity.category,
            date = entity.date,
            isExpense = entity.isExpense,
            note = entity.note,
            source = entity.source,
            sourceColor = entity.sourceColor,
            categoryId = entity.categoryId,
            title = entity.title,
            isTransfer = entity.isTransfer,
            walletIds = entity.walletIds,
        )
    }
    
    /**
     * Преобразует доменную модель в Entity
     * @param transaction Доменная модель Transaction
     * @return Entity-модель для базы данных
     */
    fun mapToEntity(transaction: Transaction): TransactionEntity {
        return TransactionEntity(
            idString = transaction.id,
            amount = transaction.amount,
            category = transaction.category,
            date = transaction.date,
            isExpense = transaction.isExpense,
            note = transaction.note,
            source = transaction.source,
            sourceColor = transaction.sourceColor,
            categoryId = transaction.categoryId,
            title = transaction.title,
            isTransfer = transaction.isTransfer,
            walletIds = transaction.walletIds,
        )
    }
} 