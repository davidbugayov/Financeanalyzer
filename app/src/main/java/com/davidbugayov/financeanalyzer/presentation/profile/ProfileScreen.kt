package com.davidbugayov.financeanalyzer.presentation.profile

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.core.view.WindowCompat
import com.davidbugayov.financeanalyzer.BuildConfig
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.profile.components.AnalyticsSection
import com.davidbugayov.financeanalyzer.presentation.profile.components.AppInfoSection
import com.davidbugayov.financeanalyzer.presentation.profile.components.ExportButton
import com.davidbugayov.financeanalyzer.presentation.profile.components.ExportDescription
import com.davidbugayov.financeanalyzer.presentation.profile.components.NotificationSettingsDialog
import com.davidbugayov.financeanalyzer.presentation.profile.components.ProfileTopBar
import com.davidbugayov.financeanalyzer.presentation.profile.components.SettingsSection
import com.davidbugayov.financeanalyzer.presentation.profile.components.ThemeSelectionDialog
import com.davidbugayov.financeanalyzer.presentation.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.presentation.profile.model.ProfileState
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel

/**
 * Экран профиля пользователя.
 * Отображает информацию о пользователе и настройки приложения.
 *
 * Следует принципам MVI и SOLID:
 * - Модель (Model): ProfileViewModel и ProfileState
 * - Представление (View): Composable компоненты
 * - Интент (Intent): ProfileEvent
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLibraries: () -> Unit,
    onNavigateToChart: () -> Unit,
    onNavigateToImport: () -> Unit
) {
    // Получаем текущее состояние из ViewModel
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Получаем информацию о версии приложения
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
    
    val appVersion = remember { packageInfo?.versionName ?: "Unknown" }
    val buildVersion = remember { BuildConfig.VERSION_CODE.toString() }

    // Логируем открытие экрана профиля
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "profile",
            screenClass = "ProfileScreen"
        )
    }

    // Устанавливаем темные иконки в статус-баре
    SetupStatusBarAppearance(state.themeMode, context)

    // Обновляем UI при изменении темы
    FinanceAnalyzerTheme(themeMode = state.themeMode) {
        // Показываем сообщения о результате экспорта
        HandleExportMessages(state, snackbarHostState, viewModel)

        Scaffold(
            topBar = {
                ProfileTopBar(onNavigateBack = onNavigateBack)
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Секция финансовой аналитики
                AnalyticsSection(
                    totalIncome = state.totalIncome,
                    totalExpense = state.totalExpense,
                    balance = state.balance,
                    savingsRate = state.savingsRate,
                    onNavigateToChart = onNavigateToChart,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.profile_section_padding))
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)))

                // Секция настроек
                SettingsSection(
                    onThemeClick = { viewModel.onEvent(ProfileEvent.ShowThemeDialog) },
                    onLanguageClick = { /* Открыть диалог выбора языка */ },
                    onCurrencyClick = { /* Открыть диалог выбора валюты */ },
                    onTransactionReminderClick = { viewModel.onEvent(ProfileEvent.ShowNotificationSettingsDialog) },
                    themeMode = state.themeMode,
                    isTransactionReminderEnabled = state.isTransactionReminderEnabled,
                    transactionReminderTime = state.transactionReminderTime,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.profile_section_padding))
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)))

                // Секция информации о приложении
                AppInfoSection(
                    appVersion = appVersion,
                    buildVersion = buildVersion,
                    onLibrariesClick = onNavigateToLibraries,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.profile_section_padding))
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)))

                // Кнопка экспорта данных в CSV
                ExportButton(
                    onClick = {
                        viewModel.onEvent(ProfileEvent.ExportTransactionsToCSV, context)
                    },
                    isExporting = state.isExporting,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(horizontal = dimensionResource(R.dimen.profile_section_padding))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                ExportDescription(
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.profile_section_padding))
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)))

                HorizontalDivider(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.profile_section_padding)))

                // После секции экспорта данных добавляем секцию импорта
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)))

                // Импорт данных
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.profile_section_padding)),
                    elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.profile_section_padding))
                    ) {
                        Text(
                            text = "Импорт транзакций",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                        Text(
                            text = "Импортируйте транзакции из CSV-файлов или банковских выписок. Поддерживаются Сбербанк, Тинькофф, Альфа-Банк, ВТБ, Газпромбанк и Озон Банк.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Button(
                                onClick = onNavigateToImport,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = dimensionResource(R.dimen.spacing_small))
                                )
                                Text(text = "Импортировать транзакции")
                            }
                        }
                    }
                }

                // Диалоги
                ShowDialogs(state, viewModel, context)
            }
        }
    }
}

/**
 * Настройка внешнего вида статус-бара в зависимости от выбранной темы.
 */
@Composable
private fun SetupStatusBarAppearance(themeMode: ThemeMode, context: Context) {
    LaunchedEffect(themeMode) {
        val window = (context as? Activity)?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = themeMode == ThemeMode.LIGHT
        }
    }
}

/**
 * Обработка и отображение сообщений о результате экспорта.
 */
@Composable
private fun HandleExportMessages(
    state: ProfileState,
    snackbarHostState: SnackbarHostState,
    viewModel: ProfileViewModel
) {
    LaunchedEffect(state.exportSuccess) {
        state.exportSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ProfileEvent.ResetExportState)
        }
    }

    LaunchedEffect(state.exportError) {
        state.exportError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ProfileEvent.ResetExportState)
        }
    }
}

/**
 * Отображение диалогов настройки темы и уведомлений.
 */
@Composable
private fun ShowDialogs(
    state: ProfileState,
    viewModel: ProfileViewModel,
    context: Context
) {
    // Диалог выбора темы
    if (state.isEditingTheme) {
        ThemeSelectionDialog(
            selectedTheme = state.themeMode,
            onThemeSelected = { theme ->
                viewModel.onEvent(ProfileEvent.ChangeTheme(theme))
            },
            onDismiss = { viewModel.onEvent(ProfileEvent.HideThemeDialog) }
        )
    }

    // Диалог настроек уведомлений
    if (state.isEditingNotifications) {
        NotificationSettingsDialog(
            isEnabled = state.isTransactionReminderEnabled,
            reminderTime = state.transactionReminderTime,
            onSave = { isEnabled, time ->
                viewModel.onEvent(
                    ProfileEvent.UpdateTransactionReminder(
                        isEnabled,
                        time
                    ), context
                )
            },
            onDismiss = { viewModel.onEvent(ProfileEvent.HideNotificationSettingsDialog) }
        )
    }
} 