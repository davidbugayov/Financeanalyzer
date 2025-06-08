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
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.davidbugayov.financeanalyzer.presentation.MainScreen
import com.davidbugayov.financeanalyzer.presentation.navigation.Screen
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.PreferencesManager

class FinanceActivity : ComponentActivity() {
    
    // Начальный экран для навигации
    private var startDestination = Screen.Home.route
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Обрабатываем deep links
        handleIntent(intent)
        
        // Делаем контент приложения отображаться под системными панелями
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Определяем, какую тему использует приложение
        val isDarkTheme = when (PreferencesManager(this).getThemeMode()) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> resources.configuration.uiMode and 
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

        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            // Логирование и оповещение об ошибке
            android.util.Log.e("FinanceActivity", "Unhandled exception", e)
            
            // Перезапускаем приложение
            val intent = Intent(this, FinanceActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            
            // Завершаем текущий процесс
            Process.killProcess(Process.myPid())
            System.exit(10)
        }
        
        applyContent()
    }

    /**
     * Обрабатывает входящий intent и определяет начальный экран
     */
    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            when (uri.toString()) {
                "financeanalyzer://add" -> {
                    startDestination = "add"
                }
            }
        }
    }

    private fun applyContent() {
        setContent {
            FinanceAppContent()
        }
    }
}

@Composable
fun FinanceAppContent() {
    FinanceAnalyzerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen()
        }
    }
}