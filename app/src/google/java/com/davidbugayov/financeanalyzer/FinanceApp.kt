package com.davidbugayov.financeanalyzer

import com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsProviderBridge

/**
 * Основной класс приложения для Google flavor
 */
class FinanceApp : BaseFinanceApp() {
    override fun initFlavor() {
        AnalyticsProviderBridge.getProvider()?.logAppOpen()
    }

    override fun onCreate() {
        super.onCreate()
    }
}
