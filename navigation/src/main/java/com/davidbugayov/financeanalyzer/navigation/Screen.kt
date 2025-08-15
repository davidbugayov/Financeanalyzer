package com.davidbugayov.financeanalyzer.navigation

/**
 * Sealed класс для определения экранов в навигации приложения.
 * Каждый объект представляет отдельный экран с уникальным маршрутом.
 *
 * @property route Уникальный маршрут для навигации к экрану
 */
sealed class Screen(
    val route: String,
) {
    /** Экран онбординга для новых пользователей */
    data object Onboarding : Screen("onboarding")

    /** Экран аутентификации */
    data object Auth : Screen("auth")

    /** Главный экран с балансом и последними транзакциями */
    data object Home : Screen("home")

    /** Экран истории транзакций с фильтрами и группировкой */
    data object History : Screen("history")

    /** Экран добавления новой транзакции */
    data object AddTransaction : Screen("add") {
        // Константы для аргументов
        const val CATEGORY_ARG = "category"
        const val FORCE_EXPENSE_ARG = "forceExpense"

        const val routeWithArgs: String = "add?$CATEGORY_ARG={$CATEGORY_ARG}&$FORCE_EXPENSE_ARG={$FORCE_EXPENSE_ARG}"

        fun createRoute(
            category: String? = null,
            forceExpense: Boolean? = null,
        ): String {
            val cat = category ?: ""
            val force = forceExpense?.toString() ?: ""
            return "add?category=$cat&forceExpense=$force"
        }
    }

    // Общие константы для аргументов навигации
    companion object {
        const val TRANSACTION_ID_ARG = "transactionId"
        const val WALLET_ID_ARG = "walletId"
        const val START_DATE_ARG = "startDate"
        const val END_DATE_ARG = "endDate"
        const val PERIOD_TYPE_ARG = "periodType"
    }

    /** Экран редактирования существующей транзакции */
    data object EditTransaction : Screen("edit/{$TRANSACTION_ID_ARG}") {
        fun createRoute(transactionId: String) = "edit/$transactionId"
    }

    /** Экран с подробной финансовой статистикой */
    data object FinancialStatistics : Screen(
        "financial_statistics?$START_DATE_ARG={$START_DATE_ARG}&$END_DATE_ARG={$END_DATE_ARG}&$PERIOD_TYPE_ARG={$PERIOD_TYPE_ARG}",
    ) {
        fun createRoute(
            startDate: Long?,
            endDate: Long?,
            periodType: String? = null,
        ): String {
            val start = startDate ?: System.currentTimeMillis()
            val end = endDate ?: System.currentTimeMillis()
            return if (periodType != null) {
                "financial_statistics?$START_DATE_ARG=$start&$END_DATE_ARG=$end&$PERIOD_TYPE_ARG=$periodType"
            } else {
                "financial_statistics?$START_DATE_ARG=$start&$END_DATE_ARG=$end"
            }
        }
    }

    /** Экран с детальной финансовой статистикой (подробный анализ) */
    data object DetailedFinancialStatistics : Screen(
        "detailed_financial_statistics?$START_DATE_ARG={$START_DATE_ARG}&$END_DATE_ARG={$END_DATE_ARG}",
    ) {
        fun createRoute(
            startDate: Long?,
            endDate: Long?,
        ): String {
            val start = startDate ?: System.currentTimeMillis()
            val end = endDate ?: System.currentTimeMillis()
            return "detailed_financial_statistics?$START_DATE_ARG=$start&$END_DATE_ARG=$end"
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
    data object WalletTransactions : Screen("wallet/{$WALLET_ID_ARG}") {
        fun createRoute(walletId: String) = "wallet/$walletId"
    }

    /** Экран экспорт и импорт данных */
    data object ExportImport : Screen("export_import")

    /** Экран достижений */
    data object Achievements : Screen("achievements")

    /** Экран мастера создания кошелька */
    data object WalletSetup : Screen("wallet_setup")

    /** Экран учета долгов */
    data object Debts : Screen("debts")

    /** Экран добавления долга */
    data object AddDebt : Screen("debt_add")

    data class SubWallets(
        val parentWalletId: String,
    ) : Screen("sub_wallets/{parentWalletId}") {
        companion object {
            const val route = "sub_wallets/{parentWalletId}"
        }
    }
}
