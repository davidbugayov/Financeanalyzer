package com.davidbugayov.financeanalyzer.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.davidbugayov.financeanalyzer.navigation.NavRoutes
import com.davidbugayov.financeanalyzer.presentation.home.HomeScreen
import com.davidbugayov.financeanalyzer.presentation.onboarding.OnboardingScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionScreen
import com.davidbugayov.financeanalyzer.utils.OnboardingManager
import kotlinx.coroutines.delay
import org.koin.androidx.compose.get

/**
 * Главный экран приложения.
 * Отвечает за настройку навигационного хоста и управление отображением содержимого.
 */
@Composable
fun MainScreen(
    navController: NavHostController,
    onboardingManager: OnboardingManager = get()
) {
    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingManager.isOnboardingCompleted()) NavRoutes.HOME else NavRoutes.ONBOARDING,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Домашний экран
            composable(NavRoutes.HOME) {
                HomeScreen()
            }

            // Экран добавления транзакции
            composable(NavRoutes.ADD_TRANSACTION) {
                AddTransactionScreen()
            }

            // Экран редактирования транзакции
            composable(
                route = NavRoutes.EDIT_TRANSACTION,
                arguments = listOf(
                    navArgument(NavRoutes.Args.TRANSACTION_ID) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString(NavRoutes.Args.TRANSACTION_ID) ?: ""
                EditTransactionScreen(transactionId = transactionId)
            }

            // Экран онбординга
            composable(NavRoutes.ONBOARDING) {
                OnboardingScreen(
                    onComplete = {
                        // Небольшая задержка для плавности перехода
                        delay(300)
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
} 