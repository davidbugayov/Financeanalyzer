package com.davidbugayov.financeanalyzer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
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

    @Suppress("DEPRECATION")
    @SuppressLint("AppCompatMethod")
    fun updateContextLocale(context: Context, lang: String): Context {
        val locale = when (lang.lowercase(Locale.ROOT)) {
            "en" -> Locale.ENGLISH
            "zh" -> Locale.SIMPLIFIED_CHINESE
            else -> Locale("ru")
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}


