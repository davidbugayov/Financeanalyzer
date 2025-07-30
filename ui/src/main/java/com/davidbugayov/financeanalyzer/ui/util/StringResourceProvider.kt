package com.davidbugayov.financeanalyzer.ui.util

import android.content.Context
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Утилитный класс для получения строковых ресурсов в ViewModel'ях и других местах,
 * где нет прямого доступа к контексту.
 */
object StringResourceProvider {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun getString(
        @androidx.annotation.StringRes resId: Int,
    ): String {
        return context?.getString(resId) ?: "String not found"
    }

    fun getString(
        @androidx.annotation.StringRes resId: Int,
        vararg formatArgs: Any,
    ): String {
        return context?.getString(resId, *formatArgs) ?: "String not found"
    }

    // Строки для диалогов
    val dialogDeleteTitle: String get() = getString(R.string.dialog_delete_title)
    val dialogCancel: String get() = getString(R.string.dialog_cancel)
    val dialogConfirm: String get() = getString(R.string.dialog_confirm)
    val dialogApply: String get() = getString(R.string.dialog_apply)
    val dialogClose: String get() = getString(R.string.dialog_close)

    // Строки для периодов
    val periodAllTime: String get() = getString(R.string.period_all_time)
    val periodDay: String get() = getString(R.string.period_day)
    val periodWeek: String get() = getString(R.string.period_week)
    val periodMonth: String get() = getString(R.string.period_month)
    val periodQuarter: String get() = getString(R.string.period_quarter)
    val periodYear: String get() = getString(R.string.period_year)
    val periodCustom: String get() = getString(R.string.period_custom)

    // Строки для ошибок
    val errorUnknown: String get() = getString(R.string.error_unknown)
    val errorUnknownType: String get() = getString(R.string.error_unknown_type)

    fun errorActionFailed(message: String): String = getString(R.string.error_action_failed, message)

    // Строки для достижений
    val achievementsOverallProgress: String get() = getString(R.string.achievements_overall_progress)
    val achievementsProgress: String get() = getString(R.string.achievements_progress)
    val achievementsUnlocked: String get() = getString(R.string.achievements_unlocked)
    val achievementsFilterAll: String get() = getString(R.string.achievements_filter_all)
    val achievementsFilterUnlocked: String get() = getString(R.string.achievements_filter_unlocked)
    val achievementsFilterLocked: String get() = getString(R.string.achievements_filter_locked)
} 
