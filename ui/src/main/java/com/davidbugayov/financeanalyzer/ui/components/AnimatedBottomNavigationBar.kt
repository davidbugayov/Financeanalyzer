package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Enhanced bottom navigation bar with animations and support for arbitrary item set.
 *
 * @param visible whether the bar is visible
 * @param onChartClick callback for chart button
 * @param onHistoryClick callback for history button
 * @param onAddClick callback for add button
 * @param modifier for positioning
 */
@Composable
fun AnimatedBottomNavigationBar(
    visible: Boolean = true,
    onChartClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    // IME insets (keyboard)
    val imeVisible = WindowInsets.ime.getBottom(density) > 0

    // Don't show when keyboard is visible
    if (imeVisible) return

    AnimatedVisibility(
        visible = visible,
        enter =
            fadeIn(animationSpec = tween(400, easing = EaseInOut)) +
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(400, easing = EaseInOut),
                ),
        exit =
            fadeOut(animationSpec = tween(400, easing = EaseInOut)) +
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(400, easing = EaseInOut),
                ),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            shadowElevation = 0.dp,
            tonalElevation = dimensionResource(R.dimen.elevation_small),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            start = dimensionResource(R.dimen.spacing_small),
                            end = dimensionResource(R.dimen.spacing_small),
                            top = dimensionResource(R.dimen.spacing_xxsmall),
                            bottom = dimensionResource(R.dimen.spacing_xxsmall),
                        ).heightIn(min = dimensionResource(R.dimen.nav_bar_height)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavButton(
                    icon = Icons.Default.Assessment,
                    text = stringResource(R.string.statistics),
                    onClick = onChartClick,
                )

                NavButton(
                    icon = Icons.Default.Add,
                    text = stringResource(R.string.add_button),
                    onClick = onAddClick,
                )

                NavButton(
                    icon = Icons.Default.History,
                    text = stringResource(R.string.history),
                    onClick = onHistoryClick,
                )
            }
        }
    }
}

@Composable
private fun NavButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    val spacingXx = dimensionResource(R.dimen.spacing_xxsmall)
    val buttonSize = dimensionResource(R.dimen.main_nav_button_size)
    val iconSize = dimensionResource(R.dimen.main_nav_icon_size)
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent, contentColor = Color.Transparent),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .padding(start = spacingXx, end = spacingXx, top = 4.dp)
                    .widthIn(min = buttonSize + spacingXx),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier =
                    Modifier
                        .clip(
                            CircleShape,
                        ).background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp)
                        .size(iconSize),
                tint = Color.White,
            )

            Text(
                text = text,
                fontSize = dimensionResource(R.dimen.text_small).value.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}
