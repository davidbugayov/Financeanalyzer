package com.davidbugayov.financeanalyzer.presentation.subcategories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.usecase.subcategory.AddSubcategoryUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.subcategory.DeleteSubcategoryUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.subcategory.GetSubcategoriesByCategoryIdUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiSubcategory
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiSubcategory.Companion.fromDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана подкатегорий
 */
class SubcategoriesViewModel(
    private val getSubcategoriesByCategoryIdUseCase: GetSubcategoriesByCategoryIdUseCase,
    private val addSubcategoryUseCase: AddSubcategoryUseCase,
    private val deleteSubcategoryUseCase: DeleteSubcategoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubcategoriesUiState())
    val uiState: StateFlow<SubcategoriesUiState> = _uiState.asStateFlow()

    /**
     * Загружает подкатегории для указанной категории
     */
    fun loadSubcategories(categoryId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                getSubcategoriesByCategoryIdUseCase(categoryId).collect { subcategories ->
                    val uiSubcategories = subcategories.map { fromDomain(it) }
                    _uiState.value = _uiState.value.copy(
                        subcategories = uiSubcategories,
                        isLoading = false,
                        categoryId = categoryId,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Ошибка загрузки подкатегорий",
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Добавляет новую подкатегорию
     */
    fun addSubcategory(name: String) {
        val categoryId = _uiState.value.categoryId ?: return

        viewModelScope.launch {
            try {
                addSubcategoryUseCase(name, categoryId)
                // Подкатегории автоматически обновятся через Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Ошибка добавления подкатегории",
                )
            }
        }
    }

    /**
     * Удаляет подкатегорию
     */
    fun deleteSubcategory(subcategoryId: Long) {
        viewModelScope.launch {
            try {
                deleteSubcategoryUseCase(subcategoryId)
                // Подкатегории автоматически обновятся через Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Ошибка удаления подкатегории",
                )
            }
        }
    }

    /**
     * Очищает ошибку
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI состояние для экрана подкатегорий
 */
data class SubcategoriesUiState(
    val subcategories: List<UiSubcategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val categoryId: Long? = null,
)
