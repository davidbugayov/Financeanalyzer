package com.davidbugayov.financeanalyzer.feature.profile
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsConstants
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider
import com.davidbugayov.financeanalyzer.analytics.PerformanceMetrics

import com.davidbugayov.financeanalyzer.feature.profile.components.AppInfoSection
import com.davidbugayov.financeanalyzer.feature.profile.components.CurrencySelectionDialog
import com.davidbugayov.financeanalyzer.feature.profile.components.LanguageSelectionDialog
import com.davidbugayov.financeanalyzer.feature.profile.components.NotificationSettingsDialog
import com.davidbugayov.financeanalyzer.feature.profile.components.ProfileTopBar
import com.davidbugayov.financeanalyzer.feature.profile.components.SettingsSection
import com.davidbugayov.financeanalyzer.feature.profile.components.ThemeSelectionDialog
import com.davidbugayov.financeanalyzer.feature.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.feature.profile.model.ProfileState
import com.davidbugayov.financeanalyzer.feature.security.components.PinSetupDialog
import com.davidbugayov.financeanalyzer.feature.security.components.SecuritySettingsSection
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.ui.theme.ThemeMode
import com.davidbugayov.financeanalyzer.utils.MemoryUtils
import com.davidbugayov.financeanalyzer.utils.RuStoreUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
fun ProfileScreen(viewModel: ProfileViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Получаем компоненты аналитики из ViewModel
    val userEventTracker = viewModel.userEventTracker
    // val errorTracker = viewModel.errorTracker
    CrashLoggerProvider.crashLogger

    val packageInfo =
        remember {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }

    val versionName = packageInfo?.versionName
    val appVersion = versionName ?: stringResource(UiR.string.unknown)

    @SuppressLint("NewApi")
    val buildVersion =
        remember {
            packageInfo?.longVersionCode?.toString() ?: "0"
        }

    // Отслеживаем время загрузки экрана
    val screenLoadStartTime = remember { SystemClock.elapsedRealtime() }

    // Отслеживаем открытие экрана
    LaunchedEffect(Unit) {
        // Отмечаем начало загрузки экрана
        PerformanceMetrics.startScreenLoadTiming(PerformanceMetrics.Screens.PROFILE)





        // Запрос оценки через 2 сек, если экран не закрыт (в других флейворах вызов no-op)
        (context as? Activity)?.let { activity ->
            delay(2000)
            try {
                RuStoreUtils.requestReview(activity)
                AnalyticsUtils.logEvent(
                    AnalyticsConstants.Events.USER_RATING,
                    android.os.Bundle().apply {
                        putString(AnalyticsConstants.Params.SOURCE, "rustore")
                        putString("request_location", "profile_screen")
                    },
                )
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при запросе оценки в RuStore")
                CrashLoggerProvider.crashLogger.logException(e)
            }
        }

        // Завершаем отслеживание загрузки экрана
        PerformanceMetrics.endScreenLoadTiming(PerformanceMetrics.Screens.PROFILE)

        // Дополнительно отслеживаем время загрузки экрана
        val loadTime = SystemClock.elapsedRealtime() - screenLoadStartTime
        AnalyticsUtils.logEvent(
            AnalyticsConstants.Events.SCREEN_LOAD,
            android.os.Bundle().apply {
                putString(AnalyticsConstants.Params.SCREEN_NAME, "profile")
                putLong(AnalyticsConstants.Params.DURATION_MS, loadTime)
            },
        )
    }

    // Отслеживаем закрытие экрана
    DisposableEffect(Unit) {
        onDispose {
            

            // Отслеживаем использование памяти
            try {
                (context as? Activity)?.let {
                    MemoryUtils.trackMemoryUsage(it)
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при отслеживании памяти")
                CrashLoggerProvider.crashLogger.logException(e)
            }
        }
    }

    LaunchedEffect(state) {
        Timber.d("[ProfileScreen] Current state updated")
    }

    LaunchedEffect(Unit) {
        viewModel.intentCommands.collectLatest { intent ->
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Timber.e(e, "Failed to start activity for intent: $intent")

                // Отслеживаем ошибку
                CrashLoggerProvider.crashLogger.logException(e)

                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Не удалось выполнить действие: ${e.localizedMessage}",
                    )
                }
            }
        }
    }

    SetupStatusBarAppearance(state.themeMode, context)

    FinanceAnalyzerTheme(themeMode = state.themeMode) {
        HandleExportMessages(state, snackbarHostState, viewModel)

        LaunchedEffect(state.isTransactionReminderEnabled) {
            Timber.d(
                "[UI] ProfileScreen: isTransactionReminderEnabled=${state.isTransactionReminderEnabled}",
            )
        }

        Scaffold(
            topBar = {
                ProfileTopBar(onNavigateBack = {
                    viewModel.onEvent(ProfileEvent.NavigateBack)
                })
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
            ) {
                ProfileActionCard(
                    icon = Icons.Filled.Analytics,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = stringResource(UiR.string.analytics_title),
                    subtitle = stringResource(UiR.string.profile_analytics_subtitle),
                    onClick = {
                        viewModel.onEvent(ProfileEvent.NavigateToFinancialStatistics)
                    },
                    modifier =
                        Modifier.padding(
                            horizontal = dimensionResource(UiR.dimen.profile_section_padding),
                        ),
                )
                ProfileActionCard(
                    icon = Icons.Default.AccountBalanceWallet,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = stringResource(UiR.string.budget),
                    subtitle = stringResource(UiR.string.profile_budget_subtitle),
                    onClick = {
                        viewModel.onEvent(ProfileEvent.NavigateToBudget)
                    },
                    modifier =
                        Modifier.padding(
                            horizontal = dimensionResource(UiR.dimen.profile_section_padding),
                        ),
                )
                ProfileActionCard(
                    icon = Icons.Default.FileUpload,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = stringResource(UiR.string.export_import),
                    subtitle = stringResource(UiR.string.profile_export_import_subtitle),
                    onClick = {
                        viewModel.onEvent(ProfileEvent.NavigateToExportImport)
                    },
                    modifier =
                        Modifier.padding(
                            horizontal = dimensionResource(UiR.dimen.profile_section_padding),
                        ),
                )

                ProfileActionCard(
                    icon = Icons.Default.Star,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = stringResource(UiR.string.achievements),
                    subtitle = stringResource(UiR.string.profile_achievements_subtitle),
                    onClick = {
                        viewModel.onEvent(ProfileEvent.NavigateToAchievements)
                    },
                    modifier =
                        Modifier.padding(
                            horizontal = dimensionResource(UiR.dimen.profile_section_padding),
                        ),
                )

                SettingsSection(
                    onThemeClick = { viewModel.onEvent(ProfileEvent.ShowThemeDialog) },
                    onLanguageClick = { viewModel.onEvent(ProfileEvent.ShowLanguageDialog) },
                    onCurrencyClick = { viewModel.onEvent(ProfileEvent.ShowCurrencyDialog) },
                    onTransactionReminderClick = {
                        viewModel.onEvent(
                            ProfileEvent.ShowNotificationSettingsDialog,
                        )
                    },
                    themeMode = state.themeMode,
                    languageSubtitle = state.selectedLanguage,
                    selectedCurrency = state.selectedCurrency,
                    isTransactionReminderEnabled = state.isTransactionReminderEnabled,
                    transactionReminderTime = state.transactionReminderTime,
                    hasNotificationPermission = state.hasNotificationPermission,
                    modifier =
                        Modifier.padding(
                            horizontal = dimensionResource(UiR.dimen.profile_section_padding),
                        ),
                )

                // Секция безопасности
                SecuritySettingsSection(
                    isAppLockEnabled = state.isAppLockEnabled,
                    isBiometricEnabled = state.isBiometricEnabled,
                    isBiometricAvailable = state.isBiometricAvailable,
                    onAppLockClick = {
                        if (!state.isAppLockEnabled) {
                            // Если блокировка не включена, сначала нужно установить PIN
                            viewModel.onEvent(ProfileEvent.ShowPinSetupDialog)
                        } else {
                            // Если уже включена, выключаем
                            viewModel.onEvent(ProfileEvent.ChangeAppLock(false))
                        }
                    },
                    onBiometricClick = { viewModel.onEvent(ProfileEvent.ChangeBiometric(!state.isBiometricEnabled)) },
                    modifier =
                        Modifier.padding(
                            horizontal = dimensionResource(UiR.dimen.profile_section_padding),
                        ),
                )

                AppInfoSection(
                    appVersion = appVersion,
                    buildVersion = buildVersion,
                    onNavigateToLibraries = { viewModel.onEvent(ProfileEvent.NavigateToLibraries) },
                    modifier =
                        Modifier.padding(
                            horizontal = dimensionResource(UiR.dimen.profile_section_padding),
                        ),
                )

                ShowDialogs(state, viewModel)

                Spacer(
                    modifier =
                        Modifier.height(
                            dimensionResource(UiR.dimen.profile_section_spacing) * 2,
                        ),
                )
            }
        }
    }
}

