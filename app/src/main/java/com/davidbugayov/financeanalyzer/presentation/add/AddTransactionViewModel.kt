package com.davidbugayov.financeanalyzer.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана добавления транзакций.
 * Отвечает за валидацию и сохранение новых транзакций.
 */
class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    /**
     * Категории расходов для выбора пользователем
     */
    val expenseCategories = listOf(
        "Продукты", "Транспорт", "Развлечения", "Здоровье", 
        "Одежда", "Рестораны", "Коммунальные платежи", "Другое"
    )

    /**
     * Категории доходов для выбора пользователем
     */
    val incomeCategories = listOf(
        "Зарплата", "Фриланс", "Подарки", "Инвестиции", "Другое"
    )

    /**
     * Добавляет новую транзакцию
     * @param transaction Транзакция для добавления
     * @param onSuccess Callback, вызываемый при успешном добавлении
     */
    fun addTransaction(transaction: Transaction, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                addTransactionUseCase(transaction)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Ошибка при сохранении: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Сбрасывает сообщение об ошибке
     */
    fun resetError() {
        _error.value = null
    }

    /**
     * Проверяет валидность введенной суммы
     * @param amount Строка с суммой
     * @return true, если сумма валидна
     */
    fun isAmountValid(amount: String): Boolean {
        return try {
            amount.toDoubleOrNull()?.let { it > 0 } ?: false
        } catch (e: Exception) {
            false
        }
    }
} 