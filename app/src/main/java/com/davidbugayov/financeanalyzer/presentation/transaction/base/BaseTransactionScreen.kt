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
import androidx.compose.material.icons.filled.Fastfood // Рестораны
import androidx.compose.material.icons.filled.LocalTaxi // Транспорт
import androidx.compose.material.icons.filled.Movie // Развлечения
import androidx.compose.material.icons.filled.ShoppingCart // Продукты
import androidx.compose.material.icons.filled.AttachMoney // Зарплата
import androidx.compose.material.icons.filled.Work // Подработка
import androidx.compose.material.icons.filled.AccountBalance // Депозит
import androidx.compose.material.icons.filled.MoreHoriz // Другое
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardElevation
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
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem as DomainCategoryItem

import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

/**
 * Базовый экран для добавления/редактирования транзакции
 * Содержит общую логику и UI для обоих экранов
 * Улучшенный UI/UX с визуальными акцентами и оптимизированной структурой
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

    // Дефолтные категории для расходов и доходов с иконками
    val displayCategories = if (categories.isEmpty()) {
        if (state.transactionData.isExpense) {
            // Используем предопределенные категории расходов из CategoriesViewModel
            com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel.DEFAULT_EXPENSE_CATEGORIES
        } else {
            // Используем предопределенные категории доходов из CategoriesViewModel  
            com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel.DEFAULT_INCOME_CATEGORIES
        }
    } else {
        categories
    }

    // Функции для быстрого выбора даты (Сегодня/Вчера)
    val onTodayClick = {
        val today = Calendar.getInstance().time
        onEvent(BaseTransactionEvent.SetDate(today))
    }
    
    val onYesterdayClick = {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        onEvent(BaseTransactionEvent.SetDate(yesterday.time))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp) // Уменьшенный отступ
    ) {
        // Верхняя секция с заголовком и типом транзакции (доход/расход)
        TransactionHeader(
            date = state.transactionData.selectedDate,
            isExpense = state.transactionData.isExpense,
            incomeColor = MaterialTheme.colorScheme.primary,
            expenseColor = MaterialTheme.colorScheme.error,
            onDateClick = { onEvent(BaseTransactionEvent.ShowDatePicker) },
            onToggleTransactionType = {
                onEvent(BaseTransactionEvent.ToggleTransactionType)
            },
            forceExpense = state.forceExpense,
            modifier = Modifier.padding(bottom = 16.dp) // Уменьшенный отступ
        )
        
        // Секция выбора источника в карточке с тенью
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Уменьшенный отступ
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Заголовок "Куда" или "Откуда"
                Text(
                    text = if (state.transactionData.isExpense) 
                        stringResource(R.string.transaction_to) 
                    else 
                        stringResource(R.string.transaction_from),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
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
                    },
                    onSourceLongClick = { /* пустой обработчик для совместимости */ }
                )
            }
        }
        
        // Секция выбора категории
        CategorySection(
            categories = displayCategories,
            selectedCategory = state.transactionData.category,
            onCategorySelected = { category -> 
                onEvent(BaseTransactionEvent.SetCategory(category.name))
            },
            onAddCategoryClick = {
                onEvent(BaseTransactionEvent.ShowCategoryPicker)
            },
            isError = state.validationError is ValidationError.CategoryMissing,
            modifier = Modifier.padding(bottom = 16.dp) // Уменьшенный отступ
        )
        
        // Поле ввода суммы с автофокусом и улучшенной валидацией
        AmountField(
            amount = state.transactionData.amount,
            onAmountChange = { onEvent(BaseTransactionEvent.SetAmount(it)) },
            isError = state.validationError is ValidationError.AmountMissing,
            accentColor = if (state.transactionData.isExpense) 
                MaterialTheme.colorScheme.error
            else 
                MaterialTheme.colorScheme.primary,
            autoFocus = true,
            modifier = Modifier.padding(bottom = 16.dp) // Уменьшенный отступ
        )
        
        // Улучшенное поле выбора даты с кнопками "Сегодня" и "Вчера"
        DateField(
            date = state.transactionData.selectedDate,
            onClick = { onEvent(BaseTransactionEvent.ShowDatePicker) },
            onTodayClick = onTodayClick,
            onYesterdayClick = onYesterdayClick,
            modifier = Modifier.padding(bottom = 16.dp) // Уменьшенный отступ
        )
        
        // Поле примечания - теперь без обрамления
        CommentField(
            value = state.transactionData.note,
            onValueChange = { onEvent(BaseTransactionEvent.SetNote(it)) },
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Добавление в кошельки (для дохода)
        if (!state.transactionData.isExpense && !state.editMode) {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp) // Уменьшенный отступ
            ) {
                WalletSelectionSection(
                    addToWallet = state.walletState.addToWallet,
                    selectedWallets = state.walletState.selectedWallets,
                    onToggleAddToWallet = { onEvent(BaseTransactionEvent.ToggleAddToWallet(it)) },
                    onShowWalletSelector = { onEvent(BaseTransactionEvent.ShowWalletSelector) },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Кнопка добавления (более заметная)
        AddButton(
            text = submitButtonText,
            onClick = onSubmit,
            color = if (state.transactionData.isExpense) 
                MaterialTheme.colorScheme.error
            else 
                MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(56.dp) // Увеличенная высота для лучшей доступности
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
    onShowWalletSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
                shape = RoundedCornerShape(8.dp),
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