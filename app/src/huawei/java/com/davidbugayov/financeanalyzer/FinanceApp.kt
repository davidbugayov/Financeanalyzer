package com.davidbugayov.financeanalyzer

import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

/**
 * Основной класс приложения для Huawei flavor
 */
class FinanceApp : BaseFinanceApp() {
    /**
     * Инициализирует компоненты, специфичные для Huawei flavor
     */
    override fun initFlavor() {
        AnalyticsUtils.logAppOpen()
    }

    override fun onCreate() {
        super.onCreate()
    }
} 
