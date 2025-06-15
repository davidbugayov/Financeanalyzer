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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Enum для выбора темы приложения
 */
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

// Цвета для светлой темы (используются стандартные Material Design слоты)
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
    outline = md_theme_light_outline,
)

// Цвета для темной темы (используются стандартные Material Design слоты)
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
    outline = md_theme_dark_outline,
)

// Local providers для кастомных семантических цветов
val LocalIncomeColor = staticCompositionLocalOf { IncomeColorLight }
val LocalExpenseColor = staticCompositionLocalOf { ExpenseColorLight }
val LocalFabColor = staticCompositionLocalOf { FabColorLight }
val LocalBalanceTextColor = staticCompositionLocalOf { BalanceTextColorLight }
// LocalBalanceCardColor больше не нужен, т.к. md_theme_light_primaryContainer используется напрямую в colorScheme
// и может быть получен через MaterialTheme.colorScheme.primaryContainer

val LocalSuccessColor = staticCompositionLocalOf { SuccessColorLight }
val LocalWarningColor = staticCompositionLocalOf { WarningColorLight }
val LocalInfoColor = staticCompositionLocalOf { InfoColorLight }
val LocalTransferColor = staticCompositionLocalOf { TransferColorLight }

// Local providers для цветов элементов графиков
val LocalChartGridColor = staticCompositionLocalOf { ChartGridColorLight }
val LocalChartAxisTextColor = staticCompositionLocalOf { ChartAxisTextColorLight }
val LocalChartBackgroundColor = staticCompositionLocalOf { ChartBackgroundColorLight }
val LocalChartEmptyStateTextColor = staticCompositionLocalOf { ChartEmptyStateTextColorLight }

// Local providers для специфичных UI элементов
val LocalPositiveBackgroundColor = staticCompositionLocalOf { PositiveBackgroundLight }
val LocalPositiveTextColor = staticCompositionLocalOf { PositiveTextLight }
val LocalNegativeBackgroundColor = staticCompositionLocalOf { NegativeBackgroundLight }
val LocalNegativeTextColor = staticCompositionLocalOf { NegativeTextLight }
val LocalErrorStateBackgroundColor = staticCompositionLocalOf { ErrorStateBackgroundLight }
val LocalErrorStateContentColor = staticCompositionLocalOf { ErrorStateContentLight }
val LocalFriendlyCardBackgroundColor = staticCompositionLocalOf { FriendlyCardBackgroundLight }

val LocalSummaryCardBackground = staticCompositionLocalOf { SummaryCardBackgroundLight }
val LocalSummaryTextPrimary = staticCompositionLocalOf { SummaryTextPrimaryLight }
val LocalSummaryTextSecondary = staticCompositionLocalOf { SummaryTextSecondaryLight }
val LocalSummaryIncome = staticCompositionLocalOf { SummaryIncomeLight }
val LocalSummaryExpense = staticCompositionLocalOf { SummaryExpenseLight }
val LocalSummaryDivider = staticCompositionLocalOf { SummaryDividerLight }

