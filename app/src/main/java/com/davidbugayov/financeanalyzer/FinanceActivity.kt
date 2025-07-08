package com.davidbugayov.financeanalyzer

import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
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
import timber.log.Timber

class FinanceActivity : ComponentActivity() {
    private val navigationManager: NavigationManager by inject()
    private val onboardingManager: OnboardingManager by inject()

    // Начальный экран для навигации
    private var startDestination = Screen.Home.route

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Обрабатываем deep links
        handleIntent(intent)

        // Проверяем, нужно ли показывать онбординг
        if (!onboardingManager.isOnboardingCompleted()) {
            startDestination = Screen.Onboarding.route
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

        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            // Логирование и оповещение об ошибке
            Timber.tag("FinanceActivity").e(e, "Unhandled exception")

            // Перезапускаем приложение
            val intent = Intent(this, FinanceActivity::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK,
            )
            startActivity(intent)

            // Завершаем текущий процесс
            Process.killProcess(Process.myPid())
            System.exit(10)
        }

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
