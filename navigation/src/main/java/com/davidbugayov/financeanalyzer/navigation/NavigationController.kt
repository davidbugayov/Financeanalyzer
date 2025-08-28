package com.davidbugayov.financeanalyzer.navigation

import androidx.navigation.NavController
import com.davidbugayov.financeanalyzer.navigation.model.Screen
import timber.log.Timber

/**
 * Type-safe navigation controller that provides methods for navigating to screens
 * with compile-time safety and better error handling.
 */
class NavigationController(
    private val navController: NavController
) {

    /**
     * Navigate to a screen
     */
    fun navigateTo(screen: Screen) {
        try {
            navController.navigate(screen.route) {
                // Configure navigation options
                launchSingleTop = true
                restoreState = true
            }
            Timber.d("Navigated to: ${screen.route}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to navigate to: ${screen.route}")
        }
    }

    /**
     * Navigate to a screen with popup to specific destination
     */
    fun navigateTo(screen: Screen, popupTo: Screen, inclusive: Boolean = false) {
        try {
            navController.navigate(screen.route) {
                popUpTo(popupTo.route) {
                    this.inclusive = inclusive
                }
                launchSingleTop = true
                restoreState = true
            }
            Timber.d("Navigated to: ${screen.route} with popup to: ${popupTo.route}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to navigate to: ${screen.route}")
        }
    }

    /**
     * Navigate back
     */
    fun navigateBack() {
        try {
            navController.popBackStack()
            Timber.d("Navigated back")
        } catch (e: Exception) {
            Timber.e(e, "Failed to navigate back")
        }
    }

    /**
     * Navigate back to specific screen
     */
    fun navigateBackTo(screen: Screen, inclusive: Boolean = false) {
        try {
            navController.popBackStack(screen.route, inclusive)
            Timber.d("Navigated back to: ${screen.route}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to navigate back to: ${screen.route}")
        }
    }

    /**
     * Check if can navigate back
     */
    fun canNavigateBack(): Boolean {
        return navController.previousBackStackEntry != null
    }

    /**
     * Get current screen
     */
    fun getCurrentScreen(): Screen? {
        val currentRoute = navController.currentDestination?.route
        return Screen.fromRoute(currentRoute)
    }

    /**
     * Check if screen is in back stack
     */
    fun isScreenInBackStack(screen: Screen): Boolean {
        return try {
            navController.getBackStackEntry(screen.route)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Convenience methods for common navigation patterns

    fun navigateToHome() = navigateTo(Screen.Home)

    fun navigateToStatistics() = navigateTo(Screen.Statistics)

    fun navigateToProfile() = navigateTo(Screen.Profile)

    fun navigateToSettings() = navigateTo(Screen.Settings)

    fun navigateToAddTransaction() = navigateTo(Screen.AddTransaction)

    fun navigateToEditTransaction(transactionId: Long) =
        navigateTo(Screen.EditTransaction(transactionId))

    fun navigateToTransactionDetails(transactionId: Long) =
        navigateTo(Screen.TransactionDetails(transactionId))

    fun navigateToCategories() = navigateTo(Screen.Categories)

    fun navigateToWallets() = navigateTo(Screen.Wallets)

    fun navigateToTransactionHistory() = navigateTo(Screen.TransactionHistory)

    fun navigateToAchievements() = navigateTo(Screen.Achievements)

    fun navigateToBudget() = navigateTo(Screen.Budget)

    fun navigateToSearch() = navigateTo(Screen.Search)

    fun navigateToWelcome() = navigateTo(Screen.Welcome)

    fun navigateToAbout() = navigateTo(Screen.About)

    // Navigation with completion callbacks

    fun navigateToAndComplete(
        screen: Screen,
        onComplete: () -> Unit
    ) {
        navigateTo(screen)
        onComplete()
    }

    fun navigateBackAndComplete(onComplete: () -> Unit) {
        navigateBack()
        onComplete()
    }

    // Conditional navigation

    fun navigateToIfNotCurrent(screen: Screen) {
        if (getCurrentScreen() != screen) {
            navigateTo(screen)
        }
    }

    fun navigateToIf(condition: Boolean, screen: Screen) {
        if (condition) {
            navigateTo(screen)
        }
    }

    // Error handling navigation

    fun navigateToErrorScreen(errorMessage: String) {
        // Could navigate to a generic error screen
        // For now, just log the error
        Timber.e("Navigation error: $errorMessage")
    }

    fun navigateToLoginIfNeeded() {
        // Check if user is authenticated and navigate to login if needed
        // This would integrate with your authentication system
    }
}

/**
 * Extension function to create NavigationController from NavController
 */
fun NavController.toNavigationController(): NavigationController {
    return NavigationController(this)
}

/**
 * DSL for navigation actions
 */
class NavigationActions(private val controller: NavigationController) {

    fun home() = controller.navigateToHome()
    fun statistics() = controller.navigateToStatistics()
    fun profile() = controller.navigateToProfile()
    fun settings() = controller.navigateToSettings()

    fun addTransaction() = controller.navigateToAddTransaction()
    fun editTransaction(id: Long) = controller.navigateToEditTransaction(id)
    fun transactionDetails(id: Long) = controller.navigateToTransactionDetails(id)

    fun categories() = controller.navigateToCategories()
    fun wallets() = controller.navigateToWallets()
    fun history() = controller.navigateToTransactionHistory()
    fun achievements() = controller.navigateToAchievements()
    fun budget() = controller.navigateToBudget()

    fun back() = controller.navigateBack()
    fun backTo(screen: Screen, inclusive: Boolean = false) = controller.navigateBackTo(screen, inclusive)

    fun canGoBack() = controller.canNavigateBack()
    fun currentScreen() = controller.getCurrentScreen()
}

/**
 * Create navigation actions DSL
 */
fun NavigationController.actions(): NavigationActions {
    return NavigationActions(this)
}

/**
 * Navigation result handling
 */
sealed class NavigationResult {
    object Success : NavigationResult()
    data class Error(val message: String) : NavigationResult()
    object Cancelled : NavigationResult()
}

/**
 * Navigation middleware for logging and analytics
 */
class NavigationMiddleware(
    private val analyticsTracker: com.davidbugayov.financeanalyzer.core.middleware.AnalyticsMiddleware,
    private val logger: com.davidbugayov.financeanalyzer.core.middleware.LoggerMiddleware
) {

    fun onNavigateTo(screen: Screen, fromScreen: Screen?) {
        logger.i("Navigation: ${fromScreen?.route ?: "unknown"} -> ${screen.route}")
        analyticsTracker.trackScreenView(
            screenName = Screen.getDisplayName(screen),
            screenClass = screen::class.simpleName
        )
    }

    fun onNavigateBack(fromScreen: Screen?, toScreen: Screen?) {
        logger.i("Navigation back: ${fromScreen?.route ?: "unknown"} <- ${toScreen?.route ?: "unknown"}")
    }

    fun onNavigationError(error: Exception, attemptedScreen: Screen?) {
        logger.logError(error, "Navigation", mapOf(
            "attempted_screen" to (attemptedScreen?.route ?: "unknown")
        ))
    }
}
