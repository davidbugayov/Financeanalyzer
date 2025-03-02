package com.davidbugayov.financeanalyzer.presentation

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
fun MainScreen() {
    val navController = rememberNavController()
    val layoutDirection = LocalLayoutDirection.current

    FinanceAnalyzerTheme {
        Scaffold(
            modifier = Modifier.systemBarsPadding()
        ) { paddingValues ->
            NavHost(
                navController = navController, 
                startDestination = "home",
                modifier = Modifier.padding(
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                    end = paddingValues.calculateRightPadding(layoutDirection)
                )
            ) {
                composable("home") {
                    val homeViewModel: HomeViewModel = koinViewModel()
                    
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToChart = { navController.navigate("chart") },
                        onNavigateToAddTransaction = { navController.navigate("add") },
                        onNavigateToHistory = { navController.navigate("history") }
                    )
                }
                
                composable("chart") {
                    val chartViewModel: ChartViewModel = koinViewModel()
                    
                    FinanceChartScreen(
                        viewModel = chartViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable("add") {
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
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable("history") {
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