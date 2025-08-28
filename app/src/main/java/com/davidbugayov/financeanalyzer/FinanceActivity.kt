package com.davidbugayov.financeanalyzer

import android.annotation.SuppressLint
import android.content.Context
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
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.navigation.Screen
import com.davidbugayov.financeanalyzer.navigation.appNavHostImpl
import com.davidbugayov.financeanalyzer.shared.achievements.AchievementTrigger
import com.davidbugayov.financeanalyzer.ui.components.AchievementEngineProvider
import com.davidbugayov.financeanalyzer.ui.components.achievementNotificationManager
import com.davidbugayov.financeanalyzer.ui.theme.AppTheme
import com.davidbugayov.financeanalyzer.ui.theme.AppThemeProvider
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.ui.theme.ThemeMode
import com.davidbugayov.financeanalyzer.utils.AppLocale
import com.davidbugayov.financeanalyzer.utils.CurrencyProvider
import com.davidbugayov.financeanalyzer.utils.OnboardingManager
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import com.davidbugayov.financeanalyzer.widget.AndroidWidgetRefresher
import org.koin.android.ext.android.inject
import timber.log.Timber

class FinanceActivity :
    FragmentActivity(),
    DefaultLifecycleObserver {
    private val navigationManager: NavigationManager by inject()
    private val onboardingManager: OnboardingManager by inject()
    private val preferencesManager: PreferencesManager by inject()

    // Начальный экран для навигации
    private var startDestination = Screen.Home.route

    // Флаг для отслеживания первого запуска приложения в текущей сессии
    private var isFirstLaunchInSession = true

    override fun attachBaseContext(newBase: Context) {
        // Оборачиваем контекст до super.onCreate
        val wrapped =
            com.davidbugayov.financeanalyzer.utils.LocaleUtils
                .wrapContext(newBase)
        super.attachBaseContext(wrapped)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super<FragmentActivity>.onCreate(savedInstanceState)

        // Обрабатываем deep links
        handleIntent(intent)

        // Определяем начальный экран
        startDestination =
            when {
                !onboardingManager.isOnboardingCompleted() -> Screen.Onboarding.route
                preferencesManager.isAppLockEnabled() && isFirstLaunchInSession -> Screen.Auth.route
                else -> Screen.Home.route
            }

        // Применяем сохранённый язык приложения на старте, независимо от системного языка
        val langCode = preferencesManager.getAppLanguage()
        timber.log.Timber
            .tag("LANG")
            .d("FinanceActivity.onCreate: applying lang=%s", langCode)
        AppLocale.apply(langCode)

        // Делаем контент приложения отображаться под системными панелями
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Определяем, какую тему использует приложение и инициализируем AppTheme
        val themeMode = PreferencesManager(this).getThemeMode()
        AppTheme.setTheme(themeMode)

        // Инициализируем CurrencyProvider
        CurrencyProvider.init(preferencesManager)

        val isDarkTheme =
            when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM ->
                    resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                        android.content.res.Configuration.UI_MODE_NIGHT_YES
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
                    // Триггер ачивки перехода из шортката
                    AchievementTrigger.onMilestoneReached("shortcut_add_transaction")
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
                    // Триггер ачивки при повторном открытии через шорткат
                    AchievementTrigger.onMilestoneReached("shortcut_add_transaction")
                }
            }
        }
    }

    private fun applyContent() {
        setContent {
            achievementNotificationManager(
                achievementEngine = AchievementEngineProvider.get(),
            ) {
                financeAppContent(navigationManager, startDestination, ::setFirstLaunchCompleted)
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onResume(owner)
        // Экран блокировки показывается только при запуске приложения,
        // а не при сворачивании/разворачивании

        // Обновляем виджеты при возвращении в приложение
        updateWidgets()
    }

    /**
     * Обновляет все активные виджеты
     */
    private fun updateWidgets() {
        try {
            val widgetRefresher = AndroidWidgetRefresher(this)
            widgetRefresher.refresh()
            Timber.d("Виджеты обновлены при запуске приложения")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при обновлении виджетов")
        }
    }

    override fun onDestroy() {
        super<FragmentActivity>.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        // Сбрасываем флаг первого запуска при завершении приложения
        isFirstLaunchInSession = true
    }

    /**
     * Устанавливает флаг первого запуска в текущей сессии
     */
    fun setFirstLaunchCompleted() {
        isFirstLaunchInSession = false
    }
}

@Composable
@SuppressLint("ComposableNaming")
fun financeAppContent(
    navigationManager: NavigationManager,
    startDestination: String,
    onFirstLaunchCompleted: () -> Unit,
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
                appNavHostImpl(
                    navController = navController,
                    navigationManager = navigationManager,
                    startDestination = startDestination,
                    onFirstLaunchCompleted = onFirstLaunchCompleted,
                )
            }
        }
    }
}
