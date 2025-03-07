package com.davidbugayov.financeanalyzer.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Класс для определения размеров экрана и адаптации интерфейса
 */
class WindowSize(
    val width: WindowType,
    val height: WindowType
)

/**
 * Типы размеров экрана
 */
enum class WindowType {

    COMPACT,    // Телефон в портретной ориентации
    MEDIUM,     // Телефон в ландшафтной ориентации или планшет в портретной
    EXPANDED    // Планшет в ландшафтной ориентации
}

/**
 * Пороговые значения для определения типа экрана
 */
object WindowSizeThresholds {

    val COMPACT_WIDTH = 600.dp
    val MEDIUM_WIDTH = 840.dp

    val COMPACT_HEIGHT = 480.dp
    val MEDIUM_HEIGHT = 900.dp
}

/**
 * Определяет тип размера экрана на основе ширины
 */
fun getWindowWidthType(width: Dp): WindowType = when {
    width < WindowSizeThresholds.COMPACT_WIDTH -> WindowType.COMPACT
    width < WindowSizeThresholds.MEDIUM_WIDTH -> WindowType.MEDIUM
    else -> WindowType.EXPANDED
}

/**
 * Определяет тип размера экрана на основе высоты
 */
fun getWindowHeightType(height: Dp): WindowType = when {
    height < WindowSizeThresholds.COMPACT_HEIGHT -> WindowType.COMPACT
    height < WindowSizeThresholds.MEDIUM_HEIGHT -> WindowType.MEDIUM
    else -> WindowType.EXPANDED
}

/**
 * Composable функция для получения текущего размера экрана
 */
@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    return remember(screenWidth, screenHeight) {
        WindowSize(
            width = getWindowWidthType(screenWidth),
            height = getWindowHeightType(screenHeight)
        )
    }
}

/**
 * Расширение для определения, является ли экран компактным (телефон)
 */
fun WindowSize.isCompact(): Boolean = width == WindowType.COMPACT

/**
 * Расширение для определения, является ли экран средним (большой телефон или планшет в портретной ориентации)
 */
fun WindowSize.isMedium(): Boolean = width == WindowType.MEDIUM

/**
 * Расширение для определения, является ли экран расширенным (планшет в ландшафтной ориентации)
 */
fun WindowSize.isExpanded(): Boolean = width == WindowType.EXPANDED

/**
 * Расширение для определения, является ли экран достаточно широким для двухпанельного интерфейса
 */
fun WindowSize.isTwoPanelLayout(): Boolean = width != WindowType.COMPACT 