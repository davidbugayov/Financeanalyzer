package com.davidbugayov.financeanalyzer.presentation.ui

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.ui.components.BankImportCard
import com.davidbugayov.financeanalyzer.utils.PermissionUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Экран импорта транзакций.
 * Позволяет пользователю выбрать файл для импорта и показывает прогресс импорта.
 *
 * @param onNavigateBack Колбэк для навигации назад
 * @param viewModel ViewModel для этого экрана
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTransactionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImportTransactionsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isImporting by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }
    var uri by remember { mutableStateOf<Uri?>(null) }
    var selectedBank by remember { mutableStateOf("") }
    var showBankInstructionDialog by remember { mutableStateOf(false) }
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }

    // Лончер для выбора файлов (CSV или PDF)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { selectedUri: Uri? ->
        if (selectedUri != null) {
            uri = selectedUri
            isImporting = true
            importResult = null

            // Запускаем импорт в корутине
            coroutineScope.launch {
                try {
                    val resultFlow = viewModel.importTransactions(selectedUri)
                    collectImportResults(resultFlow) { result ->
                        importResult = result
                        if (result is ImportResult.Success || result is ImportResult.Error) {
                            isImporting = false
                        }
                    }
                } catch (e: Exception) {
                    importResult = ImportResult.Error("Ошибка при импорте: ${e.message}", e)
                    isImporting = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ошибка: ${e.message}")
                    }
                }
            }
        }
    }

    // Запрашиваем разрешение на чтение файлов
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Запускаем выбор файла с указанием типов файлов
            filePickerLauncher.launch(arrayOf("text/csv", "application/pdf"))
        } else {
            // Если разрешение не предоставлено, показываем диалог для перехода в настройки
            showPermissionSettingsDialog = true
        }
    }

    // Диалог для перехода в настройки приложения
    if (showPermissionSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionSettingsDialog = false },
            title = { Text(text = "Требуется разрешение") },
            text = { 
                Text(
                    text = "Для импорта файлов необходим доступ к хранилищу. " +
                           "Пожалуйста, предоставьте разрешение в настройках приложения."
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        PermissionUtils.openApplicationSettings(context)
                        showPermissionSettingsDialog = false
                    }
                ) {
                    Text("Открыть настройки")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionSettingsDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Импорт транзакций",
                showBackButton = true,
                onBackClick = onNavigateBack,
                titleFontSize = dimensionResource(R.dimen.text_size_normal).value.toInt()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ImportInstructions()

            // Секция с банками
            Text(
                text = "Поддерживаемые банки",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )

            BanksList(
                onBankClick = { bankName ->
                    selectedBank = bankName
                    showBankInstructionDialog = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка выбора файла
            Button(
                onClick = {
                    if (PermissionUtils.hasReadExternalStoragePermission(context)) {
                        filePickerLauncher.launch(arrayOf("text/csv", "application/pdf"))
                    } else {
                        // Запрашиваем разрешение
                        val permission = PermissionUtils.getReadStoragePermission()
                        
                        // Проверяем, можно ли запросить разрешение напрямую
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val activity = context as? Activity
                            if (activity != null && activity.shouldShowRequestPermissionRationale(permission)) {
                                // Можно показать объяснение и запросить разрешение снова
                                permissionLauncher.launch(permission)
                            } else {
                                // Пользователь уже отказывал в разрешении и выбрал "Больше не спрашивать"
                                // Показываем диалог для перехода в настройки
                                showPermissionSettingsDialog = true
                            }
                        } else {
                            // Для старых версий Android просто запрашиваем разрешение
                            permissionLauncher.launch(permission)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isImporting
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = "Выбрать файл для импорта (CSV, PDF)")
            }

            // Отображение результатов импорта
            ImportResultsSection(importResult, isImporting)
        }
        
        // Диалог с инструкциями по получению выписки из банка
        if (showBankInstructionDialog) {
            BankInstructionDialog(
                bankName = selectedBank,
                onDismiss = { showBankInstructionDialog = false }
            )
        }
    }
}

/**
 * Инструкции по импорту транзакций.
 */
@Composable
fun ImportInstructions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp)
            )

            Column {
                Text(
                    text = "Как импортировать транзакции",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "1. Выгрузите выписку в формате CSV из вашего банка\n" +
                            "2. Для Сбербанка также поддерживается формат PDF\n" +
                            "3. Выберите файл выписки через кнопку ниже\n" +
                            "4. Приложение автоматически определит формат\n" +
                            "5. Дождитесь завершения импорта\n\n" +
                            "Нажмите на карточку банка ниже, чтобы узнать как получить выписку.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Отображение списка поддерживаемых банков.
 */
@Composable
fun BanksList(onBankClick: (String) -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BankImportCard(
                bankName = "Сбербанк",
                iconResId = R.drawable.ic_bank_sber,
                modifier = Modifier.weight(1f),
                onClick = { onBankClick("Сбербанк") }
            )

            BankImportCard(
                bankName = "Тинькофф",
                iconResId = R.drawable.ic_bank_tinkoff,
                modifier = Modifier.weight(1f),
                onClick = { onBankClick("Тинькофф") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BankImportCard(
                bankName = "Альфа-Банк",
                iconResId = R.drawable.ic_bank_alfa,
                modifier = Modifier.weight(1f),
                onClick = { onBankClick("Альфа-Банк") }
            )

            BankImportCard(
                bankName = "ВТБ",
                iconResId = R.drawable.ic_bank_vtb,
                modifier = Modifier.weight(1f),
                onClick = { onBankClick("ВТБ") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BankImportCard(
                bankName = "Газпромбанк",
                iconResId = R.drawable.ic_bank_gazprom,
                modifier = Modifier.weight(1f),
                onClick = { onBankClick("Газпромбанк") }
            )

            BankImportCard(
                bankName = "Ozon",
                iconResId = R.drawable.ic_bank_ozon,
                modifier = Modifier.weight(1f),
                onClick = { onBankClick("Ozon") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BankImportCard(
                bankName = "CSV",
                description = "Любой CSV-файл",
                iconResId = R.drawable.ic_file_csv,
                modifier = Modifier.weight(1f),
                onClick = { onBankClick("CSV") }
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Отображение результатов импорта.
 */
@Composable
fun ImportResultsSection(
    importResult: ImportResult?,
    isImporting: Boolean
) {
    if (isImporting || importResult != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    isImporting && importResult is ImportResult.Progress -> {
                        Text(
                            text = "Импорт транзакций...",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { importResult.current.toFloat() / importResult.total },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${importResult.current} из ${importResult.total} транзакций",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = importResult.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    isImporting -> {
                        Text(
                            text = "Импорт транзакций...",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CircularProgressIndicator()
                    }

                    importResult is ImportResult.Success -> {
                        Text(
                            text = "Импорт успешно завершен",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Импортировано: ${importResult.importedCount} транзакций",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (importResult.skippedCount > 0) {
                            Text(
                                text = "Пропущено: ${importResult.skippedCount} транзакций",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Общая сумма: ${importResult.totalAmount.toInt()} ₽",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }

                    importResult is ImportResult.Error -> {
                        Text(
                            text = "Ошибка при импорте",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = importResult.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Выберите файл для импорта транзакций",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Собирает результаты импорта из Flow.
 */
private suspend fun collectImportResults(
    resultFlow: Flow<ImportResult>,
    onResult: (ImportResult) -> Unit
) {
    resultFlow.collectLatest { result ->
        onResult(result)
    }
}

/**
 * Диалог с инструкциями по получению выписки из банка.
 */
@Composable
fun BankInstructionDialog(
    bankName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Как получить выписку из $bankName",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                when (bankName) {
                    "Сбербанк" -> SberbankInstructions()
                    "Тинькофф" -> TinkoffInstructions()
                    "Альфа-Банк" -> AlfaBankInstructions()
                    "ВТБ" -> VTBInstructions()
                    "Газпромбанк" -> GazprombankInstructions()
                    "Ozon" -> OzonInstructions()
                    "CSV" -> CSVInstructions()
                    else -> Text(text = "Инструкции недоступны для этого банка.")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Понятно")
            }
        }
    )
}

@Composable
fun SberbankInstructions() {
    Column {
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Через мобильное приложение Сбербанк:\n\n")
                }
                append("1. Войдите в приложение Сбербанк\n")
                append("2. Перейдите в раздел \"История операций\"\n")
                append("3. Нажмите \"Выписки и справки\"\n")
                append("4. Выберите \"Выписка по счету карты\"\n")
                append("5. Выберите нужную карту\n")
                append("6. Укажите период, за который нужна выписка\n")
                append("7. Нажмите \"Сформировать выписку\"\n")
                append("8. Приложение создаст PDF-файл с выпиской\n")
                append("9. Импортируйте этот PDF-файл напрямую в приложение - PDF поддерживается!\n\n")
            }
        )
        
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Через Сбербанк Онлайн (веб-версия):\n\n")
                }
                append("1. Войдите в личный кабинет Сбербанк Онлайн\n")
                append("2. Выберите карту или счет\n")
                append("3. Нажмите \"Выписки и справки\"\n")
                append("4. Выберите период и формат CSV или PDF\n")
                append("5. Нажмите \"Сформировать\"\n")
                append("6. Скачайте файл выписки\n\n")
            }
        )
        
        Text(
            text = "Наше приложение поддерживает импорт как CSV, так и PDF-файлов выписок Сбербанка. PDF-формат часто удобнее, так как его не нужно дополнительно редактировать.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TinkoffInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Через мобильное приложение:\n\n")
            }
            append("1. Войдите в приложение Тинькофф\n")
            append("2. Выберите карту или счет\n")
            append("3. Нажмите \"Выписка\"\n")
            append("4. Выберите период и формат CSV\n")
            append("5. Нажмите \"Отправить на почту\"\n")
            append("6. Скачайте файл из письма\n\n")
            
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Через веб-версию:\n\n")
            }
            append("1. Войдите в личный кабинет Тинькофф\n")
            append("2. Выберите \"Выписка и справки\"\n")
            append("3. Укажите период и формат CSV\n")
            append("4. Скачайте файл\n")
        }
    )
}

@Composable
fun AlfaBankInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Через мобильное приложение:\n\n")
            }
            append("1. Войдите в приложение Альфа-Банк\n")
            append("2. Выберите карту или счет\n")
            append("3. Нажмите \"Выписка\"\n")
            append("4. Укажите период\n")
            append("5. Выберите \"Отправить на email\"\n")
            append("6. Скачайте CSV-файл из письма\n")
        }
    )
}

@Composable
fun VTBInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Через ВТБ-Онлайн:\n\n")
            }
            append("1. Войдите в личный кабинет ВТБ-Онлайн\n")
            append("2. Выберите карту или счет\n")
            append("3. Перейдите в \"Выписки\"\n")
            append("4. Укажите период\n")
            append("5. Выберите формат CSV\n")
            append("6. Скачайте файл\n")
        }
    )
}

