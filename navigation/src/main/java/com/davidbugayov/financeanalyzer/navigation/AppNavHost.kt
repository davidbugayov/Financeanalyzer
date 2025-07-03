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

/**
 * Расширение для AppNavHost, которое создает все графы навигации приложения.
 *
 * @param navController Контроллер навигации
 * @param navigationManager Менеджер навигации для обработки команд
 * @param appNavigation Класс с определениями графов навигации
 * @param onHomeScreen Функция для отображения экрана Home
 * @param onHistoryScreen Функция для отображения экрана History
 * @param onBudgetScreen Функция для отображения экрана Budget
 * @param onFinancialStatisticsScreen Функция для отображения экрана FinancialStatistics
 * @param onWalletTransactionsScreen Функция для отображения экрана WalletTransactions
 * @param onAddTransactionScreen Функция для отображения экрана AddTransaction
 * @param onEditTransactionScreen Функция для отображения экрана EditTransaction
 * @param onImportTransactionsScreen Функция для отображения экрана ImportTransactions
 * @param onAchievementsScreen Функция для отображения экрана Achievements
 * @param onProfileScreen Функция для отображения экрана Profile
 * @param onLibrariesScreen Функция для отображения экрана Libraries
 * @param onExportImportScreen Функция для отображения экрана ExportImport
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    navigationManager: NavigationManager,
    appNavigation: AppNavigation,
    onHomeScreen: @Composable () -> Unit,
    onHistoryScreen: @Composable () -> Unit,
    onBudgetScreen: @Composable () -> Unit,
    onFinancialStatisticsScreen: @Composable (startDate: Long, endDate: Long, periodType: String?) -> Unit,
    onWalletTransactionsScreen: @Composable (walletId: String) -> Unit,
    onWalletSetupScreen: @Composable () -> Unit = {},
    onSubWalletsScreen: @Composable (parentWalletId: String) -> Unit = { _ -> },
    onAddTransactionScreen: @Composable (category: String?, forceExpense: Boolean?) -> Unit,
    onEditTransactionScreen: @Composable (transactionId: String) -> Unit,
    onImportTransactionsScreen: @Composable () -> Unit,
    onAchievementsScreen: @Composable () -> Unit,
    onProfileScreen: @Composable () -> Unit,
    onLibrariesScreen: @Composable () -> Unit,
    onExportImportScreen: @Composable () -> Unit,
) {
    AppNavHost(navController, navigationManager) {
        with(appNavigation) {
            mainGraph(
                onHomeScreen,
                onHistoryScreen,
                onBudgetScreen,
                onFinancialStatisticsScreen,
                onWalletTransactionsScreen,
                onWalletSetupScreen,
                onSubWalletsScreen,
            )

            transactionGraph(
                onAddTransactionScreen,
                onEditTransactionScreen,
                onImportTransactionsScreen,
                onAchievementsScreen,
            )

            profileGraph(
                onProfileScreen,
                onLibrariesScreen,
                onExportImportScreen,
            )
        }
    }
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
