package com.davidbugayov.financeanalyzer.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import timber.log.Timber

/**
 * Утилита для применения локали приложения на лету.
 */
object AppLocale {

    fun apply(lang: String) {
        val normalized = lang.lowercase(Locale.ROOT)
        val tag = when (normalized) {
            "en" -> Locale.ENGLISH.toLanguageTag()
            "zh" -> Locale.SIMPLIFIED_CHINESE.toLanguageTag()
            else -> Locale("ru").toLanguageTag()
        }
        Timber.tag("LANG").d("AppLocale.apply: lang=%s normalized=%s tag=%s", lang, normalized, tag)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        Timber.tag("LANG").d(
            "AppLocale.apply: applied. AppLocales=%s, defaultLocale=%s",
            current,
            Locale.getDefault().toLanguageTag(),
        )
    }
}
