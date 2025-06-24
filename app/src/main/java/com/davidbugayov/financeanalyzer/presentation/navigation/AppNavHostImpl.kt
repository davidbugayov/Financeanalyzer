package com.davidbugayov.financeanalyzer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import com.davidbugayov.financeanalyzer.feature.export_import.ExportImportScreen
import com.davidbugayov.financeanalyzer.navigation.AppNavHost
import com.davidbugayov.financeanalyzer.navigation.AppNavigation
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.presentation.achievements.AchievementsScreen
import com.davidbugayov.financeanalyzer.presentation.budget.BudgetScreen
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.WalletTransactionsScreen
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.FinancialStatisticsScreen as EnhancedFinancialStatisticsScreen
import com.davidbugayov.financeanalyzer.presentation.chart.detail.FinancialDetailStatisticsScreen
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryScreen
import com.davidbugayov.financeanalyzer.presentation.home.HomeScreen
import com.davidbugayov.financeanalyzer.presentation.import_transaction.ImportTransactionsScreen
import com.davidbugayov.financeanalyzer.presentation.libraries.LibrariesScreen
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionScreen
import java.util.Date
import org.koin.androidx.compose.koinViewModel

/**
 * Реализация AppNavHost для приложения.
 * Связывает навигационную структуру из модуля navigation с конкретными экранами приложения.
 *
 * @param navController Контроллер навигации
 * @param navigationManager Менеджер навигации
 */
@Composable
fun AppNavHostImpl(
    navController: NavHostController,
    navigationManager: NavigationManager,
) {
    val appNavigation = AppNavigation()

    AppNavHost(
        navController = navController,
        navigationManager = navigationManager,
        appNavigation = appNavigation,
        onHomeScreen = {
            HomeScreen()
        },
        onHistoryScreen = {
            TransactionHistoryScreen()
        },
        onBudgetScreen = {
            BudgetScreen()
        },
        onFinancialStatisticsScreen = { startDate, endDate, periodTypeStr ->
            // Преобразуем строковое представление PeriodType в enum
            val periodType = periodTypeStr?.let {
                try {
                    com.davidbugayov.financeanalyzer.navigation.model.PeriodType.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }

            if (periodTypeStr == "DETAILED") {
                // Используем экран подробной статистики
                FinancialDetailStatisticsScreen(
                    startDate = startDate,
                    endDate = endDate,
                    onNavigateBack = {
                        // Используем PopUpTo вместо NavigateUp для корректного возврата
                        navigationManager.navigate(NavigationManager.Command.NavigateUp)
                    },
                )
            } else {
                // Используем улучшенный экран статистики
                EnhancedFinancialStatisticsScreen(
                    startDate = Date(startDate),
                    endDate = Date(endDate),
                    periodType = periodType,
                    onNavigateBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) },
                )
            }
        },
        onWalletTransactionsScreen = { walletId ->
            WalletTransactionsScreen(walletId = walletId)
        },
        onAddTransactionScreen = { category ->
            AddTransactionScreen(category = category)
        },
        onEditTransactionScreen = { transactionId ->
            EditTransactionScreen(transactionId = transactionId)
        },
        onImportTransactionsScreen = {
            ImportTransactionsScreen(
                onNavigateBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) },
                viewModel = koinViewModel(),
            )
        },
        onAchievementsScreen = {
            val achievementsViewModel =
                koinViewModel<com.davidbugayov.financeanalyzer.presentation.achievements.AchievementsViewModel>()
            val achievements = achievementsViewModel.achievements.collectAsState().value
            AchievementsScreen(
                achievements = achievements,
                onBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) },
            )
        },
        onProfileScreen = {
            ProfileScreen()
        },
        onLibrariesScreen = {
            LibrariesScreen(
                onNavigateBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) },
            )
        },
        onExportImportScreen = {
            ExportImportScreen(
                onNavigateBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) },
                onImportClick = {
                    navigationManager.navigate(
                        NavigationManager.Command.Navigate(
                            com.davidbugayov.financeanalyzer.navigation.Screen.ImportTransactions.route,
                        ),
                    )
                },
                viewModel = koinViewModel(),
            )
        },
    )
}
