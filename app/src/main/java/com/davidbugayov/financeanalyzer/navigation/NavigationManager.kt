package com.davidbugayov.financeanalyzer.navigation

import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Менеджер навигации для централизации всех операций навигации в приложении.
 * Использует типизированные NavigationAction вместо строковых маршрутов.
 */
class NavigationManager {
    private val _navigationCommands = MutableSharedFlow<NavigationCommand>()
    val navigationCommands: SharedFlow<NavigationCommand> = _navigationCommands

    /**
     * Навигация по типизированному действию
     * @param command Команда навигации
     */
    suspend fun navigate(command: NavigationCommand) {
        _navigationCommands.emit(command)
    }
}

/**
 * Базовый интерфейс для всех команд навигации
 */
sealed interface NavigationCommand {
    /**
     * Команда перехода по маршруту
     * @param route Маршрут назначения
     * @param popUpToRoute Маршрут, до которого нужно очистить стек (или null)
     * @param inclusive Включать ли popUpToRoute в очистку стека
     * @param isSingleTop Не добавлять маршрут в стек, если он уже находится наверху
     */
    data class NavigateTo(
        val route: String,
        val popUpToRoute: String? = null,
        val inclusive: Boolean = false,
        val isSingleTop: Boolean = false
    ) : NavigationCommand

    /**
     * Команда возврата назад
     */
    object NavigateBack : NavigationCommand
    
    /**
     * Команда возврата в корневой экран
     */
    object NavigateToRoot : NavigationCommand
}

/**
 * Типизированные действия навигации
 */
sealed class NavigationAction {
    /**
     * Действие перехода на экран добавления транзакции
     */
    object AddTransaction : NavigationAction()
    
    /**
     * Действие перехода на экран редактирования транзакции
     * @param transactionId ID транзакции для редактирования
     */
    data class EditTransaction(val transactionId: String) : NavigationAction()
    
    /**
     * Действие перехода на экран истории транзакций
     */
    object TransactionHistory : NavigationAction()
    
    /**
     * Действие перехода на экран аналитики
     */
    object Analytics : NavigationAction()
    
    /**
     * Действие перехода на экран бюджета
     */
    object Budget : NavigationAction()
    
    /**
     * Действие перехода на экран настроек
     */
    object Settings : NavigationAction()
    
    /**
     * Действие перехода на экран импорта транзакций
     */
    object ImportTransactions : NavigationAction()
    
    /**
     * Действие перехода на экран кошельков
     */
    object Wallets : NavigationAction()
    
    /**
     * Действие перехода на экран категорий
     */
    object Categories : NavigationAction()
    
    /**
     * Действие перехода на главный экран
     */
    object Home : NavigationAction()
    
    /**
     * Действие перехода на экран профиля
     */
    object Profile : NavigationAction()
    
    /**
     * Действие перехода на экран достижений
     */
    object Achievements : NavigationAction()
    
    /**
     * Действие перехода на экран онбординга
     */
    object Onboarding : NavigationAction()
    
    /**
     * Преобразует действие в команду навигации
     * @return Команда навигации
     */
    fun toNavigationCommand(): NavigationCommand {
        return when (this) {
            is AddTransaction -> NavigationCommand.NavigateTo(NavRoutes.ADD_TRANSACTION)
            is EditTransaction -> NavigationCommand.NavigateTo(NavRoutes.editTransaction(transactionId))
            is TransactionHistory -> NavigationCommand.NavigateTo(NavRoutes.TRANSACTION_HISTORY)
            is Analytics -> NavigationCommand.NavigateTo(NavRoutes.ANALYTICS)
            is Budget -> NavigationCommand.NavigateTo(NavRoutes.BUDGET)
            is Settings -> NavigationCommand.NavigateTo(NavRoutes.SETTINGS)
            is ImportTransactions -> NavigationCommand.NavigateTo(NavRoutes.IMPORT_TRANSACTIONS)
            is Wallets -> NavigationCommand.NavigateTo(NavRoutes.WALLETS)
            is Categories -> NavigationCommand.NavigateTo(NavRoutes.CATEGORIES)
            is Home -> NavigationCommand.NavigateTo(
                route = NavRoutes.HOME,
                popUpToRoute = NavRoutes.HOME,
                inclusive = true,
                isSingleTop = true
            )
            is Profile -> NavigationCommand.NavigateTo(NavRoutes.PROFILE)
            is Achievements -> NavigationCommand.NavigateTo(NavRoutes.ACHIEVEMENTS)
            is Onboarding -> NavigationCommand.NavigateTo(NavRoutes.ONBOARDING)
        }
    }
}

/**
 * Расширение для выполнения типизированных действий навигации
 */
suspend fun NavigationManager.navigateTo(action: NavigationAction) {
    navigate(action.toNavigationCommand())
}

/**
 * Расширение для выполнения команд с использованием NavController
 */
fun NavController.handleNavigationCommand(command: NavigationCommand) {
    when (command) {
        is NavigationCommand.NavigateTo -> {
            navigate(command.route) {
                command.popUpToRoute?.let { route ->
                    popUpTo(route) {
                        inclusive = command.inclusive
                    }
                }
                launchSingleTop = command.isSingleTop
            }
        }
        is NavigationCommand.NavigateBack -> {
            popBackStack()
        }
        is NavigationCommand.NavigateToRoot -> {
            popBackStack(graph.startDestinationId, false)
        }
    }
} 