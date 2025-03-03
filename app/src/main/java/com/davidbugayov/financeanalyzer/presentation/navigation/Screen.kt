package com.davidbugayov.financeanalyzer.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object AddTransaction : Screen("add")
    object Chart : Screen("chart")
} 