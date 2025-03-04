package com.davidbugayov.financeanalyzer.presentation.add

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.utils.EventBus
import com.davidbugayov.financeanalyzer.utils.Event
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
     * Сбрасывает состояние успешного добавления транзакции
     */
    fun resetSuccess() {
        _isSuccess.value = false
    }

    /**
     * Добавляет новую транзакцию
     */
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                addTransactionUseCase(transaction)
                
                // Отправляем событие о добавлении транзакции
                EventBus.emit(Event.TransactionAdded)
                
                // Обновляем виджет баланса
                updateBalanceWidget()
                
                // Устанавливаем флаг успеха после всех операций
                _isSuccess.value = true
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
        val context = getApplication<Application>().applicationContext
        val widgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, "com.davidbugayov.financeanalyzer.widget.BalanceWidget")
        val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)
        
        if (widgetIds.isNotEmpty()) {
            val intent = Intent(context, Class.forName("com.davidbugayov.financeanalyzer.widget.BalanceWidget"))
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            context.sendBroadcast(intent)
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