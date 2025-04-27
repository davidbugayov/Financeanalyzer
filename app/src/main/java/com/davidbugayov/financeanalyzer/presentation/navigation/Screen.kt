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
    
    /** Экран редактирования существующей транзакции */
    data object EditTransaction : Screen("edit/{transactionId}") {
        fun createRoute(transactionId: String) = "edit/$transactionId"
    }

    /** Экран с графиками и статистикой */
    data object Chart : Screen("chart")

    /** Экран с подробной финансовой статистикой */
    data object FinancialStatistics : Screen("financial_statistics")
    
    /** Экран профиля пользователя */
    data object Profile : Screen("profile")
    
    /** Экран со списком используемых библиотек */
    data object Libraries : Screen("libraries")

    /** Экран импорта транзакций */
    data object ImportTransactions : Screen("import")

    /** Экран бюджета */
    data object Budget : Screen("budget")

    
    /** Экран кошельков */
    data object Wallets : Screen("wallets")

    /** Экран транзакций кошелька */
    data object WalletTransactions : Screen("wallet/{walletId}") {
        fun createRoute(walletId: String) = "wallet/$walletId"
    }

    /** Экран экспорт и импорт данных */
    data object ExportImport : Screen("export_import")
} 