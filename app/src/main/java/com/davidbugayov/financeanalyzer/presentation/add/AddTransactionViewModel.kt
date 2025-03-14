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
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionState
import com.davidbugayov.financeanalyzer.presentation.add.model.CategoryItem
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.math.BigDecimal
import java.util.Date

/**
 * ViewModel для экрана добавления транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
class AddTransactionViewModel(
    application: Application,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val categoriesViewModel: CategoriesViewModel
) : AndroidViewModel(application), KoinComponent {

    // Категории для выбора пользователем
    private val _expenseCategories = MutableStateFlow(
        listOf(
            CategoryItem("Продукты", Icons.Default.ShoppingCart),
            CategoryItem("Транспорт", Icons.Default.DirectionsCar),
            CategoryItem("Развлечения", Icons.Default.Movie),
            CategoryItem("Рестораны", Icons.Default.Restaurant),
            CategoryItem("Здоровье", Icons.Default.LocalHospital),
            CategoryItem("Одежда", Icons.Default.Checkroom),
            CategoryItem("Жилье", Icons.Default.Home),
            CategoryItem("Связь", Icons.Default.Phone),
            CategoryItem("Питомец", Icons.Default.Pets),
            CategoryItem("Прочее", Icons.Default.MoreHoriz),
            CategoryItem("Другое", Icons.Default.Add)
        )
    )
    val expenseCategories = _expenseCategories.asStateFlow()

    private val _incomeCategories = MutableStateFlow(
        listOf(
            CategoryItem("Зарплата", Icons.Default.Payments),
            CategoryItem("Фриланс", Icons.Default.Computer),
            CategoryItem("Подарки", Icons.Default.CardGiftcard),
            CategoryItem("Проценты", Icons.AutoMirrored.Filled.TrendingUp),
            CategoryItem("Аренда", Icons.Default.HomeWork),
            CategoryItem("Прочее", Icons.Default.MoreHoriz),
            CategoryItem("Другое", Icons.Default.Add)
        )
    )
    val incomeCategories = _incomeCategories.asStateFlow()

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            categoriesViewModel.expenseCategories.collect { categories ->
                _state.update { it.copy(expenseCategories = categories) }
            }
        }
        viewModelScope.launch {
            categoriesViewModel.incomeCategories.collect { categories ->
                _state.update { it.copy(incomeCategories = categories) }
            }
        }
    }

    /**
     * Обрабатывает события экрана добавления транзакции
     */
    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.SetAmount -> {
                // Автоматически заменяем запятую на точку при вводе
                _state.update { it.copy(amount = event.amount.replace(",", ".")) }
            }
            is AddTransactionEvent.SetTitle -> {
                _state.update { it.copy(title = event.title) }
            }
            is AddTransactionEvent.SetCategory -> {
                _state.update {
                    it.copy(
                        category = event.category,
                        showCategoryPicker = false
                    )
                }
            }
            is AddTransactionEvent.SetNote -> {
                _state.update { it.copy(note = event.note) }
            }
            is AddTransactionEvent.SetDate -> {
                _state.update {
                    it.copy(
                        selectedDate = event.date,
                        showDatePicker = false
                    )
                }
            }
            is AddTransactionEvent.SetCustomCategory -> {
                _state.update { it.copy(customCategory = event.category) }
            }
            is AddTransactionEvent.AddCustomCategory -> {
                addCustomCategory(event.category)
            }
            is AddTransactionEvent.ToggleTransactionType -> {
                _state.update { it.copy(isExpense = !it.isExpense) }
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
                _state.update {
                    it.copy(
                        showCustomCategoryDialog = false,
                        customCategory = ""
                    )
                }
            }
            is AddTransactionEvent.ShowCancelConfirmation -> {
                _state.update { it.copy(showCancelConfirmation = true) }
            }
            is AddTransactionEvent.HideCancelConfirmation -> {
                _state.update { it.copy(showCancelConfirmation = false) }
            }
            is AddTransactionEvent.Submit -> {
                addTransaction()
            }
            is AddTransactionEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
            is AddTransactionEvent.HideSuccessDialog -> {
                _state.update { it.copy(isSuccess = false) }
            }
            is AddTransactionEvent.ResetFields -> {
                resetFields()
            }
        }
    }

    /**
     * Сбрасывает все поля формы к начальным значениям
     */
    private fun resetFields() {
        _state.update {
            it.copy(
                title = "",
                amount = "",
                category = "",
                note = "",
                isExpense = true,
                selectedDate = Date(),
                amountError = false,
                categoryError = false,
                titleError = false
            )
        }
    }

    private fun validateInput(): Boolean {
        val amount = _state.value.amount.replace(",", ".").toBigDecimalOrNull()

        val hasErrors = amount == null || amount <= BigDecimal.ZERO || _state.value.category.isBlank()

        _state.update {
            it.copy(
                amountError = amount == null || amount <= BigDecimal.ZERO,
                categoryError = _state.value.category.isBlank()
            )
        }

        return !hasErrors
    }

    private fun addTransaction() {
        if (!validateInput()) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val amount = _state.value.amount.replace(",", ".").toBigDecimal()
                val transaction = Transaction(
                    title = _state.value.title.ifBlank { null },
                    amount = amount,
                    category = _state.value.category,
                    isExpense = _state.value.isExpense,
                    date = _state.value.selectedDate,
                    note = _state.value.note.ifBlank { null }
                )

                addTransactionUseCase(transaction)
                _state.update { it.copy(isSuccess = true) }

                // Сбрасываем поля после успешного добавления транзакции
                resetFields()

                // Обновляем виджеты
                updateBalanceWidget()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Добавляет новую пользовательскую категорию
     */
    private fun addCustomCategory(category: String) {
        if (category.isBlank()) return
        categoriesViewModel.addCustomCategory(category, _state.value.isExpense)
        _state.update {
            it.copy(
                category = category,
                showCategoryPicker = false,
                showCustomCategoryDialog = false,
                customCategory = ""
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