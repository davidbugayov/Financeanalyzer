package com.davidbugayov.financeanalyzer.presentation.profile

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import com.davidbugayov.financeanalyzer.BuildConfig
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.navigation.Screen
import com.davidbugayov.financeanalyzer.presentation.profile.components.AnalyticsSection
import com.davidbugayov.financeanalyzer.presentation.profile.components.AppInfoSection
import com.davidbugayov.financeanalyzer.presentation.profile.components.NotificationSettingsDialog
import com.davidbugayov.financeanalyzer.presentation.profile.components.ProfileTopBar
import com.davidbugayov.financeanalyzer.presentation.profile.components.SettingsSection
import com.davidbugayov.financeanalyzer.presentation.profile.components.ThemeSelectionDialog
import com.davidbugayov.financeanalyzer.presentation.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.presentation.profile.model.ProfileState
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.ui.theme.LocalFriendlyCardBackgroundColor
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

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
    onNavigateToBudget: () -> Unit,
    onNavigateToExportImport: (String) -> Unit
) {
    // Получаем текущее состояние из ViewModel
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Получаем информацию о версии приложения
    val packageInfo = remember {
        context.packageManager.getPackageInfo(context.packageName, 0)
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

        LaunchedEffect(state.isTransactionReminderEnabled) {
            Timber.d("[UI] ProfileScreen: isTransactionReminderEnabled=${state.isTransactionReminderEnabled}")
        }

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
                    totalTransactions = state.totalTransactions,
                    totalExpenseCategories = state.totalExpenseCategories,
                    totalIncomeCategories = state.totalIncomeCategories,
                    averageExpense = state.averageExpense,
                    totalSourcesUsed = state.totalSourcesUsed,
                    dateRange = state.dateRange,
                    onSavingsRateClick = onNavigateToChart,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.profile_section_padding))
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)))
                // Секция бюджета
                BudgetSection(onNavigateToBudget, modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.profile_section_padding)))
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)))
                // Секция экспорт и импорт
                ExportImportSection(
                    onNavigateToExportImport,
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
                    onNavigateToLibraries = onNavigateToLibraries,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.profile_section_padding))
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)))

                // Диалоги
                ShowDialogs(state, viewModel)
                
                // Отступ внизу экрана для улучшения UX
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing) * 2))
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
    viewModel: ProfileViewModel
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
            onDismiss = { viewModel.onEvent(ProfileEvent.HideNotificationSettingsDialog) },
            viewModel = viewModel
        )
    }
}

/**
 * Секция бюджета на экране профиля.
 * @param onNavigateToBudget Обработчик перехода к бюджету.
 * @param modifier Модификатор для внешнего вида.
 */
@Composable
private fun BudgetSection(
    onNavigateToBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigateToBudget() },
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        colors = CardDefaults.cardColors(containerColor = LocalFriendlyCardBackgroundColor.current)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.profile_section_padding)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = stringResource(R.string.budget),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            Text(
                text = stringResource(R.string.budget),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Секция экспорта и импорта на экране профиля.
 * @param onNavigateToExportImport Обработчик перехода к экспорту/импорту.
 * @param modifier Модификатор для внешнего вида.
 */
@Composable
private fun ExportImportSection(
    onNavigateToExportImport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        colors = CardDefaults.cardColors(containerColor = LocalFriendlyCardBackgroundColor.current)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.profile_section_padding))
                .clickable { onNavigateToExportImport(Screen.ExportImport.route) },
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FileUpload,
                contentDescription = stringResource(R.string.export_import),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            Text(
                text = stringResource(R.string.export_import),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
