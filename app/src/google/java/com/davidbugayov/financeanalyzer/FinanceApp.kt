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
        super.onCreate()
    }
}
