package com.davidbugayov.financeanalyzer.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

// New Primary Palette based on #3DCFDC
val NewPrimary = Color(0xFF3DCFDC)
val OnNewPrimary = Color.White // Adjusted for better contrast with a lighter primary
val NewPrimaryLight = Color(0xFFE0F7FA) // Very light cyan/turquoise for light theme container
val OnNewPrimaryContainer = Color(0xFF00373D) // Dark text for light container

val NewPrimaryDark = Color(0xFF007C8C) // Darker cyan/turquoise for dark theme primary
val DarkPrimaryContainer = Color(0xFF005662) // Dark container for dark theme
val OnDarkPrimaryContainer = Color(0xFFB2EBF2) // Light text for dark container

// Friendly Card Background Colors
val FriendlyCardBackgroundLight = Color(0xFFE0F2F1) // Очень светлый бирюзовый (Soft Teal Light)
val FriendlyCardBackgroundDark = Color(0xFF26A69A) //  Умеренно темный бирюзовый (Soft Teal Dark)

// Material Theme Colors - Light
val md_theme_light_primary = NewPrimary
val md_theme_light_onPrimary = OnNewPrimary
val md_theme_light_primaryContainer = NewPrimaryLight
val md_theme_light_onPrimaryContainer = OnNewPrimaryContainer
val md_theme_light_secondary = Color(0xFF5677FC) // Более насыщенный синий для кнопки "Добавить"
val md_theme_light_onSecondary = Color.White
val md_theme_light_secondaryContainer = Color(0xFFB3E5FC)
val md_theme_light_onSecondaryContainer = Color(0xFF0288D1)
val md_theme_light_tertiary = Color(0xFF00BCD4)
val md_theme_light_onTertiary = Color.White
val md_theme_light_tertiaryContainer = Color(0xFFB2EBF2)
val md_theme_light_onTertiaryContainer = Color(0xFF0097A7)
val md_theme_light_error = Color(0xFFF44336)
val md_theme_light_onError = Color.White
val md_theme_light_errorContainer = Color(0xFFFFCDD2)
val md_theme_light_onErrorContainer = Color(0xFFD32F2F)
val md_theme_light_background = Color(0xFFFAFAFA)
val md_theme_light_onBackground = Color(0xFF212121)
val md_theme_light_surface = Color.White
val md_theme_light_onSurface = Color(0xFF212121)
val md_theme_light_surfaceVariant = Color(0xFFF5F5F5)
val md_theme_light_onSurfaceVariant = Color(0xFF757575)
val md_theme_light_outline = Color(0xFFBDBDBD)

// Material Theme Colors - Dark
val md_theme_dark_primary = NewPrimaryDark
val md_theme_dark_onPrimary = OnNewPrimary // Assuming black text works for NewPrimaryDark as well, or adjust if needed
val md_theme_dark_primaryContainer = DarkPrimaryContainer
val md_theme_dark_onPrimaryContainer = OnDarkPrimaryContainer
val md_theme_dark_secondary = Color(0xFF64B5E8) // Блекло-голубой
val md_theme_dark_onSecondary = Color.Black
val md_theme_dark_secondaryContainer = Color(0xFF4D92BC) // Более темный голубой
val md_theme_dark_onSecondaryContainer = Color(0xFFCCE9FF)
val md_theme_dark_tertiary = Color(0xFF80DEEA)
val md_theme_dark_onTertiary = Color.Black
val md_theme_dark_tertiaryContainer = Color(0xFF0097A7)
val md_theme_dark_onTertiaryContainer = Color(0xFFB2EBF2)
val md_theme_dark_error = Color(0xFFEF9A9A)
val md_theme_dark_onError = Color.Black
val md_theme_dark_errorContainer = Color(0xFFD32F2F)
val md_theme_dark_onErrorContainer = Color(0xFFFFCDD2)
val md_theme_dark_background = Color(0xFF121212)
val md_theme_dark_onBackground = Color.White
val md_theme_dark_surface = Color(0xFF1E1E1E)
val md_theme_dark_onSurface = Color.White
val md_theme_dark_surfaceVariant = Color(0xFF2D2D2D)
val md_theme_dark_onSurfaceVariant = Color(0xFFBDBDBD)
val md_theme_dark_outline = Color(0xFF757575)

