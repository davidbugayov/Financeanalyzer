package com.davidbugayov.financeanalyzer.analytics

/**
 * Централизованные константы для аналитики.
 * Содержит только используемые имена событий и параметров.
 */
object AnalyticsConstants {
    /**
     * События аналитики
     */
    object Events {
        // Общие события
        const val APP_OPEN = "app_open"
        const val APP_CLOSE = "app_close"
        const val APP_BACKGROUND = "app_background"
        const val APP_FOREGROUND = "app_foreground"
        const val SCREEN_VIEW = "screen_view"
        const val SCREEN_LOAD = "screen_load"
        const val SETTINGS_CHANGED = "settings_changed"

        // События транзакций
        const val TRANSACTION_ADDED = "transaction_added"
        const val TRANSACTION_EDITED = "transaction_edited"
        const val TRANSACTION_DELETED = "transaction_deleted"
        const val TRANSACTION_FILTERED = "transaction_filtered"
        const val TRANSACTION_SEARCHED = "transaction_searched"
        const val TRANSACTION_IMPORTED = "transaction_imported"
        const val TRANSACTION_EXPORT_STARTED = "transaction_export_started"
        const val TRANSACTION_EXPORT_COMPLETED = "transaction_export_completed"
        const val TRANSACTION_EXPORT_FAILED = "transaction_export_failed"

        // События категорий
        const val CATEGORY_ADDED = "category_added"
        const val CATEGORY_EDITED = "category_edited"
        const val CATEGORY_DELETED = "category_deleted"
        const val CATEGORY_SELECTED = "category_selected"

        // События бюджета
        const val BUDGET_CREATED = "budget_created"
        const val BUDGET_UPDATED = "budget_updated"
        const val BUDGET_LIMIT_REACHED = "budget_limit_reached"

        // События отчетов
        const val REPORT_GENERATED = "report_generated"
        const val REPORT_SHARED = "report_shared"

        // События ошибок
        const val ERROR = "error"
        const val APP_ERROR = "app_error"
        const val APP_EXCEPTION = "app_exception"
        const val APP_CRASH = "app_crash"
        const val VALIDATION_ERROR = "validation_error"
        const val NETWORK_ERROR = "network_error"
        const val DATABASE_ERROR = "database_error"

        // События производительности
        const val USER_ACTION = "user_action"
        const val DATABASE_OPERATION = "database_operation"
        const val BACKGROUND_TASK = "background_task"
        const val MEMORY_USAGE = "memory_usage"
        const val NETWORK_CALL = "network_call"
        const val OPERATION_COMPLETED = "operation_completed"

        // События функций
        const val FEATURE_USED = "feature_used"
        
        // Основные пользовательские действия (Product Analytics)
        const val SCREEN_STATISTICS_VIEWED = "screen_statistics_viewed"
        const val SCREEN_HISTORY_VIEWED = "screen_history_viewed"
        const val SCREEN_PROFILE_VIEWED = "screen_profile_viewed"
        const val SCREEN_ADD_TRANSACTION_VIEWED = "screen_add_transaction_viewed"
        const val SCREEN_EDIT_TRANSACTION_VIEWED = "screen_edit_transaction_viewed"
        
        // Действия с транзакциями
        const val TRANSACTION_ADD_STARTED = "transaction_add_started"
        const val TRANSACTION_ADD_COMPLETED = "transaction_add_completed"
        const val TRANSACTION_ADD_CANCELLED = "transaction_add_cancelled"
        const val TRANSACTION_EDIT_STARTED = "transaction_edit_started"
        const val TRANSACTION_EDIT_COMPLETED = "transaction_edit_completed"
        const val TRANSACTION_EDIT_CANCELLED = "transaction_edit_cancelled"
        
        // Навигация между экранами
        const val NAVIGATION_TO_STATISTICS = "navigation_to_statistics"
        const val NAVIGATION_TO_HISTORY = "navigation_to_history"
        const val NAVIGATION_TO_PROFILE = "navigation_to_profile"
        const val NAVIGATION_TO_ADD_TRANSACTION = "navigation_to_add_transaction"
        
        // Взаимодействие с элементами UI
        const val BUTTON_CLICKED = "button_clicked"
        const val CARD_CLICKED = "card_clicked"
        const val FILTER_APPLIED = "filter_applied"
        const val SEARCH_PERFORMED = "search_performed"

        // События пользователя
        const val USER_ENGAGEMENT = "user_engagement"
        const val USER_FEEDBACK = "user_feedback"
        const val USER_RATING = "user_rating"

        // События достижений
        const val ACHIEVEMENTS_SCREEN_VIEWED = "achievements_screen_viewed"
        const val ACHIEVEMENT_UNLOCKED = "achievement_unlocked"
        const val ACHIEVEMENT_FILTER_CHANGED = "achievement_filter_changed"

