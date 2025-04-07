package com.davidbugayov.financeanalyzer.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.presentation.budget.model.BudgetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class BudgetViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    init {
        loadBudgetCategories()
    }

    private fun loadBudgetCategories() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                // TODO: Implement loading budget categories from repository
                _state.value = _state.value.copy(
                    categories = listOf(
                        BudgetCategory("Продукты", 5000.0, 0.0, "products"),
                        BudgetCategory("Развлечения", 3000.0, 0.0, "entertainment"),
                        BudgetCategory("Транспорт", 2000.0, 0.0, "transport")
                    ),
                    isLoading = false
                )
            } catch (e: Exception) {
                Timber.e(e, "Error loading budget categories")
                _state.value = _state.value.copy(
                    error = e.message ?: "Unknown error",
                    isLoading = false
                )
            }
        }
    }

    fun addCategory(name: String, limit: Double) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                // TODO: Implement adding budget category to repository
                val newCategory = BudgetCategory(name, limit, 0.0, name.lowercase())
                _state.value = _state.value.copy(
                    categories = _state.value.categories + newCategory,
                    isLoading = false
                )
            } catch (e: Exception) {
                Timber.e(e, "Error adding budget category")
                _state.value = _state.value.copy(
                    error = e.message ?: "Unknown error",
                    isLoading = false
                )
            }
        }
    }

    fun updateCategory(category: BudgetCategory) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                // TODO: Implement updating budget category in repository
                val updatedCategories = _state.value.categories.map {
                    if (it.id == category.id) category else it
                }
                _state.value = _state.value.copy(
                    categories = updatedCategories,
                    isLoading = false
                )
            } catch (e: Exception) {
                Timber.e(e, "Error updating budget category")
                _state.value = _state.value.copy(
                    error = e.message ?: "Unknown error",
                    isLoading = false
                )
            }
        }
    }

    fun deleteCategory(category: BudgetCategory) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                // TODO: Implement deleting budget category from repository
                val updatedCategories = _state.value.categories.filter { it.id != category.id }
                _state.value = _state.value.copy(
                    categories = updatedCategories,
                    isLoading = false
                )
            } catch (e: Exception) {
                Timber.e(e, "Error deleting budget category")
                _state.value = _state.value.copy(
                    error = e.message ?: "Unknown error",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
} 