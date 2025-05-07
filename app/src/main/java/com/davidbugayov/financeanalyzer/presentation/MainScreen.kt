package com.davidbugayov.financeanalyzer.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import com.davidbugayov.financeanalyzer.presentation.navigation.AppNavGraph
import com.davidbugayov.financeanalyzer.presentation.onboarding.OnboardingScreen
import com.davidbugayov.financeanalyzer.presentation.onboarding.OnboardingViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import com.davidbugayov.financeanalyzer.utils.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * Главный экран приложения, содержащий навигацию между различными разделами.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    startDestination: String = "home",
    homeViewModel: HomeViewModel = koinViewModel(),
    editTransactionViewModel: EditTransactionViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel(),
    onboardingViewModel: OnboardingViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val themeMode by profileViewModel.themeMode.collectAsState()
    val view = LocalView.current
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    var shouldShowOnboarding by remember { mutableStateOf(!onboardingViewModel.isOnboardingCompleted()) }

    PermissionDialogs(
        onboardingViewModel = onboardingViewModel,
        onPermissionGranted = { /* setupNotifications() */ },
        onPermissionDenied = { /* showSettingsDialog = true */ },
        context = context
    )

    ApplyThemeAndSystemBars(themeMode, isDarkTheme, view)
    BackgroundInit(homeViewModel)

    FinanceAnalyzerTheme(themeMode = themeMode) {
        if (shouldShowOnboarding) {
            OnboardingScreen(onFinish = {
                onboardingViewModel.completeOnboarding()
                shouldShowOnboarding = false
                // PermissionDialogs будет сам обрабатывать разрешения
            })
        } else {
            Scaffold(modifier = Modifier.fillMaxSize()) {
                AppNavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    homeViewModel = homeViewModel,
                    editTransactionViewModel = editTransactionViewModel,
                    profileViewModel = profileViewModel
                )
            }
        }
    }
}

private fun setupNotifications(context: android.content.Context) {
    try {
        val notificationScheduler = NotificationScheduler()
        notificationScheduler.scheduleTransactionReminder(
            context,
            20,
            0
        ) // По умолчанию в 20:00
        Timber.d("Transaction reminders scheduled after permission granted")
    } catch (e: Exception) {
        Timber.e(e, "Failed to schedule transaction reminders")
    }
}
    
@Composable
private fun PermissionDialogs(
    onboardingViewModel: OnboardingViewModel,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    context: android.content.Context
) {
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val permissionLauncher = PermissionUtils.rememberNotificationPermissionLauncher { isGranted ->
        if (isGranted) {
            setupNotifications(context)
            onPermissionGranted()
            Timber.d("Notification permission granted after onboarding")
        } else {
            showSettingsDialog = true
            onPermissionDenied()
            Timber.d("Notification permission denied after onboarding")
        }
    }

    // Показываем диалог после онбординга, если нужно
    LaunchedEffect(onboardingViewModel.isOnboardingCompleted()) {
        if (onboardingViewModel.isOnboardingCompleted() &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !PermissionUtils.hasNotificationPermission(context)
        ) {
            showPermissionDialog = true
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(stringResource(R.string.permission_denied_title)) },
            text = { Text(stringResource(R.string.permission_denied_text)) },
            confirmButton = {
                Button(onClick = {
                    PermissionUtils.openNotificationSettings(context)
                    showSettingsDialog = false
                }) {
                    Text(stringResource(R.string.open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.permission_required_title)) },
            text = { Text(stringResource(R.string.permission_required_text)) },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        setupNotifications(context)
                        onPermissionGranted()
                    }
                }) {
                    Text(stringResource(R.string.request_permission))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ApplyThemeAndSystemBars(themeMode: ThemeMode, isDarkTheme: Boolean, view: android.view.View) {
    LaunchedEffect(themeMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = !isDarkTheme
            controller.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }
}

@Composable
private fun BackgroundInit(homeViewModel: HomeViewModel) {
    LaunchedEffect(Unit) {
        Timber.d("MainScreen: Планируем фоновую инициализацию данных")
        MainScope().launch(Dispatchers.Default) {
            delay(500)
            Timber.d("MainScreen: Запускаем initiateBackgroundDataRefresh")
            homeViewModel.initiateBackgroundDataRefresh()
            delay(2000)
            Timber.d("MainScreen: Запускаем загрузку данных для графиков")
        }
    }
}