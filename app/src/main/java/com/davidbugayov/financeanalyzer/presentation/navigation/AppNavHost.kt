package com.davidbugayov.financeanalyzer.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.davidbugayov.financeanalyzer.presentation.achievements.AchievementsScreen
import com.davidbugayov.financeanalyzer.presentation.budget.BudgetScreen
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.WalletTransactionsScreen
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.FinancialStatisticsScreen
import com.davidbugayov.financeanalyzer.presentation.export_import.ExportImportScreen
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryScreen
import com.davidbugayov.financeanalyzer.presentation.home.HomeScreen
import com.davidbugayov.financeanalyzer.presentation.import_transaction.ImportTransactionsScreen
import com.davidbugayov.financeanalyzer.presentation.libraries.LibrariesScreen
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    navigationManager: NavigationManager,
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
    ) {
        mainGraph(navigationManager)
        transactionGraph(navigationManager)
        profileGraph(navigationManager)
    }
}

private fun NavGraphBuilder.mainGraph(navigationManager: NavigationManager) {
    composable(
        route = Screen.Home.route,
        enterTransition = defaultEnterRight(),
        exitTransition = defaultExitLeft(),
        popEnterTransition = defaultEnterRight(),
        popExitTransition = defaultExitLeft(),
    ) {
        HomeScreen()
    }
    composable(
        route = Screen.History.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight(),
    ) {
        TransactionHistoryScreen()
    }
    composable(
        route = Screen.Budget.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight(),
    ) {
        BudgetScreen()
    }
    composable(
        route = Screen.FinancialStatistics.route,
        arguments = listOf(
            navArgument("startDate") {
                type = NavType.LongType
                defaultValue = -1L
            },
            navArgument("endDate") {
                type = NavType.LongType
                defaultValue = -1L
            },
        ),
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight(),
    ) {
        FinancialStatisticsScreen(onNavigateBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) })
    }
    composable(
        route = Screen.WalletTransactions.route,
        arguments = listOf(navArgument("walletId") { type = NavType.StringType }),
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight(),
    ) { backStackEntry ->
        val walletId = backStackEntry.arguments?.getString("walletId") ?: return@composable
        WalletTransactionsScreen(walletId = walletId)
    }
}

private fun NavGraphBuilder.transactionGraph(navigationManager: NavigationManager) {
    composable(
        route = Screen.AddTransaction.routeWithArgs,
        arguments = listOf(
            navArgument(Screen.AddTransaction.categoryArg) {
                type = NavType.StringType
                nullable = true
            },
        ),
        enterTransition = defaultEnterUp(),
        exitTransition = defaultExitDown(),
        popEnterTransition = defaultEnterUp(),
        popExitTransition = defaultExitDown(),
    ) { backStackEntry ->
        val category = backStackEntry.arguments?.getString(Screen.AddTransaction.categoryArg)
        AddTransactionScreen(category = category)
    }
    composable(
        route = Screen.EditTransaction.route,
        arguments = listOf(navArgument("transactionId") { type = NavType.StringType }),
        enterTransition = defaultEnterUp(),
        exitTransition = defaultExitDown(),
        popEnterTransition = defaultEnterUp(),
        popExitTransition = defaultExitDown(),
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getString("transactionId") ?: "0"
        EditTransactionScreen(
            transactionId = id,
        )
    }
    composable(
        route = Screen.ImportTransactions.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight(),
    ) {
        ImportTransactionsScreen(
            onNavigateBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) },
            viewModel = koinViewModel(),
        )
    }
    composable(
        route = Screen.Achievements.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight(),
    ) {
        val achievementsViewModel =
            koinViewModel<com.davidbugayov.financeanalyzer.presentation.achievements.AchievementsViewModel>()
        val achievements = achievementsViewModel.achievements.collectAsState().value
        AchievementsScreen(
            achievements = achievements,
            onBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) },
        )
    }
}

private fun NavGraphBuilder.profileGraph(navigationManager: NavigationManager) {
    composable(
        route = Screen.Profile.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight(),
    ) {
        ProfileScreen()
    }
    composable(
        route = Screen.Libraries.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight(),
    ) {
        LibrariesScreen(
            onNavigateBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) },
        )
    }
    composable(
        route = Screen.ExportImport.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight(),
    ) {
        ExportImportScreen(
            onNavigateBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) },
            onImportClick = {
                navigationManager.navigate(
                    NavigationManager.Command.Navigate(Screen.ImportTransactions.route),
                )
            },
            viewModel = koinViewModel(),
        )
    }
}

// --- Транзишены для читаемости ---
private fun defaultEnterLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(400, easing = EaseInOut),
    ) + fadeIn(animationSpec = tween(400, easing = EaseInOut))
}

private fun defaultExitRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(400, easing = EaseInOut),
    ) + fadeOut(animationSpec = tween(400, easing = EaseInOut))
}

private fun defaultEnterRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(400, easing = EaseInOut),
    ) + fadeIn(animationSpec = tween(400, easing = EaseInOut))
}

private fun defaultExitLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(400, easing = EaseInOut),
    ) + fadeOut(animationSpec = tween(400, easing = EaseInOut))
}

private fun defaultEnterUp(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(300, easing = EaseInOut),
    ) + fadeIn(animationSpec = tween(300))
}

private fun defaultExitDown(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Down,
        animationSpec = tween(350, easing = EaseInOut),
    ) + fadeOut(animationSpec = tween(300))
}
