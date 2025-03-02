package com.davidbugayov.financeanalyzer.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import com.davidbugayov.financeanalyzer.presentation.home.HomeScreen
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(startDestination: String = "home") {
    val navController = rememberNavController()
    val layoutDirection = LocalLayoutDirection.current

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
                    route = "home",
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
                            towards = AnimatedContentTransitionScope.SlideDirection.Start
                        )
                    },
                    popEnterTransition = {
                        fadeIn(
                            animationSpec = tween(300, easing = EaseInOut)
                        ) + slideIntoContainer(
                            animationSpec = tween(300, easing = EaseOut),
                            towards = AnimatedContentTransitionScope.SlideDirection.End
                        )
                    },
                    popExitTransition = {
                        fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        ) + slideOutOfContainer(
                            animationSpec = tween(300, easing = EaseOut),
                            towards = AnimatedContentTransitionScope.SlideDirection.End
                        )
                    }
                ) {
                    val homeViewModel: HomeViewModel = koinViewModel()
                    
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToChart = { navController.navigate("chart") },
                        onNavigateToAddTransaction = { navController.navigate("add") },
                        onNavigateToHistory = { navController.navigate("history") }
                    )
                }
                
                composable(
                    route = "chart",
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
                    val chartViewModel: ChartViewModel = koinViewModel()
                    
                    FinanceChartScreen(
                        viewModel = chartViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(
                    route = "add",
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
                    val addViewModel: AddTransactionViewModel = koinViewModel()
                    
                    AddTransactionScreen(
                        viewModel = addViewModel,
                        onTransactionAdded = {
                            navController.navigate("home") {
                                popUpTo("home") {
                                    inclusive = true
                                }
                            }
                        },
                        onNavigateBack = { 
                            if (navController.previousBackStackEntry == null) {
                                navController.navigate("home") {
                                    popUpTo("add") {
                                        inclusive = true
                                    }
                                }
                            } else {
                                navController.popBackStack()
                            }
                        }
                    )
                }
                
                composable(
                    route = "history",
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
                    val chartViewModel: ChartViewModel = koinViewModel()
                    
                    TransactionHistoryScreen(
                        viewModel = chartViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}