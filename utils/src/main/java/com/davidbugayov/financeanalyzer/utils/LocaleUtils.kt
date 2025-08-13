package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale
import timber.log.Timber

/**
 * Утилита для принудительного применения локали к baseContext активности.
 * Нужна как fallback, когда AppCompatDelegate.setApplicationLocales не срабатывает.
 */
object LocaleUtils {
    fun wrapContext(base: Context): Context {
        return try {
            val prefs = PreferencesManager(base)
            val code = prefs.getAppLanguage()
            val locale =
                when (code.lowercase(Locale.ROOT)) {
                    "en" -> Locale.ENGLISH
                    "zh" -> Locale.SIMPLIFIED_CHINESE
                    else -> Locale("ru")
                }
            Timber.tag("LANG").d("LocaleUtils.wrapContext: code=%s locale=%s", code, locale.toLanguageTag())

            val config = Configuration(base.resources.configuration)
            if (Build.VERSION.SDK_INT >= 24) {
                val list = LocaleList(locale)
                config.setLocales(list)
            } else {
                @Suppress("DEPRECATION")
                config.setLocale(locale)
            }
            val wrapped = base.createConfigurationContext(config)
            Timber.tag("LANG").d("LocaleUtils.wrapContext: wrapped context created")
            wrapped
        } catch (e: Exception) {
            Timber.tag("LANG").e(e, "LocaleUtils.wrapContext: failed, returning base")
            base
        }
    }
}
