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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Цвета для доходов и расходов
val LocalIncomeColor = staticCompositionLocalOf { Color.Unspecified }
val LocalExpenseColor = staticCompositionLocalOf { Color.Unspecified }

@Composable
fun FinanceAnalyzerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val appColors = AppColors(context)

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = appColors.md_theme_dark_primary,
            onPrimary = appColors.md_theme_dark_onPrimary,
            primaryContainer = appColors.md_theme_dark_primaryContainer,
            onPrimaryContainer = appColors.md_theme_dark_onPrimaryContainer,
            secondary = appColors.md_theme_dark_secondary,
            onSecondary = appColors.md_theme_dark_onSecondary,
            secondaryContainer = appColors.md_theme_dark_secondaryContainer,
            onSecondaryContainer = appColors.md_theme_dark_onSecondaryContainer,
            tertiary = appColors.md_theme_dark_tertiary,
            onTertiary = appColors.md_theme_dark_onTertiary,
            tertiaryContainer = appColors.md_theme_dark_tertiaryContainer,
            onTertiaryContainer = appColors.md_theme_dark_onTertiaryContainer,
            error = appColors.md_theme_dark_error,
            onError = appColors.md_theme_dark_onError,
            errorContainer = appColors.md_theme_dark_errorContainer,
            onErrorContainer = appColors.md_theme_dark_onErrorContainer,
            background = appColors.md_theme_dark_background,
            onBackground = appColors.md_theme_dark_onBackground,
            surface = appColors.md_theme_dark_surface,
            onSurface = appColors.md_theme_dark_onSurface,
            surfaceVariant = appColors.md_theme_dark_surfaceVariant,
            onSurfaceVariant = appColors.md_theme_dark_onSurfaceVariant,
            outline = appColors.md_theme_dark_outline
        )
        else -> lightColorScheme(
            primary = appColors.md_theme_light_primary,
            onPrimary = appColors.md_theme_light_onPrimary,
            primaryContainer = appColors.md_theme_light_primaryContainer,
            onPrimaryContainer = appColors.md_theme_light_onPrimaryContainer,
            secondary = appColors.md_theme_light_secondary,
            onSecondary = appColors.md_theme_light_onSecondary,
            secondaryContainer = appColors.md_theme_light_secondaryContainer,
            onSecondaryContainer = appColors.md_theme_light_onSecondaryContainer,
            tertiary = appColors.md_theme_light_tertiary,
            onTertiary = appColors.md_theme_light_onTertiary,
            tertiaryContainer = appColors.md_theme_light_tertiaryContainer,
            onTertiaryContainer = appColors.md_theme_light_onTertiaryContainer,
            error = appColors.md_theme_light_error,
            onError = appColors.md_theme_light_onError,
            errorContainer = appColors.md_theme_light_errorContainer,
            onErrorContainer = appColors.md_theme_light_onErrorContainer,
            background = appColors.md_theme_light_background,
            onBackground = appColors.md_theme_light_onBackground,
            surface = appColors.md_theme_light_surface,
            onSurface = appColors.md_theme_light_onSurface,
            surfaceVariant = appColors.md_theme_light_surfaceVariant,
            onSurfaceVariant = appColors.md_theme_light_onSurfaceVariant,
            outline = appColors.md_theme_light_outline
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Устанавливаем прозрачный статус бар и навигационную панель
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Настраиваем цвет иконок статус бара в зависимости от темы
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    // Определяем цвета для доходов и расходов в зависимости от темы
    val incomeColor = if (darkTheme) appColors.md_theme_dark_income else appColors.md_theme_light_income
    val expenseColor = if (darkTheme) appColors.md_theme_dark_expense else appColors.md_theme_light_expense

    CompositionLocalProvider(
        LocalIncomeColor provides incomeColor,
        LocalExpenseColor provides expenseColor
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}