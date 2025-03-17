package com.davidbugayov.financeanalyzer.presentation.navigation

/**
 * Sealed класс для определения экранов в навигации приложения.
 * Каждый объект представляет отдельный экран с уникальным маршрутом.
 *
 * @property route Уникальный маршрут для навигации к экрану
 */
sealed class Screen(val route: String) {

    /** Главный экран с балансом и последними транзакциями */
    data object Home : Screen("home")

    /** Экран истории транзакций с фильтрами и группировкой */
    data object History : Screen("history")

    /** Экран добавления новой транзакции */
    data object AddTransaction : Screen("add")

    /** Экран с графиками и статистикой */
    data object Chart : Screen("chart")
    
    /** Экран профиля пользователя */
    data object Profile : Screen("profile")
    
    /** Экран со списком используемых библиотек */
    data object Libraries : Screen("libraries")
} 