/**
 * Настройка внешнего вида статус-бара в зависимости от выбранной темы.
 */
@Composable
private fun SetupStatusBarAppearance(
    themeMode: ThemeMode,
    context: Context,
) {
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
    viewModel: ProfileViewModel,
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
) {
    if (state.isEditingTheme) {
        ThemeSelectionDialog(
            selectedTheme = state.themeMode,
            onThemeSelected = { theme ->
                viewModel.onEvent(ProfileEvent.ChangeTheme(theme))
            },
            onDismiss = { viewModel.onEvent(ProfileEvent.HideThemeDialog) },
        )
    }

    if (state.isEditingNotifications) {
        NotificationSettingsDialog(
            onDismiss = { viewModel.onEvent(ProfileEvent.HideNotificationSettingsDialog) },
            viewModel = viewModel,
        )
    }

    if (state.isEditingPinCode) {
        PinSetupDialog(
            onDismiss = { viewModel.onEvent(ProfileEvent.HidePinSetupDialog) },
            onPinSet = { pin -> viewModel.onEvent(ProfileEvent.SetPinCode(pin)) },
        )
    }

    if (state.showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = state.selectedCurrency,
            onCurrencySelected = { currency ->
                viewModel.onEvent(ProfileEvent.ChangeCurrency(currency))
            },
            onDismiss = { viewModel.onEvent(ProfileEvent.HideCurrencyDialog) },
        )
    }

    if (state.showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = state.selectedLanguage,
            onLanguageSelected = { code: String ->
                viewModel.onEvent(ProfileEvent.ChangeLanguage(code))
            },
            onDismiss = { viewModel.onEvent(ProfileEvent.HideLanguageDialog) },
        )
    }
}

@Composable
fun ProfileActionCard(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(iconBackground, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
