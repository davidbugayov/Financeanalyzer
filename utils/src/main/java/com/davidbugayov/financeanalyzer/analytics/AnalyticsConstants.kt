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

        // События пользователя
        const val USER_ENGAGEMENT = "user_engagement"
        const val USER_FEEDBACK = "user_feedback"
        const val USER_RATING = "user_rating"

        // События достижений
        const val ACHIEVEMENTS_SCREEN_VIEWED = "achievements_screen_viewed"
        const val ACHIEVEMENT_UNLOCKED = "achievement_unlocked"
        const val ACHIEVEMENT_FILTER_CHANGED = "achievement_filter_changed"
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
    }

    /**
     * Значения параметров
     */
    object Values {
        // Типы транзакций
        const val TRANSACTION_TYPE_EXPENSE = "expense"
        const val TRANSACTION_TYPE_INCOME = "income"

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
    }
}
