package com.davidbugayov.financeanalyzer.presentation

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.davidbugayov.financeanalyzer.presentation.components.PermissionDialogs
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import com.davidbugayov.financeanalyzer.presentation.navigation.AppNavGraph
import com.davidbugayov.financeanalyzer.presentation.onboarding.OnboardingScreen
import com.davidbugayov.financeanalyzer.presentation.onboarding.OnboardingViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.PermissionManager
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
    val navController = rememberNavController()
    val themeMode by profileViewModel.themeMode.collectAsState()
    val view = LocalView.current
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    var shouldShowOnboarding by remember { mutableStateOf(!onboardingViewModel.isOnboardingCompleted()) }
    var onboardingJustCompleted by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var wasPermissionDialogDismissed by remember { mutableStateOf(false) }

    LaunchedEffect(onboardingJustCompleted) {
        if (onboardingJustCompleted && !wasPermissionDialogDismissed && !permissionManager.wasPermissionDialogShown()) {
            showPermissionDialog = true
        }
    }

    PermissionDialogs(
        show = showPermissionDialog,
        onDismiss = {
            showPermissionDialog = false
            wasPermissionDialogDismissed = true
            permissionManager.markPermissionDialogShown()
        },
        onPermissionGranted = {
            showPermissionDialog = false
            permissionManager.markPermissionDialogShown()
        },
        onPermissionDenied = {
            showPermissionDialog = false
            wasPermissionDialogDismissed = true
            permissionManager.markPermissionDialogShown()
        },
        openSettingsOnDeny = false
    )

    ApplyThemeAndSystemBars(themeMode, isDarkTheme, view)
    BackgroundInit(homeViewModel)

    FinanceAnalyzerTheme(themeMode = themeMode) {
        if (shouldShowOnboarding) {
            OnboardingScreen(onFinish = {
                onboardingViewModel.completeOnboarding()
                shouldShowOnboarding = false
                onboardingJustCompleted = true
                Timber.d("Онбординг только что завершен, проверяем разрешения")
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