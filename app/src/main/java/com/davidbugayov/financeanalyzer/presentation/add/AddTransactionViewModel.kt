package com.davidbugayov.financeanalyzer.presentation.add

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.add.event.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.add.model.CategoryItem
import com.davidbugayov.financeanalyzer.presentation.add.state.AddTransactionState
import com.davidbugayov.financeanalyzer.utils.Event
import com.davidbugayov.financeanalyzer.utils.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

/**
 * ViewModel для экрана добавления транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
class AddTransactionViewModel(
    application: Application,
    private val addTransactionUseCase: AddTransactionUseCase
) : AndroidViewModel(application), KoinComponent {

    // Категории для выбора пользователем
    val expenseCategories = listOf(
        CategoryItem("Продукты", Icons.Default.ShoppingCart),
        CategoryItem("Транспорт", Icons.Default.DirectionsCar),
        CategoryItem("Развлечения", Icons.Default.Movie),
        CategoryItem("Рестораны", Icons.Default.Restaurant),
        CategoryItem("Здоровье", Icons.Default.LocalHospital),
        CategoryItem("Одежда", Icons.Default.Checkroom),
        CategoryItem("Жилье", Icons.Default.Home),
        CategoryItem("Связь", Icons.Default.Phone),
        CategoryItem("Образование", Icons.Default.School),
        CategoryItem("Прочее", Icons.Default.MoreHoriz),
        CategoryItem("Другое", Icons.Default.Add)
    )
    
    val incomeCategories = listOf(
        CategoryItem("Зарплата", Icons.Default.Payments),
        CategoryItem("Фриланс", Icons.Default.Computer),
        CategoryItem("Подарки", Icons.Default.CardGiftcard),
        CategoryItem("Проценты", Icons.AutoMirrored.Filled.TrendingUp),
        CategoryItem("Аренда", Icons.Default.HomeWork),
        CategoryItem("Прочее", Icons.Default.MoreHoriz),
        CategoryItem("Другое", Icons.Default.Add)
    )

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    /**
     * Обрабатывает события экрана добавления транзакции
     */
    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.SetTitle -> {
                _state.update { it.copy(title = event.title) }
            }
            is AddTransactionEvent.SetAmount -> {
                _state.update { it.copy(amount = event.amount) }
            }
            is AddTransactionEvent.SetCategory -> {
                _state.update { it.copy(category = event.category) }
            }
            is AddTransactionEvent.SetNote -> {
                _state.update { it.copy(note = event.note) }
            }
            is AddTransactionEvent.SetExpenseType -> {
                _state.update { it.copy(isExpense = event.isExpense) }
            }
            is AddTransactionEvent.SetDate -> {
                _state.update { it.copy(selectedDate = event.date) }
            }
            is AddTransactionEvent.AddTransaction -> {
                addTransaction(event.transaction)
            }
            is AddTransactionEvent.ResetSuccess -> {
                _state.update { it.copy(isSuccess = false) }
            }
            is AddTransactionEvent.ResetError -> {
                _state.update { it.copy(error = null) }
            }
            is AddTransactionEvent.ShowDatePicker -> {
                _state.update { it.copy(showDatePicker = true) }
            }
            is AddTransactionEvent.HideDatePicker -> {
                _state.update { it.copy(showDatePicker = false) }
            }
            is AddTransactionEvent.ShowCategoryPicker -> {
                _state.update { it.copy(showCategoryPicker = true) }
            }
            is AddTransactionEvent.HideCategoryPicker -> {
                _state.update { it.copy(showCategoryPicker = false) }
            }
            is AddTransactionEvent.ShowCustomCategoryDialog -> {
                _state.update { it.copy(showCustomCategoryDialog = true) }
            }
            is AddTransactionEvent.HideCustomCategoryDialog -> {
                _state.update { it.copy(showCustomCategoryDialog = false) }
            }
            is AddTransactionEvent.SetCustomCategory -> {
                _state.update { it.copy(customCategory = event.category) }
            }
            is AddTransactionEvent.AddCustomCategory -> {
                _state.update {
                    it.copy(
                        category = event.category,
                        customCategory = "",
                        showCustomCategoryDialog = false
                    )
                }
            }
        }
    }

    /**
     * Добавляет новую транзакцию
     */
    private fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            addTransactionUseCase(transaction).fold(
                onSuccess = {
                    // Отправляем событие о добавлении транзакции
                    EventBus.emit(Event.TransactionAdded)

                    // Обновляем виджет баланса
                    updateBalanceWidget()

                    // Устанавливаем флаг успеха после всех операций
                    _state.update { it.copy(isSuccess = true, isLoading = false) }
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            error = "Ошибка при добавлении транзакции: ${exception.message}",
                            isLoading = false
                        )
                    }
                }
            )
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

        // Обновляем малый виджет баланса
        val smallWidgetComponent = ComponentName(context, "com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget")
        val smallWidgetIds = widgetManager.getAppWidgetIds(smallWidgetComponent)

        if (smallWidgetIds.isNotEmpty()) {
            val intent = Intent(context, Class.forName("com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget"))
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, smallWidgetIds)
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