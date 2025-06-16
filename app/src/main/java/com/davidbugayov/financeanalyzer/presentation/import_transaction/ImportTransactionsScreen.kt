package com.davidbugayov.financeanalyzer.presentation.import_transaction

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.import_transaction.components.ImportResultsSection
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportTransactionsIntent
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.PermissionUtils
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import kotlinx.coroutines.delay
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
    val context = LocalContext.current
    val themeMode by preferencesManager.themeModeFlow.collectAsState()
    // Получаем состояние из ViewModel в соответствии с MVI
    val state by viewModel.state.collectAsState()
    // Состояние для диалогов
    var showBankInstructionDialog by remember { mutableStateOf(false) }
    var selectedBank by remember { mutableStateOf("") }
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }
    // Состояние для анимаций
    var showInstructions by remember { mutableStateOf(false) }
    var showBanksList by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    // Запускаем анимацию с задержкой для каскадного эффекта
    LaunchedEffect(Unit) {
        showInstructions = true
        delay(200)
        showBanksList = true
        showButton = true
    }

    // На Android 15 используем GetContent напрямую
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { selectedUri ->
        if (selectedUri != null) {
            viewModel.handleIntent(ImportTransactionsIntent.StartImport(selectedUri))
        }
    }

    // На Android < 15 используем стандартные разрешения и открытие документа
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { selectedUri ->
        if (selectedUri != null) {
            viewModel.handleIntent(ImportTransactionsIntent.StartImport(selectedUri))
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
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

    // Применяем тему приложения
    FinanceAnalyzerTheme(themeMode = themeMode) {
        // Диалог для перехода в настройки приложения
        if (showPermissionSettingsDialog) {
            PermissionDialog(
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
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Инструкции по импорту с анимацией
                AnimatedVisibility(
                    visible = showInstructions,
                    enter = fadeIn(animationSpec = tween(700)) +
                        slideInVertically(
                            initialOffsetY = { -50 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                        ),
                ) {
                    ImportInstructions()
                }

                // Секция с банками с анимацией
                AnimatedVisibility(
                    visible = showBanksList,
                    enter = fadeIn(animationSpec = tween(700)) +
                        slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = spring(
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

                // Секция с результатами импорта
                AnimatedVisibility(
                    visible = state.successCount > 0 || state.isLoading || state.error != null,
                    enter = fadeIn(animationSpec = tween(500)) +
                        expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                            ),
                        ),
                    exit = fadeOut(animationSpec = tween(300)),
                ) {
                    ImportResultsSection(
                        state = state,
                        onDismiss = { viewModel.handleIntent(ImportTransactionsIntent.ResetState) },
                    )
                }

                // Кнопка выбора файла
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= 35) {
                            getContentLauncher.launch("*/*")
                        } else {
                            // Проверяем разрешение на чтение файлов
                            if (PermissionUtils.hasReadExternalStoragePermission(context)) {
                                filePickerLauncher.launch(
                                    arrayOf(
                                        "text/csv",
                                        "application/pdf",
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    ),
                                )
                            } else {
                                // Запрашиваем разрешение
                                permissionLauncher.launch(PermissionUtils.getReadStoragePermission())
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                    enabled = !state.isLoading,
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = if (state.isLoading) {
                            stringResource(R.string.importing_file)
                        } else {
                            stringResource(R.string.choose_file_button)
                        },
                    )
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

@Composable
fun ImportInstructions() {
    // Implementation for import instructions
    Text(
        text = "Выберите файл выписки из банка для импорта транзакций.",
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
fun BanksList(onBankClick: (String) -> Unit) {
    // Implementation for banks list
    Text(
        text = "Поддерживаемые банки: Сбербанк, Тинькофф, Альфа-Банк, Озон",
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
fun PermissionDialog(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    // Implementation for permission dialog
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Требуется разрешение") },
        text = {
            Text(
                "Для импорта файлов необходимо разрешение на доступ к файлам. Пожалуйста, предоставьте разрешение в настройках приложения.",
            )
        },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text("Настройки")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}

@Composable
fun BankInstructionDialog(bankName: String, onDismiss: () -> Unit) {
    // Implementation for bank instruction dialog
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Инструкция для $bankName") },
        text = { Text("Как получить выписку из $bankName для импорта...") },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Понятно")
            }
        },
    )
}

fun openApplicationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e, "Failed to open application settings")
    }
}
