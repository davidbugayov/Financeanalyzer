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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.import_transaction.components.BankInstructionDialog
import com.davidbugayov.financeanalyzer.presentation.import_transaction.components.BanksList
import com.davidbugayov.financeanalyzer.presentation.import_transaction.components.ImportInstructions
import com.davidbugayov.financeanalyzer.presentation.import_transaction.components.ImportResultsSection
import com.davidbugayov.financeanalyzer.presentation.import_transaction.components.PermissionDialog
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
        delay(200)
        showButton = true
    }

    // Функция для обработки выбранного URI по MVI паттерну
    fun processUri(selectedUri: Uri?) {
        if (selectedUri != null) {
            viewModel.handleIntent(ImportTransactionsIntent.StartImport(selectedUri))
        }
    }

    // На Android 15 используем GetContent напрямую
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { selectedUri ->
        processUri(selectedUri)
    }

    // На Android < 15 используем стандартные разрешения и открытие документа
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { selectedUri ->
        processUri(selectedUri)
    }

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
                    titleFontSize = dimensionResource(R.dimen.text_size_normal).value.toInt(),
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(dimensionResource(R.dimen.padding_large))
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_medium)),
            ) {
                // Инструкции по импорту с анимацией
                AnimatedVisibility(
                    visible = showInstructions,
                    enter = fadeIn(animationSpec = tween(700)) + slideInVertically(
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

                // Оставляем только ImportResultsSection для показа результатов импорта
                AnimatedVisibility(
                    visible = state.successCount > 0 || state.isLoading || state.error != null,
                    enter = fadeIn(animationSpec = tween(500)) +
                        expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                        ),
                    exit = fadeOut(animationSpec = tween(300)),
                ) {
                    ImportResultsSection(state)
                }

                // Кнопка выбора файла
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Кнопка выбора файла
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= 35) {
                                getContentLauncher.launch("*/*")
                            } else {
                                filePickerLauncher.launch(
                                    arrayOf(
                                        "text/csv",
                                        "application/pdf",
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    ),
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.button_height)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_button)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = androidx.compose.ui.graphics.Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = dimensionResource(R.dimen.button_elevation),
                        ),
                        // Добавляем возможность отключения кнопки во время импорта
                        enabled = !state.isLoading,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(
                                end = dimensionResource(R.dimen.padding_medium),
                            ),
                        )
                        Text(
                            text = if (state.isLoading) {
                                stringResource(R.string.importing_file)
                            } else {
                                stringResource(R.string.choose_file_button)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = androidx.compose.ui.graphics.Color.White,
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
        // Опционально: показать Toast или Snackbar об ошибке
    }
}