@Composable
fun FinanceAnalyzerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Определяем, использовать ли темную тему
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Получаем цветовую схему в зависимости от темы и поддержки динамических цветов
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme -> DarkColors
        else -> LightColors
    }

    // Устанавливаем цвет статус-бара и панели навигации
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Настраиваем цвет иконок в зависимости от темы
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDarkTheme
            insetsController.isAppearanceLightNavigationBars = !isDarkTheme

            // Обновляем декорации окна
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Устанавливаем прозрачные системные панели
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.Transparent.toArgb()
        }
    }

    val incomeColor = if (isDarkTheme) IncomeColorDark else IncomeColorLight
    val expenseColor = if (isDarkTheme) ExpenseColorDark else ExpenseColorLight
    val fabColor = if (isDarkTheme) FabColorDark else FabColorLight
    val balanceTextColor = if (isDarkTheme) BalanceTextColorDark else BalanceTextColorLight
    val successColor = if (isDarkTheme) SuccessColorDark else SuccessColorLight
    val warningColor = if (isDarkTheme) WarningColorDark else WarningColorLight
    val infoColor = if (isDarkTheme) InfoColorDark else InfoColorLight
    val transferColor = if (isDarkTheme) TransferColorDark else TransferColorLight

    val chartGridColor = if (isDarkTheme) ChartGridColorDark else ChartGridColorLight
    val chartAxisTextColor = if (isDarkTheme) ChartAxisTextColorDark else ChartAxisTextColorLight
    val chartBackgroundColor = if (isDarkTheme) ChartBackgroundColorDark else ChartBackgroundColorLight
    val chartEmptyStateTextColor = if (isDarkTheme) ChartEmptyStateTextColorDark else ChartEmptyStateTextColorLight

    val positiveBackgroundColor = if (isDarkTheme) PositiveBackgroundDark else PositiveBackgroundLight
    val positiveTextColor = if (isDarkTheme) PositiveTextDark else PositiveTextLight
    val negativeBackgroundColor = if (isDarkTheme) NegativeBackgroundDark else NegativeBackgroundLight
    val negativeTextColor = if (isDarkTheme) NegativeTextDark else NegativeTextLight
    val errorStateBackgroundColor = if (isDarkTheme) ErrorStateBackgroundDark else ErrorStateBackgroundLight
    val errorStateContentColor = if (isDarkTheme) ErrorStateContentDark else ErrorStateContentLight
    val friendlyCardBackgroundColor = if (isDarkTheme) FriendlyCardBackgroundDark else FriendlyCardBackgroundLight

    val summaryCardBg = if (isDarkTheme) SummaryCardBackgroundDark else SummaryCardBackgroundLight
    val summaryTextPrimary = if (isDarkTheme) SummaryTextPrimaryDark else SummaryTextPrimaryLight
    val summaryTextSecondary = if (isDarkTheme) SummaryTextSecondaryDark else SummaryTextSecondaryLight
    val summaryIncome = if (isDarkTheme) SummaryIncomeDark else SummaryIncomeLight
    val summaryExpense = if (isDarkTheme) SummaryExpenseDark else SummaryExpenseLight
    val summaryDivider = if (isDarkTheme) SummaryDividerDark else SummaryDividerLight

    CompositionLocalProvider(
        LocalSummaryCardBackground provides summaryCardBg,
        LocalSummaryTextPrimary provides summaryTextPrimary,
        LocalSummaryTextSecondary provides summaryTextSecondary,
        LocalSummaryIncome provides summaryIncome,
        LocalSummaryExpense provides summaryExpense,
        LocalSummaryDivider provides summaryDivider,
        LocalIncomeColor provides incomeColor,
        LocalExpenseColor provides expenseColor,
        LocalFabColor provides fabColor,
        LocalBalanceTextColor provides balanceTextColor,
        LocalSuccessColor provides successColor,
        LocalWarningColor provides warningColor,
        LocalInfoColor provides infoColor,
        LocalTransferColor provides transferColor,
        LocalChartGridColor provides chartGridColor,
        LocalChartAxisTextColor provides chartAxisTextColor,
        LocalChartBackgroundColor provides chartBackgroundColor,
        LocalChartEmptyStateTextColor provides chartEmptyStateTextColor,
        LocalPositiveBackgroundColor provides positiveBackgroundColor,
        LocalPositiveTextColor provides positiveTextColor,
        LocalNegativeBackgroundColor provides negativeBackgroundColor,
        LocalNegativeTextColor provides negativeTextColor,
        LocalErrorStateBackgroundColor provides errorStateBackgroundColor,
        LocalErrorStateContentColor provides errorStateContentColor,
        LocalFriendlyCardBackgroundColor provides friendlyCardBackgroundColor,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
} 