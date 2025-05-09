package com.davidbugayov.financeanalyzer.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.davidbugayov.financeanalyzer.presentation.budget.BudgetScreen
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.WalletTransactionsScreen
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.EnhancedFinanceChartScreen
import com.davidbugayov.financeanalyzer.presentation.chart.statistics.FinancialStatisticsScreen
import com.davidbugayov.financeanalyzer.presentation.export_import.ExportImportScreen
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryScreen
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryViewModel
import com.davidbugayov.financeanalyzer.presentation.home.HomeScreen
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import com.davidbugayov.financeanalyzer.presentation.import_transaction.ImportTransactionsScreen
import com.davidbugayov.financeanalyzer.presentation.libraries.LibrariesScreen
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileScreen
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    homeViewModel: HomeViewModel,
    editTransactionViewModel: EditTransactionViewModel,
    profileViewModel: ProfileViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        mainGraph(navController, homeViewModel, editTransactionViewModel)
        transactionGraph(navController)
        profileGraph(navController, profileViewModel)
        statisticsGraph(navController)
    }
}

fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    editTransactionViewModel: EditTransactionViewModel
) {
    composable(
        route = Screen.Home.route,
        enterTransition = defaultEnterRight(),
        exitTransition = defaultExitLeft(),
        popEnterTransition = defaultEnterRight(),
        popExitTransition = defaultExitLeft()
    ) {
        HomeScreen(
            viewModel = homeViewModel,
            editTransactionViewModel = editTransactionViewModel,
            onNavigateToHistory = { navController.navigate(Screen.History.route) },
            onNavigateToAdd = { navController.navigate(Screen.AddTransaction.route) },
            onNavigateToChart = { navController.navigate(Screen.Chart.route) },
            onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
            onNavigateToEdit = { transactionId -> navController.navigate(Screen.EditTransaction.createRoute(transactionId)) }
        )
    }
    composable(
        route = Screen.History.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight()
    ) {
        TransactionHistoryScreen(
            viewModel = koinViewModel<TransactionHistoryViewModel>(),
            editTransactionViewModel = editTransactionViewModel,
            onNavigateBack = { navController.navigateUp() },
            navController = navController
        )
    }
    composable(
        route = Screen.Chart.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight()
    ) {
        EnhancedFinanceChartScreen(
            navController = navController,
            onNavigateBack = { navController.popBackStack() }
        )
    }
    composable(
        route = Screen.Budget.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight()
    ) {
        BudgetScreen(
            navController = navController,
            onNavigateBack = { navController.navigateUp() },
            onNavigateToTransactions = { walletId ->
                navController.navigate(Screen.WalletTransactions.createRoute(walletId))
            }
        )
    }
    composable(
        route = Screen.WalletTransactions.route,
        arguments = listOf(navArgument("walletId") { type = NavType.StringType }),
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight()
    ) { backStackEntry ->
        val walletId = backStackEntry.arguments?.getString("walletId") ?: return@composable
        WalletTransactionsScreen(
            walletId = walletId,
            onNavigateBack = { navController.navigateUp() },
            navController = navController
        )
    }
}

fun NavGraphBuilder.transactionGraph(navController: NavHostController) {
    composable(
        route = Screen.AddTransaction.route,
        enterTransition = defaultEnterUp(),
        exitTransition = defaultExitDown(),
        popEnterTransition = defaultEnterUp(),
        popExitTransition = defaultExitDown()
    ) {
        AddTransactionScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToImport = { navController.navigate(Screen.ImportTransactions.route) }
        )
    }
    composable(
        route = Screen.EditTransaction.route,
        arguments = listOf(navArgument("transactionId") { type = NavType.StringType }),
        enterTransition = defaultEnterUp(),
        exitTransition = defaultExitDown(),
        popEnterTransition = defaultEnterUp(),
        popExitTransition = defaultExitDown()
    ) { backStackEntry ->
        val transactionId = backStackEntry.arguments?.getString("transactionId")
        if (transactionId.isNullOrBlank()) {
            Timber.e("transactionId is null or blank, не открываем экран редактирования")
            return@composable
        }
        EditTransactionScreen(
            viewModel = koinViewModel<EditTransactionViewModel>(),
            onNavigateBack = { navController.navigateUp() },
            transactionId = transactionId
        )
    }
    composable(
        route = Screen.ImportTransactions.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight()
    ) {
        ImportTransactionsScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

fun NavGraphBuilder.profileGraph(
    navController: NavHostController,
    profileViewModel: ProfileViewModel
) {
    composable(
        route = Screen.Profile.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight()
    ) {
        ProfileScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToLibraries = { navController.navigate(Screen.Libraries.route) },
            onNavigateToChart = { navController.navigate(Screen.Chart.route) },
            onNavigateToBudget = { navController.navigate(Screen.Budget.route) },
            onNavigateToExportImport = { navController.navigate(Screen.ExportImport.route) }
        )
    }
    composable(
        route = Screen.Libraries.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight()
    ) {
        LibrariesScreen(
            onNavigateBack = { navController.navigateUp() }
        )
    }
    composable(
        route = Screen.ExportImport.route,
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight()
    ) {
        ExportImportScreen(
            onNavigateBack = { navController.popBackStack() },
            onImportClick = { navController.navigate(Screen.ImportTransactions.route) },
            viewModel = profileViewModel
        )
    }
}

fun NavGraphBuilder.statisticsGraph(navController: NavHostController) {
    composable(
        route = Screen.FinancialStatistics.route,
        arguments = listOf(
            navArgument("startDate") { type = NavType.LongType },
            navArgument("endDate") { type = NavType.LongType }
        ),
        enterTransition = defaultEnterLeft(),
        exitTransition = defaultExitRight(),
        popEnterTransition = defaultEnterLeft(),
        popExitTransition = defaultExitRight()
    ) { backStackEntry ->
        val startDate = backStackEntry.arguments?.getLong("startDate") ?: 0L
        val endDate = backStackEntry.arguments?.getLong("endDate") ?: 0L
        FinancialStatisticsScreen(
            startDate = startDate,
            endDate = endDate,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

// --- Транзишены для читаемости ---
private fun defaultEnterLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(400, easing = EaseInOut)
    ) + fadeIn(animationSpec = tween(400, easing = EaseInOut))
}

private fun defaultExitRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(400, easing = EaseInOut)
    ) + fadeOut(animationSpec = tween(400, easing = EaseInOut))
}

private fun defaultEnterRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(400, easing = EaseInOut)
    ) + fadeIn(animationSpec = tween(400, easing = EaseInOut))
}

private fun defaultExitLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(400, easing = EaseInOut)
    ) + fadeOut(animationSpec = tween(400, easing = EaseInOut))
}

private fun defaultEnterUp(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(300, easing = EaseInOut)
    ) + fadeIn(animationSpec = tween(300))
}

private fun defaultExitDown(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Down,
        animationSpec = tween(350, easing = EaseInOut)
    ) + fadeOut(animationSpec = tween(300))
} 