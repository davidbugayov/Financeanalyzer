package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R

/**
 * Улучшенная нижняя навигационная панель с анимацией и поддержкой произвольного набора пунктов
 *
 * @param visible видима ли панель
 * @param onAddClick обработчик нажатия на кнопку "Добавить"
 * @param onChartClick обработчик нажатия на кнопку "Графики"
 * @param onHistoryClick обработчик нажатия на кнопку "История"
 * @param onBudgetClick обработчик нажатия на кнопку "Бюджет"
 * @param modifier модификатор для управления позиционированием
 */
@Composable
fun AnimatedBottomNavigationBar(
    visible: Boolean = true,
    onChartClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Получаем отступы от системной навигации
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val density = LocalDensity.current
    
    // IME Insets для корректной работы с клавиатурой
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    
    // Не показываем навигацию, если клавиатура видима
    if (imeVisible) return
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400, easing = EaseInOut)) + slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(400, easing = EaseInOut)
        ),
        exit = fadeOut(animationSpec = tween(400, easing = EaseInOut)) + slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(400, easing = EaseInOut)
        ),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            shadowElevation = 0.dp,
            tonalElevation = dimensionResource(R.dimen.elevation_small)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(R.dimen.spacing_small),
                        end = dimensionResource(R.dimen.spacing_small),
                        top = dimensionResource(R.dimen.spacing_xxsmall),
                        bottom = dimensionResource(R.dimen.spacing_xxsmall) + navBarPadding.calculateBottomPadding()
                    )
                    .heightIn(min = dimensionResource(R.dimen.nav_bar_height)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Статистика (слева)
                NavButton(
                    icon = Icons.Default.Assessment,
                    text = stringResource(R.string.statistics),
                    onClick = onChartClick,
                    isMain = false
                )

                // Бюджет (центр)
                NavButton(
                    icon = Icons.Default.AccountBalanceWallet,
                    text = stringResource(R.string.budget),
                    onClick = onBudgetClick,
                    isMain = false
                )

                // История (справа)
                NavButton(
                    icon = Icons.Default.History,
                    text = stringResource(R.string.history),
                    onClick = onHistoryClick,
                    isMain = false
                )
            }
        }
    }
}

/**
 * Кнопка навигации для нижней панели
 *
 * @param icon иконка кнопки
 * @param text текст под кнопкой
 * @param onClick обработчик нажатия
 * @param isMain является ли кнопка главной (будет выделена)
 */
@Composable
private fun NavButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isMain: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = dimensionResource(R.dimen.spacing_xxsmall))
            .widthIn(min = dimensionResource(R.dimen.main_nav_button_size) + dimensionResource(R.dimen.spacing_xxsmall))
    ) {
        if (isMain) {
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier.size(dimensionResource(R.dimen.main_nav_button_size))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(dimensionResource(R.dimen.main_nav_icon_size))
                )
            }
        } else {
            FilledTonalIconButton(
                onClick = onClick,
                modifier = Modifier.size(dimensionResource(R.dimen.nav_button_size))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(dimensionResource(R.dimen.nav_icon_size))
                )
            }
        }

        Text(
            text = text,
            fontSize = dimensionResource(R.dimen.text_small).value.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}