package com.davidbugayov.financeanalyzer.presentation.add

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.add.components.AddButton
import com.davidbugayov.financeanalyzer.presentation.add.components.AmountField
import com.davidbugayov.financeanalyzer.presentation.add.components.CategoryPickerDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.CategorySection
import com.davidbugayov.financeanalyzer.presentation.add.components.ColorPickerDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.CommentField
import com.davidbugayov.financeanalyzer.presentation.add.components.CustomCategoryDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.CustomSourceDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.DateField
import com.davidbugayov.financeanalyzer.presentation.add.components.SourcePickerDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.SourceSection
import com.davidbugayov.financeanalyzer.presentation.add.components.TransactionHeader
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.components.CancelConfirmationDialog
import com.davidbugayov.financeanalyzer.presentation.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.presentation.components.ErrorDialog
import com.davidbugayov.financeanalyzer.presentation.components.SuccessDialog
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel

/**
 * Экран добавления новой транзакции в стиле CoinKeeper
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showCancelConfirmation by remember { mutableStateOf(false) }

    // Логируем открытие экрана добавления транзакции
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "add_transaction",
            screenClass = "AddTransactionScreen"
        )
    }

    // Функция для обработки выхода с экрана
    fun handleExit() {
        // Обновляем позиции категорий перед выходом
        viewModel.updateCategoryPositions()
        // Сбрасываем поля
        viewModel.resetFields()
        // Возвращаемся назад
        onNavigateBack()
    }

    // Цвета для типов транзакций
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val currentColor = if (state.isExpense) expenseColor else incomeColor
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_transaction)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.title.isNotBlank() || state.amount.isNotBlank() || state.category.isNotBlank() || state.note.isNotBlank()) {
                            showCancelConfirmation = true
                        } else {
                            handleExit()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Заголовок с датой и типом транзакции
                TransactionHeader(
                    date = state.selectedDate,
                    isExpense = state.isExpense,
                    incomeColor = incomeColor,
                    expenseColor = expenseColor,
                    onDateClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowDatePicker)
                    },
                    onToggleTransactionType = {
                        viewModel.onEvent(AddTransactionEvent.ToggleTransactionType)
                    }
                )

                // Секция "Откуда"
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (state.isExpense) stringResource(R.string.source) else "Куда",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    SourceSection(
                        sources = state.sources,
                        selectedSource = state.source,
                        onSourceSelected = { source ->
                            viewModel.onEvent(AddTransactionEvent.SetSource(source.name))
                            viewModel.onEvent(AddTransactionEvent.SetSourceColor(source.color))
                        },
                        onAddSourceClick = {
                            viewModel.onEvent(AddTransactionEvent.ShowCustomSourceDialog)
                        }
                    )
                }

                // Секция категорий с передачей состояния ошибки
                CategorySection(
                    categories = if (state.isExpense) state.expenseCategories else state.incomeCategories,
                    selectedCategory = state.category,
                    onCategorySelected = { category ->
                        viewModel.onEvent(AddTransactionEvent.SetCategory(category.name))
                    },
                    onAddCategoryClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowCustomCategoryDialog)
                    },
                    isError = state.categoryError
                )

                // Поле ввода суммы
                AmountField(
                    amount = state.amount,
                    onAmountChange = { viewModel.onEvent(AddTransactionEvent.SetAmount(it)) },
                    isError = state.amountError,
                    accentColor = currentColor
                )

                // Поле выбора даты
                DateField(
                    date = state.selectedDate,
                    onClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowDatePicker)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                // Поле для комментария с иконкой прикрепления
                CommentField(
                    note = state.note,
                    onNoteChange = { note ->
                        viewModel.onEvent(AddTransactionEvent.SetNote(note))
                    },
                    onAttachClick = {
                        viewModel.onEvent(AddTransactionEvent.AttachReceipt)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопка добавления
                AddButton(
                    onClick = { viewModel.onEvent(AddTransactionEvent.Submit) },
                    color = currentColor
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Диалоги
            if (state.showDatePicker) {
                DatePickerDialog(
                    initialDate = state.selectedDate,
                    onDateSelected = { date ->
                        viewModel.onEvent(AddTransactionEvent.SetDate(date))
                        viewModel.onEvent(AddTransactionEvent.HideDatePicker)
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideDatePicker)
                    }
                )
            }

            if (state.showCategoryPicker) {
                CategoryPickerDialog(
                    categories = if (state.isExpense) state.expenseCategories else state.incomeCategories,
                    onCategorySelected = { category ->
                        viewModel.onEvent(AddTransactionEvent.SetCategory(category))
                    },
                    onCustomCategoryClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowCustomCategoryDialog)
                        viewModel.onEvent(AddTransactionEvent.HideCategoryPicker)
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideCategoryPicker)
                    }
                )
            }

            if (state.showCustomCategoryDialog) {
                CustomCategoryDialog(
                    categoryText = state.customCategory,
                    onCategoryTextChange = { category ->
                        viewModel.onEvent(AddTransactionEvent.SetCustomCategory(category))
                    },
                    onConfirm = {
                        viewModel.onEvent(AddTransactionEvent.AddCustomCategory(state.customCategory))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideCustomCategoryDialog)
                    }
                )
            }

            if (state.isSuccess) {
                SuccessDialog(
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideSuccessDialog)
                        handleExit()
                    },
                    onAddAnother = {
                        viewModel.onEvent(AddTransactionEvent.HideSuccessDialog)
                    }
                )
            }

            state.error?.let { error ->
                ErrorDialog(
                    message = error,
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.ClearError)
                    }
                )
            }

            if (showCancelConfirmation) {
                CancelConfirmationDialog(
                    onConfirm = {
                        showCancelConfirmation = false
                        handleExit()
                    },
                    onDismiss = {
                        showCancelConfirmation = false
                    }
                )
            }

            if (state.showSourcePicker) {
                SourcePickerDialog(
                    sources = state.sources,
                    onSourceSelected = { source ->
                        viewModel.onEvent(AddTransactionEvent.SetSource(source.name))
                        viewModel.onEvent(AddTransactionEvent.SetSourceColor(source.color))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideSourcePicker)
                    },
                    onAddCustomSource = {
                        viewModel.onEvent(AddTransactionEvent.ShowCustomSourceDialog)
                    }
                )
            }

            if (state.showCustomSourceDialog) {
                CustomSourceDialog(
                    sourceName = state.customSource,
                    color = state.sourceColor,
                    onSourceNameChange = { name ->
                        viewModel.onEvent(AddTransactionEvent.SetCustomSource(name))
                    },
                    onColorClick = { selectedColor ->
                        viewModel.onEvent(AddTransactionEvent.SetSourceColor(selectedColor))
                    },
                    onConfirm = {
                        viewModel.onEvent(AddTransactionEvent.AddCustomSource(state.customSource, state.sourceColor))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideCustomSourceDialog)
                    }
                )
            }

            if (state.showColorPicker) {
                ColorPickerDialog(
                    initialColor = state.sourceColor,
                    onColorSelected = { color ->
                        viewModel.onEvent(AddTransactionEvent.SetSourceColor(color))
                        viewModel.onEvent(AddTransactionEvent.HideColorPicker)
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideColorPicker)
                    }
                )
            }
        }
    }
}