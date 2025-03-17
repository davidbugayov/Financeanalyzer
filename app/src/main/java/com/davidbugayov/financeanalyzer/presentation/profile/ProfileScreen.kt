package com.davidbugayov.financeanalyzer.presentation.profile

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.BuildConfig
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.profile.components.AnalyticsSection
import com.davidbugayov.financeanalyzer.presentation.profile.components.AppInfoSection
import com.davidbugayov.financeanalyzer.presentation.profile.components.FinancialGoalsList
import com.davidbugayov.financeanalyzer.presentation.profile.components.NotificationSettingsDialog
import com.davidbugayov.financeanalyzer.presentation.profile.components.SettingsSection
import com.davidbugayov.financeanalyzer.presentation.profile.components.ThemeSelectionDialog
import com.davidbugayov.financeanalyzer.presentation.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.PermissionUtils
import org.koin.androidx.compose.koinViewModel

/**
 * Экран профиля пользователя.
 * Отображает информацию о пользователе и настройки приложения.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLibraries: () -> Unit,
    onNavigateToChart: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
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
    
    // Вкладки для финансовых целей
    val tabs = listOf(
        stringResource(R.string.active_goals),
        stringResource(R.string.completed_goals)
    )

    // Логируем открытие экрана профиля
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "profile",
            screenClass = "ProfileScreen"
        )
    }

    // Показываем сообщение об успешном экспорте
    LaunchedEffect(state.exportSuccess) {
        state.exportSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ProfileEvent.ResetExportState)
        }
    }

    // Показываем сообщение об ошибке экспорта
    LaunchedEffect(state.exportError) {
        state.exportError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ProfileEvent.ResetExportState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
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
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Секция настроек
            SettingsSection(
                onThemeClick = { viewModel.onEvent(ProfileEvent.ShowThemeDialog) },
                onLanguageClick = { /* Открыть диалог выбора языка */ },
                onCurrencyClick = { /* Открыть диалог выбора валюты */ },
                onNotificationsClick = { /* Открыть настройки уведомлений */ },
                onTransactionReminderClick = { viewModel.onEvent(ProfileEvent.ShowNotificationSettingsDialog) },
                onSecurityClick = { /* Открыть настройки безопасности */ },
                onAdvancedSettingsClick = { /* Открыть расширенные настройки */ },
                themeMode = state.themeMode,
                isTransactionReminderEnabled = state.isTransactionReminderEnabled,
                transactionReminderTime = state.transactionReminderTime,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Секция информации о приложении
            AppInfoSection(
                appVersion = appVersion,
                buildVersion = buildVersion,
                onLibrariesClick = onNavigateToLibraries,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка экспорта данных в CSV
            Button(
                onClick = { 
                    // Проверяем разрешения перед экспортом
                    if (PermissionUtils.hasStoragePermission(context)) {
                        viewModel.onEvent(ProfileEvent.ExportTransactionsToCSV, context)
                    } else {
                        // Запрашиваем разрешение
                        // Обработка будет в RequestStoragePermission
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterHorizontally),
                enabled = !state.isExporting
            ) {
                if (state.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(text = stringResource(R.string.export_to_csv))
            }
            
            // Запрашиваем разрешение на запись во внешнее хранилище
            PermissionUtils.RequestStoragePermission(
                onPermissionGranted = {
                    // Разрешение получено, можно экспортировать
                },
                onPermissionDenied = {
                    // Вместо прямого вызова showSnackbar, устанавливаем состояние
                    // и показываем сообщение через LaunchedEffect выше
                    viewModel.onEvent(ProfileEvent.SetExportError("Для экспорта данных необходимо разрешение на запись файлов"))
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.export_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Секция финансовых целей
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.financial_goals),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(text = title) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when (selectedTabIndex) {
                    0 -> FinancialGoalsList(
                        goals = state.activeGoals,
                        onGoalClick = { goal -> viewModel.onEvent(ProfileEvent.SelectGoal(goal.id)) },
                        onAddGoalClick = { viewModel.onEvent(ProfileEvent.ShowAddGoalDialog) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    1 -> FinancialGoalsList(
                        goals = state.completedGoals,
                        onGoalClick = { goal -> viewModel.onEvent(ProfileEvent.SelectGoal(goal.id)) },
                        onAddGoalClick = { viewModel.onEvent(ProfileEvent.ShowAddGoalDialog) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    
    // Диалог выбора темы
    if (state.isEditingTheme) {
        ThemeSelectionDialog(
            selectedTheme = state.themeMode,
            onThemeSelected = { theme -> viewModel.onEvent(ProfileEvent.ChangeTheme(theme)) },
            onDismiss = { viewModel.onEvent(ProfileEvent.HideThemeDialog) }
        )
    }
    
    // Диалог настроек уведомлений
    if (state.isEditingNotifications) {
        NotificationSettingsDialog(
            isEnabled = state.isTransactionReminderEnabled,
            reminderTime = state.transactionReminderTime,
            onSave = { isEnabled, time -> viewModel.onEvent(ProfileEvent.UpdateTransactionReminder(isEnabled, time), context) },
            onDismiss = { viewModel.onEvent(ProfileEvent.HideNotificationSettingsDialog) }
        )
    }
} 