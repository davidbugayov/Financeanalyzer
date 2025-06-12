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
     * @param domainModel Доменная модель Transaction
     * @return Entity-модель для сохранения в базе данных
     */
    fun mapToEntity(domainModel: Transaction): TransactionEntity {
        return TransactionEntity(
            idString = domainModel.id,
            amount = domainModel.amount,
            category = domainModel.category,
            date = domainModel.date,
            isExpense = domainModel.isExpense,
            note = domainModel.note,
            source = domainModel.source,
            sourceColor = domainModel.sourceColor,
            categoryId = domainModel.categoryId,
            title = domainModel.title,
            isTransfer = domainModel.isTransfer,
            walletIds = domainModel.walletIds,
        )
    }

    /**
     * Преобразует список Entity в список доменных моделей
     * @param entities Список Entity-моделей
     * @return Список доменных моделей
     */
    fun mapFromEntityList(entities: List<TransactionEntity>): List<Transaction> {
        return entities.map { mapFromEntity(it) }
    }

    /**
     * Преобразует список доменных моделей в список Entity
     * @param domainModels Список доменных моделей
     * @return Список Entity-моделей
     */
    fun mapToEntityList(domainModels: List<Transaction>): List<TransactionEntity> {
        return domainModels.map { mapToEntity(it) }
    }
}
