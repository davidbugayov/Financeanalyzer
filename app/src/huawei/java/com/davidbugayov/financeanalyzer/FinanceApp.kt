package com.davidbugayov.financeanalyzer

import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils

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
} 