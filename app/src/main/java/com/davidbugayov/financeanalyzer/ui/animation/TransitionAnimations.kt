package com.davidbugayov.financeanalyzer.ui.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

/**
 * Стандартные параметры анимации
 */
object AnimationDefaults {
    const val DURATION_SHORT = 200
    const val DURATION_MEDIUM = 300
    const val DURATION_LONG = 500
    
    val easeInOut = EaseInOut
    val fastOutSlowIn = FastOutSlowInEasing
}

/**
 * Анимации для перехода между экранами
 */
object ScreenTransitions {
    /**
     * Анимация перехода слева направо (для навигации вперед)
     */
    fun slideInFromRight(): EnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(AnimationDefaults.DURATION_MEDIUM, easing = AnimationDefaults.easeInOut)
    ) + fadeIn(animationSpec = tween(AnimationDefaults.DURATION_MEDIUM))
    
    /**
     * Анимация выхода слева направо (для навигации назад)
     */
    fun slideOutToRight(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(AnimationDefaults.DURATION_MEDIUM, easing = AnimationDefaults.easeInOut)
    ) + fadeOut(animationSpec = tween(AnimationDefaults.DURATION_MEDIUM))
    
    /**
     * Анимация перехода справа налево (для навигации назад)
     */
    fun slideInFromLeft(): EnterTransition = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(AnimationDefaults.DURATION_MEDIUM, easing = AnimationDefaults.easeInOut)
    ) + fadeIn(animationSpec = tween(AnimationDefaults.DURATION_MEDIUM))
    
    /**
     * Анимация выхода справа налево (для навигации вперед)
     */
    fun slideOutToLeft(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(AnimationDefaults.DURATION_MEDIUM, easing = AnimationDefaults.easeInOut)
    ) + fadeOut(animationSpec = tween(AnimationDefaults.DURATION_MEDIUM))
    
    /**
     * Анимация перехода снизу вверх
     */
    fun slideInFromBottom(): EnterTransition = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(AnimationDefaults.DURATION_MEDIUM, easing = AnimationDefaults.easeInOut)
    ) + fadeIn(animationSpec = tween(AnimationDefaults.DURATION_MEDIUM))
    
    /**
     * Анимация выхода снизу вверх
     */
    fun slideOutToBottom(): ExitTransition = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(AnimationDefaults.DURATION_MEDIUM, easing = AnimationDefaults.easeInOut)
    ) + fadeOut(animationSpec = tween(AnimationDefaults.DURATION_MEDIUM))
}

/**
 * Анимации для диалогов и всплывающих элементов
 */
object DialogTransitions {
    fun fadeInWithScale(): EnterTransition = fadeIn(
        animationSpec = tween(AnimationDefaults.DURATION_SHORT, easing = AnimationDefaults.easeInOut)
    ) + scaleIn(
        initialScale = 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    fun fadeOutWithScale(): ExitTransition = fadeOut(
        animationSpec = tween(AnimationDefaults.DURATION_SHORT)
    ) + scaleOut(
        targetScale = 0.9f,
        animationSpec = tween(AnimationDefaults.DURATION_SHORT, easing = AnimationDefaults.easeInOut)
    )
    
    fun expandVertically(): EnterTransition = expandVertically(
        animationSpec = tween(AnimationDefaults.DURATION_SHORT, easing = AnimationDefaults.easeInOut)
    ) + fadeIn(animationSpec = tween(AnimationDefaults.DURATION_SHORT))
    
    fun shrinkVertically(): ExitTransition = shrinkVertically(
        animationSpec = tween(AnimationDefaults.DURATION_SHORT, easing = AnimationDefaults.easeInOut)
    ) + fadeOut(animationSpec = tween(AnimationDefaults.DURATION_SHORT))
}