package com.davidbugayov.financeanalyzer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Синглтон для управления темой на уровне всего приложения
 */
object AppTheme {
    // StateFlow для хранения текущей темы
    private val _currentTheme = MutableStateFlow(ThemeMode.SYSTEM)
    val currentTheme: StateFlow<ThemeMode> = _currentTheme

    /**
     * Устанавливает тему для всего приложения
     */
    fun setTheme(themeMode: ThemeMode) {
        if (_currentTheme.value != themeMode) {
            _currentTheme.value = themeMode
        }
    }
}

/**
 * CompositionLocal для предоставления текущей темы
 */
val LocalAppTheme = staticCompositionLocalOf<ThemeMode> { ThemeMode.SYSTEM }

/**
 * Провайдер для установки темы в Compose-дереве
 */
@Composable
fun AppThemeProvider(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    // Обновляем глобальную тему
    AppTheme.setTheme(themeMode)

    // Предоставляем тему через CompositionLocal
    CompositionLocalProvider(LocalAppTheme provides themeMode) {
        content()
    }
}
