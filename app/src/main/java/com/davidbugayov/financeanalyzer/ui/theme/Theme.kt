package com.davidbugayov.financeanalyzer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode

// Цвета для светлой темы
private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline
)

// Цвета для темной темы
private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline
)

// Цвета для доходов и расходов, и цвета баланса
val LocalIncomeColor = staticCompositionLocalOf { md_theme_light_income }
val LocalExpenseColor = staticCompositionLocalOf { md_theme_light_expense }
val LocalBalanceCardColor = staticCompositionLocalOf { md_theme_light_primaryContainer }
val LocalBalanceTextColor = staticCompositionLocalOf { md_theme_light_balance_text }
val LocalFabColor = staticCompositionLocalOf { md_theme_light_fab }
val LocalWarningColor = staticCompositionLocalOf { WarningColor }
val LocalInfoColor = staticCompositionLocalOf { InfoColor }

@Composable
fun FinanceAnalyzerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Выключаем динамические цвета по умолчанию
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    
    // Определяем цвета для доходов и расходов в зависимости от темы
    val incomeColor = if (darkTheme) md_theme_dark_income else md_theme_light_income
    val expenseColor = if (darkTheme) md_theme_dark_expense else md_theme_light_expense
    
    // Цвета для баланса
    val balanceCardColor = if (darkTheme) md_theme_dark_primaryContainer else md_theme_light_primaryContainer
    val balanceTextColor = if (darkTheme) md_theme_dark_balance_text else md_theme_light_balance_text
    
    // Цвет кнопки добавления
    val fabColor = if (darkTheme) md_theme_dark_fab else md_theme_light_fab

    CompositionLocalProvider(
        LocalIncomeColor provides incomeColor,
        LocalExpenseColor provides expenseColor,
        LocalBalanceCardColor provides balanceCardColor,
        LocalBalanceTextColor provides balanceTextColor,
        LocalFabColor provides fabColor,
        LocalWarningColor provides WarningColor,
        LocalInfoColor provides InfoColor
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}