package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.local.dao.SubcategoryDao
import com.davidbugayov.financeanalyzer.domain.model.Subcategory
import com.davidbugayov.financeanalyzer.domain.repository.SubcategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Реализация репозитория для работы с подкатегориями
 */
class SubcategoryRepositoryImpl(
    private val subcategoryDao: SubcategoryDao,
) : SubcategoryRepository {

    override suspend fun getAllSubcategories(): List<Subcategory> {
        return SubcategoryMapper.mapToDomainList(subcategoryDao.getAllSubcategories())
    }

    override fun observeAllSubcategories(): Flow<List<Subcategory>> {
        return subcategoryDao.observeAllSubcategories()
            .map { entities -> SubcategoryMapper.mapToDomainList(entities) }
    }

    override suspend fun getSubcategoriesByCategoryId(categoryId: Long): List<Subcategory> {
        return SubcategoryMapper.mapToDomainList(subcategoryDao.getSubcategoriesByCategoryId(categoryId))
    }

    override fun observeSubcategoriesByCategoryId(categoryId: Long): Flow<List<Subcategory>> {
        return subcategoryDao.observeSubcategoriesByCategoryId(categoryId)
            .map { entities -> SubcategoryMapper.mapToDomainList(entities) }
    }

    override suspend fun getSubcategoryById(id: Long): Subcategory? {
        return subcategoryDao.getSubcategoryById(id)?.let { SubcategoryMapper.mapToDomain(it) }
    }

    override suspend fun insertSubcategory(subcategory: Subcategory): Long {
        val entity = SubcategoryMapper.mapToEntity(subcategory)
        return subcategoryDao.insertSubcategory(entity)
    }

    override suspend fun updateSubcategory(subcategory: Subcategory) {
        val entity = SubcategoryMapper.mapToEntity(subcategory)
        subcategoryDao.updateSubcategory(entity)
    }

    override suspend fun deleteSubcategory(subcategory: Subcategory) {
        val entity = SubcategoryMapper.mapToEntity(subcategory)
        subcategoryDao.deleteSubcategory(entity)
    }

    override suspend fun deleteSubcategoryById(id: Long) {
        subcategoryDao.deleteSubcategoryById(id)
    }

    override suspend fun deleteSubcategoriesByCategoryId(categoryId: Long) {
        subcategoryDao.deleteSubcategoriesByCategoryId(categoryId)
    }

    override suspend fun incrementSubcategoryCount(id: Long) {
        subcategoryDao.incrementSubcategoryCount(id)
    }
} 