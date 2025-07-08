package com.davidbugayov.financeanalyzer.analytics

/**
 * Централизованные константы для аналитики.
 * Содержит имена событий и параметров, используемых в аналитике.
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
        const val USER_PROPERTY_CHANGE = "user_property_change"
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
        const val CATEGORY_FILTERED = "category_filtered"

        // События бюджета
        const val BUDGET_CREATED = "budget_created"
        const val BUDGET_UPDATED = "budget_updated"
        const val BUDGET_DELETED = "budget_deleted"
        const val BUDGET_LIMIT_REACHED = "budget_limit_reached"
        const val BUDGET_PROGRESS_UPDATED = "budget_progress_updated"

        // События отчетов
        const val REPORT_GENERATED = "report_generated"
        const val REPORT_SHARED = "report_shared"
        const val REPORT_VIEWED = "report_viewed"
        const val REPORT_PERIOD_CHANGED = "report_period_changed"

        // События ошибок
        const val ERROR = "error"
        const val APP_ERROR = "app_error"
        const val APP_EXCEPTION = "app_exception"
        const val APP_CRASH = "app_crash"
        const val VALIDATION_ERROR = "validation_error"
        const val NETWORK_ERROR = "network_error"
        const val DATABASE_ERROR = "database_error"

        // События производительности
        const val PERFORMANCE_METRIC = "performance_metric"
        const val USER_ACTION = "user_action"
        const val SCREEN_LOAD = "screen_load"
        const val DATABASE_OPERATION = "database_operation"
        const val BACKGROUND_TASK = "background_task"
        const val RENDER_TIME = "render_time"
        const val MEMORY_USAGE = "memory_usage"
        const val BATTERY_USAGE = "battery_usage"
        const val OPERATION_COMPLETED = "operation_completed"
        const val NETWORK_CALL = "network_call"

        // События функций
        const val FEATURE_USED = "feature_used"
        const val FEATURE_DISCOVERED = "feature_discovered"
        const val FEATURE_ENABLED = "feature_enabled"
        const val FEATURE_DISABLED = "feature_disabled"

        // События пользователя
        const val USER_ENGAGEMENT = "user_engagement"
        const val USER_FEEDBACK = "user_feedback"
        const val USER_RATING = "user_rating"


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
        const val USER_ID = "user_id"

        // Параметры транзакций
        const val TRANSACTION_TYPE = "transaction_type"
        const val TRANSACTION_AMOUNT = "transaction_amount"
        const val TRANSACTION_CATEGORY = "transaction_category"
        const val TRANSACTION_DATE = "transaction_date"
        const val TRANSACTION_SOURCE = "transaction_source"
        const val TRANSACTION_COUNT = "transaction_count"
        const val TRANSACTION_TAGS = "transaction_tags"
        const val HAS_DESCRIPTION = "has_description"
        const val IS_RECURRING = "is_recurring"
        const val RECURRENCE_TYPE = "recurrence_type"
        const val IMPORT_SOURCE = "import_source"
        const val EXPORT_FORMAT = "export_format"
        const val FILTER_APPLIED = "filter_applied"

        // Параметры категорий
        const val CATEGORY_NAME = "category_name"
        const val CATEGORY_OLD_NAME = "category_old_name"
        const val CATEGORY_TYPE = "category_type"
        const val CATEGORY_COLOR = "category_color"
        const val CATEGORY_ICON = "category_icon"
        const val CATEGORY_COUNT = "category_count"

        // Параметры бюджета
        const val BUDGET_AMOUNT = "budget_amount"
        const val BUDGET_CATEGORY = "budget_category"
        const val BUDGET_PERIOD = "budget_period"
        const val BUDGET_PROGRESS = "budget_progress"
        const val BUDGET_REMAINING = "budget_remaining"
        const val BUDGET_OVERSPENT = "budget_overspent"

        // Параметры периода
        const val PERIOD_TYPE = "period_type"
        const val PERIOD_START = "period_start"
        const val PERIOD_END = "period_end"
        const val PERIOD_DURATION = "period_duration"
        const val PERIOD_COMPARISON = "period_comparison"

        // Параметры отчетов
        const val REPORT_FORMAT = "report_format"
        const val REPORT_TYPE = "report_type"
        const val REPORT_SIZE = "report_size"
        const val REPORT_SHARE_METHOD = "report_share_method"

        // Параметры настроек
        const val SETTING_NAME = "setting_name"
        const val SETTING_VALUE = "setting_value"
        const val SETTING_GROUP = "setting_group"
        const val SETTING_PREVIOUS_VALUE = "setting_previous_value"

        // Параметры ошибок
        const val ERROR_TYPE = "error_type"
        const val ERROR_MESSAGE = "error_message"
        const val ERROR_CODE = "error_code"
        const val STACK_TRACE = "stack_trace"
        const val IS_FATAL = "is_fatal"
        const val VALIDATION_FIELD = "validation_field"

        // Параметры устройства и приложения
        const val APP_VERSION = "app_version"
        const val APP_VERSION_CODE = "app_version_code"
        const val APP_INSTALL_SOURCE = "app_install_source"
        const val APP_FLAVOR = "app_flavor"
        const val APP_BUILD_TYPE = "app_build_type"
        const val DEVICE_MODEL = "device_model"
        const val DEVICE_BRAND = "device_brand"
        const val DEVICE_TYPE = "device_type"
        const val ANDROID_VERSION = "android_version"
        const val SCREEN_SIZE = "screen_size"
        const val SCREEN_DENSITY = "screen_density"
        const val MEMORY_TOTAL = "memory_total"
        const val MEMORY_AVAILABLE = "memory_available"
        const val NETWORK_TYPE = "network_type"
        const val BATTERY_LEVEL = "battery_level"

        // Параметры производительности
        const val DURATION_MS = "duration_ms"
        const val OPERATION_NAME = "operation_name"
        const val ACTION_NAME = "action_name"
        const val MEMORY_USAGE_MB = "memory_usage_mb"
        const val CPU_USAGE = "cpu_usage"
        const val THREAD_COUNT = "thread_count"
        const val DB_QUERY_COUNT = "db_query_count"
        const val FRAME_RATE = "frame_rate"
        const val FRAME_DROP_COUNT = "frame_drop_count"

        // Параметры функций
        const val FEATURE_NAME = "feature_name"
        const val FEATURE_RESULT = "feature_result"
        const val FEATURE_VERSION = "feature_version"
        const val FEATURE_USAGE_COUNT = "feature_usage_count"

        // Параметры пользователя
        const val USER_ENGAGEMENT_TIME = "user_engagement_time"
        const val USER_FEEDBACK_SCORE = "user_feedback_score"
        const val USER_FEEDBACK_TEXT = "user_feedback_text"
        const val USER_RATING = "user_rating"
    }

    /**
     * Значения параметров
     */
    object Values {
        // Типы транзакций
        const val TRANSACTION_TYPE_EXPENSE = "expense"
        const val TRANSACTION_TYPE_INCOME = "income"
        const val TRANSACTION_TYPE_TRANSFER = "transfer"

        // Типы категорий
        const val CATEGORY_TYPE_EXPENSE = "expense"
        const val CATEGORY_TYPE_INCOME = "income"

        // Типы периодов
        const val PERIOD_TYPE_DAY = "day"
        const val PERIOD_TYPE_WEEK = "week"
        const val PERIOD_TYPE_MONTH = "month"
        const val PERIOD_TYPE_QUARTER = "quarter"
        const val PERIOD_TYPE_YEAR = "year"
        const val PERIOD_TYPE_CUSTOM = "custom"
        const val PERIOD_TYPE_ALL = "all_time"

        // Форматы отчетов
        const val REPORT_FORMAT_CSV = "csv"
        const val REPORT_FORMAT_PDF = "pdf"
        const val REPORT_FORMAT_EXCEL = "excel"
        const val REPORT_FORMAT_JSON = "json"

        // Типы отчетов
        const val REPORT_TYPE_SUMMARY = "summary"
        const val REPORT_TYPE_DETAILED = "detailed"
        const val REPORT_TYPE_CATEGORY = "category"
        const val REPORT_TYPE_TREND = "trend"

        // Результаты операций
        const val RESULT_SUCCESS = "success"
        const val RESULT_FAILURE = "failure"
        const val RESULT_CANCELLED = "cancelled"
        const val RESULT_TIMEOUT = "timeout"
        const val RESULT_PARTIAL = "partial"

        // Типы источников
        const val SOURCE_USER = "user"
        const val SOURCE_SYSTEM = "system"
        const val SOURCE_IMPORT = "import"
        const val SOURCE_SYNC = "sync"
        const val SOURCE_RECURRING = "recurring"
        const val SOURCE_WIDGET = "widget"

        // Типы ошибок
        const val ERROR_TYPE_NETWORK = "network"
        const val ERROR_TYPE_DATABASE = "database"
        const val ERROR_TYPE_VALIDATION = "validation"
        const val ERROR_TYPE_PERMISSION = "permission"
        const val ERROR_TYPE_MEMORY = "memory"
        const val ERROR_TYPE_DISK = "disk"

        // Типы устройств
        const val DEVICE_TYPE_PHONE = "phone"
        const val DEVICE_TYPE_TABLET = "tablet"
        const val DEVICE_TYPE_FOLDABLE = "foldable"

        // Типы сети
        const val NETWORK_TYPE_WIFI = "wifi"
        const val NETWORK_TYPE_MOBILE = "mobile"
        const val NETWORK_TYPE_NONE = "none"


    }
}
