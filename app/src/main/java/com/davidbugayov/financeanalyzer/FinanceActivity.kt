package com.davidbugayov.financeanalyzer

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.davidbugayov.financeanalyzer.navigation.AppNavHostImpl
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.navigation.Screen
import com.davidbugayov.financeanalyzer.ui.theme.AppTheme
import com.davidbugayov.financeanalyzer.ui.theme.AppThemeProvider
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.ui.theme.ThemeMode
import com.davidbugayov.financeanalyzer.utils.OnboardingManager
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import org.koin.android.ext.android.inject

class FinanceActivity : FragmentActivity(), DefaultLifecycleObserver {
    private val navigationManager: NavigationManager by inject()
    private val onboardingManager: OnboardingManager by inject()
    private val preferencesManager: PreferencesManager by inject()

    // Начальный экран для навигации
    private var startDestination = Screen.Home.route

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super<FragmentActivity>.onCreate(savedInstanceState)

        // Обрабатываем deep links
        handleIntent(intent)

        // Определяем начальный экран
        startDestination =
            when {
                !onboardingManager.isOnboardingCompleted() -> Screen.Onboarding.route
                preferencesManager.isAppLockEnabled() -> Screen.Auth.route
                else -> Screen.Home.route
            }

        // Делаем контент приложения отображаться под системными панелями
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Определяем, какую тему использует приложение и инициализируем AppTheme
        val themeMode = PreferencesManager(this).getThemeMode()
        AppTheme.setTheme(themeMode)

        val isDarkTheme =
            when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }

        // Устанавливаем цвет иконок в зависимости от темы:
        // - светлые иконки на темном фоне (isDarkTheme = true)
        // - темные иконки на светлом фоне (isDarkTheme = false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isDarkTheme
            isAppearanceLightNavigationBars = !isDarkTheme
        }

        // Настраиваем поведение сплеш-скрина
        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        enableEdgeToEdge()

        // Добавляем наблюдатель за жизненным циклом приложения для обработки app lock
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        applyContent()

        // Устанавливаем флаг готовности, чтобы скрыть сплеш-скрин
        isReady = true
    }

    /**
     * Обрабатывает входящий intent и определяет начальный экран
     */
    private fun handleIntent(intent: Intent?) {
        // Обрабатываем deep links
        intent?.data?.let { uri ->
            when (uri.toString()) {
                "financeanalyzer://add" -> {
                    startDestination = Screen.AddTransaction.route
                }
            }
        }

        // Обрабатываем шорткаты (extras)
        intent?.getStringExtra("screen")?.let { screen ->
            when (screen) {
                "add" -> {
                    startDestination = Screen.AddTransaction.route
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Обрабатываем новый intent когда приложение уже запущено
        intent.getStringExtra("screen")?.let { screen ->
            when (screen) {
                "add" -> {
                    navigationManager.navigate(NavigationManager.Command.Navigate(Screen.AddTransaction.route))
                }
            }
        }
    }

    private fun applyContent() {
        setContent {
            FinanceAppContent(navigationManager, startDestination)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onResume(owner)
        // Проверяем, нужна ли аутентификация при возврате в приложение
        if (preferencesManager.isAppLockEnabled()) {
            navigationManager.navigate(
                NavigationManager.Command.NavigateAndClearBackStack(
                    destination = Screen.Auth.route,
                    popUpTo = Screen.Home.route,
                ),
            )
        }
    }

    override fun onDestroy() {
        super<FragmentActivity>.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }
}

@Composable
fun FinanceAppContent(
    navigationManager: NavigationManager,
    startDestination: String,
) {
    // Получаем текущую тему из AppTheme и наблюдаем за изменениями
    val themeMode by AppTheme.currentTheme.collectAsState()

    // Используем AppThemeProvider для предоставления темы всему приложению
    AppThemeProvider(themeMode = themeMode) {
        FinanceAnalyzerTheme(themeMode = themeMode) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                val navController = rememberNavController()
                AppNavHostImpl(
                    navController = navController,
                    navigationManager = navigationManager,
                    startDestination = startDestination,
                )
            }
        }
    }
}
