package com.davidbugayov.financeanalyzer

import com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsProviderBridge

/**
 * Основной класс приложения для Huawei flavor
 */
class FinanceApp : BaseFinanceApp() {
    /**
     * Инициализирует компоненты, специфичные для Huawei flavor
     */
    override fun initFlavor() {
        AnalyticsProviderBridge.getProvider()?.logAppOpen()
    }

    override fun onCreate() {
        super.onCreate()
    }
}
