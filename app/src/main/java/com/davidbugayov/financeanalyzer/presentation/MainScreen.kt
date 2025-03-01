package com.davidbugayov.financeanalyzer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(viewModel: SharedViewModel = koinViewModel()) {
    val navController = rememberNavController()

    // Загружаем данные при запуске
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
    }

    FinanceAnalyzerTheme {
        NavHost(navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    onNavigateToChart = { navController.navigate("chart") },
                    onNavigateToAddTransaction = { navController.navigate("add") }
                )
            }
            composable("chart") {
                FinanceChartScreen(viewModel = viewModel)
            }
            composable("add") {
                AddTransactionScreen(viewModel = viewModel) {
                    navController.popBackStack()
                }
            }
        }
    }
}