// Semantic Colors - Light Theme
val IncomeColorLight = Color(0xFF4CAF50) // Зеленый
val ExpenseColorLight = Color(0xFFF44336) // Красный
val FabColorLight = md_theme_light_secondary // Используем secondary для FAB
val BalanceTextColorLight = Color(0xFF2196F3) // Яркий синий для текста баланса
val SuccessColorLight = Color(0xFF388E3C) // Темно-зеленый для успеха
val WarningColorLight = Color(0xFFFFA000) // Оранжевый для предупреждений
val InfoColorLight = Color(0xFF1976D2)    // Синий для информации
val TransferColorLight = Color(0xFF757575) // Серый для переводов (onSurfaceVariant)

// Semantic Colors - Dark Theme
val IncomeColorDark = Color(0xFF81C784)   // Светло-зеленый
val ExpenseColorDark = Color(0xFFEF5350)  // Светло-красный
val FabColorDark = md_theme_dark_secondary // Используем secondary для FAB
val BalanceTextColorDark = Color(0xFF81CFEF) // Нежно-голубой
val SuccessColorDark = Color(0xFF81C784)   // Светло-зеленый для успеха
val WarningColorDark = Color(0xFFFFB74D) // Темно-оранжевый для предупреждений
val InfoColorDark = Color(0xFF64B5F6)     // Темно-голубой для информации
val TransferColorDark = Color(0xFF4DB6AC) // Темный бирюзовый для переводов

// Bank Colors (Consistent across themes)
val BankSber = Color(0xFF1A9F29)
val BankAlfa = Color(0xFFEC3239)
val BankTinkoff = Color(0xFFFFDD2D) // Желтый, требует хорошего контраста с текстом
val BankVTB = Color(0xFF009FDF)
val BankGazprom = Color(0xFF0079C1)
val BankRaiffeisen = Color(0xFFFFED00) // Очень светлый желтый, аналогично Tinkoff
val BankOzon = Color(0xFF005BFF) // Примерный цвет Ozon, может потребоваться уточнение
val BankPochta = Color(0xFF74397E)
val BankYoomoney = Color(0xFF8F2FE2)
val CashColor = Color(0xFF9E9E9E) // Серый для наличных

// Chart Basic Element Colors - Light
val ChartGridColorLight = md_theme_light_outline.copy(alpha = 0.5f)
val ChartAxisTextColorLight = md_theme_light_onSurfaceVariant
val ChartBackgroundColorLight = md_theme_light_background
val ChartEmptyStateTextColorLight = md_theme_light_onSurfaceVariant.copy(alpha = 0.7f)

// Chart Basic Element Colors - Dark
val ChartGridColorDark = md_theme_dark_outline.copy(alpha = 0.5f)
val ChartAxisTextColorDark = md_theme_dark_onSurfaceVariant
val ChartBackgroundColorDark = md_theme_dark_background
val ChartEmptyStateTextColorDark = md_theme_dark_onSurfaceVariant.copy(alpha = 0.7f)

// SourceItem colors
val SourceItemErrorBackgroundColor = Color(0xFFFFCDD2) // Красноватый фон для ошибки
val SourceItemErrorContentColor = Color.Red // Цвет текста для ошибки
val SourceItemBorderWidth = 3.dp // Толщина рамки выбранного элемента
val SourceItemNoBorderWidth = 0.dp // Толщина рамки невыбранного элемента

// Specific UI Element Colors - Light
val PositiveBackgroundLight = IncomeColorLight.copy(alpha = 0.1f)
val PositiveTextLight = IncomeColorLight
val NegativeBackgroundLight = ExpenseColorLight.copy(alpha = 0.1f)
val NegativeTextLight = ExpenseColorLight
val ErrorStateBackgroundLight = md_theme_light_errorContainer
val ErrorStateContentLight = md_theme_light_onErrorContainer

// Specific UI Element Colors - Dark
val PositiveBackgroundDark = IncomeColorDark.copy(alpha = 0.15f) // Немного больше alpha для темной темы
val PositiveTextDark = IncomeColorDark
val NegativeBackgroundDark = ExpenseColorDark.copy(alpha = 0.15f)
val NegativeTextDark = ExpenseColorDark
val ErrorStateBackgroundDark = md_theme_dark_errorContainer
val ErrorStateContentDark = md_theme_dark_onErrorContainer

// Individual Category Colors (examples, to be populated)
// These should ideally match what was in colors.xml or CategoryColors object
val DefaultCategoryColor = Color(0xFFB0BEC5) // Light Grey Blue - Moved definition up

