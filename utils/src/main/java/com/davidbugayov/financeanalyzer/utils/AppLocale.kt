package com.davidbugayov.financeanalyzer.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Утилита для применения локали приложения на лету.
 */
object AppLocale {

    fun apply(lang: String) {
        val tag = when (lang.lowercase(Locale.ROOT)) {
            "en" -> Locale.ENGLISH.toLanguageTag()
            "zh" -> Locale.SIMPLIFIED_CHINESE.toLanguageTag()
            else -> Locale("ru").toLanguageTag()
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}
