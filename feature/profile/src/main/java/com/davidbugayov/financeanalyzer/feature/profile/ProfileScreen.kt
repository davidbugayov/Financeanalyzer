package com.davidbugayov.financeanalyzer.feature.profile
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.profile.util.StringProvider
import androidx.core.view.WindowCompat
import com.davidbugayov.financeanalyzer.analytics.AnalyticsConstants
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.PerformanceMetrics
import com.davidbugayov.financeanalyzer.feature.profile.components.AnalyticsSection
import com.davidbugayov.financeanalyzer.feature.profile.components.AppInfoSection
import com.davidbugayov.financeanalyzer.feature.profile.components.NotificationSettingsDialog
import com.davidbugayov.financeanalyzer.feature.profile.components.ProfileTopBar
import com.davidbugayov.financeanalyzer.feature.profile.components.SettingsSection
import com.davidbugayov.financeanalyzer.feature.profile.components.ThemeSelectionDialog
import com.davidbugayov.financeanalyzer.feature.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.feature.profile.model.ProfileState
import com.davidbugayov.financeanalyzer.feature.security.components.PinSetupDialog
import com.davidbugayov.financeanalyzer.feature.security.components.SecuritySettingsSection
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.ui.theme.ThemeMode
import com.davidbugayov.financeanalyzer.utils.MemoryUtils
import com.davidbugayov.financeanalyzer.utils.RuStoreUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider

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
    val crashLogger = CrashLoggerProvider.crashLogger

    val packageInfo =
        remember {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }

    val appVersion = remember { packageInfo?.versionName ?: StringProvider.unknown }

    @SuppressLint("NewApi")
    val buildVersion = remember { 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            (packageInfo?.longVersionCode ?: 0L).toString()
        } else {
            (packageInfo?.versionCode ?: 0).toString()
        }
    }

    // Отслеживаем время загрузки экрана
    val screenLoadStartTime = remember { SystemClock.elapsedRealtime() }

    // Отслеживаем открытие экрана
    LaunchedEffect(Unit) {
        // Отмечаем начало загрузки экрана
        PerformanceMetrics.startScreenLoadTiming(PerformanceMetrics.Screens.PROFILE)

        // Логируем просмотр экрана
        AnalyticsUtils.logScreenView(
            screenName = "profile",
            screenClass = "ProfileScreen",
        )

        // Отслеживаем открытие экрана для аналитики пользовательских событий
        userEventTracker.trackScreenOpen(PerformanceMetrics.Screens.PROFILE)

        // Логируем использование функции профиля
        userEventTracker.trackFeatureUsage("profile_view")

        // Отправляем данные о состоянии пользователя в аналитику
        AnalyticsUtils.setUserProperty("has_transactions", (state.totalTransactions > 0).toString())
        AnalyticsUtils.setUserProperty(
            "has_categories",
            (state.totalExpenseCategories > 0 || state.totalIncomeCategories > 0).toString(),
        )
        AnalyticsUtils.setUserProperty("savings_rate", state.savingsRate.toString())

        // Запрашиваем оценку приложения в RuStore, если это rustore flavor
        try {
            (context as? Activity)?.let { activity ->
                RuStoreUtils.requestReview(activity)

                // Логируем запрос на оценку
                AnalyticsUtils.logEvent(
                    AnalyticsConstants.Events.USER_RATING,
                    android.os.Bundle().apply {
                        putString(AnalyticsConstants.Params.SOURCE, "rustore")
                        putString("request_location", "profile_screen")
                    },
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при запросе оценки в RuStore")
            CrashLoggerProvider.crashLogger.logException(e)
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
            // Отслеживаем закрытие экрана
            userEventTracker.trackScreenClose(PerformanceMetrics.Screens.PROFILE)

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
        Timber.d(
            "[ProfileScreen] Current state: income=${state.totalIncome.amount}, expense=${state.totalExpense.amount}, balance=${state.balance.amount}",
        )
        Timber.d(
            "[ProfileScreen] More state: transactions=${state.totalTransactions}, expenseCategories=${state.totalExpenseCategories}, incomeCategories=${state.totalIncomeCategories}",
        )
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
                    // Логируем действие пользователя
                    userEventTracker.trackUserAction(
                        PerformanceMetrics.Actions.NAVIGATION,
                        mapOf(
                            "from" to "profile_screen",
                            "action" to "back",
                        ),
                    )

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
                    onSavingsRateClick = {
                        // Логируем действие пользователя
                        userEventTracker.trackUserAction(
                            PerformanceMetrics.Actions.BUTTON_CLICK,
                            mapOf(
                                "section" to "analytics",
                                "target" to "financial_statistics",
                            ),
                        )

                        viewModel.onEvent(ProfileEvent.NavigateToFinancialStatistics)
                    },
                    modifier =
                        Modifier
                            .padding(horizontal = dimensionResource(R.dimen.profile_section_padding)),
                    onSectionClick = {
                        // Логируем действие пользователя
                        userEventTracker.trackUserAction(
                            PerformanceMetrics.Actions.BUTTON_CLICK,
                            mapOf(
                                "section" to "analytics",
                                "target" to "financial_statistics",
                            ),
                        )

                        viewModel.onEvent(ProfileEvent.NavigateToFinancialStatistics)
                    }
                )

                Spacer(
                    modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)),
                )
                ProfileActionCard(
                    icon = Icons.Default.AccountBalanceWallet,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = StringProvider.budget,
                    subtitle = StringProvider.profileBudgetSubtitle,
                    onClick = {
                        // Логируем действие пользователя
                        userEventTracker.trackUserAction(
                            PerformanceMetrics.Actions.BUTTON_CLICK,
                            mapOf(
                                "section" to "profile",
                                "target" to "budget",
                            ),
                        )

                        viewModel.onEvent(ProfileEvent.NavigateToBudget)
                    },
                    modifier =
                        Modifier.padding(
                            horizontal = dimensionResource(R.dimen.profile_section_padding),
                            vertical = 4.dp,
                        ),
                )
                Spacer(
                    modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)),
                )
                ProfileActionCard(
                    icon = Icons.Default.FileUpload,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = StringProvider.exportImport,
                    subtitle = StringProvider.profileExportImportSubtitle,
                    onClick = {
                        // Логируем действие пользователя
                        userEventTracker.trackUserAction(
                            PerformanceMetrics.Actions.BUTTON_CLICK,
                            mapOf(
                                "section" to "profile",
                                "target" to "export_import",
                            ),
                        )

                        viewModel.onEvent(ProfileEvent.NavigateToExportImport)
                    },
                    modifier =
                        Modifier.padding(
                            horizontal = dimensionResource(R.dimen.profile_section_padding),
                            vertical = 4.dp,
                        ),
                )
                Spacer(
                    modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)),
                )

                ProfileActionCard(
                    icon = Icons.Default.Star,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = StringProvider.achievements,
                    subtitle = StringProvider.profileAchievementsSubtitle,
                    onClick = {
                        // Логируем действие пользователя
                        userEventTracker.trackUserAction(
                            PerformanceMetrics.Actions.BUTTON_CLICK,
                            mapOf(
                                "section" to "profile",
                                "target" to "achievements",
                            ),
                        )

                        viewModel.onEvent(ProfileEvent.NavigateToAchievements)
                    },
                    modifier =
                        Modifier.padding(
                            horizontal = dimensionResource(R.dimen.profile_section_padding),
                            vertical = 4.dp,
                        ),
                )
                Spacer(
                    modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)),
                )

                SettingsSection(
                    onThemeClick = { viewModel.onEvent(ProfileEvent.ShowThemeDialog) },
                    onLanguageClick = { /* Открыть диалог выбора языка */ },
                    onCurrencyClick = { /* Открыть диалог выбора валюты */ },
                    onTransactionReminderClick = {
                        viewModel.onEvent(
                            ProfileEvent.ShowNotificationSettingsDialog,
                        )
                    },
                    themeMode = state.themeMode,
                    isTransactionReminderEnabled = state.isTransactionReminderEnabled,
                    transactionReminderTime = state.transactionReminderTime,
                    hasNotificationPermission = state.hasNotificationPermission,
                    modifier =
                        Modifier.padding(
                            horizontal = dimensionResource(R.dimen.profile_section_padding),
                        ),
                )

                Spacer(
                    modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)),
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
                            horizontal = dimensionResource(R.dimen.profile_section_padding),
                        ),
                )

                Spacer(
                    modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)),
                )

                AppInfoSection(
                    appVersion = appVersion,
                    buildVersion = buildVersion,
                    onNavigateToLibraries = { viewModel.onEvent(ProfileEvent.NavigateToLibraries) },
                    modifier =
                        Modifier.padding(
                            horizontal = dimensionResource(R.dimen.profile_section_padding),
                        ),
                )

                Spacer(
                    modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)),
                )

                ShowDialogs(state, viewModel)

                Spacer(
                    modifier =
                        Modifier.height(
                            dimensionResource(R.dimen.profile_section_spacing) * 2,
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
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(iconBackground, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