val CategoryFood = ExpenseColorLight // F44336 (Red) - Оставить
val CategoryTransport = md_theme_light_primary // 2196F3 (Blue) - Оставить
val CategoryEntertainment = WarningColorLight // Замена Color(0xFFFF9800) на WarningColorLight (FFA000)
val CategoryHealth = md_theme_light_tertiary // Замена IncomeColorLight на md_theme_light_tertiary (00BCD4 - Cyan)
val CategoryShopping = Color(0xFF9C27B0) // Purple - Оставить
val CategoryHousing = Color(0xFF795548) // Brown - Оставить
val CategoryUtilities = Color(0xFF607D8B) // Blue Grey - Оставить
val CategoryEducation = Color(0xFF3F51B5) // Indigo - Оставить
val CategoryRestaurant = Color(0xFFFF7043) // Замена Color(0xFF795548) на Deep Orange Light (FF7043)
val CategoryClothing = Color(0xFFBA68C8) // Light Purple - Оставить
val CategoryCommunication = Color(0xFF4FC3F7) // Light Blue - Оставить
val CategoryPet = Color(0xFFFFD54F) // Amber/Yellow - Оставить
val CategoryServices = Color(0xFFA1887F) // Greyish Brown - Оставить
val CategoryCharity = DefaultCategoryColor // Замена IncomeColorDark на DefaultCategoryColor (B0BEC5)
val CategoryCredit = Color(0xFF90A4AE) // Blue Grey Light - Оставить
val CategoryTransfer = TransferColorLight // B0BEC5 (Blue Grey Lighter for light theme) - Оставить
val CategoryOtherExpense = CashColor // Specific for expense 'other' - Оставить
val CategoryOtherIncome = IncomeColorLight  // Замена CashColor на IncomeColorLight

// Income Specific Categories
val CategorySalary = IncomeColorLight // 4CAF50 (Green) - Оставить
val CategoryBusiness = Color(0xFF009688) // Teal - Оставить
val CategoryInvestments = Color(0xFFFFEB3B) // Yellow - Оставить
val CategoryFreelance = Color(0xFF7986CB) // Indigo Light - Оставить
val CategoryGifts = Color(0xFFF06292) // Pink (Listed as income gift in provider) - Оставить
val CategoryInterest = Color(0xFFFFD600) // Yellow Darker - Оставить
val CategoryRental = Color(0xFF8D6E63) // Brownish Grey (Listed as income rent) - Оставить

// Maps for category colors
val incomeCategoryColorsMap: Map<String, Color> = mapOf(
    "salary" to CategorySalary,
    "business" to CategoryBusiness,
    "investments" to CategoryInvestments,
    "rental" to CategoryRental,
    "gifts" to CategoryGifts,
    "other_income" to CategoryOtherIncome,
    "freelance" to CategoryFreelance,
    "interest" to CategoryInterest
).withDefault { DefaultCategoryColor }

val expenseCategoryColorsMap: Map<String, Color> = mapOf(
    "food" to CategoryFood,
    "transport" to CategoryTransport,
    "housing" to CategoryHousing,
    "entertainment" to CategoryEntertainment,
    "health" to CategoryHealth,
    "education" to CategoryEducation,
    "shopping" to CategoryShopping,
    "utilities" to CategoryUtilities,
    "other_expense" to CategoryOtherExpense,
    "restaurant" to CategoryRestaurant,
    "clothing" to CategoryClothing,
    "communication" to CategoryCommunication,
    "pet" to CategoryPet,
    "services" to CategoryServices,
    "charity" to CategoryCharity,
    "credit" to CategoryCredit,
    "transfer" to CategoryTransfer
).withDefault { DefaultCategoryColor }

/**
 * Converts a Compose Color object to a HEX string (e.g., "#RRGGBB").
 * Alpha channel is ignored for simplicity, as most category colors don't use it.
 */
fun Color.toHexString(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return String.format("#%02X%02X%02X", red, green, blue)
}

// Добавляем Int ARGB репрезентации для использования в не-Composable контекстах
val IncomeColorInt = IncomeColorLight.toArgb()
val ExpenseColorInt = ExpenseColorLight.toArgb()

// Palettes (Can be expanded and refined)
val IncomeChartPalette = listOf(
    Color(0xFF4CAF50), Color(0xFF66BB6A), Color(0xFF81C784),
    Color(0xFFA5D6A7), Color(0xFFC8E6C9), Color(0xFF00C853)
)

val ExpenseChartPalette = listOf(
    Color(0xFFF44336), Color(0xFFEF5350), Color(0xFFE57373),
    Color(0xFFEF9A9A), Color(0xFFFFCDD2), Color(0xFFD50000)
)