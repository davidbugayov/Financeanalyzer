package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.local.entity.SubcategoryEntity
import com.davidbugayov.financeanalyzer.domain.model.Subcategory

/**
 * Маппер для конвертации между Entity и Domain моделями подкатегорий
 */
object SubcategoryMapper {

    /**
     * Конвертирует SubcategoryEntity в Subcategory
     */
    fun mapToDomain(entity: SubcategoryEntity): Subcategory {
        return Subcategory(
            id = entity.id,
            name = entity.name,
            categoryId = entity.categoryId,
            count = entity.count,
            isCustom = entity.isCustom,
        )
    }

    /**
     * Конвертирует Subcategory в SubcategoryEntity
     */
    fun mapToEntity(domain: Subcategory): SubcategoryEntity {
        return SubcategoryEntity(
            id = domain.id,
            name = domain.name,
            categoryId = domain.categoryId,
            count = domain.count,
            isCustom = domain.isCustom,
        )
    }

    /**
     * Конвертирует список SubcategoryEntity в список Subcategory
     */
    fun mapToDomainList(entities: List<SubcategoryEntity>): List<Subcategory> {
        return entities.map { mapToDomain(it) }
    }

    /**
     * Конвертирует список Subcategory в список SubcategoryEntity
     */
    fun mapToEntityList(domains: List<Subcategory>): List<SubcategoryEntity> {
        return domains.map { mapToEntity(it) }
    }
} 