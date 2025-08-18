package com.davidbugayov.financeanalyzer.ui.utils

import android.content.Context
import com.davidbugayov.financeanalyzer.shared.util.SourceLocalizationKmp
import com.davidbugayov.financeanalyzer.shared.util.SourceResolver

/**
 * Обёртка над KMP-локализацией источников: на Android прокидываем текущую локаль.
 */
object SourceLocalization {
    fun displayName(
        context: Context,
        raw: String,
    ): String {
        val key = SourceResolver.resolve(raw)
        val localeTag = context.resources.configuration.locales[0]?.toLanguageTag() ?: "en"
        val localized = SourceLocalizationKmp.localize(key, localeTag.lowercase())
        return if (localized.isBlank()) raw else localized
    }
}
