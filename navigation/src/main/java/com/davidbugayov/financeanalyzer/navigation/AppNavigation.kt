package com.davidbugayov.financeanalyzer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * Класс для определения навигационных графов приложения.
 * Содержит методы для создания различных графов навигации.
 */
class AppNavigation {

    /**
     * Создает основной граф навигации
     * @param onHomeScreen Функция для отображения экрана Home
     * @param onHistoryScreen Функция для отображения экрана History
     * @param onBudgetScreen Функция для отображения экрана Budget
     * @param onFinancialStatisticsScreen Функция для отображения экрана FinancialStatistics
     * @param onWalletTransactionsScreen Функция для отображения экрана WalletTransactions
     */
    fun NavGraphBuilder.mainGraph(
        onHomeScreen: @Composable () -> Unit,
        onHistoryScreen: @Composable () -> Unit,
        onBudgetScreen: @Composable () -> Unit,
        onFinancialStatisticsScreen: @Composable (
            startDate: Long,
            endDate: Long,
            periodType: String?,
        ) -> Unit,
        onWalletTransactionsScreen: @Composable (walletId: String) -> Unit,
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = defaultEnterRight(),
            exitTransition = defaultExitLeft(),
            popEnterTransition = defaultEnterRight(),
            popExitTransition = defaultExitLeft(),
        ) {
            onHomeScreen()
        }
        composable(
            route = Screen.History.route,
            enterTransition = defaultEnterLeft(),
            exitTransition = defaultExitRight(),
            popEnterTransition = defaultEnterLeft(),
            popExitTransition = defaultExitRight(),
        ) {
            onHistoryScreen()
        }
        composable(
            route = Screen.Budget.route,
            enterTransition = defaultEnterLeft(),
            exitTransition = defaultExitRight(),
            popEnterTransition = defaultEnterLeft(),
            popExitTransition = defaultExitRight(),
        ) {
            onBudgetScreen()
        }
        composable(
            route = Screen.FinancialStatistics.route,
            arguments = listOf(
                navArgument(Screen.START_DATE_ARG) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument(Screen.END_DATE_ARG) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument(Screen.PERIOD_TYPE_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
            enterTransition = defaultEnterLeft(),
            exitTransition = defaultExitRight(),
            popEnterTransition = defaultEnterLeft(),
            popExitTransition = defaultExitRight(),
        ) { backStackEntry ->
            val startDate = backStackEntry.arguments?.getLong(Screen.START_DATE_ARG) ?: -1L
            val endDate = backStackEntry.arguments?.getLong(Screen.END_DATE_ARG) ?: -1L
            val periodTypeStr = backStackEntry.arguments?.getString(Screen.PERIOD_TYPE_ARG)

            // Если даты не указаны или указаны дефолтные значения, используем текущее время
            val finalStartDate = if (startDate == -1L) System.currentTimeMillis() else startDate
            val finalEndDate = if (endDate == -1L) System.currentTimeMillis() else endDate

            onFinancialStatisticsScreen(finalStartDate, finalEndDate, periodTypeStr)
        }
        composable(
            route = Screen.WalletTransactions.route,
            arguments = listOf(navArgument(Screen.WALLET_ID_ARG) { type = NavType.StringType }),
            enterTransition = defaultEnterLeft(),
            exitTransition = defaultExitRight(),
            popEnterTransition = defaultEnterLeft(),
            popExitTransition = defaultExitRight(),
        ) { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString(Screen.WALLET_ID_ARG) ?: return@composable
            onWalletTransactionsScreen(walletId)
        }
    }

    /**
     * Создает граф навигации для транзакций
     * @param onAddTransactionScreen Функция для отображения экрана AddTransaction
     * @param onEditTransactionScreen Функция для отображения экрана EditTransaction
     * @param onImportTransactionsScreen Функция для отображения экрана ImportTransactions
     * @param onAchievementsScreen Функция для отображения экрана Achievements
     */
    fun NavGraphBuilder.transactionGraph(
        onAddTransactionScreen: @Composable (category: String?) -> Unit,
        onEditTransactionScreen: @Composable (transactionId: String) -> Unit,
        onImportTransactionsScreen: @Composable () -> Unit,
        onAchievementsScreen: @Composable () -> Unit,
    ) {
        composable(
            route = Screen.AddTransaction.routeWithArgs,
            arguments = listOf(
                navArgument(Screen.AddTransaction.CATEGORY_ARG) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            enterTransition = defaultEnterUp(),
            exitTransition = defaultExitDown(),
            popEnterTransition = defaultEnterUp(),
            popExitTransition = defaultExitDown(),
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString(Screen.AddTransaction.CATEGORY_ARG)
            onAddTransactionScreen(category)
        }
        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(navArgument(Screen.TRANSACTION_ID_ARG) { type = NavType.StringType }),
            enterTransition = defaultEnterUp(),
            exitTransition = defaultExitDown(),
            popEnterTransition = defaultEnterUp(),
            popExitTransition = defaultExitDown(),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Screen.TRANSACTION_ID_ARG) ?: "0"
            onEditTransactionScreen(id)
        }
        composable(
            route = Screen.ImportTransactions.route,
            enterTransition = defaultEnterLeft(),
            exitTransition = defaultExitRight(),
            popEnterTransition = defaultEnterLeft(),
            popExitTransition = defaultExitRight(),
        ) {
            onImportTransactionsScreen()
        }
        composable(
            route = Screen.Achievements.route,
            enterTransition = defaultEnterLeft(),
            exitTransition = defaultExitRight(),
            popEnterTransition = defaultEnterLeft(),
            popExitTransition = defaultExitRight(),
        ) {
            onAchievementsScreen()
        }
    }

    /**
     * Создает граф навигации для профиля
     * @param onProfileScreen Функция для отображения экрана Profile
     * @param onLibrariesScreen Функция для отображения экрана Libraries
     * @param onExportImportScreen Функция для отображения экрана ExportImport
     */
    fun NavGraphBuilder.profileGraph(
        onProfileScreen: @Composable () -> Unit,
        onLibrariesScreen: @Composable () -> Unit,
        onExportImportScreen: @Composable () -> Unit,
    ) {
        composable(
            route = Screen.Profile.route,
            enterTransition = defaultEnterLeft(),
            exitTransition = defaultExitRight(),
            popEnterTransition = defaultEnterLeft(),
            popExitTransition = defaultExitRight(),
        ) {
            onProfileScreen()
        }
        composable(
            route = Screen.Libraries.route,
            enterTransition = defaultEnterLeft(),
            exitTransition = defaultExitRight(),
            popEnterTransition = defaultEnterLeft(),
            popExitTransition = defaultExitRight(),
        ) {
            onLibrariesScreen()
        }
        composable(
            route = Screen.ExportImport.route,
            enterTransition = defaultEnterLeft(),
            exitTransition = defaultExitRight(),
            popEnterTransition = defaultEnterLeft(),
            popExitTransition = defaultExitRight(),
        ) {
            onExportImportScreen()
        }
    }
}
