package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.home.R
import com.davidbugayov.financeanalyzer.ui.components.card.SmartCardStyle
import com.davidbugayov.financeanalyzer.ui.components.card.SmartRecommendationCard
import com.davidbugayov.financeanalyzer.ui.components.card.SmartRecommendationGenerator

/**
 * 🏠 Современная карточка советов для главного экрана
 * Использует новую Smart Recommendation System
 */
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
            // Используем новую систему рекомендаций
            val onboardingRecommendations = SmartRecommendationGenerator.generateOnboardingRecommendations()

            SmartRecommendationCard(
                recommendations = onboardingRecommendations,
                title = stringResource(R.string.welcome_title),
                subtitle = stringResource(R.string.welcome_subtitle),
                style = SmartCardStyle.MINIMAL,
                showPriorityIndicator = false,
                onDismiss = {
                    visible = false
                    onClose()
                },
                modifier = Modifier.alpha(if (visible) 1f else 0f),
            )
        }
    }
}
