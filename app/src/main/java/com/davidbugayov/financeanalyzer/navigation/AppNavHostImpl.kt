package com.davidbugayov.financeanalyzer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import com.davidbugayov.financeanalyzer.feature.onboarding.OnboardingScreen
import com.davidbugayov.financeanalyzer.feature.profile.ProfileScreen
import com.davidbugayov.financeanalyzer.feature.security.AuthScreen
import com.davidbugayov.financeanalyzer.feature.transaction.edit.EditTransactionScreen
import com.davidbugayov.financeanalyzer.feature.transaction.presentation.export.ExportImportScreen
import com.davidbugayov.financeanalyzer.feature.transaction.presentation.transaction.add.AddTransactionScreen
import com.davidbugayov.financeanalyzer.navigation.model.PeriodType
import com.davidbugayov.financeanalyzer.presentation.achievements.AchievementsScreen
import com.davidbugayov.financeanalyzer.presentation.achievements.AchievementsViewModel
import com.davidbugayov.financeanalyzer.presentation.budget.BudgetScreen
import com.davidbugayov.financeanalyzer.presentation.budget.subwallets.SubWalletsScreen
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.WalletTransactionsScreen
import com.davidbugayov.financeanalyzer.presentation.chart.detail.FinancialDetailStatisticsScreen
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.FinancialStatisticsScreen as EnhancedFinancialStatisticsScreen
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryScreen
import com.davidbugayov.financeanalyzer.presentation.home.HomeScreen
import com.davidbugayov.financeanalyzer.presentation.import_transaction.ImportTransactionsScreen
import com.davidbugayov.financeanalyzer.presentation.onboarding.OnboardingViewModel
import java.util.Date
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Реализация AppNavHost для приложения.
 * Связывает навигационную структуру из модуля navigation с конкретными экранами приложения.
 *
 * @param navController Контроллер навигации
 * @param navigationManager Менеджер навигации
 * @param startDestination Начальный экран
 */
@Composable
fun AppNavHostImpl(
    navController: NavHostController,
    navigationManager: NavigationManager,
    startDestination: String = Screen.Home.route,
) {
    val appNavigation = AppNavigation()

    AppNavHost(
        navController = navController,
        navigationManager = navigationManager,
        appNavigation = appNavigation,
        onOnboardingScreen = {
            val onboardingViewModel: OnboardingViewModel = koinViewModel()
            OnboardingScreen(
                onFinish = {
                    onboardingViewModel.completeOnboarding()
                    // Переходим на главный экран и очищаем back stack от онбординга
                    navigationManager.navigate(
                        NavigationManager.Command.NavigateAndClearBackStack(
                            destination = Screen.Home.route,
                            popUpTo = Screen.Onboarding.route,
                        ),
                    )
                },
            )
        },
        onAuthScreen = {
            AuthScreen(
                onAuthSuccess = {
                    // После успешной аутентификации переходим на главный экран
                    navigationManager.navigate(
                        NavigationManager.Command.NavigateAndClearBackStack(
                            destination = Screen.Home.route,
                            popUpTo = Screen.Auth.route,
                        ),
                    )
                }
            )
        },
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
            val periodType =
                periodTypeStr?.let {
                    try {
                        PeriodType.valueOf(it)
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
        onWalletSetupScreen = {
            com.davidbugayov.financeanalyzer.presentation.budget.setup.WalletSetupScreen()
        },
        onSubWalletsScreen = { parentWalletId ->
            SubWalletsScreen(
                parentWalletId = parentWalletId,
                onNavigateBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) },
                onNavigateToWalletSetup = {
                    navigationManager.navigate(NavigationManager.Command.Navigate(Screen.WalletSetup.route))
                },
            )
        },
        onAddTransactionScreen = { category, forceExpense ->
            AddTransactionScreen(category = category, forceExpense = forceExpense)
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
            @OptIn(ExperimentalCoroutinesApi::class)
            val achievementsViewModel =
                koinViewModel<AchievementsViewModel>()
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
            com.davidbugayov.financeanalyzer.feature.profile.libraries.LibrariesScreen(
                onNavigateBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) },
            )
        },
        onExportImportScreen = {
            ExportImportScreen(
                onNavigateBack = { navigationManager.navigate(NavigationManager.Command.NavigateUp) },
                onImportClick = {
                    navigationManager.navigate(
                        NavigationManager.Command.Navigate(
                            Screen.ImportTransactions.route,
                        ),
                    )
                },
                viewModel = koinViewModel(),
            )
        },
        startDestination = startDestination,
    )
}
