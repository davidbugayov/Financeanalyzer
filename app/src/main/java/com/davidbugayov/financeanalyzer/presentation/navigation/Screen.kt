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
    data object AddTransaction : Screen("add") {
        const val routeWithArgs: String = "add?category={category}"
        const val categoryArg: String = "category"
        fun createRoute(category: String? = null): String {
            return if (category != null) {
                "add?category=$category"
            } else {
                "add"
            }
        }
    }

    /** Экран редактирования существующей транзакции */
    data object EditTransaction : Screen("edit/{transactionId}") {
        fun createRoute(transactionId: String) = "edit/$transactionId"
    }

    /** Экран с подробной финансовой статистикой */
    data object FinancialStatistics : Screen("financial_statistics?startDate={startDate}&endDate={endDate}") {
        fun createRoute(startDate: Long?, endDate: Long?): String {
            val start = startDate ?: System.currentTimeMillis()
            val end = endDate ?: System.currentTimeMillis()
            return "financial_statistics?startDate=$start&endDate=$end"
        }
    }

    /** Экран профиля пользователя */
    data object Profile : Screen("profile")

    /** Экран со списком используемых библиотек */
    data object Libraries : Screen("libraries")

    /** Экран импорта транзакций */
    data object ImportTransactions : Screen("import")

    /** Экран бюджета */
    data object Budget : Screen("budget")

    /** Экран транзакций кошелька */
    data object WalletTransactions : Screen("wallet/{walletId}") {
        fun createRoute(walletId: String) = "wallet/$walletId"
    }

    /** Экран экспорт и импорт данных */
    data object ExportImport : Screen("export_import")

    /** Экран достижений */
    data object Achievements : Screen("achievements")
}
