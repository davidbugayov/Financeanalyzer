package com.davidbugayov.financeanalyzer.presentation.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.presentation.add.components.CategoryPickerDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.ColorPickerDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.CustomCategoryDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.CustomSourceDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.DateField
import com.davidbugayov.financeanalyzer.presentation.add.components.NoteField
import com.davidbugayov.financeanalyzer.presentation.add.components.SourcePickerDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.SourceField
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.add.model.CategoryItem
import com.davidbugayov.financeanalyzer.presentation.components.CancelConfirmationDialog
import com.davidbugayov.financeanalyzer.presentation.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.presentation.components.ErrorDialog
import com.davidbugayov.financeanalyzer.presentation.components.SuccessDialog
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

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
                            onNavigateBack()
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
                // Заголовок с датой
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFormat = SimpleDateFormat("dd MMMM", Locale("ru"))
                    val formattedDate = dateFormat.format(state.selectedDate)

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.onEvent(AddTransactionEvent.ShowDatePicker)
                            }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !state.isExpense,
                            onClick = {
                                if (state.isExpense) viewModel.onEvent(AddTransactionEvent.ToggleTransactionType)
                            },
                            label = { Text(stringResource(R.string.income_type)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = incomeColor.copy(alpha = 0.2f),
                                selectedLabelColor = incomeColor
                            )
                        )

                        FilterChip(
                            selected = state.isExpense,
                            onClick = {
                                if (!state.isExpense) viewModel.onEvent(AddTransactionEvent.ToggleTransactionType)
                            },
                            label = { Text(stringResource(R.string.expense_type)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = expenseColor.copy(alpha = 0.2f),
                                selectedLabelColor = expenseColor
                            )
                        )
                    }
                }

                // Секция "Откуда"
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (state.isExpense) stringResource(R.string.source) else "Куда",
                        style = MaterialTheme.typography.titleMedium,
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
                        },
                        isError = state.sourceError
                    )
                }

                // Секция "Куда" (категории)
                CategorySection(
                    categories = if (state.isExpense) state.expenseCategories else state.incomeCategories,
                    selectedCategory = state.category,
                    onCategorySelected = { category ->
                        viewModel.onEvent(AddTransactionEvent.SetCategory(category.name))
                    },
                    onAddCategoryClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowCustomCategoryDialog)
                    }
                )

                // Поле ввода суммы
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { viewModel.onEvent(AddTransactionEvent.SetAmount(it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = state.amountError,
                    supportingText = if (state.amountError) {
                        { Text("Введите корректную сумму") }
                    } else null,
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(
                                    textAlign = TextAlign.End,
                                    fontWeight = FontWeight.Bold,
                                    color = currentColor
                                ),
                                placeholder = {
                                    Text(
                                        text = "0",
                                        style = MaterialTheme.typography.headlineMedium,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            )

                            Text(
                                text = "₽",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = currentColor,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

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
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                NoteField(
                    note = state.note,
                    onNoteChange = { note ->
                        viewModel.onEvent(AddTransactionEvent.SetNote(note))
                            },
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = { viewModel.onEvent(AddTransactionEvent.AttachReceipt) }) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Прикрепить чек",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопка добавления
                Button(
                    onClick = { viewModel.onEvent(AddTransactionEvent.Submit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentColor
                    )
                ) {
                    Text(
                        text = stringResource(R.string.add_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

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
                        onNavigateBack()
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
                        onNavigateBack()
                    },
                    onDismiss = {
                        showCancelConfirmation = false
                    }
                )
            }

            /* Диалоги, связанные с источником, временно скрыты из-за проблем с импортами */
            if (state.showSourcePicker) {
                SourcePickerDialog(
                    sources = state.sources,
                    onSourceSelected = { source: Source ->
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
                    onColorSelected = { color: Int ->
                        viewModel.onEvent(AddTransactionEvent.SetSourceColor(color))
                        viewModel.onEvent(AddTransactionEvent.HideColorPicker)
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideColorPicker)
                    }
                )
            }
            /* */
        }
    }
}

@Composable
fun SourceSection(
    sources: List<Source>,
    selectedSource: String,
    onSourceSelected: (Source) -> Unit,
    onAddSourceClick: () -> Unit,
    isError: Boolean = false
) {
    Column {
        // Добавляем поле для отображения выбранного источника
        SourceField(
            source = selectedSource,
            color = sources.find { it.name == selectedSource }?.color ?: 0xFF21A038.toInt(),
            isError = isError,
            onClick = { onAddSourceClick() },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Text(
            text = stringResource(R.string.select_source_or_add),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sources) { source ->
                SourceItem(
                    source = source,
                    isSelected = source.name == selectedSource,
                    onClick = { onSourceSelected(source) }
                )
            }

            item {
                AddSourceItem(onClick = onAddSourceClick)
            }
        }
        
        if (isError) {
            Text(
                text = stringResource(R.string.source_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun SourceItem(
    source: Source,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(source.color))
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Здесь можно добавить иконку для источника
            Text(
                text = source.name.first().toString(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = source.name,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun AddSourceItem(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_custom_source),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.add_custom_source),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun CategorySection(
    categories: List<CategoryItem>,
    selectedCategory: String,
    onCategorySelected: (CategoryItem) -> Unit,
    onAddCategoryClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.category),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    isSelected = category.name == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }

            item {
                AddCategoryItem(onClick = onAddCategoryClick)
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun AddCategoryItem(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_custom_category),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.add_custom_category),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}