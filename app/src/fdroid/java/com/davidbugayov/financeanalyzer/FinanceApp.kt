package com.davidbugayov.financeanalyzer

import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils

/**
 * Основной класс приложения для F-Droid flavor
 */
class FinanceApp : BaseFinanceApp() {

    /**
     * Инициализирует компоненты, специфичные для F-Droid flavor
     */
    override fun initFlavor() {
        AnalyticsUtils.logAppOpen()
    }
}
