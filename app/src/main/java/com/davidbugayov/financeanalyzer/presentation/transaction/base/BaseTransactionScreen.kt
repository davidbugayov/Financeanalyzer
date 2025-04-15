package com.davidbugayov.financeanalyzer.presentation.transaction.base.components

// Explicitly list all required imports based on usage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.foundation.border

import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.AddButton
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.AmountField
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.CategorySection
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.SourceSection
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.TransactionHeader
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.DateField
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.CommentField
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionState
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.ValidationError

import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Базовый экран для добавления/редактирования транзакции
 * Содержит общую логику и UI для обоих экранов
 *
 * @param state Состояние транзакции
 * @param onEvent Обработчик событий
 * @param onSubmit Обработчик отправки формы
 * @param submitButtonText Текст кнопки отправки
 * @param modifier Модификатор для корневого элемента
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseTransactionScreen(
    state: BaseTransactionState,
    onEvent: (BaseTransactionEvent) -> Unit,
    onSubmit: () -> Unit,
    submitButtonText: String,
    modifier: Modifier = Modifier
) {
    val categories = if (state.transactionData.isExpense) state.expenseCategories else state.incomeCategories
    val sources = state.sources

    // Создаем дефолтные источники, если список пуст
    val displaySources = if (sources.isEmpty()) {
        listOf(
            com.davidbugayov.financeanalyzer.presentation.transaction.base.model.SourceItem("Сбер", 0xFF4CAF50.toInt(), false),
            com.davidbugayov.financeanalyzer.presentation.transaction.base.model.SourceItem("Т-Банк", 0xFFFFEB3B.toInt(), false),
            com.davidbugayov.financeanalyzer.presentation.transaction.base.model.SourceItem("Альфа", 0xFFF44336.toInt(), false),
            com.davidbugayov.financeanalyzer.presentation.transaction.base.model.SourceItem("Наличные", 0xFF9E9E9E.toInt(), false)
        )
    } else {
        sources
    }

    // Дефолтные категории для расходов и доходов
    val displayCategories = if (categories.isEmpty()) {
        if (state.transactionData.isExpense) {
            listOf(
                com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem(
                    name = "Продукты", 
                    count = 0, 
                    image = null, 
                    isCustom = false
                ),
                com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem(
                    name = "Рестораны", 
                    count = 0, 
                    image = null, 
                    isCustom = false
                ),
                com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem(
                    name = "Транспорт", 
                    count = 0, 
                    image = null, 
                    isCustom = false
                ),
                com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem(
                    name = "Развлечения", 
                    count = 0, 
                    image = null, 
                    isCustom = false
                )
            )
        } else {
            listOf(
                com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem(
                    name = "Зарплата", 
                    count = 0, 
                    image = null, 
                    isCustom = false
                ),
                com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem(
                    name = "Подработка", 
                    count = 0, 
                    image = null, 
                    isCustom = false
                ),
                com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem(
                    name = "Депозит", 
                    count = 0, 
                    image = null, 
                    isCustom = false
                ),
                com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem(
                    name = "Другое", 
                    count = 0, 
                    image = null, 
                    isCustom = false
                )
            )
        }
    } else {
        categories
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Верхняя строка с типом транзакции и датой перемещена выше
        TransactionHeader(
            date = state.transactionData.selectedDate,
            isExpense = state.transactionData.isExpense,
            incomeColor = MaterialTheme.colorScheme.primary,
            expenseColor = MaterialTheme.colorScheme.error,
            onDateClick = { onEvent(BaseTransactionEvent.ShowDatePicker) },
            onToggleTransactionType = {
                if (state.transactionData.isExpense) {
                    onEvent(BaseTransactionEvent.ForceSetIncomeType)
                } else {
                    onEvent(BaseTransactionEvent.ForceSetExpenseType)
                }
            },
            forceExpense = false, // Убираем принудительный режим расхода
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Секция "Куда" или "Откуда" (в зависимости от типа транзакции)
        Text(
            text = if (state.transactionData.isExpense) stringResource(R.string.transaction_to) else stringResource(R.string.transaction_from),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Используем SourceSection с отображением дефолтных источников
        SourceSection(
            sources = displaySources.map { sourceItem -> 
                com.davidbugayov.financeanalyzer.domain.model.Source(
                    id = 0,
                    name = sourceItem.name,
                    color = sourceItem.color,
                    isCustom = sourceItem.isCustom
                )
            },
            selectedSource = state.transactionData.source,
            onSourceSelected = { source ->
                onEvent(BaseTransactionEvent.SetSource(source.name))
                onEvent(BaseTransactionEvent.SetSourceColor(source.color))
            },
            onAddSourceClick = {
                onEvent(BaseTransactionEvent.ShowSourcePicker)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Секция категорий с использованием дефолтных категорий
        CategorySection(
            categories = displayCategories,
            selectedCategory = state.transactionData.category,
            onCategorySelected = { category -> 
                onEvent(BaseTransactionEvent.SetCategory(category.name))
            },
            onAddCategoryClick = {
                onEvent(BaseTransactionEvent.ShowCategoryPicker)
            },
            isError = state.validationError is ValidationError.CategoryMissing
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Поле суммы
        AmountField(
            amount = state.transactionData.amount,
            onAmountChange = { onEvent(BaseTransactionEvent.SetAmount(it)) },
            isError = state.validationError is ValidationError.AmountMissing,
            accentColor = if (state.transactionData.isExpense) 
                MaterialTheme.colorScheme.error
            else 
                MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Поле выбора даты (перемещено после суммы)
        DateField(
            date = state.transactionData.selectedDate,
            onClick = { onEvent(BaseTransactionEvent.ShowDatePicker) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Поле примечания
        CommentField(
            value = state.transactionData.note,
            onValueChange = { onEvent(BaseTransactionEvent.SetNote(it)) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // Добавление в кошельки (для дохода)
        if (!state.transactionData.isExpense && !state.editMode) {
            Spacer(modifier = Modifier.height(16.dp))
            
            WalletSelectionSection(
                addToWallet = state.walletState.addToWallet,
                selectedWallets = state.walletState.selectedWallets,
                onToggleAddToWallet = { onEvent(BaseTransactionEvent.ToggleAddToWallet(it)) },
                onShowWalletSelector = { onEvent(BaseTransactionEvent.ShowWalletSelector) }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Кнопка добавления
        AddButton(
            text = submitButtonText,
            onClick = onSubmit,
            color = if (state.transactionData.isExpense) 
                MaterialTheme.colorScheme.error
            else 
                MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }

    // DatePickerDialog
    if (state.dialogStateTransaction.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.transactionData.selectedDate.time
        )
                
        DatePickerDialog(
            onDismissRequest = { onEvent(BaseTransactionEvent.HideDatePicker) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onEvent(BaseTransactionEvent.SetDate(Date(millis)))
                    }
                    onEvent(BaseTransactionEvent.HideDatePicker)
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(BaseTransactionEvent.HideDatePicker) }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun WalletAddSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    androidx.compose.material3.Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = androidx.compose.material3.SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
fun WalletSelectionButton(
    selectedCount: Int,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (selectedCount > 0) 
                    stringResource(R.string.selected_wallets, selectedCount)
                else 
                    stringResource(R.string.select_wallets),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WalletSelectionSection(
    addToWallet: Boolean,
    selectedWallets: List<String>,
    onToggleAddToWallet: (Boolean) -> Unit,
    onShowWalletSelector: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Настройки кошелька", 
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Добавить в кошелек",
                style = MaterialTheme.typography.bodyLarge
            )
            
            androidx.compose.material3.Switch(
                checked = addToWallet,
                onCheckedChange = onToggleAddToWallet
            )
        }
        
        if (addToWallet) {
            Spacer(modifier = Modifier.height(8.dp))
            
            androidx.compose.material3.Button(
                onClick = onShowWalletSelector,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Выбрать кошельки (${selectedWallets.size})")
            }
        }
    }
}

/**
 * Преобразует SourceItem из модели BaseTransactionState в Source для компонентов UI
 */
private fun com.davidbugayov.financeanalyzer.presentation.transaction.base.model.SourceItem.toSource(): com.davidbugayov.financeanalyzer.domain.model.Source {
    return com.davidbugayov.financeanalyzer.domain.model.Source(
        name = this.name,
        color = this.color,
        isCustom = this.isCustom
    )
} 