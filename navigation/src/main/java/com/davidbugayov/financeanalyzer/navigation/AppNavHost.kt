package com.davidbugayov.financeanalyzer.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Главный компонент навигации приложения.
 * Определяет структуру навигации и переходы между экранами.
 *
 * @param navController Контроллер навигации
 * @param navigationManager Менеджер навигации для обработки команд
 * @param content Содержимое навигационного графа
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    navigationManager: NavigationManager,
    content: NavGraphBuilder.() -> Unit,
) {
    LaunchedEffect("navigation") {
        navigationManager.commands.onEach { command ->
            when (command) {
                is NavigationManager.Command.Navigate -> navController.navigate(
                    command.destination,
                ) { launchSingleTop = true }
                is NavigationManager.Command.NavigateUp -> navController.navigateUp()
                is NavigationManager.Command.PopUpTo -> navController.popBackStack(
                    command.destination,
                    command.inclusive,
                )
            }
        }.launchIn(this)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        builder = content,
    )
}

// --- Транзишены для читаемости ---
fun defaultEnterLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(400, easing = EaseInOut),
    ) + fadeIn(animationSpec = tween(400, easing = EaseInOut))
}

fun defaultExitRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(400, easing = EaseInOut),
    ) + fadeOut(animationSpec = tween(400, easing = EaseInOut))
}

fun defaultEnterRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(400, easing = EaseInOut),
    ) + fadeIn(animationSpec = tween(400, easing = EaseInOut))
}

fun defaultExitLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(400, easing = EaseInOut),
    ) + fadeOut(animationSpec = tween(400, easing = EaseInOut))
}

fun defaultEnterUp(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(400, easing = EaseInOut),
    ) + fadeIn(animationSpec = tween(400, easing = EaseInOut))
}

fun defaultExitDown(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Down,
        animationSpec = tween(400, easing = EaseInOut),
    ) + fadeOut(animationSpec = tween(400, easing = EaseInOut))
} 
