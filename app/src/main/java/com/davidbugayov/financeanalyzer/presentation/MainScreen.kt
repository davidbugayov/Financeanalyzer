package com.davidbugayov.financeanalyzer.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davidbugayov.financeanalyzer.presentation.add.AddTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.FinanceChartScreen
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryScreen
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryViewModel
import com.davidbugayov.financeanalyzer.presentation.home.HomeScreen
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import com.davidbugayov.financeanalyzer.presentation.navigation.Screen
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(startDestination: String = "home") {
    val navController = rememberNavController()
    val layoutDirection = LocalLayoutDirection.current
    val homeViewModel: HomeViewModel = koinViewModel()
    val chartViewModel: ChartViewModel = koinViewModel()
    val addTransactionViewModel: AddTransactionViewModel = koinViewModel()

    FinanceAnalyzerTheme {
        Scaffold(
            modifier = Modifier.systemBarsPadding()
        ) { paddingValues ->
            NavHost(
                navController = navController, 
                startDestination = startDestination,
                modifier = Modifier.padding(
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                    end = paddingValues.calculateRightPadding(layoutDirection)
                )
            ) {
                composable(
                    route = Screen.Home.route,
                    enterTransition = {
                        fadeIn(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    },
                    exitTransition = {
                        fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    }
                ) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToHistory = { navController.navigate(Screen.History.route) },
                        onNavigateToAdd = { navController.navigate(Screen.AddTransaction.route) },
                        onNavigateToChart = { navController.navigate(Screen.Chart.route) }
                    )
                }
                
                composable(
                    route = Screen.History.route,
                    enterTransition = {
                        fadeIn(
                            animationSpec = tween(300, easing = EaseInOut)
                        ) + slideIntoContainer(
                            animationSpec = tween(300, easing = EaseOut),
                            towards = AnimatedContentTransitionScope.SlideDirection.Start
                        )
                    },
                    exitTransition = {
                        fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        ) + slideOutOfContainer(
                            animationSpec = tween(300, easing = EaseOut),
                            towards = AnimatedContentTransitionScope.SlideDirection.End
                        )
                    }
                ) {
                    TransactionHistoryScreen(
                        viewModel = koinViewModel<TransactionHistoryViewModel>(),
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
                
                composable(
                    route = Screen.AddTransaction.route,
                    enterTransition = {
                        fadeIn(
                            animationSpec = tween(300, easing = EaseInOut)
                        ) + slideIntoContainer(
                            animationSpec = tween(300, easing = EaseOut),
                            towards = AnimatedContentTransitionScope.SlideDirection.Up
                        )
                    },
                    exitTransition = {
                        fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        ) + slideOutOfContainer(
                            animationSpec = tween(300, easing = EaseOut),
                            towards = AnimatedContentTransitionScope.SlideDirection.Down
                        )
                    }
                ) {
                    AddTransactionScreen(
                        viewModel = addTransactionViewModel,
                        onNavigateBack = {
                            homeViewModel.onEvent(com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent.LoadTransactions)
                            chartViewModel.loadTransactions()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
                
                composable(
                    route = Screen.Chart.route,
                    enterTransition = {
                        fadeIn(
                            animationSpec = tween(300, easing = EaseInOut)
                        ) + slideIntoContainer(
                            animationSpec = tween(300, easing = EaseOut),
                            towards = AnimatedContentTransitionScope.SlideDirection.Start
                        )
                    },
                    exitTransition = {
                        fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        ) + slideOutOfContainer(
                            animationSpec = tween(300, easing = EaseOut),
                            towards = AnimatedContentTransitionScope.SlideDirection.End
                        )
                    }
                ) {
                    FinanceChartScreen(
                        viewModel = chartViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}