@Composable
fun GazprombankInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Через Газпромбанк-Онлайн:\n\n")
            }
            append("1. Войдите в систему Газпромбанк-Онлайн\n")
            append("2. Выберите счет\n")
            append("3. В разделе \"Выписки\" укажите период\n")
            append("4. Выберите формат CSV\n")
            append("5. Скачайте файл\n")
        }
    )
}

@Composable
fun OzonInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Через приложение Ozon Банк:\n\n")
            }
            append("1. Войдите в приложение\n")
            append("2. Выберите карту\n")
            append("3. Нажмите \"История операций\"\n")
            append("4. Нажмите \"Выписка\"\n")
            append("5. Укажите период\n")
            append("6. Выберите формат CSV\n")
            append("7. Скачайте файл\n")
        }
    )
}

@Composable
fun CSVInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Требования к CSV-файлу:\n\n")
            }
            append("• CSV файл должен содержать данные о транзакциях\n")
            append("• Рекомендуемые поля: дата, сумма, категория, примечание\n")
            append("• Разделитель полей - запятая или точка с запятой\n")
            append("• Кодировка файла - UTF-8\n\n")
            
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Пример формата:\n\n")
            }
            append("Дата,Сумма,Категория,Примечание\n")
            append("01.01.2023,1000,Продукты,Покупка в магазине\n")
            append("02.01.2023,-500,Транспорт,Такси\n")
        }
    )
} 