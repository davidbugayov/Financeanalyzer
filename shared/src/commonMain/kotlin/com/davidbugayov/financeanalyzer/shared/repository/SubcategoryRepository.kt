package com.davidbugayov.financeanalyzer.shared.repository

import com.davidbugayov.financeanalyzer.shared.model.Subcategory
import kotlinx.coroutines.flow.Flow

interface SubcategoryRepository {
    fun observeSubcategoriesByCategoryId(categoryId: Long): Flow<List<Subcategory>>
    suspend fun getSubcategoryById(id: Long): Subcategory?
    suspend fun addSubcategory(subcategory: Subcategory)
    suspend fun deleteSubcategory(id: Long)
}


