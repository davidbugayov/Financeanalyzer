package com.davidbugayov.financeanalyzer.presentation.import_transaction

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.presentation.import_transaction.components.BankInstructionDialog
import com.davidbugayov.financeanalyzer.presentation.import_transaction.components.BanksList
import com.davidbugayov.financeanalyzer.presentation.import_transaction.components.ImportProgressSection
import com.davidbugayov.financeanalyzer.presentation.import_transaction.components.ImportResults
import com.davidbugayov.financeanalyzer.presentation.import_transaction.components.ImportResultsSection
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportTransactionsIntent
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import com.davidbugayov.financeanalyzer.ui.components.PermissionDialogs.SettingsPermissionDialog
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.PermissionUtils
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import timber.log.Timber

/**
 * Экран импорта транзакций.
 * Использует паттерн MVI (Model-View-Intent) для взаимодействия с ViewModel.
 *
 * @param onNavigateBack Колбэк для навигации назад
 * @param viewModel ViewModel для этого экрана
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTransactionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImportTransactionsViewModel = koinViewModel(),
    preferencesManager: PreferencesManager = koinInject(),
) {
    // Сбрасываем состояние при выходе с экрана
    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.handleIntent(ImportTransactionsIntent.ResetState)
        }
    }

    val context = LocalContext.current
    val themeMode by preferencesManager.themeModeFlow.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Получаем состояние из ViewModel в соответствии с MVI
    val state by viewModel.state.collectAsState()

    // Добавляем логирование для отслеживания изменений состояния
    LaunchedEffect(state) {
        Timber.d(
            "[SCREEN-DEBUG] 📱 Состояние обновлено: isLoading=${state.isLoading}, successCount=${state.successCount}, error=${state.error}",
        )
    }

    // Состояние для диалогов и UI
    var showBankInstructionDialog by remember { mutableStateOf(false) }
    var selectedBank by remember { mutableStateOf("") }
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }
    var expandedInstructions by remember { mutableStateOf(false) }

    // Состояние для анимаций
    var showBanksList by remember { mutableStateOf(false) }

    // Запускаем анимацию с задержкой
    LaunchedEffect(Unit) {
        delay(100)
        showBanksList = true
    }

    // Функция для обработки выбранного URI по MVI паттерну
    fun processUri(selectedUri: Uri?) {
        if (selectedUri != null) {
            viewModel.handleIntent(ImportTransactionsIntent.StartImport(selectedUri))
        }
    }

    // На Android 15 используем GetContent напрямую
    val getContentLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { selectedUri ->
            processUri(selectedUri)
        }

    // На Android < 15 используем стандартные разрешения и открытие документа
    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { selectedUri ->
            processUri(selectedUri)
        }

    // Лаунчер для запроса разрешений
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                filePickerLauncher.launch(
                    arrayOf(
                        "text/csv",
                        "application/pdf",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    ),
                )
            } else {
                val activity = context as? Activity
                val permission = PermissionUtils.getReadStoragePermission()

                if (activity != null) {
                    if (!activity.shouldShowRequestPermissionRationale(permission)) {
                        showPermissionSettingsDialog = true
                    }
                }
            }
        }

    // Функция для проверки и запроса разрешений при необходимости
    fun checkAndRequestPermissions() {
        // Логируем текущее состояние перед сбросом
        Timber.d(
            "Текущее состояние перед сбросом: isLoading=${state.isLoading}, error=${state.error}, successCount=${state.successCount}",
        )

        // Сбрасываем состояние импорта перед выбором нового файла
        viewModel.handleIntent(ImportTransactionsIntent.ResetState)

        // Небольшая задержка для гарантии обновления UI
        coroutineScope.launch {
            delay(100)
            Timber.d(
                "Состояние после сброса: isLoading=${state.isLoading}, error=${state.error}, successCount=${state.successCount}",
            )

            if (Build.VERSION.SDK_INT >= 35) {
                // Android 15+ использует новый API, не требующий разрешений
                getContentLauncher.launch("*/*")
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ и 14 требуют новые разрешения
                permissionLauncher.launch(PermissionUtils.getReadStoragePermission())
            } else if (PermissionUtils.hasReadExternalStoragePermission(context)) {
                // Для Android 12 и ниже, если разрешение уже есть
                filePickerLauncher.launch(
                    arrayOf(
                        "text/csv",
                        "application/pdf",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    ),
                )
            } else {
                // Запрашиваем разрешение для Android 12 и ниже
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    // Применяем тему приложения
    FinanceAnalyzerTheme(themeMode = themeMode) {
        // Диалог для перехода в настройки приложения
        if (showPermissionSettingsDialog) {
            SettingsPermissionDialog(
                onOpenSettings = {
                    openApplicationSettings(context)
                    showPermissionSettingsDialog = false
                },
                onDismiss = { showPermissionSettingsDialog = false },
            )
        }

        Scaffold(
            topBar = {
                AppTopBar(
                    title = stringResource(R.string.import_transactions_title),
                    showBackButton = true,
                    onBackClick = onNavigateBack,
                )
            },
            floatingActionButton = {
                // Плавающая кнопка для выбора файла
                FloatingActionButton(
                    onClick = { checkAndRequestPermissions() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp),
                    elevation =
                        FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.choose_file_button),
                        modifier = Modifier.size(32.dp),
                    )
                }
            },
        ) { paddingValues ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Секция инструкций (сворачиваемая)
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(),
                        ) {
                            // Заголовок с кнопкой сворачивания/разворачивания
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = stringResource(R.string.how_to_import),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )

                                IconButton(onClick = { expandedInstructions = !expandedInstructions }) {
                                    Icon(
                                        imageVector =
                                            if (expandedInstructions) {
                                                Icons.Default.KeyboardArrowUp
                                            } else {
                                                Icons.Default.KeyboardArrowDown
                                            },
                                        contentDescription = null,
                                    )
                                }
                            }

                            // Содержимое инструкций (показывается только когда развернуто)
                            AnimatedVisibility(visible = expandedInstructions) {
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.import_instructions),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )

                                    // Кнопка импорта внутри инструкций для удобства
                                    Button(
                                        onClick = { checkAndRequestPermissions() },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                            ),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 8.dp),
                                        )
                                        Text(text = stringResource(R.string.choose_file_button))
                                    }
                                }
                            }
                        }
                    }

                    // Секция результатов импорта или прогресса - перемещена выше секции банков
                    AnimatedVisibility(
                        visible = state.isLoading || state.successCount > 0 || state.error != null,
                        enter = fadeIn() + expandVertically(),
                    ) {
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                            // Увеличены отступы
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Увеличена тень
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                // Увеличены внутренние отступы
                            ) {
                                // Заголовок с кнопкой закрытия
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = stringResource(R.string.import_results),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                    )

                                    // Кнопка закрытия (X)
                                    IconButton(
                                        onClick = {
                                            viewModel.handleIntent(ImportTransactionsIntent.ResetState)
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(UiR.string.cancel),
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        )
                                    }
                                }

                                when {
                                    // Приоритет отображения: сначала загрузка, потом результаты
                                    state.isLoading -> {
                                        ImportProgressSection(
                                            progress = state.progress,
                                            message = state.progressMessage,
                                            modifier = Modifier.padding(top = 8.dp), // Добавлен отступ сверху
                                            fileName = state.fileName,
                                            bankName = state.bankName,
                                        )
                                    }
                                    // Если есть успешно импортированные транзакции, показываем результаты успеха
                                    // даже если есть ошибка
                                    state.successCount > 0 -> {
                                        ImportResultsSection(
                                            importResults =
                                                ImportResults(
                                                    importedCount = state.successCount,
                                                    skippedCount = state.skippedCount,
                                                    errorMessage = null, // Игнорируем ошибку, если есть успешно импортированные транзакции
                                                    fileName = state.fileName,
                                                    bankName = state.bankName,
                                                ),
                                            modifier = Modifier.padding(top = 8.dp), // Добавлен отступ сверху
                                        )
                                    }
                                    // Показываем ошибку только если нет успешно импортированных транзакций
                                    state.error != null -> {
                                        ImportResultsSection(
                                            importResults =
                                                ImportResults(
                                                    importedCount = 0,
                                                    skippedCount = 0,
                                                    errorMessage = state.error,
                                                    fileName = state.fileName,
                                                    bankName = state.bankName,
                                                ),
                                            modifier = Modifier.padding(top = 8.dp), // Добавлен отступ сверху
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Секция банков с горизонтальной прокруткой
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 80.dp),
                        // Добавлен отступ снизу для FAB
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.supported_banks_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )

                            AnimatedVisibility(
                                visible = showBanksList,
                                enter =
                                    fadeIn(animationSpec = tween(700)) +
                                        slideInVertically(
                                            initialOffsetY = { 50 },
                                            animationSpec =
                                                spring(
                                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                                    stiffness = Spring.StiffnessLow,
                                                ),
                                        ),
                            ) {
                                BanksList(
                                    onBankClick = { bank ->
                                        selectedBank = bank
                                        showBankInstructionDialog = true
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Диалог с инструкциями по получению выписки из банка
            if (showBankInstructionDialog) {
                BankInstructionDialog(
                    bankName = selectedBank,
                    onDismiss = { showBankInstructionDialog = false },
                )
            }
        }
    }
}

fun openApplicationSettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e, "Failed to open application settings")
        // Опционально: показать Toast или Snackbar об ошибке
    }
}
