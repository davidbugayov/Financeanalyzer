package com.davidbugayov.financeanalyzer.presentation

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import com.davidbugayov.financeanalyzer.presentation.navigation.NavGraph
import com.davidbugayov.financeanalyzer.presentation.navigation.Screen
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Главный экран приложения, содержащий навигацию между различными разделами.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Настраиваем основную тему приложения
    Box(modifier = Modifier.fillMaxSize()) {
        // Отображаем навигационный граф
        NavGraph(
            navController = navController,
            startDestination = Screen.Home.route
        )
    }
}

@Composable
private fun ApplyThemeAndSystemBars(
    themeMode: ThemeMode,
    isDarkTheme: Boolean,
    view: android.view.View
) {
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
        MainScope().launch(Dispatchers.Default) {
            delay(500)
            homeViewModel.initiateBackgroundDataRefresh()
            delay(2000)
        }
    }
}
