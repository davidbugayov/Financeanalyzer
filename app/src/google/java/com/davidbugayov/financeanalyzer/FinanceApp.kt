package com.davidbugayov.financeanalyzer

import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

/**
 * Основной класс приложения для Google flavor
 */
class FinanceApp : BaseFinanceApp() {
    override fun initFlavor() {
        AnalyticsUtils.logAppOpen()
    }

    override fun onCreate() {
        val config = AppMetricaConfig.newConfigBuilder(BuildConfig.APPMETRICA_API_KEY)
            .withLogs()
            .withSessionTimeout(60)
            .withCrashReporting(true)
            .build()
        AppMetrica.activate(this, config)
        AppMetrica.enableActivityAutoTracking(this)
        super.onCreate()
    }
}
