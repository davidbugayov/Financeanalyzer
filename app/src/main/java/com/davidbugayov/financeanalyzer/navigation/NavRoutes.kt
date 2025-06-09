package com.davidbugayov.financeanalyzer.navigation

/**
 * Константы маршрутов навигации в приложении.
 * Используются для избежания дублирования строк и ошибок.
 */
object NavRoutes {
    const val HOME = "home"
    const val ADD_TRANSACTION = "add_transaction"
    const val EDIT_TRANSACTION = "edit_transaction/{transactionId}"
    const val EDIT_TRANSACTION_BASE = "edit_transaction"
    const val TRANSACTION_HISTORY = "transaction_history"
    const val ANALYTICS = "analytics"
    const val BUDGET = "budget"
    const val SETTINGS = "settings"
    const val IMPORT_TRANSACTIONS = "import_transactions"
    const val WALLETS = "wallets"
    const val CATEGORIES = "categories"
    const val PROFILE = "profile"
    const val ACHIEVEMENTS = "achievements"
    const val ONBOARDING = "onboarding"

    /**
     * Аргументы, используемые в маршрутах навигации.
     */
    object Args {
        const val TRANSACTION_ID = "transactionId"
        const val START_DATE = "startDate"
        const val END_DATE = "endDate"
        const val WALLET_ID = "walletId"
        const val CATEGORY_ID = "categoryId"
    }
    
    /**
     * Создает маршрут для редактирования транзакции с указанным ID.
     * @param transactionId ID транзакции
     * @return Полный маршрут для навигации
     */
    fun editTransaction(transactionId: String): String {
        return "$EDIT_TRANSACTION_BASE/$transactionId"
    }
} 