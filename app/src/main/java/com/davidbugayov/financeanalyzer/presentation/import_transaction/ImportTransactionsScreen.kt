package com.davidbugayov.financeanalyzer.presentation.import_transaction

import android.app.Activity
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.PermissionUtils
import org.koin.androidx.compose.koinViewModel

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
    profileViewModel: ProfileViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val themeMode = profileViewModel.themeMode.collectAsState().value

    // Получаем состояние из ViewModel в соответствии с MVI
    val state by viewModel.state.collectAsState()

    // Состояние для диалогов
    var showBankInstructionDialog by remember { mutableStateOf(false) }
    var selectedBank by remember { mutableStateOf("") }
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }

    // Функция для обработки выбранного URI по MVI паттерну
    fun processUri(selectedUri: Uri?) {
        if (selectedUri != null) {
            viewModel.handleIntent(ImportTransactionsIntent.StartImport(selectedUri))
        }
    }

    // На Android 15 используем GetContent напрямую
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { selectedUri ->
        processUri(selectedUri)
    }

    // На Android < 15 используем стандартные разрешения и открытие документа
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { selectedUri ->
        processUri(selectedUri)
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            filePickerLauncher.launch(
                arrayOf(
                    "text/csv",
                    "application/pdf",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            )
        } else {
            val activity = context as? Activity
            val permission = PermissionUtils.getReadStoragePermission()

            if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                isAndroid15OrHigher = Build.VERSION.SDK_INT >= 35,
                onOpenSettings = {
                    PermissionUtils.openApplicationSettings(context)
                    showPermissionSettingsDialog = false
                },
                onDismiss = { showPermissionSettingsDialog = false }
            )
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                AppTopBar(
                    title = stringResource(R.string.import_transactions_title),
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
                    .padding(dimensionResource(R.dimen.padding_large))
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_medium))
            ) {
                // Инструкции по импорту
                ImportInstructions()

                // Секция с банками
                Text(
                    text = stringResource(R.string.supported_banks_title),
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                BanksList(
                    onBankClick = { bankName ->
                        selectedBank = bankName
                        showBankInstructionDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_medium)))

                // Кнопка выбора файла
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= 35) {
                            // Для Android 15+ используем ContentResolver напрямую
                            getContentLauncher.launch("*/*")
                        } else {
                            // Для старых версий проверяем разрешения
                            if (PermissionUtils.hasReadExternalStoragePermission(context)) {
                                filePickerLauncher.launch(arrayOf("text/csv", "application/pdf", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                            } else {
                                val permission = PermissionUtils.getReadStoragePermission()
                                storagePermissionLauncher.launch(permission)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_medium))
                    )
                    Text(text = stringResource(R.string.choose_file_button))
                }

                // Отображение результатов импорта
                ImportResultsSection(state)
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
} 