        // События безопасности
        const val SECURITY_AUTH_SCREEN_VIEWED = "security_auth_screen_viewed"
        const val SECURITY_AUTH_SUCCESS = "security_auth_success"
        const val SECURITY_AUTH_FAILED = "security_auth_failed"
        const val SECURITY_APP_LOCK_ENABLED = "security_app_lock_enabled"
        const val SECURITY_APP_LOCK_DISABLED = "security_app_lock_disabled"
        const val SECURITY_BIOMETRIC_ENABLED = "security_biometric_enabled"
        const val SECURITY_BIOMETRIC_DISABLED = "security_biometric_disabled"
        const val SECURITY_PIN_SETUP = "security_pin_setup"
        const val SECURITY_PIN_CHANGED = "security_pin_changed"
    }

    /**
     * Параметры аналитики
     */
    object Params {
        // Общие параметры
        const val SCREEN_NAME = "screen_name"
        const val SCREEN_CLASS = "screen_class"
        const val SOURCE = "source"
        const val DESTINATION = "destination"
        const val SESSION_ID = "session_id"
        const val DURATION_MS = "duration_ms"

        // Параметры транзакций
        const val TRANSACTION_TYPE = "transaction_type"
        const val TRANSACTION_AMOUNT = "transaction_amount"
        const val TRANSACTION_CATEGORY = "transaction_category"
        const val TRANSACTION_COUNT = "transaction_count"
        const val HAS_DESCRIPTION = "has_description"
        const val IMPORT_SOURCE = "import_source"
        const val EXPORT_FORMAT = "export_format"
        const val FILTER_APPLIED = "filter_applied"

        // Параметры категорий
        const val CATEGORY_NAME = "category_name"
        const val CATEGORY_OLD_NAME = "category_old_name"
        const val CATEGORY_TYPE = "category_type"

        // Параметры бюджета
        const val BUDGET_AMOUNT = "budget_amount"
        const val BUDGET_CATEGORY = "budget_category"
        const val BUDGET_PERIOD = "budget_period"
        const val BUDGET_PROGRESS = "budget_progress"

        // Параметры периода
        const val PERIOD_TYPE = "period_type"
        const val PERIOD_START = "period_start"
        const val PERIOD_END = "period_end"

        // Параметры отчетов
        const val REPORT_FORMAT = "report_format"
        const val REPORT_SHARE_METHOD = "report_share_method"

        // Параметры настроек
        const val SETTING_NAME = "setting_name"
        const val SETTING_VALUE = "setting_value"
        const val SETTING_PREVIOUS_VALUE = "setting_previous_value"

        // Параметры ошибок
        const val ERROR_TYPE = "error_type"
        const val ERROR_MESSAGE = "error_message"
        const val ERROR_CODE = "error_code"
        const val STACK_TRACE = "stack_trace"
        const val IS_FATAL = "is_fatal"
        const val VALIDATION_FIELD = "validation_field"
        const val OPERATION_NAME = "operation_name"

        // Параметры производительности
        const val ACTION_NAME = "action_name"
        const val MEMORY_USAGE_MB = "memory_usage_mb"
        const val MEMORY_TOTAL = "memory_total"
        const val MEMORY_AVAILABLE = "memory_available"
        const val FRAME_RATE = "frame_rate"
        const val FRAME_DROP_COUNT = "frame_drop_count"

        // Параметры устройства и приложения
        const val APP_VERSION = "app_version"
        const val APP_VERSION_CODE = "app_version_code"
        const val DEVICE_MODEL = "device_model"
        const val ANDROID_VERSION = "android_version"

        // Параметры функций
        const val FEATURE_NAME = "feature_name"
        const val FEATURE_RESULT = "feature_result"
        const val FEATURE_USAGE_COUNT = "feature_usage_count"
        
        // Параметры основных действий (Product Analytics)
        const val ACTION_TYPE = "action_type"
        const val ELEMENT_NAME = "element_name"
        const val ELEMENT_TYPE = "element_type"
        const val NAVIGATION_SOURCE = "navigation_source"
        const val NAVIGATION_DESTINATION = "navigation_destination"
        const val SESSION_DURATION = "session_duration"
        const val TIME_SPENT_ON_SCREEN = "time_spent_on_screen"
        
        // Параметры транзакций для аналитики
        const val TRANSACTION_AMOUNT_RANGE = "transaction_amount_range"
        const val TRANSACTION_CATEGORY_TYPE = "transaction_category_type"
        const val TRANSACTION_HAS_NOTE = "transaction_has_note"
        const val TRANSACTION_SOURCE = "transaction_source"
        const val TRANSACTION_EDIT_REASON = "transaction_edit_reason"

