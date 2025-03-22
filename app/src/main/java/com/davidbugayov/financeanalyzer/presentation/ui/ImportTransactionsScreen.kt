package com.davidbugayov.financeanalyzer.presentation.ui

import android.Manifest
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
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

    var importResult by remember { mutableStateOf<ImportResult?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    // Запускаем Activity для выбора файла
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            isImporting = true
            coroutineScope.launch {
                try {
                    val resultFlow = viewModel.importTransactions(uri)
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
            filePickerLauncher.launch("*/*")
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Необходимо разрешение на доступ к файлам")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Импорт транзакций") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
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

            BanksList()

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка выбора файла
            Button(
                onClick = {
                    if (PermissionUtils.hasReadExternalStoragePermission(context)) {
                        filePickerLauncher.launch("*/*")
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
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
                Text(text = "Выбрать файл для импорта")
            }

            // Отображение результатов импорта
            ImportResultsSection(importResult, isImporting)
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
                            "2. Выберите файл выписки через кнопку ниже\n" +
                            "3. Приложение автоматически определит формат\n" +
                            "4. Дождитесь завершения импорта",
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
fun BanksList() {
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
                modifier = Modifier.weight(1f)
            )

            BankImportCard(
                bankName = "Тинькофф",
                iconResId = R.drawable.ic_bank_tinkoff,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BankImportCard(
                bankName = "Альфа-Банк",
                iconResId = R.drawable.ic_bank_alfa,
                modifier = Modifier.weight(1f)
            )

            BankImportCard(
                bankName = "ВТБ",
                iconResId = R.drawable.ic_bank_vtb,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BankImportCard(
                bankName = "Газпромбанк",
                iconResId = R.drawable.ic_bank_gazprom,
                modifier = Modifier.weight(1f)
            )

            BankImportCard(
                bankName = "Ozon",
                iconResId = R.drawable.ic_bank_ozon,
                modifier = Modifier.weight(1f)
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
                modifier = Modifier.weight(1f)
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