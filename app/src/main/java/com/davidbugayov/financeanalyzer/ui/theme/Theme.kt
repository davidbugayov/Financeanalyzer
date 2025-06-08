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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode

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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // window.statusBarColor = colorScheme.background.toArgb() // Removed deprecated call
            // window.navigationBarColor = colorScheme.background.toArgb() // Removed deprecated call
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val incomeColor = if (darkTheme) IncomeColorDark else IncomeColorLight
    val expenseColor = if (darkTheme) ExpenseColorDark else ExpenseColorLight
    val fabColor = if (darkTheme) FabColorDark else FabColorLight
    val balanceTextColor = if (darkTheme) BalanceTextColorDark else BalanceTextColorLight
    val successColor = if (darkTheme) SuccessColorDark else SuccessColorLight
    val warningColor = if (darkTheme) WarningColorDark else WarningColorLight
    val infoColor = if (darkTheme) InfoColorDark else InfoColorLight
    val transferColor = if (darkTheme) TransferColorDark else TransferColorLight

    val chartGridColor = if (darkTheme) ChartGridColorDark else ChartGridColorLight
    val chartAxisTextColor = if (darkTheme) ChartAxisTextColorDark else ChartAxisTextColorLight
    val chartBackgroundColor = if (darkTheme) ChartBackgroundColorDark else ChartBackgroundColorLight
    val chartEmptyStateTextColor = if (darkTheme) ChartEmptyStateTextColorDark else ChartEmptyStateTextColorLight

    val positiveBackgroundColor = if (darkTheme) PositiveBackgroundDark else PositiveBackgroundLight
    val positiveTextColor = if (darkTheme) PositiveTextDark else PositiveTextLight
    val negativeBackgroundColor = if (darkTheme) NegativeBackgroundDark else NegativeBackgroundLight
    val negativeTextColor = if (darkTheme) NegativeTextDark else NegativeTextLight
    val errorStateBackgroundColor = if (darkTheme) ErrorStateBackgroundDark else ErrorStateBackgroundLight
    val errorStateContentColor = if (darkTheme) ErrorStateContentDark else ErrorStateContentLight
    val friendlyCardBackgroundColor = if (darkTheme) FriendlyCardBackgroundDark else FriendlyCardBackgroundLight

    val summaryCardBg = if (darkTheme) SummaryCardBackgroundDark else SummaryCardBackgroundLight
    val summaryTextPrimary = if (darkTheme) SummaryTextPrimaryDark else SummaryTextPrimaryLight
    val summaryTextSecondary = if (darkTheme) SummaryTextSecondaryDark else SummaryTextSecondaryLight
    val summaryIncome = if (darkTheme) SummaryIncomeDark else SummaryIncomeLight
    val summaryExpense = if (darkTheme) SummaryExpenseDark else SummaryExpenseLight
    val summaryDivider = if (darkTheme) SummaryDividerDark else SummaryDividerLight

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
