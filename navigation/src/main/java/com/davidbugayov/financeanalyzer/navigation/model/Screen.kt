package com.davidbugayov.financeanalyzer.navigation.model

/**
 * Sealed class representing all possible screens in the application.
 * Provides type safety for navigation and prevents navigation to non-existent screens.
 */
sealed class Screen(val route: String) {

    // Main screens
    object Home : Screen("home")
    object Statistics : Screen("statistics")
    object Profile : Screen("profile")
    object Settings : Screen("settings")

    // Transaction screens
    object AddTransaction : Screen("add_transaction")
    data class EditTransaction(val transactionId: Long) : Screen("edit_transaction/$transactionId")
    data class TransactionDetails(val transactionId: Long) : Screen("transaction_details/$transactionId")

    // Category screens
    object Categories : Screen("categories")
    data class CategoryDetails(val categoryId: Long) : Screen("category_details/$categoryId")
    data class AddCategory(val transactionType: String) : Screen("add_category/$transactionType")

    // Wallet screens
    object Wallets : Screen("wallets")
    data class WalletDetails(val walletId: Long) : Screen("wallet_details/$walletId")
    data class AddWallet(val walletType: String? = null) : Screen("add_wallet/${walletType ?: "default"}")

    // History screens
    object TransactionHistory : Screen("transaction_history")
    data class FilteredHistory(val filter: String) : Screen("filtered_history/$filter")

    // Budget screens
    object Budget : Screen("budget")
    data class BudgetDetails(val budgetId: Long) : Screen("budget_details/$budgetId")

    // Achievement screens
    object Achievements : Screen("achievements")
    data class AchievementDetails(val achievementId: String) : Screen("achievement_details/$achievementId")

    // Onboarding screens
    object Welcome : Screen("welcome")
    object OnboardingStep1 : Screen("onboarding_step1")
    object OnboardingStep2 : Screen("onboarding_step2")
    object OnboardingComplete : Screen("onboarding_complete")

    // Utility screens
    object Search : Screen("search")
    data class WebView(val url: String) : Screen("webview/$url")
    object About : Screen("about")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfService : Screen("terms_of_service")

    companion object {
        /**
         * Get screen from route string
         */
        fun fromRoute(route: String?): Screen? {
            return when {
                route == "home" -> Home
                route == "statistics" -> Statistics
                route == "profile" -> Profile
                route == "settings" -> Settings
                route == "add_transaction" -> AddTransaction
                route?.startsWith("edit_transaction/") == true -> {
                    val id = route.substringAfter("edit_transaction/").toLongOrNull()
                    id?.let { EditTransaction(it) }
                }
                route?.startsWith("transaction_details/") == true -> {
                    val id = route.substringAfter("transaction_details/").toLongOrNull()
                    id?.let { TransactionDetails(it) }
                }
                route == "categories" -> Categories
                route?.startsWith("category_details/") == true -> {
                    val id = route.substringAfter("category_details/").toLongOrNull()
                    id?.let { CategoryDetails(it) }
                }
                route?.startsWith("add_category/") == true -> {
                    val type = route.substringAfter("add_category/")
                    AddCategory(type)
                }
                route == "wallets" -> Wallets
                route?.startsWith("wallet_details/") == true -> {
                    val id = route.substringAfter("wallet_details/").toLongOrNull()
                    id?.let { WalletDetails(it) }
                }
                route?.startsWith("add_wallet/") == true -> {
                    val type = route.substringAfter("add_wallet/")
                    AddWallet(if (type == "default") null else type)
                }
                route == "transaction_history" -> TransactionHistory
                route?.startsWith("filtered_history/") == true -> {
                    val filter = route.substringAfter("filtered_history/")
                    FilteredHistory(filter)
                }
                route == "budget" -> Budget
                route?.startsWith("budget_details/") == true -> {
                    val id = route.substringAfter("budget_details/").toLongOrNull()
                    id?.let { BudgetDetails(it) }
                }
                route == "achievements" -> Achievements
                route?.startsWith("achievement_details/") == true -> {
                    val id = route.substringAfter("achievement_details/")
                    AchievementDetails(id)
                }
                route == "welcome" -> Welcome
                route == "onboarding_step1" -> OnboardingStep1
                route == "onboarding_step2" -> OnboardingStep2
                route == "onboarding_complete" -> OnboardingComplete
                route == "search" -> Search
                route?.startsWith("webview/") == true -> {
                    val url = route.substringAfter("webview/")
                    WebView(url)
                }
                route == "about" -> About
                route == "privacy_policy" -> PrivacyPolicy
                route == "terms_of_service" -> TermsOfService
                else -> null
            }
        }

        /**
         * Get all main navigation screens
         */
        fun mainScreens(): List<Screen> = listOf(
            Home,
            Statistics,
            Profile
        )

        /**
         * Check if screen requires authentication
         */
        fun requiresAuth(screen: Screen): Boolean {
            return when (screen) {
                is AddTransaction, is EditTransaction, is TransactionDetails,
                is Categories, is CategoryDetails, is AddCategory,
                is Wallets, is WalletDetails, is AddWallet,
                is TransactionHistory, is FilteredHistory,
                is Budget, is BudgetDetails,
                is Achievements, is AchievementDetails -> true
                else -> false
            }
        }

        /**
         * Get screen display name
         */
        fun getDisplayName(screen: Screen): String {
            return when (screen) {
                Home -> "Главная"
                Statistics -> "Статистика"
                Profile -> "Профиль"
                Settings -> "Настройки"
                AddTransaction -> "Добавить транзакцию"
                is EditTransaction -> "Редактировать транзакцию"
                is TransactionDetails -> "Детали транзакции"
                Categories -> "Категории"
                is CategoryDetails -> "Детали категории"
                is AddCategory -> "Добавить категорию"
                Wallets -> "Кошельки"
                is WalletDetails -> "Детали кошелька"
                is AddWallet -> "Добавить кошелек"
                TransactionHistory -> "История транзакций"
                is FilteredHistory -> "Фильтрованная история"
                Budget -> "Бюджет"
                is BudgetDetails -> "Детали бюджета"
                Achievements -> "Достижения"
                is AchievementDetails -> "Детали достижения"
                Welcome -> "Добро пожаловать"
                OnboardingStep1 -> "Шаг 1"
                OnboardingStep2 -> "Шаг 2"
                OnboardingComplete -> "Завершено"
                Search -> "Поиск"
                is WebView -> "Веб-просмотр"
                About -> "О приложении"
                PrivacyPolicy -> "Политика конфиденциальности"
                TermsOfService -> "Условия использования"
            }
        }
    }
}
