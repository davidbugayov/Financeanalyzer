package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.home.R
import com.davidbugayov.financeanalyzer.ui.R as UiR

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeTipsCard(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(true) }
    var offsetX by remember { mutableStateOf(0f) }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
    ) {
        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            offsetX += dragAmount
                            if (offsetX > 200f || offsetX < -200f) {
                                visible = false
                                onClose()
                            }
                        }
                    },
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier =
                    Modifier
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        .alpha(if (visible) 1f else 0f),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // иллюстрация убрана для минимализма
                    // Кнопка закрытия
                    IconButton(
                        onClick = {
                            visible = false
                            onClose()
                        },
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column(
                        modifier =
                            Modifier
                                .padding(start = 24.dp, end = 56.dp, top = 24.dp, bottom = 24.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.home_tips_card_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CustomTipRow(
                            iconRes = UiR.drawable.ic_profile, // достижения
                            text = stringResource(R.string.tip_achievements),
                        )
                        CustomTipRow(
                            iconRes = UiR.drawable.ic_receipt, // импорт
                            text = stringResource(R.string.tip_imports),
                        )
                        CustomTipRow(
                            iconRes = UiR.drawable.ic_chart, // статистика
                            text = stringResource(R.string.tip_statistics),
                        )
                        CustomTipRow(
                            iconRes = UiR.drawable.ic_profile, // рекомендации (или ic_star если появится)
                            text = stringResource(R.string.tip_recommendations),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomTipRow(
    iconRes: Int,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
} 
