package com.davidbugayov.financeanalyzer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

/**
 * Компонент навигации приложения.
 * Инкапсулирует логику навигации и предоставляет централизованный доступ к функциям навигации.
 */
class AppNavigation(
    private val navigationManager: NavigationManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    /**
     * Навигация на экран добавления транзакции
     */
    fun navigateToAddTransaction() {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.AddTransaction)
        }
    }

    /**
     * Навигация на экран редактирования транзакции
     * @param transactionId ID транзакции для редактирования
     */
    fun navigateToEditTransaction(transactionId: String) {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.EditTransaction(transactionId))
        }
    }

    /**
     * Навигация на экран истории транзакций
     */
    fun navigateToTransactionHistory() {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.TransactionHistory)
        }
    }

    /**
     * Навигация на экран аналитики
     */
    fun navigateToAnalytics() {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.Analytics)
        }
    }

    /**
     * Навигация на экран бюджета
     */
    fun navigateToBudget() {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.Budget)
        }
    }

    /**
     * Навигация на экран настроек
     */
    fun navigateToSettings() {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.Settings)
        }
    }

    /**
     * Навигация на экран импорта транзакций
     */
    fun navigateToImportTransactions() {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.ImportTransactions)
        }
    }

    /**
     * Навигация на экран кошельков
     */
    fun navigateToWallets() {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.Wallets)
        }
    }

    /**
     * Навигация на экран категорий
     */
    fun navigateToCategories() {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.Categories)
        }
    }

    /**
     * Навигация на главный экран
     */
    fun navigateToHome() {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.Home)
        }
    }

    /**
     * Навигация на экран профиля
     */
    fun navigateToProfile() {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.Profile)
        }
    }

    /**
     * Навигация на экран достижений
     */
    fun navigateToAchievements() {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.Achievements)
        }
    }

    /**
     * Навигация на экран онбординга
     */
    fun navigateToOnboarding() {
        coroutineScope.launch {
            navigationManager.navigateTo(NavigationAction.Onboarding)
        }
    }

    /**
     * Навигация назад
     */
    fun navigateBack() {
        coroutineScope.launch {
            navigationManager.navigate(NavigationCommand.NavigateBack)
        }
    }

    /**
     * Навигация в корневой экран
     */
    fun navigateToRoot() {
        coroutineScope.launch {
            navigationManager.navigate(NavigationCommand.NavigateToRoot)
        }
    }
}

/**
 * Composable-функция для настройки навигации в приложении.
 * Подписывается на команды навигации и обрабатывает их.
 *
 * @param navController Контроллер навигации
 * @param navigationManager Менеджер навигации
 */
@Composable
fun SetupNavigation(
    navController: NavHostController = rememberNavController(),
    navigationManager: NavigationManager = get()
) {
    // Прослушиваем команды навигации от NavigationManager
    val navCommands by navigationManager.navigationCommands.collectAsState(initial = null)

    // Обрабатываем команды навигации
    LaunchedEffect(navCommands) {
        navCommands?.let { command ->
            navController.handleNavigationCommand(command)
        }
    }
}
