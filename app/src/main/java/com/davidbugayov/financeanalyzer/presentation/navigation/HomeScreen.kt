package com.davidbugayov.financeanalyzer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.davidbugayov.financeanalyzer.presentation.home.HomeScreen

/**
 * Обертка для HomeScreen, которая обеспечивает совместимость с обновленной навигацией
 */
@Composable
fun HomeScreenWrapper(navController: NavController) {
    HomeScreen(
        navController = navController
    )
} 
