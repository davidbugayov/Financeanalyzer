package com.davidbugayov.financeanalyzer.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object History : Screen("history")
    data object AddTransaction : Screen("add")
    data object Chart : Screen("chart")
} 