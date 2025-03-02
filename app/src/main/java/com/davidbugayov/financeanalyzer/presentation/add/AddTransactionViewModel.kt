package com.davidbugayov.financeanalyzer.presentation.add

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.appwidget.AppWidgetManager
import android.content.ComponentName

/**
 * ViewModel для экрана добавления транзакции.
 * Отвечает за валидацию и сохранение новой транзакции.
 */
class AddTransactionViewModel(
    application: Application,
    private val addTransactionUseCase: AddTransactionUseCase
) : AndroidViewModel(application), KoinComponent {

    // Категории для выбора пользователем
    val expenseCategories = listOf(
        "Продукты", "Транспорт", "Развлечения", "Рестораны", 
        "Здоровье", "Одежда", "Жилье", "Связь", "Образование", "Прочее"
    )
    
    val incomeCategories = listOf(
        "Зарплата", "Фриланс", "Подарки", "Проценты", "Аренда", "Прочее"
    )
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> get() = _isSuccess

    /**
     * Добавляет новую транзакцию
     */
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                addTransactionUseCase(transaction)
                _isSuccess.value = true
                
                // Обновляем виджет баланса
                updateBalanceWidget()
            } catch (e: Exception) {
                _error.value = "Ошибка при добавлении транзакции: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Обновляет виджет баланса после изменения данных, но только если виджеты добавлены на домашний экран
     */
    private fun updateBalanceWidget() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val appWidgetManager = AppWidgetManager.getInstance(context)
                
                // Проверяем наличие основного виджета баланса
                val balanceWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, com.davidbugayov.financeanalyzer.widget.BalanceWidget::class.java)
                )
                // Обновляем основной виджет только если он добавлен (есть хотя бы один экземпляр)
                if (balanceWidgetIds.isNotEmpty()) {
                    com.davidbugayov.financeanalyzer.widget.BalanceWidget.updateAllWidgets(context)
                }
                
                // Проверяем наличие маленького виджета баланса
                val smallBalanceWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget::class.java)
                )
                // Обновляем маленький виджет только если он добавлен (есть хотя бы один экземпляр)
                if (smallBalanceWidgetIds.isNotEmpty()) {
                    com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget.updateAllWidgets(context)
                }
            } catch (e: Exception) {
                // Игнорируем ошибки при обновлении виджета
            }
        }
    }

    /**
     * Проверяет, является ли введенная сумма валидной
     */
    fun isAmountValid(amount: String): Boolean {
        return try {
            val value = amount.toDouble()
            value > 0
        } catch (e: Exception) {
            false
        }
    }
} 