        // Параметры пользователя
        const val USER_ENGAGEMENT_TIME = "user_engagement_time"
        const val USER_FEEDBACK_SCORE = "user_feedback_score"
        const val USER_FEEDBACK_TEXT = "user_feedback_text"
        const val USER_RATING = "user_rating"

        // Параметры достижений
        const val ACHIEVEMENT_ID = "achievement_id"
        const val ACHIEVEMENT_TITLE = "achievement_title"
        const val ACHIEVEMENT_CATEGORY = "achievement_category"
        const val ACHIEVEMENT_RARITY = "achievement_rarity"
        const val ACHIEVEMENT_REWARD_COINS = "achievement_reward_coins"
        const val ACHIEVEMENTS_TOTAL_COUNT = "achievements_total_count"
        const val ACHIEVEMENTS_UNLOCKED_COUNT = "achievements_unlocked_count"
        const val ACHIEVEMENTS_LOCKED_COUNT = "achievements_locked_count"
        const val ACHIEVEMENT_FILTER_TYPE = "achievement_filter_type"
        const val TOTAL_COINS_EARNED = "total_coins_earned"

        // Параметры безопасности
        const val AUTH_METHOD = "auth_method"
        const val AUTH_RESULT = "auth_result"
        const val BIOMETRIC_SUPPORTED = "biometric_supported"
        const val BIOMETRIC_ENROLLED = "biometric_enrolled"
        const val HAS_PIN_CODE = "has_pin_code"
        const val SECURITY_FEATURE = "security_feature"
        const val PREVIOUS_STATE = "previous_state"
        const val NEW_STATE = "new_state"
    }

    /**
     * Значения параметров
     */
    object Values {
        // Типы транзакций
        const val TRANSACTION_TYPE_EXPENSE = "expense"
        const val TRANSACTION_TYPE_INCOME = "income"
        
        // Основные экраны приложения
        const val SCREEN_HOME = "home"
        const val SCREEN_STATISTICS = "statistics"
        const val SCREEN_HISTORY = "history"
        const val SCREEN_PROFILE = "profile"
        const val SCREEN_ADD_TRANSACTION = "add_transaction"
        const val SCREEN_EDIT_TRANSACTION = "edit_transaction"
        
        // Типы действий
        const val ACTION_VIEW = "view"
        const val ACTION_CLICK = "click"
        const val ACTION_NAVIGATE = "navigate"
        const val ACTION_ADD = "add"
        const val ACTION_EDIT = "edit"
        const val ACTION_DELETE = "delete"
        const val ACTION_CANCEL = "cancel"
        
        // Типы элементов UI
        const val ELEMENT_BUTTON = "button"
        const val ELEMENT_CARD = "card"
        const val ELEMENT_FILTER = "filter"
        const val ELEMENT_SEARCH = "search"
        const val ELEMENT_NAVIGATION = "navigation"
        
        // Диапазоны сумм транзакций
        const val AMOUNT_RANGE_SMALL = "small" // 0-1000
        const val AMOUNT_RANGE_MEDIUM = "medium" // 1000-10000
        const val AMOUNT_RANGE_LARGE = "large" // 10000+
        
        // Источники транзакций
        const val SOURCE_MANUAL = "manual"
        const val SOURCE_IMPORT = "import"
        const val SOURCE_QUICK_ADD = "quick_add"

        // Типы категорий
        const val CATEGORY_TYPE_EXPENSE = "expense"
        const val CATEGORY_TYPE_INCOME = "income"

        // Результаты операций
        const val RESULT_SUCCESS = "success"
        const val RESULT_FAILURE = "failure"

        // Типы источников
        const val SOURCE_USER = "user"

        // Типы ошибок
        const val ERROR_TYPE_NETWORK = "network"
        const val ERROR_TYPE_DATABASE = "database"
        const val ERROR_TYPE_VALIDATION = "validation"

        // Типы фильтров достижений
        const val ACHIEVEMENT_FILTER_ALL = "all"
        const val ACHIEVEMENT_FILTER_UNLOCKED = "unlocked"
        const val ACHIEVEMENT_FILTER_LOCKED = "locked"

        // Методы аутентификации
        const val AUTH_METHOD_PIN = "pin"
        const val AUTH_METHOD_BIOMETRIC = "biometric"
        const val AUTH_METHOD_AUTO = "auto"

        // Результаты аутентификации
        const val AUTH_RESULT_SUCCESS = "success"
        const val AUTH_RESULT_FAILED = "failed"
        const val AUTH_RESULT_CANCELLED = "cancelled"
        const val AUTH_RESULT_ERROR = "error"

        // Функции безопасности
        const val SECURITY_FEATURE_APP_LOCK = "app_lock"
        const val SECURITY_FEATURE_BIOMETRIC = "biometric"
        const val SECURITY_FEATURE_PIN = "pin"
    }
}
